/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 */
package com.bloxarena.command;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.map.MapConfig;
import com.bloxarena.util.SelectionTool;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetupWizard {
    private static final String BASE_MAP_ID = "arena1";
    private final BloxArenaPlugin plugin;
    private final Map<UUID, State> sessions = new HashMap<UUID, State>();
    private static final int LAST_STEP = 11;

    public SetupWizard(BloxArenaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive(Player p) {
        return this.sessions.containsKey(p.getUniqueId());
    }

    public void start(Player p) {
        if (this.isActive(p)) {
            p.sendMessage("\u00a7c\u30a6\u30a3\u30b6\u30fc\u30c9\u306f\u3059\u3067\u306b\u8d77\u52d5\u4e2d\u3067\u3059\u3002 \u00a7f/ba admin next \u00a7c \u3067\u6b21\u3078\u9032\u3081\u3066\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        this.sessions.put(p.getUniqueId(), new State());
        this.header(p);
        p.sendMessage("\u00a76\u00a7lBAII WoNG \u30de\u30c3\u30d7\u521d\u671f\u8a2d\u5b9a\u30a6\u30a3\u30b6\u30fc\u30c9");
        p.sendMessage("\u00a77\u516812\u30b9\u30c6\u30c3\u30d7\uff08\u5fc5\u98087\uff0b\u4efb\u610f5\uff09\u3067\u65b0\u898f\u30de\u30c3\u30d7\u3092\u4f5c\u6210\u3057\u307e\u3059\u3002");
        p.sendMessage("\u00a77  \u00a7f/ba admin next \u00a77= \u78ba\u5b9a\u3057\u3066\u6b21\u3078");
        p.sendMessage("\u00a77  \u00a7f/ba admin skip \u00a77= \u4efb\u610f\u30b9\u30c6\u30c3\u30d7\u306e\u307f\u30b9\u30ad\u30c3\u30d7");
        p.sendMessage("\u00a77  \u00a7f/ba admin cancel \u00a77= \u4e2d\u65ad");
        p.sendMessage("\u00a77  \u00a77\u00a7o\u30ef\u30f3\u30c9\u304c\u5fc5\u8981\u306a\u30b9\u30c6\u30c3\u30d7\u3067\u306f /ba wand \u3067\u53d6\u5f97\u3057\u3066\u304f\u3060\u3055\u3044");
        this.footer(p);
        this.showStep(p, this.sessions.get(p.getUniqueId()));
    }

    public void next(Player p) {
        State s = this.sessions.get(p.getUniqueId());
        if (s == null) {
            p.sendMessage("\u00a7c/ba admin imigration \u3067\u30a6\u30a3\u30b6\u30fc\u30c9\u3092\u958b\u59cb\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        if (this.applyStep(p, s)) {
            this.advance(p, s);
        }
    }

    public void skip(Player p) {
        State s = this.sessions.get(p.getUniqueId());
        if (s == null) {
            p.sendMessage("\u00a7c/ba admin imigration \u3067\u30a6\u30a3\u30b6\u30fc\u30c9\u3092\u958b\u59cb\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        if (s.step < 7) {
            p.sendMessage("\u00a7c\u5fc5\u9808\u30b9\u30c6\u30c3\u30d7\u306f\u30b9\u30ad\u30c3\u30d7\u3067\u304d\u307e\u305b\u3093\u3002 \u00a7f/ba admin next \u00a7c \u3067\u9032\u3093\u3067\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        p.sendMessage("\u00a77\u30b9\u30c6\u30c3\u30d7\u3092\u30b9\u30ad\u30c3\u30d7\u3057\u307e\u3057\u305f\u3002");
        this.advance(p, s);
    }

    public void cancel(Player p) {
        if (this.sessions.remove(p.getUniqueId()) != null) {
            p.sendMessage("\u00a7c\u30bb\u30c3\u30c8\u30a2\u30c3\u30d7\u3092\u30ad\u30e3\u30f3\u30bb\u30eb\u3057\u307e\u3057\u305f\u3002 \u00a7f/ba admin imigration \u00a7c \u3067\u518d\u958b\u3067\u304d\u307e\u3059\u3002");
        } else {
            p.sendMessage("\u00a7c\u8d77\u52d5\u4e2d\u306e\u30a6\u30a3\u30b6\u30fc\u30c9\u304c\u3042\u308a\u307e\u305b\u3093\u3002");
        }
    }

    private void advance(Player p, State s) {
        if (s.step == 8 && !s.redGateSet && !s.blueGateSet) {
            s.step = 9;
        }
        ++s.step;
        if (s.step > 11) {
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
                p.sendMessage("\u00a76\u00a7l\u30101/12\u3011\u00a7e \u5fc5\u9808 \u00a76- \u30ed\u30d3\u30fc\u30b9\u30dd\u30fc\u30f3\u5730\u70b9");
                p.sendMessage("\u00a77\u8a66\u5408\u5f85\u6a5f\u4e2d\u306b\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u96c6\u307e\u308b\u5834\u6240\u3067\u3059\u3002");
                p.sendMessage("\u00a77\u30ed\u30d3\u30fc\u306e\u4e2d\u5fc3\u306b\u79fb\u52d5\u3057\u3066 \u00a7f/ba admin next \u00a77\u3092\u5b9f\u884c\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                break;
            }
            case 1: {
                p.sendMessage("\u00a76\u00a7l\u30102/12\u3011\u00a7e \u5fc5\u9808 \u00a76- \u5f85\u6a5f\u30a8\u30ea\u30a2\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u8a66\u5408\u958b\u59cb\u307e\u3067\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u81ea\u7531\u306b\u52d5\u3051\u308b\u30a8\u30ea\u30a2\u3067\u3059\u3002");
                p.sendMessage("\u00a77  \u00a77\u00a7l\u5de6\u30af\u30ea\u30c3\u30af\u00a7r\u00a77=\u59cb\u70b9\u3000\u00a77\u00a7l\u53f3\u30af\u30ea\u30c3\u30af\u00a7r\u00a77=\u7d42\u70b9");
                p.sendMessage("\u00a77\u30ef\u30f3\u30c9\u304c\u306a\u3044\u5834\u5408: \u00a7f/ba wand");
                p.sendMessage("\u00a77\u7bc4\u56f2\u3092\u9078\u629e\u3057\u305f\u3089 \u00a7f/ba admin next");
                break;
            }
            case 2: {
                p.sendMessage("\u00a76\u00a7l\u30103/12\u3011\u00a7e \u5fc5\u9808 \u00a76- \u30de\u30c3\u30d7\u4f5c\u6210");
                p.sendMessage("\u00a77\u73fe\u5728\u306e\u30ef\u30fc\u30eb\u30c9 \u00a7e" + p.getWorld().getName() + " \u00a77\u4e0a\u306b\u65b0\u898f\u30de\u30c3\u30d7\u3092\u4f5c\u6210\u3057\u307e\u3059\u3002");
                p.sendMessage("\u00a77\u30de\u30c3\u30d7ID\u306f\u81ea\u52d5\u63a1\u756a\u3055\u308c\u307e\u3059\uff08arena1, arena2...\uff09\u3002");
                p.sendMessage("\u00a7f/ba admin next \u00a77\u3067\u78ba\u5b9a");
                break;
            }
            case 3: {
                p.sendMessage("\u00a76\u00a7l\u30104/12\u3011\u00a7e \u5fc5\u9808 \u00a76- \u00a7c\u8d64\u30c1\u30fc\u30e0 \u00a76\u30b9\u30dd\u30fc\u30f3\u30be\u30fc\u30f3\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u00a7c\u8d64\u30c1\u30fc\u30e0\u00a77\u306e\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u8a66\u5408\u958b\u59cb\u6642\u306b\u51fa\u73fe\u3059\u308b\u30a8\u30ea\u30a2\u3067\u3059\u3002");
                p.sendMessage("\u00a77\u30ef\u30f3\u30c9\u3067\u7bc4\u56f2\u3092\u56f2\u3093\u3067 \u00a7f/ba admin next");
                break;
            }
            case 4: {
                p.sendMessage("\u00a76\u00a7l\u30105/12\u3011\u00a7e \u5fc5\u9808 \u00a76- \u00a79\u9752\u30c1\u30fc\u30e0 \u00a76\u30b9\u30dd\u30fc\u30f3\u30be\u30fc\u30f3\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u00a79\u9752\u30c1\u30fc\u30e0\u00a77\u306e\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u8a66\u5408\u958b\u59cb\u6642\u306b\u51fa\u73fe\u3059\u308b\u30a8\u30ea\u30a2\u3067\u3059\u3002");
                p.sendMessage("\u00a77\u30ef\u30f3\u30c9\u3067\u7bc4\u56f2\u3092\u56f2\u3093\u3067 \u00a7f/ba admin next");
                break;
            }
            case 5: {
                p.sendMessage("\u00a76\u00a7l\u30106/12\u3011\u00a7e \u5fc5\u9808 \u00a76- \u4e2d\u592e\u30b3\u30f3\u30af\u30ea\u30fc\u30c8\u57fa\u6e96\u70b9");
                p.sendMessage("\u00a77\u30d0\u30c8\u30eb\u30a2\u30ea\u30fc\u30ca\u306e\u4e2d\u592e\uff08\u30b3\u30f3\u30af\u30ea\u30fc\u30c8\u3092\u6577\u304d\u8a70\u3081\u308b\u4e2d\u5fc3\u5730\u70b9\uff09\u3067\u3059\u3002");
                p.sendMessage("\u00a77\u30a2\u30ea\u30fc\u30ca\u4e2d\u592e\u306b\u7acb\u3063\u3066 \u00a7f/ba admin next \u00a77\u3067\u73fe\u5728\u5730\u3092\u8a2d\u5b9a");
                break;
            }
            case 6: {
                p.sendMessage("\u00a76\u00a7l\u30107/12\u3011\u00a7e \u5fc5\u9808 \u00a76- \u30de\u30c3\u30d7\u89b3\u6226\u30ea\u30b9\u30dd\u30fc\u30f3\u5730\u70b9");
                p.sendMessage("\u00a77\u8a66\u5408\u4e2d\u306b\u8131\u843d\u3057\u305f\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u89b3\u6226\u30e2\u30fc\u30c9\u3067\u98db\u3070\u3055\u308c\u308b\u5834\u6240\u3067\u3059\u3002");
                p.sendMessage("\u00a77\u666e\u6bb5\u306f\u30a2\u30ea\u30fc\u30ca\u4e2d\u592e\u4e0a\u7a7a\u304c\u9069\u3057\u3066\u3044\u307e\u3059\u3002");
                p.sendMessage("\u00a77\u8a2d\u5b9a\u3057\u305f\u3044\u5834\u6240\u306b\u7acb\u3063\u3066 \u00a7f/ba admin next");
                break;
            }
            case 7: {
                p.sendMessage("\u00a76\u00a7l\u30108/12\u3011\u00a77 \u4efb\u610f \u00a76- \u00a7c\u8d64\u30c1\u30fc\u30e0\u00a76 \u30b9\u30bf\u30fc\u30c8\u30b2\u30fc\u30c8\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u8a66\u5408\u958b\u59cb\u6642\u306b\u5c01\u9396\u3059\u308b\u58c1\u3084\u5e8a\u306e\u9818\u57df\u3067\u3059\u3002");
                p.sendMessage("\u00a77\u00a7c\u8d64\u30c1\u30fc\u30e0\u00a77\u5074\u306e\u5c01\u9396\u3057\u305f\u3044\u7bc4\u56f2\u3092\u30ef\u30f3\u30c9\u3067\u56f2\u3093\u3067\u304f\u3060\u3055\u3044\u3002");
                p.sendMessage("\u00a77\u9078\u629e\u5f8c: \u00a7f/ba admin next\u3000\u3000\u4e0d\u8981\u306a\u3089: \u00a7f/ba admin skip");
                break;
            }
            case 8: {
                p.sendMessage("\u00a76\u00a7l\u30109/12\u3011\u00a77 \u4efb\u610f \u00a76- \u00a79\u9752\u30c1\u30fc\u30e0\u00a76 \u30b9\u30bf\u30fc\u30c8\u30b2\u30fc\u30c8\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u00a79\u9752\u30c1\u30fc\u30e0\u00a77\u5074\u306e\u5c01\u9396\u3057\u305f\u3044\u7bc4\u56f2\u3092\u30ef\u30f3\u30c9\u3067\u56f2\u3093\u3067\u304f\u3060\u3055\u3044\u3002");
                p.sendMessage("\u00a77\u9078\u629e\u5f8c: \u00a7f/ba admin next\u3000\u3000\u4e0d\u8981\u306a\u3089: \u00a7f/ba admin skip");
                break;
            }
            case 9: {
                p.sendMessage("\u00a76\u00a7l\u301010/12\u3011\u00a77 \u4efb\u610f \u00a76- \u30b2\u30fc\u30c8\u7d20\u6750\u306e\u78ba\u8a8d");
                String currentMat = s.mapId != null && this.plugin.getMapManager().getById(s.mapId) != null ? this.plugin.getMapManager().getById(s.mapId).getGateMaterial().name() : "BARRIER";
                p.sendMessage("\u00a77\u30b9\u30bf\u30fc\u30c8\u30b2\u30fc\u30c8\u306e\u30d6\u30ed\u30c3\u30af\u7d20\u6750\uff08\u73fe\u5728: \u00a7e" + currentMat + "\u00a77\uff09");
                p.sendMessage("\u00a77\u5909\u66f4\u3057\u305f\u3044\u5834\u5408: \u00a7f/ba gatematl " + (s.mapId != null ? s.mapId : "<mapId>") + " <\u7d20\u6750\u540d>");
                p.sendMessage("\u00a77  \u00a78\u4f8b: \u00a77\u00a7o/ba gatematl " + (s.mapId != null ? s.mapId : BASE_MAP_ID) + " IRON_BARS");
                p.sendMessage("\u00a77\u3053\u306e\u307e\u307e\u3067\u3088\u3051\u308c\u3070 \u00a7f/ba admin next\u3000\u3000\u4e0d\u8981\u306a\u3089: \u00a7f/ba admin skip");
                break;
            }
            case 10: {
                p.sendMessage("\u00a76\u00a7l\u301011/12\u3011\u00a77 \u4efb\u610f \u00a76- \u5834\u5916\u5224\u5b9a\u30be\u30fc\u30f3\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u8a66\u5408\u4e2d\u306b\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u79fb\u52d5\u3067\u304d\u308b\u7bc4\u56f2\u3067\u3059\u3002\u30a8\u30ea\u30a2\u5916\u306b\u51fa\u308b\u3068\u8131\u843d\u3057\u307e\u3059\u3002");
                p.sendMessage("\u00a77\u30ef\u30f3\u30c9\u3067\u7bc4\u56f2\u3092\u56f2\u3093\u3067 \u00a7f/ba admin next\u3000\u3000\u4e0d\u8981\u306a\u3089: \u00a7f/ba admin skip");
                break;
            }
            case 11: {
                p.sendMessage("\u00a76\u00a7l\u301012/12\u3011\u00a77 \u4efb\u610f \u00a76- \u30ed\u30d3\u30fc\u5834\u5916\u5224\u5b9a\u30be\u30fc\u30f3\uff08\u7bc4\u56f2\u9078\u629e\uff09");
                p.sendMessage("\u00a77\u5f85\u6a5f\u4e2d\u306b\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u79fb\u52d5\u3067\u304d\u308b\u7bc4\u56f2\u3067\u3059\u3002\u30a8\u30ea\u30a2\u5916\u306b\u51fa\u308b\u3068\u30ed\u30d3\u30fc\u306b\u623b\u3055\u308c\u307e\u3059\u3002");
                p.sendMessage("\u00a77\u30ef\u30f3\u30c9\u3067\u7bc4\u56f2\u3092\u56f2\u3093\u3067 \u00a7f/ba admin next\u3000\u3000\u4e0d\u8981\u306a\u3089: \u00a7f/ba admin skip");
            }
        }
        if (s.step >= 7) {
            p.sendMessage("\u00a78\u30b9\u30ad\u30c3\u30d7: \u00a7f/ba admin skip");
        }
        p.sendMessage("\u00a78\u30ad\u30e3\u30f3\u30bb\u30eb: \u00a7f/ba admin cancel");
        this.footer(p);
    }

    private boolean applyStep(Player p, State s) {
        SelectionTool tool = this.plugin.getSelectionTool();
        switch (s.step) {
            case 0: {
                this.plugin.getLobbyManager().setLobbySpawn(p.getLocation());
                p.sendMessage("\u00a7a\u2714 \u30ed\u30d3\u30fc\u30b9\u30dd\u30fc\u30f3\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f: " + this.fmtLoc(p.getLocation()));
                return true;
            }
            case 1: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                this.plugin.getLobbyManager().setWaitingAreaMin(tool.getMin(p));
                this.plugin.getLobbyManager().setWaitingAreaMax(tool.getMax(p));
                p.sendMessage("\u00a7a\u2714 \u5f85\u6a5f\u30a8\u30ea\u30a2\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 2: {
                String id = BASE_MAP_ID;
                int n = 1;
                while (this.plugin.getMapManager().getById(id) != null) {
                    id = BASE_MAP_ID + n++;
                }
                s.mapId = id;
                this.plugin.getMapManager().addMap(id, p.getWorld().getName());
                p.sendMessage("\u00a7a\u2714 \u30de\u30c3\u30d7 \u00a7e" + id + " \u00a7a\u3092\u4f5c\u6210\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 3: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
                mc.setRedSpawnMin(tool.getMin(p));
                mc.setRedSpawnMax(tool.getMax(p));
                this.plugin.getMapManager().saveMap(mc);
                p.sendMessage("\u00a7a\u2714 \u00a7c\u8d64\u30c1\u30fc\u30e0 \u00a7a\u30b9\u30dd\u30fc\u30f3\u30be\u30fc\u30f3\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 4: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
                mc.setBlueSpawnMin(tool.getMin(p));
                mc.setBlueSpawnMax(tool.getMax(p));
                this.plugin.getMapManager().saveMap(mc);
                p.sendMessage("\u00a7a\u2714 \u00a79\u9752\u30c1\u30fc\u30e0 \u00a7a\u30b9\u30dd\u30fc\u30f3\u30be\u30fc\u30f3\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 5: {
                MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
                mc.setCenter(p.getLocation());
                this.plugin.getMapManager().saveMap(mc);
                p.sendMessage("\u00a7a\u2714 \u4e2d\u592e\u57fa\u6e96\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f: " + this.fmtLoc(p.getLocation()));
                return true;
            }
            case 6: {
                MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
                mc.setLobby(p.getLocation());
                this.plugin.getMapManager().saveMap(mc);
                p.sendMessage("\u00a7a\u2714 \u30de\u30c3\u30d7\u30ed\u30d3\u30fc\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f: " + this.fmtLoc(p.getLocation()));
                return true;
            }
            case 7: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3059\u308b\u304b \u00a7f/ba admin skip \u00a7c \u3067\u30b9\u30ad\u30c3\u30d7\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
                mc.setRedGateMin(tool.getMin(p));
                mc.setRedGateMax(tool.getMax(p));
                this.plugin.getMapManager().saveMap(mc);
                s.redGateSet = true;
                p.sendMessage("\u00a7a\u2714 \u00a7c\u8d64\u30c1\u30fc\u30e0 \u00a7a\u30b2\u30fc\u30c8\u9818\u57df\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 8: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3059\u308b\u304b \u00a7f/ba admin skip \u00a7c \u3067\u30b9\u30ad\u30c3\u30d7\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
                mc.setBlueGateMin(tool.getMin(p));
                mc.setBlueGateMax(tool.getMax(p));
                this.plugin.getMapManager().saveMap(mc);
                s.blueGateSet = true;
                p.sendMessage("\u00a7a\u2714 \u00a79\u9752\u30c1\u30fc\u30e0 \u00a7a\u30b2\u30fc\u30c8\u9818\u57df\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 9: {
                MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
                String matName = mc != null ? mc.getGateMaterial().name() : "BARRIER";
                p.sendMessage("\u00a7a\u2714 \u30b2\u30fc\u30c8\u30d6\u30ed\u30c3\u30af: \u00a7e" + matName + " \u00a7a\u3067\u78ba\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 10: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3059\u308b\u304b \u00a7f/ba admin skip \u00a7c \u3067\u30b9\u30ad\u30c3\u30d7\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                MapConfig mc = this.plugin.getMapManager().getById(s.mapId);
                mc.setOobMin(tool.getMin(p));
                mc.setOobMax(tool.getMax(p));
                this.plugin.getMapManager().saveMap(mc);
                p.sendMessage("\u00a7a\u2714 \u30de\u30c3\u30d7OOB\u30be\u30fc\u30f3\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
            case 11: {
                if (!tool.hasSelection(p)) {
                    p.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3059\u308b\u304b \u00a7f/ba admin skip \u00a7c \u3067\u30b9\u30ad\u30c3\u30d7\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return false;
                }
                this.plugin.getLobbyManager().setLobbyOob(tool.getMin(p), tool.getMax(p));
                p.sendMessage("\u00a7a\u2714 \u30ed\u30d3\u30fcOOB\u30be\u30fc\u30f3\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                return true;
            }
        }
        return true;
    }

    private void finish(Player p, State s) {
        MapConfig mc = s.mapId != null ? this.plugin.getMapManager().getById(s.mapId) : null;
        this.header(p);
        p.sendMessage("\u00a7a\u00a7l\u2714 \u521d\u671f\u8a2d\u5b9a\u304c\u5b8c\u4e86\u3057\u307e\u3057\u305f\uff01");
        p.sendMessage("");
        p.sendMessage("\u00a77\u30ed\u30d3\u30fc\u30b9\u30dd\u30fc\u30f3:     \u00a7a\u8a2d\u5b9a\u6e08");
        p.sendMessage("\u00a77\u5f85\u6a5f\u30a8\u30ea\u30a2:         \u00a7a\u8a2d\u5b9a\u6e08");
        if (mc != null) {
            p.sendMessage("\u00a77\u30de\u30c3\u30d7 \u00a7e" + s.mapId + "\u00a77:     " + (mc.isReady() ? "\u00a7a\u6e96\u5099\u5b8c\u4e86\uff08\u5168\u30e2\u30fc\u30c9\u5bfe\u5fdc\uff09" : "\u00a7e\u57fa\u672c\u8a2d\u5b9a\u5b8c\u4e86\uff08\u4e00\u90e8\u30e2\u30fc\u30c9\u8981\u8ffd\u52a0\u8a2d\u5b9a\uff09"));
            p.sendMessage("\u00a77\u30b2\u30fc\u30c8(\u00a7c\u8d64\u00a77):        " + (s.redGateSet ? "\u00a7a\u8a2d\u5b9a\u6e08" : "\u00a77\u30b9\u30ad\u30c3\u30d7"));
            p.sendMessage("\u00a77\u30b2\u30fc\u30c8(\u00a79\u9752\u00a77):        " + (s.blueGateSet ? "\u00a7a\u8a2d\u5b9a\u6e08" : "\u00a77\u30b9\u30ad\u30c3\u30d7"));
            p.sendMessage("\u00a77\u5834\u5916\u5224\u5b9a:           " + (mc.hasOob() ? "\u00a7a\u8a2d\u5b9a\u6e08" : "\u00a77\u30b9\u30ad\u30c3\u30d7"));
        }
        p.sendMessage("\u00a77\u30ed\u30d3\u30fc\u5834\u5916\u5224\u5b9a:     " + (this.plugin.getLobbyManager().hasLobbyOob() ? "\u00a7a\u8a2d\u5b9a\u6e08" : "\u00a77\u30b9\u30ad\u30c3\u30d7"));
        p.sendMessage("");
        p.sendMessage("\u00a76\u3010\u6b21\u306e\u30b9\u30c6\u30c3\u30d7\u3011");
        if (s.mapId != null) {
            p.sendMessage("\u00a77  \u30de\u30c3\u30d7\u78ba\u8a8d:      \u00a7f/ba info " + s.mapId);
        }
        p.sendMessage("\u00a77  CTF\u65d7\u306e\u8a2d\u7f6e:     \u00a7f/ba setredflag " + (s.mapId != null ? s.mapId : "<mapId>"));
        p.sendMessage("\u00a77  \u7206\u7834\u5730\u70b9\u306e\u8a2d\u5b9a:  \u00a7f/ba setbombplant " + (s.mapId != null ? s.mapId : "<mapId>"));
        p.sendMessage("\u00a77  \u8a66\u5408\u3092\u59cb\u3081\u308b:    \u00a7f/ba start");
        p.sendMessage("\u00a77  \u30de\u30c3\u30d7\u3092\u8ffd\u52a0:    \u00a7f/ba admin imigration");
        p.sendMessage("\u00a77  \u30b3\u30de\u30f3\u30c9\u4e00\u89a7:    \u00a7f/ba help");
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
        int step = 0;
        String mapId = null;
        boolean redGateSet = false;
        boolean blueGateSet = false;
        String pendingMaterial = null;

        private State() {
        }
    }
}

