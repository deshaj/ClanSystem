package com.clansystem.placeholder;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClanPlaceholder extends PlaceholderExpansion {
    private final ClanSystem plugin;
    
    public ClanPlaceholder(ClanSystem plugin) {
        this.plugin = plugin;
    }
    
    @Override
    @NotNull
    public String getIdentifier() {
        return "clan";
    }
    
    @Override
    @NotNull
    public String getAuthor() {
        return "ClanSystem";
    }
    
    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }
        
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        
        if (clan == null) {
            return "";
        }
        
        return switch (identifier.toLowerCase()) {
            case "name" -> clan.getName();
            case "tag" -> plugin.getLevelManager().getClanTag(clan);
            case "level" -> String.valueOf(plugin.getLevelManager().calculateLevel(clan));
            case "kills" -> String.valueOf(clan.getTotalKills());
            case "deaths" -> String.valueOf(clan.getTotalDeaths());
            case "members" -> String.valueOf(clan.getMemberCount());
            case "owner" -> {
                Player owner = plugin.getServer().getPlayer(clan.getOwner());
                yield owner != null ? owner.getName() : "Unknown";
            }
            default -> "";
        };
    }
}