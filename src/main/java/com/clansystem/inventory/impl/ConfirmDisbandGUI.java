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

public class ConfirmDisbandGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Clan clan;
    
    public ConfirmDisbandGUI(ClanSystem plugin, Clan clan) {
        this.plugin = plugin;
        this.clan = clan;
    }
    
    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, 27, ColorUtil.colorize("&4&lConfirm Disband"));
    }
    
    @Override
    public void decorate(Player player) {
        addButton(11, new InventoryButton()
            .creator(p -> createItem("LIME_WOOL", "&a&lConfirm", "&7Click to disband clan", "&c&lThis cannot be undone!"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                
                plugin.getClanManager().disbandClan(clan.getId()).thenRun(() -> {
                    plugin.getMessageManager().send(clicker, "clan.disbanded");
                    plugin.getSoundManager().playDisband(clicker);
                });
            })
        );
        
        addButton(15, new InventoryButton()
            .creator(p -> createItem("RED_WOOL", "&c&lCancel", "&7Return to clan menu"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanMainGUI(plugin, clan), clicker, false);
            })
        );
        
        addButton(13, new InventoryButton()
            .creator(p -> createItem("TNT", "&4&lDisband Clan", "&7Are you sure?", "&cAll data will be lost!"))
            .consumer(event -> {})
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