package com.clansystem.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class InventoryGUI implements InventoryHandler {
    private Inventory inventory;
    private final Map<Integer, InventoryButton> buttonMap = new HashMap<>();
    
    public InventoryGUI() {
    }
    
    public Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = this.createInventory();
        }
        return this.inventory;
    }
    
    public void addButton(int slot, InventoryButton button) {
        System.out.println("[DEBUG] Adding button to slot " + slot + " in " + this.getClass().getSimpleName());
        this.buttonMap.put(slot, button);
    }
    
    public void decorate(Player player) {
        this.buttonMap.forEach((slot, button) -> {
            ItemStack icon = button.getIconCreator().apply(player);
            this.getInventory().setItem(slot, icon);
        });
    }
    
    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();
        InventoryButton button = this.buttonMap.get(slot);
        System.out.println("[DEBUG] InventoryGUI click: slot=" + slot + ", button exists=" + (button != null) + ", GUI class=" + this.getClass().getSimpleName());
        if (button != null) {
            button.getEventConsumer().accept(event);
        }
    }
    
    @Override
    public void onOpen(InventoryOpenEvent event) {
        this.decorate((Player) event.getPlayer());
    }
    
    @Override
    public void onClose(InventoryCloseEvent event) {
    }
    
    protected abstract Inventory createInventory();
}