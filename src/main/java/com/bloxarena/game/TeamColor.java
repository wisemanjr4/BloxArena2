/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 */
package com.bloxarena.game;

import org.bukkit.Material;

public enum TeamColor {
    RED("\u00a7c\u8d64", "\u00a7c", Material.RED_CONCRETE),
    BLUE("\u00a7b\u9752", "\u00a7b", Material.CYAN_CONCRETE);

    private final String displayName;
    private final String colorCode;
    private final Material concrete;

    private TeamColor(String displayName, String colorCode, Material concrete) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.concrete = concrete;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getColorCode() {
        return this.colorCode;
    }

    public Material getConcrete() {
        return this.concrete;
    }

    public TeamColor opposite() {
        return this == RED ? BLUE : RED;
    }
}

