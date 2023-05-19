package com.github.bfabri.hosts.listeners;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.game.Game;
import com.github.bfabri.hosts.utils.CustomItem;
import com.github.bfabri.hosts.utils.GameUtils;
import com.github.bfabri.hosts.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RewardsListener implements Listener {

    @Getter
    private final Inventory itemsRewards = Bukkit.createInventory(null, 36, Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.rewardsItem")));

    private Inventory configInventory;

    @Getter
    private String game;

    private final HashMap<Player, String> chatConfig = new HashMap<>();

    private boolean started = false;

    @Getter
    private String mode = null;

    public RewardsListener() {
        for (int i = 27; i < 35; i++) {
            itemsRewards.setItem(i, new CustomItem(GameUtils.getMaterialByVersion("STAINED"), 1, 0).setName("&c.").create());
        }
        itemsRewards.setItem(35, new CustomItem(Material.DIAMOND, 1, 0).setName("&7> &cStart Game &7<").create());
    }

    @EventHandler
    public void onRewardsInventoryClick(InventoryClickEvent event) {

        InventoryView view = event.getView();

        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        if (view.getTitle().equalsIgnoreCase(Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.rewards")))) {
            event.setCancelled(true);
            if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName()) {
                if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.NONE.displayName")))) {
                    if (event.isShiftClick()) {
                        if (!player.hasPermission("hosts.manager.config")) {
                            return;
                        }
                        configInventory = Bukkit.createInventory(null, 27, Utils.translate("&eConfiguration menu of none reward"));

                        configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.NONE.enabled")).create());
                        configInventory.setItem(12, new CustomItem(Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.NONE.material")) == null ? Material.DIRT :
                                Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.NONE.material")), 1, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.NONE.material-data")).setName("&eIcon &c(Drag to change)").create());
                        configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.NONE.displayName")).create());
                        configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.NONE.slot")).create());
                        player.openInventory(configInventory);
                    } else {
                        if (!player.hasPermission("host.reward.none")) {
                            player.sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("NO-PERMISSION")));
                            return;
                        }
                        Hosts.getInstance().getGameManager().setRewardTypes(Game.RewardTypes.NONE);
                        if (mode != null) {
                            Hosts.getInstance().getGameManager().newGameWithMode(getGame(), getMode(), player);
                            player.closeInventory();
                        }
                    }
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.ITEMS.displayName")))) {
                    if (event.isShiftClick()) {
                        if (!player.hasPermission("hosts.manager.config")) {
                            return;
                        }
                        configInventory = Bukkit.createInventory(null, 27, Utils.translate("&eConfiguration menu of items reward"));

                        configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.ITEMS.enabled")).create());
                        configInventory.setItem(12, new CustomItem(Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.ITEMS.material")) == null ? Material.DIRT :
                                Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.ITEMS.material")), 1, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.ITEMS.material-data")).setName("&eIcon &c(Drag to change)").create());
                        configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.ITEMS.displayName")).create());
                        configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.ITEMS.slot")).create());
                        player.openInventory(configInventory);
                    } else {
                        if (!player.hasPermission("host.reward.items")) {
                            player.sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("NO-PERMISSION")));
                            return;
                        }
                        started = false;
                        player.openInventory(itemsRewards);
                    }
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.RANDOM.displayName")))) {
                    if (event.isShiftClick()) {
                        if (!player.hasPermission("hosts.manager.config")) {
                            return;
                        }
                        configInventory = Bukkit.createInventory(null, 27, Utils.translate("&eConfiguration menu of random reward"));

                        configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.RANDOM.enabled")).create());
                        configInventory.setItem(12, new CustomItem(Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.RANDOM.material")) == null ? Material.DIRT :
                                Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.RANDOM.material")), 1, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.RANDOM.material-data")).setName("&eIcon &c(Drag to change)").create());
                        configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.RANDOM.displayName")).create());
                        configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.RANDOM.slot")).create());
                        player.openInventory(configInventory);
                    } else {
                        if (!player.hasPermission("host.reward.random")) {
                            player.sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("NO-PERMISSION")));
                            return;
                        }
                        Hosts.getInstance().getGameManager().setRewardTypes(Game.RewardTypes.RANDOM);
                        if (mode != null) {
                            Hosts.getInstance().getGameManager().newGameWithMode(getGame(), getMode(), player);
                            player.closeInventory();
                        }
                    }
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.DEFAULT.displayName")))) {
                    if (event.isShiftClick()) {
                        if (!player.hasPermission("hosts.manager.config")) {
                            return;
                        }
                        configInventory = Bukkit.createInventory(null, 27, Utils.translate("&eConfiguration menu of default reward"));

                        configInventory.setItem(10, new CustomItem(Material.LEVER, 1, 0).setName("&eEnabled&7: " + ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.DEFAULT.enabled")).create());
                        configInventory.setItem(12, new CustomItem(Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.DEFAULT.material")) == null ? Material.DIRT :
                                Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.DEFAULT.material")), 1, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.DEFAULT.material-data")).setName("&eIcon &c(Drag to change)").create());
                        configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.DEFAULT.displayName")).create());
                        configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.DEFAULT.slot")).create());
                        player.openInventory(configInventory);
                    } else {
                        if (!player.hasPermission("host.reward.default")) {
                            player.sendMessage(Utils.PREFIX + Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("NO-PERMISSION")));
                            return;
                        }
                        Hosts.getInstance().getGameManager().setRewardTypes(Game.RewardTypes.DEFAULT);
                        if (mode != null) {
                            Hosts.getInstance().getGameManager().newGameWithMode(getGame(), getMode(), player);
                            player.closeInventory();
                        }
                    }
                }
            }
        } else if (view.getTitle().equalsIgnoreCase(Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.rewardsItem")))) {
            if (!event.getCurrentItem().hasItemMeta() && !event.getCurrentItem().getItemMeta().hasDisplayName()) {
                return;
            }
            if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cStart Game &7<"))) {
                event.setCancelled(true);
                if (Arrays.stream(getItemsRewards().getContents()).filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR).count() <= 9) {
                    player.closeInventory();
                    return;
                }
                Hosts.getInstance().getGameManager().setRewardTypes(Game.RewardTypes.ITEMS);
                started = true;
                if (mode != null) {
                    Hosts.getInstance().getGameManager().newGameWithMode(getGame(), getMode(), player);
                }
            } else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&c."))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onRewardConfig(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() || !event.getCurrentItem().getItemMeta().hasDisplayName()) {
            return;
        }

        if (event.getView().getTitle().startsWith(Utils.translate("&eConfiguration menu of none"))) {
            event.setCancelled(true);
            if (event.getSlot() == 10) {
                ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.NONE.enabled", !ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.NONE.enabled"));
                ConfigHandler.Configs.ITEMS.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eEnabled&7: " + ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.NONE.enabled")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 12) {
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    event.getCursor().setItemMeta(meta);
                    event.setCurrentItem(event.getCursor());
                    ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.NONE.material", event.getCursor().getType().toString());
                    if (event.getCursor().getType().toString().equalsIgnoreCase("POTION")) {
                        ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.NONE.material-data", event.getCursor().getDurability());
                    } else {
                        ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.NONE.material-data", event.getCursor().getData().getData());
                    }
                    ConfigHandler.Configs.ITEMS.saveConfig();
                    event.setCursor(null);
                }
            } else if (event.getSlot() == 14) {
                chatConfig.put(player, "displayNameNone");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type displayName"));
            } else if (event.getSlot() == 16) {
                chatConfig.put(player, "slotNone");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type slot"));
            }
        } else if (event.getView().getTitle().startsWith(Utils.translate("&eConfiguration menu of items"))) {
            event.setCancelled(true);
            if (event.getSlot() == 10) {
                ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.ITEMS.enabled", !ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.ITEMS.enabled"));
                ConfigHandler.Configs.ITEMS.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eEnabled&7: " + ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.ITEMS.enabled")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 12) {
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    event.getCursor().setItemMeta(meta);
                    event.setCurrentItem(event.getCursor());
                    ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.ITEMS.material", event.getCursor().getType().toString());
                    if (event.getCursor().getType().toString().equalsIgnoreCase("POTION")) {
                        ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.ITEMS.material-data", event.getCursor().getDurability());
                    } else {
                        ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.ITEMS.material-data", event.getCursor().getData().getData());
                    }
                    ConfigHandler.Configs.ITEMS.saveConfig();
                    event.setCursor(null);
                }
            } else if (event.getSlot() == 14) {
                chatConfig.put(player, "displayNameItems");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type displayName"));
            } else if (event.getSlot() == 16) {
                chatConfig.put(player, "slotItems");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type slot"));
            }
        } else if (event.getView().getTitle().startsWith(Utils.translate("&eConfiguration menu of random"))) {
            event.setCancelled(true);
            if (event.getSlot() == 10) {
                ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.NONE.enabled", !ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.RANDOM.enabled"));
                ConfigHandler.Configs.ITEMS.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eEnabled&7: " + ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.RANDOM.enabled")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 12) {
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    event.getCursor().setItemMeta(meta);
                    event.setCurrentItem(event.getCursor());
                    ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.RANDOM.material", event.getCursor().getType().toString());
                    if (event.getCursor().getType().toString().equalsIgnoreCase("POTION")) {
                        ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.RANDOM.material-data", event.getCursor().getDurability());
                    } else {
                        ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.RANDOM.material-data", event.getCursor().getData().getData());
                    }
                    ConfigHandler.Configs.ITEMS.saveConfig();
                    event.setCursor(null);
                }
            } else if (event.getSlot() == 14) {
                chatConfig.put(player, "displayNameRandom");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type displayName"));
            } else if (event.getSlot() == 16) {
                chatConfig.put(player, "slotRandom");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type slot"));
            }
        } else if (event.getView().getTitle().startsWith(Utils.translate("&eConfiguration menu of default"))) {
            event.setCancelled(true);
            if (event.getSlot() == 10) {
                ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.DEFAULT.enabled", !ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.DEFAULT.enabled"));
                ConfigHandler.Configs.ITEMS.saveConfig();
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setDisplayName(Utils.translate("&eEnabled&7: " + ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.DEFAULT.enabled")));
                event.getCurrentItem().setItemMeta(meta);
            } else if (event.getSlot() == 12) {
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    ItemMeta meta = event.getCurrentItem().getItemMeta();
                    event.getCursor().setItemMeta(meta);
                    event.setCurrentItem(event.getCursor());
                    ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.DEFAULT.material", event.getCursor().getType().toString());
                    if (event.getCursor().getType().toString().equalsIgnoreCase("POTION")) {
                        ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.DEFAULT.material-data", event.getCursor().getDurability());
                    } else {
                        ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.DEFAULT.material-data", event.getCursor().getData().getData());
                    }
                    ConfigHandler.Configs.ITEMS.saveConfig();
                    event.setCursor(null);
                }
            } else if (event.getSlot() == 14) {
                chatConfig.put(player, "displayNameDefault");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type displayName"));
            } else if (event.getSlot() == 16) {
                chatConfig.put(player, "slotDefault");
                player.closeInventory();
                player.sendMessage(Utils.translate("&ePlease type slot"));
            }
        }
    }

    @EventHandler
    public void onCloseItemsRewards(InventoryCloseEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase(Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.rewardsItem")))) {
            if (!Hosts.getInstance().getGameManager().isGameAvailable()) {
                if (Arrays.stream(getItemsRewards().getContents()).filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR).count() > 9) {
                    if (!started) {
                        for (ItemStack itemStack : itemsRewards.getContents()) {
                            if (itemStack != null && itemStack.getType() != Material.AIR) {
                                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&7> &cStart Game &7<"))) {
                                    continue;
                                }
                                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&c."))) {
                                    continue;
                                }
                                event.getPlayer().getInventory().addItem(itemStack);
                                itemsRewards.removeItem(itemStack);
                            }
                        }
                    }
                }
            }
        }
    }

    public void openRewardsMenu(Player player, String game, String mode) {
        this.game = game;
        this.mode = mode;
        Inventory rewardsMenuInventory = Bukkit.createInventory(null, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.invSizes.rewards"), Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.rewards")));
        if (ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.NONE.enabled")) {
            rewardsMenuInventory.setItem(ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.NONE.slot"), new CustomItem(Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.NONE.material")), 1, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.NONE.material-data")).setName(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.NONE.displayName")).addLore(ConfigHandler.Configs.ITEMS.getConfig().getStringList("HOST.rewards.types.NONE.description")).create());
        }
        if (ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.ITEMS.enabled")) {
            rewardsMenuInventory.setItem(ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.ITEMS.slot"), new CustomItem(Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.ITEMS.material")), 1, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.ITEMS.material-data")).setName(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.ITEMS.displayName")).addLore(ConfigHandler.Configs.ITEMS.getConfig().getStringList("HOST.rewards.types.ITEMS.description")).create());
        }
        if (ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.RANDOM.enabled")) {
            rewardsMenuInventory.setItem(ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.RANDOM.slot"), new CustomItem(Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.RANDOM.material")), 1, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.RANDOM.material-data")).setName(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.RANDOM.displayName")).addLore(ConfigHandler.Configs.ITEMS.getConfig().getStringList("HOST.rewards.types.RANDOM.description")).create());
        }
        if (ConfigHandler.Configs.ITEMS.getConfig().getBoolean("HOST.rewards.types.DEFAULT.enabled")) {
            rewardsMenuInventory.setItem(ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.DEFAULT.slot"), new CustomItem(Material.getMaterial(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.DEFAULT.material")), 1, ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.DEFAULT.material-data")).setName(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.DEFAULT.displayName")).addLore(ConfigHandler.Configs.ITEMS.getConfig().getStringList("HOST.rewards.types.DEFAULT.description")).create());
        }
        player.openInventory(rewardsMenuInventory);
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
           if (event.getView().getTitle().startsWith(Utils.translate("&eConfiguration menu")) && event.getView().getTitle().contains("reward") && !chatConfig.containsKey((Player) event.getPlayer())) {
               Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () ->  openRewardsMenu(((Player) event.getPlayer()), game, mode), 1L);
        }
    }

    @EventHandler
    private void onChat(AsyncPlayerChatEvent event) {
        if (chatConfig.containsKey(event.getPlayer())) {
            if (event.getMessage().equalsIgnoreCase("cancel") || event.getMessage().equalsIgnoreCase("exit")) {
                chatConfig.remove(event.getPlayer());
                event.getPlayer().sendMessage(ChatColor.RED + "Operation has been canceled");
                event.setCancelled(true);
            }  else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("displayNameNone")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.NONE.displayName", event.getMessage());
                ConfigHandler.Configs.ITEMS.saveConfig();

                configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.NONE.displayName")).create());

                event.getPlayer().openInventory(configInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("slotNone")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                try {
                    ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.NONE.slot", Integer.parseInt(event.getMessage()));
                    ConfigHandler.Configs.ITEMS.saveConfig();
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.NONE.slot")).create());
                    event.getPlayer().openInventory(configInventory);
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                    event.getPlayer().openInventory(configInventory);
                }
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("displayNameItems")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.ITEMS.displayName", event.getMessage());
                ConfigHandler.Configs.ITEMS.saveConfig();

                configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.ITEMS.displayName")).create());

                event.getPlayer().openInventory(configInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("slotItems")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                try {
                    ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.ITEMS.slot", Integer.parseInt(event.getMessage()));
                    ConfigHandler.Configs.ITEMS.saveConfig();
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.ITEMS.slot")).create());
                    event.getPlayer().openInventory(configInventory);
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                    event.getPlayer().openInventory(configInventory);
                }
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("displayNameRandom")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.RANDOM.displayName", event.getMessage());
                ConfigHandler.Configs.ITEMS.saveConfig();

                configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.RANDOM.displayName")).create());

                event.getPlayer().openInventory(configInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("slotRandom")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                try {
                    ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.RANDOM.slot", Integer.parseInt(event.getMessage()));
                    ConfigHandler.Configs.ITEMS.saveConfig();
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.RANDOM.slot")).create());
                    event.getPlayer().openInventory(configInventory);
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                    event.getPlayer().openInventory(configInventory);
                }
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("displayNameDefault")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.DEFAULT.displayName", event.getMessage());
                ConfigHandler.Configs.ITEMS.saveConfig();

                configInventory.setItem(14, new CustomItem(Material.NAME_TAG, 1, 0).setName("&eDisplayName&7: " + ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.rewards.types.DEFAULT.displayName")).create());

                event.getPlayer().openInventory(configInventory);
            } else if (chatConfig.get(event.getPlayer()).equalsIgnoreCase("slotDefault")) {
                event.setCancelled(true);
                chatConfig.remove(event.getPlayer());
                try {
                    ConfigHandler.Configs.ITEMS.getConfig().set("HOST.rewards.types.DEFAULT.slot", Integer.parseInt(event.getMessage()));
                    ConfigHandler.Configs.ITEMS.saveConfig();
                    configInventory.setItem(16, new CustomItem(Material.ENDER_CHEST, 1, 0).setName("&eSlot&7: " + ConfigHandler.Configs.ITEMS.getConfig().getInt("HOST.rewards.types.DEFAULT.slot")).create());
                    event.getPlayer().openInventory(configInventory);
                } catch (NumberFormatException ex) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Type numbers");
                    event.getPlayer().openInventory(configInventory);
                }
            }
        }
    }
}