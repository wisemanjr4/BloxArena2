package com.bloxarena.map;

import com.bloxarena.game.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

/**
 * 1マップの設定。手動配置のみサポート。
 * 座標はすべて config.yml に永続保存される。
 */
public class MapConfig {

    private final String id;
    private String worldName;
    private String displayName = null;

    private Location redSpawnMin, redSpawnMax;
    private Location blueSpawnMin, blueSpawnMax;
    private Location center;
    private Location lobby;

    // ─── ゲートシステム ───
    private Location redGateMin, redGateMax;
    private Location blueGateMin, blueGateMax;
    private Material gateMaterial = Material.BARRIER;

    // ─── OOB ───
    private Location oobMin, oobMax;

    // ─── 新モード対応フィールド ───
    private final List<GameMode> supportedModes = new ArrayList<>(List.of(GameMode.BATTLE_ARENA));

    // 爆破ミッション
    private Location bombSite;
    private Location defusePoint;

    // 占領戦
    private final List<DomPoint> dominationPoints = new ArrayList<>();

    // CTF
    private Location redFlagLocation;
    private Location blueFlagLocation;
    private Location redReturnLocation;
    private Location blueReturnLocation;

    public MapConfig(String id, String worldName) {
        this.id = id;
        this.worldName = worldName;
    }

    // ─── Validation ───

    public boolean hasOob() { return oobMin != null && oobMax != null; }

    public boolean isReady() {
        return worldName != null
            && redSpawnMin != null && redSpawnMax != null
            && blueSpawnMin != null && blueSpawnMax != null
            && center != null
            && lobby != null;
    }

    public boolean isReadyFor(GameMode mode) {
        if (!isReady()) return false;
        return switch (mode) {
            case BATTLE_ARENA -> true;
            case TEAM_DEATHMATCH -> true;
            case BOMB_MISSION -> bombSite != null && defusePoint != null;
            case DOMINATION -> !dominationPoints.isEmpty();
            case CAPTURE_THE_FLAG -> redFlagLocation != null && blueFlagLocation != null
                && redReturnLocation != null && blueReturnLocation != null;
        };
    }

    public String getMissingFields() {
        StringBuilder sb = new StringBuilder();
        if (worldName == null)    sb.append("world ");
        if (redSpawnMin == null || redSpawnMax == null)  sb.append("red_spawn_zone ");
        if (blueSpawnMin == null || blueSpawnMax == null) sb.append("blue_spawn_zone ");
        if (center == null)       sb.append("center ");
        if (lobby == null)        sb.append("lobby ");
        return sb.toString().trim();
    }

    // ─── Getters / Setters: 基本 ───

    public String getId()         { return id; }
    public String getDisplayName() { return displayName != null ? displayName : id; }
    public void setDisplayName(String name) { this.displayName = name; }
    public String getWorldName()  { return worldName; }
    public void setWorldName(String w) { this.worldName = w; }
    public String getWorld()      { return worldName; }

    public Location getRedSpawnMin()  { return redSpawnMin; }
    public Location getRedSpawnMax()  { return redSpawnMax; }
    public void setRedSpawnMin(Location l) { this.redSpawnMin = l.clone(); }
    public void setRedSpawnMax(Location l) { this.redSpawnMax = l.clone(); }

    public Location getBlueSpawnMin() { return blueSpawnMin; }
    public Location getBlueSpawnMax() { return blueSpawnMax; }
    public void setBlueSpawnMin(Location l) { this.blueSpawnMin = l.clone(); }
    public void setBlueSpawnMax(Location l) { this.blueSpawnMax = l.clone(); }

    public Location getCenter() { return center; }
    public void setCenter(Location l) { this.center = l.clone(); }

    public Location getLobby()  { return lobby; }
    public void setLobby(Location l) { this.lobby = l.clone(); }

    // ─── Getters / Setters: ゲート ───

    public Location getRedGateMin()  { return redGateMin; }
    public Location getRedGateMax()  { return redGateMax; }
    public void setRedGateMin(Location l)  { this.redGateMin  = l.clone(); }
    public void setRedGateMax(Location l)  { this.redGateMax  = l.clone(); }

    public Location getBlueGateMin() { return blueGateMin; }
    public Location getBlueGateMax() { return blueGateMax; }
    public void setBlueGateMin(Location l) { this.blueGateMin = l.clone(); }
    public void setBlueGateMax(Location l) { this.blueGateMax = l.clone(); }

    public Material getGateMaterial() { return gateMaterial; }
    public void setGateMaterial(Material m) { this.gateMaterial = m; }

    public boolean hasGate() {
        return (redGateMin != null && redGateMax != null)
            || (blueGateMin != null && blueGateMax != null);
    }

    // ─── Getters / Setters: OOB ───

    public Location getOobMin() { return oobMin; }
    public Location getOobMax() { return oobMax; }
    public void setOobMin(Location l) { this.oobMin = l; }
    public void setOobMax(Location l) { this.oobMax = l; }

    // ─── Getters / Setters: 新モード ───

    public List<GameMode> getSupportedModes() { return Collections.unmodifiableList(supportedModes); }
    public void setSupportedModes(List<GameMode> modes) { supportedModes.clear(); supportedModes.addAll(modes); }
    public void addSupportedMode(GameMode mode) { if (!supportedModes.contains(mode)) supportedModes.add(mode); }

    public Location getBombSite() { return bombSite; }
    public void setBombSite(Location l) { this.bombSite = l.clone(); }

    public Location getDefusePoint() { return defusePoint; }
    public void setDefusePoint(Location l) { this.defusePoint = l.clone(); }

    public List<DomPoint> getDominationPoints() { return Collections.unmodifiableList(dominationPoints); }
    public void addDomPoint(DomPoint dp) { dominationPoints.add(dp); }
    public void clearDomPoints() { dominationPoints.clear(); }

    public Location getRedFlagLocation() { return redFlagLocation; }
    public void setRedFlagLocation(Location l) { this.redFlagLocation = l.clone(); }
    public Location getBlueFlagLocation() { return blueFlagLocation; }
    public void setBlueFlagLocation(Location l) { this.blueFlagLocation = l.clone(); }
    public Location getRedReturnLocation() { return redReturnLocation; }
    public void setRedReturnLocation(Location l) { this.redReturnLocation = l.clone(); }
    public Location getBlueReturnLocation() { return blueReturnLocation; }
    public void setBlueReturnLocation(Location l) { this.blueReturnLocation = l.clone(); }

    // ─── DomPoint ───

    public static class DomPoint {
        private final Location center;
        private final double radius;

        public DomPoint(Location center, double radius) {
            this.center = center.clone();
            this.radius = radius;
        }

        public Location getCenter() { return center; }
        public double getRadius() { return radius; }
    }
}
