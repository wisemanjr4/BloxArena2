package com.bloxarena.kit;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.TeamColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * キット一覧GUI（非OP対応）。
 * ロビーアイテムの右クリック or /ba kits で開く。
 */
public class KitInfoGUI {

    public static final String LIST_TITLE    = "§6§lキット一覧";
    public static final String DETAIL_PREFIX = "§6§l";

    private final BloxArenaPlugin plugin;
    private final NamespacedKey kitKey;

    public KitInfoGUI(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.kitKey = new NamespacedKey(plugin, "kit_info_kit");
    }

    // ─── ガイドアイテム（ロビー用）───

    public ItemStack makeGuideItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lキット一覧 §7(右クリック)");
            List<String> lore = new ArrayList<>();
            lore.add("§7右クリックで全キットのアイテムを確認");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "kit_guide"), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isGuideItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        return item.getItemMeta().getPersistentDataContainer()
            .has(new NamespacedKey(plugin, "kit_guide"), PersistentDataType.BYTE);
    }

    // ─── GUI表示 ───

    public void openList(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, LIST_TITLE);
        KitType[] kits = KitType.values();
        for (int i = 0; i < kits.length && i < 45; i++) {
            inv.setItem(i, makeKitIcon(kits[i]));
        }
        // 閉じるボタン
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        if (cm != null) { cm.setDisplayName("§c✖ 閉じる"); close.setItemMeta(cm); }
        inv.setItem(53, close);
        p.openInventory(inv);
    }

    public void openDetail(Player p, KitType kit) {
        Inventory inv = Bukkit.createInventory(null, 54, DETAIL_PREFIX + kit.getDisplayName());

        // キット固有アイテム（赤チーム想定）
        List<ItemStack> items = KitBuilder.getDefaultItems(kit, TeamColor.RED);
        int slot = 9; // 2行目から並べる
        for (ItemStack item : items) {
            if (slot >= 45) break;
            if (item != null && item.getType() != Material.AIR) {
                inv.setItem(slot++, item.clone());
            }
        }

        // キット説明（1行目中央）
        ItemStack desc = new ItemStack(KitEditorGUI.iconMaterial(kit));
        ItemMeta dm = desc.getItemMeta();
        if (dm != null) {
            dm.setDisplayName(kit.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7" + kit.getDescription());
            dm.setLore(lore);
            desc.setItemMeta(dm);
        }
        inv.setItem(4, desc);

        // 戻るボタン
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) { bm.setDisplayName("§7§l← キット一覧に戻る"); back.setItemMeta(bm); }
        inv.setItem(45, back);

        // 閉じるボタン
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        if (cm != null) { cm.setDisplayName("§c✖ 閉じる"); close.setItemMeta(cm); }
        inv.setItem(53, close);

        p.openInventory(inv);
    }

    // ─── クリック処理 ───

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String title = e.getView().getTitle();

        if (title.equals(LIST_TITLE)) {
            if (clicked.getType() == Material.BARRIER) { p.closeInventory(); return; }
            if (clicked.getItemMeta() == null) return;
            String kitName = clicked.getItemMeta().getPersistentDataContainer()
                .getOrDefault(kitKey, PersistentDataType.STRING, "");
            if (!kitName.isEmpty()) {
                try {
                    KitType kit = KitType.valueOf(kitName);
                    // Shift-click in test mode: equip kit
                    if (e.isShiftClick()) {
                        plugin.getTestFieldManager().giveTestKit(p, kit);
                    } else {
                        openDetail(p, kit);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        } else if (title.startsWith(DETAIL_PREFIX)) {
            if (e.getSlot() == 45) { openList(p); }
            else if (clicked.getType() == Material.BARRIER) { p.closeInventory(); }
        }
    }

    // ─── アイコン生成 ───

    private ItemStack makeKitIcon(KitType kit) {
        ItemStack item = new ItemStack(KitEditorGUI.iconMaterial(kit));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(kit.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7" + kit.getDescription());
            lore.add("");
            lore.add("§e▶ クリックでアイテム一覧");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(kitKey, PersistentDataType.STRING, kit.name());
            item.setItemMeta(meta);
        }
        return item;
    }
}
