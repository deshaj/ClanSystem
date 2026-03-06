package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.JoinRequest;
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
import java.util.List;

public class BrowseClanDetailsGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Player player;
    private final Clan clan;
    private final int returnPage;

    public BrowseClanDetailsGUI(ClanSystem plugin, Player player, Clan clan, int returnPage) {
        this.plugin = plugin;
        this.player = player;
        this.clan = clan;
        this.returnPage = returnPage;
    }

    @Override
    protected Inventory createInventory() {
        String title = plugin.getConfigManager().getString("gui.browse-clan-details.title", "&8{clan} Details");
        title = title.replace("{clan}", clan.getName());
        int size = plugin.getConfigManager().getInt("gui.browse-clan-details.size", 27);
        return Bukkit.createInventory(null, size, plugin.getMessageManager().format(title));
    }

    @Override
    public void decorate(Player player) {
        addFillerGlass();
        addClanInfoButton();
        addSendRequestButton();
        addBackButton();
        super.decorate(player);
    }

    private void addFillerGlass() {
        String materialName = plugin.getConfigManager().getString("gui.browse-clan-details.filler.material", "GRAY_STAINED_GLASS_PANE");
        String name = plugin.getConfigManager().getString("gui.browse-clan-details.filler.name", " ");
        
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

    private void addClanInfoButton() {
        int slot = plugin.getConfigManager().getInt("gui.browse-clan-details.clan-info.slot", 13);
        String materialName = plugin.getConfigManager().getString("gui.browse-clan-details.clan-info.material", "BANNER");
        String name = plugin.getConfigManager().getString("gui.browse-clan-details.clan-info.name", "&e&l{clan}");
        List<String> lore = plugin.getConfigManager().getStringList("gui.browse-clan-details.clan-info.lore");

        String finalName = name.replace("{clan}", clan.getName());
        double kd = clan.getTotalDeaths() > 0 ? (double) clan.getTotalKills() / clan.getTotalDeaths() : clan.getTotalKills();
        
        List<String> finalLore = new ArrayList<>();
        for (String line : lore) {
            line = line.replace("{clan}", clan.getName())
                .replace("{members}", String.valueOf(clan.getMemberCount()))
                .replace("{max}", String.valueOf(plugin.getConfigManager().getInt("clan.max-members", 10)))
                .replace("{kills}", String.valueOf(clan.getTotalKills()))
                .replace("{deaths}", String.valueOf(clan.getTotalDeaths()))
                .replace("{kd}", String.format("%.2f", kd));
            finalLore.add(line);
        }

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, finalName, finalLore))
            .consumer(event -> {})
        );
    }

    private void addSendRequestButton() {
        int slot = plugin.getConfigManager().getInt("gui.browse-clan-details.send-request.slot", 15);
        String materialName = plugin.getConfigManager().getString("gui.browse-clan-details.send-request.material", "WRITABLE_BOOK");
        String name = plugin.getConfigManager().getString("gui.browse-clan-details.send-request.name", "&a&lSend Join Request");
        List<String> lore = plugin.getConfigManager().getStringList("gui.browse-clan-details.send-request.lore");

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, lore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                
                plugin.getMessageManager().send(clicker, "gui.join-request-prompt");
                plugin.getClanChatManager().setSendingJoinRequest(clicker.getUniqueId(), clan.getId());
                
                XSound.matchXSound(plugin.getConfigManager().getString("sounds.click", "UI_BUTTON_CLICK"))
                    .ifPresent(sound -> sound.play(clicker));
            })
        );
    }

    private void addBackButton() {
        int slot = plugin.getConfigManager().getInt("gui.browse-clan-details.back-button.slot", 22);
        String materialName = plugin.getConfigManager().getString("gui.browse-clan-details.back-button.material", "ARROW");
        String name = plugin.getConfigManager().getString("gui.browse-clan-details.back-button.name", "&c← Back");
        List<String> lore = plugin.getConfigManager().getStringList("gui.browse-clan-details.back-button.lore");

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, lore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                
                ClanLookupGUI lookupGUI = new ClanLookupGUI(plugin, clicker, returnPage);
                plugin.getGuiManager().openGUI(lookupGUI, clicker);
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