package com.github.bfabri.hosts.game.games;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.kits.Kit;
import com.github.bfabri.hosts.game.Game;
import com.github.bfabri.hosts.game.GamePlayer;
import com.github.bfabri.hosts.utils.Cuboid;
import com.github.bfabri.hosts.utils.GameUtils;
import com.github.bfabri.hosts.utils.KitItemStack;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class FFA extends Game implements Listener {

    public FFA(String name, String mode, String displayName, int minPlayers, int maxPlayers, int startTime, int maxRoundTime) {
        super(name, mode, displayName, minPlayers, maxPlayers, startTime, maxRoundTime);
    }

    public FFA() {
        super("FFA", Hosts.getInstance().getGameManager().getMode(), ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.Games.FFA.displayName"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.FFA.min-players"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.FFA.max-players"), ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.FFA.start-time"), (int) TimeUnit.MINUTES.toSeconds(ConfigHandler.Configs.CONFIG.getConfig().getInt("HOST.Games.FFA.max-round-time")));
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
            if (getMode().equalsIgnoreCase("BUILDUHC")) {
                Cuboid cuboid = new Cuboid(arena.getArea());
                cuboid.getBlocks().stream()
                        .filter(blocks -> blocks.hasMetadata("blockBreakable"))
                        .forEach(playerBlocks -> Bukkit.getScheduler().runTask(Hosts.getInstance(), () -> {
                            playerBlocks.setType(Material.AIR);
                        }));
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
            if (getMode().equalsIgnoreCase("BUILDUHC")) {
                Cuboid cuboid = new Cuboid(arena.getArea());
                cuboid.getBlocks().stream()
                        .filter(blocks -> blocks.hasMetadata("blockBreakable"))
                        .forEach(playerBlocks -> Bukkit.getScheduler().runTask(Hosts.getInstance(), () -> {
                            playerBlocks.setType(Material.AIR);
                        }));
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

            String kit = null;

            if (getMode().equalsIgnoreCase("Pot")) {
                kit = "Pot";
            } else if (getMode().equalsIgnoreCase("Gapple")) {
                kit = "Gapple";
            } else if (getMode().equalsIgnoreCase("axe")) {
                kit = "Axe";
            } else if (getMode().equalsIgnoreCase("builduhc")) {
                kit = "BuildUHC";
            } else if (getMode().equalsIgnoreCase("soup")) {
                kit = "Soup";
            }


            if (getArena().isHasKit() && Hosts.getInstance().getArenaManager().getKit(getArena(), kit) != null) {
                Kit kitGlobal = Hosts.getInstance().getArenaManager().getKit(getArena(), kit);
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

        if (getGamePlayers().contains(gamePlayer) && getStartTime() > 1L) {
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

    @EventHandler
    public void onPlayerIsInInvisibility(InventoryClickEvent event) {
        if (getCurrentStatus() == Status.STARTED) {

            GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer((Player) event.getWhoClicked());

            if (gamePlayer == null) {
                return;
            }
            if (getGamePlayers().contains(gamePlayer) && gamePlayer.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == null || event.getCurrentItem().getType() == Material.AIR)
                    return;
                if ((event.getCurrentItem().getType() == Material.DIAMOND_HELMET || event.getCurrentItem().getType() == Material.DIAMOND_CHESTPLATE || event.getCurrentItem().getType() == Material.DIAMOND_LEGGINGS || event
                        .getCurrentItem().getType() == Material.DIAMOND_BOOTS || event.getCurrentItem().getType() == Material.IRON_HELMET || event
                        .getCurrentItem().getType() == Material.IRON_CHESTPLATE || event.getCurrentItem().getType() == Material.IRON_LEGGINGS || event
                        .getCurrentItem().getType() == Material.IRON_BOOTS) && (
                        event.getClick() == ClickType.WINDOW_BORDER_RIGHT || event.getClick() == ClickType.WINDOW_BORDER_LEFT || event.getClick() == ClickType.NUMBER_KEY || event.getClick() == ClickType.DOUBLE_CLICK || event.getClick() == ClickType.UNKNOWN || event.getClick() == ClickType.MIDDLE || event.getClick() == ClickType.DROP || event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (getCurrentStatus() == Status.STARTED && getMode() != null && getMode().equalsIgnoreCase("BuildUHC")) {

            GamePlayer gamePlayer = Hosts.getInstance().getGameManager().getPlayer(event.getPlayer());

            if (gamePlayer == null) {
                return;
            }

            if (event.getBucket() != Material.WATER_BUCKET && event.getBucket() != Material.LAVA_BUCKET) {
                return;
            }
            if (getGamePlayers().contains(gamePlayer)) {
                Block element = event.getBlockClicked().getRelative(event.getBlockFace());
                element.setMetadata("blockBreakable", new FixedMetadataValue(Hosts.getInstance(), "Y"));
            }
        }
    }

    @EventHandler
    public void onConsumeGoldenHead(PlayerItemConsumeEvent event) {
        if (getCurrentStatus() == Status.STARTED && getMode() != null && getMode().equalsIgnoreCase("BuildUHC") && event.getItem() != null && event.getItem().getType() == Material.GOLDEN_APPLE && event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName() && event.getItem().getItemMeta().getDisplayName().contains("Golden Head")) {
            (new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0)).apply(event.getPlayer());
            (new PotionEffect(PotionEffectType.REGENERATION, 180, 1)).apply(event.getPlayer());
        }
    }

    @EventHandler
    public void onForm(BlockFromToEvent event) {
        if (getCurrentStatus() == Status.STARTED && getMode() != null && getMode().equalsIgnoreCase("BuildUHC") && event.getBlock().isLiquid()) {
            event.setCancelled(true);
            event.getToBlock().setMetadata("blockBreakable", new FixedMetadataValue(Hosts.getInstance(), "Y"));
        }
    }
}
