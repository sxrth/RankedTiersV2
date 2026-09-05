package com.rankedtiers;

import com.rankedtiers.commands.AdminCommand;
import com.rankedtiers.commands.CosmeticsCommand;
import com.rankedtiers.commands.PartyCommand;
import com.rankedtiers.commands.PlayCommand;
import com.rankedtiers.cosmetics.CosmeticsManager;
import com.rankedtiers.data.DataStore;
import com.rankedtiers.gui.QueueMenu;
import com.rankedtiers.kit.KitManager;
import com.rankedtiers.listeners.CombatListener;
import com.rankedtiers.listeners.CosmeticsListener;
import com.rankedtiers.listeners.PlayerListener;
import com.rankedtiers.match.MatchManager;
import com.rankedtiers.party.PartyManager;
import com.rankedtiers.rating.RatingService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RankedTiers - MCPVP-style rating based ranked PvP: tiers, kits, arenas,
 * parties and matchmaking queues.
 */
public final class RankedTiers extends JavaPlugin {

    private static RankedTiers instance;

    private DataStore dataStore;
    private KitManager kitManager;
    private RatingService ratingService;
    private MatchManager matchManager;
    private PartyManager partyManager;
    private CosmeticsManager cosmeticsManager;

    private final Map<UUID, QueueMenu> openQueueMenus = new HashMap<>();
    private String messagePrefix;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.messagePrefix = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("messages.prefix", "&8[&bRankedTiers&8] &r"));

        this.dataStore = new DataStore(this);
        this.kitManager = new KitManager(this);
        this.ratingService = new RatingService(this);
        this.matchManager = new MatchManager(dataStore, ratingService, messagePrefix);
        this.partyManager = new PartyManager(messagePrefix);
        this.cosmeticsManager = new CosmeticsManager(this, ratingService);

        getCommand("play").setExecutor(new PlayCommand(this));
        getCommand("rtadmin").setExecutor(new AdminCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));
        getCommand("cosmetics").setExecutor(new CosmeticsCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new CosmeticsListener(this), this);

        getLogger().info("RankedTiers enabled - " + kitManager.getAll().size() + " kits loaded.");
    }

    @Override
    public void onDisable() {
        if (dataStore != null) {
            dataStore.saveAll();
        }
        getLogger().info("RankedTiers disabled.");
    }

    public static RankedTiers getPluginInstance() {
        return instance;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public RatingService getRatingService() {
        return ratingService;
    }

    public MatchManager getMatchManager() {
        return matchManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public CosmeticsManager getCosmeticsManager() {
        return cosmeticsManager;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public void registerOpenQueueMenu(Player player, QueueMenu menu) {
        openQueueMenus.put(player.getUniqueId(), menu);
    }

    public void unregisterOpenQueueMenu(Player player) {
        openQueueMenus.remove(player.getUniqueId());
    }

    public QueueMenu getOpenQueueMenu(Player player) {
        return openQueueMenus.get(player.getUniqueId());
    }
}
