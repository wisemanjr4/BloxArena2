/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package com.bloxarena.stats;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.stats.PlayerStats;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

public class StatsManager {
    private final BloxArenaPlugin plugin;
    private final File file;
    private YamlConfiguration cfg;
    private final Map<UUID, PlayerStats> cache = new HashMap<UUID, PlayerStats>();
    private final Map<UUID, Map<String, Integer>> playerKitUsage = new HashMap<UUID, Map<String, Integer>>();
    private final Set<UUID> debugPlayers = new HashSet<UUID>();

    public StatsManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        this.load();
    }

    private void load() {
        this.cfg = YamlConfiguration.loadConfiguration((File)this.file);
        this.cache.clear();
        this.playerKitUsage.clear();
        for (String key : this.cfg.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                PlayerStats s = new PlayerStats();
                s.kills = this.cfg.getInt(key + ".kills");
                s.deaths = this.cfg.getInt(key + ".deaths");
                s.wins = this.cfg.getInt(key + ".wins");
                s.losses = this.cfg.getInt(key + ".losses");
                s.damage = this.cfg.getDouble(key + ".damage");
                if (this.cfg.isConfigurationSection(key + ".kits")) {
                    for (String kitName : this.cfg.getConfigurationSection(key + ".kits").getKeys(false)) {
                        int count = this.cfg.getInt(key + ".kits." + kitName);
                        s.kitCounts.put(kitName, count);
                        this.playerKitUsage.computeIfAbsent(uuid, k -> new HashMap()).put(kitName, count);
                    }
                }
                this.cache.put(uuid, s);
            }
            catch (IllegalArgumentException illegalArgumentException) {}
        }
    }

    public void save() {
        this.cache.forEach((uuid, s) -> {
            String k = uuid.toString();
            this.cfg.set(k + ".kills", (Object)s.kills);
            this.cfg.set(k + ".deaths", (Object)s.deaths);
            this.cfg.set(k + ".wins", (Object)s.wins);
            this.cfg.set(k + ".losses", (Object)s.losses);
            this.cfg.set(k + ".damage", (Object)s.damage);
            this.cfg.set(k + ".kits", null);
            for (Map.Entry<String, Integer> entry : s.kitCounts.entrySet()) {
                this.cfg.set(k + ".kits." + entry.getKey(), (Object)entry.getValue());
            }
            OfflinePlayer p = Bukkit.getOfflinePlayer((UUID)uuid);
            if (p.getName() != null) {
                this.cfg.set(k + ".name", (Object)p.getName());
            }
        });
        try {
            this.cfg.save(this.file);
        }
        catch (IOException e) {
            this.plugin.getLogger().warning("stats.yml \u4fdd\u5b58\u5931\u6557: " + e.getMessage());
        }
    }

    private PlayerStats get(UUID uuid) {
        return this.cache.computeIfAbsent(uuid, k -> new PlayerStats());
    }

    public void addKill(UUID uuid) {
        if (!this.debugPlayers.contains(uuid)) {
            ++this.get((UUID)uuid).kills;
        }
    }

    public void addDeath(UUID uuid) {
        if (!this.debugPlayers.contains(uuid)) {
            ++this.get((UUID)uuid).deaths;
        }
    }

    public void addWin(UUID uuid) {
        if (!this.debugPlayers.contains(uuid)) {
            ++this.get((UUID)uuid).wins;
        }
    }

    public void addLoss(UUID uuid) {
        if (!this.debugPlayers.contains(uuid)) {
            ++this.get((UUID)uuid).losses;
        }
    }

    public void addDamage(UUID uuid, double dmg) {
        if (!this.debugPlayers.contains(uuid)) {
            this.get((UUID)uuid).damage += dmg;
        }
    }

    public void addKitPick(UUID uuid, String kitName) {
        if (this.debugPlayers.contains(uuid)) {
            return;
        }
        this.get((UUID)uuid).kitCounts.merge(kitName, 1, Integer::sum);
        this.playerKitUsage.computeIfAbsent(uuid, k -> new HashMap<String, Integer>()).merge(kitName, 1, Integer::sum);
    }

    public PlayerStats getStats(UUID uuid) {
        return this.get(uuid);
    }

    public List<Map.Entry<UUID, PlayerStats>> getTop(String field, int limit) {
        Comparator<Map.Entry> cmp = switch (field) {
            case "wins" -> Comparator.comparingInt(e -> -((PlayerStats)e.getValue()).wins);
            case "kd" -> Comparator.comparingDouble(e -> -((PlayerStats)e.getValue()).getKD());
            case "damage" -> Comparator.comparingDouble(e -> -((PlayerStats)e.getValue()).damage);
            default -> Comparator.comparingInt(e -> -((PlayerStats)e.getValue()).kills);
        };
        return this.cache.entrySet().stream().sorted(cmp).limit(limit).collect(Collectors.toList());
    }

    public List<Map.Entry<String, Integer>> getKitTop(int limit) {
        HashMap<String, Integer> total = new HashMap<String, Integer>();
        for (PlayerStats s : this.cache.values()) {
            for (Map.Entry<String, Integer> e : s.kitCounts.entrySet()) {
                total.merge(e.getKey(), e.getValue(), Integer::sum);
            }
        }
        return total.entrySet().stream().sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())).limit(limit).collect(Collectors.toList());
    }

    public boolean toggleDebug(UUID uuid) {
        if (this.debugPlayers.contains(uuid)) {
            this.debugPlayers.remove(uuid);
            return false;
        }
        this.debugPlayers.add(uuid);
        return true;
    }

    public boolean isDebug(UUID uuid) {
        return this.debugPlayers.contains(uuid);
    }

    public Map<String, List<Map.Entry<UUID, Integer>>> getKitUsageWithTopPlayers() {
        HashMap<String, Integer> kitTotals = new HashMap<String, Integer>();
        HashMap<String, Map<UUID, Integer>> kitPlayers = new HashMap<String, Map<UUID, Integer>>();
        for (Map.Entry<UUID, Map<String, Integer>> playerEntry : this.playerKitUsage.entrySet()) {
            UUID uid = playerEntry.getKey();
            for (Map.Entry<String, Integer> kitEntry : playerEntry.getValue().entrySet()) {
                String kitName = kitEntry.getKey();
                int count = kitEntry.getValue();
                kitTotals.merge(kitName, count, Integer::sum);
                kitPlayers.computeIfAbsent(kitName, k -> new HashMap<UUID, Integer>()).merge(uid, count, Integer::sum);
            }
        }
        LinkedHashMap<String, List<Map.Entry<UUID, Integer>>> result = new LinkedHashMap<String, List<Map.Entry<UUID, Integer>>>();
        List<Map.Entry<String, Integer>> sortedKits = kitTotals.entrySet().stream().sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())).collect(Collectors.toList());
        for (Map.Entry<String, Integer> kitEntry : sortedKits) {
            String kitName = kitEntry.getKey();
            List<Map.Entry<UUID, Integer>> players = kitPlayers.get(kitName).entrySet().stream().sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())).limit(3L).map(e -> new AbstractMap.SimpleEntry<UUID, Integer>(e.getKey(), e.getValue())).collect(Collectors.toList());
            result.put(kitName, players);
        }
        return result;
    }

    public String getName(UUID uuid) {
        String cached = this.cfg.getString(String.valueOf(uuid) + ".name");
        if (cached != null) {
            return cached;
        }
        OfflinePlayer p = Bukkit.getOfflinePlayer((UUID)uuid);
        return p.getName() != null ? p.getName() : uuid.toString().substring(0, 8);
    }
}

