package cn.amethyst.amethystequipment;

import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * In-game verification commands.
 *
 * <pre>
 * /amethystequipment items                 - registration check for every item
 * /amethystequipment stats                 - durability, material and coefficient values
 * /amethystequipment light [level]         - preview the formulas, or force a light level
 * /amethystequipment refraction &lt;1-10&gt;    - get a Refraction book of that level
 * /amethystequipment grant                 - give one of everything
 * </pre>
 */
public final class DebugCommands {
	private DebugCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("amethystequipment")
						.then(Commands.literal("items").executes(context -> {
							StringBuilder sb = new StringBuilder("Amethyst items: ");

							for (Item item : AmethystItems.ALL) {
								Identifier id = BuiltInRegistries.ITEM.getKey(item);
								sb.append('\n').append(id).append(" -> ")
										.append(id == null ? "UNREGISTERED" : "ok");
							}

							send(context.getSource(), sb.toString());
							return 1;
						}))
						.then(Commands.literal("stats").executes(context -> {
							StringBuilder sb = new StringBuilder();
							sb.append("tool durability=").append(AmethystItems.TOOL_DURABILITY)
									.append(" (netherite 2031 * 9/10)");
							sb.append('\n').append("armour durability helmet=").append(AmethystItems.HELMET_DURABILITY)
									.append(" chest=").append(AmethystItems.CHESTPLATE_DURABILITY)
									.append(" legs=").append(AmethystItems.LEGGINGS_DURABILITY)
									.append(" boots=").append(AmethystItems.BOOTS_DURABILITY);
							sb.append('\n').append("armour defence helmet=1 chest=3 legs=5 boots=2 (golden values)");
							sb.append('\n').append("base per light level: armour +")
									.append(LightScaling.BASE_ARMOR_PER_LIGHT)
									.append(", knockback resistance +")
									.append(LightScaling.BASE_KNOCKBACK_RESISTANCE_PER_LIGHT)
									.append(", attack damage +")
									.append(LightScaling.BASE_ATTACK_DAMAGE_PER_LIGHT);
							sb.append('\n').append("Refraction multiplies every coefficient by 1 + ")
									.append(LightScaling.REFRACTION_LEVEL_STEP).append(" * level (max ")
									.append(LightScaling.REFRACTION_MAX_LEVEL).append(')');

							for (int level : new int[] {0, 1, 5, 10}) {
								sb.append("\n  level ").append(level).append(": x")
										.append(LightScaling.refractionMultiplier(level))
										.append(" -> armour +").append(LightScaling.armorPerLight(level))
										.append("/light, damage +").append(LightScaling.attackDamagePerLight(level))
										.append("/light, at light 15 damage total ")
										.append(AmethystItems.BASE_ATTACK_DAMAGE
												+ LightScaling.attackDamageBonus(15, level));
							}

							send(context.getSource(), sb.toString());
							return 1;
						}))
						.then(Commands.literal("refraction")
								.then(Commands.argument("level", IntegerArgumentType.integer(1, 10))
										.executes(context -> {
											int level = IntegerArgumentType.getInteger(context, "level");
											ServerPlayer player = context.getSource().getPlayerOrException();
											// Must be the mod's own book item: a vanilla enchanted book cannot
											// carry the mod component, which is why the command used to hand out a
											// plain, propertyless book.
											ItemStack book = Refraction.book(level);

											if (!player.getInventory().add(book)) {
												player.drop(book, false);
											}

											send(context.getSource(), "Gave a Refraction " + level
													+ " book (light coefficients x"
													+ LightScaling.refractionMultiplier(level) + ").");
											return 1;
										})))
						.then(Commands.literal("light")
								.executes(context -> {
									ServerPlayer player = context.getSource().getPlayerOrException();
									ServerLevel level = (ServerLevel) player.level();
									int light = LightScaling.lightAt(level, player.blockPosition());

									send(context.getSource(), describeLight(player, light));
									return 1;
								})
								.then(Commands.argument("level", IntegerArgumentType.integer(0, 15))
										.executes(context -> {
											int light = IntegerArgumentType.getInteger(context, "level");
											ServerPlayer player = context.getSource().getPlayerOrException();

											send(context.getSource(), "If the light level were " + light + ":\n"
													+ describeLight(player, light));
											return 1;
										})))
						.then(Commands.literal("grant").executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();

							for (Item item : AmethystItems.ALL) {
								ItemStack stack = new ItemStack(item);

								if (!player.getInventory().add(stack)) {
									player.drop(stack, false);
								}
							}

							send(context.getSource(), "Granted " + AmethystItems.ALL.size() + " amethyst items.");
							return 1;
						}))));
	}

	private static String describeLight(ServerPlayer player, int light) {
		AttributeInstance armorInstance = player.getAttribute(Attributes.ARMOR);
		AttributeInstance knockbackInstance = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
		AttributeInstance damageInstance = player.getAttribute(Attributes.ATTACK_DAMAGE);

		int pieces = 0;
		int bestRefraction = 0;

		for (EquipmentSlot slot : new EquipmentSlot[] {
				EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			ItemStack stack = player.getItemBySlot(slot);

			if (AmethystItems.ARMOR_PIECES.contains(stack.getItem())) {
				pieces++;
				bestRefraction = Math.max(bestRefraction, Refraction.levelOf(stack));
			}
		}

		ItemStack held = player.getMainHandItem();

		if (LightScaling.isLightWeapon(held)) {
			bestRefraction = Math.max(bestRefraction, Refraction.levelOf(held));
		}

		return "light=" + light
				+ "  amethyst armour pieces=" + pieces
				+ "  refraction level=" + bestRefraction
				+ " (x" + LightScaling.refractionMultiplier(bestRefraction) + ")"
				+ "\n+armour (total across pieces)=" + LightScaling.armorBonus(light, bestRefraction) * pieces
				+ "  +knockback resistance=" + LightScaling.knockbackResistanceBonus(light, bestRefraction) * pieces
				+ "\n+attack damage=" + LightScaling.attackDamageBonus(light, bestRefraction)
				+ "  weapon total=" + (AmethystItems.BASE_ATTACK_DAMAGE
						+ LightScaling.attackDamageBonus(light, bestRefraction))
				+ "\nactual ARMOR attribute=" + (armorInstance == null ? "?" : armorInstance.getValue())
				+ "  KNOCKBACK_RESISTANCE=" + (knockbackInstance == null ? "?" : knockbackInstance.getValue())
				+ "  ATTACK_DAMAGE=" + (damageInstance == null ? "?" : damageInstance.getValue());
	}

	private static void send(net.minecraft.commands.CommandSourceStack source, String message) {
		source.sendSuccess(() -> Component.literal(message), false);
	}
}
