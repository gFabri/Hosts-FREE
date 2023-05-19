package com.github.bfabri.hosts.managers;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.game.Game;
import com.github.bfabri.hosts.game.GamePlayer;
import com.github.bfabri.hosts.utils.CustomItem;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class SpectatorManager {

	@Getter
	private ArrayList<GamePlayer> spectatorPlayers = new ArrayList<>();

	public void joinSpectator(Game game, GamePlayer gamePlayer) {
		spectatorPlayers.add(gamePlayer);
		gamePlayer.getPlayer().setHealth(20);
		gamePlayer.getPlayer().setAllowFlight(true);
		gamePlayer.getPlayer().setFlying(true);
		game.getGamePlayers().forEach(gamePlayers -> gamePlayers.getPlayer().hidePlayer(gamePlayer.getPlayer()));
		gamePlayer.getPlayer().teleport(Location.deserialize(game.getArena().getSpectatorLocation()));
		gamePlayer.getPlayer().getInventory().setItem(8, new CustomItem(Material.getMaterial(
				ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.spectator.leave.material")), 1,
				ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.spectator.leave.material-data")).
				setName(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.spectator.leave.displayName")).
				addLore(ConfigHandler.Configs.ITEMS.getConfig().getStringList("HOST.spectator.leave.description")).create());

		gamePlayer.getPlayer().getInventory().setItem(0, new CustomItem(Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.spectator.teleport.material")), 1, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.spectator.teleport.material-data")).setName(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.spectator.teleport.displayName")).addLore(ConfigHandler.Configs.ITEMS.getConfig().getStringList("HOST.spectator.teleport.description")).create());
	}

	public void leaveSpectator(Game game, GamePlayer gamePlayer) {
		Player player = gamePlayer.getPlayer();
		Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(player));
		player.getInventory().clear();
		player.setGameMode(GameMode.SURVIVAL);
		player.setHealth(20);
		player.setFlying(false);
		player.setAllowFlight(false);
		boolean teleportToSpawnOnEnd = ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END");
		Location teleportLocation = teleportToSpawnOnEnd ? Location.deserialize(game.getArena().getServerSpawn()) : gamePlayer.getLocation();
		player.teleport(teleportLocation);
		game.loadInv(gamePlayer);
		spectatorPlayers.remove(gamePlayer);
	}

	public void onFinish(Game game) {
		boolean teleportToSpawnOnEnd = ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END");

		for (GamePlayer spectatorPlayer : spectatorPlayers) {
			Location teleportLocation = teleportToSpawnOnEnd ? Location.deserialize(game.getArena().getServerSpawn()) : spectatorPlayer.getLocation();

			spectatorPlayer.getPlayer().setFlying(false);
			spectatorPlayer.getPlayer().setAllowFlight(false);
			spectatorPlayer.getPlayer().setHealth(20);
			spectatorPlayer.getPlayer().getInventory().clear();

			for (Player player : Bukkit.getOnlinePlayers()) {
				player.showPlayer(spectatorPlayer.getPlayer());
			}

			spectatorPlayer.getPlayer().teleport(teleportLocation);
		}

		spectatorPlayers.clear();
	}


	public boolean isInSpectatorMode(GamePlayer gamePlayer) {
		return spectatorPlayers.contains(gamePlayer);
	}
}
