package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanMember;
import com.clansystem.data.ClanRank;
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
        addFillerGlass();
        
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
        
        ClanMember member = clan.getMember(player.getUniqueId());
        if (member != null && (member.getRank() == ClanRank.OWNER || member.getRank() == ClanRank.MOD)) {
            addButton(9, new InventoryButton()
                .creator(p -> {
                    int requestCount = plugin.getInvitationManager().getClanRequests(clan.getId()).size();
                    String name = plugin.getMessageManager().getMessage("gui.items.join-requests.name", 
                        Map.of("count", String.valueOf(requestCount)));
                    return createItemWithLore("WRITABLE_BOOK", name,
                        plugin.getMessageManager().getLore("gui.items.join-requests.lore", Map.of("count", String.valueOf(requestCount))));
                })
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getGuiManager().openGUI(new JoinRequestsGUI(plugin, clicker, clan), clicker);
                })
            );
        }

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
                    if (clan.canManageMembers(clicker.getUniqueId())) {
                        plugin.getHomeManager().setHome(clan, clicker.getLocation());
                        plugin.getMessageManager().send(clicker, "home.set-gui");
                        plugin.getSoundManager().play(clicker, "home-set");
                        clicker.closeInventory();
                        plugin.getFoliaLib().getImpl().runLater(() -> {
                            plugin.getGuiManager().openGUI(new ClanMainGUI(plugin, clan), clicker);
                        }, 2L);
                    } else {
                        plugin.getMessageManager().send(clicker, "home.no-home");
                    }
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
        
        addButton(20, new InventoryButton()
            .creator(p -> {
                String name = plugin.getMessageManager().getMessage("gui.items.pvp.name");
                if (clan.isPvpEnabled()) {
                    return createItemWithLore("DIAMOND_SWORD", name,
                        plugin.getMessageManager().getLore("gui.items.pvp.lore-enabled"));
                } else {
                    return createItemWithLore("WOODEN_SWORD", name,
                        plugin.getMessageManager().getLore("gui.items.pvp.lore-disabled"));
                }
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                if (!clan.isOwner(clicker.getUniqueId())) {
                    plugin.getMessageManager().send(clicker, "clan.only-owner");
                    return;
                }
                clan.setPvpEnabled(!clan.isPvpEnabled());
                plugin.getClanManager().updateClan(clan);
                String messageKey = clan.isPvpEnabled() ? "pvp.enabled" : "pvp.disabled";
                plugin.getMessageManager().send(clicker, messageKey);
                plugin.getSoundManager().play(clicker, clan.isPvpEnabled() ? "success" : "error");
                
                plugin.getFoliaLib().getImpl().runLater(() -> {
                    plugin.getGuiManager().openGUI(new ClanMainGUI(plugin, clan), clicker);
                }, 2L);
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
                plugin.getGuiManager().openGUI(new ClanLookupGUI(plugin, 1), clicker, false);
            })
        );
        
        if (clan.isOwner(player.getUniqueId())) {
            addButton(26, new InventoryButton()
                .creator(p -> createItemWithLore("TNT", 
                    ColorUtil.colorize("&c&lDisband Clan"),
                    List.of(ColorUtil.colorize("&7Click to disband your clan"), 
                            ColorUtil.colorize("&c&lWarning: This cannot be undone!"))))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getGuiManager().openGUI(new ConfirmDisbandGUI(plugin, clan), clicker, false);
                })
            );
        }
        
        super.decorate(player);
    }

    private void addFillerGlass() {
        boolean enabled = plugin.getConfigManager().getBoolean("gui.main-menu.filler.enabled", true);
        if (!enabled) return;

        String materialName = plugin.getConfigManager().getString("gui.main-menu.filler.material", "GRAY_STAINED_GLASS_PANE");
        String name = plugin.getConfigManager().getString("gui.main-menu.filler.name", " ");
        
        ItemStack filler = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(null);
        if (filler == null) return;
        
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().format(name));
            filler.setItemMeta(meta);
        }

        for (int i = 0; i < getInventory().getSize(); i++) {
            if (i != 9 && i != 11 && i != 13 && i != 15 && i != 20 && i != 22 && i != 26) {
                getInventory().setItem(i, filler);
            }
        }
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