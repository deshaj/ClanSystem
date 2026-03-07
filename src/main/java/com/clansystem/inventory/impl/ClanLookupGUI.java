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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanLookupGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final int page;
    private static final int CLANS_PER_PAGE = 45;
    
    private final Player playerContext;

    public ClanLookupGUI(ClanSystem plugin, int page) {
        this.plugin = plugin;
        this.page = page;
        this.playerContext = null;
    }

    public ClanLookupGUI(ClanSystem plugin, Player player, int page) {
        this.plugin = plugin;
        this.page = page;
        this.playerContext = player;
    }
    
    @Override
    protected Inventory createInventory() {
        List<Clan> allClans = plugin.getClanManager().getClansSorted();
        int totalPages = (int) Math.ceil((double) allClans.size() / CLANS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("page", String.valueOf(page));
        placeholders.put("pages", String.valueOf(totalPages));
        
        String title = plugin.getMessageManager().getMessage("gui.lookup-title", placeholders);
        return Bukkit.createInventory(null, 54, title);
    }
    
    @Override
    public void decorate(Player player) {
        String fillerMat = plugin.getConfigManager().getString("gui.lookup-menu.filler.material", "GRAY_STAINED_GLASS_PANE");
        String fillerName = plugin.getConfigManager().getString("gui.lookup-menu.filler.name", " ");
        ItemStack filler = XMaterial.matchXMaterial(fillerMat).map(XMaterial::parseItem).orElse(null);
        if (filler != null) {
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(plugin.getMessageManager().format(fillerName));
                filler.setItemMeta(fillerMeta);
            }
            for (int i = 45; i < 54; i++) {
                if (i != 45 && i != 49 && i != 53) {
                    getInventory().setItem(i, filler);
                }
            }
        }

        List<Clan> allClans = plugin.getClanManager().getClansSorted();
        int totalPages = (int) Math.ceil((double) allClans.size() / CLANS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        int startIndex = (page - 1) * CLANS_PER_PAGE;
        int endIndex = Math.min(startIndex + CLANS_PER_PAGE, allClans.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Clan clan = allClans.get(i);
            int slot = i - startIndex;
            
            addButton(slot, new InventoryButton()
                .creator(p -> createClanItemForPlayer(p, clan))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    if (event.isLeftClick()) {
                        BrowseClanDetailsGUI detailsGUI = new BrowseClanDetailsGUI(plugin, clicker, clan, page);
                        plugin.getGuiManager().openGUI(detailsGUI, clicker);
                    } else if (event.isRightClick()) {
                        clicker.closeInventory();
                        plugin.getMessageManager().send(clicker, "invitation.prompt-join-message");
                        plugin.getClanChatManager().setSendingJoinRequest(clicker.getUniqueId(), clan.getId());
                    }
                })
            );
        }
        
        if (page > 1) {
            addButton(45, new InventoryButton()
                .creator(p -> createItem("ARROW", "&aPrevious Page"))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    ClanLookupGUI lookupGUI = playerContext != null ? 
                        new ClanLookupGUI(plugin, playerContext, page - 1) : 
                        new ClanLookupGUI(plugin, page - 1);
                    plugin.getGuiManager().openGUI(lookupGUI, clicker, false);
                })
            );
        }
        
        if (page < totalPages) {
            addButton(53, new InventoryButton()
                .creator(p -> createItem("ARROW", "&aNext Page"))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    ClanLookupGUI lookupGUI = playerContext != null ? 
                        new ClanLookupGUI(plugin, playerContext, page + 1) : 
                        new ClanLookupGUI(plugin, page + 1);
                    plugin.getGuiManager().openGUI(lookupGUI, clicker, false);
                })
            );
        }
        
        addButton(49, new InventoryButton()
            .creator(p -> createItem("BARRIER", "&cBack to Menu"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                if (playerContext != null) {
                    plugin.getGuiManager().openGUI(new NoClanGUI(plugin, clicker), clicker, false);
                } else {
                    clicker.closeInventory();
                }
            })
        );
        
        super.decorate(player);
    }
    
    private ItemStack createClanItem(Clan clan) {
        int level = plugin.getLevelManager().calculateLevel(clan);
        String tag = plugin.getLevelManager().getClanTag(clan);
        
        ItemStack item = XMaterial.matchXMaterial("BANNER").map(XMaterial::parseItem)
            .orElse(new ItemStack(Material.WHITE_BANNER));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.colorize("&#3498DB" + clan.getName()));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.colorize("&#95A5A6Level: &f" + level));
        lore.add(ColorUtil.colorize("&#95A5A6Tag: " + tag));
        lore.add(ColorUtil.colorize("&#95A5A6Members: &f" + clan.getMemberCount()));
        lore.add(ColorUtil.colorize("&#95A5A6Kills: &f" + clan.getTotalKills()));
        lore.add("");
        lore.add(ColorUtil.colorize("&#F1C40FClick for more info"));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createClanItemForPlayer(Player player, Clan clan) {
        int level = plugin.getLevelManager().calculateLevel(clan);
        String tag = plugin.getLevelManager().getClanTag(clan);
        
        Clan playerClan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        boolean isOwnClan = playerClan != null && playerClan.getId().equals(clan.getId());
        
        String bannerMaterial = isOwnClan ? "LIME_BANNER" : "WHITE_BANNER";
        ItemStack item = XMaterial.matchXMaterial(bannerMaterial).map(XMaterial::parseItem)
            .orElse(new ItemStack(Material.WHITE_BANNER));
        ItemMeta meta = item.getItemMeta();
        
        String displayName = isOwnClan ? "&#2ECC71" + clan.getName() + " &#2ECC71(Your Clan)" : "&#3498DB" + clan.getName();
        meta.setDisplayName(ColorUtil.colorize(displayName));
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.colorize("&#95A5A6Level: &f" + level));
        lore.add(ColorUtil.colorize("&#95A5A6Tag: " + tag));
        lore.add(ColorUtil.colorize("&#95A5A6Members: &f" + clan.getMemberCount()));
        lore.add(ColorUtil.colorize("&#95A5A6Kills: &f" + clan.getTotalKills()));
        lore.add("");
        lore.add(ColorUtil.colorize("&#F1C40FClick for more info"));
        
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
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem)
            .orElse(new ItemStack(Material.STONE));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.colorize(name));
        
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(ColorUtil.colorize(line));
            }
            meta.setLore(loreList);
        }
        
        item.setItemMeta(meta);
        return item;
    }
}