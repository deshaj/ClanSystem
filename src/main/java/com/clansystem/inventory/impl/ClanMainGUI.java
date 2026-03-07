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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanMainGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Clan clan;
    private FileConfiguration guiConfig;
    
    public ClanMainGUI(ClanSystem plugin, Clan clan) {
        this.plugin = plugin;
        this.clan = clan;
        loadGuiConfig();
    }
    
    private void loadGuiConfig() {
        File file = new File(plugin.getDataFolder(), "guis.yml");
        if (file.exists()) {
            guiConfig = YamlConfiguration.loadConfiguration(file);
        }
    }
    
    @Override
    protected Inventory createInventory() {
        String title = guiConfig != null ? guiConfig.getString("main-menu.title", "&#34495EClan Menu") : "&#34495EClan Menu";
        int size = guiConfig != null ? guiConfig.getInt("main-menu.size", 45) : 45;
        return Bukkit.createInventory(null, size, plugin.getMessageManager().format(title));
    }
    
    @Override
    public void decorate(Player player) {
        addFillerGlass();
        addPvpButton();
        addMembersButton();
        addHomeButton();
        addStatsButton();
        addLookupButton();
        addJoinRequestsButton(player);
        addDisbandButton(player);
        super.decorate(player);
    }

    private void addPvpButton() {
        int slot = guiConfig != null ? guiConfig.getInt("main-menu.items.pvp-enabled.slot", 11) : 11;
        String key = clan.isPvpEnabled() ? "pvp-enabled" : "pvp-disabled";
        String materialName = guiConfig != null ? guiConfig.getString("main-menu.items." + key + ".material") : (clan.isPvpEnabled() ? "DIAMOND_AXE" : "IRON_AXE");
        String name = guiConfig != null ? guiConfig.getString("main-menu.items." + key + ".name") : "&#E74C3C&l⚔ Clan PvP";
        List<String> lore = guiConfig != null ? guiConfig.getStringList("main-menu.items." + key + ".lore") : new ArrayList<>();

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, lore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                if (!clan.isOwner(clicker.getUniqueId())) {
                    plugin.getMessageManager().send(clicker, "clan.only-owner");
                    plugin.getSoundManager().play(clicker, "error");
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
    }

    private void addMembersButton() {
        int slot = guiConfig != null ? guiConfig.getInt("main-menu.items.members.slot", 13) : 13;
        String materialName = guiConfig != null ? guiConfig.getString("main-menu.items.members.material", "PLAYER_HEAD") : "PLAYER_HEAD";
        String name = guiConfig != null ? guiConfig.getString("main-menu.items.members.name") : "&#3498DB&l👥 Clan Members";
        List<String> loreTemplate = guiConfig != null ? guiConfig.getStringList("main-menu.items.members.lore") : new ArrayList<>();

        addButton(slot, new InventoryButton()
            .creator(p -> {
                int maxMembers = plugin.getConfigManager().maxMembers();
                List<String> lore = new ArrayList<>();
                for (String line : loreTemplate) {
                    line = line.replace("{count}", String.valueOf(clan.getMemberCount()))
                        .replace("{max}", String.valueOf(maxMembers));
                    lore.add(line);
                }
                return createItem(materialName, name, lore);
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanMembersGUI(plugin, clan), clicker);
            })
        );
    }

    private void addHomeButton() {
        int slot = guiConfig != null ? guiConfig.getInt("main-menu.items.home-set.slot", 15) : 15;
        String key = clan.getHome() != null ? "home-set" : "home-not-set";
        String materialName = guiConfig != null ? guiConfig.getString("main-menu.items." + key + ".material") : (clan.getHome() != null ? "RED_BED" : "WHITE_BED");
        String name = guiConfig != null ? guiConfig.getString("main-menu.items." + key + ".name") : "&#F1C40F&l⌂ Clan Home";
        List<String> lore = guiConfig != null ? guiConfig.getStringList("main-menu.items." + key + ".lore") : new ArrayList<>();

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, lore))
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
                        plugin.getSoundManager().play(clicker, "error");
                    }
                    return;
                }
                if (plugin.getHomeManager().hasCooldown(clicker.getUniqueId())) {
                    long remaining = plugin.getHomeManager().getRemainingCooldown(clicker.getUniqueId());
                    plugin.getMessageManager().send(clicker, "home.cooldown",
                        Map.of("time", String.valueOf(remaining)));
                    plugin.getSoundManager().play(clicker, "error");
                    return;
                }
                plugin.getHomeManager().teleportHome(clicker, clan);
                plugin.getMessageManager().send(clicker, "home.teleporting");
                plugin.getSoundManager().play(clicker, "home-teleport");
                clicker.closeInventory();
            })
        );
    }

    private void addStatsButton() {
        int slot = guiConfig != null ? guiConfig.getInt("main-menu.items.stats.slot", 20) : 20;
        String materialName = guiConfig != null ? guiConfig.getString("main-menu.items.stats.material", "DIAMOND_SWORD") : "DIAMOND_SWORD";
        String name = guiConfig != null ? guiConfig.getString("main-menu.items.stats.name") : "&#E67E22&l⚔ Clan Statistics";
        List<String> loreTemplate = guiConfig != null ? guiConfig.getStringList("main-menu.items.stats.lore") : new ArrayList<>();

        addButton(slot, new InventoryButton()
            .creator(p -> {
                int level = plugin.getLevelManager().calculateLevel(clan);
                double kd = clan.getTotalDeaths() > 0 ? 
                    Math.round((double) clan.getTotalKills() / clan.getTotalDeaths() * 100.0) / 100.0 : clan.getTotalKills();
                
                List<String> lore = new ArrayList<>();
                for (String line : loreTemplate) {
                    line = line.replace("{kills}", String.valueOf(clan.getTotalKills()))
                        .replace("{deaths}", String.valueOf(clan.getTotalDeaths()))
                        .replace("{kd}", String.valueOf(kd))
                        .replace("{level}", String.valueOf(level));
                    lore.add(line);
                }
                return createItem(materialName, name, lore);
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanStatsGUI(plugin, clan), clicker);
            })
        );
    }

    private void addLookupButton() {
        int slot = guiConfig != null ? guiConfig.getInt("main-menu.items.lookup.slot", 22) : 22;
        String materialName = guiConfig != null ? guiConfig.getString("main-menu.items.lookup.material", "COMPASS") : "COMPASS";
        String name = guiConfig != null ? guiConfig.getString("main-menu.items.lookup.name") : "&#3498DB&l🔍 Browse Clans";
        List<String> lore = guiConfig != null ? guiConfig.getStringList("main-menu.items.lookup.lore") : new ArrayList<>();

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, lore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanLookupGUI(plugin, 1), clicker, false);
            })
        );
    }

    private void addJoinRequestsButton(Player player) {
        int slot = guiConfig != null ? guiConfig.getInt("main-menu.items.join-requests.slot", 24) : 24;
        String materialName = guiConfig != null ? guiConfig.getString("main-menu.items.join-requests.material", "WRITABLE_BOOK") : "WRITABLE_BOOK";
        String name = guiConfig != null ? guiConfig.getString("main-menu.items.join-requests.name") : "&#F39C12&lJoin Requests";
        List<String> loreTemplate = guiConfig != null ? guiConfig.getStringList("main-menu.items.join-requests.lore") : new ArrayList<>();

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member != null && (member.getRank() == ClanRank.OWNER || member.getRank() == ClanRank.MOD)) {
            addButton(slot, new InventoryButton()
                .creator(p -> {
                    int requestCount = plugin.getInvitationManager().getClanRequests(clan.getId()).size();
                    String finalName = name.replace("{count}", String.valueOf(requestCount));
                    List<String> lore = new ArrayList<>();
                    for (String line : loreTemplate) {
                        line = line.replace("{count}", String.valueOf(requestCount));
                        lore.add(line);
                    }
                    return createItem(materialName, finalName, lore);
                })
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getGuiManager().openGUI(new JoinRequestsGUI(plugin, clicker, clan), clicker);
                })
            );
        } else {
            addButton(slot, new InventoryButton()
                .creator(p -> {
                    String finalName = name.replace("{count}", "0");
                    List<String> noPermLore = new ArrayList<>();
                    noPermLore.add(ColorUtil.colorize("&#95A5A6&m                    "));
                    noPermLore.add("");
                    noPermLore.add(ColorUtil.colorize(" &#E74C3CYou don't have permission"));
                    noPermLore.add(ColorUtil.colorize(" &#E74C3Cto view join requests."));
                    noPermLore.add("");
                    noPermLore.add(ColorUtil.colorize(" &#95A5A6Only &fOwner &#95A5A6and &fMods"));
                    noPermLore.add(ColorUtil.colorize(" &#95A5A6can manage requests."));
                    noPermLore.add("");
                    return createItem(materialName, finalName, noPermLore);
                })
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getMessageManager().send(clicker, "clan.only-owner-mod");
                    plugin.getSoundManager().play(clicker, "error");
                })
            );
        }
    }

    private void addDisbandButton(Player player) {
        if (!clan.isOwner(player.getUniqueId())) return;
        
        int slot = guiConfig != null ? guiConfig.getInt("main-menu.items.disband.slot", 44) : 44;
        String materialName = guiConfig != null ? guiConfig.getString("main-menu.items.disband.material", "TNT_MINECART") : "TNT_MINECART";
        String name = guiConfig != null ? guiConfig.getString("main-menu.items.disband.name") : "&#E74C3C&l✖ Disband Clan";
        List<String> lore = guiConfig != null ? guiConfig.getStringList("main-menu.items.disband.lore") : new ArrayList<>();

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, lore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ConfirmDisbandGUI(plugin, clan), clicker, false);
            })
        );
    }

    private void addFillerGlass() {
        boolean enabled = guiConfig != null && guiConfig.getBoolean("main-menu.filler.enabled", true);
        if (!enabled) return;

        String materialName = guiConfig != null ? guiConfig.getString("main-menu.filler.material", "GRAY_STAINED_GLASS_PANE") : "GRAY_STAINED_GLASS_PANE";
        String name = guiConfig != null ? guiConfig.getString("main-menu.filler.name", " ") : " ";
        
        ItemStack filler = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(null);
        if (filler == null) return;
        
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().format(name));
            filler.setItemMeta(meta);
        }

        int pvpSlot = guiConfig != null ? guiConfig.getInt("main-menu.items.pvp-enabled.slot", 11) : 11;
        int membersSlot = guiConfig != null ? guiConfig.getInt("main-menu.items.members.slot", 13) : 13;
        int homeSlot = guiConfig != null ? guiConfig.getInt("main-menu.items.home-set.slot", 15) : 15;
        int statsSlot = guiConfig != null ? guiConfig.getInt("main-menu.items.stats.slot", 20) : 20;
        int lookupSlot = guiConfig != null ? guiConfig.getInt("main-menu.items.lookup.slot", 22) : 22;
        int requestsSlot = guiConfig != null ? guiConfig.getInt("main-menu.items.join-requests.slot", 24) : 24;
        int disbandSlot = guiConfig != null ? guiConfig.getInt("main-menu.items.disband.slot", 44) : 44;
        
        for (int i = 0; i < getInventory().getSize(); i++) {
            if (i != pvpSlot && i != membersSlot && i != homeSlot && i != statsSlot && 
                i != lookupSlot && i != requestsSlot && i != disbandSlot) {
                getInventory().setItem(i, filler);
            }
        }
    }
    
    private ItemStack createItem(String materialName, String name, List<String> lore) {
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem)
            .orElse(new ItemStack(Material.STONE));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().format(name));
            
            List<String> formattedLore = new ArrayList<>();
            for (String line : lore) {
                formattedLore.add(plugin.getMessageManager().format(line));
            }
            meta.setLore(formattedLore);
            
            item.setItemMeta(meta);
        }
        return item;
    }
}