package net.darkhax.maxhealthfix.neoforge.impl;

import net.darkhax.maxhealthfix.common.impl.MaxHealthFixMod;
import net.darkhax.maxhealthfix.common.impl.Constants;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class NeoForgeMod {

    public NeoForgeMod() {
        MaxHealthFixMod.getInstance().init();
    }
}