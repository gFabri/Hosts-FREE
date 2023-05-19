package com.github.bfabri.hosts.arenas;

import com.github.bfabri.hosts.arenas.kits.Kit;
import com.github.bfabri.hosts.utils.Cuboid;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Arena {

    @Getter
    private String gameName;
    @Getter
    private String arenaName;
    @Getter
    private ArrayList<String> modesName;
    @Setter
    @Getter
    private String worldName;
    @Setter
    @Getter
    private Map<String, Object> area;
    @Setter
    @Getter
    private Map<String, Object> serverSpawn;
    @Setter
    @Getter
    private Map<String, Object> preSpawn;
    @Setter
    @Getter
    private Map<String, Object> spectatorLocation;
    @Setter
    @Getter
    private List<Map<String, Object>> locations;
    @Setter
    @Getter
    private ArrayList<Kit> kits;
    @Setter
    @Getter
    private boolean hasKit;
    @Getter
    @Setter
    private boolean configured;

    public Arena(String arenaName, String gameName) {
        this.gameName = gameName;
        this.arenaName = arenaName;
        this.configured = false;
        this.setHasKit(false);
        this.modesName = new ArrayList<>();
        this.locations = new ArrayList<>();
        this.kits = new ArrayList<>();
    }
}
