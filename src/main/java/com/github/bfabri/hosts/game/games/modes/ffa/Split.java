package com.github.bfabri.hosts.game.games.modes.ffa;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.kits.Kit;
import com.github.bfabri.hosts.events.HostLeaveEvent;
import com.github.bfabri.hosts.game.GamePlayer;
import com.github.bfabri.hosts.game.Team;
import com.github.bfabri.hosts.game.games.FFA;
import com.github.bfabri.hosts.utils.GameUtils;
import com.github.bfabri.hosts.utils.KitItemStack;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Split extends FFA implements Listener {

    public Split() {
        super("FFA", "Split", ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.Games.FFA.displayName"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.FFA.min-players"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.FFA.max-players"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.FFA.start-time"), (int) TimeUnit.MINUTES.toSeconds(ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.FFA.max-round-time")));
        Bukkit.getPluginManager().registerEvents(this, Hosts.getInstance());
        onStart();
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
            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(gamePlayers), 20L);
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
    }

    @Override
    public void onLoss(GamePlayer gamePlayer) {

        Team team = Hosts.getInstance().getTeamManager().getTeamByPlayer(gamePlayer);

        if (team == null) {
            return;
        }

        team.getPlayers().remove(gamePlayer);

        getGamePlayers().remove(gamePlayer);

        getGamePlayers().forEach(gamePlayers -> gamePlayers.getPlayer().sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig()
                .getString("ELIMINATED").replace("{player}", gamePlayer.getPlayer().getDisplayName())
                .replace("{game}", getDisplayName()).replace("{players}", String.valueOf(getGamePlayers().size()))
                .replace("{max-players}", String.valueOf(getMaxPlayers())))));

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
                Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(gamePlayer), 10L);
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

        if (team.isEliminated()) {
            onWin(team.getOpponent().getPlayers());
        }
    }

    @Override
    public void onWin(ArrayList<GamePlayer> gamePlayer) {
        Hosts.getInstance().getSpectatorManager().onFinish(this);

        GameUtils.sendVictoryImageTeam(gamePlayer, this);

        gamePlayer.forEach(gamePlayers -> {
            Player player = gamePlayers.getPlayer();

            player.setFallDistance(0);
            player.setHealth(20);
            player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(gamePlayers), 10L);

            Location targetLocation = ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")
                    ? Location.deserialize(this.arena.getServerSpawn())
                    : gamePlayers.getLocation();
            player.teleport(targetLocation);

            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> GameUtils.giveRewards(this, gamePlayers), 20L);
        });

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
    public void onStartedGame() {

        ArrayList<GamePlayer> teamA = new ArrayList<>(getGamePlayers().subList(0, (getGamePlayers().size() / 2)));
        ArrayList<GamePlayer> teamB = new ArrayList<>(getGamePlayers().subList((getGamePlayers().size() / 2), getGamePlayers().size()));

        Team splitA = new Team("splitA", teamA);
        Team splitB = new Team("splitB", teamB);

        splitA.setOpponent(splitB);
        splitB.setOpponent(splitA);

        getGamePlayers().forEach(gamePlayer -> {
            gamePlayer.getPlayer().teleport(Location.deserialize(arena.getLocations().get(new Random().nextInt(arena.getLocations().size()))));

            try {
                Hosts.getInstance().getConfig().getStringList("HOST.Games.FFA.effects-in." + getMode().toLowerCase()).forEach(lines -> gamePlayer.getPlayer().addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(lines.split(":")[0])), 2147483647, Integer.parseInt(lines.split(":")[1]) - 1)));
            } catch (IllegalArgumentException ignored) {
                Bukkit.broadcast(ChatColor.RED + "FFA Event has invalid effect", "*");
            }

            gamePlayer.getPlayer().setFlying(false);
            gamePlayer.getPlayer().setAllowFlight(false);
            gamePlayer.getPlayer().setHealth(20.0D);
            gamePlayer.getPlayer().setFoodLevel(20);


            if (getArena().isHasKit() && Hosts.getInstance().getArenaManager().getKit(getArena(), "split") != null) {
                Kit kitGlobal = Hosts.getInstance().getArenaManager().getKit(getArena(), "split");
                Player player = gamePlayer.getPlayer();
                player.getInventory().setContents(KitItemStack.deserialize(kitGlobal.getInventory()));
                player.getInventory().setArmorContents(KitItemStack.deserialize(kitGlobal.getArmor()));
                player.updateInventory();
            }
        });

        Hosts.getInstance().getTeamManager().getTeams().add(splitA);
        Hosts.getInstance().getTeamManager().getTeams().add(splitB);

        GameUtils.initPvPTime(this, getGamePlayers());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (getCurrentStatus() != Status.STARTED) {
            return;
        }

        GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

        if (gamePlayer == null) {
            return;
        }

        Team team = Hosts.getInstance().getTeamManager().getTeamByPlayer(gamePlayer);

        if (team == null) {
            return;
        }

        if (team.getPlayers().contains(gamePlayer) && getStartTime() > 1L) {
            event.setTo(event.getFrom());
            event.getTo().setY(event.getFrom().getY());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

        if (gamePlayer == null) {
            return;
        }

        Team team = Hosts.getInstance().getTeamManager().getTeamByPlayer(gamePlayer);

        if (team == null) {
            return;
        }

        if (getGamePlayers().contains(gamePlayer)) {
            if (team.getPlayers().contains(gamePlayer) || team.getOpponent().getPlayers().contains(gamePlayer)) {
                gamePlayer.setStatus(GamePlayer.Status.DISCONNECTED);
                onLoss(gamePlayer);
            } else {
                Bukkit.getPluginManager().callEvent(new HostLeaveEvent(event.getPlayer(), this, HostLeaveEvent.Types.ELIMINATION));
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

        if (team.getPlayers().contains(gamePlayer) || team.getOpponent().getPlayers().contains(gamePlayer)) {
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
                if (getStartTime() > 1L) {
                    event.setCancelled(true);
                    return;
                }


                Team team = Hosts.getInstance().getTeamManager().getTeamByPlayer(gameDamager);

                if (team == null) {
                    return;
                }

                if (team.getPlayers().contains(gamePlayer) && team.getPlayers().contains(gameDamager)) {
                    gameDamager.getPlayer().sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("IS-TEAM").replace("{player}", gamePlayer.getPlayer().getDisplayName())));
                    event.setCancelled(true);
                }

                if (team.getOpponent().getPlayers().contains(gamePlayer) && team.getOpponent().getPlayers().contains(gameDamager)) {
                    gameDamager.getPlayer().sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("IS-TEAM").replace("{player}", gamePlayer.getPlayer().getDisplayName())));
                    event.setCancelled(true);
                }

                if (team.getPlayers().contains(gamePlayer) && team.getOpponent().getPlayers().contains(gameDamager)) {
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockPlace(BlockPlaceEvent event) {

        GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

        if (gamePlayer == null) {
            return;
        }

        if (getMode() != null && getMode().equalsIgnoreCase("BUILDUHC")) {
            if (getCurrentStatus() == Status.STARTING && (getGamePlayers().contains(gamePlayer) || getStartTime() > 1L)) {
                event.setCancelled(true);
            } else if (getCurrentStatus() == Status.STARTED && getGamePlayers().contains(gamePlayer)) {
                event.getBlock().setMetadata("blockBreakable", new FixedMetadataValue(Hosts.getInstance(), "isEventBlock"));
                event.setCancelled(false);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlockBreak(BlockBreakEvent event) {
        if (getCurrentStatus() == Status.STARTED) {

            GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

            if (gamePlayer == null) {
                return;
            }

            if (getCurrentStatus() == Status.STARTED && getMode() != null && getMode().equalsIgnoreCase("BUILDUHC") && getGamePlayers().contains(gamePlayer)) {
                event.setCancelled(!event.getBlock().hasMetadata("blockBreakable"));
            }
        }
    }

    @EventHandler
    private void onPlayerInteractWithSoup(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (getCurrentStatus() == Status.STARTED) {
            GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

            if (gamePlayer == null) {
                return;
            }

            if (getMode() != null && getMode().equalsIgnoreCase("Soup") && getGamePlayers().contains(gamePlayer) && event.getItem() != null && event.getItem().getType() != Material.AIR && event.getItem().getType() == GameUtils.getMaterialByVersion("SOUP")) {
                int newLife = (int) (player.getHealth() + 8.0D);
                player.setHealth(Math.min(newLife, 20));
                Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> player.setItemInHand(new ItemStack(Material.AIR)), 1L);
            }
        }
    }

}
