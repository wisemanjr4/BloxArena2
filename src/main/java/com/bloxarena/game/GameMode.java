/*
 * Decompiled with CFR 0.152.
 */
package com.bloxarena.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum GameMode {
    BATTLE_ARENA("\u30d0\u30c8\u30eb\u30a2\u30ea\u30fc\u30ca", "\u6bb2\u6ec5\u307e\u305f\u306f\u5236\u5727", 0),
    TEAM_DEATHMATCH("\u30c1\u30fc\u30e0\u30c7\u30b9\u30de\u30c3\u30c1", "\u6642\u9593\u5185\u30ad\u30eb\u6570\u52dd\u8ca0", 300),
    BOMB_MISSION("\u7206\u7834\u30df\u30c3\u30b7\u30e7\u30f3", "\u7206\u5f3e\u8a2d\u7f6e\u30fb\u89e3\u9664", 180),
    DOMINATION("\u5360\u9818\u6226", "\u62e0\u70b9\u30dd\u30a4\u30f3\u30c8\u5148\u53d6", 120),
    CAPTURE_THE_FLAG("\u30ad\u30e3\u30d7\u30c1\u30e3\u30fc\u30b6\u30d5\u30e9\u30c3\u30b0", "\u65d7\u596a\u53d6", 300),
    FFA("FFA", "\u5168\u54e1\u304c\u6575\uff01\u6700\u5f8c\u307e\u3067\u751f\u304d\u6b8b\u308c", 300);

    private final String displayName;
    private final String description;
    private final int defaultTimeLimitSeconds;

    private GameMode(String displayName, String description, int defaultTimeLimitSeconds) {
        this.displayName = displayName;
        this.description = description;
        this.defaultTimeLimitSeconds = defaultTimeLimitSeconds;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public int getDefaultTimeLimitSeconds() {
        return this.defaultTimeLimitSeconds;
    }

    public static GameMode random() {
        GameMode[] modes = GameMode.values();
        return modes[new Random().nextInt(modes.length)];
    }

    public static GameMode random(int playerCount) {
        List<GameMode> list = new ArrayList<GameMode>(List.of(GameMode.values()));
        if (playerCount % 2 != 0 || playerCount <= 3) {
            list.remove((Object)DOMINATION);
            list.remove((Object)CAPTURE_THE_FLAG);
        }
        if (list.isEmpty()) {
            list = Arrays.asList(GameMode.values());
        }
        return (GameMode)((Object)list.get(new Random().nextInt(list.size())));
    }
}

