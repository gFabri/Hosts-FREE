package com.github.bfabri.hosts.listeners;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.game.GamePlayer;
import com.github.bfabri.hosts.utils.CustomItem;
import com.github.bfabri.hosts.utils.GameUtils;
import com.github.bfabri.hosts.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SpectatorListener implements Listener {

	private final List<Inventory> playerListInventories = new ArrayList<>();

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack clickedItem = event.getItem();
			if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
				Player player = event.getPlayer();
				String itemDisplayName = clickedItem.getItemMeta().getDisplayName();

				if (itemDisplayName.equalsIgnoreCase(Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.spectator.leave.displayName")))) {
					Hosts.getInstance().getSpectatorManager().leaveSpectator(Hosts.getInstance().getGameManager().getGame(), Hosts.getInstance().getGameManager().getPlayer(player));
				} else if (itemDisplayName.equalsIgnoreCase(Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.spectator.teleport.displayName")))) {
					openPlayerListInventory(player);
				}
			}
		}
	}

	public void openPlayerListInventory(Player player) {
		if (playerListInventories.isEmpty()) {
			updatePlayerListInventory();
		}
		player.openInventory(playerListInventories.get(0));
	}

	public void updatePlayerListInventory() {
		playerListInventories.clear();

		List<GamePlayer> players = new ArrayList<>(Hosts.getInstance().getGameManager().getGame().getGamePlayers());
		List<List<GamePlayer>> playerGroups = new ArrayList<>();
		int groupSize = 54;
		for (int i = 0; i < players.size(); i += groupSize) {
			playerGroups.add(players.subList(i, Math.min(i + groupSize, players.size())));
		}

		int currentPage = 0;
		for (List<GamePlayer> playerGroup : playerGroups) {
			Inventory inventory = Bukkit.createInventory(null, 54, Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.spectatorPlayersMenu").replace("{page}",  String.valueOf((currentPage + 1)))));

			for (GamePlayer p : playerGroup) {
				inventory.addItem(new CustomItem(GameUtils.getMaterialByVersion("SKULL"), 1, 0).createHead(p.getPlayer(), Utils.translate("&7> &4" + p.getPlayer().getName() + " &7<")));
			}

			if (playerGroups.size() > 1) {
				ItemStack previousPageButton = new ItemStack(Material.ARROW, 1);
				ItemMeta previousPageMeta = previousPageButton.getItemMeta();
				previousPageMeta.setDisplayName(ChatColor.YELLOW + "Prev Page");
				previousPageButton.setItemMeta(previousPageMeta);

				if (currentPage > 0) {
					inventory.setItem(45, previousPageButton);
				}

				ItemStack nextPageButton = new ItemStack(Material.ARROW, 1);
				ItemMeta nextPageMeta = nextPageButton.getItemMeta();
				nextPageMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
				nextPageButton.setItemMeta(nextPageMeta);

				if (currentPage < playerGroups.size() - 1) {
					inventory.setItem(53, nextPageButton);
				}
			}

			playerListInventories.add(inventory);
			currentPage++;
		}
	}

	@EventHandler
	private void onCloseInventory(InventoryCloseEvent event) {
		Inventory clickedInventory = event.getInventory();

		playerListInventories.remove(clickedInventory);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory clickedInventory = event.getClickedInventory();

		if (playerListInventories.contains(clickedInventory)) {
			event.setCancelled(true);

			Player player = (Player) event.getWhoClicked();

			if (event.getCurrentItem() == null) {
				return;
			}

			if (event.getCurrentItem().getType() == GameUtils.getMaterialByVersion("SKULL")) {
				String playerName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName().replace("\u00AB", "").replace("\u00BB", "").replace(" ", ""));
				Player targetPlayer = Bukkit.getPlayer(playerName);
				if (targetPlayer != null) {
					player.teleport(targetPlayer.getLocation());
				} else {
					player.sendMessage(Utils.translate(ConfigHandler.Configs.LANG.getConfig().getString("INVALID-PLAYER")));
				}
			} else if (event.getCurrentItem().getType() == Material.ARROW) {
				int inventoryIndex = playerListInventories.indexOf(clickedInventory);
				ItemMeta meta = event.getCurrentItem().getItemMeta();
				String buttonName = meta.getDisplayName();

				if (buttonName.equals(ChatColor.YELLOW + "Prev Page") && inventoryIndex > 0) {
					player.openInventory(playerListInventories.get(inventoryIndex - 1));
				} else if (buttonName.equals(ChatColor.YELLOW + "Next Page") && inventoryIndex < playerListInventories.size() - 1) {
					player.openInventory(playerListInventories.get(inventoryIndex + 1));
				}
			}
		}
	}
}
