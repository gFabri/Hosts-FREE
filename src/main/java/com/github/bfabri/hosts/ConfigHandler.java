package com.github.bfabri.hosts;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigHandler {

    private static ConfigHandler instance;
    private static JavaPlugin plugin;

    public ConfigHandler(JavaPlugin plugins) {
        plugin = plugins;
        instance = this;
        createConfigs();
    }

    private void createConfigs() {
        for (Configs config : Configs.values()) {
            config.init(this);
        }
    }

    public FileConfiguration createConfig(String name) {
        File config = new File(plugin.getDataFolder(), name);

        if (!config.exists()) {
            config.getParentFile().mkdirs();
            plugin.saveResource(name, false);
        }

        FileConfiguration configRet = new YamlConfiguration();

        try {
            configRet.load(config);
            return configRet;
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void saveConfig(FileConfiguration config, String name) {
        try {
            config.save(new File(plugin.getDataFolder(), name));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig(FileConfiguration config, String name) {
        try {
            config.load(new File(plugin.getDataFolder(), name));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum Configs {
        CONFIG("config.yml"),
        ARENAS("arenas.json"),
        REWARDS("rewards.json"),
        LANG("lang.yml"),
        ITEMS("items.yml");

        private final String name;
        private FileConfiguration config;

        Configs(String name) {
            this.name = name;
        }

        public void init(ConfigHandler handler) {
            this.config = handler.createConfig(name);
        }

        public FileConfiguration getConfig() {
            return config;
        }

        public File getFile() {
            return new File(plugin.getDataFolder(), name);
        }

        public void saveConfig() {
            instance.saveConfig(config, name);
        }

        public void reloadConfig() {
            instance.reloadConfig(config, name);
        }
    }
}