package com.rankedtiers.rating;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts a player's rating into a tier label (LT5 .. HT1) and applies
 * win/loss rating changes according to config.yml.
 */
public class RatingService {

    private final JavaPlugin plugin;
    private final Map<String, Integer> tierThresholds = new LinkedHashMap<>();

    public RatingService(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        tierThresholds.clear();
        var section = plugin.getConfig().getConfigurationSection("tiers");
        if (section == null) {
            plugin.getLogger().warning("No 'tiers' section found in config.yml");
            return;
        }
        for (String tier : section.getKeys(false)) {
            tierThresholds.put(tier, section.getInt(tier));
        }
    }

    public int startingRating() {
        return plugin.getConfig().getInt("rating.starting", 1000);
    }

    public int minimumRating() {
        return plugin.getConfig().getInt("rating.minimum", 0);
    }

    public int ratingForWin() {
        return plugin.getConfig().getInt("rating.win-gain", 25);
    }

    public int ratingForLoss() {
        return plugin.getConfig().getInt("rating.loss-loss", 20);
    }

    /** Highest tier whose threshold is <= the given rating. */
    public String tierFor(int rating) {
        String best = "LT5";
        int bestThreshold = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> entry : tierThresholds.entrySet()) {
            if (rating >= entry.getValue() && entry.getValue() >= bestThreshold) {
                best = entry.getKey();
                bestThreshold = entry.getValue();
            }
        }
        return best;
    }

    public int applyWin(int currentRating) {
        return currentRating + ratingForWin();
    }

    public int applyLoss(int currentRating) {
        return Math.max(minimumRating(), currentRating - ratingForLoss());
    }

    /** Position of a tier in the ladder, ordered by its rating threshold (0 = lowest tier). Case-insensitive. */
    public int tierIndex(String tier) {
        if (tier == null) return -1;
        java.util.List<String> ordered = tierThresholds.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).equalsIgnoreCase(tier)) return i;
        }
        return -1;
    }

    /** Returns the tier name exactly as configured (correct case), or null if unknown. */
    public String canonicalTierName(String tier) {
        if (tier == null) return null;
        for (String known : tierThresholds.keySet()) {
            if (known.equalsIgnoreCase(tier)) return known;
        }
        return null;
    }

    /** True if `currentTier` is at or above `requiredTier` in the ladder. */
    public boolean meetsTier(String currentTier, String requiredTier) {
        if (requiredTier == null || requiredTier.isBlank()) return true;
        return tierIndex(currentTier) >= tierIndex(requiredTier);
    }
}
