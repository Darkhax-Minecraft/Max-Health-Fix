package net.darkhax.maxhealthfix;

import net.fabricmc.api.ModInitializer;

public class MaxHealthFixFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        Constants.LOG.info("Loaded {} for Fabric.", Constants.MOD_ID);
    }
}