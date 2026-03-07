package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanMember;
import com.clansystem.data.ClanRank;
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

public class PromoteDemoteGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Clan clan;
    private final ClanMember targetMember;
    
    public PromoteDemoteGUI(ClanSystem plugin, Clan clan, ClanMember targetMember) {
        this.plugin = plugin;
        this.clan = clan;
        this.targetMember = targetMember;
    }
    
    @Override
    protected Inventory createInventory() {
        String title = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessageManager().getMessage("gui.promote-demote-title")
                .replace("{player}", targetMember.getPlayerName()));
        return Bukkit.createInventory(null, 27, title);
    }
    
    @Override
    public void decorate(Player player) {
        if (targetMember.getRank() != ClanRank.MOD) {
            addButton(11, new InventoryButton()
                .creator(p -> createItem("EMERALD",
                    "&aPromote to Moderator",
                    "&7Give this player",
                    "&7moderator permissions"))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    if (!clan.isOwner(clicker.getUniqueId())) {
                        plugin.getMessageManager().send(clicker, "clan.only-owner");
                        clicker.closeInventory();
                        return;
                    }
                    targetMember.setRank(ClanRank.MOD);
                    plugin.getClanManager().updateClan(clan);
                    
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", targetMember.getPlayerName());
                    placeholders.put("rank", ClanRank.MOD.getDisplayName());
                    plugin.getMessageManager().send(clicker, "member.promoted", placeholders);
                    
                    Player target = Bukkit.getPlayer(targetMember.getPlayerUUID());
                    if (target != null) {
                        plugin.getMessageManager().send(target, "member.promoted", placeholders);
                    }
                    
                    clicker.closeInventory();
                })
            );
        }
        
        if (targetMember.getRank() != ClanRank.MEMBER) {
            addButton(13, new InventoryButton()
                .creator(p -> createItem("REDSTONE",
                    "&cDemote to Member",
                    "&7Remove moderator",
                    "&7permissions"))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    if (!clan.isOwner(clicker.getUniqueId())) {
                        plugin.getMessageManager().send(clicker, "clan.only-owner");
                        clicker.closeInventory();
                        return;
                    }
                    targetMember.setRank(ClanRank.MEMBER);
                    plugin.getClanManager().updateClan(clan);
                    
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", targetMember.getPlayerName());
                    placeholders.put("rank", ClanRank.MEMBER.getDisplayName());
                    plugin.getMessageManager().send(clicker, "member.demoted", placeholders);
                    
                    Player target = Bukkit.getPlayer(targetMember.getPlayerUUID());
                    if (target != null) {
                        plugin.getMessageManager().send(target, "member.demoted", placeholders);
                    }
                    
                    clicker.closeInventory();
                })
            );
        }
        
        addButton(15, new InventoryButton()
            .creator(p -> createItem("BARRIER",
                "&cKick Member",
                "&7Remove this player",
                "&7from the clan"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ConfirmKickGUI(plugin, clan, targetMember), clicker);
            })
        );
        
        addButton(22, new InventoryButton()
            .creator(p -> createItem("ARROW", "&cBack", "&7Return to members list"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanMembersGUI(plugin, clan), clicker, false);
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