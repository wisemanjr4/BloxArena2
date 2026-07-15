package com.bloxarena.kit;

public enum KitRole {
    DUELIST("Duelist", "§6"),
    INITIATOR("Initiator", "§a"),
    CONTROLLER("Controller", "§e"),
    SENTINEL("Sentinel", "§f");

    private final String name;
    private final String colorCode;

    KitRole(String name, String colorCode) {
        this.name = name;
        this.colorCode = colorCode;
    }

    public String getName() { return name; }
    public String getColorCode() { return colorCode; }
}
