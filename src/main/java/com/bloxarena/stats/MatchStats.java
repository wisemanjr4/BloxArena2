package com.bloxarena.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 1試合分の統計（揮発性・試合終了後に StatsManager へ反映） */
public class MatchStats {

    private final Map<UUID, Integer> kills  = new HashMap<>();
    private final Map<UUID, Double>  damage = new HashMap<>();

    public void addKill(UUID uuid)              { kills.merge(uuid, 1, Integer::sum); }
    public void addDamage(UUID uuid, double dmg){ damage.merge(uuid, dmg, Double::sum); }

    public int    getKills(UUID uuid)  { return kills.getOrDefault(uuid, 0); }
    public double getDamage(UUID uuid) { return damage.getOrDefault(uuid, 0.0); }

    /** MVP = 最多キル → 同数ならダメージで判定 */
    public UUID getMVP() {
        return kills.entrySet().stream()
            .max((a, b) -> {
                int cmp = Integer.compare(a.getValue(), b.getValue());
                return cmp != 0 ? cmp : Double.compare(damage.getOrDefault(a.getKey(), 0.0),
                                                         damage.getOrDefault(b.getKey(), 0.0));
            })
            .map(Map.Entry::getKey).orElse(null);
    }

    public UUID getMostDamage() {
        return damage.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse(null);
    }

    public Map<UUID, Integer> getKillsMap()  { return kills; }
    public Map<UUID, Double>  getDamageMap() { return damage; }
}
