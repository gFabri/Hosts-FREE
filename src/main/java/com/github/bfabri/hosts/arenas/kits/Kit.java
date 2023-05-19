package com.github.bfabri.hosts.arenas.kits;

import com.github.bfabri.hosts.utils.KitItemStack;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Kit {

    @Getter
    private final String name;

    @Getter
    private final String type;

    @Getter
    private Map<Integer, Object> armor = new HashMap<>();

    @Getter
    private Map<Integer, Object> inventory = new HashMap<>();

    public Kit(String name, Player player, String type) {

        for (int i = 0; i < player.getInventory().getArmorContents().length; i++) {
            if (player.getInventory().getArmorContents()[i] != null) {
                this.armor.put(i, new KitItemStack(player.getInventory().getArmorContents()[i]).serialize());
            }
        }

        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            if (player.getInventory().getContents()[i] != null) {
                this.inventory.put(i, new KitItemStack(player.getInventory().getContents()[i]).serialize());
            }
        }

        this.type = type;
        this.name = name;
    }

}