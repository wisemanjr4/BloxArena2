package com.bloxarena.game;

import org.bukkit.Material;

public enum TeamColor {
    RED("§c赤", "§c", Material.RED_CONCRETE),
    BLUE("§b青", "§b", Material.CYAN_CONCRETE);

    private final String displayName;
    private final String colorCode;
    private final Material concrete;

    TeamColor(String displayName, String colorCode, Material concrete) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.concrete = concrete;
    }

    public String getDisplayName() { return displayName; }
    public String getColorCode() { return colorCode; }
    public Material getConcrete() { return concrete; }

    public TeamColor opposite() {
        return this == RED ? BLUE : RED;
    }
}
