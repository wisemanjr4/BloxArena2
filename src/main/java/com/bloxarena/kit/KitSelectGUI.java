package com.bloxarena.kit;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameManager;
import com.bloxarena.game.TeamColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * ホットバー型キット選択（統合版互換）
 * スロット0=前ページ、1〜7=キット、8=次ページ
 * 右クリックで選択・ページ切り替え
 */
public class KitSelectGUI {

    // 後方互換のため残す（GameListeners の instanceof チェック等で使用）
    public static final String GUI_TITLE = "__hotbar__";

    private static final int KITS_PER_PAGE = 7;

    private final BloxArenaPlugin plugin;
    private final GameManager gm;

    private final NamespacedKey KIT_KEY;
    private final NamespacedKey NAV_KEY;

    private final Set<UUID> confirmed  = new HashSet<>();
    private final Set<UUID> allPlayers = new HashSet<>();
    private final Map<UUID, Integer> pages = new HashMap<>();

    private BukkitTask timeoutTask;
    private BukkitTask watchdogTask;
    private boolean done = false;

    public KitSelectGUI(BloxArenaPlugin plugin, GameManager gm) {
        this.plugin  = plugin;
        this.gm      = gm;
        this.KIT_KEY = new NamespacedKey(plugin, "ks_kit");
        this.NAV_KEY = new NamespacedKey(plugin, "ks_nav");
    }

    // ─── 公開API ───

