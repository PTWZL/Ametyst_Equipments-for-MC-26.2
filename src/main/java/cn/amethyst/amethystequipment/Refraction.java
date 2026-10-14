package cn.amethyst.amethystequipment;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

/**
 * The {@code 折光强化 / Refraction} infusion.
 *
 * <h2>Why this is not a real enchantment</h2>
 * Minecraft 26.2 populates its enchantment registry exclusively from the vanilla data
 * pack. A mod cannot add an enchantment at all:
 * <ul>
 *   <li>a {@code data/&lt;ns&gt;/enchantment/*.json} file is never read (verified by
 *       corrupting the file — no parse error was ever logged — and by dumping the live
 *       registry, which held exactly the 44 vanilla enchantments), and</li>
 *   <li>{@code BuiltInRegistries} has <b>no</b> {@code ENCHANTMENT} registry to register
 *       into from code, and Fabric API ships no enchantment-registry hook
 *       ({@code EnchantmentEvents} only observes enchanting, it cannot add entries).</li>
 * </ul>
 * So Refraction is implemented as a registered data component instead. Functionally it is
 * the requested thing: a level 1..10 upgrade with a name, a tooltip entry, a crafting
 * recipe, and an effect that scales every per-light coefficient by {@code 1 + 0.5 * level}.
 * The only visible difference is that it appears as its own tooltip line rather than in the
 * vanilla enchantment list.
 */
public final class Refraction {
	private Refraction() {
	}

	public static final String PATH = "refraction";

	public static final int MAX_LEVEL = 10;

	/** The per-level coefficient bonus: multiplier = 1 + 0.5 * level. */
	public static final double LEVEL_STEP = 0.5D;

	/**
	 * Stores the Refraction level of an item. Absent means "no Refraction".
	 */
	public static final DataComponentType<Integer> LEVEL = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			AmethystEquipment.id(PATH),
			DataComponentType.<Integer>builder()
					.persistent(ExtraCodecs.intRange(0, MAX_LEVEL))
					.networkSynchronized(ByteBufCodecs.VAR_INT)
					.cacheEncoding()
					.build());

	/** The level on the stack (0 when absent). */
	public static int levelOf(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}

		return stack.getOrDefault(LEVEL, 0);
	}

	/** Applies {@code level} to the stack and returns it, for fluent use. */
	public static ItemStack apply(ItemStack stack, int level) {
		stack.set(LEVEL, Math.max(0, Math.min(MAX_LEVEL, level)));
		return stack;
	}

	/** The multiplier a given level applies to the light coefficients. */
	public static double multiplier(int level) {
		return LightScaling.refractionMultiplier(level);
	}

	/**
	 * A level {@code level} Refraction book: the mod's own book item, which carries the
	 * component. Always non-empty; levels are clamped to the valid range.
	 */
	public static ItemStack book(int level) {
		return apply(new ItemStack(AmethystItems.REFRACTION_BOOK), level);
	}
}
