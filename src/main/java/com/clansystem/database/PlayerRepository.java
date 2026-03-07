package com.clansystem.database;

import com.clansystem.ClanSystem;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerRepository {
    private final ClanSystem plugin;
    private final Database database;
    
    public PlayerRepository(ClanSystem plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }
    
    public CompletableFuture<Void> setPlayerClan(UUID playerUUID, UUID clanId) {
        return database.executeAsync(conn -> {
            if (clanId == null) {
                try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM player_data WHERE player_uuid = ?"
                )) {
                    stmt.setString(1, playerUUID.toString());
                    stmt.executeUpdate();
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement(
                    "REPLACE INTO player_data (player_uuid, clan_id) VALUES (?, ?)"
                )) {
                    stmt.setString(1, playerUUID.toString());
                    stmt.setString(2, clanId.toString());
                    stmt.executeUpdate();
                }
            }
            plugin.debug("Set player clan: " + playerUUID + " -> " + clanId);
        });
    }
    
    public CompletableFuture<UUID> getPlayerClan(UUID playerUUID) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT clan_id FROM player_data WHERE player_uuid = ?"
            )) {
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String clanIdStr = rs.getString("clan_id");
                    return clanIdStr != null ? UUID.fromString(clanIdStr) : null;
                }
            }
            return null;
        });
    }
    
    public CompletableFuture<Void> addInvitation(UUID playerUUID, UUID clanId) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO clan_invitations (player_uuid, clan_id) VALUES (?, ?)"
            )) {
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, clanId.toString());
                stmt.executeUpdate();
            }
            plugin.debug("Added invitation: " + playerUUID + " <- " + clanId);
        });
    }
    
    public CompletableFuture<Void> removeInvitation(UUID playerUUID, UUID clanId) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM clan_invitations WHERE player_uuid = ? AND clan_id = ?"
            )) {
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, clanId.toString());
                stmt.executeUpdate();
            }
            plugin.debug("Removed invitation: " + playerUUID + " <- " + clanId);
        });
    }
    
    public CompletableFuture<Boolean> hasInvitation(UUID playerUUID, UUID clanId) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT 1 FROM clan_invitations WHERE player_uuid = ? AND clan_id = ?"
            )) {
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, clanId.toString());
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        });
    }
    
    public CompletableFuture<Set<UUID>> getInvitations(UUID playerUUID) {
        return database.executeAsync(conn -> {
            Set<UUID> invitations = new HashSet<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT clan_id FROM clan_invitations WHERE player_uuid = ?"
            )) {
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    invitations.add(UUID.fromString(rs.getString("clan_id")));
                }
            }
            return invitations;
        });
    }

    public CompletableFuture<Void> addPendingNotification(UUID playerUUID, String notificationType, String clanName) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO pending_notifications (player_uuid, notification_type, clan_name, timestamp) VALUES (?, ?, ?, ?)"
            )) {
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, notificationType);
                stmt.setString(3, clanName);
                stmt.setLong(4, System.currentTimeMillis());
                stmt.executeUpdate();
                plugin.debug("Added pending notification for " + playerUUID + ": " + notificationType + " from " + clanName);
            }
        });
    }

    public CompletableFuture<List<Map<String, String>>> getPendingNotifications(UUID playerUUID) {
        return database.executeAsync(conn -> {
            List<Map<String, String>> notifications = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT notification_type, clan_name FROM pending_notifications WHERE player_uuid = ?"
            )) {
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Map<String, String> notification = new HashMap<>();
                    notification.put("type", rs.getString("notification_type"));
                    notification.put("clan", rs.getString("clan_name"));
                    notifications.add(notification);
                }
            }
            return notifications;
        });
    }

    public CompletableFuture<Void> clearPendingNotifications(UUID playerUUID) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM pending_notifications WHERE player_uuid = ?"
            )) {
                stmt.setString(1, playerUUID.toString());
                int deleted = stmt.executeUpdate();
                plugin.debug("Cleared " + deleted + " pending notifications for " + playerUUID);
            }
        });
    }
}