package com.clansystem.data;

public enum ClanRank {
    OWNER,
    MOD,
    MEMBER;
    
    public String getDisplayName() {
        return switch (this) {
            case OWNER -> "Owner";
            case MOD -> "Moderator";
            case MEMBER -> "Member";
        };
    }
    
    public int getPriority() {
        return switch (this) {
            case OWNER -> 3;
            case MOD -> 2;
            case MEMBER -> 1;
        };
    }
    
    public boolean canManageMembers() {
        return this == OWNER || this == MOD;
    }
    
    public boolean isOwner() {
        return this == OWNER;
    }
}