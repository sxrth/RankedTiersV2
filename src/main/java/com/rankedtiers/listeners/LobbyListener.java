package com.rankedtiers.listeners;

import com.rankedtiers.RankedTiers;
import com.rankedtiers.gui.CosmeticsMenu;
import com.rankedtiers.gui.PartyMenu;
import com.rankedtiers.gui.QueueMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Wires up the MCPVP-style clickable hotbar items: hands them out, opens the
 * matching menu on right-click, stops them being dropped/shuffled, and keeps
 * them in sync with match state (removed during a duel, restored after - see
 * MatchManager, which calls LobbyItemsManager directly on start/end).
 */
public class LobbyListener implements Listener {

    private final RankedTiers plugin;

    public LobbyListener(RankedTiers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Run a tick later so this doesn't fight with other join-time inventory logic.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getMatchManager().isInMatch(player.getUniqueId())) {
                    plugin.getLobbyItemsManager().give(player);
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getMatchManager().isInMatch(player.getUniqueId())) {
                    plugin.getLobbyItemsManager().give(player);
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onDropLobbyItem(PlayerDropItemEvent event) {
        if (plugin.getLobbyItemsManager().isLobbyItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    /** Stops lobby items being shuffled around or thrown out of the player's own inventory. */
    @EventHandler
    public void onInventoryClickProtectLobbyItems(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // Menus we already fully cancel elsewhere; this handler only guards
        // the player's personal inventory / hotbar against lobby items being moved.
        Component title = event.getView().title();
        if (title.equals(Component.text(QueueMenu.TITLE))
                || title.equals(Component.text(CosmeticsMenu.TITLE))
                || title.equals(Component.text(PartyMenu.TITLE))) {
            return;
        }

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (plugin.getLobbyItemsManager().isLobbyItem(current) || plugin.getLobbyItemsManager().isLobbyItem(cursor)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        String action = plugin.getLobbyItemsManager().actionOf(item);
        if (action == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        switch (action) {
            case "queue" -> {
                QueueMenu menu = new QueueMenu();
                plugin.registerOpenQueueMenu(player, menu);
                player.openInventory(menu.build(plugin.getKitManager(), player));
            }
            case "party" -> player.openInventory(new PartyMenu().build(plugin.getPartyManager(), player));
            case "cosmetics" -> {
                var data = plugin.getDataStore().get(player.getUniqueId());
                player.openInventory(new CosmeticsMenu().build(plugin.getCosmeticsManager(), data));
            }
            default -> { /* unknown action, ignore */ }
        }
    }

    @EventHandler
    public void onPartyMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().title().equals(Component.text(PartyMenu.TITLE))) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        String action = meta.getPersistentDataContainer().get(PartyMenu.ACTION_KEY, PersistentDataType.STRING);
        if (action == null) return;

        switch (action) {
            case "create" -> {
                plugin.getPartyManager().createParty(player);
                player.sendMessage(Component.text(plugin.getMessagePrefix() + "Party created."));
                player.closeInventory();
            }
            case "leave" -> {
                plugin.getPartyManager().leaveParty(player);
                player.closeInventory();
            }
            case "invite" -> {
                String targetIdRaw = meta.getPersistentDataContainer().get(PartyMenu.TARGET_KEY, PersistentDataType.STRING);
                if (targetIdRaw == null) return;
                Player target = plugin.getServer().getPlayer(UUID.fromString(targetIdRaw));
                if (target == null || !target.isOnline()) {
                    player.sendMessage(Component.text(plugin.getMessagePrefix() + "That player left."));
                    return;
                }
                plugin.getPartyManager().invite(player, target);
                player.closeInventory();
            }
            default -> { /* ignore */ }
        }
    }
}
