package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanMember;
import com.clansystem.inventory.InventoryButton;
import com.clansystem.inventory.InventoryGUI;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClanMembersGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Clan clan;
    
    public ClanMembersGUI(ClanSystem plugin, Clan clan) {
        this.plugin = plugin;
        this.clan = clan;
    }
    
    @Override
    protected Inventory createInventory() {
        String title = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessageManager().getMessage("gui.members-title"));
        return Bukkit.createInventory(null, 54, title);
    }
    
    @Override
    public void decorate(Player player) {
        List<ClanMember> members = new ArrayList<>(clan.getMembers());
        members.sort((m1, m2) -> Integer.compare(m2.getRank().getPriority(), m1.getRank().getPriority()));
        
        for (int i = 0; i < Math.min(members.size(), 45); i++) {
            ClanMember member = members.get(i);
            final int slot = i;
            
            addButton(slot, new InventoryButton()
                .creator(p -> createMemberHead(member))
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    ClanMember clickerMember = clan.getMember(clicker.getUniqueId());
                    
                    if (clickerMember == null || !clickerMember.getRank().canManageMembers()) {
                        plugin.getMessageManager().send(clicker, "clan.only-owner-mod");
                        return;
                    }
                    
                    if (member.getPlayerUUID().equals(clan.getOwner())) {
                        plugin.getMessageManager().send(clicker, "member.cannot-kick-owner");
                        return;
                    }
                    
                    plugin.getGuiManager().openGUI(new PromoteDemoteGUI(plugin, clan, member), clicker);
                })
            );
        }
        
        addButton(49, new InventoryButton()
            .creator(p -> createItem("ARROW", "&cBack", "&7Return to main menu"))
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanMainGUI(plugin, clan), clicker);
            })
        );
        
        super.decorate(player);
    }
    
    private ItemStack createMemberHead(ClanMember member) {
        ItemStack skull = XSkull.createItem().profile(Profileable.username(member.getPlayerName())).apply();
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
            "&b" + member.getPlayerName()));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Rank: &f" + member.getRank().getDisplayName()));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Kills: &f" + member.getKills()));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Deaths: &f" + member.getDeaths()));
        lore.add("");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&eClick to manage"));
        
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }
    
    private ItemStack createItem(String materialName, String name, String... lore) {
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(new ItemStack(org.bukkit.Material.STONE));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(Arrays.stream(lore)
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .toList());
        item.setItemMeta(meta);
        return item;
    }
}