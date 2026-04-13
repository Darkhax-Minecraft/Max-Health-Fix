package net.darkhax.maxhealthfix.common.mixin;

import net.darkhax.maxhealthfix.common.impl.IHealthFixable;
import net.darkhax.maxhealthfix.common.impl.MaxHealthFixMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntity.class, priority = 9001)
public abstract class MixinLivingEntity implements IHealthFixable {

    /**
     * Temporarily holds a restore point used to correct entity health. If this value is ever not null the health will
     * be restored to this value at the end of the next tick.
     */
    @Unique
    @Nullable
    private Float maxhealthfix$restorePoint = null;

    @Override
    public void maxhealthfix$setRestorePoint(Float restorePoint) {
        this.maxhealthfix$restorePoint = restorePoint;
    }

    /**
     * The vanilla code loads entity health before updating their max health attribute causing the value to be clamped
     * to the default max health for the entity type. This is the source of MC-17876. This mixin sets a restore point
     * before the health is clamped allowing the health value to be restored at the end of the next tick.
     */
    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void maxhealthfix$readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        final float savedHealth = input.getFloatOr("Health", -9999f);
        if (savedHealth > 0 && savedHealth > getMaxHealth()) {
            this.maxhealthfix$setRestorePoint(savedHealth);
        }
    }

    /**
     * This mixin is used to restore the entity health back to the restore point at the end of a tick.
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void maxhealthfix$tick(CallbackInfo callback) {
        if (this.maxhealthfix$restorePoint != null) {
            if (MaxHealthFixMod.getConfig().mod_enabled && this.maxhealthfix$restorePoint > 0 && this.maxhealthfix$restorePoint > this.getHealth()) {
                this.setHealth(this.maxhealthfix$restorePoint);
            }
            this.maxhealthfix$setRestorePoint(null);
        }
    }

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract void setHealth(float newHealth);
}
