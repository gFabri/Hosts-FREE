package com.github.bfabri.hosts.utils;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.game.Game;
import com.github.bfabri.hosts.game.GamePlayer;
import com.github.bfabri.hosts.utils.images.ImageChar;
import com.github.bfabri.hosts.utils.images.ImageMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameUtils {

    public static String[] games = {"Sumo", "FFA", "1v1", "OITC", "Spleef", "Bridge", "Parkour", "FallIntoWater", "TNTTag", "TNTRun", "BattleRush", "BedFight", "Paintball"};

    public static String[] sumoModes = {"FFA", "Split", "2v2"};
    public static String[] ffaModes = {"Soup", "Axe", "BuildUHC", "Split"};

    public static String[] oneVSoneModes = {"Soup", "Axe", "BuildUHC", "Split"};
    public static String[] spleefModes = {"Soup", "Axe", "BuildUHC", "Split"};

    private static final Hashtable<String, String> materialByString = new Hashtable<>();

    public static Material getMaterialByVersion(String material) {
        String version = Bukkit.getServer().getBukkitVersion().split("-")[0];
        materialByString.put("STAINED", Integer.parseInt(version.split("\\.")[1]) > 18 ? "LEGACY_STAINED_GLASS_PANE" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_STAINED_GLASS_PANE" : "STAINED_GLASS_PANE");
        materialByString.put("GOLD_AXE", Integer.parseInt(version.split("\\.")[1]) > 18 ? "GOLDEN_AXE" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "GOLDEN_AXE" : "GOLD_AXE");
        materialByString.put("BED_BLOCK", Integer.parseInt(version.split("\\.")[1]) > 18 ? "RED_BED" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "RED_BED" : "BED_BLOCK");
        materialByString.put("BED_BLOCK2", Integer.parseInt(version.split("\\.")[1]) > 18 ? "RED_BED" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "RED_BED" : "BED_BLOCK");
        materialByString.put("WATER", Integer.parseInt(version.split("\\.")[1]) > 18 ? "LEGACY_STATIONARY_WATER" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_STATIONARY_WATER" : "STATIONARY_WATER");
        materialByString.put("SOUP", Integer.parseInt(version.split("\\.")[1]) > 13 ? "MUSHROOM_STEW" : Integer.parseInt(version.split("\\.")[1]) > 18 ? "MUSHROOM_STEW" : "MUSHROOM_SOUP");
        materialByString.put("ENDPORTAL", Integer.parseInt(version.split("\\.")[1]) > 18 ? "END_PORTAL_FRAME" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_ENDER_PORTAL_FRAME" : "ENDER_PORTAL_FRAME");
        materialByString.put("LEASH", Integer.parseInt(version.split("\\.")[1]) > 18 ? "LEAD" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_LEASH" : "LEASH");
        materialByString.put("DYE", Integer.parseInt(version.split("\\.")[1]) > 18 ? "LEGACY_INK_SACK" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_INK_SACK" : "INK_SACK");
        materialByString.put("CLAY", Integer.parseInt(version.split("\\.")[1]) > 18 ? "LEGACY_STAINED_CLAY" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_STAINED_CLAY" : "STAINED_CLAY");
        materialByString.put("WOOD", Integer.parseInt(version.split("\\.")[1]) > 18 ? "LEGACY_WOOD" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_WOOD" : "WOOD");
        materialByString.put("SNOW", Integer.parseInt(version.split("\\.")[1]) > 18 ? "SNOWBALL" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_SNOW_BALL" : "SNOW_BALL");
        materialByString.put("IRON_PLATE", Integer.parseInt(version.split("\\.")[1]) > 18 ? "HEAVY_WEIGHTED_PRESSURE_PLATE" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_IRON_PLATE" : "IRON_PLATE");
        materialByString.put("GOLD_PLATE", Integer.parseInt(version.split("\\.")[1]) > 18 ? "LIGHT_WEIGHTED_PRESSURE_PLATE" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_GOLD_PLATE" : "GOLD_PLATE");
        materialByString.put("SKULL", Integer.parseInt(version.split("\\.")[1]) > 18 ? "SKELETON_SKULL" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_SKULL_ITEM" : "SKULL_ITEM");
        materialByString.put("SHOVEL", Integer.parseInt(version.split("\\.")[1]) > 18 ? "DIAMOND_SHOVEL" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "LEGACY_DIAMOND_SPADE" : "DIAMOND_SPADE");
        materialByString.put("MINECART", Integer.parseInt(version.split("\\.")[1]) > 18 ? "CHEST_MINECART" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "CHEST_MINECART" : "STORAGE_MINECART");
        materialByString.put("TORCH", Integer.parseInt(version.split("\\.")[1]) > 18 ? "REDSTONE_TORCH" : Integer.parseInt(version.split("\\.")[1]) > 13 ? "REDSTONE_TORCH" : "REDSTONE_TORCH_ON");

        return Material.getMaterial(materialByString.get(material));
    }

    public static int getVersion() {
        return Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.")[1]);
    }

    public static void sendImage(Game game) throws IOException {
        List<String> messageList;
        BufferedImage imageToSend = ImageIO.read(Hosts.getInstance().getResource("Images/" + game.getName() + ".png"));
        switch (game.getCurrentStatus()) {
            case OFFLINE:
                messageList = ConfigHandler.Configs.LANG.getConfig().getStringList("ANNOUNCEMENTS.START.LINES");
                break;
            case STARTED:
                messageList = ConfigHandler.Configs.LANG.getConfig().getStringList("ANNOUNCEMENTS.STARTED.LINES");
                break;
            case STARTING:
                messageList = ConfigHandler.Configs.LANG.getConfig().getStringList("ANNOUNCEMENTS.STARTING.LINES");
                break;
            default:
                messageList = new ArrayList<>();
                break;
        }
        String player = game.getHoster().getName();
        String displayName = game.getDisplayName();
        int numPlayers = game.getGamePlayers().size();
        int maxPlayers = game.getMaxPlayers();
        String time = String.valueOf(game.getStartTime());
        for (int i = 0; i < messageList.size(); i++) {
            String message = messageList.get(i);
            message = message.replace("{time}", time);
            message = message.replace("{player}", player);
            message = message.replace("{game}", displayName);
            message = message.replace("{players}", String.valueOf(numPlayers));
            message = message.replace("{max}", String.valueOf(maxPlayers));
            messageList.set(i, message);
        }
        if (Hosts.getInstance().getConfig().getBoolean("HOST.GENERAL.HOST-IMAGE")) {
            ImageMessage imageMessage = new ImageMessage(imageToSend, 9, ImageChar.DARK_SHADE.getChar());
            imageMessage.appendText(messageList).sendToAllPlayers();
        } else {
            showMessage(messageList);
        }
    }


    private static void showMessage(List<String> list) {
            list.forEach(messages -> {
                TextComponent message = new TextComponent(Utils.translate(messages));
                if (messages.contains(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.START.EVENT-ACTIVATOR"))) {
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.START.HOVER")))).create()));
                }
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/host join"));

                Bukkit.getServer().spigot().broadcast(message);
            });
    }

    public static void sendVictoryImage(Player player, Game game) {
        Bukkit.getScheduler().runTaskAsynchronously(Hosts.getInstance(), () -> {
            try {
                BufferedImage imageToSend = ImageIO.read(new URL("https://minotar.net/avatar/" + player.getName() + ".png"));

                List<String> newList = ConfigHandler.Configs.LANG.getConfig().getStringList("ANNOUNCEMENTS.WINNER.LINES");
                newList.replaceAll(s -> s.replace("{player}", player.getName()).replace("{game}", game.getDisplayName()));

                if (Hosts.getInstance().getConfig().getBoolean("HOST.GENERAL.VICTORY-IMAGE")) {
                    ImageMessage imageMessage = new ImageMessage(imageToSend, 9, ImageChar.DARK_SHADE.getChar());
                    imageMessage.appendText(newList);
                    imageMessage.sendToAllPlayers();

                } else {
                    showVictoryMessage(newList);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void sendVictoryImageTeam(ArrayList<GamePlayer> players, Game game) {
        Random rand = new Random();
        GamePlayer randomPlayer = players.get(rand.nextInt(players.size()));
        Bukkit.getScheduler().runTaskAsynchronously(Hosts.getInstance(), () -> {
            try {
                BufferedImage imageToSend = ImageIO.read(new URL("https://minotar.net/avatar/" + randomPlayer.getPlayer().getName() + ".png"));

                List<String> newList = ConfigHandler.Configs.LANG.getConfig().getStringList("ANNOUNCEMENTS.WINNER.LINES");
                newList.replaceAll(s -> s.replace("{player}", players.stream().map(gamePlayer -> gamePlayer.getPlayer().getDisplayName()).collect(Collectors.joining(", "))).replace("{game}", game.getDisplayName()));

                if (Hosts.getInstance().getConfig().getBoolean("HOST.GENERAL.VICTORY-IMAGE")) {
                    ImageMessage imageMessage = new ImageMessage(imageToSend, 9, ImageChar.DARK_SHADE.getChar());
                    imageMessage.appendText(newList);
                    imageMessage.sendToAllPlayers();

                } else {
                    showVictoryMessage(newList);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }



    private static void showVictoryMessage(List<String> list) {
        list.stream()
                .map(messages -> new TextComponent(Utils.translate(messages)))
                .forEach(message -> Bukkit.getServer().spigot().broadcast(message));
    }

    public static void init(Game game) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int timeLeft = (int) game.getStartTime() - 1;
                game.setStartTime(timeLeft);
                if (game.getCurrentStatus() == Game.Status.OFFLINE) {
                    this.cancel();
                    return;
                }
                String announceTimes = ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TIMES");
                for (String count : announceTimes.split(",")) {
                    if (count.equalsIgnoreCase(Integer.toString(timeLeft))) {
                        try {
                            sendImage(game);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    }
                }
                if (timeLeft <= 1L) {
                    this.cancel();
                    int minPlayers = game.getMinPlayers();
                    if (game.getGamePlayers().size() < minPlayers) {
                        String announceType = ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TYPE");
                        if (announceType.equalsIgnoreCase("GLOBAL")) {
                            Bukkit.broadcastMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("MIN-PLAYERS").replace("{game}", game.getDisplayName())));
                        } else {
                            game.getGamePlayers().forEach(gamePlayer -> {
                                Player player = gamePlayer.getPlayer();
                                player.sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("MIN-PLAYERS").replace("{game}", game.getDisplayName())));
                            });
                        }
                        if (game.getHoster() instanceof Player) {
                            Hosts.getInstance().getPlayerUtil().getPlayersCooldowns().remove(((Player) game.getHoster()).getUniqueId());
                        }
                        game.onStop();
                        return;
                    }
                    game.setCurrentStatus(Game.Status.STARTED);
                    try {
                        sendImage(game);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    game.onStartedGame();
                }
            }
        }.runTaskTimer(Hosts.getInstance(), 0L, 20L);
    }


    public static void initPvPTime(Game game, ArrayList<GamePlayer> players) {
        int pvpTime = Hosts.getInstance().getConfig().getInt("HOST.Games." + game.getName() + ".pvp-time");
        game.setStartTime(pvpTime);

        BukkitTask task = new BukkitRunnable() {
            private final String announceTimes = ConfigHandler.Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TIMES");
            private final String startingMsg = Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.PVP.STARTING"));
            private final String startedMsg = Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("ANNOUNCEMENTS.PVP.STARTED"));

            @Override
            public void run() {
                int timeLeft = (int) game.getStartTime();
                game.setStartTime(--timeLeft);

                for (String count : announceTimes.split(",")) {
                    if (count.equalsIgnoreCase(Long.toString(game.getStartTime()))) {
                        players.forEach(gamePlayer -> {
                            gamePlayer.getPlayer().sendMessage(startingMsg.replace("{time}", String.valueOf(game.getStartTime())));
                            Hosts.getInstance().getPlayerUtil().sendTitle(gamePlayer.getPlayer(), startingMsg.replace("{time}", String.valueOf(game.getStartTime())), "");
                        });
                        break;
                    }
                }

                if (timeLeft <= 1L) {
                    this.cancel();
                    players.forEach(gamePlayer -> {
                        gamePlayer.getPlayer().sendMessage(startedMsg.replace("{time}", String.valueOf(game.getStartTime())));
                        Hosts.getInstance().getPlayerUtil().sendTitle(gamePlayer.getPlayer(), startedMsg.replace("{time}", String.valueOf(game.getStartTime())), "");
                    });
                }
            }
        }.runTaskTimer(Hosts.getInstance(), 0L, 20L);
        game.setGeneralTask(task);
    }

    public static void giveRewards(Game game, GamePlayer gamePlayer) {
        if (Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + game.getName().toUpperCase()) == null || Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + game.getName().toUpperCase()) == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Reward not configured!");
            return;
        }
        switch (game.getSelectedReward()) {
            case DEFAULT:
                Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + game.getName().toUpperCase()).forEach((id, reward) -> {
                    ItemStackCustom itemStackCustom = new ItemStackCustom();
                    itemStackCustom.runItems(gamePlayer.getPlayer(), reward);
                });
                break;
            case ITEMS:
                Inventory itemsRewards = Hosts.getInstance().getRewardsListener().getItemsRewards();
                for (ItemStack itemStack : itemsRewards.getContents()) {
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        continue;
                    }
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta != null && itemMeta.hasDisplayName()) {
                        String displayName = itemMeta.getDisplayName();
                        if (displayName.equalsIgnoreCase(Utils.translate("&7> &cStart Game &7<")) || displayName.equalsIgnoreCase(Utils.translate("&c."))) {
                            continue;
                        }
                    }
                    gamePlayer.getPlayer().getInventory().addItem(itemStack);
                    itemsRewards.removeItem(itemStack);
                }
                break;
            case RANDOM:
                int dropAmount = ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.RANDOM.amount-of-rewards");
                for (int i = 0; i < dropAmount; i++) {
                    ItemStackCustom itemStackCustomRandom = new ItemStackCustom();
                    itemStackCustomRandom.runItems(gamePlayer.getPlayer(), Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + game.getName().toUpperCase()).get(new Random().nextInt(Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + game.getName().toUpperCase()).size())));
                }
                break;
        }

        Inventory itemsRewards = Hosts.getInstance().getRewardsListener().getItemsRewards();
        itemsRewards.clear();
        for (int i = 27; i < 35; i++) {
            itemsRewards.setItem(i, new CustomItem(GameUtils.getMaterialByVersion("STAINED"), 1, 0).setName("&c.").create());
        }
        itemsRewards.setItem(35, new CustomItem(Material.DIAMOND, 1, 0).setName("&7> &cStart Game &7<").create());
    }

    public static void giveRewards(Game game, ArrayList<GamePlayer> gamePlayers) {
        if (Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + game.getName().toUpperCase()) == null || Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + game.getName().toUpperCase()) == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Reward not configured!");
            return;
        }
        switch (game.getSelectedReward()) {
            case DEFAULT:
                Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + game.getName().toUpperCase()).forEach((id, reward) -> {
                    gamePlayers.forEach(gamePlayer -> {
                        ItemStackCustom itemStackCustom = new ItemStackCustom();
                        itemStackCustom.runItems(gamePlayer.getPlayer(), reward);
                    });
                });
                break;
            case ITEMS:
                Inventory itemsRewards = Hosts.getInstance().getRewardsListener().getItemsRewards();
                for (ItemStack itemStack : itemsRewards.getContents()) {
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        continue;
                    }
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta != null && itemMeta.hasDisplayName()) {
                        String displayName = itemMeta.getDisplayName();
                        if (displayName.equalsIgnoreCase(Utils.translate("&7> &cStart Game &7<")) || displayName.equalsIgnoreCase(Utils.translate("&c."))) {
                            continue;
                        }
                    }
                    for (GamePlayer gamePlayer : gamePlayers) {
                        gamePlayer.getPlayer().getInventory().addItem(itemStack);
                    }
                    itemsRewards.removeItem(itemStack);
                }
                break;
            case RANDOM:
                int dropAmount = ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.RANDOM.amount-of-rewards");
                for (int i = 0; i < dropAmount; i++) {
                    ItemStackCustom itemStackCustomRandom = new ItemStackCustom();
                    itemStackCustomRandom.runItems(gamePlayers.get(i % gamePlayers.size()).getPlayer(), Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + game.getName().toUpperCase()).get(new Random().nextInt(Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + game.getName().toUpperCase()).size())));
                }
                break;
        }

        Inventory itemsRewards = Hosts.getInstance().getRewardsListener().getItemsRewards();
        itemsRewards.clear();
        for (int i = 27; i < 35; i++) {
            itemsRewards.setItem(i, new CustomItem(GameUtils.getMaterialByVersion("STAINED"), 1, 0).setName("&c.").create());
        }
        itemsRewards.setItem(35, new CustomItem(Material.DIAMOND, 1, 0).setName("&7> &cStart Game &7<").create());
    }
}
