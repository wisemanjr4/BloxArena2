/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 */
package com.bloxarena.map;

import com.bloxarena.game.GameMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;

public class MapConfig {
    private final String id;
    private String worldName;
    private String displayName = null;
    private Location redSpawnMin;
    private Location redSpawnMax;
    private Location blueSpawnMin;
    private Location blueSpawnMax;
    private Location center;
    private Location lobby;
    private Location redGateMin;
    private Location redGateMax;
    private Location blueGateMin;
    private Location blueGateMax;
    private Material gateMaterial = Material.BARRIER;
    private Location oobMin;
    private Location oobMax;
    private final List<GameMode> supportedModes = new ArrayList<GameMode>(List.of(GameMode.BATTLE_ARENA));
    private Location bombSite;
    private Location defusePoint;
    private final List<DomPoint> dominationPoints = new ArrayList<DomPoint>();
    private Location redFlagLocation;
    private Location blueFlagLocation;
    private Location redReturnLocation;
    private Location blueReturnLocation;

    public MapConfig(String id, String worldName) {
        this.id = id;
        this.worldName = worldName;
    }

    public boolean hasOob() {
        return this.oobMin != null && this.oobMax != null;
    }

    public boolean isReady() {
        return this.worldName != null && this.redSpawnMin != null && this.redSpawnMax != null && this.blueSpawnMin != null && this.blueSpawnMax != null && this.center != null && this.lobby != null;
    }

    public boolean isReadyFor(GameMode mode) {
        if (!this.isReady()) {
            return false;
        }
        return switch (mode) {
            case BATTLE_ARENA -> true;
            case TEAM_DEATHMATCH -> true;
            case FFA -> true;
            case BOMB_MISSION -> {
                if (this.bombSite != null && this.defusePoint != null) {
                    yield true;
                }
                yield false;
            }
            case DOMINATION -> {
                if (!this.dominationPoints.isEmpty()) {
                    yield true;
                }
                yield false;
            }
            case CAPTURE_THE_FLAG -> this.redFlagLocation != null && this.blueFlagLocation != null && this.redReturnLocation != null && this.blueReturnLocation != null;
            default -> throw new IncompatibleClassChangeError();
        };
    }

    public String getMissingFields() {
        StringBuilder sb = new StringBuilder();
        if (this.worldName == null) {
            sb.append("world ");
        }
        if (this.redSpawnMin == null || this.redSpawnMax == null) {
            sb.append("red_spawn_zone ");
        }
        if (this.blueSpawnMin == null || this.blueSpawnMax == null) {
            sb.append("blue_spawn_zone ");
        }
        if (this.center == null) {
            sb.append("center ");
        }
        if (this.lobby == null) {
            sb.append("lobby ");
        }
        return sb.toString().trim();
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName != null ? this.displayName : this.id;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public void setWorldName(String w) {
        this.worldName = w;
    }

    public String getWorld() {
        return this.worldName;
    }

    public Location getRedSpawnMin() {
        return this.redSpawnMin;
    }

    public Location getRedSpawnMax() {
        return this.redSpawnMax;
    }

    public void setRedSpawnMin(Location l) {
        this.redSpawnMin = l.clone();
    }

    public void setRedSpawnMax(Location l) {
        this.redSpawnMax = l.clone();
    }

    public Location getBlueSpawnMin() {
        return this.blueSpawnMin;
    }

    public Location getBlueSpawnMax() {
        return this.blueSpawnMax;
    }

    public void setBlueSpawnMin(Location l) {
        this.blueSpawnMin = l.clone();
    }

    public void setBlueSpawnMax(Location l) {
        this.blueSpawnMax = l.clone();
    }

    public Location getCenter() {
        return this.center;
    }

    public void setCenter(Location l) {
        this.center = l.clone();
    }

    public Location getLobby() {
        return this.lobby;
    }

    public void setLobby(Location l) {
        this.lobby = l.clone();
    }

    public Location getRedGateMin() {
        return this.redGateMin;
    }

    public Location getRedGateMax() {
        return this.redGateMax;
    }

    public void setRedGateMin(Location l) {
        this.redGateMin = l.clone();
    }

    public void setRedGateMax(Location l) {
        this.redGateMax = l.clone();
    }

    public Location getBlueGateMin() {
        return this.blueGateMin;
    }

    public Location getBlueGateMax() {
        return this.blueGateMax;
    }

    public void setBlueGateMin(Location l) {
        this.blueGateMin = l.clone();
    }

    public void setBlueGateMax(Location l) {
        this.blueGateMax = l.clone();
    }

    public Material getGateMaterial() {
        return this.gateMaterial;
    }

    public void setGateMaterial(Material m) {
        this.gateMaterial = m;
    }

    public boolean hasGate() {
        return this.redGateMin != null && this.redGateMax != null || this.blueGateMin != null && this.blueGateMax != null;
    }

    public Location getOobMin() {
        return this.oobMin;
    }

    public Location getOobMax() {
        return this.oobMax;
    }

    public void setOobMin(Location l) {
        this.oobMin = l;
    }

    public void setOobMax(Location l) {
        this.oobMax = l;
    }

    public List<GameMode> getSupportedModes() {
        return Collections.unmodifiableList(this.supportedModes);
    }

    public void setSupportedModes(List<GameMode> modes) {
        this.supportedModes.clear();
        this.supportedModes.addAll(modes);
    }

    public void addSupportedMode(GameMode mode) {
        if (!this.supportedModes.contains((Object)mode)) {
            this.supportedModes.add(mode);
        }
    }

    public Location getBombSite() {
        return this.bombSite;
    }

    public void setBombSite(Location l) {
        this.bombSite = l.clone();
    }

    public Location getDefusePoint() {
        return this.defusePoint;
    }

    public void setDefusePoint(Location l) {
        this.defusePoint = l.clone();
    }

    public List<DomPoint> getDominationPoints() {
        return Collections.unmodifiableList(this.dominationPoints);
    }

    public void addDomPoint(DomPoint dp) {
        this.dominationPoints.add(dp);
    }

    public void clearDomPoints() {
        this.dominationPoints.clear();
    }

    public Location getRedFlagLocation() {
        return this.redFlagLocation;
    }

    public void setRedFlagLocation(Location l) {
        this.redFlagLocation = l.clone();
    }

    public Location getBlueFlagLocation() {
        return this.blueFlagLocation;
    }

    public void setBlueFlagLocation(Location l) {
        this.blueFlagLocation = l.clone();
    }

    public Location getRedReturnLocation() {
        return this.redReturnLocation;
    }

    public void setRedReturnLocation(Location l) {
        this.redReturnLocation = l.clone();
    }

    public Location getBlueReturnLocation() {
        return this.blueReturnLocation;
    }

    public void setBlueReturnLocation(Location l) {
        this.blueReturnLocation = l.clone();
    }

    public static class DomPoint {
        private final Location center;
        private final double radius;

        public DomPoint(Location center, double radius) {
            this.center = center.clone();
            this.radius = radius;
        }

        public Location getCenter() {
            return this.center;
        }

        public double getRadius() {
            return this.radius;
        }
    }
}

