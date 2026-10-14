package cn.amethyst.amethystequipment;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Registers the mod's recipe serializer and type. The recipe files themselves live in
 * {@code data/amethyst_equipment/recipe/}.
 */
public final class ModRecipes {
	private ModRecipes() {
	}

	public static final RecipeSerializer<RefractionApplyRecipe> REFRACTION_APPLY_SERIALIZER =
			new RecipeSerializer<>(RefractionApplyRecipe.CODEC, RefractionApplyRecipe.STREAM_CODEC);

	public static final RecipeType<RefractionApplyRecipe> REFRACTION_APPLY_TYPE = new RecipeType<>() {
		@Override
		public String toString() {
			return AmethystEquipment.id("refraction_apply").toString();
		}
	};

	public static void register() {
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
				AmethystEquipment.id("refraction_apply"), REFRACTION_APPLY_SERIALIZER);
		Registry.register(BuiltInRegistries.RECIPE_TYPE,
				AmethystEquipment.id("refraction_apply"), REFRACTION_APPLY_TYPE);
	}
}
