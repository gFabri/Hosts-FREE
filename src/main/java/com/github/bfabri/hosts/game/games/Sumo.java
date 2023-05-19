package com.github.bfabri.hosts.game.games;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.kits.Kit;
import com.github.bfabri.hosts.events.HostLeaveEvent;
import com.github.bfabri.hosts.game.Game;
import com.github.bfabri.hosts.game.GamePlayer;
import com.github.bfabri.hosts.utils.GameUtils;
import com.github.bfabri.hosts.utils.KitItemStack;
import com.github.bfabri.hosts.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Sumo extends Game implements Listener {

    @Getter
    private final ArrayList<GamePlayer> roundPlayers = new ArrayList<>();

    @Getter
    private HashMap<GamePlayer, Integer> HITS_COUNT = new HashMap<>();

    BukkitTask task;

    public Sumo(String name, String mode, String displayName, int minPlayers, int maxPlayers, int startTime, int maxRoundTime) {
        super(name, mode, displayName, minPlayers, maxPlayers, startTime, maxRoundTime);
    }

    public Sumo() {
        super("Sumo", "1v1", ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.Games.SUMO.displayName"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.min-players"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.max-players"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.start-time"), (int) TimeUnit.MINUTES.toSeconds(ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.max-round-time")));
        Bukkit.getPluginManager().registerEvents(this, Hosts.getInstance());
        onStart();
    }

    @Override
    public void onStart() {
        GameUtils.init(this);
    }

    @Override
    public void onStop() {
        if (ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TYPE").equalsIgnoreCase("GLOBAL")) {
            Bukkit.broadcastMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-STOP").replace("{player}", "Console").replace("{game}", getDisplayName())));
        } else {
            getGamePlayers().forEach(gamePlayer -> {
                Player player = gamePlayer.getPlayer();
                player.sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-STOP").replace("{player}", "Console").replace("{game}", getDisplayName())));
            });
        }
        if (getHoster() instanceof Player) {
            Hosts.getInstance().getPlayerUtil().getPlayersCooldowns().remove(((Player) getHoster()).getUniqueId());
        }

        getGamePlayers().forEach(gamePlayers -> {
            gamePlayers.getPlayer().getInventory().setArmorContents(null);
            gamePlayers.getPlayer().getInventory().clear();
            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(gamePlayers), 1L);
            if (!ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                gamePlayers.getPlayer().teleport(gamePlayers.getLocation());
            } else {
                gamePlayers.getPlayer().teleport(Location.deserialize(getArena().getServerSpawn()));
            }
        });

        Hosts.getInstance().getSpectatorManager().onFinish(this);

        if (getHoster() instanceof Player) {
            if (Hosts.getInstance().getRewardsListener().getItemsRewards().getContents().length > 8) {
                for (ItemStack itemStack : Hosts.getInstance().getRewardsListener().getItemsRewards().getContents()) {
                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cStart Game &7<"))) {
                            continue;
                        }
                        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&c."))) {
                            continue;
                        }
                        Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> {
                            if (getHoster() instanceof Player) {
                                ((Player) getHoster()).getInventory().addItem(itemStack);
                            }
                            Hosts.getInstance().getRewardsListener().getItemsRewards().removeItem(itemStack);
                        }, 22L);
                    }
                }
            }
            Hosts.getInstance().getPlayerUtil().getPlayersCooldowns().remove(((Player) getHoster()).getUniqueId());
        }
        Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> {
            if (getGeneralTask() != null) {
                getGeneralTask().cancel();
            }
            setCurrentStatus(Status.OFFLINE);
            setSelectedReward(null);
            setArena(null);
            Hosts.getInstance().getGameManager().setRewardTypes(null);
            Hosts.getInstance().getGameManager().setSelected(null);
            setHoster(null);
            getGamePlayers().clear();
            Hosts.getInstance().getGameManager().setGame(null);
            HandlerList.unregisterAll(this);
        }, 40L);
    }

    @Override
    public void onWin(GamePlayer gamePlayer) {
        Hosts.getInstance().getSpectatorManager().onFinish(this);
        Player player = gamePlayer.getPlayer();

        player.setFallDistance(0);
        player.setHealth(20);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(gamePlayer), 1L);

        Location targetLocation = ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")
                ? Location.deserialize(this.arena.getServerSpawn())
                : gamePlayer.getLocation();
        player.teleport(targetLocation);

        GameUtils.sendVictoryImage(player, this);

        Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> GameUtils.giveRewards(this, gamePlayer), 2L);

        Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> {
            setCurrentStatus(Status.OFFLINE);
            setSelectedReward(null);
            setArena(null);
            Hosts.getInstance().getGameManager().setRewardTypes(null);
            Hosts.getInstance().getGameManager().setSelected(null);
            setHoster(null);
            getGamePlayers().clear();
            Hosts.getInstance().getGameManager().setGame(null);
            HandlerList.unregisterAll(this);
        }, 40L);
    }


    @Override
    public void onLoss(GamePlayer gamePlayer) {
        getRoundPlayers().remove(gamePlayer);
        getGamePlayers().remove(gamePlayer);

        getGamePlayers().forEach(gamePlayers -> gamePlayers.getPlayer().sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig()
                .getString("ELIMINATED").replace("{player}", gamePlayer.getPlayer().getDisplayName())
                .replace("{game}", getDisplayName()).replace("{players}", String.valueOf(getGamePlayers().size()))
                .replace("{max-players}", String.valueOf(getMaxPlayers())))));

        Player firstPlayer = getRoundPlayers().get(0).getPlayer();

        firstPlayer.setFallDistance(0);
        firstPlayer.setHealth(20);
        firstPlayer.getActivePotionEffects().forEach(potionEffect -> firstPlayer.removePotionEffect(potionEffect.getType()));
        firstPlayer.getInventory().clear();
        firstPlayer.getInventory().setArmorContents(null);

        firstPlayer.teleport(Location.deserialize(arena.getPreSpawn()));

        Player player = gamePlayer.getPlayer();

        player.setFallDistance(0);
        player.setHealth(20);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        if (gamePlayer.getStatus() != GamePlayer.Status.DISCONNECTED) {
            if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.SPECTATOR")) {
                Hosts.getInstance().getSpectatorManager().joinSpectator(this, gamePlayer);
            } else {
                if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                    player.teleport(Location.deserialize(this.arena.getServerSpawn()));
                } else {
                    player.teleport(gamePlayer.getLocation());
                }
                Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(gamePlayer), 1L);
            }
        } else {
            if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                player.teleport(Location.deserialize(this.arena.getServerSpawn()));
            } else {
                player.teleport(gamePlayer.getLocation());
            }
            loadInv(gamePlayer);
            gamePlayer.setStatus(GamePlayer.Status.LIVE);
        }

        if (getGamePlayers().size() <= 1) {
            onWin(getRoundPlayers().get(0));
        } else {
            onStartedGame();
        }
    }

    @Override
    public void onWin(ArrayList<GamePlayer> gamePlayer) {

    }

    @Override
    public void onLoss(ArrayList<GamePlayer> gamePlayer) {

    }

    @Override
    public void loadInv(GamePlayer gamePlayer) {
        if (gamePlayer.getArmor() != null) {
            gamePlayer.getPlayer().getInventory().setArmorContents(gamePlayer.getArmor());
        }
        if (gamePlayer.getInventory() != null) {
            gamePlayer.getPlayer().getInventory().setContents(gamePlayer.getInventory());
        }
        gamePlayer.getPlayer().updateInventory();
    }

    @Override
    public void onStartedGame() {
        setMaxRoundTime((int) TimeUnit.MINUTES.toSeconds(ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.max-round-time")));
        getHITS_COUNT().clear();
        getRoundPlayers().clear();
        if (task != null) {
            task.cancel();
            task = null;
        }
        Random random = new Random();
        int firstPlayerIndex = random.nextInt(gamePlayers.size());
        GamePlayer firstPlayer = gamePlayers.get(firstPlayerIndex);
        int secondPlayerIndex;
        do {
            secondPlayerIndex = random.nextInt(gamePlayers.size());
        } while (secondPlayerIndex == firstPlayerIndex);
        GamePlayer secondPlayer = gamePlayers.get(secondPlayerIndex);

        getRoundPlayers().add(firstPlayer);
        getRoundPlayers().add(secondPlayer);

        getHITS_COUNT().put(firstPlayer, 0);
        getHITS_COUNT().put(secondPlayer, 0);

        firstPlayer.getPlayer().teleport(Location.deserialize(arena.getLocations().get(0)));
        secondPlayer.getPlayer().teleport(Location.deserialize(arena.getLocations().get(1)));

        getRoundPlayers().forEach(players -> {
            players.getPlayer().getActivePotionEffects().forEach(type -> players.getPlayer().removePotionEffect(type.getType()));
            players.getPlayer().setFlying(false);
            players.getPlayer().setAllowFlight(false);
            players.getPlayer().setHealth(20.0D);
            players.getPlayer().setFoodLevel(20);

            if (getArena().isHasKit() && Hosts.getInstance().getArenaManager().getKit(getArena(), "global") != null) {
                Kit kitGlobal = Hosts.getInstance().getArenaManager().getKit(getArena(), "global");
                Player player = players.getPlayer();
                player.getInventory().setContents(KitItemStack.deserialize(kitGlobal.getInventory()));
                player.getInventory().setArmorContents(KitItemStack.deserialize(kitGlobal.getArmor()));
                player.updateInventory();
            }
        });

        GameUtils.initPvPTime(this, getRoundPlayers());

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (getCurrentStatus() == Status.OFFLINE) {
                    return;
                }
                if (getGamePlayers().size() <= 2) {
                    return;
                }
                int timeLeft = (int) getMaxRoundTime();
                setMaxRoundTime(--timeLeft);
                String announceTimes = ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TIMES");
                for (String count : announceTimes.split(",")) {
                    if (count.equalsIgnoreCase(Long.toString(getMaxRoundTime()))) {
                        getGamePlayers().forEach(gamePlayers -> gamePlayers.getPlayer().sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.GAMES.SUMO.ROUND-FINISH").replace("{time}", String.valueOf(getMaxRoundTime())))));
                        break;
                    }
                }
                if (timeLeft <= 1L) {
                    this.cancel();
                    GamePlayer gamePlayer = getHitWinner(getRoundPlayers().get(0), getRoundPlayers().get(1));
                    onLoss(gamePlayer);
                    getGamePlayers().forEach(gamePlayers -> gamePlayers.getPlayer().sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.GAMES.SUMO.ROUND-TIME-REACHED"))));
                }
            }
        }.runTaskTimer(Hosts.getInstance(), 0L, 20L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (getCurrentStatus() == Status.STARTED) {
            GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

            if (gamePlayer == null) {
                return;
            }

            World world = gamePlayer.getPlayer().getLocation().getWorld();
            Location playerLoc = gamePlayer.getPlayer().getLocation();
            boolean isRoundPlayer = getRoundPlayers().contains(gamePlayer);

            if (isRoundPlayer && getStartTime() > 1L) {
                event.setTo(event.getFrom());
                event.getTo().setY(event.getFrom().getY());
                return;
            }

            Block waterBlock = world.getBlockAt((int) playerLoc.getX(), (int) playerLoc.getY(), (int) playerLoc.getZ());
            if ((isRoundPlayer && waterBlock.getType() == GameUtils.getMaterialByVersion("WATER")) || (isRoundPlayer && waterBlock.getType() == Material.WATER) || playerLoc.getY() <= 5.0D) {
                onLoss(gamePlayer);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

        if (gamePlayer == null) {
            return;
        }

         if (Hosts.getInstance().getSpectatorManager().getSpectatorPlayers().contains(gamePlayer)) {
            if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                gamePlayer.getPlayer().teleport(Location.deserialize(this.arena.getServerSpawn()));
            } else {
                gamePlayer.getPlayer().teleport(gamePlayer.getLocation());
            }
            loadInv(gamePlayer);
            return;
        }

        if (getGamePlayers().contains(gamePlayer)) {
            if (getRoundPlayers().contains(gamePlayer)) {
                gamePlayer.setStatus(GamePlayer.Status.DISCONNECTED);
                getGeneralTask().cancel();
                onLoss(gamePlayer);
            } else {
                Bukkit.getPluginManager().callEvent(new HostLeaveEvent(event.getPlayer(), this, HostLeaveEvent.Types.ELIMINATION));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (getCurrentStatus() == Status.STARTED) {
            if (event.getEntity() instanceof Player) {
                GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer((Player) event.getEntity());

                if (gamePlayer == null) {
                    return;
                }

                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    if (getRoundPlayers().contains(gamePlayer) || getGamePlayers().contains(gamePlayer)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getEntity());

        if (gamePlayer == null) {
            return;
        }

        if (getRoundPlayers().contains(gamePlayer)) {
            event.setDeathMessage(null);
            event.getDrops().forEach(itemStack -> itemStack.setType(Material.AIR));

            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> gamePlayer.getPlayer().spigot().respawn(), 0L);
            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> onLoss(gamePlayer), 1L);
        } else {
            Bukkit.getPluginManager().callEvent(new HostLeaveEvent(event.getEntity(), this, HostLeaveEvent.Types.ELIMINATION));
        }
    }

    @EventHandler
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        if (getCurrentStatus() == Status.STARTED) {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {

                GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer((Player) event.getEntity());

                GamePlayer gameDamager = Hosts.getInstance().getGameManager().getPlayer((Player) event.getDamager());

                if (gamePlayer == null || gameDamager == null) {
                    return;
                }

                if (getGamePlayers().contains(gamePlayer) || getGamePlayers().contains(gameDamager)) {
                    event.setCancelled(true);
                }

                if (getRoundPlayers().contains(gamePlayer) && getRoundPlayers().contains(gameDamager)) {
                    event.setCancelled(false);
                    event.setDamage(0.0D);
                    int hits = getHITS_COUNT().getOrDefault(gameDamager, 0);
                    getHITS_COUNT().put(gameDamager, hits + 1);
                }
            }
        }
    }

    public GamePlayer getHitWinner(GamePlayer player1, GamePlayer player2) {
        int hits1 = getHITS_COUNT().get(player1);
        int hits2 = getHITS_COUNT().get(player2);

        return hits1 == hits2 ? (Math.random() < 0.5 ? player1 : player2) : (hits1 < hits2 ? player1 : player2);
    }
}
