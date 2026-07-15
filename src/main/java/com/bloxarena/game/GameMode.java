package com.bloxarena.game;

public enum GameMode {
    BATTLE_ARENA("バトルアリーナ", "殲滅または制圧", 0),
    TEAM_DEATHMATCH("チームデスマッチ", "時間内キル数勝負", 300),
    BOMB_MISSION("爆破ミッション", "爆弾設置・解除", 180),
    DOMINATION("占領戦", "拠点ポイント先取", 120),
    CAPTURE_THE_FLAG("キャプチャーザフラッグ", "旗奪取", 300);

    private final String displayName;
    private final String description;
    private final int defaultTimeLimitSeconds;

    GameMode(String displayName, String description, int defaultTimeLimitSeconds) {
        this.displayName = displayName;
        this.description = description;
        this.defaultTimeLimitSeconds = defaultTimeLimitSeconds;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getDefaultTimeLimitSeconds() { return defaultTimeLimitSeconds; }

    public static GameMode random() {
        GameMode[] modes = values();
        return modes[new java.util.Random().nextInt(modes.length)];
    }
}
