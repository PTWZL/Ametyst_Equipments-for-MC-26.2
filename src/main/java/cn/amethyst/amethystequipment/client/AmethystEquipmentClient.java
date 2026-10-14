package cn.amethyst.amethystequipment.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side entrypoint. The equipment asset that maps the armour textures is a plain
 * resource file under {@code assets/amethyst_equipment/equipment/}, so the only thing
 * that needs wiring up in code is the tooltip.
 */
public class AmethystEquipmentClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LightTooltip.register();
	}
}
