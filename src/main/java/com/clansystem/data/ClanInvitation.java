package com.clansystem.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClanInvitation {
    private UUID id;
    private UUID clanId;
    private UUID playerId;
    private UUID invitedBy;
    private long createdAt;
    private long expiresAt;
    private boolean expired;

    public ClanInvitation(UUID clanId, UUID playerId) {
        this.id = UUID.randomUUID();
        this.clanId = clanId;
        this.playerId = playerId;
        this.invitedBy = null;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = this.createdAt + (3L * 24 * 60 * 60 * 1000);
        this.expired = false;
    }

    public ClanInvitation(UUID id, UUID clanId, UUID playerId, long createdAt, long expiresAt) {
        this.id = id;
        this.clanId = clanId;
        this.playerId = playerId;
        this.invitedBy = null;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.expired = false;
    }

    public long getTimestamp() {
        return createdAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public long getTimeRemaining() {
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    public String getTimeRemainingFormatted() {
        long remaining = getTimeRemaining();
        long days = remaining / (24 * 60 * 60 * 1000);
        long hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        if (days > 0) {
            return days + "d " + hours + "h";
        } else {
            return hours + "h";
        }
    }
}