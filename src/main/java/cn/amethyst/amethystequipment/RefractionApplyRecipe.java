package cn.amethyst.amethystequipment;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * "Apply Refraction": combines a Refraction book with a piece of amethyst equipment and
 * moves the infused level onto the equipment.
 *
 * <p>This is a custom recipe because the level has to travel as the mod's own data
 * component, which a vanilla shaped recipe cannot express (its {@code components} block is
 * resolved before mod component types are available, so it fails to parse — verified).
 * Using one recipe also covers every level, instead of one recipe file per level.
 *
 * <p>Shapeless: one Refraction book plus exactly one amethyst equipment item, no other
 * ingredients. The result keeps the equipment's own damage and its previous level, and
 * takes the highest of the two levels.
 */
public class RefractionApplyRecipe extends CustomRecipe {
	/** Book levels this recipe will consume; level 1 comes from the crafting recipe. */
	public static final int MIN_LEVEL = 1;
	public static final int MAX_LEVEL = Refraction.MAX_LEVEL;

	public static final MapCodec<RefractionApplyRecipe> CODEC =
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
							.forGetter(recipe -> recipe.category))
					.apply(instance, RefractionApplyRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RefractionApplyRecipe> STREAM_CODEC =
			StreamCodec.composite(
					CraftingBookCategory.STREAM_CODEC, recipe -> recipe.category,
					RefractionApplyRecipe::new);

	private final CraftingBookCategory category;

	public RefractionApplyRecipe(CraftingBookCategory category) {
		this.category = category;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		ItemStack book = ItemStack.EMPTY;
		ItemStack gear = ItemStack.EMPTY;
		int items = 0;

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);

			if (stack.isEmpty()) {
				continue;
			}

			items++;

			if (isRefractionBook(stack)) {
				if (!book.isEmpty()) {
					return false;
				}

				book = stack;
			} else if (isAmethystEquipment(stack)) {
				if (!gear.isEmpty()) {
					return false;
				}

				gear = stack;
			} else {
				return false;
			}
		}

		return items == 2 && !book.isEmpty() && !gear.isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack book = ItemStack.EMPTY;
		ItemStack gear = ItemStack.EMPTY;

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);

			if (isRefractionBook(stack)) {
				book = stack;
			} else if (isAmethystEquipment(stack) && gear.isEmpty()) {
				gear = stack;
			}
		}

		if (book.isEmpty() || gear.isEmpty()) {
			return ItemStack.EMPTY;
		}

		int level = Math.min(MAX_LEVEL, Math.max(Refraction.levelOf(book), Refraction.levelOf(gear)));
		ItemStack result = gear.copyWithCount(1);

		return Refraction.apply(result, level);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);

			if (isRefractionBook(stack)) {
				// The book is consumed; give back a plain book so the recipe is not a total loss.
				remaining.set(i, new ItemStack(net.minecraft.world.item.Items.BOOK));
			}
		}

		return remaining;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return ModRecipes.REFRACTION_APPLY_SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return category;
	}

	/** A Refraction book is a book item carrying a level of at least 1. */
	public static boolean isRefractionBook(ItemStack stack) {
		return stack.is(AmethystItems.REFRACTION_BOOK) && Refraction.levelOf(stack) >= MIN_LEVEL;
	}

	/** There is no vanilla or mod tag for "the mod's gear", so the list is explicit. */
	public static boolean isAmethystEquipment(ItemStack stack) {
		return LightScaling.isAmethystArmor(stack)
				|| LightScaling.isLightWeapon(stack)
				|| stack.is(AmethystItems.AMETHYST_PICKAXE)
				|| stack.is(AmethystItems.AMETHYST_AXE)
				|| stack.is(AmethystItems.AMETHYST_SHOVEL)
				|| stack.is(AmethystItems.AMETHYST_HOE);
	}
}
