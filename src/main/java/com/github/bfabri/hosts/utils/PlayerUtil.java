package com.github.bfabri.hosts.utils;

import com.github.bfabri.hosts.ConfigHandler;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerUtil {

    @Getter
    private HashMap<UUID, Long> playersCooldowns = new HashMap<UUID, Long>();

    public void sendTitle(Player player, String title, String subtitle) {
        if (GameUtils.getVersion() > 11) {
            player.sendTitle(Utils.translate(title), Utils.translate(subtitle), 0, 40, 60);
        }
    }

    public static boolean isEmpty(Inventory inventory, boolean checkArmour) {
        boolean result = true;

        for (int i = 0; i < inventory.getContents().length; i++) {
            ItemStack itemStack = inventory.getContents()[i];
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                result = false;
                break;
            }
        }
        if (!result) {
            return false;
        }
        if (checkArmour && inventory instanceof PlayerInventory) {
            for (int i = 0; i < ((PlayerInventory) inventory).getArmorContents().length; i++) {
                ItemStack itemStack = ((PlayerInventory) inventory).getArmorContents()[i];
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public Long getPlayerCooldown(Player player) {
        if (getPlayersCooldowns().getOrDefault(player.getUniqueId(), 0L) < System.currentTimeMillis()) {
            return 0L;
        } else {
            return (getPlayersCooldowns().getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis());
        }
    }

    public boolean isInCooldown(Player player) {
        return getPlayerCooldown(player) > 0;
    }

    public void setCooldown(Player player) {
        ConfigHandler.Configs.CONFIG.getConfig().getStringList("HOST.CUSTOM_COOLDOWNS").forEach(cooldownsPermission -> {
            String time = cooldownsPermission.split("\\.")[cooldownsPermission.split("\\.").length - 1];
            if (player.hasPermission("hosts.cooldown.bypass")) {
                playersCooldowns.put(player.getUniqueId(), 0L);
            } else if (player.hasPermission(cooldownsPermission)) {
                playersCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (TimeUnit.MINUTES.toMillis(Long.parseLong(time))));
            } else {
                playersCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (TimeUnit.MINUTES.toMillis(120)));
            }
        });
    }
}
