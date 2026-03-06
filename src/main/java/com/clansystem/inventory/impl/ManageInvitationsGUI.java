package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanInvitation;
import com.clansystem.data.ClanMember;
import com.clansystem.data.ClanRank;
import com.clansystem.inventory.InventoryButton;
import com.clansystem.inventory.InventoryGUI;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManageInvitationsGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Player player;

    public ManageInvitationsGUI(ClanSystem plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    protected Inventory createInventory() {
        String title = plugin.getMessageManager().getMessage("gui.invitations.title");
        return Bukkit.createInventory(null, 27, title);
    }

    @Override
    public void decorate(Player player) {
        List<ClanInvitation> invitations = plugin.getInvitationManager().getPlayerInvitations(player.getUniqueId());
        plugin.getFoliaLib().getImpl().runAtEntity(player, task -> {
            {
                int slot = 0;
                for (ClanInvitation invitation : invitations) {
                    if (slot >= 27) break;
                    
                    Clan clan = plugin.getClanManager().getClan(invitation.getClanId());
                    if (clan == null) continue;
                    
                    OfflinePlayer inviter = invitation.getInvitedBy() != null ? Bukkit.getOfflinePlayer(invitation.getInvitedBy()) : null;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                    long daysLeft = (invitation.getExpiresAt() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
                    
                    addButton(slot, new InventoryButton()
                        .creator(p -> {
                            String materialName = plugin.getConfigManager().getConfig()
                                .getString("gui.invitations.invitation-item.material", "PAPER");
                            ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem)
                                .orElse(new ItemStack(XMaterial.PAPER.parseMaterial()));
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                String name = plugin.getMessageManager().getMessage("gui.invitations.invitation-item.name")
                                    .replace("{clan}", clan.getName());
                                meta.setDisplayName(plugin.getMessageManager().color(name));
                                
                                String inviterName = inviter != null ? inviter.getName() : "Unknown";
                                List<String> lore = plugin.getMessageManager().getLore("gui.invitations.invitation-item.lore");
                                lore = lore.stream()
                                    .map(line -> line
                                        .replace("{inviter}", inviterName)
                                        .replace("{date}", dateFormat.format(new Date(invitation.getCreatedAt())))
                                        .replace("{expires}", daysLeft + " days"))
                                    .map(plugin.getMessageManager()::color)
                                    .collect(Collectors.toList());
                                meta.setLore(lore);
                                item.setItemMeta(meta);
                            }
                            return item;
                        })
                        .consumer(event -> {
                            if (event.isLeftClick()) {
                                Player clicker = (Player) event.getWhoClicked();
                                handleAcceptInvitation(clicker, invitation, clan);
                            } else if (event.isRightClick()) {
                                Player clicker = (Player) event.getWhoClicked();
                                handleDeclineInvitation(clicker, invitation, clan);
                            }
                        })
                    );
                    slot++;
                }
                ManageInvitationsGUI.super.decorate(player);
            }
        });
    }

    private void handleAcceptInvitation(Player player, ClanInvitation invitation, Clan clan) {
        if (plugin.getClanManager().getPlayerClan(player.getUniqueId()) != null) {
            plugin.getMessageManager().send(player, "clan.already-in-clan");
            return;
        }

        if (clan.getMemberCount() >= plugin.getConfigManager().maxMembers()) {
            plugin.getMessageManager().send(player, "clan.clan-full");
            return;
        }

        ClanMember newMember = new ClanMember(player.getUniqueId(), player.getName(), ClanRank.MEMBER, 0, 0, 0L, System.currentTimeMillis());
        clan.getMembers().add(newMember);

        plugin.getClanManager().updateClan(clan).thenRun(() -> {
            plugin.getInvitationManager().removeInvitation(player.getUniqueId(), invitation.getClanId());
            plugin.getMessageManager().send(player, "clan.joined", Map.of("clan", clan.getName()));
            plugin.getSoundManager().play(player, "clan-join");
            player.closeInventory();
        });
    }

    private void handleDeclineInvitation(Player player, ClanInvitation invitation, Clan clan) {
        plugin.getInvitationManager().removeInvitation(player.getUniqueId(), invitation.getClanId()).thenRun(() -> {
            plugin.getMessageManager().send(player, "clan.invitation-declined", 
                Map.of("clan", clan.getName()));
            player.closeInventory();
            plugin.getFoliaLib().getImpl().runLater(() -> {
                plugin.getGuiManager().openGUI(new ManageInvitationsGUI(plugin, player), player);
            }, 2L);
        });
    }
}