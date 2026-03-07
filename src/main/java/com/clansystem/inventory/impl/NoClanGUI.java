package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.ClanInvitation;
import com.clansystem.inventory.InventoryButton;
import com.clansystem.inventory.InventoryGUI;
import com.clansystem.inventory.impl.CreateClanGUI;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NoClanGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Player player;

    public NoClanGUI(ClanSystem plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    protected Inventory createInventory() {
        String title = plugin.getConfigManager().getString("gui.no-clan.title", "&8No Clan Menu");
        int size = plugin.getConfigManager().getInt("gui.no-clan.size", 27);
        return Bukkit.createInventory(null, size, plugin.getMessageManager().format(title));
    }

    @Override
    public void decorate(Player player) {
        addFillerGlass();
        addCreateClanButton();
        addBrowseClansButton();
        addInvitationsButton();
        super.decorate(player);
    }

    private void addFillerGlass() {
        boolean enabled = plugin.getConfigManager().getBoolean("gui.no-clan.filler.enabled", true);
        if (!enabled) return;

        String materialName = plugin.getConfigManager().getString("gui.no-clan.filler.material", "GRAY_STAINED_GLASS_PANE");
        String name = plugin.getConfigManager().getString("gui.no-clan.filler.name", " ");
        
        ItemStack filler = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(null);
        if (filler == null) return;
        
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().format(name));
            filler.setItemMeta(meta);
        }

        List<Integer> fillerSlots = plugin.getConfigManager().getIntList("gui.no-clan.filler.slots");
        if (fillerSlots.isEmpty()) {
            for (int i = 0; i < getInventory().getSize(); i++) {
                if (i != 0 && i != 11 && i != 15) {
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

    private void addCreateClanButton() {
        int slot = plugin.getConfigManager().getInt("gui.no-clan.create-clan.slot", 11);
        String materialName = plugin.getConfigManager().getString("gui.no-clan.create-clan.material", "ANVIL");
        String name = plugin.getConfigManager().getString("gui.no-clan.create-clan.name", "&e&lCreate Clan");
        List<String> lore = plugin.getConfigManager().getStringList("gui.no-clan.create-clan.lore");

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, lore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                CreateClanGUI createGUI = new CreateClanGUI(plugin, clicker);
                plugin.getGuiManager().openGUI(createGUI, clicker);
            })
        );
    }

    private void addBrowseClansButton() {
        int slot = plugin.getConfigManager().getInt("gui.no-clan.browse-clans.slot", 15);
        String materialName = plugin.getConfigManager().getString("gui.no-clan.browse-clans.material", "COMPASS");
        String name = plugin.getConfigManager().getString("gui.no-clan.browse-clans.name", "&b&lBrowse Clans");
        List<String> lore = plugin.getConfigManager().getStringList("gui.no-clan.browse-clans.lore");

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, lore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                
                ClanLookupGUI lookupGUI = new ClanLookupGUI(plugin, clicker, 1);
                plugin.getGuiManager().openGUI(lookupGUI, clicker);
            })
        );
    }

    private void addInvitationsButton() {
        List<ClanInvitation> invitations = plugin.getInvitationManager().getPlayerInvitations(player.getUniqueId());
        if (invitations.isEmpty()) return;

        int slot = plugin.getConfigManager().getInt("gui.no-clan.invitations.slot", 0);
        String materialName = plugin.getConfigManager().getString("gui.no-clan.invitations.material", "PAPER");
        String name = plugin.getConfigManager().getString("gui.no-clan.invitations.name", "&a&lInvitations ({count})");
        List<String> lore = plugin.getConfigManager().getStringList("gui.no-clan.invitations.lore");

        String finalName = name.replace("{count}", String.valueOf(invitations.size()));
        List<String> finalLore = new ArrayList<>();
        for (String line : lore) {
            finalLore.add(line.replace("{count}", String.valueOf(invitations.size())));
        }

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, finalName, finalLore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();

                InvitationsGUI invitationsGUI = new InvitationsGUI(plugin, clicker);
                plugin.getGuiManager().openGUI(invitationsGUI, clicker);
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
}