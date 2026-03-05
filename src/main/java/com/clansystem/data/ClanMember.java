package com.clansystem.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClanMember {
    private UUID playerUUID;
    private String playerName;
    private ClanRank rank;
    private int kills;
    private int deaths;
    private long playtime;
    private long joinedAt;
}