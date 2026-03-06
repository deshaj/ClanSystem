package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.data.ClanMember;
import com.clansystem.inventory.InventoryButton;
import com.clansystem.inventory.InventoryGUI;
import com.clansystem.util.ColorUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanMembersGUI extends InventoryGUI {
    private final ClanSystem plugin;
    private final Clan clan;
    
    public ClanMembersGUI(ClanSystem plugin, Clan clan) {
        this.plugin = plugin;
        this.clan = clan;
    }
    
    @Override
    protected Inventory createInventory() {
        String title = plugin.getMessageManager().getMessage("gui.members-title");
        return Bukkit.createInventory(null, 54, title);
    }
    
    @Override
    public void decorate(Player player) {
        addFillerGlass();
        
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
                    
                    plugin.getGuiManager().openGUI(new PromoteDemoteGUI(plugin, clan, member), clicker, false);
                })
            );
        }
        
        addButton(49, new InventoryButton()
            .creator(p -> {
                String name = plugin.getMessageManager().getMessage("gui.items.back.name");
                List<String> lore = plugin.getMessageManager().getLore("gui.items.back.lore");
                return createItemWithLore("ARROW", name, lore);
            })
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                plugin.getGuiManager().openGUI(new ClanMainGUI(plugin, clan), clicker, false);
            })
        );
        
        super.decorate(player);
    }

    private void addFillerGlass() {
        boolean enabled = plugin.getConfigManager().getBoolean("gui.members-menu.filler.enabled", true);
        if (!enabled) return;

        String materialName = plugin.getConfigManager().getString("gui.members-menu.filler.material", "GRAY_STAINED_GLASS_PANE");
        String name = plugin.getConfigManager().getString("gui.members-menu.filler.name", " ");
        
        ItemStack filler = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(null);
        if (filler == null) return;
        
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().format(name));
            filler.setItemMeta(meta);
        }

        for (int i = 45; i < 54; i++) {
            if (i != 49) {
                getInventory().setItem(i, filler);
            }
        }
    }
    
    private ItemStack createMemberHead(ClanMember member) {
        ItemStack skull = XSkull.createItem().profile(Profileable.username(member.getPlayerName())).apply();
        ItemMeta meta = skull.getItemMeta();
        
        double kd = member.getDeaths() > 0 ? 
            Math.round((double) member.getKills() / member.getDeaths() * 100.0) / 100.0 : member.getKills();
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", member.getPlayerName());
        placeholders.put("rank", member.getRank().getDisplayName());
        placeholders.put("kills", String.valueOf(member.getKills()));
        placeholders.put("deaths", String.valueOf(member.getDeaths()));
        placeholders.put("kd", String.valueOf(kd));
        
        String name = plugin.getMessageManager().getMessage("gui.items.member-head.name", placeholders);
        List<String> lore = plugin.getMessageManager().getLore("gui.items.member-head.lore", placeholders);
        
        meta.setDisplayName(name);
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }
    
    private ItemStack createItemWithLore(String materialName, String name, List<String> lore) {
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem)
            .orElse(new ItemStack(Material.STONE));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}