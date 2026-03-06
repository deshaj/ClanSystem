package com.clansystem.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinRequest {
    private UUID id;
    private UUID playerId;
    private String playerName;
    private UUID clanId;
    private String message;
    private int kills;
    private int deaths;
    private long timestamp;
    private long createdAt;
    private double playerKills;
    private double playerDeaths;
    private boolean expired;

    public JoinRequest(UUID id, UUID clanId, UUID playerId, String message, long createdAt, long expiresAt) {
        this.id = id;
        this.clanId = clanId;
        this.playerId = playerId;
        this.message = message;
        this.createdAt = createdAt;
        this.timestamp = createdAt;
        this.expired = false;
    }

    public JoinRequest(UUID id, UUID clanId, UUID playerId, String message, long createdAt, double playerKills, double playerDeaths) {
        this.id = id;
        this.clanId = clanId;
        this.playerId = playerId;
        this.message = message;
        this.createdAt = createdAt;
        this.timestamp = createdAt;
        this.playerKills = playerKills;
        this.playerDeaths = playerDeaths;
        this.expired = false;
    }

    public double getKD() {
        if (deaths == 0) return kills;
        return Math.round((double) kills / deaths * 100.0) / 100.0;
    }

    public boolean isExpired() {
        long threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L;
        return System.currentTimeMillis() - timestamp > threeDaysInMillis;
    }
}