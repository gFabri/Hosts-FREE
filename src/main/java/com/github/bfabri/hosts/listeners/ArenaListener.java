package com.github.bfabri.hosts.listeners;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.Arena;
import com.github.bfabri.hosts.utils.Cuboid;
import com.github.bfabri.hosts.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ArenaListener implements Listener {

    private boolean first;
    private boolean second;

    @Getter
    @Setter
    private Location pos1Location;

    @Getter
    @Setter
    private Location pos2Location;

    @EventHandler
    private void onItemInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName() && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&eSet corners location")) && event.getPlayer().hasMetadata("arenaName")) {
            Arena arena = Hosts.getInstance().getArenaManager().getArena(event.getPlayer().getMetadata("arenaName").get(0).asString());
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                setPos1Location(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage(Utils.translate("&eCorner #1 has been set"));
                first = true;
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                setPos2Location(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage(Utils.translate("&eCorner #2 has been set"));
                second = true;
            }
            if (first && second) {
                if (getPos1Location() != null && getPos2Location() != null) {
                    event.getPlayer().removeMetadata("arenaName", Hosts.getInstance());
                    arena.setWorldName(event.getPlayer().getWorld().getName());
                    arena.setArea(new Cuboid(getPos1Location(), getPos2Location()).serialize());
                    event.getPlayer().sendMessage(Utils.translate("&bArena corners has been configured!"));
                    event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
                    first = false;
                    second = false;
                    Hosts.getInstance().getArenaManager().saveArenas();
                }
            }
        }
    }

    @EventHandler
    private void onItemDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().hasItemMeta() && event.getItemDrop().getItemStack().getItemMeta().hasDisplayName() && event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&eSet corners location")) && event.getPlayer().hasMetadata("arenaName")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Utils.translate("&bYou can´t drop corner item"));
        }
    }

    @EventHandler
    private void onInvClose(InventoryCloseEvent event) {
        String invTitle = Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.arena"));
        if (event.getView().getTitle().equalsIgnoreCase(invTitle)) {
            Hosts.getInstance().getGameManager().setRewardTypes(null);
        }
    }

    @EventHandler
    private void onClickInventory(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() || !event.getCurrentItem().getItemMeta().hasDisplayName())
            return;
        if (event.getView().getTitle().equalsIgnoreCase(Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.arena")))) {
            event.setCancelled(true);
            Arena arena = Hosts.getInstance().getArenaManager().getArena(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName().replace("«", "").replace("»", "")).trim());
            Hosts.getInstance().getGameManager().setSelected(arena);
            if (Hosts.getInstance().getGameManager().getMode() != null) {
                Hosts.getInstance().getGameManager().newGameWithMode(arena.getGameName(), Hosts.getInstance().getGameManager().getMode(), event.getWhoClicked());
                event.getWhoClicked().closeInventory();
            }
        }
    }
}