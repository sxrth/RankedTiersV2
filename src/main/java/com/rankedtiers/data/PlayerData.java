package com.rankedtiers.data;

import java.util.UUID;

/**
 * Persistent per-player ranked statistics.
 */
public class PlayerData {

    private final UUID uuid;
    private int rating;
    private int wins;
    private int losses;
    private String tier;
    private String selectedTrimPattern; // e.g. "sentry", or null = no trim
    private String selectedTrimMaterial; // e.g. "iron", or null = no trim

    public PlayerData(UUID uuid, int rating, int wins, int losses, String tier,
                       String selectedTrimPattern, String selectedTrimMaterial) {
        this.uuid = uuid;
        this.rating = rating;
        this.wins = wins;
        this.losses = losses;
        this.tier = tier;
        this.selectedTrimPattern = selectedTrimPattern;
        this.selectedTrimMaterial = selectedTrimMaterial;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        this.wins++;
    }

    public int getLosses() {
        return losses;
    }

    public void addLoss() {
        this.losses++;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public double winRate() {
        int total = wins + losses;
        return total == 0 ? 0.0 : (wins * 100.0) / total;
    }

    public String getSelectedTrimPattern() {
        return selectedTrimPattern;
    }

    public void setSelectedTrimPattern(String selectedTrimPattern) {
        this.selectedTrimPattern = selectedTrimPattern;
    }

    public String getSelectedTrimMaterial() {
        return selectedTrimMaterial;
    }

    public void setSelectedTrimMaterial(String selectedTrimMaterial) {
        this.selectedTrimMaterial = selectedTrimMaterial;
    }
}
