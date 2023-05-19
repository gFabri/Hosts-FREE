package com.github.bfabri.hosts.game.games.modes.sumo;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.kits.Kit;
import com.github.bfabri.hosts.events.HostLeaveEvent;
import com.github.bfabri.hosts.game.Game;
import com.github.bfabri.hosts.game.GamePlayer;
import com.github.bfabri.hosts.game.Team;
import com.github.bfabri.hosts.game.TeamManager;
import com.github.bfabri.hosts.utils.GameUtils;
import com.github.bfabri.hosts.utils.KitItemStack;
import com.github.bfabri.hosts.utils.Utils;
import com.github.bfabri.hosts.utils.images.ImageChar;
import com.github.bfabri.hosts.utils.images.ImageMessage;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TwoVSTwo extends Game implements Listener {

    @Getter
    private List<GamePlayer> players;

    @Getter
    private final ArrayList<Team> roundPlayers = new ArrayList<>();

    @Getter
    private HashMap<Team, Integer> HITS_COUNT = new HashMap<>();

    BukkitTask task;

    public TwoVSTwo() {
        super("Sumo", "2v2", ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.Games.SUMO.displayName"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.min-players"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.max-players"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.start-time"), (int) TimeUnit.MINUTES.toSeconds(ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.max-round-time")));
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

        if (getCurrentStatus() == Status.STARTING || Hosts.getInstance().getTeamManager().getTeams().size() == 0) {
            getGamePlayers().forEach(gamePlayers -> {
                gamePlayers.getPlayer().getInventory().setArmorContents(null);
                gamePlayers.getPlayer().getInventory().clear();
                Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(gamePlayers), 20L);
                if (!ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                    gamePlayers.getPlayer().teleport(gamePlayers.getLocation());
                } else {
                    gamePlayers.getPlayer().teleport(Location.deserialize(getArena().getServerSpawn()));
                }
            });
        } else if (getCurrentStatus() == Status.STARTED) {
            Hosts.getInstance().getTeamManager().getTeams().forEach(teams -> teams.getTeamPlayers().forEach(teamPlayer -> {
                teamPlayer.getPlayer().getInventory().setArmorContents(null);
                teamPlayer.getPlayer().getInventory().clear();
                Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(teamPlayer), 20L);
                if (!ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                    teamPlayer.getPlayer().teleport(teamPlayer.getLocation());
                } else {
                    teamPlayer.getPlayer().teleport(Location.deserialize(getArena().getServerSpawn()));
                }
            }));
        }

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
            getGeneralTask().cancel();
            Hosts.getInstance().getTeamManager().getTeams().clear();
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
    }


    @Override
    public void onLoss(GamePlayer gamePlayer) {
        Team team = Hosts.getInstance().getTeamManager().getTeamByPlayer(gamePlayer);

        if (team == null) {
            return;
        }

        if (team.getPlayerA() == gamePlayer) {
            team.setEliminatedA(true);
        } else if (team.getPlayerB() == gamePlayer) {
            team.setEliminatedB(true);
        }
        getGamePlayers().remove(gamePlayer);

        gamePlayer.getPlayer().setFallDistance(0);
        gamePlayer.getPlayer().setHealth(20);
        gamePlayer.getPlayer().getActivePotionEffects().forEach(potionEffect -> gamePlayer.getPlayer().removePotionEffect(potionEffect.getType()));
        gamePlayer.getPlayer().getInventory().clear();
        gamePlayer.getPlayer().getInventory().setArmorContents(null);

        if (gamePlayer.getStatus() != GamePlayer.Status.DISCONNECTED) {
            if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.SPECTATOR")) {
                Hosts.getInstance().getSpectatorManager().joinSpectator(this, gamePlayer);
            } else {
                if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                    gamePlayer.getPlayer().teleport(Location.deserialize(this.arena.getServerSpawn()));
                } else {
                    gamePlayer.getPlayer().teleport(gamePlayer.getLocation());
                }
                Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(gamePlayer), 10L);
            }
        } else {
            if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                gamePlayer.getPlayer().teleport(Location.deserialize(this.arena.getServerSpawn()));
            } else {
                gamePlayer.getPlayer().teleport(gamePlayer.getLocation());
            }
            loadInv(gamePlayer);
            gamePlayer.setStatus(GamePlayer.Status.LIVE);
        }

        if (team.isEliminatedA() && team.isEliminatedB()) {
            Hosts.getInstance().getTeamManager().getTeams().remove(team);
            Hosts.getInstance().getTeamManager().sendMessage(ConfigHandler.Configs.LANG.getConfig()
                    .getString("ELIMINATED")
                    .replace("{player}", team.getTeamPlayers().stream().map(teamPlayers -> teamPlayers.getPlayer().getDisplayName()).collect(Collectors.joining(", ")))
                    .replace("{game}", getDisplayName()).replace("{players}", String.valueOf(Hosts.getInstance().getTeamManager().getTeams().size()))
                    .replace("{max-players}", String.valueOf(getMaxPlayers())));
            getRoundPlayers().remove(team);

            Team win = getRoundPlayers().get(0);

            win.getTeamPlayers().forEach(gamePlayers -> {
                gamePlayers.getPlayer().setFallDistance(0);
                gamePlayers.getPlayer().setHealth(20);
                gamePlayers.getPlayer().getActivePotionEffects().forEach(potionEffect -> gamePlayers.getPlayer().removePotionEffect(potionEffect.getType()));
                gamePlayers.getPlayer().getInventory().clear();
                gamePlayers.getPlayer().getInventory().setArmorContents(null);

                gamePlayers.getPlayer().teleport(Location.deserialize(arena.getPreSpawn()));
            });

            if (Hosts.getInstance().getTeamManager().getTeams().size() <= 1) {
                onWin(getRoundPlayers().get(0).getTeamPlayers());
            } else {
                onStartedGame();
            }
        }
    }

    @Override
    public void onWin(ArrayList<GamePlayer> gamePlayer) {
        Hosts.getInstance().getSpectatorManager().onFinish(this);

        gamePlayer.forEach(gamePlayers -> {
            gamePlayers.getPlayer().setFallDistance(0);
            gamePlayers.getPlayer().setHealth(20);
            gamePlayers.getPlayer().getActivePotionEffects().forEach(potionEffect -> gamePlayers.getPlayer().removePotionEffect(potionEffect.getType()));
            gamePlayers.getPlayer().getInventory().clear();
            gamePlayers.getPlayer().getInventory().setArmorContents(null);

            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(gamePlayers), 10L);

            Location targetLocation = ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")
                    ? Location.deserialize(this.arena.getServerSpawn())
                    : gamePlayers.getLocation();
            gamePlayers.getPlayer().teleport(targetLocation);

        });
        GameUtils.sendVictoryImageTeam(gamePlayer, this);

        Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> GameUtils.giveRewards(this, gamePlayer), 20L);

        Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> {
            getGeneralTask().cancel();
            Hosts.getInstance().getTeamManager().getTeams().clear();
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
    public void onLoss(ArrayList<GamePlayer> gamePlayer) {
        Team team = Hosts.getInstance().getTeamManager().getTeamByPlayer(gamePlayer.get(0));

        if (team == null) {
            return;
        }

        team.getTeamPlayers().forEach(teamPlayers -> {
            getGamePlayers().remove(teamPlayers);
            teamPlayers.getPlayer().setFallDistance(0);
            teamPlayers.getPlayer().setHealth(20);
            teamPlayers.getPlayer().getActivePotionEffects().forEach(potionEffect -> teamPlayers.getPlayer().removePotionEffect(potionEffect.getType()));
            teamPlayers.getPlayer().getInventory().clear();
            teamPlayers.getPlayer().getInventory().setArmorContents(null);

            if (teamPlayers.getStatus() != GamePlayer.Status.DISCONNECTED) {
                if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.SPECTATOR")) {
                    Hosts.getInstance().getSpectatorManager().joinSpectator(this, teamPlayers);
                } else {
                    if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                        teamPlayers.getPlayer().teleport(Location.deserialize(this.arena.getServerSpawn()));
                    } else {
                        teamPlayers.getPlayer().teleport(teamPlayers.getLocation());
                    }
                    Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(teamPlayers), 10L);
                }
            } else {
                if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")) {
                    teamPlayers.getPlayer().teleport(Location.deserialize(this.arena.getServerSpawn()));
                } else {
                    teamPlayers.getPlayer().teleport(teamPlayers.getLocation());
                }
                loadInv(teamPlayers);
                teamPlayers.setStatus(GamePlayer.Status.LIVE);
            }
        });

        Hosts.getInstance().getTeamManager().getTeams().remove(team);
        Hosts.getInstance().getTeamManager().sendMessage(ConfigHandler.Configs.LANG.getConfig()
                .getString("ELIMINATED")
                .replace("{player}", team.getTeamPlayers().stream().map(teamPlayers -> teamPlayers.getPlayer().getDisplayName()).collect(Collectors.joining(", ")))
                .replace("{game}", getDisplayName()).replace("{players}", String.valueOf(Hosts.getInstance().getTeamManager().getTeams().size()))
                .replace("{max-players}", String.valueOf(getMaxPlayers())));
        getRoundPlayers().remove(team);

        Team win = getRoundPlayers().get(0);

        win.getTeamPlayers().forEach(gamePlayers -> {
            gamePlayers.getPlayer().setFallDistance(0);
            gamePlayers.getPlayer().setHealth(20);
            gamePlayers.getPlayer().getActivePotionEffects().forEach(potionEffect -> gamePlayers.getPlayer().removePotionEffect(potionEffect.getType()));
            gamePlayers.getPlayer().getInventory().clear();
            gamePlayers.getPlayer().getInventory().setArmorContents(null);

            gamePlayers.getPlayer().teleport(Location.deserialize(arena.getPreSpawn()));
        });

        if (Hosts.getInstance().getTeamManager().getTeams().size() <= 1) {
            onWin(getRoundPlayers().get(0).getTeamPlayers());
        } else {
            onStartedGame();
        }
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

    private void createTeams() {
        TeamManager teamManager = Hosts.getInstance().getTeamManager();
        List<Team> teams = teamManager.getTeams();
        Iterator<GamePlayer> playerIterator = players.iterator();

        if (players.size() % 2 != 0) {
            GamePlayer lastPlayer = playerIterator.next();
            Team team = new Team("normalTeam", lastPlayer, null);
            teams.add(team);
            playerIterator.remove();
        }

        while (playerIterator.hasNext()) {
            GamePlayer firstPlayer = playerIterator.next();
            playerIterator.remove();
            GamePlayer secondPlayer = playerIterator.next();
            playerIterator.remove();
            Team team = new Team("normalTeam", firstPlayer, secondPlayer);
            teams.add(team);
        }

        onStartedGame();
    }

    @Override
    public void onStartedGame() {
        if (players == null) {
            players = new ArrayList<>(getGamePlayers());
        }
        if (players.size() != 0) {
            initTeam();
            return;
        }
        setMaxRoundTime((int) TimeUnit.MINUTES.toSeconds(ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.max-round-time")));
        getHITS_COUNT().clear();
        getRoundPlayers().clear();
        if (task != null) {
            task.cancel();
            task = null;
        }
        Random random = new Random();
        ArrayList<Team> teams = Hosts.getInstance().getTeamManager().getTeams();

        int firstTeamIndex = random.nextInt(teams.size());
        Team firstTeam = teams.get(firstTeamIndex);
        int secondTeamIndex;
        do {
            secondTeamIndex = random.nextInt(teams.size());
        } while (secondTeamIndex == firstTeamIndex);
        Team secondTeam = teams.get(secondTeamIndex);

        getRoundPlayers().add(firstTeam);
        getRoundPlayers().add(secondTeam);

        getHITS_COUNT().put(firstTeam, 0);
        getHITS_COUNT().put(secondTeam, 0);

        firstTeam.getPlayerA().getPlayer().teleport(Location.deserialize(arena.getLocations().get(0)));
        if (firstTeam.getPlayerB() != null) {
            firstTeam.getPlayerB().getPlayer().teleport(Location.deserialize(arena.getLocations().get(1)));
        }

        firstTeam.getTeamPlayers().forEach(teamAPlayers -> {
            teamAPlayers.getPlayer().getActivePotionEffects().forEach(type -> teamAPlayers.getPlayer().removePotionEffect(type.getType()));
            teamAPlayers.getPlayer().setFlying(false);
            teamAPlayers.getPlayer().setAllowFlight(false);
            teamAPlayers.getPlayer().setHealth(20.0D);
            teamAPlayers.getPlayer().setFoodLevel(20);

            if (getArena().isHasKit() && Hosts.getInstance().getArenaManager().getKit(getArena(), "global") != null) {
                Kit kitGlobal = Hosts.getInstance().getArenaManager().getKit(getArena(), "global");
                Player player = teamAPlayers.getPlayer();
                player.getInventory().setContents(KitItemStack.deserialize(kitGlobal.getInventory()));
                player.getInventory().setArmorContents(KitItemStack.deserialize(kitGlobal.getArmor()));

                player.updateInventory();
            }
        });

        secondTeam.getPlayerA().getPlayer().teleport(Location.deserialize(arena.getLocations().get(2)));

        if (secondTeam.getPlayerB() != null) {
            secondTeam.getPlayerB().getPlayer().teleport(Location.deserialize(arena.getLocations().get(3)));
        }

        secondTeam.getTeamPlayers().forEach(teamBPlayers -> {
            teamBPlayers.getPlayer().getActivePotionEffects().forEach(type -> teamBPlayers.getPlayer().removePotionEffect(type.getType()));
            teamBPlayers.getPlayer().setFlying(false);
            teamBPlayers.getPlayer().setAllowFlight(false);
            teamBPlayers.getPlayer().setHealth(20.0D);
            teamBPlayers.getPlayer().setFoodLevel(20);

            if (getArena().isHasKit() && Hosts.getInstance().getArenaManager().getKit(getArena(), "global") != null) {
                Kit kitGlobal = Hosts.getInstance().getArenaManager().getKit(getArena(), "global");
                Player player = teamBPlayers.getPlayer();
                player.getInventory().setContents(KitItemStack.deserialize(kitGlobal.getInventory()));
                player.getInventory().setArmorContents(KitItemStack.deserialize(kitGlobal.getArmor()));
                player.updateInventory();
            }
        });

        ArrayList<GamePlayer> tempRound = (ArrayList<GamePlayer>) getRoundPlayers().get(0).getTeamPlayers().clone();
        tempRound.addAll(getRoundPlayers().get(1).getTeamPlayers());

        GameUtils.initPvPTime(this, tempRound);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (getCurrentStatus() == Status.OFFLINE) {
                    return;
                }
                if (Hosts.getInstance().getTeamManager().getTeams().size() <= 2) {
                    return;
                }
                int timeLeft = (int) getMaxRoundTime();
                setMaxRoundTime(--timeLeft);
                String announceTimes = ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TIMES");
                for (String count : announceTimes.split(",")) {
                    if (count.equalsIgnoreCase(Long.toString(getMaxRoundTime()))) {
                        Hosts.getInstance().getTeamManager().sendMessage(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.GAMES.SUMO.ROUND-FINISH").replace("{time}", String.valueOf(getMaxRoundTime())));
                        break;
                    }
                }
                if (timeLeft <= 1L) {
                    this.cancel();
                    Team winnerHit = getHitWinner(getRoundPlayers().get(0), getRoundPlayers().get(1));
                    onLoss(winnerHit.getTeamPlayers());
                    Hosts.getInstance().getTeamManager().sendMessage(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.GAMES.SUMO.ROUND-TIME-REACHED"));
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

            Team team = Hosts.getInstance().getTeamManager().getTeamByPlayer(gamePlayer);

            if (team == null) {
                return;
            }

            boolean isRoundPlayer = getRoundPlayers().contains(team);

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


        Team team = Hosts.getInstance().getTeamManager().getTeamByPlayer(gamePlayer);

        if (team == null) {
            return;
        }

        if (getRoundPlayers().contains(team)) {
            gamePlayer.setStatus(GamePlayer.Status.DISCONNECTED);
            onLoss(gamePlayer);
        } else {
            Bukkit.getPluginManager().callEvent(new HostLeaveEvent(event.getPlayer(), this, HostLeaveEvent.Types.ELIMINATION));
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

                Team team = Hosts.getInstance().getTeamManager().getTeamByPlayer(gamePlayer);

                if (team == null) {
                    return;
                }

                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    if (getRoundPlayers().contains(team) || getGamePlayers().contains(gamePlayer)) {
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

        Team team = Hosts.getInstance().getTeamManager().getTeamByPlayer(gamePlayer);

        if (team == null) {
            return;
        }

        if (getRoundPlayers().contains(team)) {
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

                Team teamA = Hosts.getInstance().getTeamManager().getTeamByPlayer(gamePlayer);

                Team teamB = Hosts.getInstance().getTeamManager().getTeamByPlayer(gameDamager);

                if (gamePlayer == null || gameDamager == null) {
                    return;
                }

                if (teamA == null || teamB == null) {
                    return;
                }

                if (getGamePlayers().contains(gamePlayer) || getGamePlayers().contains(gameDamager)) {
                    event.setCancelled(true);
                }

                if (teamA.getTeamPlayers().contains(gamePlayer) && teamA.getTeamPlayers().contains(gameDamager) || teamB.getTeamPlayers().contains(gamePlayer) && teamB.getTeamPlayers().contains(gameDamager)) {
                    event.setCancelled(true);
                    return;
                }

                if (getRoundPlayers().contains(teamB) && getRoundPlayers().contains(teamA)) {
                    event.setCancelled(false);
                    event.setDamage(0.0D);
                    int hits = getHITS_COUNT().getOrDefault(teamB, 0);
                    getHITS_COUNT().put(teamB, hits + 1);
                }
            }
        } else if (Hosts.getInstance().getGameManager().getGame().getCurrentStatus() == Status.STARTING) {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {

                GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer((Player) event.getEntity());

                GamePlayer gameDamager = Hosts.getInstance().getGameManager().getPlayer((Player) event.getDamager());

                Team teamA = Hosts.getInstance().getTeamManager().getTeamByPlayer(gamePlayer);

                Team teamB = Hosts.getInstance().getTeamManager().getTeamByPlayer(gameDamager);

                if (gamePlayer == null || gameDamager == null) {
                    return;
                }

                if (teamA == null || teamB == null) {
                    return;
                }

                if (Hosts.getInstance().getTeamManager().getTeams().contains(teamA)
                        || Hosts.getInstance().getTeamManager().getTeams().contains(teamB)) {
                    event.setCancelled(true);
                    gameDamager.getPlayer().sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("GAME-NO-PVP")));
                }
            }
        }
    }

    public void initTeam() {
        setStartTime(ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.team-creation"));
        getGamePlayers().forEach(gamePlayer -> gamePlayer.getPlayer().sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.GAMES.SUMO.TEAM.HOW-TO-CREATE"))));
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                int timeLeft = (int) getStartTime() - 1;
                setStartTime(timeLeft);
                String announceTimes = ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TIMES");
                for (String count : announceTimes.split(",")) {
                    if (count.equalsIgnoreCase(Integer.toString(timeLeft))) {
                        try {
                            sendTeamImage();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    }
                }
                if (timeLeft <= 1L) {
                    this.cancel();
                    createTeams();
                }
            }
        }.runTaskTimer(Hosts.getInstance(), 0L, 20L);
        setGeneralTask(task);
    }

    public void sendTeamImage() throws IOException {
        List<String> messageList = ConfigHandler.Configs.LANG.getConfig().getStringList("ANNOUNCEMENTS.GAMES.SUMO.TEAM.STARTING.LINES");
        BufferedImage imageToSend = ImageIO.read(Hosts.getInstance().getResource("Images/Team.png"));

        String startTime = String.valueOf(getStartTime());

        if (Hosts.getInstance().getConfig().getBoolean("HOST.GENERAL.HOST-IMAGE")) {
            ImageMessage imageMessage = new ImageMessage(imageToSend, 9, ImageChar.DARK_SHADE.getChar());

            List<String> updatedMessages = new ArrayList<>();
            for (String message : messageList) {
                updatedMessages.add(message.replace("{time}", startTime));
            }
            imageMessage.appendText(updatedMessages).sendToPlayers(gamePlayers);
        } else {
            for (String message : messageList) {
                String updatedMessage = message.replace("{time}", startTime);
                String translatedMessage = Utils.translate(updatedMessage);
                gamePlayers.forEach(gamePlayer -> gamePlayer.getPlayer().sendMessage(translatedMessage));
            }
        }
    }

    public Team getHitWinner(Team teamA, Team teamB) {
        int hits1 = getHITS_COUNT().get(teamA);
        int hits2 = getHITS_COUNT().get(teamB);

        return hits1 == hits2 ? (Math.random() < 0.5 ? teamA : teamB) : (hits1 < hits2 ? teamA : teamB);
    }
}
