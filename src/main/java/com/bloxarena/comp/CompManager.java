package com.bloxarena.comp;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameMode;
import com.bloxarena.game.TeamColor;
import com.bloxarena.map.MapConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CompManager {
    private final BloxArenaPlugin plugin;
    private final Map<UUID, TeamColor> compTeams = new LinkedHashMap<UUID, TeamColor>();
    private final java.util.Set<UUID> readySet = new java.util.HashSet<UUID>();
    private final Map<TeamColor, Integer> compScore = new java.util.EnumMap<TeamColor, Integer>(TeamColor.class);
    private int compBo = 1;
    private boolean compActive = false;

    public CompManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.compScore.put(TeamColor.RED, 0);
        this.compScore.put(TeamColor.BLUE, 0);
    }

    public void setTeam(Player p, TeamColor team) {
        this.compTeams.put(p.getUniqueId(), team);
    }

    public boolean remove(UUID uid) {
        return this.compTeams.remove(uid) != null;
    }

    public void clear() {
        this.compTeams.clear();
    }

    public TeamColor getTeam(UUID uid) {
        return this.compTeams.get(uid);
    }

    public Map<UUID, TeamColor> view() {
        return Collections.unmodifiableMap(this.compTeams);
    }

    public String start(GameMode mode, MapConfig map) {
        List<UUID> red = new ArrayList<UUID>();
        List<UUID> blue = new ArrayList<UUID>();
        for (Map.Entry<UUID, TeamColor> e : this.compTeams.entrySet()) {
            Player p = Bukkit.getPlayer((UUID)e.getKey());
            if (p == null || !p.isOnline()) continue;
            if (e.getValue() == TeamColor.RED) {
                red.add(e.getKey());
            } else {
                blue.add(e.getKey());
            }
        }
        if (red.isEmpty() || blue.isEmpty()) {
            return "\u00a7c\u8d64/\u9752\u4e21\u30c1\u30fc\u30e0\u306b\u6700\u4f551\u4eba\u305a\u3064\u767b\u9332\u3057\u3066\u304f\u3060\u3055\u3044";
        }
        if (map == null) {
            return "\u00a7c\u30de\u30c3\u30d7\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093";
        }
        this.plugin.getGameManager().startGame(map, mode, red, blue);
        this.compActive = true;
        this.readySet.clear();
        return null;
    }

    public boolean isCompActive() {
        return this.compActive;
    }

    public void startReadyCheck() {
        this.readySet.clear();
        int total = 0;
        for (UUID uid : this.compTeams.keySet()) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null || !p.isOnline()) continue;
            ++total;
            p.sendTitle("\u00a7e\u6e96\u5099\u78ba\u8a8d", "\u00a77/ba comp ready \u3067\u6e96\u5099\u5b8c\u4e86", 5, 40, 5);
        }
        Bukkit.broadcastMessage((String)("\u00a7e\u2694 \u30ec\u30c7\u30a3\u30c1\u30a7\u30c3\u30af\u958b\u59cb \u00a78\u00bb \u00a7f" + total + "\u540d \u00a77/ba comp ready \u3067\u5acc\u544a"));
    }

    public boolean toggleReady(UUID uid) {
        if (!this.compTeams.containsKey(uid)) {
            return false;
        }
        if (this.readySet.contains(uid)) {
            this.readySet.remove(uid);
            return false;
        }
        this.readySet.add(uid);
        return true;
    }

    public boolean isReady(UUID uid) {
        return this.readySet.contains(uid);
    }

    public boolean isAllReady() {
        for (UUID uid : this.compTeams.keySet()) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null || !p.isOnline()) continue;
            if (!this.readySet.contains(uid)) {
                return false;
            }
        }
        return true;
    }

    public int readyCount() {
        return this.readySet.size();
    }

    public int registeredOnlineCount() {
        int total = 0;
        for (UUID uid : this.compTeams.keySet()) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p != null && p.isOnline()) {
                ++total;
            }
        }
        return total;
    }

    public void setBo(int bo) {
        this.compBo = bo;
    }

    public int getBo() {
        return this.compBo;
    }

    public int getScore(TeamColor team) {
        return this.compScore.getOrDefault(team, 0);
    }

    public void resetScore() {
        this.compScore.put(TeamColor.RED, 0);
        this.compScore.put(TeamColor.BLUE, 0);
    }

    public String scoreLine() {
        return "\u00a7c\u8d64 " + this.getScore(TeamColor.RED) + " \u00a77- \u00a79" + this.getScore(TeamColor.BLUE) + " \u00a77\u9752 \u00a78(BO" + this.compBo + ")";
    }

    public String awardWin(TeamColor winner) {
        if (winner == null) {
            return null;
        }
        this.compScore.put(winner, this.getScore(winner) + 1);
        int need = this.compBo / 2 + 1;
        if (this.getScore(winner) >= need) {
            return "\u00a76\u00a7l\u2726 \u30b7\u30ea\u30fc\u30ba\u7d42\u4e86 \u00a78\u00bb " + winner.getColorCode() + "\u00a7l" + winner.getDisplayName() + " \u00a7f\u30c1\u30fc\u30e0\u512a\u52dd\uff01 \u00a78(" + this.scoreLine() + "\u00a78)";
        }
        return "\u00a76\u00a7l\u2605 \u30bb\u30c3\u30c8\u7372\u5f97 \u00a78\u00bb " + winner.getColorCode() + winner.getDisplayName() + " \u00a78(" + this.scoreLine() + "\u00a78)";
    }

    public String onMatchEnd(TeamColor winner) {
        if (!this.compActive) {
            return null;
        }
        return this.awardWin(winner);
    }
}
