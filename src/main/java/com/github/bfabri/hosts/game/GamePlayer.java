package com.github.bfabri.hosts.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GamePlayer {
    @Getter
    private final Player player;
    @Getter
    @Setter
    private String prevServer;
    @Getter
    private final Game game;
    @Getter
    private final Location location;
    @Getter
    @Setter
    private Status status;
    @Getter
    private ItemStack[] inventory;
    @Getter
    private ItemStack[] armor;

    public GamePlayer(Player player, Game game) {
        this.player = player;
        this.prevServer = null;
        this.game = game;
        this.location = player.getLocation();
        this.status = Status.LIVE;

        this.inventory = player.getInventory().getContents();
        this.armor = player.getInventory().getArmorContents();
    }

    public enum Status {LIVE, DEATH, SPECTATOR, DISCONNECTED}
}