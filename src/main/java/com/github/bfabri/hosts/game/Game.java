package com.github.bfabri.hosts.game;

import com.github.bfabri.hosts.arenas.Arena;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public abstract class Game {

    @Getter
    protected String name;

    @Getter
    @Setter
    protected BukkitTask generalTask;

    @Getter
    @Setter
    protected String mode;

    @Getter
    protected String displayName;

    @Getter
    protected int maxPlayers;

    @Getter
    protected int minPlayers;

    @Getter
    @Setter
    protected CommandSender hoster;

    @Getter
    protected ArrayList<GamePlayer> gamePlayers;

    @Getter
    @Setter
    protected long maxRoundTime;
    @Getter
    @Setter
    protected long startTime;
    @Getter
    @Setter
    protected Status currentStatus;
    @Getter
    @Setter
    protected RewardTypes selectedReward;
    @Getter
    @Setter
    protected Arena arena;

    public Game(String name, String displayName, int minPlayers, int maxPlayers, int startTime, int maxRoundTime) {
        this.name = name;
        this.displayName = displayName;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.startTime = startTime;
        this.maxRoundTime = maxRoundTime;
        this.gamePlayers = new ArrayList<>();

        setCurrentStatus(Status.OFFLINE);
    }

    public Game(String name, String mode, String displayName, int minPlayers, int maxPlayers, int startTime, int maxRoundTime) {
        this.name = name;
        this.mode = mode;
        this.displayName = displayName;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.startTime = startTime;
        this.maxRoundTime = maxRoundTime;
        this.gamePlayers = new ArrayList<>();

        setCurrentStatus(Status.OFFLINE);
    }

    public abstract void onStart();

    public abstract void onStop();

    public abstract void onStartedGame();

    public abstract void onWin(GamePlayer gamePlayer);

    public abstract void onLoss(GamePlayer gamePlayer);

    public abstract void onWin(ArrayList<GamePlayer> gamePlayer);

    public abstract void onLoss(ArrayList<GamePlayer> gamePlayer);

    public abstract void loadInv(GamePlayer player);


    @Getter
    public enum Status {STARTING, STARTED, OFFLINE}

    @Getter
    public enum RewardTypes {NONE, DEFAULT, ITEMS, RANDOM}
}