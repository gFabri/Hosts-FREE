package com.github.bfabri.hosts.managers;

import com.github.bfabri.hosts.ConfigHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RewardsManager {

	private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
	private final Type type = (new TypeToken<HashMap<String, HashMap<Integer, Map<String, Object>>>>() {

	}).getType();

	@Getter
	private HashMap<String, HashMap<Integer, Map<String, Object>>> rewards = new HashMap<>();

	public RewardsManager() {
		load();
	}

	private void load() {
		try {
			HashMap<String, HashMap<Integer, Map<String, Object>>> json = this.gson.fromJson(new FileReader(ConfigHandler.Configs.REWARDS.getFile()), this.type);
			if (json != null) {
				rewards = json;
			}
		} catch (FileNotFoundException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "rewards file not found");
		}
	}

	public void save() {
		try (FileWriter writer = new FileWriter(ConfigHandler.Configs.REWARDS.getFile())) {
			this.gson.toJson(rewards, writer);
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "An error occurred");
		}
	}
}
