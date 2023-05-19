package com.github.bfabri.hosts;

import com.github.bfabri.hosts.commands.utils.CommandsModule;
import com.github.bfabri.hosts.commands.utils.framework.SimpleCommandManager;
import com.github.bfabri.hosts.game.GameManager;
import com.github.bfabri.hosts.game.TeamManager;
import com.github.bfabri.hosts.listeners.*;
import com.github.bfabri.hosts.managers.ArenaManager;
import com.github.bfabri.hosts.managers.InventoryManager;
import com.github.bfabri.hosts.managers.RewardsManager;
import com.github.bfabri.hosts.managers.SpectatorManager;
import com.github.bfabri.hosts.utils.PlayerUtil;
import com.github.bfabri.hosts.utils.UpdateChecker;
import com.github.bfabri.hosts.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Hosts extends JavaPlugin {

	@Getter
	private static Hosts instance;

	@Getter
	private InventoryManager inventoryManager;

	@Getter
	private ArenaManager arenaManager;

	@Getter
	private TeamManager teamManager;

	@Getter
	private SpectatorManager spectatorManager;

	@Getter
	private RewardsManager rewardsManager;

	@Getter
	public GameManager gameManager;

	@Getter
	private PlayerUtil playerUtil;

	@Getter
	private RewardsListener rewardsListener;

	@Override
	public void onEnable() {
		instance = this;
		new ConfigHandler(this);

		new UpdateChecker().checkForUpdate();

		registerManagers();
		registerListeners(Bukkit.getPluginManager());
		registerCommands();

		Bukkit.getConsoleSender().sendMessage(Utils.PREFIX + Utils.translate("&bDetected Server Version&7: &f" + Bukkit.getServer().getBukkitVersion().split("-")[0]));
	}

    @Override
    public void onDisable() {
	    this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
	    this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
	    if (getGameManager().isGameAvailable()) {
		    getGameManager().getGame().onStop();
	    }

	    instance = null;
    }

    private void registerListeners(PluginManager pluginManager) {
        pluginManager.registerEvents(new InventoryListener(), this);
        pluginManager.registerEvents(new SpectatorListener(), this);
        pluginManager.registerEvents(new ArenaListener(), this);
	    pluginManager.registerEvents(new GameListener(), this);
	    pluginManager.registerEvents(rewardsListener = new RewardsListener(), this);
    }

    private void registerManagers() {
	    this.rewardsManager = new RewardsManager();
	    this.inventoryManager = new InventoryManager();
	    this.arenaManager = new ArenaManager();
	    this.spectatorManager = new SpectatorManager();
	    this.gameManager = new GameManager();
	    this.playerUtil = new PlayerUtil();
	    this.teamManager = new TeamManager();
    }

	private void registerCommands() {
		new SimpleCommandManager(this).registerAll(new CommandsModule());
	}
}
