package cn.amethyst.amethystequipment.client;

import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

import cn.amethyst.amethystequipment.AmethystItems;
import cn.amethyst.amethystequipment.LightScaling;
import cn.amethyst.amethystequipment.Refraction;

/**
 * Shows the light-scaled values on the amethyst armour, sword and spear, so the numbers
 * can be checked without having to fight anything.
 *
 * <p>Armour is listed per piece (matching {@link LightScaling}, which grants the light
 * bonus to each equipped piece), and everything updates live as the player moves between
 * light levels. The tooltip reads the same light level and the same Refraction level the
 * server uses, so the figures shown are the figures in force.
 */
@Environment(EnvType.CLIENT)
public final class LightTooltip {
	private LightTooltip() {
	}

	public static void register() {
		ItemTooltipCallback.EVENT.register(LightTooltip::append);
	}

	private static void append(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
			TooltipFlag flag, List<Component> lines) {
		boolean armor = isArmor(stack);
		boolean weapon = LightScaling.isLightWeapon(stack);
		boolean book = stack.is(AmethystItems.REFRACTION_BOOK);

		if (!armor && !weapon && !book) {
			return;
		}

		int refractionLevel = Refraction.levelOf(stack);

		// The book is the one item whose whole point is the infusion, so describe itself.
		if (book) {
			lines.add(Component.translatable("tooltip.amethyst_equipment.refraction",
					refractionLevel, format(LightScaling.refractionMultiplier(refractionLevel)))
					.withStyle(ChatFormatting.LIGHT_PURPLE));
			lines.add(Component.translatable("tooltip.amethyst_equipment.refraction_howto")
					.withStyle(ChatFormatting.GRAY));
			return;
		}

		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null || player.level() == null) {
			return;
		}

		int light = LightScaling.lightAt(player.level(), player.blockPosition());

		lines.add(Component.translatable("tooltip.amethyst_equipment.light", light)
				.withStyle(ChatFormatting.GRAY));

		if (refractionLevel > 0) {
			lines.add(Component.translatable("tooltip.amethyst_equipment.refraction",
					refractionLevel, format(LightScaling.refractionMultiplier(refractionLevel)))
					.withStyle(ChatFormatting.LIGHT_PURPLE));
		}

		lines.add(Component.empty());

		if (armor) {
			lines.add(Component.translatable("tooltip.amethyst_equipment.current_armor",
					format(LightScaling.armorBonus(light, refractionLevel))).withStyle(ChatFormatting.AQUA));
			lines.add(Component.translatable("tooltip.amethyst_equipment.current_knockback",
					format(LightScaling.knockbackResistanceBonus(light, refractionLevel)))
					.withStyle(ChatFormatting.AQUA));
			lines.add(Component.translatable("tooltip.amethyst_equipment.max_armor",
					format(LightScaling.armorBonus(LightScaling.MAX_LIGHT, refractionLevel)))
					.withStyle(ChatFormatting.DARK_AQUA));
			lines.add(Component.translatable("tooltip.amethyst_equipment.max_knockback",
					format(LightScaling.knockbackResistanceBonus(LightScaling.MAX_LIGHT, refractionLevel)))
					.withStyle(ChatFormatting.DARK_AQUA));
		}

		if (weapon) {
			lines.add(Component.translatable("tooltip.amethyst_equipment.current_damage",
					format(LightScaling.attackDamageBonus(light, refractionLevel))).withStyle(ChatFormatting.RED));
			lines.add(Component.translatable("tooltip.amethyst_equipment.total_damage",
					format(AmethystItems.BASE_ATTACK_DAMAGE
							+ LightScaling.attackDamageBonus(light, refractionLevel)))
					.withStyle(ChatFormatting.GOLD));
			lines.add(Component.translatable("tooltip.amethyst_equipment.max_damage",
					format(LightScaling.attackDamageBonus(LightScaling.MAX_LIGHT, refractionLevel)))
					.withStyle(ChatFormatting.DARK_RED));
		}
	}

	private static String format(double value) {
		if (value == Math.floor(value)) {
			return Integer.toString((int) value);
		}

		return String.format(Locale.ROOT, "%.1f", value);
	}

	private static boolean isArmor(ItemStack stack) {
		return AmethystItems.ARMOR_PIECES.contains(stack.getItem());
	}
}
