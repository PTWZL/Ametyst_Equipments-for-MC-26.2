package cn.amethyst.amethystequipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * Every item added by the mod, plus the two custom materials they are built from.
 *
 * <h2>Numbers</h2>
 * <ul>
 *   <li><b>Durability</b> — every tool, weapon and armour piece uses 9/10 of the
 *       netherite value. Netherite tools have 2031 durability, so an amethyst tool
 *       gets {@value #TOOL_DURABILITY}. Netherite armour has a durability factor of 37,
 *       and {@link ArmorType#getDurability(int)} multiplies it per piece, so the exact
 *       integers are pinned with {@code durability(int)} below.</li>
 *   <li><b>Armour base</b> — defence points are exactly the golden armour values
 *       (helmet 1 / chestplate 3 / leggings 5 / boots 2).</li>
 *   <li><b>Attack damage</b> — the tool material deliberately has an attack damage
 *       bonus of {@code 0.0}, so the {@code attackDamage} argument passed to
 *       {@code sword()} / {@code spear()} / {@code tool()} is the whole item damage.
 *       Vanilla adds the player's base 1.0 on top, hence the sword and spear ask for
 *       5.0 to display the requested base attack damage of 6.</li>
 * </ul>
 */
public final class AmethystItems {
	private AmethystItems() {
	}

	// ------------------------------------------------------------------
	// Durability: 9/10 of the netherite values (netherite tools = 2031).
	// ------------------------------------------------------------------
	public static final int TOOL_DURABILITY = 1827; // 2031 * 9 / 10 = 1827.9 -> 1827
	public static final int HELMET_DURABILITY = 407; // 452 * 9 / 10
	public static final int CHESTPLATE_DURABILITY = 592; // 658 * 9 / 10
	public static final int LEGGINGS_DURABILITY = 555; // 616 * 9 / 10
	public static final int BOOTS_DURABILITY = 481; // 535 * 9 / 10

	/** Attack speed shared by every amethyst tool (vanilla netherite tools use -2.8). */
	public static final float TOOL_ATTACK_SPEED = -2.8F;

	/**
	 * The amethyst tool material: netherite-tier mining behaviour and enchantability,
	 * but a zero attack damage bonus so the per-item numbers below are absolute.
	 */
	public static final ToolMaterial AMETHYST_TOOL_MATERIAL = new ToolMaterial(
			BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
			TOOL_DURABILITY,
			9.0F,   // mining speed (netherite)
			0.0F,   // attack damage bonus: the item supply it instead
			15,     // enchantment value (netherite)
			ModTags.AMETHYST_TOOL_MATERIALS);

	/**
	 * The amethyst armour material. Defence is the vanilla golden armour spread, the
	 * durability factor is netherite's 37 (overridden per piece), and both toughness and
	 * knockback resistance are 0 because those two stats are applied dynamically from
	 * the light level by {@link LightScaling}.
	 */
	public static final ArmorMaterial AMETHYST_ARMOR_MATERIAL = new ArmorMaterial(
			37, // netherite armour durability factor
			Map.of(
					ArmorType.HELMET, 1,
					ArmorType.CHESTPLATE, 3,
					ArmorType.LEGGINGS, 5,
					ArmorType.BOOTS, 2),
			25, // enchantment value (same as gold)
			SoundEvents.ARMOR_EQUIP_GENERIC,
			0.0F, // toughness: supplied by the light level
			0.0F, // knockback resistance: supplied by the light level
			ModTags.REPAIRS_AMETHYST_ARMOR,
			equipmentAsset("amethyst"));

	/** The client-side equipment asset describing which textures the armour uses. */
	public static final ResourceKey<EquipmentAsset> AMETHYST_EQUIPMENT_ASSET = equipmentAsset("amethyst");

	private static ResourceKey<EquipmentAsset> equipmentAsset(String path) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, AmethystEquipment.id(path));
	}

	// ------------------------------------------------------------------
	// Registration bookkeeping
	//
	// These must be declared *before* the item fields below: Java runs static
	// initialisers in source order, and register() appends to REGISTERED.
	// ------------------------------------------------------------------
	private static final List<Item> REGISTERED = new ArrayList<>();

	/** Every item added by the mod, in registration (and creative tab) order. */
	public static final List<Item> ALL = Collections.unmodifiableList(REGISTERED);

	/**
	 * The displayed attack damage the sword and the spear are built around. Vanilla adds
	 * the player's own base 1.0 on top of what the item supplies, so the items ask for
	 * {@code BASE_ATTACK_DAMAGE - 1.0}.
	 */
	public static final double BASE_ATTACK_DAMAGE = 6.0D;

	// ------------------------------------------------------------------
	// Items
	// ------------------------------------------------------------------
	public static final Item AMETHYST_INGOT =
			register("amethyst_ingot", new Item.Properties());

	/**
	 * The Refraction book. Level 1 is the item's default state, so the requesting recipe
	 * simply crafts this book from amethyst blocks and a diamond block; higher levels are
	 * produced by combining a book with amethyst gear (see {@link RefractionApplyRecipe}).
	 */
	public static final Item REFRACTION_BOOK =
			register("refraction_book", new Item.Properties()
					.useItemDescriptionPrefix()
					.component(Refraction.LEVEL, 1));

	public static final Item AMETHYST_SWORD =
			register("amethyst_sword", new Item.Properties()
					.durability(TOOL_DURABILITY)
					.sword(AMETHYST_TOOL_MATERIAL, 5.0F, -2.4F));

	/**
	 * The amethyst spear.
	 *
	 * <p>The nine floats are, in order:
	 * {@code attackDuration, damageMultiplier, delay, dismountTime, dismountThreshold,
	 * knockbackTime, knockbackThreshold, damageTime, damageThreshold}. The first is the
	 * swing/attack cooldown in seconds ({@code ATTACK_SPEED = 1/attackDuration - 4}), and
	 * {@code delay / dismountTime / knockbackTime / damageTime} are the four windows the
	 * right-click charge pose timeline is built from
	 * ({@code dismountTime >= 1}, {@code knockbackTime >= dismountTime + 2},
	 * {@code damageTime >= knockbackTime + 0.25}). {@code knockbackThreshold} (5.1) and
	 * {@code damageThreshold} (4.6) are the two constants every vanilla spear shares.
	 *
	 * <p>These are the vanilla <b>copper</b> spear values copied verbatim. Copper is the
	 * closest structural match to the amethyst material: its {@code attackDamageBonus} is
	 * also {@code 0.0}, so {@code spear()} picks exactly the same code paths (notably the
	 * non-wood sound branch). Copying a vanilla tuple verbatim guarantees the pose timeline
	 * is one the animation code was authored against.
	 */
	public static final Item AMETHYST_SPEAR =
			register("amethyst_spear", new Item.Properties()
					.durability(TOOL_DURABILITY)
					.spear(AMETHYST_TOOL_MATERIAL,
							0.85F,   // attackDuration
							0.82F,   // damageMultiplier
							0.65F,   // delay
							4.00F,   // dismountTime
							12.0F,   // dismountThreshold
							8.25F,   // knockbackTime
							5.10F,   // knockbackThreshold (vanilla constant)
							12.50F,  // damageTime
							4.60F)); // damageThreshold (vanilla constant)

	public static final Item AMETHYST_PICKAXE =
			register("amethyst_pickaxe", new Item.Properties()
					.durability(TOOL_DURABILITY)
					.pickaxe(AMETHYST_TOOL_MATERIAL, 1.0F, TOOL_ATTACK_SPEED));

	public static final Item AMETHYST_AXE =
			register("amethyst_axe", new Item.Properties()
					.durability(TOOL_DURABILITY)
					.axe(AMETHYST_TOOL_MATERIAL, 4.0F, TOOL_ATTACK_SPEED));

	public static final Item AMETHYST_SHOVEL =
			register("amethyst_shovel", new Item.Properties()
					.durability(TOOL_DURABILITY)
					.shovel(AMETHYST_TOOL_MATERIAL, 1.5F, TOOL_ATTACK_SPEED));

	public static final Item AMETHYST_HOE =
			register("amethyst_hoe", new Item.Properties()
					.durability(TOOL_DURABILITY)
					.hoe(AMETHYST_TOOL_MATERIAL, 0.0F, TOOL_ATTACK_SPEED));

	public static final Item AMETHYST_HELMET =
			register("amethyst_helmet", new Item.Properties()
					.durability(HELMET_DURABILITY)
					.humanoidArmor(AMETHYST_ARMOR_MATERIAL, ArmorType.HELMET));

	public static final Item AMETHYST_CHESTPLATE =
			register("amethyst_chestplate", new Item.Properties()
					.durability(CHESTPLATE_DURABILITY)
					.humanoidArmor(AMETHYST_ARMOR_MATERIAL, ArmorType.CHESTPLATE));

	public static final Item AMETHYST_LEGGINGS =
			register("amethyst_leggings", new Item.Properties()
					.durability(LEGGINGS_DURABILITY)
					.humanoidArmor(AMETHYST_ARMOR_MATERIAL, ArmorType.LEGGINGS));

	public static final Item AMETHYST_BOOTS =
			register("amethyst_boots", new Item.Properties()
					.durability(BOOTS_DURABILITY)
					.humanoidArmor(AMETHYST_ARMOR_MATERIAL, ArmorType.BOOTS));

	/** The four armour pieces, used by the tooltip and by the light-scaling tick. */
	public static final List<Item> ARMOR_PIECES = List.of(
			AMETHYST_HELMET, AMETHYST_CHESTPLATE, AMETHYST_LEGGINGS, AMETHYST_BOOTS);

	private static Item register(String path, Item.Properties properties) {
		Item item = new Item(properties.setId(AmethystEquipment.itemKey(path)));
		Registry.register(BuiltInRegistries.ITEM, AmethystEquipment.itemKey(path), item);
		REGISTERED.add(item);
		return item;
	}

	/** No-op: the fields above register themselves when the class is initialised. */
	public static void register() {
	}
}
