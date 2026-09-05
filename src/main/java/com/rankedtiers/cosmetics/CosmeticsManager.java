package com.rankedtiers.cosmetics;

import com.rankedtiers.data.PlayerData;
import com.rankedtiers.rating.RatingService;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tier-gated cosmetic armor trims, MCPVP-style: reaching a higher rank
 * unlocks more trim patterns/materials, purely cosmetic (no gameplay effect).
 * Fully driven by the `cosmetics` section of config.yml.
 */
public class CosmeticsManager {

    private final JavaPlugin plugin;
    private final RatingService ratingService;
    private final List<CosmeticOption> patterns = new ArrayList<>();
    private final List<CosmeticOption> materials = new ArrayList<>();
    private boolean enabled;

    public CosmeticsManager(JavaPlugin plugin, RatingService ratingService) {
        this.plugin = plugin;
        this.ratingService = ratingService;
        reload();
    }

    public void reload() {
        patterns.clear();
        materials.clear();
        enabled = plugin.getConfig().getBoolean("cosmetics.enabled", true);

        loadInto(plugin.getConfig().getConfigurationSection("cosmetics.patterns"), patterns);
        loadInto(plugin.getConfig().getConfigurationSection("cosmetics.materials"), materials);
    }

    private void loadInto(ConfigurationSection section, List<CosmeticOption> target) {
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            target.add(new CosmeticOption(key, section.getString(key)));
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<CosmeticOption> getPatterns() {
        return patterns;
    }

    public List<CosmeticOption> getMaterials() {
        return materials;
    }

    public boolean isUnlocked(PlayerData data, CosmeticOption option) {
        return ratingService.meetsTier(data.getTier(), option.requiredTier());
    }

    private TrimPattern resolvePattern(String id) {
        NamespacedKey key = NamespacedKey.minecraft(id.toLowerCase(Locale.ROOT));
        return Registry.TRIM_PATTERN.get(key);
    }

    private TrimMaterial resolveMaterial(String id) {
        NamespacedKey key = NamespacedKey.minecraft(id.toLowerCase(Locale.ROOT));
        return Registry.TRIM_MATERIAL.get(key);
    }

    /**
     * Applies the player's currently selected trim (if any, and if unlocked)
     * to every armor piece they're wearing right now.
     */
    public void applySelectedTrim(Player player, PlayerData data) {
        if (!enabled) return;

        String patternId = data.getSelectedTrimPattern();
        String materialId = data.getSelectedTrimMaterial();
        if (patternId == null || materialId == null) return;

        boolean patternUnlocked = patterns.stream()
                .filter(o -> o.id().equalsIgnoreCase(patternId))
                .findFirst().map(o -> isUnlocked(data, o)).orElse(false);
        boolean materialUnlocked = materials.stream()
                .filter(o -> o.id().equalsIgnoreCase(materialId))
                .findFirst().map(o -> isUnlocked(data, o)).orElse(false);

        if (!patternUnlocked || !materialUnlocked) return;

        TrimPattern pattern = resolvePattern(patternId);
        TrimMaterial material = resolveMaterial(materialId);
        if (pattern == null || material == null) return;

        ArmorTrim trim = new ArmorTrim(material, pattern);
        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) return;

        for (ItemStack piece : new ItemStack[]{
                equipment.getHelmet(), equipment.getChestplate(),
                equipment.getLeggings(), equipment.getBoots()}) {
            if (piece == null) continue;
            if (piece.getItemMeta() instanceof ArmorMeta armorMeta) {
                armorMeta.setTrim(trim);
                piece.setItemMeta(armorMeta);
            }
        }
    }

    public void select(PlayerData data, String patternId, String materialId) {
        data.setSelectedTrimPattern(patternId);
        data.setSelectedTrimMaterial(materialId);
    }
}