    public void openForAll(List<UUID> redTeam, List<UUID> blueTeam, int timeoutSeconds) {
        allPlayers.addAll(redTeam);
        allPlayers.addAll(blueTeam);

        for (UUID uid : allPlayers) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) renderHotbar(p);
        }

        // ウォッチドッグ: 毎秒ホットバーが壊れていないか確認・再描画
        watchdogTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (done) { watchdogTask.cancel(); return; }
            for (UUID uid : allPlayers) {
                if (confirmed.contains(uid)) continue;
                Player p = Bukkit.getPlayer(uid);
                if (p != null && p.isOnline() && !hasHotbarRendered(p)) renderHotbar(p);
            }
        }, 20L, 20L);

        // タイムアウト
        timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (UUID uid : allPlayers) {
                if (!confirmed.contains(uid)) {
                    Player p = Bukkit.getPlayer(uid);
                    if (p != null) autoSelectKit(p);
                }
            }
            if (!done) {
                done = true;
                giveKitsToAll();
                gm.onKitSelectDone();
            }
        }, timeoutSeconds * 20L);
    }

    /** ホットバーのキットアイテムを右クリックしたときに呼ぶ */
    public void onInteract(Player p) {
        if (confirmed.contains(p.getUniqueId())) return;
        if (done) return;

        ItemStack held = p.getInventory().getItemInMainHand();
        if (held == null || held.getType() == Material.AIR) return;
        ItemMeta meta = held.getItemMeta();
        if (meta == null) return;

        // ナビゲーションボタン
        String nav = meta.getPersistentDataContainer().get(NAV_KEY, PersistentDataType.STRING);
        if (nav != null) {
            int page = pages.getOrDefault(p.getUniqueId(), 0);
            int maxPage = (KitType.values().length - 1) / KITS_PER_PAGE;
            if ("PREV".equals(nav) && page > 0) pages.put(p.getUniqueId(), page - 1);
            if ("NEXT".equals(nav) && page < maxPage) pages.put(p.getUniqueId(), page + 1);
            renderHotbar(p);
            return;
        }

        // キット選択
        String kitName = meta.getPersistentDataContainer().get(KIT_KEY, PersistentDataType.STRING);
        if (kitName == null) return;

        KitType kit = findKit(kitName);
        if (kit == null) return;

        if (gm.isKitTakenInTeam(p.getUniqueId(), kit.name())) {
            p.sendMessage("§cそのキットはチームメンバーが選択済みです。");
            return;
        }

        // 確定
        gm.setPlayerKit(p.getUniqueId(), kit.name());
        plugin.getStatsManager().addKitPick(p.getUniqueId(), kit.name());
        confirmed.add(p.getUniqueId());
        TeamColor team = gm.getTeamOf(p);
        if (team != null) KitBuilder.giveKit(p, kit, team, plugin);
        p.sendMessage("§a" + kit.getDisplayName() + " §aを選択しました！");
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);

        refreshForTeam(p.getUniqueId());
        checkAllSelected();
    }

    /** インベントリが閉じられても無視（ホットバー方式では不要） */
    public void onInventoryClose(Player p) { /* no-op */ }

    // ─── 内部処理 ───

    private void renderHotbar(Player p) {
        int page = pages.getOrDefault(p.getUniqueId(), 0);
        KitType[] kits = java.util.Arrays.stream(KitType.values()).filter(k -> k != KitType.MIMIC).toArray(KitType[]::new);
        int start = page * KITS_PER_PAGE;
        int maxPage = (kits.length - 1) / KITS_PER_PAGE;

        // ホットバー（0〜8）だけ書き換え
        for (int slot = 0; slot < 9; slot++) p.getInventory().setItem(slot, null);

        // 前ページボタン
        if (page > 0) p.getInventory().setItem(0, navItem("§e§l◀ 前のページ", "PREV"));

        // キット（最大7個）
        for (int i = 0; i < KITS_PER_PAGE; i++) {
            int ki = start + i;
            if (ki >= kits.length) break;
            p.getInventory().setItem(i + 1, kitItem(p, kits[ki]));
        }

        // 次ページボタン
        if (page < maxPage) p.getInventory().setItem(8, navItem("§e§l次のページ ▶", "NEXT"));

        // タイトルバー代わりのメッセージ（初回のみ）
        if (!pages.containsKey(p.getUniqueId())) {
            p.sendMessage("§6§lキットを選択: §f右クリックで選択 §7| §eページ: " + (page + 1) + "/" + (maxPage + 1));
        }
        pages.put(p.getUniqueId(), page);
    }

    private boolean hasHotbarRendered(Player p) {
        ItemStack item = p.getInventory().getItem(1); // slot1にキットがあるか
        if (item == null || item.getItemMeta() == null) return false;
        return item.getItemMeta().getPersistentDataContainer().has(KIT_KEY, PersistentDataType.STRING);
    }

    private void refreshForTeam(UUID selectedBy) {
        TeamColor team = gm.getTeamOf(Bukkit.getPlayer(selectedBy));
        if (team == null) return;
        List<UUID> teammates = team == TeamColor.RED ? gm.getRedTeam() : gm.getBlueTeam();
        for (UUID uid : teammates) {
            if (confirmed.contains(uid)) continue;
            Player p = Bukkit.getPlayer(uid);
            if (p != null) renderHotbar(p);
        }
    }

    private void checkAllSelected() {
        if (done) return;
        if (confirmed.containsAll(allPlayers)) {
            done = true;
            if (timeoutTask  != null) { timeoutTask.cancel();  timeoutTask  = null; }
            if (watchdogTask != null) { watchdogTask.cancel(); watchdogTask = null; }
            giveKitsToAll();
            gm.onKitSelectDone();
        }
    }

    private void giveKitsToAll() {
        for (UUID uid : allPlayers) {
            Player p = Bukkit.getPlayer(uid);
            if (p == null) continue;
            String kitName = gm.getPlayerKit(uid);
            KitType kit = findKit(kitName);
            if (kit == null) kit = KitType.BLADE;
            TeamColor team = gm.getTeamOf(p);
            if (team == null) continue;
            p.setItemOnCursor(null);
            KitBuilder.giveKit(p, kit, team, plugin);
        }
    }

    private void autoSelectKit(Player p) {
        if (confirmed.contains(p.getUniqueId())) return;
        for (KitType kit : KitType.values()) {
            if (kit == KitType.MIMIC) continue;
            if (!gm.isKitTakenInTeam(p.getUniqueId(), kit.name())) {
                gm.setPlayerKit(p.getUniqueId(), kit.name());
                confirmed.add(p.getUniqueId());
                TeamColor t = gm.getTeamOf(p);
                if (t != null) KitBuilder.giveKit(p, kit, t, plugin);
                p.sendMessage("§e自動選択: §a" + kit.getDisplayName());
                return;
            }
        }
        gm.setPlayerKit(p.getUniqueId(), KitType.BLADE.name());
        confirmed.add(p.getUniqueId());
    }

    // ─── アイテム生成 ───

    private ItemStack kitItem(Player p, KitType kit) {
        boolean taken = gm.isKitTakenInTeam(p.getUniqueId(), kit.name());
        Material mat = taken ? Material.BARRIER : iconMaterial(kit);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(taken ? "§c§m" + kit.getName() + " §c(選択済み)" : kit.getDisplayName());
        meta.setLore(Arrays.asList(
            "§7" + kit.getDescription(),
            "§f" + kit.getLore(),
            taken ? "§c選択不可" : "§a► 右クリックで選択"
        ));
        if (!taken) {
            meta.getPersistentDataContainer().set(KIT_KEY, PersistentDataType.STRING, kit.name());
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack navItem(String name, String navValue) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        meta.setLore(Collections.singletonList("§7右クリックでページ切り替え"));
        meta.getPersistentDataContainer().set(NAV_KEY, PersistentDataType.STRING, navValue);
        item.setItemMeta(meta);
        return item;
    }

    private Material iconMaterial(KitType kit) {
        return switch (kit) {
            case BLADE     -> Material.DIAMOND_SWORD;
            case BREAKER   -> Material.DIAMOND_AXE;
            case SCOUT     -> Material.BOW;
            case FLASHER   -> Material.GLOWSTONE_DUST;
            case ROCKETER  -> Material.FIREWORK_ROCKET;
            case ALCHEMIST -> Material.BREWING_STAND;
            case TRAPPER   -> Material.TRIPWIRE_HOOK;
            case GUARDIAN  -> Material.TOTEM_OF_UNDYING;
            case NINJA     -> Material.ENDER_PEARL;
            case BERSERKER -> Material.NETHERITE_AXE;
            case MEDIC     -> Material.TIPPED_ARROW;
            case ENGINEER  -> Material.IRON_PICKAXE;
            case SNIPER    -> Material.CROSSBOW;
            case COUNTER   -> Material.IRON_SWORD;
            case MARKSMAN  -> Material.SPECTRAL_ARROW;
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

    private KitType findKit(String name) {
        if (name == null) return null;
        try { return KitType.valueOf(name); } catch (IllegalArgumentException ignored) {}
        for (KitType k : KitType.values()) {
            if (k.getName().equals(name)) return k;
        }
        return null;
    }
}
