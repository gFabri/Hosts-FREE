package com.github.bfabri.hosts.game;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.Arena;
import com.github.bfabri.hosts.events.HostJoinEvent;
import com.github.bfabri.hosts.game.games.OneVsOne;
import com.github.bfabri.hosts.game.games.Sumo;
import com.github.bfabri.hosts.game.games.modes.sumo.FFA;
import com.github.bfabri.hosts.game.games.modes.sumo.Split;
import com.github.bfabri.hosts.game.games.modes.sumo.TwoVSTwo;
import com.github.bfabri.hosts.utils.GameUtils;
import com.github.bfabri.hosts.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class GameManager {

    @Getter
    @Setter
    private Game game;

    @Getter
    @Setter
    private String mode = null;


    @Setter
    public Arena selected;

    @Getter
    @Setter
    public Game.RewardTypes rewardTypes = null;

    public void newGameWithMode(String gameName, String mode, CommandSender player) {
        this.mode = mode;

        if (game != null) {
            player.sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-ACTIVE")));
            return;
        }

        ArrayList<Arena> valuesList = new ArrayList<>(Hosts.getInstance().getArenaManager().getArenas().values());
        if (valuesList.stream().noneMatch(arena -> arena.getGameName().equalsIgnoreCase(gameName) && !arena.getModesName().isEmpty()) || valuesList.stream().noneMatch(entry -> entry.getModesName().contains("ALL") ||
                entry.getModesName().contains(Hosts.getInstance().getGameManager().getMode().toUpperCase()))) {
            player.sendMessage(Utils.PREFIX + Utils.translate("&cNo arenas found"));
            return;
        }

        if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.REWARDS-SELECTOR")) {
            if (ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.NONE.enabled") || ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.ITEMS.enabled")
                    || ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.RANDOM.enabled") || ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.DEFAULT.enabled")) {

                if (rewardTypes == null) {
                    Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> Hosts.getInstance().getRewardsListener().openRewardsMenu((Player) player, gameName, mode), 1L);
                    return;
                }
            }
        } else {
            Game.RewardTypes defaultRewardType;
            try {
                defaultRewardType = Game.RewardTypes.valueOf(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.default"));
            } catch (IllegalArgumentException ignored) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Invalid default reward type");
                return;
            }
            rewardTypes = defaultRewardType;
        }

        if (selected == null) {
            if (player instanceof Player && ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.ARENA-SELECTOR") && player.hasPermission("hosts.arena.select")) {
                Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> ((Player) player).openInventory(Hosts.getInstance().getArenaManager().getArenasInInv(gameName)), 1L);
                return;
            } else {
                selected = valuesList.stream().filter(arena -> arena.getGameName().equalsIgnoreCase(gameName) && arena.isConfigured()).collect(Collectors.toList()).get(new Random().nextInt((int) valuesList.stream().filter(arena -> arena.getGameName().equalsIgnoreCase(gameName) && arena.isConfigured()).count()));
            }
        }

        if (gameName.equalsIgnoreCase("Sumo")) {
            if (mode.equalsIgnoreCase("1v1")) {
                this.game = new Sumo();
            } else if (mode.equalsIgnoreCase("Split")) {
                this.game = new Split();
            } else if (mode.equalsIgnoreCase("2v2")) {
                this.game = new TwoVSTwo();
            } else if (mode.equalsIgnoreCase("FFA")) {
                this.game = new FFA();
            }
        } else if (gameName.equalsIgnoreCase("FFA")) {
            if (mode.equalsIgnoreCase("Pot") || mode.equalsIgnoreCase("Gapple") || mode.equalsIgnoreCase("Axe") || mode.equalsIgnoreCase("BuildUHC") || mode.equalsIgnoreCase("Soup")) {
                this.game = new com.github.bfabri.hosts.game.games.FFA();
            } else if (mode.equalsIgnoreCase("Split")) {
                this.game = new com.github.bfabri.hosts.game.games.modes.ffa.Split();
            }
        } else if (gameName.equalsIgnoreCase("1v1")) {
            this.game = new OneVsOne();
        }

        if (game != null) {
            game.setSelectedReward(rewardTypes);
            game.setHoster(player);
            game.setArena(selected);
            game.setMode(mode);
            player.sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ARENA-SELECTED").replace("{arenaName}", selected.getArenaName())));
            Bukkit.getOnlinePlayers().forEach(players -> {
                try {
                    players.playSound(players.getLocation(), Sound.valueOf(ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.GENERAL.SOUND-ON-START")), 1.0f, 1.0f);
                } catch (IllegalArgumentException ex) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Invalid Sound name in config.yml, check the sound names for your version");
                }
            });
            try {
                GameUtils.sendImage(game);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            game.setCurrentStatus(Game.Status.STARTING);
            if (player instanceof Player) {
                Hosts.getInstance().getPlayerUtil().setCooldown((Player) player);
                Bukkit.getPluginManager().callEvent(new HostJoinEvent((Player) player, game));
            }
        }
    }

    public GamePlayer getPlayer(Player player) {
        if (game == null) {
            return null;
        }
        for (GamePlayer gamePlayer : game.getGamePlayers()) {
            if (gamePlayer.getPlayer() == player) {
                return gamePlayer;
            }
        }
        for (GamePlayer gamePlayer : Hosts.getInstance().getSpectatorManager().getSpectatorPlayers()) {
            if (gamePlayer.getPlayer() == player) {
                return gamePlayer;
            }
        }
        return null;
    }

    public boolean isGameAvailable() {
        return game != null;
    }
}