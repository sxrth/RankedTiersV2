package com.rankedtiers.gui;

import com.rankedtiers.RankedTiers;
import com.rankedtiers.kit.Kit;
import com.rankedtiers.kit.KitManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Inventory-based menu that lists every ranked kit so the player can click
 * one to join its matchmaking queue. The kit name is tagged onto each item
 * via PersistentDataContainer so the listener can resolve clicks reliably,
 * independent of display name / locale.
 */
public class QueueMenu {

    public static final String TITLE = "RankedTiers - Queue";
    public static final NamespacedKey KIT_KEY = new NamespacedKey(RankedTiers.getPluginInstance(), "queue-kit");

    public Inventory build(KitManager kitManager, Player viewer) {
        Inventory inventory = Bukkit.createInventory(null, 27, Component.text(TITLE));

        int slot = 0;
        for (Kit kit : kitManager.getAll().values()) {
            if (slot >= inventory.getSize()) break;

            ItemStack item = kit.toMenuItem();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(kit.getName()));
                meta.getPersistentDataContainer().set(KIT_KEY, PersistentDataType.STRING, kit.getName());
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);
            slot++;
        }

        return inventory;
    }
}
