package net.darkhax.maxhealthfix.fabric.mixin;

import net.darkhax.maxhealthfix.common.impl.IHealthFixable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerList.class)
public class MixinPlayerList {

    @Inject(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setHealth(F)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onPlayerRespawn(ServerPlayer player, boolean keepInventory, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir, DimensionTransition transition, ServerLevel targetLevel, ServerPlayer newPlayer, Vec3 transferPos, byte keepInv, ServerLevel playerLevel, LevelData playerLevelData) {
        if (newPlayer instanceof IHealthFixable fixable) {
            fixable.maxhealthfix$setRestorePoint(player.isDeadOrDying() ? player.getMaxHealth() : player.getHealth());
        }
    }
}