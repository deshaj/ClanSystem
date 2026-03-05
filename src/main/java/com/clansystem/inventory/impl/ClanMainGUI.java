package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.inventory.InventoryButton;
import com.clansystem.inventory.InventoryGUI;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ClanMainGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Clan clan;
    
    public ClanMainGUI(ClanSystem plugin, Clan clan) {
        this.plugin = plugin;
        this.clan = clan;
    }
    
    @Override
    protected Inventory createInventory() {
        String title = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessageManager().getMessage("gui.main-title"));
        return Bukkit.createInventory(null, 27, title);
    }
    
    @Override
    public void decorate(Player player) {
        addButton(11, new InventoryButton()
            .creator(p -> createItem("PLAYER_HEAD", 
                plugin.getMessageManager().getIcon("members") + " &bMembers",
                "&7Click to view clan members",
                "&7and manage ranks"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanMembersGUI(plugin, clan), clicker);
            })
        );
        
        addButton(13, new InventoryButton()
            .creator(p -> createItem("DIAMOND_SWORD",
                plugin.getMessageManager().getIcon("stats") + " &6Statistics",
                "&7Total Kills: &f" + clan.getTotalKills(),
                "&7Total Deaths: &f" + clan.getTotalDeaths(),
                "&7Level: &f" + plugin.getLevelManager().calculateLevel(clan)))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanStatsGUI(plugin, clan), clicker);
            })
        );
        
        addButton(15, new InventoryButton()
            .creator(p -> {
                if (clan.getHome() != null) {
                    return createItem("RED_BED",
                        plugin.getMessageManager().getIcon("home") + " &eClan Home",
                        "&7Click to teleport to",
                        "&7your clan home");
                } else {
                    return createItem("WHITE_BED",
                        plugin.getMessageManager().getIcon("home") + " &eClan Home",
                        "&cNo home set!",
                        "&7Use /clan sethome");
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
                        java.util.Map.of("time", String.valueOf(remaining)));
                    return;
                }
                plugin.getHomeManager().teleportHome(clicker, clan);
                plugin.getMessageManager().send(clicker, "home.teleporting");
                clicker.closeInventory();
            })
        );
        
        addButton(22, new InventoryButton()
            .creator(p -> createItem("COMPASS",
                "&aLookup Clans",
                "&7Browse all clans",
                "&7on the server"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanLookupGUI(plugin, 1), clicker);
            })
        );
        
        super.decorate(player);
    }
    
    private ItemStack createItem(String materialName, String name, String... lore) {
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.STONE));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(Arrays.stream(lore)
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .toList());
        item.setItemMeta(meta);
        return item;
    }
}