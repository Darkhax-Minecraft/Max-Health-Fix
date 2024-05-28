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
     * This float is used to temporarily hold the actual health of the entity while the entity data is being
     * deserialized. A null value is used to indicate that the health does not need correcting.
     */
    @Unique
    @Nullable
    private Float actualHealth = null;

    /**
     * The vanilla code will reset the entity health when the deserialized value exceeds
     * {@link LivingEntity#getMaxHealth()}. This generally is not an issue, however when entities are initially loaded
     * their max health attribute has not been properly initialized. This is the source of MC-17876.
     * <p>
     * This mixin is used to circumvent this faulty logic by capturing the deserialized value early and storing it in
     * {@link #actualHealth}. This approach is favoured over attempting to initialize attributes early as there is no
     * standard way to do this that would reasonably account for modded attribute sources.
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