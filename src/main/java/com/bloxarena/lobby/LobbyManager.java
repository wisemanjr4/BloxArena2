package com.bloxarena.lobby;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameState;
import com.bloxarena.map.MapConfig;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

public class LobbyManager {

    private final BloxArenaPlugin plugin;

    private World waitingWorld;
    private Location waitingMin;
    private Location waitingMax;
    private Location lobbySpawn;

    private final Set<UUID> waitingPlayers = new LinkedHashSet<>();
    private BukkitTask countdownTask = null;
    private int currentCountdown = -1;
    private boolean continuousMode = false;
    private Location lobbyOobMin = null;
    private Location lobbyOobMax = null;

    private BossBar countdownBossBar = null;

    private static final int THRESHOLD_4 = 4;
    private static final int THRESHOLD_6 = 6;

    public LobbyManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration cfg = plugin.getConfig();
        String worldName = cfg.getString("lobby.waiting_area.world", "world");
        waitingWorld = Bukkit.getWorld(worldName);

        waitingMin = readLoc(cfg, "lobby.waiting_area.min", worldName);
        waitingMax = readLoc(cfg, "lobby.waiting_area.max", worldName);
        lobbySpawn = readLoc(cfg, "lobby.spawn", worldName);

        // ロビーOOB
        if (cfg.contains("lobby.oob.min.x")) {
            lobbyOobMin = readLoc(cfg, "lobby.oob.min", worldName);
            lobbyOobMax = readLoc(cfg, "lobby.oob.max", worldName);
        }
    }

    private Location readLoc(FileConfiguration cfg, String path, String worldName) {
        World w = Bukkit.getWorld(worldName);
        double x = cfg.getDouble(path + ".x", 0);
        double y = cfg.getDouble(path + ".y", 64);
        double z = cfg.getDouble(path + ".z", 0);
        return new Location(w, x, y, z);
    }

    // ─────────────────────────────────────────────
    // 入退場
    // ─────────────────────────────────────────────

    public void onPlayerEnter(Player p) {
        if (plugin.getGameManager().getState() != GameState.WAITING) {
            if (!plugin.getGameManager().isSpectator(p)) {
                plugin.getGameManager().addSpectator(p);
                p.sendMessage("§e試合が進行中です。観戦モードへ移行しました。");
            }
            return;
        }
        if (!p.hasPermission("bloxarena.admin")) {
            p.setGameMode(GameMode.ADVENTURE);
        }
        // ロビー回復
        var attr = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        double maxHp = attr != null ? attr.getValue() : 20.0;
        p.setHealth(maxHp);
        p.setFoodLevel(20);
        p.setSaturation(5f);
        for (var eff : p.getActivePotionEffects()) p.removePotionEffect(eff.getType());
        if (waitingPlayers.add(p.getUniqueId())) {
            p.sendMessage("§a待機エリアに参加しました。 §7(" + waitingPlayers.size() + "人待機中)");
            // キット一覧ガイドアイテムを配布（ホットバースロット8）
            p.getInventory().setItem(8, plugin.getKitInfoGUI().makeGuideItem());
            updateCountdown();
        }
    }

    public void onPlayerExit(Player p) {
        if (waitingPlayers.remove(p.getUniqueId())) {
            if (plugin.getGameManager().getState() == GameState.WAITING) {
                if (!p.hasPermission("bloxarena.admin")) {
                    p.setGameMode(GameMode.SURVIVAL);
                }
                p.sendMessage("§c待機エリアから退出しました。");
            }
            updateCountdown();
        }
    }

    public void onPlayerQuit(Player p) {
        waitingPlayers.remove(p.getUniqueId());
        updateCountdown();
    }

    // ─────────────────────────────────────────────
    // 待機エリア判定
    // ─────────────────────────────────────────────

    public boolean isInWaitingArea(Location loc) {
        if (waitingMin == null || waitingMax == null) return false;
        if (waitingMin.getWorld() == null) return false;
        if (loc.getWorld() == null || !waitingMin.getWorld().equals(loc.getWorld())) return false;
        // min/max の大小関係を保証（ワンドで逆方向に選択した場合でも正しく判定）
        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        return x >= Math.min(waitingMin.getX(), waitingMax.getX())
            && x <= Math.max(waitingMin.getX(), waitingMax.getX())
            && y >= Math.min(waitingMin.getY(), waitingMax.getY())
            && y <= Math.max(waitingMin.getY(), waitingMax.getY())
            && z >= Math.min(waitingMin.getZ(), waitingMax.getZ())
            && z <= Math.max(waitingMin.getZ(), waitingMax.getZ());
    }

    // ─────────────────────────────────────────────
    // カウントダウン
    // ─────────────────────────────────────────────

    private void updateCountdown() {
        int count = waitingPlayers.size();
        if (count < 2) {
            cancelCountdown();
            return;
        }
        int newTime = getCountdownTime(count);
        if (countdownTask == null) {
            startCountdown(newTime);
        } else if (newTime < currentCountdown) {
            cancelCountdown();
            broadcastWaiting("§e人数増加！カウントダウンを §a" + newTime + "秒 §eに短縮します");
            startCountdown(newTime);
        }
    }

    private int getCountdownTime(int count) {
        if (count >= THRESHOLD_4) return 30;
        return 45;
    }

    private void startCountdown(int seconds) {
        currentCountdown = seconds;
        String timeStr = seconds >= 60 ? (seconds / 60) + "分" : seconds + "秒";
        broadcastWaiting("§a試合が §e" + timeStr + " §a後に開始します");

        countdownTask = new BukkitRunnable() {
            int remaining = seconds;
            @Override
            public void run() {
                if (waitingPlayers.size() < 2) {
                    cancel(); countdownTask = null; currentCountdown = -1; return;
                }
                currentCountdown = remaining;
                if (countdownBossBar != null) {
                    countdownBossBar.setProgress((double) remaining / seconds);
                    countdownBossBar.setTitle("§e§l試合開始まで §6" + remaining + "§e 秒");
                }
                // ActionBar 常時表示
                for (UUID uid : waitingPlayers) {
                    Player p = Bukkit.getPlayer(uid);
                    if (p != null) p.sendActionBar(
                        net.kyori.adventure.text.Component.text("§e試合開始まで: §a" + remaining + "秒"));
                }
                if (remaining == 10) {
                    broadcastTitle("§e⚔ まもなく開始！", "");
                    broadcastSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                } else if (remaining <= 3 && remaining > 0) {
                    broadcastTitle("§c" + remaining, "");
                    broadcastSound(Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                }
                if (remaining <= 0) {
                    if (countdownBossBar != null) { countdownBossBar.removeAll(); countdownBossBar = null; }
                    cancel(); countdownTask = null; currentCountdown = -1;
                    launchGame(); return;
                }
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        countdownBossBar = Bukkit.createBossBar("§e§l試合開始まで...", BarColor.YELLOW, BarStyle.SOLID);
        for (UUID uid : waitingPlayers) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) countdownBossBar.addPlayer(p);
        }
    }

    private void cancelCountdown() {
        if (countdownTask != null) { countdownTask.cancel(); countdownTask = null; }
        if (countdownBossBar != null) { countdownBossBar.removeAll(); countdownBossBar = null; }
        currentCountdown = -1;
    }

    private void launchGame() {
        List<UUID> participants = new ArrayList<>(waitingPlayers);
        waitingPlayers.clear();
        // オフラインプレイヤーを除外（quitしたのに残っていた場合の対策）
        participants.removeIf(uid -> org.bukkit.Bukkit.getPlayer(uid) == null);
        com.bloxarena.game.GameMode mode = com.bloxarena.game.GameMode.random();
        MapConfig map = plugin.getMapManager().selectMap(mode);
        if (map == null) {
            broadcastWaiting("§c使用可能なマップがありません。config.yml を確認してください。");
            return;
        }
        plugin.getGameManager().startGame(map, mode, participants);
    }

    // ─── ロビーOOB ───
    public Location getLobbyOobMin() { return lobbyOobMin; }
    public Location getLobbyOobMax() { return lobbyOobMax; }
    public boolean hasLobbyOob()     { return lobbyOobMin != null && lobbyOobMax != null; }

    public void setLobbyOob(Location min, Location max) {
        this.lobbyOobMin = min.clone();
        this.lobbyOobMax = max.clone();
        FileConfiguration cfg = plugin.getConfig();
        saveLoc(cfg, "lobby.oob.min.", min);
        saveLoc(cfg, "lobby.oob.max.", max);
        plugin.saveConfig();
    }

        public void onGameEnd() { /* 試合終了後は自動でプレイヤーが戻ってくる */ }

    // ─── 連続試合モード ───

    public boolean isContinuousMode() { return continuousMode; }
    public void setContinuousMode(boolean v) {
        this.continuousMode = v;
        broadcastWaiting(v ? "§a連続試合モードが有効になりました。試合終了後に自動で次の試合が始まります。"
                           : "§7連続試合モードが無効になりました。");
    }

    /** 試合終了後に呼ばれ、一定時間後に次の試合を自動開始する */
    public void startContinuousCountdown() {
        if (countdownTask != null) countdownTask.cancel();
        int delay = 15; // 秒
        broadcastWaiting("§e次の試合まで §6" + delay + "秒...");
        countdownBossBar = Bukkit.createBossBar("§e§l試合開始まで...", BarColor.YELLOW, BarStyle.SOLID);
        for (UUID uid : waitingPlayers) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) countdownBossBar.addPlayer(p);
        }
        countdownTask = new BukkitRunnable() {
            int t = delay;
            @Override public void run() {
                if (plugin.getGameManager().getState() != com.bloxarena.game.GameState.WAITING) { cancel(); return; }
                t--;
                if (countdownBossBar != null) {
                    countdownBossBar.setProgress((double) t / delay);
                    countdownBossBar.setTitle("§e§l試合開始まで §6" + t + "§e 秒");
                }
                if (t <= 0) {
                    if (countdownBossBar != null) { countdownBossBar.removeAll(); countdownBossBar = null; }
                    cancel();
                    countdownTask = null; // タスク終了後にnull化（updateCountdown誤動作防止）
                    java.util.List<java.util.UUID> participants =
                        new java.util.ArrayList<>(getWaitingPlayers());
                    if (participants.size() < 2) {
                        broadcastWaiting("§c人数が足りないため次の試合を開始できません。");
                        return;
                    }
                    com.bloxarena.game.GameMode mode = com.bloxarena.game.GameMode.random();
                    com.bloxarena.map.MapConfig map = plugin.getMapManager().selectMap(mode);
                    if (map == null) { broadcastWaiting("§c使用可能なマップがありません。"); return; }
                    plugin.getGameManager().startGame(map, mode, participants);
                } else if (t <= 5 || t % 5 == 0) {
                    broadcastWaiting("§e次の試合まで §6" + t + "秒...");
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    // ─────────────────────────────────────────────
    // ユーティリティ
    // ─────────────────────────────────────────────

    private void saveLoc(FileConfiguration cfg, String prefix, Location loc) {
        cfg.set(prefix + "x", loc.getX());
        cfg.set(prefix + "y", loc.getY());
        cfg.set(prefix + "z", loc.getZ());
    }

    private void broadcastWaiting(String msg) {
        for (UUID uid : waitingPlayers) {
            Player p = Bukkit.getPlayer(uid); if (p != null) p.sendMessage(msg);
        }
    }
    private void broadcastTitle(String title, String sub) {
        for (UUID uid : waitingPlayers) {
            Player p = Bukkit.getPlayer(uid); if (p != null) p.sendTitle(title, sub, 5, 30, 5);
        }
    }
    private void broadcastSound(Sound sound, float vol, float pitch) {
        for (UUID uid : waitingPlayers) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) p.playSound(p.getLocation(), sound, vol, pitch);
        }
    }

    // ─────────────────────────────────────────────
    // 座標設定 (コマンドから呼ばれる)
    // ─────────────────────────────────────────────

    public void setWaitingAreaMin(Location loc) {
        this.waitingMin = loc.clone();
        plugin.getConfig().set("lobby.waiting_area.world", loc.getWorld().getName());
        plugin.getConfig().set("lobby.waiting_area.min.x", loc.getBlockX());
        plugin.getConfig().set("lobby.waiting_area.min.y", loc.getBlockY());
        plugin.getConfig().set("lobby.waiting_area.min.x", loc.getBlockX());
        plugin.getConfig().set("lobby.waiting_area.min.y", loc.getBlockY());
        plugin.getConfig().set("lobby.waiting_area.min.z", loc.getBlockZ());
        plugin.saveConfig();
    }

    public void setWaitingAreaMax(Location loc) {
        this.waitingMax = loc.clone();
        plugin.getConfig().set("lobby.waiting_area.max.x", loc.getBlockX());
        plugin.getConfig().set("lobby.waiting_area.max.y", loc.getBlockY());
        plugin.getConfig().set("lobby.waiting_area.max.z", loc.getBlockZ());
        plugin.saveConfig();
    }

    public void setLobbySpawn(Location loc) {
        this.lobbySpawn = loc.clone();
        plugin.getConfig().set("lobby.spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("lobby.spawn.x", loc.getX());
        plugin.getConfig().set("lobby.spawn.y", loc.getY());
        plugin.getConfig().set("lobby.spawn.z", loc.getZ());
        plugin.saveConfig();
    }

    public Location getLobbySpawn()   { return lobbySpawn; }
    public Location getWaitingMin()   { return waitingMin; }
    public Location getWaitingMax()   { return waitingMax; }
    public World    getWaitingWorld()  { return waitingWorld; }
    public Set<UUID> getWaitingPlayers() { return Collections.unmodifiableSet(waitingPlayers); }
    public int getCurrentCountdown()  { return currentCountdown; }
}
