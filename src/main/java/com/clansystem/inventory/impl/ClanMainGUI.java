package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.inventory.InventoryButton;
import com.clansystem.inventory.InventoryGUI;
import com.clansystem.util.ColorUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanMainGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Clan clan;
    
    public ClanMainGUI(ClanSystem plugin, Clan clan) {
        this.plugin = plugin;
        this.clan = clan;
    }
    
    @Override
    protected Inventory createInventory() {
        String title = plugin.getMessageManager().getMessage("gui.main-title");
        return Bukkit.createInventory(null, 27, title);
    }
    
    @Override
    public void decorate(Player player) {
        int maxMembers = plugin.getConfigManager().maxMembers();
        Map<String, String> memberPlaceholders = new HashMap<>();
        memberPlaceholders.put("count", String.valueOf(clan.getMemberCount()));
        memberPlaceholders.put("max", String.valueOf(maxMembers));
        
        addButton(11, new InventoryButton()
            .creator(p -> {
                String name = plugin.getMessageManager().getMessage("gui.items.members.name");
                return createItemWithLore("PLAYER_HEAD", name, 
                    plugin.getMessageManager().getLore("gui.items.members.lore", memberPlaceholders));
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanMembersGUI(plugin, clan), clicker);
            })
        );
        
        int level = plugin.getLevelManager().calculateLevel(clan);
        double kd = clan.getTotalDeaths() > 0 ? 
            Math.round((double) clan.getTotalKills() / clan.getTotalDeaths() * 100.0) / 100.0 : clan.getTotalKills();
        
        Map<String, String> statsPlaceholders = new HashMap<>();
        statsPlaceholders.put("kills", String.valueOf(clan.getTotalKills()));
        statsPlaceholders.put("deaths", String.valueOf(clan.getTotalDeaths()));
        statsPlaceholders.put("kd", String.valueOf(kd));
        statsPlaceholders.put("level", String.valueOf(level));
        
        addButton(13, new InventoryButton()
            .creator(p -> {
                String name = plugin.getMessageManager().getMessage("gui.items.stats.name");
                return createItemWithLore("DIAMOND_SWORD", name,
                    plugin.getMessageManager().getLore("gui.items.stats.lore", statsPlaceholders));
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanStatsGUI(plugin, clan), clicker);
            })
        );
        
        addButton(15, new InventoryButton()
            .creator(p -> {
                String name = plugin.getMessageManager().getMessage("gui.items.home.name");
                if (clan.getHome() != null) {
                    String status = plugin.getMessageManager().getMessage("gui.items.home.status-set");
                    Map<String, String> homePlaceholders = new HashMap<>();
                    homePlaceholders.put("status", status);
                    return createItemWithLore("RED_BED", name,
                        plugin.getMessageManager().getLore("gui.items.home.lore", homePlaceholders));
                } else {
                    return createItemWithLore("WHITE_BED", name,
                        plugin.getMessageManager().getLore("gui.items.home.no-home-lore"));
                }
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                if (clan.getHome() == null) {
                    plugin.getMessageManager().send(clicker, "home.no-home");
                    return;
                }
                if (plugin.getHomeManager().hasCooldown(clicker.getUniqueId())) {
                    long remaining = plugin.getHomeManager().getRemainingCooldown(clicker.getUniqueId());
                    plugin.getMessageManager().send(clicker, "home.cooldown",
                        Map.of("time", String.valueOf(remaining)));
                    return;
                }
                plugin.getHomeManager().teleportHome(clicker, clan);
                plugin.getMessageManager().send(clicker, "home.teleporting");
                clicker.closeInventory();
            })
        );
        
        addButton(22, new InventoryButton()
            .creator(p -> {
                String name = plugin.getMessageManager().getMessage("gui.items.lookup.name");
                return createItemWithLore("COMPASS", name,
                    plugin.getMessageManager().getLore("gui.items.lookup.lore"));
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanLookupGUI(plugin, 1), clicker);
            })
        );
        
        super.decorate(player);
    }
    
    private ItemStack createItemWithLore(String materialName, String name, List<String> lore) {
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem)
            .orElse(new ItemStack(Material.STONE));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}