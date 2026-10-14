package cn.amethyst.amethystequipment;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Amethyst Equipment.
 *
 * <p>Adds an amethyst ingot (smelted from amethyst shards, also found in abandoned
 * mineshafts) plus a full amethyst tool set, a sword, a spear and a set of armour.
 *
 * <p>The armour, the sword and the spear scale with the light level the wearer is
 * standing in; see {@link LightScaling} for the exact formulas.
 */
public class AmethystEquipment implements ModInitializer {
	public static final String MOD_ID = "amethyst_equipment";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static ResourceKey<Item> itemKey(String path) {
		return ResourceKey.create(Registries.ITEM, id(path));
	}

	/** The vanilla "Ingredients" creative tab (its key is private, so it is rebuilt here). */
	public static final ResourceKey<CreativeModeTab> INGREDIENTS_TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("ingredients"));

	/** The vanilla "Combat" creative tab. */
	public static final ResourceKey<CreativeModeTab> COMBAT_TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("combat"));

	@Override
	public void onInitialize() {
		ModTags.register();
		AmethystItems.register();
		ModRecipes.register();
		CreativeTabs.register();
		LightScaling.register();
		LootTables.register();
		DebugCommands.register();

		LOGGER.info("Amethyst equipment registered: {} items", AmethystItems.ALL.size());
	}
}
