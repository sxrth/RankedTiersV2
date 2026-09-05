package com.rankedtiers.gui;

import com.rankedtiers.kit.Kit;
import com.rankedtiers.kit.KitManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only overview for admins: shows each kit's icon and whether its two
 * arena positions have been set. Actual position editing is done with
 * /rtadmin setpos1 &lt;kit&gt; and /rtadmin setpos2 &lt;kit&gt; while standing
 * at the desired spot.
 */
public class KitEditorMenu {

    public static final String TITLE = "RankedTiers - Kit Editor";

    public Inventory build(KitManager kitManager) {
        Inventory inventory = Bukkit.createInventory(null, 27, Component.text(TITLE));

        int slot = 0;
        for (Kit kit : kitManager.getAll().values()) {
            if (slot >= inventory.getSize()) break;

            ItemStack item = kit.toMenuItem();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(kit.getName()));
                List<String> lore = new ArrayList<>();
                boolean configured = kit.getArena().isConfigured();
                lore.add(configured ? "§aArena configured" : "§cArena NOT configured");
                lore.add("§7Block place: " + kit.allowsBlockPlace());
                lore.add("§7Block break: " + kit.allowsBlockBreak());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);
            slot++;
        }

        return inventory;
    }
}
