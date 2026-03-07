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
        
        int membersSlot = plugin.getConfigManager().getInt("gui.main-menu.items.members.slot", 11);
        String membersMaterial = plugin.getConfigManager().getString("gui.main-menu.items.members.material", "PLAYER_HEAD");
        addButton(membersSlot, new InventoryButton()
            .creator(p -> {
                String name = plugin.getMessageManager().getMessage("gui.items.members.name");
                return createItemWithLore(membersMaterial, name, 
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
        
        int statsSlot = plugin.getConfigManager().getInt("gui.main-menu.items.stats.slot", 13);
        String statsMaterial = plugin.getConfigManager().getString("gui.main-menu.items.stats.material", "DIAMOND_SWORD");
        addButton(statsSlot, new InventoryButton()
            .creator(p -> {
                String name = plugin.getMessageManager().getMessage("gui.items.stats.name");
                return createItemWithLore(statsMaterial, name,
                    plugin.getMessageManager().getLore("gui.items.stats.lore", statsPlaceholders));
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanStatsGUI(plugin, clan), clicker);
            })
        );
        
        int joinRequestsSlot = plugin.getConfigManager().getInt("gui.main-menu.items.join-requests.slot", 9);
        String joinRequestsMaterial = plugin.getConfigManager().getString("gui.main-menu.items.join-requests.material", "WRITABLE_BOOK");
        ClanMember member = clan.getMember(player.getUniqueId());
        if (member != null && (member.getRank() == ClanRank.OWNER || member.getRank() == ClanRank.MOD)) {
            addButton(joinRequestsSlot, new InventoryButton()
                .creator(p -> {
                    int requestCount = plugin.getInvitationManager().getClanRequests(clan.getId()).size();
                    String name = plugin.getMessageManager().getMessage("gui.items.join-requests.name", 
                        Map.of("count", String.valueOf(requestCount)));
                    return createItemWithLore(joinRequestsMaterial, name,
                        plugin.getMessageManager().getLore("gui.items.join-requests.lore", Map.of("count", String.valueOf(requestCount))));
                })
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getGuiManager().openGUI(new JoinRequestsGUI(plugin, clicker, clan), clicker);
                })
            );
        } else {
            addButton(joinRequestsSlot, new InventoryButton()
                .creator(p -> {
                    String name = plugin.getMessageManager().getMessage("gui.items.join-requests.name", 
                        Map.of("count", "0"));
                    List<String> noPermLore = new ArrayList<>();
                    noPermLore.add(ColorUtil.colorize("&#95A5A6&m                    "));
                    noPermLore.add("");
                    noPermLore.add(ColorUtil.colorize(" &#E74C3CYou don't have permission"));
                    noPermLore.add(ColorUtil.colorize(" &#E74C3Cto view join requests."));
                    noPermLore.add("");
                    noPermLore.add(ColorUtil.colorize(" &#95A5A6Only &fOwner &#95A5A6and &fMods"));
                    noPermLore.add(ColorUtil.colorize(" &#95A5A6can manage requests."));
                    noPermLore.add("");
                    return createItemWithLore(joinRequestsMaterial, name, noPermLore);
                })
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getMessageManager().send(clicker, "clan.only-owner-mod");
                    plugin.getSoundManager().play(clicker, "error");
                })
            );
        }

        int homeSlot = plugin.getConfigManager().getInt("gui.main-menu.items.home.slot", 15);
        String homeSetMaterial = plugin.getConfigManager().getString("gui.main-menu.items.home.material-set", "RED_BED");
        String homeNotSetMaterial = plugin.getConfigManager().getString("gui.main-menu.items.home.material-not-set", "WHITE_BED");
        addButton(homeSlot, new InventoryButton()
            .creator(p -> {
                String name = plugin.getMessageManager().getMessage("gui.items.home.name");
                if (clan.getHome() != null) {
                    String status = plugin.getMessageManager().getMessage("gui.items.home.status-set");
                    Map<String, String> homePlaceholders = new HashMap<>();
                    homePlaceholders.put("status", status);
                    return createItemWithLore(homeSetMaterial, name,
                        plugin.getMessageManager().getLore("gui.items.home.lore", homePlaceholders));
                } else {
                    return createItemWithLore(homeNotSetMaterial, name,
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
        
        int pvpSlot = plugin.getConfigManager().getInt("gui.main-menu.items.pvp.slot", 20);
        String pvpEnabledMaterial = plugin.getConfigManager().getString("gui.main-menu.items.pvp.material-enabled", "DIAMOND_SWORD");
        String pvpDisabledMaterial = plugin.getConfigManager().getString("gui.main-menu.items.pvp.material-disabled", "WOODEN_SWORD");
        addButton(pvpSlot, new InventoryButton()
            .creator(p -> {
                String name = plugin.getMessageManager().getMessage("gui.items.pvp.name");
                if (clan.isPvpEnabled()) {
                    return createItemWithLore(pvpEnabledMaterial, name,
                        plugin.getMessageManager().getLore("gui.items.pvp.lore-enabled"));
                } else {
                    return createItemWithLore(pvpDisabledMaterial, name,
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

        int lookupSlot = plugin.getConfigManager().getInt("gui.main-menu.items.lookup.slot", 22);
        String lookupMaterial = plugin.getConfigManager().getString("gui.main-menu.items.lookup.material", "COMPASS");
        addButton(lookupSlot, new InventoryButton()
            .creator(p -> {
                String name = plugin.getMessageManager().getMessage("gui.items.lookup.name");
                return createItemWithLore(lookupMaterial, name,
                    plugin.getMessageManager().getLore("gui.items.lookup.lore"));
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanLookupGUI(plugin, 1), clicker, false);
            })
        );
        
        if (clan.isOwner(player.getUniqueId())) {
            int disbandSlot = plugin.getConfigManager().getInt("gui.main-menu.items.disband.slot", 26);
            String disbandMaterial = plugin.getConfigManager().getString("gui.main-menu.items.disband.material", "TNT");
            addButton(disbandSlot, new InventoryButton()
                .creator(p -> createItemWithLore(disbandMaterial, 
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

        List<Integer> fillerSlots = plugin.getConfigManager().getIntList("gui.main-menu.filler.slots");
        if (fillerSlots.isEmpty()) {
            int joinRequestsSlot2 = plugin.getConfigManager().getInt("gui.main-menu.items.join-requests.slot", 9);
            int membersSlot2 = plugin.getConfigManager().getInt("gui.main-menu.items.members.slot", 11);
            int statsSlot2 = plugin.getConfigManager().getInt("gui.main-menu.items.stats.slot", 13);
            int homeSlot2 = plugin.getConfigManager().getInt("gui.main-menu.items.home.slot", 15);
            int pvpSlot2 = plugin.getConfigManager().getInt("gui.main-menu.items.pvp.slot", 20);
            int lookupSlot2 = plugin.getConfigManager().getInt("gui.main-menu.items.lookup.slot", 22);
            int disbandSlot2 = plugin.getConfigManager().getInt("gui.main-menu.items.disband.slot", 26);
            
            for (int i = 0; i < getInventory().getSize(); i++) {
                if (i != joinRequestsSlot2 && i != membersSlot2 && i != statsSlot2 && i != homeSlot2 && i != pvpSlot2 && i != lookupSlot2 && i != disbandSlot2) {
                    getInventory().setItem(i, filler);
                }
            }
        } else {
            for (int slot : fillerSlots) {
                if (slot >= 0 && slot < getInventory().getSize()) {
                    getInventory().setItem(slot, filler);
                }
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