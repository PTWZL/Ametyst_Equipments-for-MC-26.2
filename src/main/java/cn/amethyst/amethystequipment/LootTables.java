package cn.amethyst.amethystequipment;

import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

/**
 * Adds the amethyst ingot to abandoned mineshaft chests.
 *
 * <p>An extra pool is appended to the vanilla table rather than replacing it, so the
 * original mineshaft loot is left completely untouched. The pool rolls once with a 25%
 * chance and drops 1-2 ingots.
 */
public final class LootTables {
	private LootTables() {
	}

	/** Chance that a mineshaft chest contains amethyst ingots at all. */
	public static final float CHANCE = 0.25F;

	public static void register() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (source.isBuiltin() && BuiltInLootTables.ABANDONED_MINESHAFT.equals(key)) {
				tableBuilder.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(CHANCE))
						.add(LootItem.lootTableItem(AmethystItems.AMETHYST_INGOT)
								.setWeight(1)
								.apply(SetItemCountFunction.setCount(
										UniformGenerator.between(1.0F, 2.0F)))));
			}
		});
	}
}
