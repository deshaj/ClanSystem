package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.inventory.InventoryButton;
import com.clansystem.inventory.InventoryGUI;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClanDetailsGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Clan clan;

    public ClanDetailsGUI(ClanSystem plugin, Clan clan) {
        this.plugin = plugin;
        this.clan = clan;
    }

    @Override
    protected Inventory createInventory() {
        String title = plugin.getMessageManager().getMessage("gui.clan-details.title")
            .replace("{clan}", clan.getName());
        return Bukkit.createInventory(null, 27, title);
    }

    @Override
    public void decorate(Player player) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(clan.getOwner());
        double kd = clan.getTotalDeaths() > 0 ? 
            Math.round((double) clan.getTotalKills() / clan.getTotalDeaths() * 100.0) / 100.0 : clan.getTotalKills();

        addButton(13, new InventoryButton()
            .creator(p -> {
                String materialName = plugin.getConfigManager().getConfig().getString("gui.clan-details.info-button.material", "PAPER");
                ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.PAPER.parseMaterial()));
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String name = plugin.getConfigManager().getConfig().getString("gui.clan-details.info-button.name", "Clan Info");
                    meta.setDisplayName(plugin.getMessageManager().color(name));
                    
                    String ownerName = owner.getName() != null ? owner.getName() : "Unknown";
                    List<String> lore = plugin.getConfigManager().getConfig().getStringList("gui.clan-details.info-button.lore");
                    lore = lore.stream()
                        .map(line -> line
                            .replace("{owner}", ownerName)
                            .replace("{members}", String.valueOf(clan.getMemberCount()))
                            .replace("{kills}", String.valueOf(clan.getTotalKills()))
                            .replace("{deaths}", String.valueOf(clan.getTotalDeaths()))
                            .replace("{kd}", String.valueOf(kd)))
                        .map(plugin.getMessageManager()::color)
                        .collect(Collectors.toList());
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                return item;
            })
            .consumer(event -> {})
        );

        addButton(11, new InventoryButton()
            .creator(p -> {
                String materialName = plugin.getConfigManager().getConfig().getString("gui.clan-details.request-button.material", "WRITABLE_BOOK");
                ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.WRITABLE_BOOK.parseMaterial()));
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String name = plugin.getConfigManager().getConfig().getString("gui.clan-details.request-button.name", "Request to Join");
                    meta.setDisplayName(plugin.getMessageManager().color(name));
                    List<String> lore = plugin.getConfigManager().getConfig().getStringList("gui.clan-details.request-button.lore");
                    meta.setLore(lore.stream().map(plugin.getMessageManager()::color).collect(Collectors.toList()));
                    item.setItemMeta(meta);
                }
                return item;
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                clicker.closeInventory();
                plugin.getChatInputManager().prompt(clicker, "clan.request-message-prompt", message -> {
                    handleJoinRequest(clicker, message);
                });
            })
        );

        addButton(15, new InventoryButton()
            .creator(p -> {
                String materialName = plugin.getConfigManager().getConfig().getString("gui.clan-details.back-button.material", "ARROW");
                ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.ARROW.parseMaterial()));
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String name = plugin.getConfigManager().getConfig().getString("gui.clan-details.back-button.name", "Back");
                    meta.setDisplayName(plugin.getMessageManager().color(name));
                    item.setItemMeta(meta);
                }
                return item;
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new BrowseClansGUI(plugin), clicker);
            })
        );

        super.decorate(player);
    }

    private void handleJoinRequest(Player player, String message) {
        if (plugin.getClanManager().getPlayerClan(player.getUniqueId()) != null) {
            plugin.getMessageManager().send(player, "clan.already-in-clan");
            return;
        }

        plugin.getInvitationManager().sendJoinRequest(player.getUniqueId(), clan, message);
        plugin.getMessageManager().send(player, "clan.request-sent", Map.of("clan", clan.getName()));
        plugin.getSoundManager().play(player, "invite-send");
    }
}