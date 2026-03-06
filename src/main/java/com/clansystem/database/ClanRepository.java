package com.clansystem.database;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanHome;
import com.clansystem.data.ClanMember;
import com.clansystem.data.ClanRank;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ClanRepository {
    private final ClanSystem plugin;
    private final Database database;
    
    public ClanRepository(ClanSystem plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }
    
    public CompletableFuture<Void> saveClan(Clan clan) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "REPLACE INTO clans (id, name, owner, total_kills, total_deaths, total_playtime, " +
                "total_money, created_at, home_world, home_x, home_y, home_z, home_yaw, home_pitch) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            )) {
                stmt.setString(1, clan.getId().toString());
                stmt.setString(2, clan.getName());
                stmt.setString(3, clan.getOwner().toString());
                stmt.setInt(4, clan.getTotalKills());
                stmt.setInt(5, clan.getTotalDeaths());
                stmt.setLong(6, clan.getTotalPlaytime());
                stmt.setDouble(7, clan.getTotalMoney());
                stmt.setLong(8, clan.getCreatedAt());
                
                if (clan.getHome() != null) {
                    ClanHome home = clan.getHome();
                    stmt.setString(9, home.getWorld());
                    stmt.setDouble(10, home.getX());
                    stmt.setDouble(11, home.getY());
                    stmt.setDouble(12, home.getZ());
                    stmt.setFloat(13, home.getYaw());
                    stmt.setFloat(14, home.getPitch());
                } else {
                    stmt.setNull(9, java.sql.Types.VARCHAR);
                    stmt.setNull(10, java.sql.Types.DOUBLE);
                    stmt.setNull(11, java.sql.Types.DOUBLE);
                    stmt.setNull(12, java.sql.Types.DOUBLE);
                    stmt.setNull(13, java.sql.Types.FLOAT);
                    stmt.setNull(14, java.sql.Types.FLOAT);
                }
                
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM clan_members WHERE clan_id = ?"
            )) {
                stmt.setString(1, clan.getId().toString());
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO clan_members (clan_id, player_uuid, player_name, rank, kills, deaths, playtime, joined_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            )) {
                for (ClanMember member : clan.getMembers()) {
                    stmt.setString(1, clan.getId().toString());
                    stmt.setString(2, member.getPlayerUUID().toString());
                    stmt.setString(3, member.getPlayerName());
                    stmt.setString(4, member.getRank().name());
                    stmt.setInt(5, member.getKills());
                    stmt.setInt(6, member.getDeaths());
                    stmt.setLong(7, member.getPlaytime());
                    stmt.setLong(8, member.getJoinedAt());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            plugin.debug("Saved clan: " + clan.getName());
        });
    }
    
    public CompletableFuture<Void> deleteClan(UUID clanId) {
        return database.executeAsync(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM clans WHERE id = ?")) {
                stmt.setString(1, clanId.toString());
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM clan_members WHERE clan_id = ?")) {
                stmt.setString(1, clanId.toString());
                stmt.executeUpdate();
            }
            
            plugin.debug("Deleted clan: " + clanId);
        });
    }
    
    public CompletableFuture<Map<UUID, Clan>> loadAllClans() {
        return database.executeAsync(conn -> {
            Map<UUID, Clan> clans = new HashMap<>();
            
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM clans")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Clan clan = new Clan();
                    clan.setId(UUID.fromString(rs.getString("id")));
                    clan.setName(rs.getString("name"));
                    clan.setOwner(UUID.fromString(rs.getString("owner")));
                    clan.setTotalKills(rs.getInt("total_kills"));
                    clan.setTotalDeaths(rs.getInt("total_deaths"));
                    clan.setTotalPlaytime(rs.getLong("total_playtime"));
                    clan.setTotalMoney(rs.getDouble("total_money"));
                    clan.setCreatedAt(rs.getLong("created_at"));
                    
                    String homeWorld = rs.getString("home_world");
                    if (homeWorld != null) {
                        ClanHome home = new ClanHome(
                            homeWorld,
                            rs.getDouble("home_x"),
                            rs.getDouble("home_y"),
                            rs.getDouble("home_z"),
                            rs.getFloat("home_yaw"),
                            rs.getFloat("home_pitch")
                        );
                        clan.setHome(home);
                    }
                    
                    clans.put(clan.getId(), clan);
                }
            }
            
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM clan_members")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    UUID clanId = UUID.fromString(rs.getString("clan_id"));
                    Clan clan = clans.get(clanId);
                    if (clan != null) {
                        ClanMember member = new ClanMember(
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("player_name"),
                            ClanRank.valueOf(rs.getString("rank")),
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getLong("playtime"),
                            rs.getLong("joined_at")
                        );
                        clan.getMembers().add(member);
                    }
                }
            }
            
            plugin.log("Loaded " + clans.size() + " clans from database");
            return clans;
        });
    }
    
    public CompletableFuture<Clan> loadClan(UUID clanId) {
        return database.executeAsync(conn -> {
            Clan clan = null;
            
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM clans WHERE id = ?")) {
                stmt.setString(1, clanId.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    clan = new Clan();
                    clan.setId(UUID.fromString(rs.getString("id")));
                    clan.setName(rs.getString("name"));
                    clan.setOwner(UUID.fromString(rs.getString("owner")));
                    clan.setTotalKills(rs.getInt("total_kills"));
                    clan.setTotalDeaths(rs.getInt("total_deaths"));
                    clan.setTotalPlaytime(rs.getLong("total_playtime"));
                    clan.setTotalMoney(rs.getDouble("total_money"));
                    clan.setCreatedAt(rs.getLong("created_at"));
                    
                    String homeWorld = rs.getString("home_world");
                    if (homeWorld != null) {
                        ClanHome home = new ClanHome(
                            homeWorld,
                            rs.getDouble("home_x"),
                            rs.getDouble("home_y"),
                            rs.getDouble("home_z"),
                            rs.getFloat("home_yaw"),
                            rs.getFloat("home_pitch")
                        );
                        clan.setHome(home);
                    }
                }
            }
            
            if (clan != null) {
                try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM clan_members WHERE clan_id = ?"
                )) {
                    stmt.setString(1, clanId.toString());
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        ClanMember member = new ClanMember(
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("player_name"),
                            ClanRank.valueOf(rs.getString("rank")),
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getLong("playtime"),
                            rs.getLong("joined_at")
                        );
                        clan.getMembers().add(member);
                    }
                }
            }
            
            return clan;
        });
    }
}