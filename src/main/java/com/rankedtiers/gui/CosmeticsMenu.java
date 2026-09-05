package com.rankedtiers.gui;

import com.rankedtiers.cosmetics.CosmeticOption;
import com.rankedtiers.cosmetics.CosmeticsManager;
import com.rankedtiers.data.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Lets a player pick an unlocked trim pattern (row 1) and material (row 2).
 * Locked options are shown as barriers with a "requires <tier>" lore line.
 */
public class CosmeticsMenu {

    public static final String TITLE = "RankedTiers - Cosmetics";
    public static final NamespacedKey TYPE_KEY;
    public static final NamespacedKey ID_KEY;

    static {
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(CosmeticsMenu.class);
        TYPE_KEY = new NamespacedKey(plugin, "cosmetic-type");
        ID_KEY = new NamespacedKey(plugin, "cosmetic-id");
    }

    public Inventory build(CosmeticsManager cosmetics, PlayerData data) {
        Inventory inventory = Bukkit.createInventory(null, 27, Component.text(TITLE));

        placeRow(inventory, 0, "pattern", cosmetics.getPatterns(), cosmetics, data, Material.LEATHER_HELMET);
        placeRow(inventory, 9, "material", cosmetics.getMaterials(), cosmetics, data, Material.IRON_INGOT);

        return inventory;
    }

    private void placeRow(Inventory inventory, int startSlot, String type, List<CosmeticOption> options,
                           CosmeticsManager cosmetics, PlayerData data, Material unlockedIcon) {
        int slot = startSlot;
        for (CosmeticOption option : options) {
            if (slot >= startSlot + 9) break;

            boolean unlocked = cosmetics.isUnlocked(data, option);
            ItemStack item = new ItemStack(unlocked ? unlockedIcon : Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(option.id()));
                meta.setLore(List.of(unlocked
                        ? "§aUnlocked - click to select"
                        : "§cRequires tier: " + option.requiredTier()));
                meta.getPersistentDataContainer().set(TYPE_KEY, PersistentDataType.STRING, type);
                meta.getPersistentDataContainer().set(ID_KEY, PersistentDataType.STRING, option.id());
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);
            slot++;
        }
    }
}
