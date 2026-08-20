/*
 * Decompiled with CFR 0.152.
 */
package com.bloxarena.stats;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats {
    public int kills;
    public int deaths;
    public int wins;
    public int losses;
    public double damage;
    public final Map<String, Integer> kitCounts = new HashMap<String, Integer>();

    public double getKD() {
        return this.deaths == 0 ? (double)this.kills : (double)this.kills / (double)this.deaths;
    }

    public int getGames() {
        return this.wins + this.losses;
    }

    public double getWinRate() {
        return this.getGames() == 0 ? 0.0 : (double)this.wins / (double)this.getGames() * 100.0;
    }
}

