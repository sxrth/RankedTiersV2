package com.rankedtiers.kit;

import com.rankedtiers.match.Arena;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads and stores the set of ranked kits defined in config.yml.
 */
public class KitManager {

    private final JavaPlugin plugin;
    private final Map<String, Kit> kits = new LinkedHashMap<>();

    public KitManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        kits.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("kits");
        if (section == null) {
            plugin.getLogger().warning("No 'kits' section found in config.yml");
            return;
        }

        for (String kitName : section.getKeys(false)) {
            ConfigurationSection kitSection = section.getConfigurationSection(kitName);
            if (kitSection == null) continue;

            Material icon = Material.matchMaterial(kitSection.getString("icon", "STONE"));
            if (icon == null) icon = Material.STONE;

            boolean allowPlace = kitSection.getBoolean("allow-block-place", false);
            boolean allowBreak = kitSection.getBoolean("allow-block-break", false);

            Arena arena = Arena.fromConfigStrings(
                    kitSection.getString("position1"),
                    kitSection.getString("position2")
            );

            kits.put(kitName, new Kit(kitName, icon, allowPlace, allowBreak, arena));
        }

        plugin.getLogger().info("Loaded " + kits.size() + " ranked kits.");
    }

    public Optional<Kit> get(String name) {
        return Optional.ofNullable(kits.get(name));
    }

    public Map<String, Kit> getAll() {
        return kits;
    }
}
