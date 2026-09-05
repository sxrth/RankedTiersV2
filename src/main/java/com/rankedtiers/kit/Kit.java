package com.rankedtiers.kit;

import com.rankedtiers.match.Arena;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * A ranked kit: its display icon, arena bounds and block-interaction rules.
 */
public class Kit {

    private final String name;
    private final Material icon;
    private final boolean allowBlockPlace;
    private final boolean allowBlockBreak;
    private final Arena arena;

    public Kit(String name, Material icon, boolean allowBlockPlace, boolean allowBlockBreak, Arena arena) {
        this.name = name;
        this.icon = icon;
        this.allowBlockPlace = allowBlockPlace;
        this.allowBlockBreak = allowBlockBreak;
        this.arena = arena;
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public boolean allowsBlockPlace() {
        return allowBlockPlace;
    }

    public boolean allowsBlockBreak() {
        return allowBlockBreak;
    }

    public Arena getArena() {
        return arena;
    }

    public ItemStack toMenuItem() {
        return new ItemStack(icon);
    }
}
