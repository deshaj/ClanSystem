package com.clansystem.listener;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanMember;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerListener implements Listener {
    private final ClanSystem plugin;
    private final Map<Player, Long> loginTimes = new HashMap<>();
    
    public PlayerListener(ClanSystem plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().getPlayerClan(player.getUniqueId()).thenAccept(clanId -> {
            if (clanId != null) {
                plugin.debug("Loaded clan data for " + player.getName() + ": " + clanId);
            }
        });
        
        plugin.getPlayerRepository().getPendingNotifications(player.getUniqueId()).thenAccept(notifications -> {
            if (!notifications.isEmpty()) {
                plugin.getFoliaLib().getScheduler().runAtEntity(player, task -> {
                    for (Map<String, String> notification : notifications) {
                        String type = notification.get("type");
                        String clanName = notification.get("clan");
                        
                        if ("ACCEPTED".equals(type)) {
                            plugin.getMessageManager().send(player, "invitation.accepted", Map.of("clan", clanName));
                        } else if ("DECLINED".equals(type)) {
                            plugin.getMessageManager().send(player, "invitation.declined", Map.of("clan", clanName));
                        }
                    }
                    
                    plugin.getPlayerRepository().clearPendingNotifications(player.getUniqueId());
                });
            }
        });
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().clearCache(player.getUniqueId());
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        Clan victimClan = plugin.getClanManager().getPlayerClan(victim.getUniqueId());
        Clan damagerClan = plugin.getClanManager().getPlayerClan(damager.getUniqueId());

        if (victimClan != null && damagerClan != null && victimClan.getId().equals(damagerClan.getId())) {
            if (!victimClan.isPvpEnabled()) {
                event.setCancelled(true);
                plugin.debug("Cancelled PvP between clan members: " + damager.getName() + " -> " + victim.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        Clan victimClan = plugin.getClanManager().getPlayerClan(victim.getUniqueId());
        if (victimClan != null) {
            victimClan.addDeath();
            ClanMember victimMember = victimClan.getMember(victim.getUniqueId());
            if (victimMember != null) {
                victimMember.setDeaths(victimMember.getDeaths() + 1);
            }
            plugin.getClanManager().updateClan(victimClan);
        }
        
        if (killer != null) {
            Clan killerClan = plugin.getClanManager().getPlayerClan(killer.getUniqueId());
            if (killerClan != null) {
                int oldLevel = plugin.getLevelManager().calculateLevel(killerClan);
                
                killerClan.addKill();
                ClanMember killerMember = killerClan.getMember(killer.getUniqueId());
                if (killerMember != null) {
                    killerMember.setKills(killerMember.getKills() + 1);
                }
                plugin.getClanManager().updateClan(killerClan);
                
                int newLevel = plugin.getLevelManager().calculateLevel(killerClan);
                if (newLevel > oldLevel) {
                    String newTag = plugin.getLevelManager().getClanTag(killerClan);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("level", String.valueOf(newLevel));
                    placeholders.put("tag", newTag);
                    
                    for (ClanMember member : killerClan.getMembers()) {
                        Player memberPlayer = plugin.getServer().getPlayer(member.getPlayerUUID());
                        if (memberPlayer != null && memberPlayer.isOnline()) {
                            plugin.getMessageManager().send(memberPlayer, "level.up", placeholders);
                        }
                    }
                }
            }
        }
    }
}