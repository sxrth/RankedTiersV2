package com.rankedtiers.listeners;

import com.rankedtiers.RankedTiers;
import com.rankedtiers.data.PlayerData;
import com.rankedtiers.gui.CosmeticsMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Wires the cosmetics menu clicks to PlayerData, and re-applies the chosen
 * trim automatically whenever the player puts on a new piece of armor.
 */
public class CosmeticsListener implements Listener {

    private final RankedTiers plugin;

    public CosmeticsListener(RankedTiers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().title().equals(Component.text(CosmeticsMenu.TITLE))) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        String type = meta.getPersistentDataContainer().get(CosmeticsMenu.TYPE_KEY, PersistentDataType.STRING);
        String id = meta.getPersistentDataContainer().get(CosmeticsMenu.ID_KEY, PersistentDataType.STRING);
        if (type == null || id == null) return;

        PlayerData data = plugin.getDataStore().get(player.getUniqueId());
        boolean unlocked = (type.equals("pattern") ? plugin.getCosmeticsManager().getPatterns() : plugin.getCosmeticsManager().getMaterials())
                .stream()
                .filter(o -> o.id().equalsIgnoreCase(id))
                .findFirst()
                .map(o -> plugin.getCosmeticsManager().isUnlocked(data, o))
                .orElse(false);

        if (!unlocked) {
            player.sendMessage(Component.text(plugin.getMessagePrefix() + "That cosmetic is still locked."));
            return;
        }

        if (type.equals("pattern")) {
            data.setSelectedTrimPattern(id);
        } else {
            data.setSelectedTrimMaterial(id);
        }
        plugin.getDataStore().save(data);
        plugin.getCosmeticsManager().applySelectedTrim(player, data);

        player.sendMessage(Component.text(plugin.getMessagePrefix() + "Selected " + type + ": " + id));
    }

    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataStore().get(player.getUniqueId());
        // Re-apply on next tick so the newly equipped item is settled first.
        plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getCosmeticsManager().applySelectedTrim(player, data));
    }
}
