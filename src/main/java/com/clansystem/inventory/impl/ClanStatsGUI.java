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

import java.util.ArrayList;
import java.util.List;

public class ClanStatsGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Clan clan;
    
    public ClanStatsGUI(ClanSystem plugin, Clan clan) {
        this.plugin = plugin;
        this.clan = clan;
    }
    
    @Override
    protected Inventory createInventory() {
        String title = plugin.getMessageManager().getMessage("gui.stats-title");
        return Bukkit.createInventory(null, 27, title);
    }
    
    @Override
    public void decorate(Player player) {
        int level = plugin.getLevelManager().calculateLevel(clan);
        String tag = plugin.getLevelManager().getClanTag(clan);
        int nextRequired = plugin.getLevelManager().getNextLevelKills(clan);
        
        addButton(11, new InventoryButton()
            .creator(p -> createItem("DIAMOND_SWORD",
                "&6Total Kills",
                "&7" + clan.getTotalKills()))
            .consumer(event -> {})
        );
        
        addButton(13, new InventoryButton()
            .creator(p -> createItem("SKELETON_SKULL",
                "&cTotal Deaths",
                "&7" + clan.getTotalDeaths()))
            .consumer(event -> {})
        );
        
        addButton(15, new InventoryButton()
            .creator(p -> {
                if (plugin.getLevelManager().isMaxLevel(clan)) {
                    return createItem("NETHER_STAR",
                        plugin.getMessageManager().getIcon("level") + " &dLevel " + level,
                        "&7TAG: " + tag,
                        "",
                        "&dMAX LEVEL!");
                } else {
                    return createItem("EXPERIENCE_BOTTLE",
                        plugin.getMessageManager().getIcon("level") + " &dLevel " + level,
                        "&7TAG: " + tag,
                        "",
                        "&7Next: &f" + clan.getTotalKills() + "&7/&f" + nextRequired);
                }
            })
            .consumer(event -> {})
        );
        
        addButton(22, new InventoryButton()
            .creator(p -> createItem("ARROW", "&cBack", "&7Return to main menu"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanMainGUI(plugin, clan), clicker);
            })
        );
        
        super.decorate(player);
    }
    
    private ItemStack createItem(String materialName, String name, String... lore) {
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem)
            .orElse(new ItemStack(Material.STONE));
        ItemMeta meta = item.getItemMeta();
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ColorUtil.colorize(line));
        }
        
        meta.setDisplayName(ColorUtil.colorize(name));
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }
}