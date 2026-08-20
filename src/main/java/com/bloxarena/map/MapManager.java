/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.configuration.file.FileConfiguration
 */
package com.bloxarena.map;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameMode;
import com.bloxarena.map.MapConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class MapManager {
    private final BloxArenaPlugin plugin;
    private final List<MapConfig> maps = new ArrayList<MapConfig>();
    private String nextMapId = null;

    public MapManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        this.maps.clear();
        this.nextMapId = null;
        FileConfiguration cfg = this.plugin.getConfig();
        if (!cfg.isConfigurationSection("maps")) {
            return;
        }
        for (String id : cfg.getConfigurationSection("maps").getKeys(false)) {
            String modeStr;
            String base = "maps." + id + ".";
            String world = cfg.getString(base + "world", null);
            MapConfig mc = new MapConfig(id, world);
            String dname = cfg.getString(base + "display_name", null);
            if (dname != null) {
                mc.setDisplayName(dname);
            }
            if (cfg.contains(base + "red_spawn_zone.min.x")) {
                mc.setRedSpawnMin(this.loadLoc(cfg, base + "red_spawn_zone.min.", world));
                mc.setRedSpawnMax(this.loadLoc(cfg, base + "red_spawn_zone.max.", world));
            }
            if (cfg.contains(base + "blue_spawn_zone.min.x")) {
                mc.setBlueSpawnMin(this.loadLoc(cfg, base + "blue_spawn_zone.min.", world));
                mc.setBlueSpawnMax(this.loadLoc(cfg, base + "blue_spawn_zone.max.", world));
            }
            if (cfg.contains(base + "center.x")) {
                mc.setCenter(this.loadLoc(cfg, base + "center.", world));
            }
            if (cfg.contains(base + "lobby.x")) {
                mc.setLobby(this.loadLoc(cfg, base + "lobby.", world));
            }
            if (cfg.contains(base + "gate.red.min.x")) {
                mc.setRedGateMin(this.loadLoc(cfg, base + "gate.red.min.", world));
                mc.setRedGateMax(this.loadLoc(cfg, base + "gate.red.max.", world));
            }
            if (cfg.contains(base + "gate.blue.min.x")) {
                mc.setBlueGateMin(this.loadLoc(cfg, base + "gate.blue.min.", world));
                mc.setBlueGateMax(this.loadLoc(cfg, base + "gate.blue.max.", world));
            }
            String matName = cfg.getString(base + "gate.material", "BARRIER");
            try {
                mc.setGateMaterial(Material.valueOf((String)matName));
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (cfg.contains(base + "oob.min.x")) {
                mc.setOobMin(this.loadLoc(cfg, base + "oob.min.", world));
                mc.setOobMax(this.loadLoc(cfg, base + "oob.max.", world));
            }
            if (cfg.contains(base + "bomb_site.x")) {
                mc.setBombSite(this.loadLoc(cfg, base + "bomb_site.", world));
            }
            if (cfg.contains(base + "defuse_point.x")) {
                mc.setDefusePoint(this.loadLoc(cfg, base + "defuse_point.", world));
            }
            if ((modeStr = cfg.getString(base + "supported_modes", null)) != null && !modeStr.isEmpty()) {
                ArrayList<GameMode> modes = new ArrayList<GameMode>();
                for (String s : modeStr.split(",")) {
                    try {
                        modes.add(GameMode.valueOf(s.trim()));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (!modes.isEmpty()) {
                    mc.setSupportedModes(modes);
                }
            }
            if (cfg.contains(base + "dom_points")) {
                mc.clearDomPoints();
                Set<String> indices = cfg.getConfigurationSection(base + "dom_points").getKeys(false);
                for (String idx : indices) {
                    String dp = base + "dom_points." + idx + ".";
                    Location c = this.loadLoc(cfg, dp + "center.", world);
                    double r = cfg.getDouble(dp + "radius", 5.0);
                    mc.addDomPoint(new MapConfig.DomPoint(c, r));
                }
            }
            if (cfg.contains(base + "red_flag_location.x")) {
                mc.setRedFlagLocation(this.loadLoc(cfg, base + "red_flag_location.", world));
            }
            if (cfg.contains(base + "blue_flag_location.x")) {
                mc.setBlueFlagLocation(this.loadLoc(cfg, base + "blue_flag_location.", world));
            }
            if (cfg.contains(base + "red_return_location.x")) {
                mc.setRedReturnLocation(this.loadLoc(cfg, base + "red_return_location.", world));
            }
            if (cfg.contains(base + "blue_return_location.x")) {
                mc.setBlueReturnLocation(this.loadLoc(cfg, base + "blue_return_location.", world));
            }
            this.maps.add(mc);
        }
        this.plugin.getLogger().info("\u30de\u30c3\u30d7 " + this.maps.size() + " \u4ef6\u3092\u30ed\u30fc\u30c9\u3057\u307e\u3057\u305f\u3002");
    }

    public void saveMap(MapConfig mc) {
        FileConfiguration cfg = this.plugin.getConfig();
        String base = "maps." + mc.getId() + ".";
        cfg.set(base + "world", (Object)mc.getWorldName());
        if (mc.getDisplayName() != null && !mc.getDisplayName().equals(mc.getId())) {
            cfg.set(base + "display_name", (Object)mc.getDisplayName());
        }
        if (mc.getRedSpawnMin() != null) {
            this.saveLoc(cfg, base + "red_spawn_zone.min.", mc.getRedSpawnMin());
            this.saveLoc(cfg, base + "red_spawn_zone.max.", mc.getRedSpawnMax());
        }
        if (mc.getBlueSpawnMin() != null) {
            this.saveLoc(cfg, base + "blue_spawn_zone.min.", mc.getBlueSpawnMin());
            this.saveLoc(cfg, base + "blue_spawn_zone.max.", mc.getBlueSpawnMax());
        }
        if (mc.getCenter() != null) {
            this.saveLoc(cfg, base + "center.", mc.getCenter());
        }
        if (mc.getLobby() != null) {
            this.saveLoc(cfg, base + "lobby.", mc.getLobby());
        }
        if (mc.getRedGateMin() != null) {
            this.saveLoc(cfg, base + "gate.red.min.", mc.getRedGateMin());
            this.saveLoc(cfg, base + "gate.red.max.", mc.getRedGateMax());
        }
        if (mc.getBlueGateMin() != null) {
            this.saveLoc(cfg, base + "gate.blue.min.", mc.getBlueGateMin());
            this.saveLoc(cfg, base + "gate.blue.max.", mc.getBlueGateMax());
        }
        cfg.set(base + "gate.material", (Object)mc.getGateMaterial().name());
        if (mc.getOobMin() != null) {
            this.saveLoc(cfg, base + "oob.min.", mc.getOobMin());
            this.saveLoc(cfg, base + "oob.max.", mc.getOobMax());
        }
        if (mc.getBombSite() != null) {
            this.saveLoc(cfg, base + "bomb_site.", mc.getBombSite());
        } else {
            cfg.set(base + "bomb_site", null);
        }
        if (mc.getDefusePoint() != null) {
            this.saveLoc(cfg, base + "defuse_point.", mc.getDefusePoint());
        } else {
            cfg.set(base + "defuse_point", null);
        }
        cfg.set(base + "supported_modes", (Object)mc.getSupportedModes().stream().map(Enum::name).reduce((a, b) -> a + "," + b).orElse(""));
        cfg.set(base + "dom_points", null);
        int idx = 0;
        for (MapConfig.DomPoint dp : mc.getDominationPoints()) {
            String dpBase = base + "dom_points." + idx + ".";
            this.saveLoc(cfg, dpBase + "center.", dp.getCenter());
            cfg.set(dpBase + "radius", (Object)dp.getRadius());
            ++idx;
        }
        if (mc.getRedFlagLocation() != null) {
            this.saveLoc(cfg, base + "red_flag_location.", mc.getRedFlagLocation());
        } else {
            cfg.set(base + "red_flag_location", null);
        }
        if (mc.getBlueFlagLocation() != null) {
            this.saveLoc(cfg, base + "blue_flag_location.", mc.getBlueFlagLocation());
        } else {
            cfg.set(base + "blue_flag_location", null);
        }
        if (mc.getRedReturnLocation() != null) {
            this.saveLoc(cfg, base + "red_return_location.", mc.getRedReturnLocation());
        } else {
            cfg.set(base + "red_return_location", null);
        }
        if (mc.getBlueReturnLocation() != null) {
            this.saveLoc(cfg, base + "blue_return_location.", mc.getBlueReturnLocation());
        } else {
            cfg.set(base + "blue_return_location", null);
        }
        this.plugin.saveConfig();
    }

    public boolean addMap(String id, String worldName) {
        if (this.getById(id) != null) {
            return false;
        }
        MapConfig mc = new MapConfig(id, worldName);
        this.maps.add(mc);
        this.saveMap(mc);
        return true;
    }

    public MapConfig selectMap() {
        List ready;
        if (this.nextMapId != null) {
            MapConfig mc = this.getById(this.nextMapId);
            this.nextMapId = null;
            if (mc != null && mc.isReady()) {
                return mc;
            }
        }
        if ((ready = this.maps.stream().filter(MapConfig::isReady).toList()).isEmpty()) {
            return null;
        }
        return (MapConfig)ready.get(new Random().nextInt(ready.size()));
    }

    public MapConfig selectMap(GameMode mode) {
        List ready;
        if (this.nextMapId != null) {
            MapConfig mc = this.getById(this.nextMapId);
            this.nextMapId = null;
            if (mc != null && mc.isReadyFor(mode)) {
                return mc;
            }
        }
        if ((ready = this.maps.stream().filter(m -> m.isReadyFor(mode)).toList()).isEmpty()) {
            return null;
        }
        return (MapConfig)ready.get(new Random().nextInt(ready.size()));
    }

    public void setNextMap(String id) {
        this.nextMapId = id;
    }

    public MapConfig getById(String id) {
        return this.maps.stream().filter(m -> m.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public List<MapConfig> getMaps() {
        return Collections.unmodifiableList(this.maps);
    }

    private Location loadLoc(FileConfiguration cfg, String prefix, String worldName) {
        double x = cfg.getDouble(prefix + "x", 0.0);
        double y = cfg.getDouble(prefix + "y", 64.0);
        double z = cfg.getDouble(prefix + "z", 0.0);
        float yaw = (float)cfg.getDouble(prefix + "yaw", 0.0);
        float pitch = (float)cfg.getDouble(prefix + "pitch", 0.0);
        World w = this.plugin.getServer().getWorld(worldName != null ? worldName : "world");
        return new Location(w, x, y, z, yaw, pitch);
    }

    private void saveLoc(FileConfiguration cfg, String prefix, Location loc) {
        cfg.set(prefix + "x", (Object)loc.getX());
        cfg.set(prefix + "y", (Object)loc.getY());
        cfg.set(prefix + "z", (Object)loc.getZ());
        cfg.set(prefix + "yaw", (Object)loc.getYaw());
        cfg.set(prefix + "pitch", (Object)loc.getPitch());
    }
}

