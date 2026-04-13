package net.darkhax.maxhealthfix.fabric;

import net.darkhax.maxhealthfix.common.impl.MaxHealthFixMod;
import net.fabricmc.api.ModInitializer;

public class MaxHealthFixFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        MaxHealthFixMod.getInstance().init();
    }
}