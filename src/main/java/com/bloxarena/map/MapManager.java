package com.bloxarena.map;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * マップの読み込み・保存・選択を管理する。
 * auto_generate は完全廃止。手動配置マップのみサポート。
 */
public class MapManager {

    private final BloxArenaPlugin plugin;
    private final List<MapConfig> maps = new ArrayList<>();
    private String nextMapId = null; // /ba setmap で指定された優先マップ

    public MapManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    // ─── 読み込み ───

    public void reload() {
        maps.clear();
        nextMapId = null;
        FileConfiguration cfg = plugin.getConfig();

        if (!cfg.isConfigurationSection("maps")) return;

        for (String id : cfg.getConfigurationSection("maps").getKeys(false)) {
            String base = "maps." + id + ".";
            String world = cfg.getString(base + "world", null);
            MapConfig mc = new MapConfig(id, world);
            String dname = cfg.getString(base + "display_name", null);
            if (dname != null) mc.setDisplayName(dname);

            // red spawn zone
            if (cfg.contains(base + "red_spawn_zone.min.x")) {
                mc.setRedSpawnMin(loadLoc(cfg, base + "red_spawn_zone.min.", world));
                mc.setRedSpawnMax(loadLoc(cfg, base + "red_spawn_zone.max.", world));
            }
            // blue spawn zone
            if (cfg.contains(base + "blue_spawn_zone.min.x")) {
                mc.setBlueSpawnMin(loadLoc(cfg, base + "blue_spawn_zone.min.", world));
                mc.setBlueSpawnMax(loadLoc(cfg, base + "blue_spawn_zone.max.", world));
            }
            // center
            if (cfg.contains(base + "center.x")) {
                mc.setCenter(loadLoc(cfg, base + "center.", world));
            }
            // lobby
            if (cfg.contains(base + "lobby.x")) {
                mc.setLobby(loadLoc(cfg, base + "lobby.", world));
            }

            // gate
            if (cfg.contains(base + "gate.red.min.x")) {
                mc.setRedGateMin(loadLoc(cfg, base + "gate.red.min.", world));
                mc.setRedGateMax(loadLoc(cfg, base + "gate.red.max.", world));
            }
            if (cfg.contains(base + "gate.blue.min.x")) {
                mc.setBlueGateMin(loadLoc(cfg, base + "gate.blue.min.", world));
                mc.setBlueGateMax(loadLoc(cfg, base + "gate.blue.max.", world));
            }
            String matName = cfg.getString(base + "gate.material", "BARRIER");
            try { mc.setGateMaterial(Material.valueOf(matName)); } catch (Exception ignored) {}

            // oob
            if (cfg.contains(base + "oob.min.x")) {
                mc.setOobMin(loadLoc(cfg, base + "oob.min.", world));
                mc.setOobMax(loadLoc(cfg, base + "oob.max.", world));
            }

            // bomb site
            if (cfg.contains(base + "bomb_site.x")) {
                mc.setBombSite(loadLoc(cfg, base + "bomb_site.", world));
            }
            // defuse point
            if (cfg.contains(base + "defuse_point.x")) {
                mc.setDefusePoint(loadLoc(cfg, base + "defuse_point.", world));
            }

            // supported modes
            String modeStr = cfg.getString(base + "supported_modes", null);
            if (modeStr != null && !modeStr.isEmpty()) {
                List<GameMode> modes = new ArrayList<>();
                for (String s : modeStr.split(",")) {
                    try { modes.add(GameMode.valueOf(s.trim())); } catch (Exception ignored) {}
                }
                if (!modes.isEmpty()) mc.setSupportedModes(modes);
            }

            // domination points
            if (cfg.contains(base + "dom_points")) {
                mc.clearDomPoints();
                Set<String> indices = cfg.getConfigurationSection(base + "dom_points").getKeys(false);
                for (String idx : indices) {
                    String dp = base + "dom_points." + idx + ".";
                    Location c = loadLoc(cfg, dp + "center.", world);
                    double r = cfg.getDouble(dp + "radius", 5);
                    mc.addDomPoint(new MapConfig.DomPoint(c, r));
                }
            }

            // CTF
            if (cfg.contains(base + "red_flag_location.x")) {
                mc.setRedFlagLocation(loadLoc(cfg, base + "red_flag_location.", world));
            }
            if (cfg.contains(base + "blue_flag_location.x")) {
                mc.setBlueFlagLocation(loadLoc(cfg, base + "blue_flag_location.", world));
            }
            if (cfg.contains(base + "red_return_location.x")) {
                mc.setRedReturnLocation(loadLoc(cfg, base + "red_return_location.", world));
            }
            if (cfg.contains(base + "blue_return_location.x")) {
                mc.setBlueReturnLocation(loadLoc(cfg, base + "blue_return_location.", world));
            }

            maps.add(mc);
        }
        plugin.getLogger().info("マップ " + maps.size() + " 件をロードしました。");
    }

    // ─── 保存 ───

