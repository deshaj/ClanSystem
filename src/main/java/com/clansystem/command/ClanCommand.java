package com.clansystem.command;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanMember;
import com.clansystem.data.ClanRank;
import com.clansystem.inventory.impl.ClanLookupGUI;
import com.clansystem.inventory.impl.ClanMainGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class ClanCommand implements CommandExecutor, TabCompleter {
    private final ClanSystem plugin;

    public ClanCommand(ClanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("cc")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            handleChat(player);
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Usage: /clan <subcommand>");
                return true;
            }
            handleGUI(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                if (args.length < 2) { plugin.getMessageManager().send(player, "clan.usage-create"); return true; }
                handleCreate(player, args[1]);
            }
            case "disband" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                handleDisband(player);
            }
            case "invite" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                if (args.length < 2) { plugin.getMessageManager().send(player, "clan.usage-invite"); return true; }
                handleInvite(player, args[1]);
            }
            case "join" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                if (args.length < 2) { plugin.getMessageManager().send(player, "clan.usage-join"); return true; }
                handleJoin(player, args[1]);
            }
            case "leave" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                handleLeave(player);
            }
            case "kick" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                if (args.length < 2) { plugin.getMessageManager().send(player, "clan.usage-kick"); return true; }
                handleKick(player, args[1]);
            }
            case "home" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                handleHome(player);
            }
            case "sethome" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                handleSetHome(player);
            }
            case "delhome" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                handleDelHome(player);
            }
            case "chat", "cc" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                handleChat(player);
            }
            case "info" -> {
                String clanName = args.length >= 2 ? args[1] : null;
                handleInfo(sender, clanName);
            }
            case "list" -> handleList(sender);
            case "gui" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                handleGUI(player);
            }
            case "promote" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                if (args.length < 2) { plugin.getMessageManager().send(player, "clan.usage-promote"); return true; }
                handlePromote(player, args[1]);
            }
            case "demote" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Only players."); return true; }
                if (args.length < 2) { plugin.getMessageManager().send(player, "clan.usage-demote"); return true; }
                handleDemote(player, args[1]);
            }
            default -> plugin.getMessageManager().send(sender, "clan.unknown-command");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("cc")) return Collections.emptyList();
        if (args.length == 1) {
            return Arrays.asList("create", "disband", "invite", "join", "leave", "kick", "home", "sethome", "delhome", "chat", "info", "list", "gui", "promote", "demote");
        }
        return Collections.emptyList();
    }

    private void handleCreate(Player player, String name) {
        if (plugin.getClanManager().getPlayerClan(player.getUniqueId()) != null) {
            plugin.getMessageManager().send(player, "clan.already-in-clan");
            return;
        }
        if (name.length() < plugin.getConfigManager().minNameLength()) {
            plugin.getMessageManager().send(player, "clan.name-too-short",
                Map.of("min", String.valueOf(plugin.getConfigManager().minNameLength())));
            return;
        }
        if (name.length() > plugin.getConfigManager().maxNameLength()) {
            plugin.getMessageManager().send(player, "clan.name-too-long",
                Map.of("max", String.valueOf(plugin.getConfigManager().maxNameLength())));
            return;
        }
        if (plugin.getClanManager().clanExists(name)) {
            plugin.getMessageManager().send(player, "clan.clan-exists");
            return;
        }
        plugin.getClanManager().createClan(player, name).thenAccept(clan -> {
            plugin.getPlayerDataManager().setPlayerClan(player.getUniqueId(), clan.getId());
            plugin.getSoundManager().play(player, "clan-create");
            plugin.getMessageManager().send(player, "clan.created", Map.of("clan", name));
        }).exceptionally(ex -> {
            plugin.error("Failed to create clan", ex);
            return null;
        });
    }

    private void handleDisband(Player player) {
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) { plugin.getMessageManager().send(player, "clan.not-in-clan"); return; }
        if (!clan.isOwner(player.getUniqueId())) { plugin.getMessageManager().send(player, "clan.only-owner"); return; }
        String clanName = clan.getName();
        for (ClanMember member : clan.getMembers()) {
            Player memberPlayer = Bukkit.getPlayer(member.getPlayerUUID());
            if (memberPlayer != null && !memberPlayer.getUniqueId().equals(player.getUniqueId())) {
                plugin.getMessageManager().send(memberPlayer, "clan.disbanded", Map.of("clan", clanName));
            }
        }
        plugin.getClanManager().disbandClan(clan.getId());
        plugin.getSoundManager().play(player, "clan-disband");
        plugin.getMessageManager().send(player, "clan.disbanded", Map.of("clan", clanName));
    }

    private void handleInvite(Player player, String targetName) {
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) { plugin.getMessageManager().send(player, "clan.not-in-clan"); return; }
        if (!clan.canManageMembers(player.getUniqueId())) { plugin.getMessageManager().send(player, "clan.only-owner-mod"); return; }
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) { plugin.getMessageManager().send(player, "clan.player-not-found"); return; }
        if (plugin.getClanManager().getPlayerClan(target.getUniqueId()) != null) { plugin.getMessageManager().send(player, "invite.already-member"); return; }
        if (clan.getMemberCount() >= plugin.getConfigManager().maxMembers()) { plugin.getMessageManager().send(player, "clan.clan-full"); return; }
        plugin.getPlayerDataManager().addInvitation(target.getUniqueId(), clan.getId());
        plugin.getSoundManager().play(player, "invite-send");
        plugin.getSoundManager().play(target, "invite-receive");
        plugin.getMessageManager().send(player, "invite.sent", Map.of("player", target.getName()));
        plugin.getMessageManager().send(target, "invite.received", Map.of("clan", clan.getName()));
    }

    private void handleJoin(Player player, String clanName) {
        if (plugin.getClanManager().getPlayerClan(player.getUniqueId()) != null) { plugin.getMessageManager().send(player, "clan.already-in-clan"); return; }
        Clan clan = plugin.getClanManager().getClanByName(clanName);
        if (clan == null) { plugin.getMessageManager().send(player, "clan.clan-not-found"); return; }
        if (clan.getMemberCount() >= plugin.getConfigManager().maxMembers()) { plugin.getMessageManager().send(player, "clan.clan-full"); return; }
        final Clan finalClan = clan;
        plugin.getPlayerDataManager().hasInvitation(player.getUniqueId(), clan.getId()).thenAccept(hasInvite -> {
            if (!hasInvite) { plugin.getMessageManager().send(player, "invite.no-invitation"); return; }
            plugin.getClanManager().addMember(finalClan, player, ClanRank.MEMBER);
            plugin.getPlayerDataManager().removeInvitation(player.getUniqueId(), finalClan.getId());
            plugin.getSoundManager().play(player, "clan-join");
            plugin.getMessageManager().send(player, "clan.joined", Map.of("clan", finalClan.getName()));
        }).exceptionally(ex -> {
            plugin.error("Failed to check invitation", ex);
            return null;
        });
    }

    private void handleLeave(Player player) {
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) { plugin.getMessageManager().send(player, "clan.not-in-clan"); return; }
        if (clan.isOwner(player.getUniqueId())) { plugin.getMessageManager().send(player, "clan.owner-cannot-leave"); return; }
        plugin.getClanManager().removeMember(clan, player.getUniqueId());
        plugin.getSoundManager().play(player, "clan-leave");
        plugin.getMessageManager().send(player, "clan.left", Map.of("clan", clan.getName()));
    }

    private void handleKick(Player player, String targetName) {
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) { plugin.getMessageManager().send(player, "clan.not-in-clan"); return; }
        if (!clan.canManageMembers(player.getUniqueId())) { plugin.getMessageManager().send(player, "clan.only-owner-mod"); return; }
        UUID targetUUID = null;
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            targetUUID = target.getUniqueId();
        } else {
            for (ClanMember member : clan.getMembers()) {
                if (member.getPlayerName().equalsIgnoreCase(targetName)) {
                    targetUUID = member.getPlayerUUID();
                    break;
                }
            }
        }
        if (targetUUID == null) { plugin.getMessageManager().send(player, "clan.player-not-found"); return; }
        if (clan.isOwner(targetUUID)) { plugin.getMessageManager().send(player, "member.cannot-kick-owner"); return; }
        plugin.getClanManager().removeMember(clan, targetUUID);
        plugin.getSoundManager().play(player, "clan-kick");
        if (target != null) plugin.getSoundManager().play(target, "clan-kick");
        plugin.getMessageManager().send(player, "member.kicked", Map.of("player", targetName));
        if (target != null) plugin.getMessageManager().send(target, "clan.kicked", Map.of("clan", clan.getName()));
    }

    private void handleHome(Player player) {
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) { plugin.getMessageManager().send(player, "clan.not-in-clan"); return; }
        if (!plugin.getHomeManager().hasHome(clan)) { plugin.getMessageManager().send(player, "home.no-home"); return; }
        if (plugin.getHomeManager().hasCooldown(player.getUniqueId())) {
            long remaining = plugin.getHomeManager().getRemainingCooldown(player.getUniqueId());
            plugin.getMessageManager().send(player, "home.cooldown", Map.of("time", String.valueOf(remaining)));
            return;
        }
        plugin.getHomeManager().teleportHome(player, clan);
        plugin.getSoundManager().play(player, "home-teleport");
        plugin.getMessageManager().send(player, "home.teleporting");
    }

    private void handleSetHome(Player player) {
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) { plugin.getMessageManager().send(player, "clan.not-in-clan"); return; }
        if (!clan.canManageMembers(player.getUniqueId())) { plugin.getMessageManager().send(player, "clan.only-owner-mod"); return; }
        plugin.getHomeManager().setHome(clan, player.getLocation());
        plugin.getSoundManager().play(player, "home-set");
        plugin.getMessageManager().send(player, "home.set");
    }

    private void handleDelHome(Player player) {
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) { plugin.getMessageManager().send(player, "clan.not-in-clan"); return; }
        if (!clan.canManageMembers(player.getUniqueId())) { plugin.getMessageManager().send(player, "clan.only-owner-mod"); return; }
        plugin.getHomeManager().deleteHome(clan);
        plugin.getSoundManager().play(player, "home-delete");
        plugin.getMessageManager().send(player, "home.deleted");
    }

    private void handleChat(Player player) {
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) { plugin.getMessageManager().send(player, "clan.not-in-clan"); return; }
        plugin.getClanChatManager().toggleClanChat(player.getUniqueId());
        if (plugin.getClanChatManager().isClanChatEnabled(player.getUniqueId())) {
            plugin.getSoundManager().play(player, "chat-toggle-on");
            plugin.getMessageManager().send(player, "chat.enabled");
        } else {
            plugin.getSoundManager().play(player, "chat-toggle-off");
            plugin.getMessageManager().send(player, "chat.disabled");
        }
    }

    private void handleInfo(CommandSender sender, String clanName) {
        Clan clan;
        if (clanName != null) {
            clan = plugin.getClanManager().getClanByName(clanName);
        } else if (sender instanceof Player player) {
            clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        } else {
            sender.sendMessage("Please specify a clan name!");
            return;
        }
        if (clan == null) { plugin.getMessageManager().send(sender, "clan.clan-not-found"); return; }
        int level = plugin.getLevelManager().calculateLevel(clan);
        String tag = plugin.getLevelManager().getClanTag(clan);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        String createdDate = sdf.format(new Date(clan.getCreatedAt()));
        Player owner = Bukkit.getPlayer(clan.getOwner());
        String ownerName = owner != null ? owner.getName() : "Unknown";
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("clan", clan.getName());
        placeholders.put("owner", ownerName);
        placeholders.put("count", String.valueOf(clan.getMemberCount()));
        placeholders.put("max", String.valueOf(plugin.getConfigManager().maxMembers()));
        placeholders.put("level", String.valueOf(level));
        placeholders.put("tag", tag);
        placeholders.put("kills", String.valueOf(clan.getTotalKills()));
        placeholders.put("deaths", String.valueOf(clan.getTotalDeaths()));
        placeholders.put("date", createdDate);
        sender.sendMessage(plugin.getMessageManager().getMessage("info.header", placeholders));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.owner", placeholders));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.members", placeholders));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.level", placeholders));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.tag", placeholders));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.stats", placeholders));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.created", placeholders));
    }

    private void handleList(CommandSender sender) {
        Collection<Clan> clans = plugin.getClanManager().getAllClans();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("count", String.valueOf(clans.size()));
        sender.sendMessage(plugin.getMessageManager().getMessage("list.header", placeholders));
        List<Clan> sortedClans = plugin.getClanManager().getClansSorted();
        for (int i = 0; i < Math.min(10, sortedClans.size()); i++) {
            Clan clan = sortedClans.get(i);
            int level = plugin.getLevelManager().calculateLevel(clan);
            Map<String, String> entryPlaceholders = new HashMap<>();
            entryPlaceholders.put("clan", clan.getName());
            entryPlaceholders.put("level", String.valueOf(level));
            entryPlaceholders.put("members", String.valueOf(clan.getMemberCount()));
            sender.sendMessage(plugin.getMessageManager().getMessage("list.entry", entryPlaceholders));
        }
    }

    private void handleGUI(Player player) {
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            plugin.getGuiManager().openGUI(new ClanLookupGUI(plugin, 1), player);
        } else {
            plugin.getGuiManager().openGUI(new ClanMainGUI(plugin, clan), player);
        }
    }

    private void handlePromote(Player player, String targetName) {
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) { plugin.getMessageManager().send(player, "clan.not-in-clan"); return; }
        if (!clan.isOwner(player.getUniqueId())) { plugin.getMessageManager().send(player, "clan.only-owner"); return; }
        UUID targetUUID = null;
        for (ClanMember member : clan.getMembers()) {
            if (member.getPlayerName().equalsIgnoreCase(targetName)) {
                targetUUID = member.getPlayerUUID();
                break;
            }
        }
        if (targetUUID == null) { plugin.getMessageManager().send(player, "clan.player-not-found"); return; }
        if (clan.isOwner(targetUUID)) { plugin.getMessageManager().send(player, "member.already-owner"); return; }
        plugin.getClanManager().promoteMember(clan, targetUUID);
        plugin.getSoundManager().playPromote(player);
        plugin.getMessageManager().send(player, "member.promoted", Map.of("player", targetName));
        Player target = Bukkit.getPlayer(targetUUID);
        if (target != null) {
            plugin.getSoundManager().playPromote(target);
            plugin.getMessageManager().send(target, "member.you-promoted", Map.of("clan", clan.getName()));
        }
    }

    private void handleDemote(Player player, String targetName) {
        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) { plugin.getMessageManager().send(player, "clan.not-in-clan"); return; }
        if (!clan.isOwner(player.getUniqueId())) { plugin.getMessageManager().send(player, "clan.only-owner"); return; }
        UUID targetUUID = null;
        for (ClanMember member : clan.getMembers()) {
            if (member.getPlayerName().equalsIgnoreCase(targetName)) {
                targetUUID = member.getPlayerUUID();
                break;
            }
        }
        if (targetUUID == null) { plugin.getMessageManager().send(player, "clan.player-not-found"); return; }
        if (clan.isOwner(targetUUID)) { plugin.getMessageManager().send(player, "member.cannot-demote-owner"); return; }
        plugin.getClanManager().demoteMember(clan, targetUUID);
        plugin.getSoundManager().playDemote(player);
        plugin.getMessageManager().send(player, "member.demoted", Map.of("player", targetName));
        Player target = Bukkit.getPlayer(targetUUID);
        if (target != null) {
            plugin.getSoundManager().playDemote(target);
            plugin.getMessageManager().send(target, "member.you-demoted", Map.of("clan", clan.getName()));
        }
    }
}