package com.github.bfabri.hosts.listeners;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.events.HostJoinEvent;
import com.github.bfabri.hosts.events.HostLeaveEvent;
import com.github.bfabri.hosts.game.Game;
import com.github.bfabri.hosts.game.GamePlayer;
import com.github.bfabri.hosts.game.games.Sumo;
import com.github.bfabri.hosts.utils.PlayerUtil;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class GameListener implements Listener {

    @EventHandler
    private void onHostJoin(HostJoinEvent event) {

        Player player = event.getPlayer();
        Game game = event.getGame();

        GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(player);

        if (game.getCurrentStatus() == Game.Status.STARTED) {
            player.sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-STARTED")));
            return;
        }

        if (!ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.ALLOW-ITEMS")) {
            if (!PlayerUtil.isEmpty(player.getInventory(), true)) {
                player.sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("EMPTY-INVENTORY")));
                return;
            }
        }

        if (game.getGamePlayers().contains(gamePlayer)) {
            player.sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("PLAYER-ALREADY-JOINED")));
            return;
        }

        if (game.getGamePlayers().size() >= game.getMaxPlayers() && !event.getPlayer().hasPermission("hosts.join.bypass")) {
            player.sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-FULL")));
            return;
        }

        game.getGamePlayers().add((gamePlayer == null ? new GamePlayer(player, game) : gamePlayer));

        if (ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TYPE").equalsIgnoreCase("GLOBAL")) {
            Bukkit.broadcastMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-JOINED").replace("{game}", game.getDisplayName()).replace("{player}", player.getDisplayName())
                    .replace("{players}", String.valueOf(game.getGamePlayers().size())).replace("{max-players}", String.valueOf(event.getGame().getMaxPlayers()))));
        } else {
            game.getGamePlayers().forEach(gamePlayers -> gamePlayers.getPlayer().sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-JOINED").replace("{game}", game.getDisplayName()).replace("{player}", player.getDisplayName())
                    .replace("{players}", String.valueOf(game.getGamePlayers().size())).replace("{max-players}", String.valueOf(event.getGame().getMaxPlayers())))));
        }

        ConfigHandler.Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.COMMANDS-ON-JOIN").forEach(commands -> {
            if (ConfigHandler.Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.COMMANDS-ON-JOIN").size() >= 1) {
                player.getServer().dispatchCommand(player, commands.replace("{player}", player.getName()));
            }
        });

        player.getActivePotionEffects().forEach(potionEffect -> event.getPlayer().removePotionEffect(potionEffect.getType()));

        player.setFallDistance(0);
        player.setHealth(20);
        player.setFoodLevel(20);

        player.setFlying(false);
        player.setAllowFlight(false);

        player.getInventory().setArmorContents(null);
        player.getInventory().clear();

        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(Location.deserialize(game.getArena().getPreSpawn()));
    }

    @EventHandler
    private void onHostLeave(HostLeaveEvent event) {
        Player player = event.getPlayer();
        Game game = event.getGame();
        HostLeaveEvent.Types type = event.getType();

        GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(player);

        if (gamePlayer == null) {
            player.sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("PLAYER-NOT-JOINED")));
            return;
        }

        if (!event.getGame().getGamePlayers().contains(gamePlayer)) {
            player.sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("PLAYER-NOT-JOINED")));
            return;
        }

        if (type == HostLeaveEvent.Types.LEAVED) {
            player.getActivePotionEffects().forEach(types -> player.removePotionEffect(types.getType()));
            player.getInventory().setArmorContents(gamePlayer.getArmor());
            player.getInventory().setContents(gamePlayer.getInventory());
            player.updateInventory();

            game.getGamePlayers().remove(gamePlayer);

            if (ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TYPE").equalsIgnoreCase("GLOBAL")) {
                Bukkit.broadcastMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-LEAVED").replace("{game}", game.getDisplayName()).replace("{player}", player.getDisplayName())
                        .replace("{players}", String.valueOf(game.getGamePlayers().size())).replace("{max-players}", String.valueOf(game.getMaxPlayers()))));
            } else {
                game.getGamePlayers().forEach(gamePlayers -> gamePlayers.getPlayer().sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-LEAVED").replace("{game}", game.getDisplayName()).replace("{player}", player.getDisplayName())
                        .replace("{players}", String.valueOf(game.getGamePlayers().size())).replace("{max-players}", String.valueOf(game.getMaxPlayers())))));
            }

            if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                gamePlayer.getPlayer().teleport(Location.deserialize(event.getGame().getArena().getServerSpawn()));
            } else {
                gamePlayer.getPlayer().teleport(gamePlayer.getLocation());
            }
        } else if (type == HostLeaveEvent.Types.ELIMINATION) {
            game.getGamePlayers().remove(gamePlayer);

            game.getGamePlayers().forEach(gamePlayers -> gamePlayers.getPlayer().sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ELIMINATED").replace("{player}", gamePlayer.getPlayer().getDisplayName()).replace("{game}", game.getDisplayName()).
                    replace("{players}", String.valueOf(game.getGamePlayers().size())).replace("{max-players}", String.valueOf(game.getMaxPlayers())))));

            gamePlayer.getPlayer().setFallDistance(0);
            gamePlayer.getPlayer().setHealth(20);
            gamePlayer.getPlayer().getActivePotionEffects().forEach(types -> gamePlayer.getPlayer().removePotionEffect(types.getType()));
                game.loadInv(gamePlayer);
                if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                    gamePlayer.getPlayer().teleport(Location.deserialize(game.getArena().getServerSpawn()));
                } else {
                    gamePlayer.getPlayer().teleport(gamePlayer.getLocation());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Hosts.getInstance().getGameManager().isGameAvailable()) {

            Game game = Hosts.getInstance().getGameManager().getGame();

            GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

            if (gamePlayer == null) {
                return;
            }

            if (game.getCurrentStatus() == Game.Status.STARTING) {
                Bukkit.getPluginManager().callEvent(new HostLeaveEvent(event.getPlayer(), game, HostLeaveEvent.Types.LEAVED));
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (Hosts.getInstance().getGameManager().isGameAvailable()) {

            GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer((Player) event.getEntity());

            if (gamePlayer == null) {
                return;
            }

            if (Hosts.getInstance().getGameManager().getGame().getGamePlayers().contains(gamePlayer) || Hosts.getInstance().getSpectatorManager().getSpectatorPlayers().contains(gamePlayer)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCommands(PlayerCommandPreprocessEvent event) {
        if (Hosts.getInstance().getGameManager().isGameAvailable()) {

            GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

            if (gamePlayer == null) {
                return;
            }

            if (Hosts.getInstance().getGameManager().getGame().getGamePlayers().contains(gamePlayer) || Hosts.getInstance().getSpectatorManager().getSpectatorPlayers().contains(gamePlayer)) {

                if (event.getPlayer().hasPermission("host.bypass.commands")) {
                    return;
                }

                Hosts.getInstance().getConfig().getStringList("HOST.GENERAL.ALLOWED-COMMANDS").forEach(commands -> {
                    if (!event.getMessage().startsWith("/" + commands)) {
                        event.setCancelled(true);
                        gamePlayer.getPlayer().sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-DISABLED-COMMAND")));
                    }
                });
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (Hosts.getInstance().getGameManager().isGameAvailable()) {
            if (Hosts.getInstance().getGameManager().getGame().getCurrentStatus() == Game.Status.STARTING) {
                if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {

                    GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(((Player) event.getEntity()));

                    GamePlayer gameDamager = Hosts.getInstance().getGameManager().getPlayer(((Player) event.getDamager()));

                    if (gamePlayer == null || gameDamager == null) {
                        return;
                    }

                    if (Hosts.getInstance().getGameManager().getGame().getGamePlayers().contains(gamePlayer)
                            || Hosts.getInstance().getGameManager().getGame().getGamePlayers().contains(gameDamager) ||
                    Hosts.getInstance().getSpectatorManager().getSpectatorPlayers().contains(gamePlayer) || Hosts.getInstance().getSpectatorManager().getSpectatorPlayers().contains(gameDamager)) {
                        event.setCancelled(true);
                        gameDamager.getPlayer().sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-NO-PVP")));
                    }
                }
            } else if (Hosts.getInstance().getGameManager().getGame().getCurrentStatus() == Game.Status.STARTED) {
                if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {

                    GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(((Player) event.getEntity()));

                    GamePlayer gameDamager = Hosts.getInstance().getGameManager().getPlayer(((Player) event.getDamager()));

                    if (gamePlayer == null || gameDamager == null) {
                        return;
                    }

                    if (Hosts.getInstance().getSpectatorManager().getSpectatorPlayers().contains(gamePlayer) || Hosts.getInstance().getSpectatorManager().getSpectatorPlayers().contains(gameDamager)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (Hosts.getInstance().getGameManager().isGameAvailable()) {
            if (Hosts.getInstance().getGameManager().getGame().getCurrentStatus() == Game.Status.STARTING || Hosts.getInstance().getGameManager().getGame().getCurrentStatus() == Game.Status.STARTED) {
                if (event.getEntity() instanceof Player) {

                    GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(((Player) event.getEntity()));

                    if (gamePlayer == null) {
                        return;
                    }
                    if (Hosts.getInstance().getGameManager().getGame().getGamePlayers().contains(gamePlayer)) {
                        if (event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK || event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                            event.setCancelled(true);
                        }
                    } else if (Hosts.getInstance().getSpectatorManager().getSpectatorPlayers().contains(gamePlayer)) {
                        if (event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK || event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBreakBlock(BlockBreakEvent event) {
        if (Hosts.getInstance().getGameManager().isGameAvailable()) {
            GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

            if (gamePlayer == null) {
                return;
            }

            if (Hosts.getInstance().getGameManager().getGame().getGamePlayers().contains(gamePlayer) || Hosts.getInstance().getSpectatorManager().getSpectatorPlayers().contains(gamePlayer)) {
                if (!event.getBlock().hasMetadata("blockBreakable")) {
                    event.setCancelled(true);
                    gamePlayer.getPlayer().sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-NO-BREAK")));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (Hosts.getInstance().getGameManager().isGameAvailable()) {

            GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

            if (gamePlayer == null) {
                return;
            }

            if (Hosts.getInstance().getGameManager().getGame().getGamePlayers().contains(gamePlayer) || Hosts.getInstance().getSpectatorManager().getSpectatorPlayers().contains(gamePlayer)) {
                if (!event.getBlock().hasMetadata("blockBreakable")) {
                    event.setCancelled(true);
                    gamePlayer.getPlayer().sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-NO-BUILD")));
                }
            }
        }
    }
}