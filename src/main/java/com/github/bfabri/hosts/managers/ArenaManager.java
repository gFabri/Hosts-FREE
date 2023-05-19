package com.github.bfabri.hosts.managers;

import com.github.bfabri.hosts.ConfigHandler;
import com.github.bfabri.hosts.Hosts;
import com.github.bfabri.hosts.arenas.Arena;
import com.github.bfabri.hosts.arenas.kits.Kit;
import com.github.bfabri.hosts.utils.CustomItem;
import com.github.bfabri.hosts.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class ArenaManager {

    private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
    private final Type type = (new TypeToken<HashMap<String, Arena>>() {

    }).getType();
    @Getter
    private HashMap<String, Arena> arenas = new HashMap<>();

    public ArenaManager() {
        loadArenas();
    }

    public void createArena(String arenaName, String gameName) {
        Arena arena = new Arena(arenaName, gameName);
        arenas.put(arenaName, arena);
        saveArenas();
    }

    public void deleteArena(String arenaName) {
        arenas.remove(arenaName);
        saveArenas();
    }

    public Kit getKit(Arena arena, String type) {
	    for (Kit kit : arena.getKits()) {
		    if (kit.getType().equalsIgnoreCase(type)) {
			    return kit;
		    }
	    }
	    return null;
    }

    public Inventory getArenasInInv(String gameName) {
	    String invTitle = Utils.translate(ConfigHandler.Configs.ITEMS.getConfig().getString("HOST.invTitles.arena"));
	    int invSize = (int) Math.ceil(arenas.size() / 9.0) * 9;
	    Inventory arenasInventory = Bukkit.createInventory(null, invSize, invTitle);

	    arenas.entrySet().stream()
			    .filter(entry -> entry.getValue().isConfigured() && entry.getValue().getGameName().equalsIgnoreCase(gameName) &&
					    (entry.getValue().getModesName().contains("ALL") ||
							    entry.getValue().getModesName().contains(Hosts.getInstance().getGameManager().getMode().toUpperCase())))
			    .map(entry -> new CustomItem(Material.PAPER, 1, 0).setName("&7> &e" + entry.getKey() + " &7<").create())
			    .forEach(arenasInventory::addItem);


        return arenasInventory;
    }

    private void loadArenas() {
        try {
            HashMap<String, Arena> json = gson.fromJson(new FileReader(ConfigHandler.Configs.ARENAS.getFile()), type);
            if (json != null) {
                arenas = json;

                arenas.forEach((name, arena) -> {
                    arena.setConfigured(arena.getArea() != null && arena.getPreSpawn() != null && arena.getServerSpawn() != null && arena.getWorldName() != null && !arena.getModesName().isEmpty());
                    if (!arena.isConfigured()) {
                        Bukkit.getConsoleSender().sendMessage(Utils.PREFIX + Utils.translate("&a" + name + "&c is misconfigured"));
                    }
                });

                long arenaCount = arenas.values().stream().filter(Arena::isConfigured).count();
                Bukkit.getConsoleSender().sendMessage(Utils.PREFIX + Utils.translate("&eSuccessfully loaded &7" + arenaCount + (arenaCount > 1 ? " &earenas" : " &earena")));
            }
        } catch (FileNotFoundException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "arenas file not found");
        }
        saveArenas();
    }

    public void saveArenas() {
        try (FileWriter writer = new FileWriter(ConfigHandler.Configs.ARENAS.getFile())) {
            this.gson.toJson(arenas, writer);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "An error occurred");
        }
    }

    public Arena getArena(String arenaName) {
        if (arenas.containsKey(arenaName)) {
            return arenas.get(arenaName);
        }
        return null;
    }
}
