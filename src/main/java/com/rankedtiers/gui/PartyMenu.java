package com.rankedtiers.gui;

import com.rankedtiers.party.Party;
import com.rankedtiers.party.PartyManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Slot 0: create/leave party toggle. Slots 1..8: online players (heads) to invite.
 */
public class PartyMenu {

    public static final String TITLE = "RankedTiers - Party";
    public static final NamespacedKey ACTION_KEY;
    public static final NamespacedKey TARGET_KEY;

    static {
        JavaPlugin owner = JavaPlugin.getProvidingPlugin(PartyMenu.class);
        ACTION_KEY = new NamespacedKey(owner, "party-action");
        TARGET_KEY = new NamespacedKey(owner, "party-target");
    }

    public Inventory build(PartyManager partyManager, Player viewer) {
        Inventory inventory = Bukkit.createInventory(null, 27, Component.text(TITLE));

        Party party = partyManager.getParty(viewer.getUniqueId());
        boolean inParty = party != null;

        ItemStack toggle = new ItemStack(inParty ? Material.BARRIER : Material.EMERALD_BLOCK);
        ItemMeta toggleMeta = toggle.getItemMeta();
        if (toggleMeta != null) {
            toggleMeta.displayName(Component.text(inParty ? "Leave Party" : "Create Party"));
            toggleMeta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING,
                    inParty ? "leave" : "create");
            toggle.setItemMeta(toggleMeta);
        }
        inventory.setItem(0, toggle);

        int slot = 1;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(viewer.getUniqueId())) continue;
            if (slot > 8) break;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            if (head.getItemMeta() instanceof SkullMeta skullMeta) {
                skullMeta.setOwningPlayer(online);
                skullMeta.displayName(Component.text("Invite " + online.getName()));
                skullMeta.setLore(List.of("§7Click to send a party invite"));
                skullMeta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, "invite");
                skullMeta.getPersistentDataContainer().set(TARGET_KEY, PersistentDataType.STRING, online.getUniqueId().toString());
                head.setItemMeta(skullMeta);
            }
            inventory.setItem(slot, head);
            slot++;
        }

        return inventory;
    }
}
