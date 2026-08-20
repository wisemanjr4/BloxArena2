/*
 * Decompiled with CFR 0.152.
 */
package com.bloxarena.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MatchStats {
    private final Map<UUID, Integer> kills = new HashMap<UUID, Integer>();
    private final Map<UUID, Double> damage = new HashMap<UUID, Double>();

    public void addKill(UUID uuid) {
        this.kills.merge(uuid, 1, Integer::sum);
    }

    public void addDamage(UUID uuid, double dmg) {
        this.damage.merge(uuid, dmg, Double::sum);
    }

    public int getKills(UUID uuid) {
        return this.kills.getOrDefault(uuid, 0);
    }

    public double getDamage(UUID uuid) {
        return this.damage.getOrDefault(uuid, 0.0);
    }

    public UUID getMVP() {
        return this.kills.entrySet().stream().max((a, b) -> {
            int cmp = Integer.compare((Integer)a.getValue(), (Integer)b.getValue());
            return cmp != 0 ? cmp : Double.compare(this.damage.getOrDefault(a.getKey(), 0.0), this.damage.getOrDefault(b.getKey(), 0.0));
        }).map(Map.Entry::getKey).orElse(null);
    }

    public UUID getMostDamage() {
        return this.damage.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    public Map<UUID, Integer> getKillsMap() {
        return this.kills;
    }

    public Map<UUID, Double> getDamageMap() {
        return this.damage;
    }
}

