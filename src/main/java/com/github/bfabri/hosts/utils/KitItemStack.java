package com.github.bfabri.hosts.utils;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KitItemStack implements ConfigurationSerializable {

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
	public KitItemStack(ItemStack stack) {
		this.material = stack.getType().toString();
		this.amount = String.valueOf(stack.getAmount());
		if(material.equalsIgnoreCase("POTION")) {
			this.material_data = String.valueOf(stack.getDurability());
		} else {
			this.material_data = String.valueOf(stack.getData().getData());
		}
		if (stack.hasItemMeta()) {
			if (stack.getItemMeta().hasLore()) {
				this.lore = Utils.translate(stack.getItemMeta().getLore());
			}
			if (stack.getItemMeta().hasDisplayName()) {
				this.displayName = Utils.translate(stack.getItemMeta().getDisplayName());
			}
			if (stack.getItemMeta().hasEnchants()) {
				stack.getEnchantments().forEach((enchant, level) -> enchantments.add(enchant.getName().toUpperCase() + ":" + level));
			}
		}
	}

	public static ItemStack deserializeItemStack(Map<String, Object> args) {
		ArrayList<String> enchantments = (ArrayList<String>) args.get("enchantments");
		List<String> lore = (List<String>) args.get("lore");
		String displayName = (String) args.get("displayName");
		int amount = Integer.parseInt((String) args.get("amount"));
		short data = Short.parseShort((String) args.get("data"));
		String material = (String) args.get("material");

		return new CustomItem(Material.getMaterial(material), amount, data).setName(displayName).addLore(lore).addEnchantments(enchantments).create();
	}

	public static ItemStack[] deserialize(Map<Integer, Object> arg) {
		List<ItemStack> itemList = new ArrayList<>();

		for (Object value : arg.values()) {
			if (value instanceof Map) {
				Map<String, Object> itemMap = (Map<String, Object>) value;
				ItemStack itemStack = deserializeItemStack(itemMap);
				itemList.add(itemStack);
			}
		}

		return itemList.toArray(new ItemStack[0]);
	}

	@NotNull
	@Override
	public Map<String, Object> serialize() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();

		map.put("material", getMaterial());
		map.put("amount", getAmount());
		map.put("data", getMaterial_data());
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