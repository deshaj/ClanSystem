package com.clansystem.inventory.impl;

import com.clansystem.ClanSystem;
import com.clansystem.data.Clan;
import com.clansystem.inventory.InventoryButton;
import com.clansystem.inventory.InventoryGUI;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class BrowseClansGUI extends InventoryGUI {
    private final ClanSystem plugin;

    public BrowseClansGUI(ClanSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Inventory createInventory() {
        String title = plugin.getMessageManager().getMessage("gui.browse.title");
        return Bukkit.createInventory(null, 54, title);
    }

    @Override
    public void decorate(Player player) {
        List<Clan> clans = plugin.getClanManager().getClansSorted();
        
        int slot = 0;
        for (Clan clan : clans) {
            if (slot >= 54) break;
            
            int finalSlot = slot;
            addButton(slot, new InventoryButton()
                .creator(p -> {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(clan.getOwner());
                    ItemStack skull = XSkull.createItem().profile(Profileable.detect(owner.getName())).apply();
                    ItemMeta meta = skull.getItemMeta();
                    if (meta != null) {
                        String name = plugin.getMessageManager().getMessage("gui.browse.clan-item.name")
                            .replace("{clan}", clan.getName());
                        meta.setDisplayName(plugin.getMessageManager().color(name));
                        
                        List<String> lore = plugin.getMessageManager().getLore("gui.browse.clan-item.lore");
                        double kd = clan.getTotalDeaths() > 0 ? 
                            Math.round((double) clan.getTotalKills() / clan.getTotalDeaths() * 100.0) / 100.0 : clan.getTotalKills();
                        String ownerName = owner.getName() != null ? owner.getName() : "Unknown";
                        lore = lore.stream()
                            .map(line -> line
                                .replace("{owner}", ownerName)
                                .replace("{members}", String.valueOf(clan.getMemberCount()))
                                .replace("{kills}", String.valueOf(clan.getTotalKills()))
                                .replace("{deaths}", String.valueOf(clan.getTotalDeaths()))
                                .replace("{kd}", String.valueOf(kd)))
                            .map(plugin.getMessageManager()::color)
                            .collect(Collectors.toList());
                        meta.setLore(lore);
                        skull.setItemMeta(meta);
                    }
                    return skull;
                })
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    plugin.getGuiManager().openGUI(new ClanDetailsGUI(plugin, clan), clicker);
                })
            );
            slot++;
        }
        
        super.decorate(player);
    }
}