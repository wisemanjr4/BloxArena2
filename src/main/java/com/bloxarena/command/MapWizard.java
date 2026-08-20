/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 */
package com.bloxarena.command;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameMode;
import com.bloxarena.map.MapConfig;
import com.bloxarena.util.SelectionTool;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MapWizard {
    private static final int LAST_STEP = 7;
    private static final int OPTIONAL_FROM = 4;
    private final BloxArenaPlugin plugin;
    private final Map<UUID, State> sessions = new HashMap<UUID, State>();

    public MapWizard(BloxArenaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive(Player p) {
        return this.sessions.containsKey(p.getUniqueId());
    }

    public void start(Player p, String mapId) {
        if (this.isActive(p)) {
            p.sendMessage("\u00a7c\u30de\u30c3\u30d7\u30a6\u30a3\u30b6\u30fc\u30c9\u306f\u3059\u3067\u306b\u8d77\u52d5\u4e2d\u3067\u3059\u3002 \u00a7f/ba admin next \u00a7c \u3067\u6b21\u3078\u9032\u3081\u3066\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        if (this.plugin.getMapManager().getById(mapId) != null) {
            p.sendMessage("\u00a7c\u30de\u30c3\u30d7 \u00a7e" + mapId + " \u00a7c\u306f\u3059\u3067\u306b\u5b58\u5728\u3057\u307e\u3059\u3002\u5225\u306eID\u3092\u6307\u5b9a\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        this.plugin.getMapManager().addMap(mapId, p.getWorld().getName());
        State s = new State(mapId);
        this.sessions.put(p.getUniqueId(), s);
        this.header(p);
        p.sendMessage("\u00a76\u00a7l\u30de\u30c3\u30d7 \u00a7e\u00a7l" + mapId + " \u00a76\u00a7l\u3092\u4f5c\u6210\u3057\u307e\u3059");
        p.sendMessage("\u00a77\u5fc5\u98084\uff0b\u4efb\u610f4\u306e\u51688\u30b9\u30c6\u30c3\u30d7\u3067\u3059\u3002");
        p.sendMessage("\u00a77  \u00a7f/ba admin next \u00a77= \u78ba\u5b9a\u3000\u00a7f/ba admin skip \u00a77= \u4efb\u610f\u3092\u30b9\u30ad\u30c3\u30d7\u3000\u00a7f/ba admin cancel \u00a77= \u4e2d\u65ad");
        p.sendMessage("\u00a77  \u00a77\u00a7o\u30ef\u30f3\u30c9\u304c\u5fc5\u8981\u3067\u3059 \u2192 /ba wand");
        this.footer(p);
        this.showStep(p, s);
    }

    public void startUpgrade(Player p, String mapId) {
        MapConfig mc = this.plugin.getMapManager().getById(mapId);
        if (mc == null) {
            p.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + mapId + "' \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
            return;
        }
        if (mc.isReadyFor(GameMode.BOMB_MISSION) && mc.isReadyFor(GameMode.DOMINATION) && mc.isReadyFor(GameMode.CAPTURE_THE_FLAG)) {
            p.sendMessage("\u00a7a\u3053\u306e\u30de\u30c3\u30d7\u306f\u65e2\u306b\u5168\u30e2\u30fc\u30c9\u306b\u5bfe\u5fdc\u3057\u3066\u3044\u307e\u3059\u3002");
            return;
        }
        State s = new State(mapId);
        s.isUpgrade = true;
        this.sessions.put(p.getUniqueId(), s);
        this.header(p);
        p.sendMessage("\u00a76\u00a7l\u30de\u30c3\u30d7 \u00a7e" + mapId + " \u00a76\u00a7l\u306e\u30a2\u30c3\u30d7\u30b0\u30ec\u30fc\u30c9");
        p.sendMessage("\u00a77\u65b0\u30e2\u30fc\u30c9\uff08\u7206\u7834\u30fbCTF\u30fb\u5360\u9818\uff09\u306b\u5fc5\u8981\u306a\u8a2d\u5b9a\u3092\u9806\u306b\u6848\u5185\u3057\u307e\u3059\u3002");
        p.sendMessage("\u00a77  \u00a7f/ba admin next \u00a77= \u78ba\u5b9a\u3000\u00a7f/ba admin skip \u00a77= \u30b9\u30ad\u30c3\u30d7\u3000\u00a7f/ba admin cancel \u00a77= \u4e2d\u65ad");
        this.footer(p);
        this.showUpgradeStep(p, s);
    }

    public void next(Player p) {
        State s = this.sessions.get(p.getUniqueId());
        if (s == null) {
            p.sendMessage("\u00a7c/ba admin addmap <id> \u3067\u30a6\u30a3\u30b6\u30fc\u30c9\u3092\u958b\u59cb\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        if (this.applyStep(p, s)) {
            this.advance(p, s);
        }
    }

    public void skip(Player p) {
        State s = this.sessions.get(p.getUniqueId());
        if (s == null) {
            p.sendMessage("\u00a7c\u30a6\u30a3\u30b6\u30fc\u30c9\u304c\u8d77\u52d5\u3057\u3066\u3044\u307e\u305b\u3093\u3002");
            return;
        }
        if (s.isUpgrade) {
            p.sendMessage("\u00a77\u30b9\u30c6\u30c3\u30d7\u3092\u30b9\u30ad\u30c3\u30d7\u3057\u307e\u3057\u305f\u3002");
            this.advance(p, s);
            return;
        }
        if (s.step < 4) {
            p.sendMessage("\u00a7c\u5fc5\u9808\u30b9\u30c6\u30c3\u30d7\u306f\u30b9\u30ad\u30c3\u30d7\u3067\u304d\u307e\u305b\u3093\u3002 \u00a7f/ba admin next \u00a7c \u3067\u9032\u3093\u3067\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        p.sendMessage("\u00a77\u30b9\u30c6\u30c3\u30d7\u3092\u30b9\u30ad\u30c3\u30d7\u3057\u307e\u3057\u305f\u3002");
        this.advance(p, s);
    }

    public void cancel(Player p) {
        State s = this.sessions.remove(p.getUniqueId());
        if (s != null) {
            p.sendMessage("\u00a7c\u30a6\u30a3\u30b6\u30fc\u30c9\u3092\u30ad\u30e3\u30f3\u30bb\u30eb\u3057\u307e\u3057\u305f\u3002");
        } else {
            p.sendMessage("\u00a7c\u8d77\u52d5\u4e2d\u306e\u30a6\u30a3\u30b6\u30fc\u30c9\u304c\u3042\u308a\u307e\u305b\u3093\u3002");
        }
    }

    private void showUpgradeStep(Player p, State s) {
        MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
        if (mc == null) {
            this.cancel(p);
            return;
        }
        if (!mc.isReadyFor(GameMode.BOMB_MISSION)) {
            if (mc.getBombSite() == null) {
                this.header(p);
                p.sendMessage("\u00a76\u00a7l\u3010\u7206\u7834\u30e2\u30fc\u30c9\u3011\u00a7e \u5fc5\u9808 \u00a76- \u7206\u5f3e\u8a2d\u7f6e\u5730\u70b9");
                p.sendMessage("\u00a77\u653b\u6483\u5074\u304c\u7206\u5f3e\u3092\u8a2d\u7f6e\u3059\u308b\u5834\u6240\u3067\u3059\u3002\u305d\u306e\u5730\u70b9\u306b\u7acb\u3063\u3066\u304f\u3060\u3055\u3044\u3002");
                p.sendMessage("\u00a7f/ba admin next \u00a77= \u73fe\u5728\u5730\u3092\u8a2d\u5b9a\u3000\u00a7f/ba admin skip \u00a77= \u7206\u7834\u30e2\u30fc\u30c9\u3092\u98db\u3070\u3059");
                this.footer(p);
                return;
            }
            if (mc.getDefusePoint() == null) {
                this.header(p);
                p.sendMessage("\u00a76\u00a7l\u3010\u7206\u7834\u30e2\u30fc\u30c9\u3011\u00a7e \u5fc5\u9808 \u00a76- \u7206\u5f3e\u89e3\u9664\u5730\u70b9");
                p.sendMessage("\u00a77\u5b88\u5099\u5074\u304c\u7206\u5f3e\u3092\u89e3\u9664\u3059\u308b\u5834\u6240\u3067\u3059\u3002\u305d\u306e\u5730\u70b9\u306b\u7acb\u3063\u3066\u304f\u3060\u3055\u3044\u3002");
                p.sendMessage("\u00a7f/ba admin next \u00a77= \u73fe\u5728\u5730\u3092\u8a2d\u5b9a");
                this.footer(p);
                return;
            }
        }
        if (!mc.isReadyFor(GameMode.CAPTURE_THE_FLAG)) {
            if (mc.getRedFlagLocation() == null) {
                this.header(p);
                p.sendMessage("\u00a76\u00a7l\u3010CTF\u3011\u00a7e \u5fc5\u9808 \u00a76- \u00a7c\u8d64\u65d7 \u00a76\u521d\u671f\u4f4d\u7f6e\uff08\u76f8\u624b\u9663\u5730\uff09");
                p.sendMessage("\u00a77\u00a7c\u8d64\u30c1\u30fc\u30e0\u00a77\u304c\u596a\u3044\u306b\u884c\u304f\u65d7\u3067\u3059\u3002\u00a79\u9752\u9663\u5730\u5074\u00a77\u306b\u8a2d\u7f6e\u3057\u307e\u3059\u3002");
                p.sendMessage("\u00a77\u8a2d\u7f6e\u3057\u305f\u3044\u5834\u6240\u306b\u7acb\u3063\u3066 \u00a7f/ba admin next\u3000\u00a7f/ba admin skip \u00a77= CTF\u3092\u98db\u3070\u3059");
                this.footer(p);
                return;
            }
            if (mc.getBlueFlagLocation() == null) {
                this.header(p);
                p.sendMessage("\u00a76\u00a7l\u3010CTF\u3011\u00a7e \u5fc5\u9808 \u00a76- \u00a79\u9752\u65d7 \u00a76\u521d\u671f\u4f4d\u7f6e\uff08\u81ea\u9663\uff09");
                p.sendMessage("\u00a77\u81ea\u9663\u306b\u7f6e\u304f\u65d7\u3067\u3059\u3002\u00a7c\u8d64\u30c1\u30fc\u30e0\u00a77\u304c\u596a\u3044\u306b\u6765\u307e\u3059\u3002");
                p.sendMessage("\u00a77\u8a2d\u7f6e\u3057\u305f\u3044\u5834\u6240\u306b\u7acb\u3063\u3066 \u00a7f/ba admin next");
                this.footer(p);
                return;
            }
            if (mc.getRedReturnLocation() == null) {
                this.header(p);
                p.sendMessage("\u00a76\u00a7l\u3010CTF\u3011\u00a7e \u5fc5\u9808 \u00a76- \u00a7c\u8d64 \u00a76\u6301\u3061\u5e30\u308a\u5730\u70b9");
                p.sendMessage("\u00a77\u00a7c\u8d64\u30c1\u30fc\u30e0\u00a77\u304c\u76f8\u624b\u306e\u00a79\u9752\u65d7\u00a77\u3092\u6301\u3061\u5e30\u308b\u30b4\u30fc\u30eb\u5730\u70b9\u3067\u3059\u3002");
                p.sendMessage("\u00a7f/ba admin next \u00a77= \u73fe\u5728\u5730\u3092\u8a2d\u5b9a");
                this.footer(p);
                return;
            }
            if (mc.getBlueReturnLocation() == null) {
                this.header(p);
                p.sendMessage("\u00a76\u00a7l\u3010CTF\u3011\u00a7e \u5fc5\u9808 \u00a76- \u00a79\u9752 \u00a76\u6301\u3061\u5e30\u308a\u5730\u70b9");
                p.sendMessage("\u00a77\u00a79\u9752\u30c1\u30fc\u30e0\u00a77\u304c\u76f8\u624b\u306e\u00a7c\u8d64\u65d7\u00a77\u3092\u6301\u3061\u5e30\u308b\u30b4\u30fc\u30eb\u5730\u70b9\u3067\u3059\u3002");
                p.sendMessage("\u00a7f/ba admin next \u00a77= \u73fe\u5728\u5730\u3092\u8a2d\u5b9a");
                this.footer(p);
                return;
            }
        }
        if (!mc.isReadyFor(GameMode.DOMINATION)) {
            this.header(p);
            p.sendMessage("\u00a76\u00a7l\u3010\u5360\u9818\u6226\u3011\u00a77 \u4efb\u610f \u00a76- \u5360\u9818\u62e0\u70b9\u306e\u8ffd\u52a0");
            p.sendMessage("\u00a77\u5360\u9818\u62e0\u70b9\u306e\u4e2d\u5fc3\u306b\u7acb\u3063\u3066\u304f\u3060\u3055\u3044\u3002\u534a\u5f845m\u3067\u8a2d\u5b9a\u3055\u308c\u307e\u3059\u3002");
            p.sendMessage("\u00a7f/ba admin next \u00a77= \u62e0\u70b9\u3092\u8ffd\u52a0\uff08\u4f55\u500b\u3067\u3082OK\uff09\u3000\u00a7f/ba admin skip \u00a77= \u7d42\u4e86");
            this.footer(p);
            return;
        }
        this.finish(p, s);
        this.sessions.remove(p.getUniqueId());
    }

    private void advance(Player p, State s) {
        if (s.isUpgrade) {
            this.showUpgradeStep(p, s);
            return;
        }
        if (s.step == 5 && !s.redGateSet && !s.blueGateSet) {
            s.step = 6;
        }
        ++s.step;
        if (s.step > 7) {
            this.finish(p, s);
            this.sessions.remove(p.getUniqueId());
        } else {
            this.showStep(p, s);
        }
    }

    private void showStep(Player p, State s) {
        this.header(p);
        switch (s.step) {
            case 0: {
                p.sendMessage("\u00a76\u00a7l\u30101/8\u3011\u00a7e \u5fc5\u9808 \u00a76- \u00a7c\u8d64\u30c1\u30fc\u30e0 \u00a76\u30b9\u30dd\u30fc\u30f3\u30be\u30fc\u30f3\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u00a7c\u8d64\u30c1\u30fc\u30e0\u00a77\u306e\u51fa\u73fe\u30a8\u30ea\u30a2\u3092\u30ef\u30f3\u30c9\u3067\u56f2\u3093\u3067\u304f\u3060\u3055\u3044\u3002");
                p.sendMessage("\u00a77\u00a7l\u5de6\u30af\u30ea\u30c3\u30af\u00a7r\u00a77=\u59cb\u70b9\u3000\u00a77\u00a7l\u53f3\u30af\u30ea\u30c3\u30af\u00a7r\u00a77=\u7d42\u70b9\u3000\u2192 \u00a7f/ba admin next");
                break;
            }
            case 1: {
                p.sendMessage("\u00a76\u00a7l\u30102/8\u3011\u00a7e \u5fc5\u9808 \u00a76- \u00a79\u9752\u30c1\u30fc\u30e0 \u00a76\u30b9\u30dd\u30fc\u30f3\u30be\u30fc\u30f3\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u00a79\u9752\u30c1\u30fc\u30e0\u00a77\u306e\u51fa\u73fe\u30a8\u30ea\u30a2\u3092\u30ef\u30f3\u30c9\u3067\u56f2\u3093\u3067\u304f\u3060\u3055\u3044\u3002");
                p.sendMessage("\u00a77\u9078\u629e\u5f8c \u2192 \u00a7f/ba admin next");
                break;
            }
            case 2: {
                p.sendMessage("\u00a76\u00a7l\u30103/8\u3011\u00a7e \u5fc5\u9808 \u00a76- \u4e2d\u592e\u30b3\u30f3\u30af\u30ea\u30fc\u30c8\u57fa\u6e96\u70b9");
                p.sendMessage("\u00a77\u30d0\u30c8\u30eb\u30a2\u30ea\u30fc\u30ca\u4e2d\u592e\uff08\u30b3\u30f3\u30af\u30ea\u30fc\u30c8\u4e2d\u5fc3\uff09\u306b\u7acb\u3063\u3066\u304f\u3060\u3055\u3044\u3002");
                p.sendMessage("\u00a7f/ba admin next \u00a77\u3067\u73fe\u5728\u5730\u3092\u8a2d\u5b9a");
                break;
            }
            case 3: {
                p.sendMessage("\u00a76\u00a7l\u30104/8\u3011\u00a7e \u5fc5\u9808 \u00a76- \u89b3\u6226\u30ea\u30b9\u30dd\u30fc\u30f3\u5730\u70b9");
                p.sendMessage("\u00a77\u8131\u843d\u3057\u305f\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u89b3\u6226\u30e2\u30fc\u30c9\u3067\u98db\u3070\u3055\u308c\u308b\u5834\u6240\u3067\u3059\u3002");
                p.sendMessage("\u00a7f/ba admin next \u00a77\u3067\u73fe\u5728\u5730\u3092\u8a2d\u5b9a");
                break;
            }
            case 4: {
                p.sendMessage("\u00a76\u00a7l\u30105/8\u3011\u00a77 \u4efb\u610f \u00a76- \u00a7c\u8d64\u30c1\u30fc\u30e0\u00a76 \u30b9\u30bf\u30fc\u30c8\u30b2\u30fc\u30c8\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u8a66\u5408\u958b\u59cb\u6642\u306b\u5c01\u9396\u3059\u308b\u00a7c\u8d64\u00a77\u5074\u306e\u9818\u57df\u3092\u30ef\u30f3\u30c9\u3067\u56f2\u3093\u3067\u304f\u3060\u3055\u3044\u3002");
                p.sendMessage("\u00a77\u9078\u629e\u5f8c: \u00a7f/ba admin next\u3000\u3000\u4e0d\u8981\u306a\u3089: \u00a7f/ba admin skip");
                break;
            }
            case 5: {
                p.sendMessage("\u00a76\u00a7l\u30106/8\u3011\u00a77 \u4efb\u610f \u00a76- \u00a79\u9752\u30c1\u30fc\u30e0\u00a76 \u30b9\u30bf\u30fc\u30c8\u30b2\u30fc\u30c8\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u00a79\u9752\u00a77\u5074\u306e\u5c01\u9396\u9818\u57df\u3092\u30ef\u30f3\u30c9\u3067\u56f2\u3093\u3067\u304f\u3060\u3055\u3044\u3002");
                p.sendMessage("\u00a77\u9078\u629e\u5f8c: \u00a7f/ba admin next\u3000\u3000\u4e0d\u8981\u306a\u3089: \u00a7f/ba admin skip");
                break;
            }
            case 6: {
                MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
                String mat = mc != null ? mc.getGateMaterial().name() : "BARRIER";
                p.sendMessage("\u00a76\u00a7l\u30107/8\u3011\u00a77 \u4efb\u610f \u00a76- \u30b2\u30fc\u30c8\u7d20\u6750\u306e\u78ba\u8a8d");
                p.sendMessage("\u00a77\u30b9\u30bf\u30fc\u30c8\u30b2\u30fc\u30c8\u306e\u30d6\u30ed\u30c3\u30af\u7d20\u6750\uff08\u73fe\u5728: \u00a7e" + mat + "\u00a77\uff09");
                p.sendMessage("\u00a77\u5909\u66f4: \u00a7f/ba gatematl " + s.mapId + " <\u7d20\u6750\u540d>");
                p.sendMessage("\u00a77  \u00a78\u4f8b: \u00a77\u00a7o/ba gatematl " + s.mapId + " IRON_BARS");
                p.sendMessage("\u00a77\u78ba\u5b9a: \u00a7f/ba admin next\u3000\u3000\u4e0d\u8981\u306a\u3089: \u00a7f/ba admin skip");
                break;
            }
            case 7: {
                p.sendMessage("\u00a76\u00a7l\u30108/8\u3011\u00a77 \u4efb\u610f \u00a76- \u5834\u5916\u5224\u5b9a\u30be\u30fc\u30f3\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u8a66\u5408\u4e2d\u306e\u79fb\u52d5\u8a31\u53ef\u7bc4\u56f2\u3067\u3059\u3002\u30a8\u30ea\u30a2\u5916\u306b\u51fa\u308b\u3068\u8131\u843d\u3057\u307e\u3059\u3002");
                p.sendMessage("\u00a77\u9078\u629e\u5f8c: \u00a7f/ba admin next\u3000\u3000\u4e0d\u8981\u306a\u3089: \u00a7f/ba admin skip");
            }
        }
        if (s.step >= 4) {
            p.sendMessage("\u00a78\u30b9\u30ad\u30c3\u30d7: \u00a7f/ba admin skip");
        }
        p.sendMessage("\u00a78\u30ad\u30e3\u30f3\u30bb\u30eb: \u00a7f/ba admin cancel");
        this.footer(p);
    }

    private boolean applyStep(Player p, State s) {
        SelectionTool tool = this.plugin.getSelectionTool();
        MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
        if (mc == null) {
            return true;
        }
        if (s.isUpgrade) {
            if (!mc.isReadyFor(GameMode.BOMB_MISSION)) {
                if (mc.getBombSite() == null) {
                    mc.setBombSite(p.getLocation());
                    this.plugin.getMapManager().saveMap(mc);
                    p.sendMessage("\u00a7a\u2714 \u7206\u5f3e\u8a2d\u7f6e\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f");
                    return true;
                }
                if (mc.getDefusePoint() == null) {
                    mc.setDefusePoint(p.getLocation());
                    this.plugin.getMapManager().saveMap(mc);
                    p.sendMessage("\u00a7a\u2714 \u7206\u5f3e\u89e3\u9664\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f");
                    return true;
                }
            }
            if (!mc.isReadyFor(GameMode.CAPTURE_THE_FLAG)) {
                if (mc.getRedFlagLocation() == null) {
                    mc.setRedFlagLocation(p.getLocation());
                    this.plugin.getMapManager().saveMap(mc);
                    p.sendMessage("\u00a7a\u2714 \u00a7c\u8d64\u65d7\u00a7a\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\uff08\u00a7c\u81ea\u9663\u00a7a\u306b\u7f6e\u3044\u3066\u304f\u3060\u3055\u3044\uff09");
                    return true;
                }
                if (mc.getBlueFlagLocation() == null) {
                    mc.setBlueFlagLocation(p.getLocation());
                    this.plugin.getMapManager().saveMap(mc);
                    p.sendMessage("\u00a7a\u2714 \u00a79\u9752\u65d7\u00a7a\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\uff08\u00a79\u81ea\u9663\u00a7a\u306b\u7f6e\u3044\u3066\u304f\u3060\u3055\u3044\uff09");
                    return true;
                }
                if (mc.getRedReturnLocation() == null) {
                    mc.setRedReturnLocation(p.getLocation());
                    this.plugin.getMapManager().saveMap(mc);
                    p.sendMessage("\u00a7a\u2714 \u00a7c\u8d64\u00a7a\u6301\u3061\u5e30\u308a\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f");
                    return true;
                }
                if (mc.getBlueReturnLocation() == null) {
                    mc.setBlueReturnLocation(p.getLocation());
                    this.plugin.getMapManager().saveMap(mc);
                    p.sendMessage("\u00a7a\u2714 \u00a79\u9752\u00a7a\u6301\u3061\u5e30\u308a\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f");
                    return true;
                }
            }
            if (!mc.isReadyFor(GameMode.DOMINATION)) {
                mc.addDomPoint(new MapConfig.DomPoint(p.getLocation(), 5.0));
                this.plugin.getMapManager().saveMap(mc);
                p.sendMessage("\u00a7a\u2714 \u5360\u9818\u62e0\u70b9\u3092\u8ffd\u52a0\u3057\u307e\u3057\u305f\uff08\u534a\u5f845m\uff09\u3002\u7d9a\u3051\u3066\u8ffd\u52a0\u3067\u304d\u307e\u3059\u3002");
            }
            return true;
        }
        switch (s.step) {
            case 0: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                mc.setRedSpawnMin(tool.getMin(p));
                mc.setRedSpawnMax(tool.getMax(p));
                this.plugin.getMapManager().saveMap(mc);
                p.sendMessage("\u00a7a\u2714 \u00a7c\u8d64\u30c1\u30fc\u30e0 \u00a7a\u30b9\u30dd\u30fc\u30f3\u30be\u30fc\u30f3\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 1: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                mc.setBlueSpawnMin(tool.getMin(p));
                mc.setBlueSpawnMax(tool.getMax(p));
                this.plugin.getMapManager().saveMap(mc);
                p.sendMessage("\u00a7a\u2714 \u00a79\u9752\u30c1\u30fc\u30e0 \u00a7a\u30b9\u30dd\u30fc\u30f3\u30be\u30fc\u30f3\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 2: {
                mc.setCenter(p.getLocation());
                this.plugin.getMapManager().saveMap(mc);
                p.sendMessage("\u00a7a\u2714 \u4e2d\u592e\u57fa\u6e96\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f: " + this.fmtLoc(p.getLocation()));
                return true;
            }
            case 3: {
                mc.setLobby(p.getLocation());
                this.plugin.getMapManager().saveMap(mc);
                p.sendMessage("\u00a7a\u2714 \u30de\u30c3\u30d7\u30ed\u30d3\u30fc\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f: " + this.fmtLoc(p.getLocation()));
                return true;
            }
            case 4: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3059\u308b\u304b \u00a7f/ba admin skip \u00a7c \u3067\u30b9\u30ad\u30c3\u30d7\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                mc.setRedGateMin(tool.getMin(p));
                mc.setRedGateMax(tool.getMax(p));
                this.plugin.getMapManager().saveMap(mc);
                s.redGateSet = true;
                p.sendMessage("\u00a7a\u2714 \u00a7c\u8d64\u30c1\u30fc\u30e0 \u00a7a\u30b2\u30fc\u30c8\u9818\u57df\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 5: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3059\u308b\u304b \u00a7f/ba admin skip \u00a7c \u3067\u30b9\u30ad\u30c3\u30d7\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                mc.setBlueGateMin(tool.getMin(p));
                mc.setBlueGateMax(tool.getMax(p));
                this.plugin.getMapManager().saveMap(mc);
                s.blueGateSet = true;
                p.sendMessage("\u00a7a\u2714 \u00a79\u9752\u30c1\u30fc\u30e0 \u00a7a\u30b2\u30fc\u30c8\u9818\u57df\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 6: {
                String matName = mc != null ? mc.getGateMaterial().name() : "BARRIER";
                p.sendMessage("\u00a7a\u2714 \u30b2\u30fc\u30c8\u30d6\u30ed\u30c3\u30af: \u00a7e" + matName + " \u00a7a\u3067\u78ba\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 7: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3059\u308b\u304b \u00a7f/ba admin skip \u00a7c \u3067\u30b9\u30ad\u30c3\u30d7\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                mc.setOobMin(tool.getMin(p));
                mc.setOobMax(tool.getMax(p));
                this.plugin.getMapManager().saveMap(mc);
                p.sendMessage("\u00a7a\u2714 \u30de\u30c3\u30d7OOB\u30be\u30fc\u30f3\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
        }
        return true;
    }

    private void finish(Player p, State s) {
        MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
        this.header(p);
        p.sendMessage("\u00a7a\u00a7l\u2714 \u30de\u30c3\u30d7 \u00a7e\u00a7l" + s.mapId + " \u00a7a\u00a7l\u306e\u8a2d\u5b9a\u304c\u5b8c\u4e86\u3057\u307e\u3057\u305f");
        p.sendMessage("");
        if (mc != null) {
            p.sendMessage("\u00a77\u72b6\u614b:        " + (mc.isReady() ? "\u00a7a\u5168\u30e2\u30fc\u30c9\u5bfe\u5fdc\u5b8c\u4e86" : "\u00a7e\u57fa\u672c\u8a2d\u5b9a\u5b8c\u4e86\uff08\u4e0d\u8db3\u30e2\u30fc\u30c9\u3042\u308a\uff09"));
            p.sendMessage("\u00a77\u30b2\u30fc\u30c8(\u00a7c\u8d64\u00a77):   " + (s.redGateSet ? "\u00a7a\u8a2d\u5b9a\u6e08" : "\u00a77\u306a\u3057"));
            p.sendMessage("\u00a77\u30b2\u30fc\u30c8(\u00a79\u9752\u00a77):   " + (s.blueGateSet ? "\u00a7a\u8a2d\u5b9a\u6e08" : "\u00a77\u306a\u3057"));
            p.sendMessage("\u00a77\u5834\u5916\u5224\u5b9a:    " + (mc.hasOob() ? "\u00a7a\u8a2d\u5b9a\u6e08" : "\u00a77\u306a\u3057"));
        }
        p.sendMessage("");
        p.sendMessage("\u00a76\u3010\u6b21\u306e\u30b9\u30c6\u30c3\u30d7\u3011");
        p.sendMessage("\u00a77  \u30de\u30c3\u30d7\u78ba\u8a8d:     \u00a7f/ba info " + s.mapId);
        p.sendMessage("\u00a77  \u4e0d\u8db3\u3092\u8ffd\u52a0:     \u00a7f/ba upgrade " + s.mapId);
        p.sendMessage("\u00a77  CTF\u65d7\u306e\u8a2d\u5b9a:   \u00a7f/ba setredflag " + s.mapId);
        p.sendMessage("\u00a77  \u7206\u7834\u5730\u70b9\u306e\u8a2d\u5b9a: \u00a7f/ba setbombplant " + s.mapId);
        p.sendMessage("\u00a77  \u5225\u306e\u30de\u30c3\u30d7\u8ffd\u52a0: \u00a7f/ba admin addmap <id>");
        this.footer(p);
    }

    private void header(Player p) {
        p.sendMessage("\u00a78\u00a7m\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
    }

    private void footer(Player p) {
        p.sendMessage("\u00a78\u00a7m\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
    }

    private String fmtLoc(Location l) {
        return String.format("\u00a7f(%d,%d,%d)\u00a77[\u00a7f%s\u00a77]", l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld() != null ? l.getWorld().getName() : "?");
    }

    private static class State {
        String mapId;
        int step = 0;
        boolean redGateSet = false;
        boolean blueGateSet = false;
        boolean isUpgrade = false;

        State(String mapId) {
            this.mapId = mapId;
        }
    }
}

