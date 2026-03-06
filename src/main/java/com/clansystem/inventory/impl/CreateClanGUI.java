package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.inventory.InventoryButton;
import com.clansystem.inventory.InventoryGUI;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateClanGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Player player;
    private static final Map<UUID, ClanData> pendingClans = new HashMap<>();

    public CreateClanGUI(ClanSystem plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        if (!pendingClans.containsKey(player.getUniqueId())) {
            pendingClans.put(player.getUniqueId(), new ClanData());
        }
    }

    @Override
    protected Inventory createInventory() {
        String title = plugin.getConfigManager().getString("gui.create-clan.title", "&8Create Clan");
        int size = plugin.getConfigManager().getInt("gui.create-clan.size", 27);
        return Bukkit.createInventory(null, size, plugin.getMessageManager().format(title));
    }

    @Override
    public void decorate(Player player) {
        addFillerGlass();
        addNameButton();
        addTagButton();
        addConfirmButton();
        addCancelButton();
        super.decorate(player);
    }

    private void addFillerGlass() {
        boolean enabled = plugin.getConfigManager().getBoolean("gui.create-clan.filler.enabled", true);
        if (!enabled) return;

        String materialName = plugin.getConfigManager().getString("gui.create-clan.filler.material", "GRAY_STAINED_GLASS_PANE");
        String name = plugin.getConfigManager().getString("gui.create-clan.filler.name", " ");
        
        ItemStack filler = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(null);
        if (filler == null) return;
        
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().format(name));
            filler.setItemMeta(meta);
        }

        for (int i = 0; i < getInventory().getSize(); i++) {
            if (i != 11 && i != 13 && i != 15 && i != 22) {
                getInventory().setItem(i, filler);
            }
        }
    }

    private void addNameButton() {
        ClanData data = pendingClans.get(player.getUniqueId());
        int slot = plugin.getConfigManager().getInt("gui.create-clan.items.clan-name.slot", 11);
        String materialName = plugin.getConfigManager().getString("gui.create-clan.items.clan-name.material", "NAME_TAG");
        String name = plugin.getConfigManager().getString("gui.create-clan.items.clan-name.name", "&e&lClan Name");
        List<String> lore = plugin.getConfigManager().getStringList("gui.create-clan.items.clan-name.lore");

        List<String> finalLore = new ArrayList<>();
        for (String line : lore) {
            finalLore.add(line.replace("{name}", data.name != null ? data.name : "&#E74C3CNot Set"));
        }

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, finalLore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                
                plugin.getChatInputManager().prompt(clicker, "invitation.prompt-clan-name", input -> {
                    ClanData clanData = pendingClans.get(clicker.getUniqueId());
                    if (clanData != null) {
                        clanData.name = input;
                        plugin.getFoliaLib().getImpl().runLater(() -> {
                            plugin.getGuiManager().openGUI(new CreateClanGUI(plugin, clicker), clicker);
                        }, 2L);
                    }
                });
            })
        );
    }

    private void addTagButton() {
        ClanData data = pendingClans.get(player.getUniqueId());
        int slot = plugin.getConfigManager().getInt("gui.create-clan.items.clan-tag.slot", 13);
        String materialName = plugin.getConfigManager().getString("gui.create-clan.items.clan-tag.material", "BANNER");
        String name = plugin.getConfigManager().getString("gui.create-clan.items.clan-tag.name", "&6&lClan Tag");
        List<String> lore = plugin.getConfigManager().getStringList("gui.create-clan.items.clan-tag.lore");

        String currentTag = data.tag != null ? data.tag : "&#95A5A6[None]";
        List<String> finalLore = new ArrayList<>();
        for (String line : lore) {
            finalLore.add(line.replace("{tag}", currentTag));
        }

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, finalLore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                
                clicker.sendMessage(plugin.getMessageManager().format("&#F39C12Type your clan tag in chat (or 'cancel' to go back)"));
                plugin.getChatInputManager().prompt(clicker, "", input -> {
                    ClanData clanData = pendingClans.get(clicker.getUniqueId());
                    if (clanData != null) {
                        clanData.tag = input;
                        plugin.getFoliaLib().getImpl().runLater(() -> {
                            plugin.getGuiManager().openGUI(new CreateClanGUI(plugin, clicker), clicker);
                        }, 2L);
                    }
                });
            })
        );
    }

    private void addConfirmButton() {
        ClanData data = pendingClans.get(player.getUniqueId());
        int slot = plugin.getConfigManager().getInt("gui.create-clan.items.confirm-create.slot", 15);
        String materialName = plugin.getConfigManager().getString("gui.create-clan.items.confirm-create.material", "LIME_DYE");
        String name = plugin.getConfigManager().getString("gui.create-clan.items.confirm-create.name", "&a&l✔ Create Clan");
        List<String> lore = plugin.getConfigManager().getStringList("gui.create-clan.items.confirm-create.lore");

        List<String> finalLore = new ArrayList<>();
        for (String line : lore) {
            line = line.replace("{name}", data.name != null ? data.name : "&#E74C3CNot Set");
            line = line.replace("{tag}", data.tag != null ? data.tag : "&#95A5A6[None]");
            finalLore.add(line);
        }

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, finalLore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                ClanData clanData = pendingClans.get(clicker.getUniqueId());
                
                if (clanData == null || clanData.name == null || clanData.name.isEmpty()) {
                    plugin.getMessageManager().send(clicker, "clan.name-too-short", 
                        Map.of("min", String.valueOf(plugin.getConfigManager().minClanNameLength())));
                    return;
                }

                int minLength = plugin.getConfigManager().minClanNameLength();
                int maxLength = plugin.getConfigManager().maxClanNameLength();
                
                if (clanData.name.length() < minLength) {
                    plugin.getMessageManager().send(clicker, "clan.name-too-short", 
                        Map.of("min", String.valueOf(minLength)));
                    return;
                }
                
                if (clanData.name.length() > maxLength) {
                    plugin.getMessageManager().send(clicker, "clan.name-too-long", 
                        Map.of("max", String.valueOf(maxLength)));
                    return;
                }

                if (plugin.getClanManager().getClanByName(clanData.name) != null) {
                    plugin.getMessageManager().send(clicker, "clan.clan-exists");
                    return;
                }

                clicker.closeInventory();
                pendingClans.remove(clicker.getUniqueId());
                
                plugin.getClanManager().createClan(clicker, clanData.name).thenAccept(clan -> {
                    plugin.getMessageManager().send(clicker, "clan.created", 
                        Map.of("clan", clanData.name));
                    plugin.getSoundManager().playCreate(clicker);
                });
            })
        );
    }

    private void addCancelButton() {
        int slot = plugin.getConfigManager().getInt("gui.create-clan.items.cancel.slot", 22);
        String materialName = plugin.getConfigManager().getString("gui.create-clan.items.cancel.material", "BARRIER");
        String name = plugin.getConfigManager().getString("gui.create-clan.items.cancel.name", "&c&l✖ Cancel");
        List<String> lore = plugin.getConfigManager().getStringList("gui.create-clan.items.cancel.lore");

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, lore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                pendingClans.remove(clicker.getUniqueId());
                plugin.getGuiManager().openGUI(new NoClanGUI(plugin, clicker), clicker);
            })
        );
    }

    private ItemStack createItem(String materialName, String name, List<String> lore) {
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(null);
        if (item == null) {
            item = XMaterial.matchXMaterial("STONE").map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.STONE));
        }

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

    private static class ClanData {
        String name;
        String tag;
    }
}