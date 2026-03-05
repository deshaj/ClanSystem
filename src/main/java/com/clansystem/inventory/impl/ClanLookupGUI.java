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

import java.text.SimpleDateFormat;
import java.util.*;

public class ClanLookupGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final int page;
    private static final int CLANS_PER_PAGE = 45;
    
    public ClanLookupGUI(ClanSystem plugin, int page) {
        this.plugin = plugin;
        this.page = page;
    }
    
    @Override
    protected Inventory createInventory() {
        List<Clan> allClans = plugin.getClanManager().getClansSorted();
        int totalPages = (int) Math.ceil((double) allClans.size() / CLANS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        String title = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessageManager().getMessage("gui.lookup-title")
                .replace("{page}", String.valueOf(page))
                .replace("{pages}", String.valueOf(totalPages)));
        return Bukkit.createInventory(null, 54, title);
    }
    
    @Override
    public void decorate(Player player) {
        List<Clan> allClans = plugin.getClanManager().getClansSorted();
        int totalPages = (int) Math.ceil((double) allClans.size() / CLANS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        int startIndex = (page - 1) * CLANS_PER_PAGE;
        int endIndex = Math.min(startIndex + CLANS_PER_PAGE, allClans.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Clan clan = allClans.get(i);
            int slot = i - startIndex;
            
            addButton(slot, new InventoryButton()
                .creator(p -> createClanItem(clan))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    showClanInfo(clicker, clan);
                    clicker.closeInventory();
                })
            );
        }
        
        if (page > 1) {
            addButton(48, new InventoryButton()
                .creator(p -> createItem("ARROW", "&aPrevious Page"))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getGuiManager().openGUI(new ClanLookupGUI(plugin, page - 1), clicker);
                })
            );
        }
        
        if (page < totalPages) {
            addButton(50, new InventoryButton()
                .creator(p -> createItem("ARROW", "&aNext Page"))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getGuiManager().openGUI(new ClanLookupGUI(plugin, page + 1), clicker);
                })
            );
        }
        
        addButton(49, new InventoryButton()
            .creator(p -> createItem("BARRIER", "&cClose"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
            })
        );
        
        super.decorate(player);
    }
    
    private ItemStack createClanItem(Clan clan) {
        int level = plugin.getLevelManager().calculateLevel(clan);
        String tag = plugin.getLevelManager().getClanTag(clan);
        
        ItemStack item = XMaterial.matchXMaterial("BANNER").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.WHITE_BANNER));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + clan.getName()));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Level: &f" + level));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Tag: " + tag));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Members: &f" + clan.getMemberCount()));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Kills: &f" + clan.getTotalKills()));
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&eClick for more info"));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private void showClanInfo(Player player, Clan clan) {
        int level = plugin.getLevelManager().calculateLevel(clan);
        String tag = plugin.getLevelManager().getClanTag(clan);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        String createdDate = sdf.format(new Date(clan.getCreatedAt()));
        
        Player owner = Bukkit.getPlayer(clan.getOwner());
        String ownerName = owner != null ? owner.getName() : "Unknown";
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("clan", clan.getName());
        placeholders.put("owner", ownerName);
        placeholders.put("count", String.valueOf(clan.getMemberCount()));
        placeholders.put("max", String.valueOf(plugin.getConfigManager().maxMembers()));
        placeholders.put("level", String.valueOf(level));
        placeholders.put("tag", tag);
        placeholders.put("kills", String.valueOf(clan.getTotalKills()));
        placeholders.put("deaths", String.valueOf(clan.getTotalDeaths()));
        placeholders.put("date", createdDate);
        
        player.sendMessage(plugin.getMessageManager().getMessage("info.header", placeholders));
        player.sendMessage(plugin.getMessageManager().getMessage("info.owner", placeholders));
        player.sendMessage(plugin.getMessageManager().getMessage("info.members", placeholders));
        player.sendMessage(plugin.getMessageManager().getMessage("info.level", placeholders));
        player.sendMessage(plugin.getMessageManager().getMessage("info.tag", placeholders));
        player.sendMessage(plugin.getMessageManager().getMessage("info.stats", placeholders));
        player.sendMessage(plugin.getMessageManager().getMessage("info.created", placeholders));
    }
    
    private ItemStack createItem(String materialName, String name, String... lore) {
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.STONE));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (lore.length > 0) {
            meta.setLore(Arrays.stream(lore)
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .toList());
        }
        item.setItemMeta(meta);
        return item;
    }
}