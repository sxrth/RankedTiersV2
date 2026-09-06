package com.rankedtiers.lobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The MCPVP-style lobby loadout: fixed hotbar items the player uses to open
 * the queue/party/cosmetics menus instead of typing commands. Each item is
 * tagged with a PersistentDataContainer key so listeners can recognise it
 * (and stop it from being dropped or moved) regardless of its display name.
 */
public class LobbyItemsManager {

    public static final NamespacedKey ACTION_KEY;

    static {
        JavaPlugin owner = JavaPlugin.getProvidingPlugin(LobbyItemsManager.class);
        ACTION_KEY = new NamespacedKey(owner, "lobby-action");
    }

    private final JavaPlugin plugin;
    private final Map<Integer, ItemStack> items = new LinkedHashMap<>();
    private boolean enabled;

    public LobbyItemsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        items.clear();
        enabled = plugin.getConfig().getBoolean("lobby-items.enabled", true);
        if (!enabled) return;

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("lobby-items");
        if (section == null) return;

        for (String action : new String[]{"queue", "party", "cosmetics"}) {
            ConfigurationSection itemSection = section.getConfigurationSection(action);
            if (itemSection == null) continue;

            int slot = itemSection.getInt("slot", 0);
            Material material = Material.matchMaterial(itemSection.getString("material", "STONE"));
            if (material == null) material = Material.STONE;
            String name = itemSection.getString("name", action);

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
                meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, action);
                item.setItemMeta(meta);
            }

            items.put(slot, item);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Places all lobby items into the player's hotbar, overwriting whatever is there. */
    public void give(Player player) {
        if (!enabled) return;
        items.forEach((slot, item) -> player.getInventory().setItem(slot, item.clone()));
    }

    /** Removes lobby items from the hotbar slots they occupy - used when a match starts. */
    public void clear(Player player) {
        if (!enabled) return;
        items.keySet().forEach(slot -> player.getInventory().setItem(slot, null));
    }

    public boolean isLobbyItem(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return false;
        return stack.getItemMeta().getPersistentDataContainer().has(ACTION_KEY, PersistentDataType.STRING);
    }

    public String actionOf(ItemStack stack) {
        if (!isLobbyItem(stack)) return null;
        return stack.getItemMeta().getPersistentDataContainer().get(ACTION_KEY, PersistentDataType.STRING);
    }
}
