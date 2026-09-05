package com.rankedtiers.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Loads and saves {@link PlayerData} to disk as one YAML file per player
 * under plugins/RankedTiers/playerdata/&lt;uuid&gt;.yml.
 */
public class DataStore {

    private final JavaPlugin plugin;
    private final File folder;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public DataStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "playerdata");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Could not create playerdata folder.");
        }
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::load);
    }

    private PlayerData load(UUID uuid) {
        File file = new File(folder, uuid.toString() + ".yml");
        int startingRating = plugin.getConfig().getInt("rating.starting", 1000);

        if (!file.exists()) {
            return new PlayerData(uuid, startingRating, 0, 0, "LT5", null, null);
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        int rating = yaml.getInt("rating", startingRating);
        int wins = yaml.getInt("wins", 0);
        int losses = yaml.getInt("losses", 0);
        String tier = yaml.getString("tier", "LT5");
        String trimPattern = yaml.getString("cosmetics.trim-pattern", null);
        String trimMaterial = yaml.getString("cosmetics.trim-material", null);
        return new PlayerData(uuid, rating, wins, losses, tier, trimPattern, trimMaterial);
    }

    public void save(PlayerData data) {
        File file = new File(folder, data.getUuid().toString() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("rating", data.getRating());
        yaml.set("wins", data.getWins());
        yaml.set("losses", data.getLosses());
        yaml.set("tier", data.getTier());
        yaml.set("cosmetics.trim-pattern", data.getSelectedTrimPattern());
        yaml.set("cosmetics.trim-material", data.getSelectedTrimMaterial());
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save data for " + data.getUuid(), e);
        }
    }

    public void saveAll() {
        cache.values().forEach(this::save);
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            save(data);
        }
    }
}
