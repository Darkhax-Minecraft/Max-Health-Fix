package net.darkhax.maxhealthfix.mixin;

import net.darkhax.maxhealthfix.IHealthFixable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = LivingEntity.class, priority = 9001)
public abstract class MixinLivingEntity implements IHealthFixable {

    /**
     * Temporarily holds the original health value of the entity.
     */
    @Unique
    @Nullable
    private Float actualHealth = null;

    /**
     * The vanilla code loads entity health before attributes and equipment have been loaded in. This leads to players
     * with health over the default maximum being reset when they join the game. This is the source of MC-17876. This
     * mixin attempts to solve this issue by capturing the health value before it is applied and applying it again at a
     * later point.
     */
    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void maxhealthfix$readAdditionalSaveData(CompoundTag tag, CallbackInfo callback) {

        if (tag.contains("Health", Tag.TAG_ANY_NUMERIC)) {

            final float savedHealth = tag.getFloat("Health");

            if (savedHealth > getMaxHealth() && savedHealth > 0) {

                actualHealth = savedHealth;
            }
        }
    }

    /**
     * This mixin is used to apply the {@link #actualHealth} at the end of the first tick. This is done to give
     * equipment and modded mechanics like baubles/curios a chance to load in.
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void maxhealthfix$tick(CallbackInfo callback) {

        if (actualHealth != null) {

            if (actualHealth > 0 && actualHealth > this.getHealth()) {

                this.setHealth(actualHealth);
            }

            actualHealth = null;
        }
    }

    @Override
    public void maxhealthfix$setRestorePoint(float restorePoint) {

        this.actualHealth = restorePoint;
    }

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract void setHealth(float newHealth);
}