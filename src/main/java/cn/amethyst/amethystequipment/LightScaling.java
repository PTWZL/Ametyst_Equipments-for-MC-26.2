package cn.amethyst.amethystequipment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * Makes the amethyst armour, sword and spear stronger the brighter it is where the
 * player is standing.
 *
 * <h2>Base formulas (x = light level at the player's position, 0..15)</h2>
 * <ul>
 *   <li><b>Armour</b> (per equipped piece): golden armour value {@code + x * 0.8} armour,
 *       and {@code + x * 0.2} knockback resistance.</li>
 *   <li><b>Weapons</b>: base attack damage 6, and {@code + x * 0.6} from the light for the
 *       sword and the spear. The item itself supplies the 6, so only the light part is
 *       dynamic.</li>
 * </ul>
 *
 * <h2>The Refraction infusion</h2>
 * {@code 折光强化 / Refraction} multiplies <em>every</em> per-light coefficient of that item
 * by {@code 1 + 0.5 * level} (level 1 = 1.5x, level 10 = 6x). A level 5 sword therefore
 * gains {@code x * 0.6 * 3.5} attack damage from the light instead of {@code x * 0.6}.
 * See {@link Refraction} for why this is a data component rather than a real enchantment.
 *
 * <p>Light is read with {@code LevelReader.getMaxLocalRawBrightness(BlockPos)}, i.e. block
 * light and sky light are both taken into account and the value is the brightness actually
 * affecting the player (so it drops at night or in a thunderstorm).
 *
 * <p>Only the server tick is hooked: the modifiers stay authoritative on the server and
 * reach the client through the normal attribute synchronisation, so the tooltip
 * ({@code LightTooltip}) reports exactly the same numbers.
 */
public final class LightScaling {
	private LightScaling() {
	}

	/** Armour value granted per light level, per piece, before Refraction. */
	public static final double BASE_ARMOR_PER_LIGHT = 0.8D;
	/** Knockback resistance granted per light level, per piece, before Refraction. */
	public static final double BASE_KNOCKBACK_RESISTANCE_PER_LIGHT = 0.2D;
	/** Attack damage granted per light level, before Refraction (sword and spear). */
	public static final double BASE_ATTACK_DAMAGE_PER_LIGHT = 0.6D;

	/** Coefficient bonus added by each Refraction level: multiplier = 1 + 0.5 * level. */
	public static final double REFRACTION_LEVEL_STEP = 0.5D;
	public static final int REFRACTION_MAX_LEVEL = 10;

	/** The brightest light level, used for the "maximum" figures shown in tooltips. */
	public static final int MAX_LIGHT = 15;

	private static final Identifier ARMOR_MODIFIER_ID = AmethystEquipment.id("light_armor");
	private static final Identifier KNOCKBACK_MODIFIER_ID = AmethystEquipment.id("light_knockback_resistance");
	private static final Identifier DAMAGE_MODIFIER_ID = AmethystEquipment.id("light_attack_damage");

	private static final EquipmentSlot[] ARMOR_SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	/** Bit 0..3 are the four armour slots; bit 4 records a scaling weapon. */
	private static final int ARMOR_FINGERPRINT_MASK = 0b1111;
	private static final int WEAPON_FINGERPRINT_BIT = 0b10000;

	/** What was actually installed last time, so removals only touch live attributes. */
	private record Applied(int light, int fingerprint, int refraction,
			boolean armorModifiers, boolean damageModifier) {
	}

	private static final Map<UUID, Applied> APPLIED = new HashMap<>();

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				refresh(player);
			}
		});
	}

	// ------------------------------------------------------------------
	// Coefficients
	// ------------------------------------------------------------------

	/** The multiplier Refraction applies at the given level: 1 + 0.5 * level. */
	public static double refractionMultiplier(int level) {
		return 1.0D + REFRACTION_LEVEL_STEP * Math.max(0, level);
	}

	public static double armorPerLight(int refraction) {
		return BASE_ARMOR_PER_LIGHT * refractionMultiplier(refraction);
	}

	public static double knockbackResistancePerLight(int refraction) {
		return BASE_KNOCKBACK_RESISTANCE_PER_LIGHT * refractionMultiplier(refraction);
	}

	public static double attackDamagePerLight(int refraction) {
		return BASE_ATTACK_DAMAGE_PER_LIGHT * refractionMultiplier(refraction);
	}

	public static double armorBonus(int light, int refraction) {
		return light * armorPerLight(refraction);
	}

	public static double knockbackResistanceBonus(int light, int refraction) {
		return light * knockbackResistancePerLight(refraction);
	}

	public static double attackDamageBonus(int light, int refraction) {
		return light * attackDamagePerLight(refraction);
	}

	/** The light level used by the formulas at the given position. */
	public static int lightAt(Level level, BlockPos pos) {
		return level.getMaxLocalRawBrightness(pos);
	}

	// ------------------------------------------------------------------
	// Per-player refresh
	// ------------------------------------------------------------------

	/** Applies (or clears) the light-scaled modifiers for one player. */
	public static void refresh(ServerPlayer player) {
		int light = lightAt(player.level(), player.blockPosition());
		int fingerprint = gearFingerprint(player);
		Applied previous = APPLIED.get(player.getUUID());

		if (fingerprint == 0) {
			// Nothing that scales is equipped: drop whatever a previous tick installed.
			remove(player, APPLIED.remove(player.getUUID()));
			return;
		}

		int refraction = refractionFor(player);

		if (previous != null
				&& previous.light() == light
				&& previous.fingerprint() == fingerprint
				&& previous.refraction() == refraction) {
			return;
		}

		remove(player, previous);

		boolean armorEquipped = (fingerprint & ARMOR_FINGERPRINT_MASK) != 0;
		boolean weaponHeld = (fingerprint & WEAPON_FINGERPRINT_BIT) != 0;

		if (armorEquipped) {
			set(player.getAttribute(Attributes.ARMOR), ARMOR_MODIFIER_ID, armorBonus(light, refraction));
			set(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_MODIFIER_ID,
					knockbackResistanceBonus(light, refraction));
		}

		if (weaponHeld) {
			set(player.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE_MODIFIER_ID,
					attackDamageBonus(light, refraction));
		}

		APPLIED.put(player.getUUID(), new Applied(light, fingerprint, refraction, armorEquipped, weaponHeld));
	}

	/**
	 * A cheap fingerprint of which scaling items are equipped. The low bits record the
	 * armour slots (so swapping one amethyst piece for another still triggers a rebuild)
	 * and the next bit records whether a scaling weapon is held.
	 */
	private static int gearFingerprint(ServerPlayer player) {
		int fingerprint = 0;
		int bit = 1;

		for (EquipmentSlot slot : ARMOR_SLOTS) {
			if (isAmethystArmor(player.getItemBySlot(slot))) {
				fingerprint |= bit;
			}

			bit <<= 1;
		}

		if (isLightWeapon(player.getMainHandItem()) || isLightWeapon(player.getOffhandItem())) {
			fingerprint |= bit;
		}

		return fingerprint;
	}

	/** True when the player is wearing at least one amethyst armour piece or holding a weapon. */
	public static boolean hasScalingGear(ServerPlayer player) {
		return gearFingerprint(player) != 0;
	}

	/**
	 * The Refraction level that applies to this player: the highest level found on any
	 * equipped amethyst piece or held amethyst weapon.
	 */
	public static int refractionFor(ServerPlayer player) {
		int best = 0;

		for (EquipmentSlot slot : ARMOR_SLOTS) {
			ItemStack stack = player.getItemBySlot(slot);

			if (isAmethystArmor(stack)) {
				best = Math.max(best, Refraction.levelOf(stack));
			}
		}

		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();

		if (isLightWeapon(main)) {
			best = Math.max(best, Refraction.levelOf(main));
		}

		if (isLightWeapon(off)) {
			best = Math.max(best, Refraction.levelOf(off));
		}

		return best;
	}

	private static void remove(ServerPlayer player, Applied applied) {
		if (applied == null) {
			return;
		}

		if (applied.armorModifiers()) {
			remove(player.getAttribute(Attributes.ARMOR), ARMOR_MODIFIER_ID);
			remove(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_MODIFIER_ID);
		}

		if (applied.damageModifier()) {
			remove(player.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE_MODIFIER_ID);
		}
	}

	/**
	 * Installs a single ADD_VALUE modifier under a stable id. A zero amount removes it
	 * instead, so light level 0 never leaves a pointless +0 entry behind.
	 */
	private static void set(AttributeInstance instance, Identifier id, double amount) {
		if (instance == null) {
			return;
		}

		AttributeModifier existing = instance.getModifier(id);

		if (existing != null && existing.amount() == amount) {
			return;
		}

		if (amount == 0.0D) {
			instance.removeModifier(id);
			return;
		}

		instance.addOrUpdateTransientModifier(
				new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
	}

	private static void remove(AttributeInstance instance, Identifier id) {
		if (instance != null) {
			instance.removeModifier(id);
		}
	}

	/** True for the four amethyst armour pieces. */
	public static boolean isAmethystArmor(ItemStack stack) {
		Item item = stack.getItem();

		return item == AmethystItems.AMETHYST_HELMET
				|| item == AmethystItems.AMETHYST_CHESTPLATE
				|| item == AmethystItems.AMETHYST_LEGGINGS
				|| item == AmethystItems.AMETHYST_BOOTS;
	}

	/** The amethyst sword and spear scale their attack damage with light. */
	public static boolean isLightWeapon(ItemStack stack) {
		Item item = stack.getItem();

		return item == AmethystItems.AMETHYST_SWORD || item == AmethystItems.AMETHYST_SPEAR;
	}
}
