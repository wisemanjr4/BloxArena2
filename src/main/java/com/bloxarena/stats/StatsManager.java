package com.bloxarena.stats;

import com.bloxarena.BloxArenaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StatsManager {

    private final BloxArenaPlugin plugin;
    private final File file;
    private YamlConfiguration cfg;
    private final Map<UUID, PlayerStats> cache = new HashMap<>();

    public StatsManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        load();
    }

    // ─── 読み込み ───

    private void load() {
        cfg = YamlConfiguration.loadConfiguration(file);
        cache.clear();
        for (String key : cfg.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                PlayerStats s = new PlayerStats();
                s.kills   = cfg.getInt(key + ".kills");
                s.deaths  = cfg.getInt(key + ".deaths");
                s.wins    = cfg.getInt(key + ".wins");
                s.losses  = cfg.getInt(key + ".losses");
                s.damage  = cfg.getDouble(key + ".damage");
                // Load kit counts
                if (cfg.isConfigurationSection(key + ".kits")) {
                    for (String kitName : cfg.getConfigurationSection(key + ".kits").getKeys(false)) {
                        s.kitCounts.put(kitName, cfg.getInt(key + ".kits." + kitName));
                    }
                }
                cache.put(uuid, s);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    // ─── 保存 ───

    public void save() {
        cache.forEach((uuid, s) -> {
            String k = uuid.toString();
            cfg.set(k + ".kills",   s.kills);
            cfg.set(k + ".deaths",  s.deaths);
            cfg.set(k + ".wins",    s.wins);
            cfg.set(k + ".losses",  s.losses);
            cfg.set(k + ".damage",  s.damage);
            // Save kit counts
            cfg.set(k + ".kits", null);
            for (var entry : s.kitCounts.entrySet()) {
                cfg.set(k + ".kits." + entry.getKey(), entry.getValue());
            }
            // 名前キャッシュ（表示用）
            var p = Bukkit.getOfflinePlayer(uuid);
            if (p.getName() != null) cfg.set(k + ".name", p.getName());
        });
        try { cfg.save(file); } catch (IOException e) { plugin.getLogger().warning("stats.yml 保存失敗: " + e.getMessage()); }
    }

    // ─── データ更新 ───

    private PlayerStats get(UUID uuid) { return cache.computeIfAbsent(uuid, k -> new PlayerStats()); }

    public void addKill(UUID uuid)               { get(uuid).kills++; }
    public void addDeath(UUID uuid)              { get(uuid).deaths++; }
    public void addWin(UUID uuid)                { get(uuid).wins++; }
    public void addLoss(UUID uuid)               { get(uuid).losses++; }
    public void addDamage(UUID uuid, double dmg) { get(uuid).damage += dmg; }
    public void addKitPick(UUID uuid, String kitName) { get(uuid).kitCounts.merge(kitName, 1, Integer::sum); }

    // ─── 参照 ───

    public PlayerStats getStats(UUID uuid) { return get(uuid); }

    /** /ba top 用: 指定フィールドでソートされた上位N件 */
    public List<Map.Entry<UUID, PlayerStats>> getTop(String field, int limit) {
        Comparator<Map.Entry<UUID, PlayerStats>> cmp = switch (field) {
            case "wins"   -> Comparator.comparingInt(e -> -e.getValue().wins);
            case "kd"     -> Comparator.comparingDouble(e -> -e.getValue().getKD());
            case "damage" -> Comparator.comparingDouble(e -> -e.getValue().damage);
            default       -> Comparator.comparingInt(e -> -e.getValue().kills);
        };
        return cache.entrySet().stream().sorted(cmp).limit(limit).collect(Collectors.toList());
    }

    /** /ba top kits 用: 全プレイヤーのキット使用回数集計 */
    public List<Map.Entry<String, Integer>> getKitTop(int limit) {
        Map<String, Integer> total = new HashMap<>();
        for (PlayerStats s : cache.values()) {
            for (var e : s.kitCounts.entrySet()) {
                total.merge(e.getKey(), e.getValue(), Integer::sum);
            }
        }
        return total.entrySet().stream()
            .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
            .limit(limit).collect(Collectors.toList());
    }

    public String getName(UUID uuid) {
        String cached = cfg.getString(uuid + ".name");
        if (cached != null) return cached;
        var p = Bukkit.getOfflinePlayer(uuid);
        return p.getName() != null ? p.getName() : uuid.toString().substring(0, 8);
    }
}
