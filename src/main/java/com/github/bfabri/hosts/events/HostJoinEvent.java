package com.github.bfabri.hosts.events;

import com.github.bfabri.hosts.game.Game;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HostJoinEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    @Getter
    private final Player player;

    @Getter
    private final Game game;

    public HostJoinEvent(Player sender, Game game) {
        super();
        this.player = sender;
        this.game = game;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public HandlerList getHandlers() {
        return handlerList;
    }
}