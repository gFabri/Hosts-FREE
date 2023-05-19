package com.github.bfabri.hosts.listeners;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.ConfigHandler.Configs;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.game.Game;
import com.github.bfabri.hosts.utils.CustomItem;
import com.github.bfabri.hosts.utils.GameUtils;
import com.github.bfabri.hosts.utils.ItemStackCustom;
import com.github.bfabri.hosts.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class InventoryListener implements Listener {

    private Inventory configInventory;

    private final Inventory generalInventory = Bukkit.createInventory(null, 54, Utils.translate("&7> &6General Settings&7<"));
    private final Inventory gameInventory = Bukkit.createInventory(null, 54, Utils.translate("&7> &cGame Settings &7<"));

    private final Inventory rewardsInventory = Bukkit.createInventory(null, 36, Utils.translate("&7> &bRewards Settings &7<"));

    private final Inventory randomRewardsInventory = Bukkit.createInventory(null, 36, Utils.translate("&7> &bRandom Rewards Settings &7<"));

    private Inventory tempInventory = null;

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();

        if (Utils.translate(Configs.ITEMS.getConfig().getString("HOST.invTitles.gameSelector")).equals(event.getView().getTitle())) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null || !event.getCurrentItem().hasItemMeta() || !event.getCurrentItem().getItemMeta().hasDisplayName()) {
                return;
            }

            if (event.isShiftClick()) {
                if (!player.hasPermission("hosts.manager.config")) {
                    return;
                }
                String hostName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName().replace("«", "").replace("»", "").replace(" ", "").trim());
                configInventory = Bukkit.createInventory(null, 36, Utils.translate("&eConfiguration menu of &7" + hostName));

                Configs.ITEMS.getConfig().getConfigurationSection("HOST.games").getKeys(false).forEach(games -> {
                    if (games.equalsIgnoreCase(hostName)) {
                        setTempGameEditing(games);
                        configInventory.setItem(1, new CustomItem(Material.PAPER, 1, 0).setName("&7> &6General Settings &7<").create());
                        configInventory.setItem(3, new CustomItem(GameUtils.getMaterialByVersion("MINECART"), 1, 0).setName("&7> &bRewards Settings &7<").create());
                        configInventory.setItem(5, new CustomItem(GameUtils.getMaterialByVersion("MINECART"), 1, 0).setName("&7> &bRandom Rewards Settings &7<").create());
                        configInventory.setItem(7, new CustomItem(Material.BLAZE_POWDER, 1, 0).setName("&7> &cGame Settings &7<").create());

                        configInventory.setItem(19, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.CONFIG.getConfig().getBoolean("HOST.Games." + games + ".enabled")).create());
                        configInventory.setItem(21, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games." + games + ".material")) == null ? Material.DIRT :
                                Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games." + games + ".material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games." + games + ".material-data")).setName("&eIcon &c(Drag to change)").create());
                        configInventory.setItem(23, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games." + games + ".displayName")).create());
                        configInventory.setItem(25, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games." + games + ".slot")).create());
                    }
                });
                tempInventory = configInventory;
                player.openInventory(configInventory);
            } else {
                if (Hosts.getInstance().getPlayerUtil().isInCooldown(player)) {
                    player.sendMessage(Utils.translate(Configs.LANG.getConfig().getString("IN-COOLDOWN").replace("{time}", DurationFormatUtils.formatDuration(Hosts.getInstance().getPlayerUtil().getPlayerCooldown(player), "HH:mm:ss"))));
                    player.closeInventory();
                    return;
                }

                if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.slot")) {
                    Hosts.getInstance().getInventoryManager().openSumoModesInventory(player);
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.slot")) {
                    Hosts.getInstance().getInventoryManager().openFFAModesInventory(player);
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.1v1.slot")) {
                    Hosts.getInstance().getInventoryManager().open1v1ModesInventory(player);
                }
            }
        }
    }

    @EventHandler
    private void onInventoryClickInModes(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (Utils.translate(Configs.ITEMS.getConfig().getString("HOST.invTitles.sumoMode")).equals(event.getView().getTitle())) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() || !event.getCurrentItem().getItemMeta().hasDisplayName()) {
                return;
            }

            if (event.isShiftClick()) {
                if (!player.hasPermission("hosts.manager.config")) {
                    return;
                }
                setTempGameEditing("SUMO");
                configInventory = Bukkit.createInventory(null, 27, Utils.translate("&eConfiguration of &7" + ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName().replace("\u00AB", "").replace("\u00BB", "").replace(" ", "").trim())));
                if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.1v1.slot")) {
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.SUMO.modes.1v1.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes.1v1.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes.1v1.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.1v1.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes.1v1.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.1v1.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("1v1");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.2v2.slot")) {
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.SUMO.modes.2v2.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes.2v2.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes.2v2.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.2v2.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes.2v2.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.2v2.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("2v2");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.split.slot")) {
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.SUMO.modes.split.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes.split.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.split.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes.split.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.split.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("split");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.ffa.slot")) {
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.SUMO.modes.ffa.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes.ffa.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.ffa.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.SUMO.modes.ffa.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.ffa.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("ffa");
                }
            } else {
                if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.1v1.slot")) {
                    if (!player.hasPermission("hosts.game.sumo.1v1")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("Sumo", "1v1", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.2v2.slot")) {
                    if (!player.hasPermission("hosts.game.sumo.2v2")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }

                    Hosts.getInstance().getGameManager().newGameWithMode("Sumo", "2v2", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.split.slot")) {
                    if (!player.hasPermission("hosts.game.sumo.split")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("Sumo", "Split", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.SUMO.modes.ffa.slot")) {
                    if (!player.hasPermission("hosts.game.sumo.ffa")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("Sumo", "FFA", player);
                    player.closeInventory();
                }
            }
        } else if (Utils.translate(Configs.ITEMS.getConfig().getString("HOST.invTitles.ffaMode")).equals(event.getView().getTitle())) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() || !event.getCurrentItem().getItemMeta().hasDisplayName()) {
                return;
            }

            if (event.isShiftClick()) {
                if (!player.hasPermission("hosts.manager.config")) {
                    return;
                }
                setTempGameEditing("FFA");
                configInventory = Bukkit.createInventory(null, 27, Utils.translate("&eConfiguration of &7" + event.getCurrentItem().getItemMeta().getDisplayName().replace("\u00AB", "").replace("\u00BB", "").trim()));
                if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.pot.slot")) {
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games.FFA.effects-in.pot")), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.FFA.modes.pot.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.pot.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.pot.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.pot.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.pot.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.pot.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("pot");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.gapple.slot")) {
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games.FFA.effects-in.gapple")), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.FFA.modes.gapple.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.gapple.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.gapple.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.gapple.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.gapple.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.gapple.slot")).create());

                    player.openInventory(configInventory);
                    setTempModeEditing("gapple");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.axe.slot")) {
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("axe.Games.FFA.effects-in.axe")), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.FFA.modes.axe.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.axe.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.axe.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.axe.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.axe.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.axe.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("axe");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.builduhc.slot")) {
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games.FFA.effects-in.builduhc")), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.FFA.modes.builduhc.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.builduhc.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.builduhc.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.builduhc.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.builduhc.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.builduhc.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("builduhc");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.soup.slot")) {
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games.FFA.effects-in.soup")), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.FFA.modes.soup.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.soup.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.soup.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.soup.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.soup.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.soup.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("soup");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.split.slot")) {
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games.FFA.effects-in.split")), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.FFA.modes.split.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.split.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.split.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.split.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.FFA.modes.split.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.split.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("split");
                }
            } else {
                if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.pot.slot")) {
                    if (!player.hasPermission("hosts.game.ffa.pot")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("FFA", "Pot", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.gapple.slot")) {
                    if (!player.hasPermission("hosts.game.ffa.gapple")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    if (Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.NONE.enabled") || Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.ITEMS.enabled")
                            || Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.RANDOM.enabled") || Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.DEFAULT.enabled")) {
                        if (Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.REWARDS-SELECTOR")) {
                            Hosts.getInstance().getRewardsListener().openRewardsMenu(player, "FFA", "Gapple");
                            return;
                        }
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("FFA", "Gapple", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.axe.slot")) {
                    if (!player.hasPermission("hosts.game.ffa.axe")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    if (Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.NONE.enabled") || Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.ITEMS.enabled")
                            || Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.RANDOM.enabled") || Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.DEFAULT.enabled")) {
                        if (Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.REWARDS-SELECTOR")) {
                            Hosts.getInstance().getRewardsListener().openRewardsMenu(player, "FFA", "Axe");
                            return;
                        }
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("FFA", "Axe", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.builduhc.slot")) {
                    if (!player.hasPermission("hosts.game.ffa.builduhc")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    if (Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.NONE.enabled") || Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.ITEMS.enabled")
                            || Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.RANDOM.enabled") || Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.DEFAULT.enabled")) {
                        if (Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.REWARDS-SELECTOR")) {
                            Hosts.getInstance().getRewardsListener().openRewardsMenu(player, "FFA", "BuildUHC");
                            return;
                        }
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("FFA", "BuildUHC", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.soup.slot")) {
                    if (!player.hasPermission("hosts.game.ffa.soup")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("FFA", "Soup", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.FFA.modes.split.slot")) {
                    if (!player.hasPermission("hosts.game.ffa.split")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("FFA", "Split", player);
                    player.closeInventory();
                }
            }
        } else if (Utils.translate(Configs.ITEMS.getConfig().getString("HOST.invTitles.1v1Mode")).equals(event.getView().getTitle())) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() || !event.getCurrentItem().getItemMeta().hasDisplayName()) {
                return;
            }

            if (event.isShiftClick()) {
                if (!player.hasPermission("hosts.manager.config")) {
                    return;
                }
                setTempGameEditing("1v1");
                configInventory = Bukkit.createInventory(null, 27, Utils.translate("&eConfiguration of &7" + event.getCurrentItem().getItemMeta().getDisplayName().replace("\u00AB", "").replace("\u00BB", "").trim()));

                if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.pot.slot")) {
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games.1v1.effects-in.pot")), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.1v1.modes.pot.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.pot.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.pot.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.pot.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.pot.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.pot.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("pot");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.gapple.slot")) {
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games.1v1.effects-in.gapple")), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.1v1.modes.gapple.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.gapple.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.gapple.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.gapple.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.gapple.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.gapple.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("gapple");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.axe.slot")) {
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games.1v1.effects-in.axe")), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.1v1.modes.axe.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.axe.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.axe.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.axe.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.axe.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.axe.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("axe");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.builduhc.slot")) {
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games.1v1.effects-in.builduhc")), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.1v1.modes.builduhc.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.builduhc.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.builduhc.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.builduhc.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.builduhc.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.builduhc.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("builduhc");
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.soup.slot")) {
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games.1v1.effects-in.soup")), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games.1v1.modes.soup.enabled")).create());
                    configInventory.setItem(12, new CustomItem(Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.soup.material")) == null ? Material.DIRT :
                            Material.getMaterial(Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.soup.material")), 1, Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.soup.material-data")).setName("&eIcon &c(Drag to change)").create());
                    configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games.1v1.modes.soup.displayName")).create());
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.soup.slot")).create());

                    tempInventory = configInventory;
                    player.openInventory(configInventory);
                    setTempModeEditing("soup");
                }
            } else {
                if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.pot.slot")) {
                    if (!player.hasPermission("hosts.game.1v1.pot")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("1v1", "Pot", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.gapple.slot")) {
                    if (!player.hasPermission("hosts.game.1v1.gapple")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("1v1", "Gapple", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.axe.slot")) {
                    if (!player.hasPermission("hosts.game.1v1.axe")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("1v1", "Axe", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.builduhc.slot")) {
                    if (!player.hasPermission("hosts.game.1v1.builduhc")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("1v1", "BuildUHC", player);
                    player.closeInventory();
                } else if (event.getSlot() == Configs.ITEMS.getConfig().getInt("HOST.games.1v1.modes.soup.slot")) {
                    if (!player.hasPermission("hosts.game.1v1.soup")) {
                        player.sendMessage(Utils.PREFIX + Utils.translate(Configs.LANG.getConfig().getString("NO-PERMISSION")));
                        return;
                    }
                    Hosts.getInstance().getGameManager().newGameWithMode("1v1", "Soup", player);
                    player.closeInventory();
                }
            }
        }
    }

    @Getter
    @Setter
    private String tempGameEditing = null;
    @Getter
    @Setter
    private String tempModeEditing = null;

    private final HashMap<Player, String> chatConfig = new HashMap<>();

    @EventHandler
    private void onInventoryClickConfig(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() || !event.getCurrentItem().getItemMeta().hasDisplayName()) {
            return;
        }

        if (event.getView().getTitle().startsWith(Utils.translate("&eConfiguration of"))) {
            event.setCancelled(true);
            if (event.getSlot() == 4) {
                if (event.isLeftClick()) {
                    chatConfig.put(player, "newPotion");
                    player.sendMessage(Utils.translate("&eAvailables Effects&7: &f" + Arrays.stream(PotionEffectType.values()).filter(Objects::nonNull).map(PotionEffectType::getName).collect(Collectors.joining("&e, &f"))));
                    player.sendMessage(Utils.translate("&cType effect with this example&7: &4EFFECT:LEVEL &c| Example&7: &cSPEED:2"));
                    player.closeInventory();
                } else if (event.isRightClick()) {
                    chatConfig.put(player, "removePotion");
                    player.sendMessage(Utils.translate("&cType index of effect"));
                    player.closeInventory();
                }
            } else if (event.getSlot() == 10) {
                Configs.ITEMS.getConfig().set("HOST.games." + getTempGameEditing() + ".modes." + getTempModeEditing() + ".enabled", !Configs.ITEMS.getConfig().getBoolean("HOST.games." + getTempGameEditing() + ".modes." + getTempGameEditing() + ".enabled"));
                Configs.ITEMS.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eEnabled&7: " + Configs.ITEMS.getConfig().getBoolean("HOST.games." + getTempGameEditing() + ".modes." + getTempModeEditing() + ".enabled")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 12) {
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    event.getCursor().setItemMeta(meta);
                    event.setCurrentItem(event.getCursor());
                    Configs.ITEMS.getConfig().set("HOST.games." + getTempGameEditing() + ".modes." + getTempModeEditing() + ".material", event.getCursor().getType().toString());
                    if (event.getCursor().getType().toString().equalsIgnoreCase("POTION")) {
                        Configs.ITEMS.getConfig().set("HOST.games." + getTempGameEditing() + ".modes." + getTempModeEditing() + ".material-data", event.getCursor().getDurability());
                    } else {
                        Configs.ITEMS.getConfig().set("HOST.games." + getTempGameEditing() + ".modes." + getTempModeEditing() + ".material-data", event.getCursor().getData().getData());
                    }
                    Configs.ITEMS.saveConfig();
                    event.setCursor(null);
                }
            } else if (event.getSlot() == 14) {
                chatConfig.put(player, "displayNameMode");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type displayName"));
            } else if (event.getSlot() == 16) {
                chatConfig.put(player, "slotMode");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type slot"));
            }
        } else if (event.getView().getTitle().startsWith(Utils.translate("&eConfiguration menu of"))) {
            event.setCancelled(true);

            if (event.getSlot() == 1) {
                generalInventory.setItem(10, new CustomItem(Material.BLAZE_POWDER, 1, 0).setName("&eSound on start&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.SOUND-ON-START")).create());
                generalInventory.setItem(12, new CustomItem(Material.LEVER, 1, 0).setName("&eAnnounce Type&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TYPE")).create());
                generalInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eAnnounce times&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TIMES")).create());
                generalInventory.setItem(16, new CustomItem(Material.LEVER, 1, 0).setName("&eHost image&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.HOST-IMAGE")).create());
                generalInventory.setItem(19, new CustomItem(Material.LEVER, 1, 0).setName("&eVictory Image&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.VICTORY-IMAGE")).create());
                generalInventory.setItem(21, new CustomItem(Material.LEVER, 1, 0).setName("&eRewards selector&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.REWARDS-SELECTOR")).create());
                generalInventory.setItem(23, new CustomItem(Material.LEVER, 1, 0).setName("&eSpectator&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.SPECTATOR")).create());
                generalInventory.setItem(25, new CustomItem(Material.LEVER, 1, 0).setName("&eArena selector&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.ARENA-SELECTOR")).create());
                generalInventory.setItem(30, new CustomItem(Material.LEVER, 1, 0).setName("&eTeleport to spawn on end&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")).create());
                generalInventory.setItem(32, new CustomItem(Material.LEVER, 1, 0).setName("&eAllow Items&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.ALLOW-ITEMS")).create());

                List<String> customList = new ArrayList<>();

                customList.add(Utils.translate("&7&lPlaceholders"));
                customList.add(" ");
                customList.add(Utils.translate("&6{player}"));
                customList.add(" ");
                AtomicInteger i = new AtomicInteger();
                Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.COMMANDS-ON-JOIN").forEach(element -> customList.add(Utils.translate("&a" + i.getAndIncrement() + " &f" + element)));
                customList.add(" ");
                customList.add(Utils.translate("&aShift Left click to add item"));
                customList.add(Utils.translate("&cShift Right click to remove item"));

                generalInventory.setItem(48, new CustomItem(GameUtils.getMaterialByVersion("MINECART"), 1, 0).setName("&eCommands on join&7: &6").addLore(customList).create());

                List<String> customList2 = new ArrayList<>();

                customList2.add(" ");
                AtomicInteger i2 = new AtomicInteger();
                Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.ALLOWED-COMMANDS").forEach(element -> customList2.add(Utils.translate("&a" + i2.getAndIncrement() + " &f" + element)));
                customList2.add(" ");
                customList2.add(Utils.translate("&aShift Left click to add item"));
                customList2.add(Utils.translate("&cShift Right click to remove item"));

                generalInventory.setItem(50, new CustomItem(GameUtils.getMaterialByVersion("MINECART"), 1, 0).setName("&eAllowed commands&7: &6").addLore(customList2).create());

                player.openInventory(generalInventory);
            } else if (event.getSlot() == 3) {
                rewardsInventory.clear();

                for (int i = 27; i < 34; i++) {
                    this.rewardsInventory.setItem(i, new CustomItem(GameUtils.getMaterialByVersion("STAINED"), 1, 0).setName("&c.").create());
                }

                this.rewardsInventory.setItem(34, new CustomItem(Material.TORCH, 1, 0).setName("&7> &cEdit items &7<").create());
                this.rewardsInventory.setItem(35, new CustomItem(Material.DIAMOND, 1, 0).setName("&7> &cSave &7<").create());

                if (Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing().toUpperCase()) != null) {
                    Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing().toUpperCase()).forEach((slot, item) -> {
                        if (item != null) {
                            ItemStack itemStack = ItemStackCustom.deserialize(item);
                            ItemMeta meta = itemStack.getItemMeta();

                            List<String> lore = new ArrayList<>();

                            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
                                lore = itemStack.getItemMeta().getLore();
                            }

                            lore.add(" ");
                            lore.add(Utils.translate("&eUse commands&7: " + item.get("useCommand")));
                            lore.add(" ");
                            lore.add(Utils.translate("&aLeft click to add commands on this item"));
                            lore.add(Utils.translate("&cRight click to remove commands on this item"));
                            lore.add(Utils.translate("&aShift left click to change enable or disable commands on this item"));
                            lore.add(Utils.translate("&eMiddle click to view list of commands"));

                            meta.setLore(lore);

                            itemStack.setItemMeta(meta);

                            rewardsInventory.setItem(slot, itemStack);
                        }
                    });
                }

                player.openInventory(rewardsInventory);
            } else if (event.getSlot() == 5) {
                randomRewardsInventory.clear();
                for (int i = 27; i < 33; i++) {
                    this.randomRewardsInventory.setItem(i, new CustomItem(GameUtils.getMaterialByVersion("STAINED"), 1, 0).setName("&c.").create());
                }

                this.randomRewardsInventory.setItem(33, new CustomItem(Material.TORCH, 1, 0).setName("&7> &cEdit items &7<").create());
                this.randomRewardsInventory.setItem(34, new CustomItem(Material.PAPER, 1, 0).setName("&7> &eAmount of Rewards&7: &7" + Configs.ITEMS.getConfig().getInt("HOST.rewards.types.RANDOM.amount-of-rewards") + " &7<").create());
                this.randomRewardsInventory.setItem(35, new CustomItem(Material.DIAMOND, 1, 0).setName("&7> &cSave &7<").create());

                if (Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing().toUpperCase()) != null) {
                    Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing().toUpperCase()).forEach((slot, item) -> {
                        if (item != null) {
                            ItemStack itemStack = ItemStackCustom.deserialize(item);
                            ItemMeta meta = itemStack.getItemMeta();

                            List<String> lore = new ArrayList<>();

                            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
                                lore = itemStack.getItemMeta().getLore();
                            }

                            lore.add(" ");
                            lore.add(Utils.translate("&eUse commands&7: " + item.get("useCommand")));
                            lore.add(" ");
                            lore.add(Utils.translate("&aLeft click to add commands on this item"));
                            lore.add(Utils.translate("&cRight click to remove commands on this item"));
                            lore.add(Utils.translate("&aShift left click to change enable or disable commands on this item"));
                            lore.add(Utils.translate("&eMiddle click to view list of commands"));

                            meta.setLore(lore);

                            itemStack.setItemMeta(meta);

                            randomRewardsInventory.setItem(slot, itemStack);
                        }
                    });
                }
                player.openInventory(randomRewardsInventory);
            } else if (event.getSlot() == 7) {

                gameInventory.clear();
                gameInventory.setItem(10, new CustomItem(Material.NAME_TAG, 1,0).setName("&eDisplayName&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".displayName")).create());
                gameInventory.setItem(12, new CustomItem(Material.ENDER_CHEST, 1,0).setName("&eMin Players&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".min-players")).addLore(" ", "&aLeft click to increase slot", "&cRight click to decrease slot").create());
                gameInventory.setItem(14, new CustomItem(Material.ENDER_CHEST, 1,0).setName("&eMax Players&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".max-players")).addLore(" ", "&aLeft click to increase slot", "&cRight click to decrease slot").create());
                gameInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1,0).setName("&eStart Time&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".start-time")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());
                gameInventory.setItem(19, new CustomItem(Material.ENDER_CHEST, 1,0).setName("&eDefault cooldown&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".default-cooldown")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());

                switch (getTempGameEditing()) {
                    case "SUMO":
                        gameInventory.setItem(23, new CustomItem(Material.ENDER_CHEST, 1,0).setName("&eMax round time&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".max-round-time")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());
                        gameInventory.setItem(21, new CustomItem(Material.ENDER_CHEST, 1,0).setName("&ePvP time&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".pvp-time")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());
                        gameInventory.setItem(25, new CustomItem(Material.ENDER_CHEST, 1,0).setName("&eTeam creation&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".team-creation")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());
                        break;
                    case "FFA":
                        gameInventory.setItem(21, new CustomItem(Material.ENDER_CHEST, 1,0).setName("&ePvP time&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".pvp-time")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());
                        break;
                    case "1v1":
                        gameInventory.setItem(23, new CustomItem(Material.ENDER_CHEST, 1,0).setName("&eMax round time&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".max-round-time")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());
                        gameInventory.setItem(21, new CustomItem(Material.ENDER_CHEST, 1,0).setName("&ePvP time&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".pvp-time")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());
                        break;
                }

                tempInventory = gameInventory;
                player.openInventory(gameInventory);
            } else if (event.getSlot() == 19) {
                Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".enabled", !Configs.CONFIG.getConfig().getBoolean("HOST.Games." + getTempGameEditing() + ".enabled"));
                Configs.CONFIG.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eEnabled&7: " + Configs.CONFIG.getConfig().getBoolean("HOST.Games." + getTempGameEditing() + ".enabled")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 21) {
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    event.getCursor().setItemMeta(meta);
                    event.setCurrentItem(event.getCursor());
                    Configs.ITEMS.getConfig().set("HOST.games." + getTempGameEditing() + ".material", event.getCursor().getType().toString());
                    if (event.getCursor().getType().toString().equalsIgnoreCase("POTION")) {
                        Configs.ITEMS.getConfig().set("HOST.games." + getTempGameEditing() + ".material-data", event.getCursor().getDurability());
                    } else {
                        Configs.ITEMS.getConfig().set("HOST.games." + getTempGameEditing() + ".material-data", event.getCursor().getData().getData());
                    }
                    Configs.ITEMS.saveConfig();
                    event.setCursor(null);
                }
            } else if (event.getSlot() == 23) {
                chatConfig.put(player, "displayNameGame");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type displayName"));
            } else if (event.getSlot() == 25) {
                chatConfig.put(player, "slotGame");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type slot"));
            }
        }
    }

    @EventHandler
    private void onClickGeneral(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() || !event.getCurrentItem().getItemMeta().hasDisplayName()) {
            return;
        }

        if (event.getView().getTitle().startsWith(Utils.translate("&7> &6General"))) {
            event.setCancelled(true);
            if (event.getSlot() == 10) {
                chatConfig.put(player, "soundName");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type Sound name"));
            } else if (event.getSlot() == 12) {
                Configs.CONFIG.getConfig().set("HOST.GENERAL.ANNOUNCE-TYPE", Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TYPE").equalsIgnoreCase("GAME") ? "GLOBAL" : "GAME");
                Configs.CONFIG.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eAnnounce Type&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TYPE")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 14) {
                chatConfig.put(player, "announceName");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type announce times with ,"));
            } else if (event.getSlot() == 16) {
                Configs.CONFIG.getConfig().set("HOST.GENERAL.HOST-IMAGE", !Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.HOST-IMAGE"));
                Configs.CONFIG.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eHost image&7: &6" + Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.HOST-IMAGE")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 19) {
                Configs.CONFIG.getConfig().set("HOST.GENERAL.VICTORY-IMAGE", !Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.VICTORY-IMAGE"));
                Configs.CONFIG.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eVictory Image&7: &6" + Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.VICTORY-IMAGE")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 21) {
                Configs.CONFIG.getConfig().set("HOST.GENERAL.REWARDS-SELECTOR", !Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.REWARDS-SELECTOR"));
                Configs.CONFIG.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eRewards selector&7: &6" + Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.REWARDS-SELECTOR")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 23) {
                Configs.CONFIG.getConfig().set("HOST.GENERAL.SPECTATOR", !Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.SPECTATOR"));
                Configs.CONFIG.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eSpectator&7: &6" + Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.SPECTATOR")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 25) {
                Configs.CONFIG.getConfig().set("HOST.GENERAL.ARENA-SELECTOR", !Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.ARENA-SELECTOR"));
                Configs.CONFIG.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eArena selector&7: &6" + Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.ARENA-SELECTOR")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 30) {
                Configs.CONFIG.getConfig().set("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END", !Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END"));
                Configs.CONFIG.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eTeleport to spawn on end&7: &6" + Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.TELEPORT-TO-SPAWN-ON-END")));
                event.getCurrentItem().setItemMeta(meta);
            }  else if (event.getSlot() == 32) {
                Configs.CONFIG.getConfig().set("HOST.GENERAL.ALLOW-ITEMS", !Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.ALLOW-ITEMS"));
                Configs.CONFIG.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eAllow Items&7: &6" + Configs.CONFIG.getConfig().getBoolean("HOST.GENERAL.ALLOW-ITEMS")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 48) {
                if (event.isShiftClick() && event.isLeftClick()) {
                    chatConfig.put(player, "addItemOnJoin");
                    player.closeInventory();
                    player.sendMessage(Utils.translate("&ePlease input value"));
                } else if (event.isShiftClick() && event.isRightClick()) {
                    chatConfig.put(player, "removeItemOnJoin");
                    player.closeInventory();
                    player.sendMessage(Utils.translate("&ePlease input index value"));
                }
            } else if (event.getSlot() == 50) {
                if (event.isShiftClick() && event.isLeftClick()) {
                    chatConfig.put(player, "addItemOnAllowed");
                    player.closeInventory();
                    player.sendMessage(Utils.translate("&ePlease input value"));
                } else if (event.isShiftClick() && event.isRightClick()) {
                    chatConfig.put(player, "removeItemOnAllowed");
                    player.closeInventory();
                    player.sendMessage(Utils.translate("&ePlease input index value"));
                }
            }
        }
    }

    private boolean editableRewards = false;

    private boolean editableRandomRewards = false;

    @EventHandler
    private void onGameClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null) {
            return;
        }

        if (event.getView().getTitle().equalsIgnoreCase(Utils.translate("&7> &cGame Settings &7<"))) {
            event.setCancelled(true);

            if (event.getSlot() == 10) {
                chatConfig.put(player, "gameDisplayName");
                player.sendMessage(Utils.translate("&ePlease input value"));
                player.closeInventory();
            }

            if (event.getSlot() == 12) {
                int i = Integer.parseInt(Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".min-players"));
                if (event.isLeftClick()) {
                    i++;
                    Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".min-players", i);
                    Configs.CONFIG.saveConfig();
                } else if (event.isRightClick()) {
                    i--;
                    if (i < 0) {
                        return;
                    }
                    Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".min-players", i);
                    Configs.CONFIG.saveConfig();
                }
                gameInventory.setItem(12, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eMin Players&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".min-players")).addLore(" ", "&aLeft click to increase slot", "&cRight click to decrease slot").create());
            } else if (event.getSlot() == 14) {
                int i = Integer.parseInt(Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".max-players"));
                if (event.isLeftClick()) {
                    i++;
                    Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".max-players", i);
                    Configs.CONFIG.saveConfig();
                } else if (event.isRightClick()) {
                    i--;
                    if (i < 0) {
                        return;
                    }
                    Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".max-players", i);
                    Configs.CONFIG.saveConfig();
                }
                gameInventory.setItem(14, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eMax Players&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".max-players")).addLore(" ", "&aLeft click to increase slot", "&cRight click to decrease slot").create());
            } else if (event.getSlot() == 16) {
                int i = Integer.parseInt(Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".start-time"));
                if (event.isLeftClick()) {
                    i++;
                    Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".start-time", i);
                    Configs.CONFIG.saveConfig();
                } else if (event.isRightClick()) {
                    i--;
                    if (i < 0) {
                        return;
                    }
                    Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".start-time", i);
                    Configs.CONFIG.saveConfig();
                }
                gameInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eStart Time&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".start-time")).addLore(" ", "&aLeft click to increase slot", "&cRight click to decrease slot").create());
            } else if (event.getSlot() == 19) {
                int i = Integer.parseInt(Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".default-cooldown"));
                if (event.isLeftClick()) {
                    i++;
                    Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".default-cooldown", i);
                    Configs.CONFIG.saveConfig();
                } else if (event.isRightClick()) {
                    i--;
                    if (i < 0) {
                        return;
                    }
                    Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".default-cooldown", i);
                    Configs.CONFIG.saveConfig();
                }
                gameInventory.setItem(19, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eDefault cooldown&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".default-cooldown")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());
            } else if (event.getSlot() == 21) {
                switch (getTempGameEditing()) {
                    case "SUMO":
                    case "FFA":
                    case "1v1":
                        int i = Integer.parseInt(Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".pvp-time"));
                        if (event.isLeftClick()) {
                            i++;
                            Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".pvp-time", i);
                            Configs.CONFIG.saveConfig();
                        } else if (event.isRightClick()) {
                            i--;
                            if (i < 0) {
                                return;
                            }
                            Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".pvp-time", i);
                            Configs.CONFIG.saveConfig();
                        }
                        gameInventory.setItem(21, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&ePvP time&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".pvp-time")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());
                        break;
                }
            } else if (event.getSlot() == 23) {
                switch (getTempGameEditing()) {
                    case "SUMO":
                    case "1v1":
                        int i = Integer.parseInt(Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".max-round-time"));
                        if (event.isLeftClick()) {
                            i++;
                            Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".max-round-time", i);
                            Configs.CONFIG.saveConfig();
                        } else if (event.isRightClick()) {
                            i--;
                            if (i < 0) {
                                return;
                            }
                            Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".max-round-time", i);
                            Configs.CONFIG.saveConfig();
                        }
                        gameInventory.setItem(23, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eMax round time&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".max-round-time")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());
                        break;
                }
            } else if (event.getSlot() == 25) {
                switch (getTempGameEditing()) {
                    case "SUMO":
                        int i = Integer.parseInt(Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".team-creation"));
                        if (event.isLeftClick()) {
                            i++;
                            Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".team-creation", i);
                            Configs.CONFIG.saveConfig();
                        } else if (event.isRightClick()) {
                            i--;
                            if (i < 0) {
                                return;
                            }
                            Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".team-creation", i);
                            Configs.CONFIG.saveConfig();
                        }
                        gameInventory.setItem(25, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eTeam creation&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".team-creation")).addLore(" ", "&aLeft click to increase time", "&cRight click to decrease time").create());
                        break;
                }
            }
        }
    }

    @EventHandler
    private void onClickInRewards(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null) {
            return;
        }

        if (event.getView().getTitle().equalsIgnoreCase(Utils.translate("&7> &bRewards Settings &7<"))) {
            event.setCancelled(!editableRewards);

            if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName()) {
                if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cSave &7<"))) {
                    event.setCancelled(true);

                    HashMap<Integer, Map<String, Object>> item = new HashMap<>();
                    for (int i = 0; i < rewardsInventory.getSize(); i++) {

                        if (rewardsInventory.getItem(i) != null) {

                            ItemStack itemStack = rewardsInventory.getItem(i);

                            if (itemStack.getType() != Material.AIR) {
                                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cSave &7<"))) {
                                    continue;
                                }
                                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&c."))) {
                                    continue;
                                }
                                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cEdit items &7<"))) {
                                    continue;
                                }
                            }

                            ItemStackCustom itemWork = new ItemStackCustom(itemStack);
                            if (itemWork.getLore() != null && itemWork.getLore().size() > 7) {
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                            }
                            item.put(i, itemWork.serialize());
                            Hosts.getInstance().getRewardsManager().getRewards().put("Rewards" + getTempGameEditing().toUpperCase(), item);
                        } else {
                            item.put(i, null);
                            Hosts.getInstance().getRewardsManager().getRewards().put("Rewards" + getTempGameEditing().toUpperCase(), item);
                        }
                        Hosts.getInstance().getRewardsManager().save();
                    }
                    player.closeInventory();
                    player.openInventory(configInventory);
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&c."))) {
                    event.setCancelled(true);
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cEdit items &7<"))) {
                    event.setCancelled(true);
                    editableRewards = !editableRewards;
                    if (editableRewards) {
                        for (int i = 0; i < rewardsInventory.getSize(); i++) {
                            if (rewardsInventory.getItem(i) != null) {
                                if (rewardsInventory.getItem(i).hasItemMeta() && rewardsInventory.getItem(i).getItemMeta().hasLore()) {
                                    ItemStack stack = rewardsInventory.getItem(i);
                                    ItemMeta meta = stack.getItemMeta();

                                    List<String> lore = meta.getLore();

                                    if (stack.hasItemMeta() && stack.getItemMeta().hasLore()) {
                                        lore = stack.getItemMeta().getLore();
                                    }

                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    meta.setLore(lore);
                                    stack.setItemMeta(meta);
                                    rewardsInventory.setItem(i, stack);
                                }
                            }
                        }
                        this.rewardsInventory.setItem(34, new CustomItem(GameUtils.getMaterialByVersion("TORCH"), 1, 0).setName("&7> &cEdit items &7<").create());
                    } else {
                        for (int i = 0; i < rewardsInventory.getSize(); i++) {
                            if (rewardsInventory.getItem(i) != null) {
                                ItemStack stack = rewardsInventory.getItem(i);
                                ItemMeta meta = stack.getItemMeta();
                                boolean useCommand = false;
                                if (Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()) != null) {
                                    if (Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(i) != null) {
                                        useCommand = (boolean) Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(i).get("useCommand");
                                    }
                                }
                                List<String> lore = new ArrayList<>();

                                if (stack.hasItemMeta() && stack.getItemMeta().hasLore()) {
                                    lore = stack.getItemMeta().getLore();
                                }

                                lore.add(" ");
                                lore.add(Utils.translate("&eUse commands&7: " + useCommand));
                                lore.add(" ");
                                lore.add(Utils.translate("&aLeft click to add commands on this item"));
                                lore.add(Utils.translate("&cRight click to remove commands on this item"));
                                lore.add(Utils.translate("&aShift left click to change enable or disable commands on this item"));
                                lore.add(Utils.translate("&eMiddle click to view list of commands"));
                                meta.setLore(lore);
                                rewardsInventory.getItem(i).setItemMeta(meta);
                                rewardsInventory.setItem(i, stack);
                            }
                        }
                        this.rewardsInventory.setItem(34, new CustomItem(Material.TORCH, 1, 0).setName("&7> &cEdit items &7<").create());
                        Hosts.getInstance().getRewardsManager().save();
                    }
                }
            }
            if (!editableRewards) {

                if (event.getCurrentItem().getType() == Material.AIR) {
                    return;
                }
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cSave &7<"))) {
                    return;
                }
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&c."))) {
                    return;
                }
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cEdit items &7<"))) {
                    return;
                }

                if (event.isShiftClick() && event.isLeftClick() && event.getClickedInventory() != player.getInventory()) {
                    boolean useCommand = (boolean) Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(event.getSlot()).get("useCommand");
                    Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(event.getSlot()).put("useCommand", !useCommand);
                    Hosts.getInstance().getRewardsManager().save();

                    ItemMeta meta = event.getCurrentItem().getItemMeta();

                    List<String> lore = new ArrayList<>();

                    if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasLore()) {
                        lore = event.getCurrentItem().getItemMeta().getLore();
                    }

                    int index = 0;

                    for (String lines : lore) {
                        if (lines.startsWith(Utils.translate("&eUse commands"))) {
                            lore.set(index, Utils.translate("&eUse commands&7: " + !useCommand));
                        }
                        index++;
                    }

                    meta.setLore(lore);
                    event.getCurrentItem().setItemMeta(meta);
                } else if (event.isLeftClick() && event.getClickedInventory() != player.getInventory()) {
                    if (Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(event.getSlot()) == null) {
                        player.sendMessage(ChatColor.RED + "Save first");
                        return;
                    }
                    boolean useCommand = (boolean) Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(event.getSlot()).get("useCommand");
                    if (useCommand) {
                        chatConfig.put(player, "commandAddRewardI" + event.getSlot());
                        player.sendMessage(Utils.translate("&ePlease input value"));
                        player.closeInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + "This feature is disabled!");
                    }
                } else if (event.isRightClick() && event.getClickedInventory() != player.getInventory()) {
                    if (Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(event.getSlot()) == null) {
                        player.sendMessage(ChatColor.RED + "Save first");
                        return;
                    }

                    boolean useCommand = (boolean) Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(event.getSlot()).get("useCommand");
                    if (useCommand) {
                        chatConfig.put(player, "commandRemoveRewardI" + event.getSlot());
                        player.sendMessage(Utils.translate("&ePlease type index"));
                        player.closeInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + "This feature is disabled!");
                    }
                } else if (event.getClick().equals(ClickType.MIDDLE)) {
                    if (Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(event.getSlot()) == null) {
                        player.sendMessage(ChatColor.RED + "Save first");
                        return;
                    }
                    boolean useCommand = (boolean) Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(event.getSlot()).get("useCommand");
                    if (useCommand) {
                        ArrayList<String> commands = (ArrayList<String>) Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(event.getSlot()).get("commands");
                        player.sendMessage(Utils.translate("&e" + commands.stream().collect(Collectors.joining("&7, &e"))));
                    } else {
                        player.sendMessage(ChatColor.RED + "This feature is disabled!");
                    }
                }
            }
        } else if (event.getView().getTitle().equalsIgnoreCase(Utils.translate("&7> &bRandom Rewards Settings &7<"))) {
            event.setCancelled(!editableRandomRewards);
            if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName()) {
                if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cSave &7<"))) {
                    event.setCancelled(true);

                    HashMap<Integer, Map<String, Object>> item = new HashMap<>();

                    for (int i = 0; i < randomRewardsInventory.getSize(); i++) {

                        if (randomRewardsInventory.getItem(i) != null) {

                            ItemStack itemStack = randomRewardsInventory.getItem(i);

                            if (itemStack.getType() != Material.AIR) {
                                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cSave &7<"))) {
                                    continue;
                                }
                                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().startsWith(Utils.translate("&7> &eAmount of"))) {
                                    continue;
                                }
                                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&c."))) {
                                    continue;
                                }
                                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cEdit items &7<"))) {
                                    continue;
                                }
                            }

                            ItemStackCustom itemWork = new ItemStackCustom(itemStack);
                            if (itemWork.getLore() != null && itemWork.getLore().size() > 7) {
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                                itemWork.getLore().remove(itemWork.getLore().size() - 1);
                            }
                            item.put(i, itemWork.serialize());
                            Hosts.getInstance().getRewardsManager().getRewards().put("RandomRewards" + getTempGameEditing().toUpperCase(), item);
                        } else {
                            item.put(i, null);
                            Hosts.getInstance().getRewardsManager().getRewards().put("RandomRewards" + getTempGameEditing().toUpperCase(), item);
                            Hosts.getInstance().getRewardsManager().save();
                        }
                        player.closeInventory();
                        player.openInventory(configInventory);
                    }
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&c."))) {
                    event.setCancelled(true);
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().startsWith(Utils.translate("&7> &eAmount of Rewards"))) {
                    chatConfig.put(player, "randomRewards");
                    player.closeInventory();
                    player.sendMessage(Utils.translate("&ePlease type amount of rewards"));
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cEdit items &7<"))) {
                    event.setCancelled(true);
                    editableRandomRewards = !editableRandomRewards;
                    if (editableRandomRewards) {
                        for (int i = 0; i < randomRewardsInventory.getSize(); i++) {
                            if (randomRewardsInventory.getItem(i) != null) {
                                if (randomRewardsInventory.getItem(i).hasItemMeta() && randomRewardsInventory.getItem(i).getItemMeta().hasLore()) {
                                    ItemStack stack = randomRewardsInventory.getItem(i);
                                    ItemMeta meta = stack.getItemMeta();

                                    List<String> lore = new ArrayList<>();

                                    if (stack.hasItemMeta() && stack.getItemMeta().hasLore()) {
                                        lore = stack.getItemMeta().getLore();
                                    }

                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);
                                    lore.remove(lore.size() - 1);

                                    meta.setLore(lore);
                                    stack.setItemMeta(meta);
                                    randomRewardsInventory.setItem(i, stack);
                                }
                            }
                        }
                        this.randomRewardsInventory.setItem(33, new CustomItem(GameUtils.getMaterialByVersion("TORCH"), 1, 0).setName("&7> &cEdit items &7<").create());
                    } else {
                        for (int i = 0; i < randomRewardsInventory.getSize(); i++) {
                            if (randomRewardsInventory.getItem(i) != null) {
                                ItemStack stack = randomRewardsInventory.getItem(i);
                                ItemMeta meta = stack.getItemMeta();
                                boolean useCommand = false;
                                if (Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()) != null) {
                                    if (Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(i) != null) {
                                        useCommand = (boolean) Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(i).get("useCommand");
                                    }
                                }
                                List<String> lore = new ArrayList<>();

                                if (stack.hasItemMeta() && stack.getItemMeta().hasLore()) {
                                    lore = stack.getItemMeta().getLore();
                                }

                                lore.add(" ");
                                lore.add(Utils.translate("&eUse commands&7: " + useCommand));
                                lore.add(" ");
                                lore.add(Utils.translate("&aLeft click to add commands on this item"));
                                lore.add(Utils.translate("&cRight click to remove commands on this item"));
                                lore.add(Utils.translate("&aShift left click to change enable or disable commands on this item"));
                                lore.add(Utils.translate("&eMiddle click to view list of commands"));
                                meta.setLore(lore);
                                randomRewardsInventory.getItem(i).setItemMeta(meta);
                                randomRewardsInventory.setItem(i, stack);
                            }
                        }
                        this.randomRewardsInventory.setItem(33, new CustomItem(Material.TORCH, 1, 0).setName("&7> &cEdit items &7<").create());
                        Hosts.getInstance().getRewardsManager().save();
                    }
                }
            }
            if (!editableRandomRewards) {

                if (event.getCurrentItem().getType() == Material.AIR) {
                    return;
                }
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cSave &7<"))) {
                    return;
                }
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().startsWith(Utils.translate("&7> &eAmount of"))) {
                    return;
                }
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&c."))) {
                    return;
                }
                if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cEdit items &7<"))) {
                    return;
                }

                if (event.isShiftClick() && event.isLeftClick() && event.getClickedInventory() != player.getInventory()) {
                    boolean useCommand = (boolean) Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(event.getSlot()).get("useCommand");
                    Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(event.getSlot()).put("useCommand", !useCommand);
                    Hosts.getInstance().getRewardsManager().save();

                    ItemMeta meta = event.getCurrentItem().getItemMeta();

                    List<String> lore = new ArrayList<>();

                    if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasLore()) {
                        lore = event.getCurrentItem().getItemMeta().getLore();
                    }

                    int index = 0;

                    for (String lines : lore) {
                        if (lines.startsWith(Utils.translate("&eUse commands"))) {
                            lore.set(index, Utils.translate("&eUse commands&7: " + !useCommand));
                        }
                        index++;
                    }

                    meta.setLore(lore);
                    event.getCurrentItem().setItemMeta(meta);
                } else if (event.isLeftClick() && event.getClickedInventory() != player.getInventory()) {
                    if (Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(event.getSlot()) == null) {
                        player.sendMessage(ChatColor.RED + "Save first");
                        return;
                    }
                    boolean useCommand = (boolean) Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(event.getSlot()).get("useCommand");
                    if (useCommand) {
                        chatConfig.put(player, "commandAddRandomRewardI" + event.getSlot());
                        player.sendMessage(Utils.translate("&ePlease input value"));
                        player.closeInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + "This feature is disabled!");
                    }
                } else if (event.isRightClick() && event.getClickedInventory() != player.getInventory()) {
                    if (Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(event.getSlot()) == null) {
                        player.sendMessage(ChatColor.RED + "Save first");
                        return;
                    }
                    boolean useCommand = (boolean) Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(event.getSlot()).get("useCommand");
                    if (useCommand) {
                        chatConfig.put(player, "commandRemoveRandomRewardI" + event.getSlot());
                        player.sendMessage(Utils.translate("&ePlease type index"));
                        player.closeInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + "This feature is disabled!");

                    }
                } else if (event.getClick().equals(ClickType.MIDDLE)) {
                    if (Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(event.getSlot()) == null) {
                        player.sendMessage(ChatColor.RED + "Save first");
                        return;
                    }
                    boolean useCommand = (boolean) Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(event.getSlot()).get("useCommand");
                    if (useCommand) {
                        ArrayList<String> commands = (ArrayList<String>) Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(event.getSlot()).get("commands");
                        player.sendMessage(Utils.translate("&e" + commands.stream().collect(Collectors.joining("&7, &e"))));
                    } else {
                        player.sendMessage(ChatColor.RED + "This feature is disabled!");
                    }
                }
            }
        }
    }

    @EventHandler
    private void onChat(AsyncPlayerChatEvent event) {
        if (chatConfig.containsKey(event.getPlayer())) {
            if (event.getMessage().equalsIgnoreCase("cancel") || event.getMessage().equalsIgnoreCase("exit")) {
                chatConfig.remove(event.getPlayer());
                event.getPlayer().sendMessage(ChatColor.RED + "Operation has been canceled");
                event.setCancelled(true);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("addItemOnJoin")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());

                List<String> commands = Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.COMMANDS-ON-JOIN");
                commands.add(event.getMessage().replace("{player}", event.getPlayer().getName()));
                Configs.CONFIG.getConfig().set("HOST.GENERAL.COMMANDS-ON-JOIN", commands);
                Configs.CONFIG.saveConfig();

                List<String> customList = new ArrayList<>();

                customList.add(Utils.translate("&7&lPlaceholders"));
                customList.add(" ");
                customList.add(Utils.translate("&6{player}"));
                customList.add(" ");
                AtomicInteger i = new AtomicInteger();
                Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.COMMANDS-ON-JOIN").forEach(element -> customList.add(Utils.translate("&a" + i.getAndIncrement() + " &f" + element)));
                customList.add(" ");
                customList.add(Utils.translate("&aShift Left click to add item"));
                customList.add(Utils.translate("&cShift Right click to remove item"));

                generalInventory.setItem(39, new CustomItem(GameUtils.getMaterialByVersion("MINECART"), 1, 0).setName("&eCommands on join&7: &6").addLore(customList).create());
                event.getPlayer().openInventory(generalInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("removeItemOnJoin")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                List<String> commands = Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.COMMANDS-ON-JOIN");
                try {
                    commands.remove(Integer.parseInt(event.getMessage()));
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                    event.getPlayer().openInventory(generalInventory);
                    return;
                } catch (IndexOutOfBoundsException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Invalid index");
                    event.getPlayer().openInventory(generalInventory);
                    return;
                }
                Configs.CONFIG.getConfig().set("HOST.GENERAL.COMMANDS-ON-JOIN", commands);
                Configs.CONFIG.saveConfig();

                List<String> customList = new ArrayList<>();

                customList.add(Utils.translate("&7&lPlaceholders"));
                customList.add(" ");
                customList.add(Utils.translate("&6{player}"));
                customList.add(" ");
                AtomicInteger i = new AtomicInteger();
                Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.COMMANDS-ON-JOIN").forEach(element -> customList.add(Utils.translate("&a" + i.getAndIncrement() + " &f" + element)));
                customList.add(" ");
                customList.add(Utils.translate("&aShift Left click to add item"));
                customList.add(Utils.translate("&cShift Right click to remove item"));

                generalInventory.setItem(39, new CustomItem(GameUtils.getMaterialByVersion("MINECART"), 1, 0).setName("&eCommands on join&7: &6").addLore(customList).create());
                event.getPlayer().openInventory(generalInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("addItemOnAllowed")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                List<String> commands = Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.ALLOWED-COMMANDS");
                commands.add(event.getMessage());
                Configs.CONFIG.getConfig().set("HOST.GENERAL.ALLOWED-COMMANDS", commands);
                Configs.CONFIG.saveConfig();

                List<String> customList = new ArrayList<>();

                customList.add(" ");
                AtomicInteger i = new AtomicInteger();
                Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.ALLOWED-COMMANDS").forEach(element -> customList.add(Utils.translate("&a" + i.getAndIncrement() + " &f" + element)));
                customList.add(" ");
                customList.add(Utils.translate("&aShift Left click to add item"));
                customList.add(Utils.translate("&cShift Right click to remove item"));

                generalInventory.setItem(41, new CustomItem(GameUtils.getMaterialByVersion("MINECART"), 1, 0).setName("&eAllowed commands&7: &6").addLore(customList).create());
                event.getPlayer().openInventory(generalInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("removeItemOnAllowed")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                List<String> commands = Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.ALLOWED-COMMANDS");
                try {
                    commands.remove(Integer.parseInt(event.getMessage()));
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                    event.getPlayer().openInventory(generalInventory);
                    return;
                } catch (IndexOutOfBoundsException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Invalid index");
                    event.getPlayer().openInventory(generalInventory);
                    return;
                }
                Configs.CONFIG.getConfig().set("HOST.GENERAL.ALLOWED-COMMANDS", commands);
                Configs.CONFIG.saveConfig();

                List<String> customList = new ArrayList<>();

                customList.add(" ");
                AtomicInteger i = new AtomicInteger();
                Configs.CONFIG.getConfig().getStringList("HOST.GENERAL.ALLOWED-COMMANDS").forEach(element -> customList.add(Utils.translate("&a" + i.getAndIncrement() + " &f" + element)));
                customList.add(" ");
                customList.add(Utils.translate("&aShift Left click to add item"));
                customList.add(Utils.translate("&cShift Right click to remove item"));

                generalInventory.setItem(41, new CustomItem(GameUtils.getMaterialByVersion("MINECART"), 1, 0).setName("&eAllowed commands&7: &6").addLore(customList).create());
                event.getPlayer().openInventory(generalInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("displayNameMode")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                Configs.ITEMS.getConfig().set("HOST.games." + getTempGameEditing() + ".modes." + getTempModeEditing() + ".displayName", event.getMessage());
                Configs.ITEMS.saveConfig();

                tempInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games." + getTempGameEditing() + ".modes." + getTempModeEditing() + ".displayName")).create());

                event.getPlayer().openInventory(tempInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("displayNameGame")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                Configs.ITEMS.getConfig().set("HOST.games." + getTempGameEditing() + ".displayName", event.getMessage());
                Configs.ITEMS.saveConfig();

                tempInventory.setItem(23, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + Configs.ITEMS.getConfig().getString("HOST.games." + getTempGameEditing() + ".displayName")).create());

                event.getPlayer().openInventory(tempInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("slotGame")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                try {
                    Configs.ITEMS.getConfig().set("HOST.games." + getTempGameEditing() + ".slot", Integer.parseInt(event.getMessage()));
                    Configs.ITEMS.saveConfig();
                    configInventory.setItem(25, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games." + getTempGameEditing() + ".slot")).create());
                    event.getPlayer().openInventory(tempInventory);
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                    event.getPlayer().openInventory(tempInventory);
                }
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("slotMode")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                try {
                    Configs.ITEMS.getConfig().set("HOST.games." + getTempGameEditing() + ".modes." + getTempModeEditing() + ".slot", Integer.parseInt(event.getMessage()));
                    Configs.ITEMS.saveConfig();
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + Configs.ITEMS.getConfig().getInt("HOST.games." + getTempGameEditing() + ".modes." + getTempModeEditing() + ".slot")).create());
                    event.getPlayer().openInventory(tempInventory);
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                    event.getPlayer().openInventory(tempInventory);
                }
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("soundName")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                try {
                    Configs.CONFIG.getConfig().set("HOST.GENERAL.SOUND-ON-START", Sound.valueOf(event.getMessage()).toString());
                    Configs.CONFIG.saveConfig();
                    generalInventory.setItem(10, new CustomItem(Material.BLAZE_POWDER, 1, 0).setName("&eSound on start&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.SOUND-ON-START")).create());
                    event.getPlayer().openInventory(generalInventory);
                } catch (IllegalArgumentException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Invalid Sound name");
                    event.getPlayer().openInventory(generalInventory);
                }
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("announceName")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                if (event.getMessage().contains(",")) {
                    Configs.CONFIG.getConfig().set("HOST.GENERAL.ANNOUNCE-TIMES", event.getMessage());
                    Configs.CONFIG.saveConfig();
                    generalInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eAnnounce times&7: &6" + Configs.CONFIG.getConfig().getString("HOST.GENERAL.ANNOUNCE-TIMES")).create());
                    event.getPlayer().openInventory(generalInventory);
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "Invalid announce time, Usage: 60,50,40,etc");
                }
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("randomRewards")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                try {
                    Configs.ITEMS.getConfig().set("HOST.rewards.types.RANDOM.amount-of-rewards", Integer.parseInt(event.getMessage()));
                    Configs.ITEMS.saveConfig();
                    this.randomRewardsInventory.setItem(34, new CustomItem(Material.PAPER, 1, 0).setName("&7> &eAmount of Rewards&7: &7" + Configs.ITEMS.getConfig().getInt("HOST.rewards.types.RANDOM.amount-of-rewards") + " &7<").create());
                    event.getPlayer().openInventory(randomRewardsInventory);
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                }
            } else if (chatConfig.get(event.getPlayer()).startsWith("commandAddReward")) {
                event.setCancelled(true);
                int slot = Integer.parseInt(chatConfig.get(event.getPlayer()).split("I")[1]);
                ArrayList<String> commands = (ArrayList<String>) Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(slot).get("commands");
                commands.add(event.getMessage().replace("{player}", event.getPlayer().getName()));
                Hosts.getInstance().getRewardsManager().save();
                event.getPlayer().openInventory(rewardsInventory);
                chatConfig.remove(event.getPlayer());
            } else if (chatConfig.get(event.getPlayer()).startsWith("commandRemoveReward")) {
                event.setCancelled(true);
                int slot = Integer.parseInt(chatConfig.get(event.getPlayer()).split("I")[1]);
                ArrayList<String> commands = (ArrayList<String>) Hosts.getInstance().getRewardsManager().getRewards().get("Rewards" + getTempGameEditing()).get(slot).get("commands");
                try {
                    commands.remove(Integer.parseInt(event.getMessage()));
                    Hosts.getInstance().getRewardsManager().save();
                    event.getPlayer().openInventory(rewardsInventory);
                } catch (NumberFormatException e) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                } catch (IndexOutOfBoundsException e) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type valid index");
                }
                Hosts.getInstance().getRewardsManager().save();
                chatConfig.remove(event.getPlayer());
            } else if (chatConfig.get(event.getPlayer()).startsWith("commandAddRandomReward")) {
                event.setCancelled(true);
                int slot = Integer.parseInt(chatConfig.get(event.getPlayer()).split("I")[1]);
                ArrayList<String> commands = (ArrayList<String>) Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(slot).get("commands");
                commands.add(event.getMessage().replace("{player}", event.getPlayer().getName()));
                Hosts.getInstance().getRewardsManager().save();
                event.getPlayer().openInventory(randomRewardsInventory);
                chatConfig.remove(event.getPlayer());
            } else if (chatConfig.get(event.getPlayer()).startsWith("commandRemoveRandomReward")) {
                event.setCancelled(true);
                int slot = Integer.parseInt(chatConfig.get(event.getPlayer()).split("I")[1]);
                ArrayList<String> commands = (ArrayList<String>) Hosts.getInstance().getRewardsManager().getRewards().get("RandomRewards" + getTempGameEditing()).get(slot).get("commands");
                try {
                    commands.remove(Integer.parseInt(event.getMessage()));
                    Hosts.getInstance().getRewardsManager().save();
                    event.getPlayer().openInventory(randomRewardsInventory);
                } catch (NumberFormatException e) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                } catch (IndexOutOfBoundsException e) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type valid index");
                }
                chatConfig.remove(event.getPlayer());
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("gameDisplayName")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".displayName", event.getMessage());
                Configs.CONFIG.saveConfig();

                gameInventory.setItem(10, new CustomItem(Material.NAME_TAG, 1,0).setName("&eDisplayName&7: " + Configs.CONFIG.getConfig().getString("HOST.Games." + getTempGameEditing() + ".displayName")).create());

                event.getPlayer().openInventory(gameInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("newPotion")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                List<String> potions = Configs.CONFIG.getConfig().getStringList("HOST.Games." + getTempGameEditing() + ".effects-in." + getTempModeEditing());
                if (!event.getMessage().contains(":")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Invalid format");
                    return;
                }
                if (Arrays.stream(PotionEffectType.values()).filter(Objects::nonNull).noneMatch(potionEffectType -> potionEffectType.getName().equalsIgnoreCase(event.getMessage().split(":")[0]))) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Invalid potion name");
                    return;
                }

                potions.add(event.getMessage());
                Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".effects-in." + getTempModeEditing(), potions);
                Configs.CONFIG.saveConfig();

                configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games." + getTempGameEditing() + ".effects-in." + getTempModeEditing())), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
            event.getPlayer().openInventory(configInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("removePotion")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                List<String> potions = Configs.CONFIG.getConfig().getStringList("HOST.Games." + getTempGameEditing() + ".effects-in." + getTempModeEditing());
                try {
                    potions.remove(Integer.parseInt(event.getMessage()));
                    Configs.CONFIG.getConfig().set("HOST.Games." + getTempGameEditing() + ".effects-in." + getTempModeEditing(), potions);
                    Configs.CONFIG.saveConfig();
                    configInventory.setItem(4, new CustomItem(Material.PAPER, 1, 0).setName("&ePassive Effects").addLore(Utils.translate("&c" + Configs.CONFIG.getConfig().getStringList("HOST.Games." + getTempGameEditing() + ".effects-in." + getTempModeEditing())), " ", "&aLeft click to add effect", "&cRight click to remove effect").create());
                    event.getPlayer().openInventory(configInventory);
                } catch (NumberFormatException e) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                } catch (IndexOutOfBoundsException e) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type valid index");
                }
            }
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase(Utils.translate(Configs.ITEMS.getConfig().getString("HOST.invTitles.gameSelector")))) {
            Hosts.getInstance().getInventoryManager().closeHostMenu((Player) event.getPlayer());
        }
        if (event.getView().getTitle().startsWith(Utils.translate("&7> &6General")) && !chatConfig.containsKey((Player) event.getPlayer())) {
            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> event.getPlayer().openInventory(configInventory), 1L);
        } else if (event.getView().getTitle().startsWith(Utils.translate("&7> &bRewards Settings")) && !chatConfig.containsKey((Player) event.getPlayer())) {
            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> {
                event.getPlayer().openInventory(configInventory);
                editableRewards = false;
            }, 1L);
        } else if (event.getView().getTitle().startsWith(Utils.translate("&7> &bRandom Rewards")) && !chatConfig.containsKey((Player) event.getPlayer())) {
            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> {
                event.getPlayer().openInventory(configInventory);
                editableRandomRewards = false;
            }, 1L);
        } else if (event.getView().getTitle().startsWith(Utils.translate("&7> &cGame Settings")) && !chatConfig.containsKey((Player) event.getPlayer())) {
            Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> event.getPlayer().openInventory(configInventory), 1L);
        } else if (event.getView().getTitle().startsWith(Utils.translate("&eConfiguration of")) && !chatConfig.containsKey((Player) event.getPlayer())) {
            if (getTempGameEditing().equalsIgnoreCase("SUMO")) {
                Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> Hosts.getInstance().getInventoryManager().openSumoModesInventory((Player) event.getPlayer()), 1L);
            } else if (getTempGameEditing().equalsIgnoreCase("FFA")) {
                Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> Hosts.getInstance().getInventoryManager().openFFAModesInventory((Player) event.getPlayer()), 1L);
            } else if (getTempGameEditing().equalsIgnoreCase("1v1")) {
                Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> Hosts.getInstance().getInventoryManager().open1v1ModesInventory((Player) event.getPlayer()), 1L);
            }
        }
    }
}
