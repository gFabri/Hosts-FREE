package com.github.bfabri.hosts.utils;

import com.github.bfabri.hosts.Hosts;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemStackCustom implements ConfigurationSerializable {

	@Getter
	private String material;

	@Getter
	private String amount;

	@Getter
	private String material_data;

	@Getter
	private List<String> lore;

	@Getter
	private String displayName;
	@Getter
	private ArrayList<String> enchantments = new ArrayList<>();

	@Getter
	private boolean useCommand;

	@Getter
	private final ArrayList<String> commands = new ArrayList<>();

	public ItemStackCustom() {
	}

	public ItemStackCustom(ItemStack stack) {
		material = stack.getType().toString();
		amount = String.valueOf(stack.getAmount());
		material_data = material.equalsIgnoreCase("POTION") ? String.valueOf(stack.getDurability()) : String.valueOf(stack.getData().getData());
		useCommand = false;

		if (stack.hasItemMeta()) {
			ItemMeta meta = stack.getItemMeta();
			lore = meta.hasLore() ? Utils.translate(meta.getLore()) : null;
			displayName = meta.hasDisplayName() ? Utils.translate(meta.getDisplayName()) : null;

			if (meta.hasEnchants()) {
				for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
					enchantments.add(entry.getKey().getName().toUpperCase() + ":" + entry.getValue());
				}
			}
		}
	}

	public static ItemStack deserialize(Map<String, Object> args) {
		ArrayList<String> enchantments = (ArrayList<String>) args.get("enchantments");
		List<String> lore = (List<String>) args.get("lore");
		String displayName = (String) args.get("displayName");
		int amount = Integer.parseInt((String) args.get("amount"));
		short data = Short.parseShort((String) args.get("data"));
		String material = (String) args.get("material");


		return new CustomItem(Material.getMaterial(material), amount, data).setName(displayName).addLore(lore).addEnchantments(enchantments).create();
	}

	public void runItems(Player player, Map<String, Object> args) {
		if (isUseCommand() && !getCommands().isEmpty()) {
			Bukkit.getScheduler().runTaskLater(Hosts.getInstance(), () -> runCommands(player), 0L);
		} else if (!isUseCommand()) {
			player.getInventory().addItem(deserialize(args));
		}
	}

	private void runCommands(Player player) {
		for (String command : getCommands()) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", player.getName()));
		}
	}


	@NotNull
	@Override
	public Map<String, Object> serialize() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();

		map.put("material", getMaterial());
		map.put("amount", getAmount());
		map.put("data", getMaterial_data());
		map.put("useCommand", useCommand);
		map.put("commands", commands);
		if (getLore() != null) {
			map.put("lore", getLore());
		}
		if (getDisplayName() != null) {
			map.put("displayName", getDisplayName());
		}
		if (!getEnchantments().isEmpty()) {
			map.put("enchantments", getEnchantments());
		}
		return map;
	}
}
