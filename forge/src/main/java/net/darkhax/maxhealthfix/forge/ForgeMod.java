package net.darkhax.maxhealthfix.forge;

import net.darkhax.maxhealthfix.common.impl.MaxHealthFixMod;
import net.darkhax.maxhealthfix.common.impl.Constants;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ForgeMod {

    public ForgeMod() {
        MaxHealthFixMod.getInstance().init();
    }
}