/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package com.bloxarena.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SelectionTool {
    public static final String WAND_NAME = "\u00a7b\u00a7lBAII WoNG \u00a7f\u30bb\u30ec\u30af\u30bf\u30fc";
    public static final Material WAND_MATERIAL = Material.GLASS;
    private final Map<UUID, Location> pos1 = new HashMap<UUID, Location>();
    private final Map<UUID, Location> pos2 = new HashMap<UUID, Location>();

    public ItemStack createWand() {
        ItemStack wand = new ItemStack(WAND_MATERIAL);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(WAND_NAME);
        meta.setLore(Arrays.asList("\u00a77\u5de6\u30af\u30ea\u30c3\u30af \u00a7f: Pos1\uff08\u6700\u5c0f\u5ea7\u6a19\uff09", "\u00a77\u53f3\u30af\u30ea\u30c3\u30af \u00a7f: Pos2\uff08\u6700\u5927\u5ea7\u6a19\uff09"));
        wand.setItemMeta(meta);
        return wand;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType() != WAND_MATERIAL) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && WAND_NAME.equals(meta.getDisplayName());
    }

    public void setPos1(Player p, Location loc) {
        this.pos1.put(p.getUniqueId(), loc.clone());
        p.sendMessage("\u00a7bPos1 \u00a7f\u3092\u8a2d\u5b9a: \u00a7e" + this.formatLoc(loc));
    }

    public void setPos2(Player p, Location loc) {
        this.pos2.put(p.getUniqueId(), loc.clone());
        p.sendMessage("\u00a7bPos2 \u00a7f\u3092\u8a2d\u5b9a: \u00a7e" + this.formatLoc(loc));
    }

    public Location getPos1(Player p) {
        return this.pos1.get(p.getUniqueId());
    }

    public Location getPos2(Player p) {
        return this.pos2.get(p.getUniqueId());
    }

    public boolean hasSelection(Player p) {
        return this.pos1.containsKey(p.getUniqueId()) && this.pos2.containsKey(p.getUniqueId());
    }

    public Location getMin(Player p) {
        Location a = this.pos1.get(p.getUniqueId());
        Location b = this.pos2.get(p.getUniqueId());
        if (a == null || b == null) {
            return null;
        }
        return new Location(a.getWorld(), (double)Math.min(a.getBlockX(), b.getBlockX()), (double)Math.min(a.getBlockY(), b.getBlockY()), (double)Math.min(a.getBlockZ(), b.getBlockZ()));
    }

    public Location getMax(Player p) {
        Location a = this.pos1.get(p.getUniqueId());
        Location b = this.pos2.get(p.getUniqueId());
        if (a == null || b == null) {
            return null;
        }
        return new Location(a.getWorld(), (double)Math.max(a.getBlockX(), b.getBlockX()), (double)Math.max(a.getBlockY(), b.getBlockY()), (double)Math.max(a.getBlockZ(), b.getBlockZ()));
    }

    private String formatLoc(Location l) {
        return String.format("(%d, %d, %d)", l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }
}

