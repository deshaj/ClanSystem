package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.JoinRequest;
import com.clansystem.inventory.InventoryButton;
import com.clansystem.inventory.InventoryGUI;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JoinRequestsGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Player player;
    private final Clan clan;

    public JoinRequestsGUI(ClanSystem plugin, Player player, Clan clan) {
        this.plugin = plugin;
        this.player = player;
        this.clan = clan;
    }

    @Override
    protected Inventory createInventory() {
        String title = plugin.getConfigManager().getString("gui.join-requests.title", "&8Join Requests");
        int size = plugin.getConfigManager().getInt("gui.join-requests.size", 54);
        return Bukkit.createInventory(null, size, plugin.getMessageManager().format(title));
    }

    @Override
    public void decorate(Player player) {
        addFillerGlass();
        loadJoinRequests();
        addBackButton();
        super.decorate(player);
    }
    
    private void addFillerGlass() {
        boolean enabled = plugin.getConfigManager().getBoolean("gui.join-requests-menu.filler.enabled", true);
        if (!enabled) return;

        String materialName = plugin.getConfigManager().getString("gui.join-requests-menu.filler.material", "GRAY_STAINED_GLASS_PANE");
        String name = plugin.getConfigManager().getString("gui.join-requests-menu.filler.name", " ");

        ItemStack filler = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(null);
        if (filler == null) return;

        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().format(name));
            filler.setItemMeta(meta);
        }

        List<Integer> fillerSlots = plugin.getConfigManager().getIntList("gui.join-requests-menu.filler.slots");
        if (fillerSlots.isEmpty()) {
            for (int i = 45; i < 54; i++) {
                getInventory().setItem(i, filler);
            }
        } else {
            for (int slot : fillerSlots) {
                if (slot >= 0 && slot < getInventory().getSize()) {
                    getInventory().setItem(slot, filler);
                }
            }
        }
    }

    private void loadJoinRequests() {
        plugin.getInvitationManager().loadClanRequests(clan.getId()).thenRun(() -> {
            plugin.getFoliaLib().getImpl().runLater(() -> {
                List<JoinRequest> requests = plugin.getInvitationManager().getClanRequests(clan.getId());
                int slot = 0;
                for (JoinRequest request : requests) {
                    if (slot >= 45) break;
                    addRequestButton(slot, request);
                    slot++;
                }
            }, 1L);
        });
    }

    private void addRequestButton(int slot, JoinRequest request) {
        OfflinePlayer requester = Bukkit.getOfflinePlayer(request.getPlayerId());
        String name = plugin.getConfigManager().getString("gui.join-requests-menu.items.request-item.name", "&e{player}");
        List<String> lore = plugin.getConfigManager().getStringList("gui.join-requests-menu.items.request-item.lore");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        String timeAgo = getTimeAgo(request.getTimestamp());
        String requesterName = requester.getName() != null ? requester.getName() : "Unknown";

        String finalName = name.replace("{player}", requesterName);
        double kd = request.getDeaths() > 0 ? Math.round((double) request.getKills() / request.getDeaths() * 100.0) / 100.0 : request.getKills();
        List<String> finalLore = new ArrayList<>();
        for (String line : lore) {
            line = line.replace("{player}", requesterName)
                .replace("{message}", request.getMessage() != null ? request.getMessage() : "")
                .replace("{time}", dateFormat.format(new Date(request.getTimestamp())))
                .replace("{time-ago}", timeAgo)
                .replace("{kills}", String.valueOf(request.getKills()))
                .replace("{deaths}", String.valueOf(request.getDeaths()))
                .replace("{kd}", String.format("%.2f", kd));
            finalLore.add(line);
        }

        addButton(slot, new InventoryButton()
            .creator(p -> createPlayerHead(requesterName, finalName, finalLore))
            .consumer(event -> {
                if (event.isLeftClick()) {
                    acceptRequest(request, requester);
                } else if (event.isRightClick()) {
                    declineRequest(request, requester);
                }
            })
        );
    }

    private void acceptRequest(JoinRequest request, OfflinePlayer requester) {
        if (clan.getMemberCount() >= plugin.getConfigManager().getInt("clan.max-members", 10)) {
            plugin.getMessageManager().send(player, "clan.clan-full");
            return;
        }

        Player onlineRequester = requester.getPlayer();
        if (onlineRequester == null) {
            plugin.getPlayerRepository().addPendingNotification(request.getPlayerId(), "ACCEPTED", clan.getName());
        }

        if (onlineRequester != null) {
            plugin.getClanManager().addMember(clan, onlineRequester, com.clansystem.data.ClanRank.MEMBER).thenRun(() -> {
                plugin.getInvitationManager().removeRequest(request.getId());
                plugin.getRequestRepository().removeRequest(request.getPlayerId(), clan.getId());
                String requesterName = requester.getName() != null ? requester.getName() : "Unknown";
                
                plugin.getFoliaLib().getImpl().runLater(() -> {
                    plugin.getMessageManager().send(player, "gui.join-request-accepted", Map.of("player", requesterName));
                    plugin.getMessageManager().send(onlineRequester, "invitation.accepted", Map.of("clan", clan.getName()));

                    XSound.matchXSound(plugin.getConfigManager().getString("sounds.join", "ENTITY_EXPERIENCE_ORB_PICKUP"))
                        .ifPresent(sound -> {
                            sound.play(player);
                            sound.play(onlineRequester);
                        });

                    if (player.getOpenInventory().getTopInventory().equals(getInventory())) {
                        getInventory().clear();
                        decorate(player);
                    }
                }, 1L);
            });
        } else {
            plugin.getInvitationManager().removeRequest(request.getId());
            plugin.getRequestRepository().removeRequest(request.getPlayerId(), clan.getId());
            String requesterName = requester.getName() != null ? requester.getName() : "Unknown";
            
            plugin.getFoliaLib().getImpl().runLater(() -> {
                plugin.getMessageManager().send(player, "gui.join-request-accepted", Map.of("player", requesterName));

                XSound.matchXSound(plugin.getConfigManager().getString("sounds.join", "ENTITY_EXPERIENCE_ORB_PICKUP"))
                    .ifPresent(sound -> sound.play(player));

                if (player.getOpenInventory().getTopInventory().equals(getInventory())) {
                    getInventory().clear();
                    decorate(player);
                }
            }, 1L);
        }
    }

    private void declineRequest(JoinRequest request, OfflinePlayer requester) {
        Player onlineRequester = requester.getPlayer();
        if (onlineRequester == null) {
            plugin.getPlayerRepository().addPendingNotification(request.getPlayerId(), "DECLINED", clan.getName());
        }
        
        plugin.getInvitationManager().removeRequest(request.getId());
        plugin.getRequestRepository().removeRequest(request.getPlayerId(), clan.getId());
        String requesterName = requester.getName() != null ? requester.getName() : "Unknown";
        
        plugin.getFoliaLib().getImpl().runLater(() -> {
            plugin.getMessageManager().send(player, "gui.join-request-declined", Map.of("player", requesterName));
            
            if (onlineRequester != null) {
                plugin.getMessageManager().send(onlineRequester, "invitation.declined", Map.of("clan", clan.getName()));
            }

            XSound.matchXSound(plugin.getConfigManager().getString("sounds.click", "UI_BUTTON_CLICK"))
                .ifPresent(sound -> sound.play(player));

            if (player.getOpenInventory().getTopInventory().equals(getInventory())) {
                getInventory().clear();
                decorate(player);
            }
        }, 1L);
    }

    private void addBackButton() {
        int slot = plugin.getConfigManager().getInt("gui.join-requests-menu.items.back.slot", 49);
        String materialName = plugin.getConfigManager().getString("gui.join-requests-menu.items.back.material", "ARROW");
        String name = plugin.getConfigManager().getString("gui.join-requests-menu.items.back.name", "&c← Back");
        List<String> lore = plugin.getConfigManager().getStringList("gui.join-requests-menu.items.back.lore");

        addButton(slot, new InventoryButton()
            .creator(p -> createItem(materialName, name, lore))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanMainGUI(plugin, clan), clicker, false);
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

    private ItemStack createPlayerHead(String playerName, String name, List<String> lore) {
        ItemStack head = XSkull.createItem().profile(Profileable.detect(playerName)).apply();

        ItemMeta meta = head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().format(name));

            List<String> formattedLore = new ArrayList<>();
            for (String line : lore) {
                formattedLore.add(plugin.getMessageManager().format(line));
            }
            meta.setLore(formattedLore);

            head.setItemMeta(meta);
        }

        return head;
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