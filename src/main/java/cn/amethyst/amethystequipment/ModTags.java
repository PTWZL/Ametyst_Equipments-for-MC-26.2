package cn.amethyst.amethystequipment;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Item tags added by the mod. Both tags contain the amethyst ingot and are used as the
 * repair ingredient of the amethyst tools/armour, mirroring how vanilla uses
 * {@code minecraft:netherite_tool_materials} and {@code minecraft:repairs_netherite_armor}.
 */
public final class ModTags {
	private ModTags() {
	}

	public static final TagKey<Item> AMETHYST_TOOL_MATERIALS =
			TagKey.create(Registries.ITEM, AmethystEquipment.id("amethyst_tool_materials"));

	public static final TagKey<Item> REPAIRS_AMETHYST_ARMOR =
			TagKey.create(Registries.ITEM, AmethystEquipment.id("repairs_amethyst_armor"));

	/** Nothing to do yet; the tags themselves are data files under data/&lt;ns&gt;/tags/item/. */
	public static void register() {
	}
}
