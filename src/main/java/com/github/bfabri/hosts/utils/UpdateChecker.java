package com.github.bfabri.hosts.utils;

import com.github.bfabri.hosts.Hosts;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker implements Listener {

    private final String localPluginVersion;
    private String spigotPluginVersion;

    private static final int ID = 90518;
    private static final String ERR_MSG = "&cUpdate checker failed!";
    private static final String UPDATE_MSG = "&eA new update of Hosts is available at:&c https://www.spigotmc.org/resources/" + ID + "/updates";
    private static final long CHECK_INTERVAL = 12_00;

    public UpdateChecker() {
        this.localPluginVersion = Hosts.getInstance().getDescription().getVersion();
    }

    public void checkForUpdate() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTaskAsynchronously(Hosts.getInstance(), () -> {
                    try {
                        final HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + ID).openConnection();
                        connection.setRequestMethod("GET");
                        spigotPluginVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                    } catch (final IOException e) {
                        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ERR_MSG));
                        cancel();
                        return;
                    }

                    if (localPluginVersion.equals(spigotPluginVersion)) {
                        cancel();
                        return;
                    }

                    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', UPDATE_MSG));
                    notifyUpdateToPlayers();

                    cancel();
                });
            }
        }.runTaskTimer(Hosts.getInstance(), 0, CHECK_INTERVAL);
    }

    private void notifyUpdateToPlayers() {
        Bukkit.getScheduler().runTask(Hosts.getInstance(), () -> Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.NORMAL)
            public void onPlayerJoin(final PlayerJoinEvent event) {
                final Player player = event.getPlayer();
                if (!player.hasPermission("hosts.receiveupdates")) return;
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', UPDATE_MSG));
            }
        }, Hosts.getInstance()));
    }
}