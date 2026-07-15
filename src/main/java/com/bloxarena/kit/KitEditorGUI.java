package com.bloxarena.kit;

import com.bloxarena.BloxArenaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 運営専用キットエディタ GUI。
 *
 * 画面1 (リスト):  全8キットをアイコンで表示
 * 画面2 (エディタ): 36スロットに自由にアイテムを並べて保存
 *
 * 保存先: config.yml の kit_editor.<KIT名>.items.<slot> (Bukkit ItemStack シリアライズ)
 * KitBuilder はゲーム開始時にここから読み込む。
 */
public class KitEditorGUI {

    // GUI タイトル定数 (クリックイベントの判定に使用)
    public static final String LIST_TITLE  = "§6§lKit Editor §7- キット一覧";
    public static final String EDIT_PREFIX = "§6§lEdit: §e";

    /** 各プレイヤーが現在編集中のキット */
    private final Map<UUID, KitType> editing = new HashMap<>();

    private final BloxArenaPlugin plugin;
    private final org.bukkit.NamespacedKey kitKey;

    public KitEditorGUI(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.kitKey = new org.bukkit.NamespacedKey(plugin, "kit_editor_kit");
    }

    // ─── 開く ───

    /** キット一覧画面を開く */
    public void openList(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, LIST_TITLE);
        fill(inv, pane(Material.BLACK_STAINED_GLASS_PANE));

