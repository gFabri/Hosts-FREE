package com.github.bfabri.hosts.managers;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.utils.CustomItem;
import com.github.bfabri.hosts.utils.Utils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;

public class InventoryManager {

    private final HashMap<Player, Integer> taskPlayer = new HashMap<>();

    public void openHostAndReloadMenu(Player player) {

        Inventory inventory = Bukkit.createInventory(null, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.invSizes.gameSelector"), Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.gameSelector")));
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Hosts.getInstance(), () -> update(player, inventory), 0L, 20L);
        taskPlayer.put(player, task.getTaskId());

        player.openInventory(inventory);
    }

    private void update(Player player, Inventory inventory) {
        for (String cfg : ConfigHandler.Configs.CONFIG.getConfig().getConfigurationSection("HOST.Games").getKeys(false)) {
                if (ConfigHandler.Configs.CONFIG.getConfig().getBoolean("HOST.Games." + cfg + ".enabled")) {
                    ArrayList<String> newLore = new ArrayList<>();
                    ConfigHandler.Configs.ITEMS.getConfig().getStringList("HOST.games." + cfg + ".description").
                            forEach(oldLore -> newLore.add(oldLore.replace("%status%",
                                            Hosts.getInstance().getGameManager().isGameAvailable() ?
                                                    String.valueOf(Hosts.getInstance().getGameManager().getGame().getCurrentStatus()) : "OFFLINE")
                                    .replace("%cooldown%", DurationFormatUtils.formatDuration(Hosts.getInstance().getPlayerUtil().getPlayerCooldown(player), "HH:mm:ss"))));

                    inventory.setItem(ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.games." + cfg + ".slot"),
                            new CustomItem(Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.games." + cfg + ".material")) == null ? Material.DIRT :
                                    Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.games." + cfg + ".material")), 1,
                                    ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.games." + cfg + ".material-data"))
                                    .setName(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.games." + cfg + ".displayName"))
                                    .addLore(newLore).create());
                }
            }
    }

    public void closeHostMenu(Player player) {
        if (taskPlayer.containsKey(player)) {
            Bukkit.getScheduler().cancelTask(taskPlayer.get(player));
            taskPlayer.remove(player);
        }
    }

    public void openSumoModesInventory(Player player) {
        Inventory sumoModesInventory = Bukkit.createInventory(null, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.invSizes.sumoMode"),
                Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.sumoMode")));
        ConfigHandler.Configs.ITEMS.getConfig().getConfigurationSection("HOST.games.SUMO.modes").getKeys(false).forEach(modes -> {
            if (ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.games.SUMO.modes." + modes + ".enabled")) {
                Material material = Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes." + modes + ".material"));
                if (material == null) {
                    material = Material.DIRT;
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Invalid material name in sumo mode " + modes + " change in inventory.yml, check the material names for your version");
                }
                sumoModesInventory.setItem(ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes." + modes + ".slot"),
                        new CustomItem(material, 1,
                                ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes." + modes + ".material-data")).
                                setName(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes." + modes + ".displayName")).
                                addLore(ConfigHandler.Configs.ITEMS.getConfig().getStringList("HOST.games.SUMO.modes." + modes + ".description")).create());
            }
        });
        player.openInventory(sumoModesInventory);
    }

    public void openFFAModesInventory(Player player) {
        Inventory ffaModesInventory = Bukkit.createInventory(null, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.invSizes.ffaMode"), Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.ffaMode")));
        ConfigHandler.Configs.ITEMS.getConfig().getConfigurationSection("HOST.games.FFA.modes").getKeys(false).forEach(modes -> {
            if (ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.games.FFA.modes." + modes + ".enabled")) {
                Material material = Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes." + modes + ".material"));
                if (material == null) {
                    material = Material.DIRT;
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Invalid material name in ffa mode " + modes + " change in inventory.yml, check the material names for your version");
                }
                ffaModesInventory.setItem(ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes." + modes + ".slot"),
                        new CustomItem(material, 1,
                                ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes." + modes + ".material-data")).
                                setName(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes." + modes + ".displayName")).
                                addLore(ConfigHandler.Configs.ITEMS.getConfig().getStringList("HOST.games.FFA.modes." + modes + ".description")).create());
            }
        });
        player.openInventory(ffaModesInventory);
    }

    public void open1v1ModesInventory(Player player) {
        Inventory v1v1ModesInventory = Bukkit.createInventory(null, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.invSizes.1v1Mode"), Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.1v1Mode")));
        ConfigHandler.Configs.ITEMS.getConfig().getConfigurationSection("HOST.games.1v1.modes").getKeys(false).forEach(modes -> {
            if (ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.games.1v1.modes." + modes + ".enabled")) {
                Material material = Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes." + modes + ".material"));
                if (material == null) {
                    material = Material.DIRT;
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Invalid material name in 1v1 mode " + modes + " change in inventory.yml, check the material names for your version");
                }
                v1v1ModesInventory.setItem(ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes." + modes + ".slot"),
                        new CustomItem(material, 1,
                                ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes." + modes + ".material-data")).
                                setName(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes." + modes + ".displayName")).
                                addLore(ConfigHandler.Configs.ITEMS.getConfig().getStringList("HOST.games.1v1.modes." + modes + ".description")).create());
            }
        });
        player.openInventory(v1v1ModesInventory);
    }
}
