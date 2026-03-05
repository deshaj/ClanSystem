package com.clansystem.listener;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanMember;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;

public class ChatListener implements Listener {
    private final ClanSystem plugin;
    
    public ChatListener(ClanSystem plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getClanChatManager().isClanChatEnabled(player.getUniqueId())) {
            return;
        }
        
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            return;
        }
        
        event.setCancelled(true);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("message", event.getMessage());
        
        String message = plugin.getMessageManager().getMessage("chat.format", placeholders);
        
        for (ClanMember member : clan.getMembers()) {
            Player memberPlayer = plugin.getServer().getPlayer(member.getPlayerUUID());
            if (memberPlayer != null && memberPlayer.isOnline()) {
                memberPlayer.sendMessage(message);
            }
        }
    }
}