        // 15キット用スロット（3行 × 5列、中央寄せ）
        KitType[] kits = KitType.values();
        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,   // 行2: 7キット
            19, 20, 21, 22, 23, 24, 25,   // 行3: 7キット
            28, 29, 30, 31, 32            // 行4: 5キット
        };
        for (int i = 0; i < kits.length && i < slots.length; i++) {
            inv.setItem(slots[i], kitIcon(kits[i]));
        }
        inv.setItem(53, btn(Material.BARRIER, "§c閉じる", ""));
        p.openInventory(inv);
    }

    /** キットエディタ画面を開く */
    public void openEdit(Player p, KitType kit) {
        editing.put(p.getUniqueId(), kit);
        Inventory inv = Bukkit.createInventory(null, 54, EDIT_PREFIX + kit.getDisplayName());

        // スロット 0-35: カスタムアイテムがあればそれを、なければデフォルトを初期表示
        List<ItemStack> items = hasCustomItems(kit)
            ? loadItems(kit)
            : KitBuilder.getDefaultItems(kit, com.bloxarena.game.TeamColor.RED);
        for (int i = 0; i < Math.min(items.size(), 36); i++) {
            inv.setItem(i, items.get(i));
        }

        // スロット 36-44: 下部コントロール行 (gray pane で埋める)
        for (int i = 36; i < 54; i++) inv.setItem(i, pane(Material.GRAY_STAINED_GLASS_PANE));

        // 防具スロット表示（スロット37=helmet, 38=chest, 39=legs, 40=boots, 41=offhand）
        List<ItemStack> armor = loadArmor(kit);
        inv.setItem(37, armor.size() > 0 && armor.get(0) != null ? armor.get(0) : pane(Material.IRON_HELMET));
        inv.setItem(38, armor.size() > 1 && armor.get(1) != null ? armor.get(1) : pane(Material.IRON_CHESTPLATE));
        inv.setItem(39, armor.size() > 2 && armor.get(2) != null ? armor.get(2) : pane(Material.IRON_LEGGINGS));
        inv.setItem(40, armor.size() > 3 && armor.get(3) != null ? armor.get(3) : pane(Material.IRON_BOOTS));
        inv.setItem(41, armor.size() > 4 && armor.get(4) != null ? armor.get(4) : pane(Material.AIR));
        setCustomName(inv.getItem(37), "§7Helmet (クリックして変更)");
        setCustomName(inv.getItem(38), "§7Chestplate (クリックして変更)");
        setCustomName(inv.getItem(39), "§7Leggings (クリックして変更)");
        setCustomName(inv.getItem(40), "§7Boots (クリックして変更)");

        // コントロールボタン
        inv.setItem(36, infoSign(kit));
        inv.setItem(45, btn(Material.LIME_WOOL,   "§a§l保存",     "§7現在のアイテム配置を保存します"));
        inv.setItem(47, btn(Material.YELLOW_WOOL,  "§e§lデフォルトに戻す", "§7カスタマイズを削除してデフォルトに戻します"));
        inv.setItem(49, btn(Material.CYAN_WOOL,    "§b§l一覧に戻る", "§7保存せず一覧に戻ります"));
        inv.setItem(53, btn(Material.RED_WOOL,     "§c§l閉じる",   "§7GUIを閉じます"));

        p.openInventory(inv);
    }

    // ─── クリックハンドラ ───

    /**
     * InventoryClickEvent をここに渡す。
     * @return true = イベントをキャンセルした (GUIが処理した)
     */
    public boolean handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return false;
        String title = e.getView().getTitle();

        // ─ 一覧画面 ─
        if (LIST_TITLE.equals(title)) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return true;
            if (clicked.getType() == Material.BARRIER) { p.closeInventory(); return true; }
            if (clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return true;

            // キットアイコンをクリック → PDCでキット名を取得してエディタへ
            if (clicked.getItemMeta() != null) {
                String kitName = clicked.getItemMeta().getPersistentDataContainer()
                    .getOrDefault(kitKey, org.bukkit.persistence.PersistentDataType.STRING, "");
                if (!kitName.isEmpty()) {
                    try {
                        openEdit(p, KitType.valueOf(kitName));
                    } catch (IllegalArgumentException ignored) {}
                    return true;
                }
            }
            return true;
        }

        // ─ エディタ画面 ─
        if (title.startsWith(EDIT_PREFIX)) {
            int slot = e.getRawSlot();

            // スロット 36-53 はコントロール行 → アイテム配置不可
            if (slot >= 36 && slot < 54) {
                e.setCancelled(true);
                handleControlClick(p, slot, e.getView().getTitle());
                return true;
            }
            // スロット 54以上 = プレイヤーインベントリ → 自由に操作させる
            // スロット 0-35 = アイテム編集エリア → キャンセルしない (自由配置)
            return false;
        }

        return false;
    }

    private void handleControlClick(Player p, int slot, String title) {
        KitType kit = editing.get(p.getUniqueId());
        if (kit == null) return;

        switch (slot) {
            case 45 -> { // 保存
                Inventory inv = p.getOpenInventory().getTopInventory();
                saveItems(kit, inv);
                p.sendMessage("§a§lKit Editor: §e" + kit.getDisplayName() + " §aを保存しました。");
                // 画面を更新して保存確認
                p.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> openEdit(p, kit), 1L);
            }
            case 47 -> { // デフォルトに戻す
                deleteItems(kit);
                p.sendMessage("§e§lKit Editor: §e" + kit.getDisplayName() + " §eをデフォルトに戻しました。");
                p.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> openEdit(p, kit), 1L);
            }
            case 49 -> { // 一覧に戻る
                editing.remove(p.getUniqueId());
                openList(p);
            }
            case 53 -> { // 閉じる
                editing.remove(p.getUniqueId());
                p.closeInventory();
            }
        }
    }

    // ─── 保存 / 読み込み ───

    /** アイテム(0-35)と防具(37-41)を config.yml に保存 */
    public void saveItems(KitType kit, Inventory inv) {
        FileConfiguration cfg = plugin.getConfig();
        String base = "kit_editor." + kit.name() + ".items.";

        // いったん削除してから書き直し
        cfg.set("kit_editor." + kit.name(), null);

        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                cfg.set(base + i, item);
            }
        }
        // 防具スロット保存 (37=helmet,38=chest,39=legs,40=boots,41=offhand)
        String armorBase = "kit_editor." + kit.name() + ".armor.";
        cfg.set("kit_editor." + kit.name() + ".armor", null);
        int[] armorSlots = {37, 38, 39, 40, 41};
        for (int i = 0; i < armorSlots.length; i++) {
            ItemStack item = inv.getItem(armorSlots[i]);
            if (item != null && item.getType() != Material.AIR
                    && !isControlItem(item)) {
                cfg.set(armorBase + i, item);
            }
        }
        plugin.saveConfig();
    }

    private boolean isControlItem(ItemStack item) {
        // ガラスパネルやウールボタンはコントロールアイテム
        return item.getType().name().contains("STAINED_GLASS_PANE")
            || item.getType().name().endsWith("_WOOL");
    }

    /** カスタムアイテムを削除 (デフォルト復帰) */
    public void deleteItems(KitType kit) {
        plugin.getConfig().set("kit_editor." + kit.name(), null);
        plugin.saveConfig();
    }

    /**
     * config.yml からカスタムアイテムを読み込む。
     * カスタマイズがなければ空リストを返す。
     */
    public List<ItemStack> loadItems(KitType kit) {
        FileConfiguration cfg = plugin.getConfig();
        String base = "kit_editor." + kit.name() + ".items";
        if (!cfg.isConfigurationSection(base)) return new ArrayList<>();

        List<ItemStack> items = new ArrayList<>(Collections.nCopies(36, null));
        for (String key : cfg.getConfigurationSection(base).getKeys(false)) {
            try {
                int slot = Integer.parseInt(key);
                ItemStack item = cfg.getItemStack(base + "." + key);
                if (slot >= 0 && slot < 36) items.set(slot, item);
            } catch (NumberFormatException ignored) {}
        }
        return items;
    }

    /** 防具スロット(helmet,chest,legs,boots,offhand)を読み込む */
    public List<ItemStack> loadArmor(KitType kit) {
        FileConfiguration cfg = plugin.getConfig();
        String base = "kit_editor." + kit.name() + ".armor";
        List<ItemStack> armor = new ArrayList<>(Collections.nCopies(5, null));
        if (!cfg.isConfigurationSection(base)) return armor;
        for (String key : cfg.getConfigurationSection(base).getKeys(false)) {
            try {
                int idx = Integer.parseInt(key);
                ItemStack item = cfg.getItemStack(base + "." + key);
                if (idx >= 0 && idx < 5) armor.set(idx, item);
            } catch (NumberFormatException ignored) {}
        }
        return armor;
    }

    /** カスタムアイテムが設定されているか */
    public boolean hasCustomItems(KitType kit) {
        return plugin.getConfig().isConfigurationSection("kit_editor." + kit.name() + ".items");
    }

    // ─── UI ヘルパー ───

    private void fill(Inventory inv, ItemStack item) {
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, item);
    }

    private void setCustomName(ItemStack item, String name) {
        if (item == null) return;
        ItemMeta m = item.getItemMeta();
        if (m != null) { m.setDisplayName(name); item.setItemMeta(m); }
    }

    private ItemStack pane(Material mat) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        if (m != null) { m.setDisplayName(" "); i.setItemMeta(m); }
        return i;
    }

    private ItemStack btn(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName(name);
            if (!lore.isEmpty()) m.setLore(List.of(lore));
            i.setItemMeta(m);
        }
        return i;
    }

    private ItemStack kitIcon(KitType kit) {
        ItemStack i = new ItemStack(kitMaterial(kit));
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName("§e§l" + kit.getDisplayName());
            m.setLore(List.of(
                "§7" + kit.getDescription(),
                "",
                hasCustomItems(kit) ? "§a✔ カスタム設定あり" : "§7デフォルト設定",
                "§eクリックして編集"
            ));
            // キット名をPDCに保存（材質重複によるミスクリック防止）
            m.getPersistentDataContainer().set(
                kitKey,
                org.bukkit.persistence.PersistentDataType.STRING,
                kit.name()
            );
            i.setItemMeta(m);
        }
        return i;
    }

    private ItemStack infoSign(KitType kit) {
        ItemStack i = new ItemStack(Material.OAK_SIGN);
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName("§6§l" + kit.getDisplayName());
            m.setLore(List.of(
                "§7" + kit.getDescription(),
                "",
                "§7スロット 0-35 に",
                "§7アイテムを自由に配置し、",
                "§a保存ボタン§7で確定。",
                "",
                hasCustomItems(kit) ? "§a✔ カスタム設定済み" : "§7未カスタマイズ（デフォルト）"
            ));
            i.setItemMeta(m);
        }
        return i;
    }

    public static Material iconMaterial(KitType kit) { return kitMaterial(kit); }
    private static Material kitMaterial(KitType kit) {
        return switch (kit) {
            case BLADE    -> Material.IRON_SWORD;
            case BREAKER  -> Material.IRON_AXE;
            case SCOUT    -> Material.FEATHER;
            case FLASHER  -> Material.GLOWSTONE_DUST;
            case ROCKETER -> Material.TNT;
            case ALCHEMIST -> Material.GLASS_BOTTLE;
            case TRAPPER  -> Material.STRING;
            case GUARDIAN  -> Material.SHIELD;
            case NINJA     -> Material.ENDER_PEARL;
            case BERSERKER -> Material.DIAMOND_SWORD;
            case MEDIC     -> Material.SPLASH_POTION;
            case ENGINEER  -> Material.IRON_PICKAXE;
            case SNIPER    -> Material.CROSSBOW;
            case COUNTER   -> Material.IRON_SWORD;
            case MARKSMAN  -> Material.BOW;
            case PYRO      -> Material.BLAZE_POWDER;
            case SUPPORTER -> Material.NETHER_WART;
            case JESTER    -> Material.RABBIT_FOOT;
            case SUNDANCE  -> Material.LIGHTNING_ROD;
            case VAMPIRE   -> Material.REDSTONE;
            case BOMBER    -> Material.TNT;
            case COOK      -> Material.COOKED_BEEF;
            case WHIRLWIND -> Material.FEATHER;
            case SWAPPER   -> Material.ENDER_PEARL;
            case STICKER   -> Material.FISHING_ROD;
            case DECOY     -> Material.SKELETON_SKULL;
            case RESTRICTIONER -> Material.CHAIN;
            case TRANSPORTER -> Material.ENDER_EYE;
            case MIMIC     -> Material.ECHO_SHARD;
            case PHANTOM   -> Material.PHANTOM_MEMBRANE;
            case ANCHOR    -> Material.LODESTONE;
            case RELEASER  -> Material.FIRE_CHARGE;
            case GRANG     -> Material.SHIELD;
            case NECRO     -> Material.SKELETON_SKULL;
        };
    }
}
