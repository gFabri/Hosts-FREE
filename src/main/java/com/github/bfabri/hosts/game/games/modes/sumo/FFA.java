package com.github.bfabri.hosts.game.games.modes.sumo;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.kits.Kit;
import com.github.bfabri.hosts.game.GamePlayer;
import com.github.bfabri.hosts.game.games.Sumo;
import com.github.bfabri.hosts.utils.GameUtils;
import com.github.bfabri.hosts.utils.KitItemStack;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FFA extends Sumo implements Listener {

    public FFA() {
        super("Sumo", "FFA", ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.Games.SUMO.displayName"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.min-players"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.max-players"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.start-time"), (int) TimeUnit.MINUTES.toSeconds(ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.SUMO.max-round-time")));
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
        Hosts.getInstance().getSpectatorManager().onFinish(this);
        Player player = gamePlayer.getPlayer();
        player.setFallDistance(0);
        player.setHealth(20);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> loadInv(gamePlayer), 10L);

        Location targetLocation = ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")
                ? Location.deserialize(this.arena.getServerSpawn())
                : gamePlayer.getLocation();
        player.teleport(targetLocation);

        GameUtils.sendVictoryImage(player, this);

        Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> GameUtils.giveRewards(this, gamePlayer), 20L);

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

        if (getGamePlayers().size() <= 1) {
            onWin(getGamePlayers().get(0));
        }
    }

    @Override
    public void onStartedGame() {

        getGamePlayers().forEach(gamePlayer -> {
            gamePlayer.getPlayer().teleport(Location.deserialize(arena.getLocations().get(0)));
            gamePlayer.getPlayer().getActivePotionEffects().forEach(type -> gamePlayer.getPlayer().removePotionEffect(type.getType()));
            gamePlayer.getPlayer().setFlying(false);
            gamePlayer.getPlayer().setAllowFlight(false);
            gamePlayer.getPlayer().setHealth(20.0D);
            gamePlayer.getPlayer().setFoodLevel(20);

            if (getArena().isHasKit() && Hosts.getInstance().getArenaManager().getKit(getArena(), "ffa") != null) {
                Kit kitGlobal = Hosts.getInstance().getArenaManager().getKit(getArena(), "ffa");
                Player player = gamePlayer.getPlayer();
                player.getInventory().setContents(KitItemStack.deserialize(kitGlobal.getInventory()));
                player.getInventory().setArmorContents(KitItemStack.deserialize(kitGlobal.getArmor()));
                player.updateInventory();
            }
        });

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

        Location playerLoc = gamePlayer.getPlayer().getLocation().getBlock().getLocation();

        for (Block block : getSurroundingBlocks(playerLoc)) {
            Material blockType = block.getType();

            if (getGamePlayers().contains(gamePlayer) && (blockType == GameUtils.getMaterialByVersion("WATER") || blockType == GameUtils.getMaterialByVersion("LAVA")) || gamePlayer.getPlayer().getLocation().getY() <= 5.0D) {
                onLoss(gamePlayer);
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

        if (gamePlayer == null) {
            return;
        }

        if (getGamePlayers().contains(gamePlayer)) {
            gamePlayer.setStatus(GamePlayer.Status.DISCONNECTED);
            onLoss(gamePlayer);
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
                    if (getGamePlayers().contains(gamePlayer)) {
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

        if (getGamePlayers().contains(gamePlayer)) {
            event.setDeathMessage(null);
            event.getDrops().forEach(itemStack -> itemStack.setType(Material.AIR));

            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> gamePlayer.getPlayer().spigot().respawn(), 0L);
            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> onLoss(gamePlayer), 1L);
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

                if (getGamePlayers().contains(gamePlayer)) {
                    event.setCancelled(false);
                    event.setDamage(0.0D);
                }
            }
        }
    }

    private List<Block> getSurroundingBlocks(Location location) {
        List<Block> blocks = new ArrayList<>();
        World world = location.getWorld();

        for (BlockFace face : BlockFace.values()) {
            Block relativeBlock = location.getBlock().getRelative(face);

            if (world != null && (face == BlockFace.DOWN || face == BlockFace.UP || face == BlockFace.SELF || face == BlockFace.NORTH || face == BlockFace.SOUTH || face == BlockFace.EAST || face == BlockFace.WEST)) {
                blocks.add(relativeBlock);
            }
        }

        return blocks;
    }
}
