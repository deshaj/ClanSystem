package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanMember;
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
import java.util.HashMap;
import java.util.Map;

public class ConfirmKickGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Clan clan;
    private final ClanMember targetMember;
    
    public ConfirmKickGUI(ClanSystem plugin, Clan clan, ClanMember targetMember) {
        this.plugin = plugin;
        this.clan = clan;
        this.targetMember = targetMember;
    }
    
    @Override
    protected Inventory createInventory() {
        String title = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessageManager().getMessage("gui.confirm-kick-title"));
        return Bukkit.createInventory(null, 27, title);
    }
    
    @Override
    public void decorate(Player player) {
        addButton(11, new InventoryButton()
            .creator(p -> createItem("EMERALD_BLOCK",
                "&aConfirm Kick",
                "&7Click to kick",
                "&b" + targetMember.getPlayerName()))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                
                if (targetMember.getPlayerUUID().equals(clicker.getUniqueId())) {
                    plugin.getMessageManager().send(clicker, "member.cannot-kick-self");
                    plugin.getSoundManager().play(clicker, "error");
                    clicker.closeInventory();
                    return;
                }
                
                clan.removeMember(targetMember.getPlayerUUID());
                plugin.getClanManager().removeMember(clan, targetMember.getPlayerUUID());
                
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", targetMember.getPlayerName());
                placeholders.put("clan", clan.getName());
                plugin.getMessageManager().send(clicker, "member.kicked", placeholders);
                plugin.getSoundManager().play(clicker, "kick");
                
                Player target = Bukkit.getPlayer(targetMember.getPlayerUUID());
                if (target != null) {
                    plugin.getMessageManager().send(target, "clan.kicked", placeholders);
                    plugin.getSoundManager().play(target, "kicked");
                }
                
                clicker.closeInventory();
            })
        );
        
        addButton(15, new InventoryButton()
            .creator(p -> createItem("REDSTONE_BLOCK",
                "&cCancel",
                "&7Go back"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new PromoteDemoteGUI(plugin, clan, targetMember), clicker, false);
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