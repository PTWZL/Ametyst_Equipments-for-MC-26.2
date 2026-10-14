package cn.amethyst.amethystequipment;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

/**
 * Puts the mod's items into the vanilla creative tabs: the ingot under "Ingredients",
 * and the tools, weapons and armour under "Combat".
 */
public final class CreativeTabs {
	private CreativeTabs() {
	}

	public static void register() {
		CreativeModeTabEvents.modifyOutputEvent(AmethystEquipment.INGREDIENTS_TAB)
				.register(output -> {
					output.accept(AmethystItems.AMETHYST_INGOT);
					output.accept(AmethystItems.REFRACTION_BOOK);
				});

		CreativeModeTabEvents.modifyOutputEvent(AmethystEquipment.COMBAT_TAB)
				.register(output -> {
					output.accept(AmethystItems.AMETHYST_SWORD);
					output.accept(AmethystItems.AMETHYST_SPEAR);
					output.accept(AmethystItems.AMETHYST_PICKAXE);
					output.accept(AmethystItems.AMETHYST_AXE);
					output.accept(AmethystItems.AMETHYST_SHOVEL);
					output.accept(AmethystItems.AMETHYST_HOE);
					output.accept(AmethystItems.AMETHYST_HELMET);
					output.accept(AmethystItems.AMETHYST_CHESTPLATE);
					output.accept(AmethystItems.AMETHYST_LEGGINGS);
					output.accept(AmethystItems.AMETHYST_BOOTS);
				});
	}
}
