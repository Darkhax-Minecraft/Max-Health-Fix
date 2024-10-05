package net.darkhax.maxhealthfix.common.impl;

import net.darkhax.pricklemc.common.api.config.ConfigManager;

public class MaxHealthFixMod {

    private static MaxHealthFixMod instance;
    private boolean hasInitialized = false;
    private Config config;

    public void init() {
        if (hasInitialized) {
            throw new IllegalStateException("The " + Constants.MOD_NAME + " has already been initialized.");
        }
        this.config = ConfigManager.load(Constants.MOD_ID, new Config());
        hasInitialized = true;
    }

    public static Config getConfig() {
        final MaxHealthFixMod mod = getInstance();
        if (!mod.hasInitialized) {
            mod.init();
        }
        return mod.config;
    }

    public static MaxHealthFixMod getInstance() {
        if (instance == null) {
            instance = new MaxHealthFixMod();
        }
        return instance;
    }
}