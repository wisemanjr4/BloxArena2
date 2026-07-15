package com.bloxarena.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * ガラスブロックのワンドで2点選択するシステム
 * 左クリック = Pos1、右クリック = Pos2
 */
public class SelectionTool {

    public static final String WAND_NAME = "§b§lBAII WoNG §fセレクター";
    public static final Material WAND_MATERIAL = Material.GLASS;

    // プレイヤーごとの選択座標
    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    public ItemStack createWand() {
        ItemStack wand = new ItemStack(WAND_MATERIAL);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(WAND_NAME);
        meta.setLore(Arrays.asList(
            "§7左クリック §f: Pos1（最小座標）",
            "§7右クリック §f: Pos2（最大座標）"
        ));
        wand.setItemMeta(meta);
        return wand;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType() != WAND_MATERIAL) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && WAND_NAME.equals(meta.getDisplayName());
    }

    public void setPos1(Player p, Location loc) {
        pos1.put(p.getUniqueId(), loc.clone());
        p.sendMessage("§bPos1 §fを設定: §e" + formatLoc(loc));
    }

    public void setPos2(Player p, Location loc) {
        pos2.put(p.getUniqueId(), loc.clone());
        p.sendMessage("§bPos2 §fを設定: §e" + formatLoc(loc));
    }

    public Location getPos1(Player p) { return pos1.get(p.getUniqueId()); }
    public Location getPos2(Player p) { return pos2.get(p.getUniqueId()); }

    public boolean hasSelection(Player p) {
        return pos1.containsKey(p.getUniqueId()) && pos2.containsKey(p.getUniqueId());
    }

    /** min/maxを正規化して返す */
    public Location getMin(Player p) {
        Location a = pos1.get(p.getUniqueId());
        Location b = pos2.get(p.getUniqueId());
        if (a == null || b == null) return null;
        return new Location(a.getWorld(),
            Math.min(a.getBlockX(), b.getBlockX()),
            Math.min(a.getBlockY(), b.getBlockY()),
            Math.min(a.getBlockZ(), b.getBlockZ()));
    }

    public Location getMax(Player p) {
        Location a = pos1.get(p.getUniqueId());
        Location b = pos2.get(p.getUniqueId());
        if (a == null || b == null) return null;
        return new Location(a.getWorld(),
            Math.max(a.getBlockX(), b.getBlockX()),
            Math.max(a.getBlockY(), b.getBlockY()),
            Math.max(a.getBlockZ(), b.getBlockZ()));
    }

    private String formatLoc(Location l) {
        return String.format("(%d, %d, %d)", l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }
}
