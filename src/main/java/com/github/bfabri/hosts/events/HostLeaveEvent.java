package com.github.bfabri.hosts.events;

import com.github.bfabri.hosts.game.Game;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HostLeaveEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    @Getter
    private final Player player;

    @Getter
    public enum Types {ELIMINATION, LEAVED}

    @Getter
    @Setter
    private Types type;

    @Getter
    private final Game game;

    public HostLeaveEvent(Player sender, Game game, Types types) {
        super();
        this.player = sender;
        this.game = game;
        this.type = types;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public HandlerList getHandlers() {
        return handlerList;
    }
}