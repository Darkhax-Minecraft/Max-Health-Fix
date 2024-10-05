package net.darkhax.maxhealthfix.fabric.impl;

import net.darkhax.maxhealthfix.common.impl.MaxHealthFixMod;
import net.fabricmc.api.ClientModInitializer;

public class FabricModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MaxHealthFixMod.getInstance().init();
    }
}