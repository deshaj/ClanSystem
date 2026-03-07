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
    
@EventHandler(priority = EventPriority.LOWEST)
public void onChat(AsyncPlayerChatEvent event) {
    Player player = event.getPlayer();
    
    plugin.debug("ChatListener: Event triggered for " + player.getName() + " - Message: '" + event.getMessage() + "'");
    plugin.debug("ChatListener: Event cancelled: " + event.isCancelled());
    
    if (plugin.getChatInputManager().isAwaiting(player.getUniqueId())) {
        plugin.debug("ChatListener: Player is awaiting input, cancelling event");
        event.setCancelled(true);
        final String message = event.getMessage();
        
        if (message.equalsIgnoreCase("cancel")) {
            plugin.debug("ChatListener: Player typed 'cancel', cancelling prompt");
            plugin.getFoliaLib().getScheduler().runAtEntity(player, task -> {
                plugin.getChatInputManager().cancel(player.getUniqueId());
                plugin.getMessageManager().send(player, "invitation.cancelled");
            });
            return;
        }
        
        plugin.debug("ChatListener: Scheduling input handler on entity thread");
        plugin.getFoliaLib().getScheduler().runAtEntity(player, task -> {
            plugin.debug("ChatListener: Entity thread executing handleInput");
            plugin.getChatInputManager().handleInput(player, message);
        });
        return;
    }
    
    plugin.debug("ChatListener: Player not awaiting input, checking clan chat");
        
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