    /** MapConfig の全座標を config.yml に書き込んで保存する */
    public void saveMap(MapConfig mc) {
        FileConfiguration cfg = plugin.getConfig();
        String base = "maps." + mc.getId() + ".";

        cfg.set(base + "world", mc.getWorldName());
        if (mc.getDisplayName() != null && !mc.getDisplayName().equals(mc.getId())) {
            cfg.set(base + "display_name", mc.getDisplayName());
        }

        if (mc.getRedSpawnMin() != null) {
            saveLoc(cfg, base + "red_spawn_zone.min.", mc.getRedSpawnMin());
            saveLoc(cfg, base + "red_spawn_zone.max.", mc.getRedSpawnMax());
        }
        if (mc.getBlueSpawnMin() != null) {
            saveLoc(cfg, base + "blue_spawn_zone.min.", mc.getBlueSpawnMin());
            saveLoc(cfg, base + "blue_spawn_zone.max.", mc.getBlueSpawnMax());
        }
        if (mc.getCenter() != null) {
            saveLoc(cfg, base + "center.", mc.getCenter());
        }
        if (mc.getLobby() != null) {
            saveLoc(cfg, base + "lobby.", mc.getLobby());
        }
        if (mc.getRedGateMin() != null) {
            saveLoc(cfg, base + "gate.red.min.", mc.getRedGateMin());
            saveLoc(cfg, base + "gate.red.max.", mc.getRedGateMax());
        }
        if (mc.getBlueGateMin() != null) {
            saveLoc(cfg, base + "gate.blue.min.", mc.getBlueGateMin());
            saveLoc(cfg, base + "gate.blue.max.", mc.getBlueGateMax());
        }
        cfg.set(base + "gate.material", mc.getGateMaterial().name());
        if (mc.getOobMin() != null) {
            saveLoc(cfg, base + "oob.min.", mc.getOobMin());
            saveLoc(cfg, base + "oob.max.", mc.getOobMax());
        }

        if (mc.getBombSite() != null) {
            saveLoc(cfg, base + "bomb_site.", mc.getBombSite());
        } else {
            cfg.set(base + "bomb_site", null);
        }
        if (mc.getDefusePoint() != null) {
            saveLoc(cfg, base + "defuse_point.", mc.getDefusePoint());
        } else {
            cfg.set(base + "defuse_point", null);
        }

        cfg.set(base + "supported_modes", mc.getSupportedModes().stream()
                .map(GameMode::name).reduce((a, b) -> a + "," + b).orElse(""));

        cfg.set(base + "dom_points", null);
        int idx = 0;
        for (MapConfig.DomPoint dp : mc.getDominationPoints()) {
            String dpBase = base + "dom_points." + idx + ".";
            saveLoc(cfg, dpBase + "center.", dp.getCenter());
            cfg.set(dpBase + "radius", dp.getRadius());
            idx++;
        }

        if (mc.getRedFlagLocation() != null) {
            saveLoc(cfg, base + "red_flag_location.", mc.getRedFlagLocation());
        } else {
            cfg.set(base + "red_flag_location", null);
        }
        if (mc.getBlueFlagLocation() != null) {
            saveLoc(cfg, base + "blue_flag_location.", mc.getBlueFlagLocation());
        } else {
            cfg.set(base + "blue_flag_location", null);
        }
        if (mc.getRedReturnLocation() != null) {
            saveLoc(cfg, base + "red_return_location.", mc.getRedReturnLocation());
        } else {
            cfg.set(base + "red_return_location", null);
        }
        if (mc.getBlueReturnLocation() != null) {
            saveLoc(cfg, base + "blue_return_location.", mc.getBlueReturnLocation());
        } else {
            cfg.set(base + "blue_return_location", null);
        }

        plugin.saveConfig();
    }

    // ─── マップ追加 ───

    /**
     * 新しいマップエントリを作成して config.yml に保存する。
     * @return 既に同名 ID が存在する場合 false
     */
    public boolean addMap(String id, String worldName) {
        if (getById(id) != null) return false;
        MapConfig mc = new MapConfig(id, worldName);
        maps.add(mc);
        saveMap(mc);
        return true;
    }

    // ─── 選択 ───

    /** /ba setmap で予約されたマップ、またはランダム選択 */
    public MapConfig selectMap() {
        if (nextMapId != null) {
            MapConfig mc = getById(nextMapId);
            nextMapId = null;
            if (mc != null && mc.isReady()) return mc;
        }
        List<MapConfig> ready = maps.stream().filter(MapConfig::isReady).toList();
        if (ready.isEmpty()) return null;
        return ready.get(new Random().nextInt(ready.size()));
    }

    /** 指定ゲームモード用のマップを選択する */
    public MapConfig selectMap(GameMode mode) {
        if (nextMapId != null) {
            MapConfig mc = getById(nextMapId);
            nextMapId = null;
            if (mc != null && mc.isReadyFor(mode)) return mc;
        }
        List<MapConfig> ready = maps.stream().filter(m -> m.isReadyFor(mode)).toList();
        if (ready.isEmpty()) return null;
        return ready.get(new Random().nextInt(ready.size()));
    }

    public void setNextMap(String id) { this.nextMapId = id; }

    // ─── ルックアップ ───

    public MapConfig getById(String id) {
        return maps.stream().filter(m -> m.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public List<MapConfig> getMaps() { return Collections.unmodifiableList(maps); }

    // ─── 内部ユーティリティ ───

    private Location loadLoc(FileConfiguration cfg, String prefix, String worldName) {
        double x = cfg.getDouble(prefix + "x", 0);
        double y = cfg.getDouble(prefix + "y", 64);
        double z = cfg.getDouble(prefix + "z", 0);
        float yaw   = (float) cfg.getDouble(prefix + "yaw", 0);
        float pitch = (float) cfg.getDouble(prefix + "pitch", 0);
        World w = plugin.getServer().getWorld(worldName != null ? worldName : "world");
        return new Location(w, x, y, z, yaw, pitch);
    }

        private void saveLoc(FileConfiguration cfg, String prefix, Location loc) {
        cfg.set(prefix + "x", loc.getX());
        cfg.set(prefix + "y", loc.getY());
        cfg.set(prefix + "z", loc.getZ());
        cfg.set(prefix + "yaw",   (double) loc.getYaw());
        cfg.set(prefix + "pitch", (double) loc.getPitch());
    }
}
