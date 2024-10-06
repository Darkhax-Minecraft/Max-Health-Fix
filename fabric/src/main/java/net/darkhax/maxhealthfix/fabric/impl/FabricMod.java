package net.darkhax.maxhealthfix.fabric.impl;

import net.darkhax.maxhealthfix.common.impl.MaxHealthFixMod;
import net.fabricmc.api.ModInitializer;

public class FabricMod implements ModInitializer {

    @Override
    public void onInitialize() {
        MaxHealthFixMod.getInstance().init();
    }
}