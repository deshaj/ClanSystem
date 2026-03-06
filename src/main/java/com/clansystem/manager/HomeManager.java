package com.clansystem.manager;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanHome;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HomeManager {
    private final ClanSystem plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    public HomeManager(ClanSystem plugin) {
        this.plugin = plugin;
    }
    
    public CompletableFuture<Void> setHome(Clan clan, Location location) {
        ClanHome home = new ClanHome(
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
        clan.setHome(home);
        
        plugin.debug("Set home for clan " + clan.getName());
        return plugin.getClanManager().updateClan(clan);
    }
    
    public CompletableFuture<Void> deleteHome(Clan clan) {
        clan.setHome(null);
        plugin.debug("Deleted home for clan " + clan.getName());
        return plugin.getClanManager().updateClan(clan);
    }
    
    public boolean hasHome(Clan clan) {
        return clan.getHome() != null;
    }
    
    public Location getHome(Clan clan) {
        if (clan.getHome() == null) {
            return null;
        }
        
        ClanHome home = clan.getHome();
        World world = Bukkit.getWorld(home.getWorld());
        if (world == null) {
            return null;
        }
        
        return new Location(world, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());
    }
    
    public void teleportHome(Player player, Clan clan) {
        Location home = getHome(clan);
        if (home == null) {
            return;
        }
        
        plugin.debug("Teleporting " + player.getName() + " to clan home");
        
        plugin.getFoliaLib().getScheduler().teleportAsync(player, home)
            .thenAccept(success -> {
                if (success) {
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    plugin.debug("Player " + player.getName() + " teleported successfully");
                } else {
                    plugin.warn("Failed to teleport " + player.getName() + " to clan home");
                }
            });
    }
    
    public boolean hasCooldown(UUID playerUUID) {
        if (!cooldowns.containsKey(playerUUID)) {
            return false;
        }
        
        long lastTeleport = cooldowns.get(playerUUID);
        long cooldownSeconds = plugin.getConfigManager().homeCooldown();
        long elapsed = (System.currentTimeMillis() - lastTeleport) / 1000;
        
        return elapsed < cooldownSeconds;
    }
    
    public long getRemainingCooldown(UUID playerUUID) {
        if (!cooldowns.containsKey(playerUUID)) {
            return 0;
        }
        
        long lastTeleport = cooldowns.get(playerUUID);
        long cooldownSeconds = plugin.getConfigManager().homeCooldown();
        long elapsed = (System.currentTimeMillis() - lastTeleport) / 1000;
        
        return Math.max(0, cooldownSeconds - elapsed);
    }
}