/*
 * Decompiled with CFR 0.152.
 */
package com.bloxarena.kit;

public enum KitRole {
    DUELIST("Duelist", "\u00a76"),
    INITIATOR("Initiator", "\u00a7a"),
    CONTROLLER("Controller", "\u00a7e"),
    SENTINEL("Sentinel", "\u00a7f");

    private final String name;
    private final String colorCode;

    private KitRole(String name, String colorCode) {
        this.name = name;
        this.colorCode = colorCode;
    }

    public String getName() {
        return this.name;
    }

    public String getColorCode() {
        return this.colorCode;
    }
}

