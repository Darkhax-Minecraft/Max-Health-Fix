package net.darkhax.maxhealthfix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * This mod fixes MC-17876 which is a bug that causes a player's health to reset to the default
 * value (20) when they log out. For example if a player has a helmet with +10 max health and
 * logs out while having 25 health their health will be reset to 20.
 * 
 * This fix works by recording the player's current health in Forge's persistent player data
 * tag when the player logs out. When the player logs in again this value will be referenced
 * and their previous health value will be restored.
 * 
 * https://bugs.mojang.com/browse/MC-17876
 */
@Mod(MaxHealthFix.MOD_ID)
public class MaxHealthFix {
    
    public static final String MOD_ID = "maxhealthfix";
    public static final String TAG_NAME = "MaxHealthFixPrevAmount";
    
    private final Logger log = LogManager.getLogger("Max Health Fix");
    private final List<ServerPlayer> joiningPlayerQueue = new ArrayList<>();
    
    public MaxHealthFix() {
        
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogOut);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogIn);
        MinecraftForge.EVENT_BUS.addListener(this::onWorldTick);
    }
    
    /**
     * This event happens after the player has logged in and joined the world but before the
     * attributes for the player has been ticked. Most attribute sources including armor are
     * applied on entity tick meaning we need to wait at least one tick to restore their
     * previous health.
     * 
     * To delay the effect by one tick we add the player to the {@link #joiningPlayerQueue}
     * which will be processed after the world finishes it's current tick through the
     * {@link #onWorldTick(net.minecraftforge.event.TickEvent.WorldTickEvent)} listener.
     */
    private void onPlayerLogIn (PlayerLoggedInEvent event) {
        
        if (event.getPlayer() instanceof ServerPlayer) {
            
            this.joiningPlayerQueue.add((ServerPlayer) event.getPlayer());
        }
    }
    
    /**
     * When the player logs out we simply store their current health in Forge's persistent
     * player tag.
     */
    private void onPlayerLogOut (PlayerLoggedOutEvent event) {
        
        if (event.getPlayer() instanceof ServerPlayer) {
            
            final ServerPlayer player = (ServerPlayer) event.getPlayer();
            player.getPersistentData().putFloat(TAG_NAME, event.getPlayer().getHealth());
        }
    }
    
    /**
     * At the end of each world tick we process any players in the {@link #joiningPlayerQueue}.
     * This will try to correct the health for each player in the queue before removing them.
     */
    private void onWorldTick (TickEvent.WorldTickEvent event) {
        
        // Only process the queue at the end of the world tick, and if there are players in
        // queue.
        if (event.phase == Phase.END && !this.joiningPlayerQueue.isEmpty()) {
            
            // We use an iterator to process players so they can be easily removed.
            final Iterator<ServerPlayer> iter = this.joiningPlayerQueue.iterator();
            
            while (iter.hasNext()) {
                
                // Get the player from the iterator and remove them instantly. We only ever
                // want to process a player once for each time they log in.
                final ServerPlayer player = iter.next();
                iter.remove();
                
                // Grab the persistent player data from Forge.
                final CompoundTag playerData = player.getPersistentData();
                
                if (playerData != null && playerData.contains(TAG_NAME)) {
                    
                    final float oldHealth = playerData.getFloat(TAG_NAME);
                    final float currentHealth = player.getHealth();
                    
                    // Remove the custom tag. We only want to process this once.
                    playerData.remove(TAG_NAME);
                    
                    if (oldHealth > currentHealth) {
                        
                        // Caps the old health to the player's new maximum health. This is done
                        // in case their attributes were modified since they were last online.
                        // For example updating a mod may change the max health modifiers being
                        // applied.
                        final float correctedHealth = Math.min(oldHealth, player.getMaxHealth());
                        
                        player.setHealth(correctedHealth);
                        this.log.debug("Corrected health of {} from {} to {}.", player.getDisplayName().getString(), currentHealth, oldHealth);
                    }
                }
            }
        }
    }
}