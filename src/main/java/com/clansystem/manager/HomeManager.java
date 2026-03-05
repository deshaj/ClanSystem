package com.clansystem.manager;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanHome;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeManager {
    private final ClanSystem plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    public HomeManager(ClanSystem plugin) {
        this.plugin = plugin;
    }
    
    public void setHome(Clan clan, Location location) {
        ClanHome home = ClanHome.fromLocation(location);
        clan.setHome(home);
        plugin.getClanManager().updateClan(clan);
    }
    
    public void deleteHome(Clan clan) {
        clan.setHome(null);
        plugin.getClanManager().updateClan(clan);
    }
    
    public boolean hasHome(Clan clan) {
        return clan.getHome() != null;
    }
    
    public Location getHome(Clan clan) {
        if (clan.getHome() == null) {
            return null;
        }
        return clan.getHome().toLocation();
    }
    
    public void teleportHome(Player player, Clan clan) {
        Location home = getHome(clan);
        if (home != null) {
            player.teleport(home);
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
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