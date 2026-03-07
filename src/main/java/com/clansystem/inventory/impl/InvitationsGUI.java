package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanInvitation;
import com.clansystem.inventory.InventoryButton;
import com.clansystem.inventory.InventoryGUI;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InvitationsGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Player player;

    public InvitationsGUI(ClanSystem plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    protected Inventory createInventory() {
        String title = plugin.getConfigManager().getString("gui.invitations.title", "&8Clan Invitations");
        int size = plugin.getConfigManager().getInt("gui.invitations.size", 54);
        return Bukkit.createInventory(null, size, plugin.getMessageManager().format(title));
    }

    @Override
    public void decorate(Player player) {
        addFillerGlass();
        loadInvitations();
        addBackButton();
        super.decorate(player);
    }

    private void addFillerGlass() {
        String materialName = plugin.getConfigManager().getString("gui.invitations.filler.material", "GRAY_STAINED_GLASS_PANE");
        String name = plugin.getConfigManager().getString("gui.invitations.filler.name", " ");
        
        ItemStack filler = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(null);
        if (filler == null) return;
        
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().format(name));
            filler.setItemMeta(meta);
        }

        for (int i = 45; i < 54; i++) {
            getInventory().setItem(i, filler);
        }
    }

    private void loadInvitations() {
        List<ClanInvitation> invitations = plugin.getInvitationManager().getPlayerInvitations(player.getUniqueId());
        int slot = 0;
        for (ClanInvitation invitation : invitations) {
            if (slot >= 45) break;

            Clan clan = plugin.getClanManager().getClan(invitation.getClanId());
            if (clan == null) continue;

            addInvitationButton(slot, invitation, clan);
            slot++;
        }
    }

    private void addInvitationButton(int slot, ClanInvitation invitation, Clan clan) {
        String materialName = plugin.getConfigManager().getString("gui.invitations.invitation-item.material", "PAPER");
        String name = plugin.getConfigManager().getString("gui.invitations.invitation-item.name", "&e{clan}");
        List<String> lore = plugin.getConfigManager().getStringList("gui.invitations.invitation-item.lore");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        String timeAgo = getTimeAgo(invitation.getTimestamp());

        String finalName = name.replace("{clan}", clan.getName());
        List<String> finalLore = new ArrayList<>();
        for (String line : lore) {
            line = line.replace("{clan}", clan.getName())
                .replace("{members}", String.valueOf(clan.getMemberCount()))
                .replace("{max}", String.valueOf(plugin.getConfigManager().getInt("clan.max-members", 10)))
                .replace("{kills}", String.valueOf(clan.getTotalKills()))
                .replace("{deaths}", String.valueOf(clan.getTotalDeaths()))
                .replace("{time}", dateFormat.format(new Date(invitation.getTimestamp())))
                .replace("{time-ago}", timeAgo);
            finalLore.add(line);
        }

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, finalName, finalLore))
            .consumer(event -> {
                if (event.isLeftClick()) {
                    acceptInvitation(invitation, clan);
                } else if (event.isRightClick()) {
                    declineInvitation(invitation, clan);
                }
            })
        );
    }

    private void acceptInvitation(ClanInvitation invitation, Clan clan) {
        player.closeInventory();

        if (plugin.getClanManager().getPlayerClan(player.getUniqueId()) != null) {
            plugin.getMessageManager().send(player, "clan.already-in-clan");
            return;
        }

        if (clan.getMemberCount() >= plugin.getConfigManager().getInt("clan.max-members", 10)) {
            plugin.getMessageManager().send(player, "clan.clan-full");
            plugin.getInvitationManager().removeInvitation(player.getUniqueId(), clan.getId());
            return;
        }

        plugin.getClanManager().addMember(clan, player, com.clansystem.data.ClanRank.MEMBER).thenRun(() -> {
            plugin.getInvitationManager().removeInvitation(player.getUniqueId(), clan.getId());
            plugin.getMessageManager().send(player, "clan.joined", java.util.Map.of("clan", clan.getName()));
            XSound.matchXSound(plugin.getConfigManager().getString("sounds.join", "ENTITY_EXPERIENCE_ORB_PICKUP"))
                .ifPresent(sound -> sound.play(player));
        });
    }

    private void declineInvitation(ClanInvitation invitation, Clan clan) {
        plugin.getInvitationManager().removeInvitation(player.getUniqueId(), clan.getId());
        plugin.getFoliaLib().getImpl().runAtEntity(player, task -> {
            plugin.getMessageManager().send(player, "gui.invitation-declined", java.util.Map.of("clan", clan.getName()));
            XSound.matchXSound(plugin.getConfigManager().getString("sounds.click", "UI_BUTTON_CLICK"))
                .ifPresent(sound -> sound.play(player));
            getInventory().clear();
            decorate(player);
        });
    }

    private void addBackButton() {
        int slot = plugin.getConfigManager().getInt("gui.invitations.back-button.slot", 49);
        String materialName = plugin.getConfigManager().getString("gui.invitations.back-button.material", "ARROW");
        String name = plugin.getConfigManager().getString("gui.invitations.back-button.name", "&c← Back");
        List<String> lore = plugin.getConfigManager().getStringList("gui.invitations.back-button.lore");

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, lore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new NoClanGUI(plugin, clicker), clicker, false);
            })
        );
    }

    private String getTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d ago";
        if (hours > 0) return hours + "h ago";
        if (minutes > 0) return minutes + "m ago";
        return seconds + "s ago";
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