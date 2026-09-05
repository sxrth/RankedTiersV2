package com.rankedtiers.listeners;

import com.rankedtiers.RankedTiers;
import com.rankedtiers.gui.QueueMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerListener implements Listener {

    private final RankedTiers plugin;

    public PlayerListener(RankedTiers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Warms the data cache so the player's tier is ready immediately.
        plugin.getDataStore().get(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getMatchManager().handleDisconnect(player);
        plugin.getDataStore().unload(player.getUniqueId());
        plugin.unregisterOpenQueueMenu(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        QueueMenu menu = plugin.getOpenQueueMenu(player);
        if (menu == null) return;
        if (!event.getView().title().equals(Component.text(QueueMenu.TITLE))) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        String kitName = meta.getPersistentDataContainer().get(QueueMenu.KIT_KEY, PersistentDataType.STRING);
        if (kitName == null) return;

        plugin.getKitManager().get(kitName).ifPresent(kit -> {
            player.closeInventory();
            plugin.getMatchManager().joinQueue(player, kit);
        });
    }
}
