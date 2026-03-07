package com.clansystem.database;

import com.clansystem.ClanSystem;
import com.clansystem.data.JoinRequest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RequestRepository {
    private final ClanSystem plugin;
    private final Database database;

    public RequestRepository(ClanSystem plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    public CompletableFuture<Boolean> addRequest(UUID playerUUID, UUID clanId, String message, long timestamp) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO join_requests (player_uuid, clan_id, message, timestamp) VALUES (?, ?, ?, ?)"
            )) {
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, clanId.toString());
                stmt.setString(3, message);
                stmt.setLong(4, timestamp);
                int rows = stmt.executeUpdate();
                plugin.debug("Added join request: " + playerUUID + " -> " + clanId + " with message: " + message);
                return rows > 0;
            }
        });
    }

    public CompletableFuture<Void> removeRequest(UUID playerUUID, UUID clanId) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM join_requests WHERE player_uuid = ? AND clan_id = ?"
            )) {
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, clanId.toString());
                stmt.executeUpdate();
            }
            plugin.debug("Removed join request: " + playerUUID + " -> " + clanId);
        });
    }

    public CompletableFuture<Boolean> hasRequest(UUID playerUUID, UUID clanId) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT 1 FROM join_requests WHERE player_uuid = ? AND clan_id = ?"
            )) {
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, clanId.toString());
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        });
    }

    public CompletableFuture<Set<UUID>> getRequestsForClan(UUID clanId) {
        return database.executeAsync(conn -> {
            Set<UUID> requests = new HashSet<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT player_uuid FROM join_requests WHERE clan_id = ?"
            )) {
                stmt.setString(1, clanId.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    requests.add(UUID.fromString(rs.getString("player_uuid")));
                }
            }
            return requests;
        });
    }

    public CompletableFuture<Set<JoinRequest>> getFullRequestsForClan(UUID clanId) {
        return database.executeAsync(conn -> {
            Set<JoinRequest> requests = new HashSet<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT player_uuid, clan_id, message, timestamp FROM join_requests WHERE clan_id = ?"
            )) {
                stmt.setString(1, clanId.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                    UUID storedClanId = UUID.fromString(rs.getString("clan_id"));
                    String message = rs.getString("message");
                    long timestamp = rs.getLong("timestamp");
                    requests.add(new JoinRequest(playerUUID, storedClanId, message, timestamp));
                }
            }
            plugin.debug("Loaded " + requests.size() + " full requests for clan " + clanId);
            return requests;
        });
    }

    public CompletableFuture<Set<UUID>> getRequestsByPlayer(UUID playerUUID) {
        return database.executeAsync(conn -> {
            Set<UUID> requests = new HashSet<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT clan_id FROM join_requests WHERE player_uuid = ?"
            )) {
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    requests.add(UUID.fromString(rs.getString("clan_id")));
                }
            }
            return requests;
        });
    }

    public CompletableFuture<Void> removeAllRequestsByPlayer(UUID playerUUID) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM join_requests WHERE player_uuid = ?"
            )) {
                stmt.setString(1, playerUUID.toString());
                int deleted = stmt.executeUpdate();
                plugin.debug("Removed " + deleted + " join requests for player " + playerUUID);
            }
        });
    }
}