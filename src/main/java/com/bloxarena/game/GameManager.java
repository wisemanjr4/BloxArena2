package com.bloxarena.game;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.stats.MatchStats;
import com.bloxarena.stats.StatsManager;
import com.bloxarena.kit.KitType;
import com.bloxarena.skill.SkillManager;

import com.bloxarena.kit.KitSelectGUI;
import com.bloxarena.map.MapConfig;
import com.bloxarena.util.Effects;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private final BloxArenaPlugin plugin;

    private GameState state = GameState.WAITING;
    private MapConfig currentMap = null;

    private final List<UUID> redTeam = new ArrayList<>();
    private final List<UUID> blueTeam = new ArrayList<>();
    private final Set<UUID> spectators = new LinkedHashSet<>();

    // 落下ダメージ免除フラグ
    private final Set<UUID> noFallDamage = new HashSet<>();
    private final Set<UUID> deadPlayers  = new HashSet<>();  // 死亡済み（リスポーン待ち）
    private MatchStats matchStats = new MatchStats();

    // キル・デス統計
    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();
    private final Map<UUID, Integer> roundKills = new HashMap<>(); // ラウンド内連続キル数

    // キット選択状態
    private final Map<UUID, String> selectedKits = new HashMap<>(); // チーム別選択済みキット名
    private final Map<UUID, String> playerKit = new HashMap<>();

    private BukkitTask endingTask = null;
    private BukkitTask holdTask    = null;
    private TeamColor  holdingTeam = null;
    private long inGameStartTime   = 0;
    private static final long OBJECTIVE_LOCK_MS = 2 * 60 * 1000L; // 2分ロック
    private static final int  HOLD_SECONDS      = 15;             // ホールド秒数

    // ─── ラウンドシステム ───
    private static final int WINS_TO_WIN = 3;
    private int currentRound   = 0;
    private int roundWinsRed   = 0;
    private int roundWinsBlue  = 0;
    private GameMode currentGameMode = GameMode.BATTLE_ARENA;

    private org.bukkit.boss.BossBar preBattleBossBar = null;

    // TDM
    private int tdmKillsRed = 0;
    private int tdmKillsBlue = 0;
    private long tdmStartTime = 0;
    private BukkitTask tdmTimerTask = null;

    // Bomb Mission
    private boolean bombPlanted = false;
    private Location bombLoc = null;
    private BukkitTask bombTimerTask = null;
    private int bombSecondsRemaining = 0;
    private boolean bombDefusing = false;
    private Player bombDefuser = null;
    private int defuseProgress = 0;
    private boolean bombRoundAttackerRed = true;

    // Domination
    private final Map<Integer, Float> domCapProgress = new HashMap<>();
    private final Map<Integer, TeamColor> domCapOwner = new HashMap<>();
    private int domPointsRed = 0;
    private int domPointsBlue = 0;
    private BukkitTask domTimerTask = null;

    // CTF
    private boolean redFlagTaken = false;
    private boolean blueFlagTaken = false;
    private UUID redFlagCarrier = null;
    private UUID blueFlagCarrier = null;
    private long redFlagDropTime = -1;
    private long blueFlagDropTime = -1;
    private int ctfRedCaptures = 0;
    private int ctfBlueCaptures = 0;
    private final Map<Integer, Material> ctfOriginalBlocks = new HashMap<>();
    private final Map<UUID, Long> ctfPickupCooldown = new HashMap<>();

    public GameManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────
    // ゲーム開始処理
    // ─────────────────────────────────────────────

    public void startGame(MapConfig map, GameMode mode, List<UUID> participants) {
        this.currentMap = map;
        this.currentGameMode = mode;
        if (currentGameMode == GameMode.BOMB_MISSION) {
            bombRoundAttackerRed = true;
        }
        Bukkit.broadcastMessage("§d§l[ゲームモード] §f" + currentGameMode.getDisplayName() + " §7- " + currentGameMode.getDescription());
        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(pl.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.5f);
        }
        for (UUID uid : participants) {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) {
                pl.sendTitle("§d§l⚡ ゲームモード ⚡", "§f" + currentGameMode.getDisplayName() + " §7- " + currentGameMode.getDescription(), 5, 50, 15);
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String rules = switch (currentGameMode) {
                case BATTLE_ARENA -> "§6§lルール§f: 敵を殲滅するか、中央コンクリート(25枚)を自色で埋めて15秒ホールドせよ！BO3先取制。";
                case TEAM_DEATHMATCH -> "§6§lルール§f: 制限時間内により多くの敵を倒せ！死亡しても3秒でリスポーン。目標30キル先取でも勝利。";
                case BOMB_MISSION -> "§6§lルール§f: 攻撃側は爆弾を設置(5秒)→爆発45秒。守備側は解除(7秒)せよ！ラウンド毎に攻守交代。";
                case DOMINATION -> "§6§lルール§f: 拠点に立ち続けて占領せよ！占領拠点から毎秒ポイント獲得。先に目標ポイント到達で勝利。";
                case CAPTURE_THE_FLAG -> "§6§lルール§f: 敵陣の旗を奪い自陣に持ち帰れ！先に3回奪取で勝利。死亡時は旗を落とす。";
            };
            Bukkit.broadcastMessage(rules);
        }, 60L);
        this.state = GameState.KIT_SELECT;
        this.currentRound  = 1;
        this.roundWinsRed  = 0;
        this.roundWinsBlue = 0;

        // チームをクリアして再振り分け
        redTeam.clear();
        blueTeam.clear();
        kills.clear();
        deaths.clear();
        selectedKits.clear();
        playerKit.clear();
        noFallDamage.clear();
        deadPlayers.clear();

        assignTeams(participants);
        // Restore all max HP
        for (UUID uid : participants) {
            Player pp = Bukkit.getPlayer(uid);
            if (pp != null) plugin.getSkillManager().restoreMaxHp(pp);
        }
        // マップ名を全員にタイトル表示
        Bukkit.broadcastMessage("§6§l[BAII WoNG] §eMAP: §f" + map.getDisplayName());
        broadcastTeamAnnouncement();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (UUID uid : participants) {
                Player pl = Bukkit.getPlayer(uid);
                if (pl != null && pl.isOnline()) {
                    TeamColor t = getTeamOf(pl);
                    if (t != null) {
                        pl.sendTitle(t.getColorCode() + "§lあなたは " + t.getDisplayName() + " チーム", "§7キット選択画面で準備してください", 5, 40, 10);
                    }
                }
            }
        }, 50L);

        // マップ初期化（中央コンクリート設置）
        initCenterBlocks(map);

        // ゲート設置
        placeGates(map);

        // BOTをスポーン（pendingBotsがあれば）
        plugin.getBotManager().spawnBotsForGame(redTeam, blueTeam, map);

        // 各プレイヤーをスポーンゾーンへ転送しキット選択GUI
        for (UUID uid : redTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p == null) continue;
            kills.put(uid, 0);
            deaths.put(uid, 0);
            p.setGameMode(org.bukkit.GameMode.SURVIVAL);
            teleportToSpawnZonePublic(p, map, TeamColor.RED);
        }
        for (UUID uid : blueTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p == null) continue;
            kills.put(uid, 0);
            deaths.put(uid, 0);
            p.setGameMode(org.bukkit.GameMode.SURVIVAL);
            teleportToSpawnZonePublic(p, map, TeamColor.BLUE);
        }

        // キット選択GUIを開く
        int timeoutSeconds = plugin.getConfig().getInt("kit_select.timeout_seconds", 30);
        KitSelectGUI gui = new KitSelectGUI(plugin, this);
        plugin.getGameListeners().setActiveGUI(gui);
        gui.openForAll(redTeam, blueTeam, timeoutSeconds);
    }

    // キット選択完了後に呼ばれる（KitSelectGUIから）
    public void onKitSelectDone() {
        state = GameState.IN_GAME;
        inGameStartTime = System.currentTimeMillis();
        plugin.getScoreboardManager().start(this);
        // Distribute burst skill items
        for (UUID uid : redTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) plugin.getSkillManager().refreshBurst(p);
        }
        for (UUID uid : blueTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) plugin.getSkillManager().refreshBurst(p);
        }

        if (currentGameMode == GameMode.DOMINATION) {
            domPointsRed = 0;
            domPointsBlue = 0;
            domCapProgress.clear();
            domCapOwner.clear();
            if (currentMap != null) {
                int idx = 0;
                for (MapConfig.DomPoint dp : currentMap.getDominationPoints()) {
                    domCapProgress.put(idx, 0f);
                    domCapOwner.put(idx, null);
                    Location cloc = dp.getCenter();
                    if (cloc.getWorld() != null) {
                        cloc.getBlock().setType(Material.BEACON);
                    }
                    idx++;
                }
            }
            int timeLimit = plugin.getConfig().getInt("domination.time_limit_seconds", 120);
            int targetPoints = plugin.getConfig().getInt("domination.target_points", 100);
            domTimerTask = new BukkitRunnable() {
                @Override public void run() {
                    if (state != GameState.IN_GAME) { cancel(); return; }
                    updateDomination(currentMap, targetPoints);
                    long elapsed = (System.currentTimeMillis() - inGameStartTime) / 1000;
                    if (elapsed >= timeLimit) {
                        cancel();
                        TeamColor winner = domPointsRed > domPointsBlue ? TeamColor.RED
                            : domPointsBlue > domPointsRed ? TeamColor.BLUE : null;
                        endGame(winner, WinCondition.OBJECTIVE);
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }

        startCountdownBeforeBarrierRemoval();

        // TDM timer
        if (currentGameMode == GameMode.TEAM_DEATHMATCH) {
            tdmKillsRed = 0;
            tdmKillsBlue = 0;
            tdmStartTime = System.currentTimeMillis();
            int timeLimit = plugin.getConfig().getInt("team_deathmatch.time_limit_seconds", 300);
            tdmTimerTask = new BukkitRunnable() {
                @Override public void run() {
                    if (state != GameState.IN_GAME) { cancel(); return; }
                    long elapsed = (System.currentTimeMillis() - tdmStartTime) / 1000;
                    long remaining = timeLimit - elapsed;
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        if (isParticipant(pl) || isSpectator(pl))
                            pl.sendActionBar(net.kyori.adventure.text.Component.text(
                                "§c⚔ TDM §7| §c赤:" + tdmKillsRed + " §9青:" + tdmKillsBlue
                                + " §7| §e残り " + formatTime(remaining)));
                    }
                    if (remaining <= 0) {
                        cancel();
                        TeamColor winner = tdmKillsRed > tdmKillsBlue ? TeamColor.RED
                            : tdmKillsBlue > tdmKillsRed ? TeamColor.BLUE : null;
                        endGame(winner, WinCondition.ELIMINATION);
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }

        // Bomb Mission
        if (currentGameMode == GameMode.BOMB_MISSION) {
            TeamColor attacker = bombRoundAttackerRed ? TeamColor.RED : TeamColor.BLUE;
            for (UUID uid : (attacker == TeamColor.RED ? redTeam : blueTeam)) {
                Player pl = Bukkit.getPlayer(uid);
                if (pl != null) {
            ItemStack bomb = new ItemStack(Material.TNT);
            ItemMeta m = bomb.getItemMeta();
            if (m != null) { m.setDisplayName("§c§l💣 爆弾 §7(設置地点で右クリック)"); bomb.setItemMeta(m); }
            pl.getInventory().addItem(bomb);
                }
            }
            broadcastBombRoundInfo();
        }

        // CTF flag init
        if (currentGameMode == GameMode.CAPTURE_THE_FLAG) {
            ctfRedCaptures = 0;
            ctfBlueCaptures = 0;
            redFlagTaken = false; blueFlagTaken = false;
            redFlagCarrier = null; blueFlagCarrier = null;
            if (currentMap != null) {
                if (currentMap.getRedFlagLocation() != null) {
                    Location rloc = currentMap.getRedFlagLocation();
                    ctfOriginalBlocks.put(0, rloc.getBlock().getType());
                    rloc.getBlock().setType(Material.RED_BANNER);
                }
                if (currentMap.getBlueFlagLocation() != null) {
                    Location bloc = currentMap.getBlueFlagLocation();
                    ctfOriginalBlocks.put(1, bloc.getBlock().getType());
                    bloc.getBlock().setType(Material.CYAN_BANNER);
                }
            }
            new BukkitRunnable() {
                @Override public void run() {
                    if (state != GameState.IN_GAME) { cancel(); return; }
                    updateCTF();
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    private void updateDomination(MapConfig map, int targetPoints) {
        if (map == null) return;
        int idx = 0;
        int pointsPerSec = plugin.getConfig().getInt("domination.points_per_second", 2);
        for (MapConfig.DomPoint dp : map.getDominationPoints()) {
            Location center = dp.getCenter();
            double radius = dp.getRadius();
            World w = center.getWorld();
            if (w == null) { idx++; continue; }

            int redCount = 0, blueCount = 0;
            for (Player pl : w.getPlayers()) {
                if (!isParticipant(pl) || isSpectator(pl)) continue;
                if (pl.getLocation().distance(center) > radius) continue;
                if (redTeam.contains(pl.getUniqueId())) redCount++;
                else if (blueTeam.contains(pl.getUniqueId())) blueCount++;
            }

            float progress = domCapProgress.getOrDefault(idx, 0f);
            if (redCount > blueCount) {
                progress = Math.min(1f, progress + 0.05f * (redCount - blueCount));
            } else if (blueCount > redCount) {
                progress = Math.max(-1f, progress - 0.05f * (blueCount - redCount));
            }
            domCapProgress.put(idx, progress);

            TeamColor prev = domCapOwner.get(idx);
            TeamColor now = progress >= 0.8f ? TeamColor.RED : progress <= -0.8f ? TeamColor.BLUE : null;
            if (now != prev) {
                domCapOwner.put(idx, now);
                if (now != null) {
                    Bukkit.broadcastMessage(now.getColorCode() + "拠点" + (idx+1) + "を占領！");
                }
            }

            if (center.getBlock().getType() == Material.BEACON) {
                org.bukkit.Color c = org.bukkit.Color.fromRGB(
                    progress > 0 ? (int)(255*progress) : 0, 0,
                    progress < 0 ? (int)(-255*progress) : 0);
                w.spawnParticle(Particle.REDSTONE, center.clone().add(0, 1, 0), 5, 1, 0.5, 1,
                    new org.bukkit.Particle.DustOptions(c, 1.5f));
            }

            if (now == TeamColor.RED) domPointsRed += pointsPerSec;
            else if (now == TeamColor.BLUE) domPointsBlue += pointsPerSec;

            idx++;
        }

        if (domPointsRed >= targetPoints) { endGame(TeamColor.RED, WinCondition.OBJECTIVE); return; }
        if (domPointsBlue >= targetPoints) { endGame(TeamColor.BLUE, WinCondition.OBJECTIVE); return; }

        for (UUID uid : getAllParticipantsAndSpectators()) {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) pl.sendActionBar(net.kyori.adventure.text.Component.text(
                "§e⚑ 占領戦 §7| §c赤:" + domPointsRed + "pts §9青:" + domPointsBlue + "pts"));
        }
    }

    private void startCountdownBeforeBarrierRemoval() {
        // キット選択完了 → ステージ名アナウンス
        String mapName = (currentMap != null) ? currentMap.getDisplayName() : "???";
        broadcastTitle("§6§l⚔ BAII WoNG", "§e" + mapName + " §7- §fバトル開始まで待機中…", 10, 60, 10);
        broadcastSound(Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.2f);

        preBattleBossBar = Bukkit.createBossBar("§c§l⚔ バトル準備中...", org.bukkit.boss.BarColor.RED, org.bukkit.boss.BarStyle.SOLID);
        for (UUID uid : getAllParticipantsAndSpectators()) {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) preBattleBossBar.addPlayer(pl);
        }

        new BukkitRunnable() {
            int count = 5;
            @Override
            public void run() {
                if (count > 0) {
                    // 数字ごとに色・ピッチを変化させてド派手に
                    String color = switch (count) {
                        case 5 -> "§c§l";
                        case 4 -> "§6§l";
                        case 3 -> "§e§l";
                        case 2 -> "§a§l";
                        default -> "§b§l";
                    };
                    float pitch = 0.6f + (5 - count) * 0.15f;
                    broadcastTitle(color + count, "§7" + mapName, 0, 22, 3);
                    broadcastSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.2f, pitch);
                    broadcastActionBar("§f準備しろ！§e" + count + "§f秒後にゲートが開く！");
                    if (preBattleBossBar != null) {
                        preBattleBossBar.setProgress(count / 5.0);
                        preBattleBossBar.setTitle("§c§l⚔ ゲート開放まで §e" + count + "§c 秒");
                    }
                    count--;
                } else {
                    if (preBattleBossBar != null) { preBattleBossBar.removeAll(); preBattleBossBar = null; }
                    cancel();
                    removeGates(currentMap); // ゲート開放
                    grantNoFallDamage();
                    broadcastTitle("§c§l⚔  FIGHT!!  ⚔", "§e" + mapName + " §7| §fオブジェクトを制圧せよ！", 3, 50, 12);
                    broadcastSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 1.0f);
                    broadcastSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.3f);
                    broadcastSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    broadcastActionBar("§c§l⚔ FIGHT!! §7| §e2分後にオブジェクトが解放される！");
                    // 中央に雷エフェクト（ダメージなし）
                    if (currentMap != null && currentMap.getCenter() != null) {
                        currentMap.getCenter().getWorld().strikeLightningEffect(currentMap.getCenter());
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (currentMap != null && currentMap.getCenter() != null)
                                currentMap.getCenter().getWorld().strikeLightningEffect(currentMap.getCenter());
                        }, 5L);
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 20L); // 2秒後からカウント開始
    }


    // ─────────────────────────────────────────────
    // チーム振り分け
    // ─────────────────────────────────────────────

    private void assignTeams(List<UUID> participants) {
        List<UUID> shuffled = new ArrayList<>(participants);
        // 毎回新しいRandomで確実にシャッフル
        java.util.Random rng = new java.util.Random();
        for (int i = shuffled.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            UUID tmp = shuffled.get(i);
            shuffled.set(i, shuffled.get(j));
            shuffled.set(j, tmp);
        }
        int half = shuffled.size() / 2;
        // 奇数なら赤か青にランダムで1人多く
        int redCount = half + (shuffled.size() % 2 == 1 && rng.nextBoolean() ? 1 : 0);
        for (int i = 0; i < shuffled.size(); i++) {
            if (i < redCount) redTeam.add(shuffled.get(i));
            else blueTeam.add(shuffled.get(i));
        }
        // デバッグ: チーム割り当て結果をログ
        StringBuilder log = new StringBuilder("§8[BA] チーム振り分け: §c赤=");
        for (UUID uid : redTeam) { Player p = Bukkit.getPlayer(uid); if (p != null) log.append(p.getName()).append(" "); }
        log.append("§b青=");
        for (UUID uid : blueTeam) { Player p = Bukkit.getPlayer(uid); if (p != null) log.append(p.getName()).append(" "); }
        Bukkit.getConsoleSender().sendMessage(log.toString());
    }

    private void broadcastTeamAnnouncement() {
        StringBuilder sb = new StringBuilder();
        sb.append("§c[赤] §f");
        for (UUID uid : redTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) sb.append(p.getName()).append(", ");
        }
        sb.append(" §b[青] §f");
        for (UUID uid : blueTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) sb.append(p.getName()).append(", ");
        }
        Bukkit.broadcastMessage(sb.toString());

        for (UUID uid : redTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) {
                p.sendTitle("§c赤チーム", "§fあなたは赤チームです", 5, 60, 10);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
        }
        for (UUID uid : blueTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) {
                p.sendTitle("§b青チーム", "§fあなたは青チームです", 5, 60, 10);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
        }
    }

    // ─────────────────────────────────────────────
    // 中央コンクリート初期化
    // ─────────────────────────────────────────────

    private void initCenterBlocks(MapConfig map) {
        World world = Bukkit.getWorld(map.getWorld());
        if (world == null) return;
        Location center = map.getCenter();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        // 土台: Y-1 に緑コンクリート、Y に白コンクリート (5x5)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.getBlockAt(cx + dx, cy - 1, cz + dz).setType(Material.LIME_CONCRETE);
                world.getBlockAt(cx + dx, cy, cz + dz).setType(Material.WHITE_CONCRETE);
            }
        }
    }

    private void resetCenterBlocks() {
        if (currentMap == null) return;
        World world = Bukkit.getWorld(currentMap.getWorld());
        if (world == null) return;
        Location center = currentMap.getCenter();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.getBlockAt(cx + dx, cy, cz + dz).setType(Material.WHITE_CONCRETE);
            }
        }
        // ホールドタイマーリセット
        cancelHoldTimer();
    }

    // ─────────────────────────────────────────────
    // バリア操作
    // ─────────────────────────────────────────────


    // ─── ゲートシステム ───

    /** ゲート領域をブロックで埋める（試合開始時に呼ぶ） */
    public void placeGates(MapConfig map) {
        if (!map.hasGate()) return;
        Material mat = map.getGateMaterial();
        fillRegion(map.getRedGateMin(),  map.getRedGateMax(),  mat);
        fillRegion(map.getBlueGateMin(), map.getBlueGateMax(), mat);
    }

    /** ゲート領域を空気にする（カウントダウン終了時に呼ぶ） */
    public void removeGates(MapConfig map) {
        if (!map.hasGate()) return;
        fillRegion(map.getRedGateMin(),  map.getRedGateMax(),  Material.AIR);
        fillRegion(map.getBlueGateMin(), map.getBlueGateMax(), Material.AIR);
    }

    /** 指定領域内の全ブロックを指定マテリアルに書き換える */
    private void fillRegion(Location min, Location max, Material material) {
        if (min == null || max == null || min.getWorld() == null) return;
        World world = min.getWorld();
        int x1 = Math.min(min.getBlockX(), max.getBlockX());
        int y1 = Math.min(min.getBlockY(), max.getBlockY());
        int z1 = Math.min(min.getBlockZ(), max.getBlockZ());
        int x2 = Math.max(min.getBlockX(), max.getBlockX());
        int y2 = Math.max(min.getBlockY(), max.getBlockY());
        int z2 = Math.max(min.getBlockZ(), max.getBlockZ());
        for (int x = x1; x <= x2; x++)
            for (int y = y1; y <= y2; y++)
                for (int z = z1; z <= z2; z++)
                    world.getBlockAt(x, y, z).setType(material);
    }

    // バリアシステム廃止（ゲートシステムに統合済み）

    private void grantNoFallDamage() {
        for (UUID uid : getAllParticipants()) {
            noFallDamage.add(uid);
        }
    }

    // ─────────────────────────────────────────────
    // スポーン転送
    // ─────────────────────────────────────────────

    public void teleportToSpawnZonePublic(Player p, MapConfig map, TeamColor team) {
        Location min = team == TeamColor.RED ? map.getRedSpawnMin() : map.getBlueSpawnMin();
        Location max = team == TeamColor.RED ? map.getRedSpawnMax() : map.getBlueSpawnMax();
        // ゾーン中央上部へ
        double x = (min.getBlockX() + max.getBlockX()) / 2.0 + 0.5;
        double y = max.getBlockY() + 0.1;
        double z = (min.getBlockZ() + max.getBlockZ()) / 2.0 + 0.5;
        p.teleport(new Location(min.getWorld(), x, y, z));
    }

    // ─────────────────────────────────────────────
    // 勝利チェック
    // ─────────────────────────────────────────────

    public void checkEliminationWin() {
        if (state != GameState.IN_GAME) return;
        long redAlive = getAliveCount(TeamColor.RED);
        long blueAlive = getAliveCount(TeamColor.BLUE);

        if (blueAlive == 0 && redAlive > 0) {
            endGame(TeamColor.RED, WinCondition.ELIMINATION);
        } else if (redAlive == 0 && blueAlive > 0) {
            endGame(TeamColor.BLUE, WinCondition.ELIMINATION);
        } else if (redAlive == 0 && blueAlive == 0) {
            endGame(null, WinCondition.ELIMINATION); // 引き分け（稀ケース）
        }
    }

    public void checkObjectiveWin(Location placedBlock) {
        if (state != GameState.IN_GAME) return;
        if (currentGameMode != GameMode.BATTLE_ARENA) return;
        if (currentMap == null) return;

        Location center = currentMap.getCenter();
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int checkY = placedBlock.getBlockY();

        World world = Bukkit.getWorld(currentMap.getWorld());
        if (world == null) return;

        // 進捗カウント＋通知
        int redCount = 0, cyanCount = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Material mat = world.getBlockAt(cx + dx, checkY, cz + dz).getType();
                if (mat == Material.RED_CONCRETE)  redCount++;
                if (mat == Material.CYAN_CONCRETE) cyanCount++;
            }
        }
        Bukkit.broadcastMessage("§6[BA] §eオブジェクトが奪取されています！ §c赤:" + redCount + "/25 §b青:" + cyanCount + "/25");

        // 25枚全部同じ色か確認
        Material first = null;
        boolean allSame = true;
        outer:
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Material mat = world.getBlockAt(cx + dx, checkY, cz + dz).getType();
                if (first == null) { first = mat; }
                else if (mat != first) { allSame = false; break outer; }
            }
        }
        if (!allSame || first == null) { cancelHoldTimer(); return; }

        TeamColor winner = null;
        if (first == Material.RED_CONCRETE)  winner = TeamColor.RED;
        if (first == Material.CYAN_CONCRETE) winner = TeamColor.BLUE;
        if (winner == null) return;

        if (holdingTeam == winner && holdTask != null) return; // 既にホールド中
        cancelHoldTimer();
        holdingTeam = winner;
        final TeamColor fw = winner;
        broadcastTitle(winner.getColorCode() + "§l25枚制圧！", "§e15秒ホールドで勝利", 5, 30, 5);
        Bukkit.broadcastMessage(winner.getColorCode() + "§l[BA] " + winner.getDisplayName() + " チームが25枚制圧！15秒ホールドで勝利！");
        holdTask = new org.bukkit.scheduler.BukkitRunnable() {
            int rem = HOLD_SECONDS;
            @Override public void run() {
                if (state != GameState.IN_GAME) { cancel(); return; }
                if (rem <= 0) { cancel(); endGame(fw, WinCondition.OBJECTIVE); return; }
                broadcastActionBar(fw.getColorCode() + "§l" + fw.getDisplayName() + " §eホールド §f" + rem + "§e秒");
                rem--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /** ブロック破壊時：ホールドタイマーをリセット */
    public void onObjectiveBlockBroken() {
        if (holdTask != null) {
            cancelHoldTimer();
            Bukkit.broadcastMessage("§6[BA] §cホールドが中断されました！");
        }
    }

    private void cancelHoldTimer() {
        if (holdTask != null) { holdTask.cancel(); holdTask = null; }
        holdingTeam = null;
    }

    /** オブジェクトロック中か（試合開始3分以内） */
    public boolean isObjectiveLocked() {
        return System.currentTimeMillis() - inGameStartTime < OBJECTIVE_LOCK_MS;
    }

    // ─────────────────────────────────────────────
    // ゲーム終了処理
    // ─────────────────────────────────────────────

    public void endGame(TeamColor winner, WinCondition condition) {
        // TDM/Domination/CTF: no rounds, go directly to match end
        if (currentGameMode == GameMode.TEAM_DEATHMATCH
                || currentGameMode == GameMode.DOMINATION
                || currentGameMode == GameMode.CAPTURE_THE_FLAG) {
            endMatch(winner, condition);
        } else {
            endRound(winner, condition);
        }
    }

    private void endRound(TeamColor winner, WinCondition condition) {
        if (state == GameState.ENDING) return;
        state = GameState.ENDING;
        noFallDamage.clear();
        plugin.getScoreboardManager().stop();
        // ラウンド終了: 投射中のエンティティ（エンダーパール・ロケット・ポーション等）を除去
        if (currentMap != null && currentMap.getCenter() != null) {
            org.bukkit.World w = currentMap.getCenter().getWorld();
            if (w != null) {
                w.getEntities().stream()
                    .filter(e -> e instanceof org.bukkit.entity.EnderPearl
                        || e instanceof org.bukkit.entity.Firework
                        || e instanceof org.bukkit.entity.ThrownPotion
                        || e instanceof org.bukkit.entity.Arrow
                        || e instanceof org.bukkit.entity.SpectralArrow)
                    .forEach(org.bukkit.entity.Entity::remove);
            }
        }

        // ラウンド勝利数を加算
        if (winner == TeamColor.RED)  roundWinsRed++;
        else if (winner == TeamColor.BLUE) roundWinsBlue++;

        // ラウンド結果を全員に表示
        String roundResult = winner != null
            ? winner.getColorCode() + winner.getDisplayName() + " §aがラウンドを制しました！"
            : "§7引き分け";
        String scoreStr = "§c赤 " + roundWinsRed + " §7- §9" + roundWinsBlue + " §7青";
        for (UUID uid : getAllParticipantsAndSpectators()) {
            Player p = Bukkit.getPlayer(uid);
            if (p == null) continue;
            p.sendTitle(roundResult, scoreStr, 5, 50, 10);
            p.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            p.sendMessage("§6§l  ラウンド " + currentRound + " 終了");
            p.sendMessage("§7結果: " + roundResult);
            p.sendMessage("§7スコア: " + scoreStr);
            p.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }

        // 3点先取でマッチ終了
        if (roundWinsRed >= WINS_TO_WIN || roundWinsBlue >= WINS_TO_WIN) {
            endingTask = new BukkitRunnable() {
                @Override public void run() { endMatch(winner, condition); }
            }.runTaskLater(plugin, 80L); // 4秒後
        } else {
            // 次ラウンドへ
            endingTask = new BukkitRunnable() {
                @Override public void run() { startNextRound(); }
            }.runTaskLater(plugin, 80L); // 4秒後
        }
    }

    private void startNextRound() {
        currentRound++;
        if (currentGameMode == GameMode.BOMB_MISSION) {
            bombRoundAttackerRed = !bombRoundAttackerRed;
            bombCleanup();
        }
        String scoreStr = "§c赤 " + roundWinsRed + " §7- §9" + roundWinsBlue + " §7青";
        broadcastTitle("§6§l⚔ ラウンド " + currentRound + " ⚔", scoreStr, 10, 60, 10);
        for (UUID uid : getAllParticipantsAndSpectators()) {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) pl.sendMessage("§8§m                                ");
            if (pl != null) pl.sendMessage("§6§l★ ラウンド " + currentRound + " 開始！ ★");
            if (pl != null) pl.sendMessage("§7" + scoreStr);
            if (pl != null) pl.sendMessage("§8§m                                ");
        }
        state = GameState.IN_GAME;
        noFallDamage.clear();
        deadPlayers.clear();
        matchStats = new MatchStats();
        deaths.clear();
        roundKills.clear(); // ラウンドごとに連続キルをリセット
        // kills はマッチ通算でそのまま継続

        // マップリセット
        resetCenterBlocks();
        initCenterBlocks(currentMap);
        placeGates(currentMap);

        // 全プレイヤーを復活させてスポーンへ（インベントリはキット選択後に配布）
        for (UUID uid : redTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p == null) continue;
            roundRestorePlayer(p);
            teleportToSpawnZonePublic(p, currentMap, TeamColor.RED);
        }
        for (UUID uid : blueTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p == null) continue;
            roundRestorePlayer(p);
            teleportToSpawnZonePublic(p, currentMap, TeamColor.BLUE);
        }
        spectators.clear();
        selectedKits.clear();
        playerKit.clear();

        plugin.getSkillManager().resetRound();

        // キット選択GUI
        state = GameState.KIT_SELECT;
        int timeoutSeconds = plugin.getConfig().getInt("kit_select.timeout_seconds", 30);
        KitSelectGUI gui = new KitSelectGUI(plugin, this);
        plugin.getGameListeners().setActiveGUI(gui);
        gui.openForAll(redTeam, blueTeam, timeoutSeconds);

        broadcastTitle("§6§lラウンド " + currentRound, scoreStr, 5, 40, 10);
    }

    /** ラウンド間の体力・状態リセット（ロビーには戻さない） */
    private void roundRestorePlayer(Player p) {
        deadPlayers.remove(p.getUniqueId());
        spectators.remove(p.getUniqueId());
        p.setInvulnerable(false);
        p.setGameMode(org.bukkit.GameMode.SURVIVAL);
        p.getInventory().clear();
        var attr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHp = attr != null ? attr.getValue() : 20.0;
        p.setHealth(maxHp);
        p.setFoodLevel(20);
        p.setSaturation(5f);
        p.setExhaustion(0f);
        for (var eff : p.getActivePotionEffects()) p.removePotionEffect(eff.getType());
        p.setFireTicks(0);
        p.setArrowsInBody(0);
        p.setWalkSpeed(0.2f);
        p.resetTitle();
        plugin.getSkillManager().restoreMaxHp(p);
    }

    public com.bloxarena.kit.KitType getPlayerKitType(UUID uid) {
        String name = playerKit.get(uid);
        if (name == null) return null;
        try { return com.bloxarena.kit.KitType.valueOf(name); }
        catch (IllegalArgumentException e) { return null; }
    }

    private void endMatch(TeamColor winner, WinCondition condition) {
        // StatsManager に勝敗を記録
        StatsManager sm = plugin.getStatsManager();
        List<UUID> winTeam  = winner == TeamColor.RED ? redTeam  : blueTeam;
        List<UUID> loseTeam = winner == TeamColor.RED ? blueTeam : redTeam;
        if (winner != null) {
            for (UUID uid : winTeam)  sm.addWin(uid);
            for (UUID uid : loseTeam) sm.addLoss(uid);
        }
        sm.save();

        // 試合レポート表示
        showMatchReport(winner);

        // 演出
        Effects.playVictoryEffect(winner, condition, getAllParticipantsAndSpectators(),
                redTeam, blueTeam, currentMap, kills, deaths, plugin);

        // 5秒後にロビーへ
        endingTask = new BukkitRunnable() {
            @Override
            public void run() {
                returnAllToLobby();
                if (plugin.getLobbyManager().isContinuousMode()) {
                    plugin.getLobbyManager().startContinuousCountdown();
                }
            }
        }.runTaskLater(plugin, 100L); // 5秒
    }

    private void showMatchReport(TeamColor winner) {
        List<UUID> all = getAllParticipantsAndSpectators();
        String header = "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━";
        String winStr = winner != null ? winner.getColorCode() + winner.getDisplayName() + "チーム勝利！" : "引き分け";

        UUID mvpUid     = matchStats.getMVP();
        UUID mostDmgUid = matchStats.getMostDamage();

        String mvpName  = mvpUid     != null ? Bukkit.getOfflinePlayer(mvpUid).getName() : "なし";
        String dmgName  = mostDmgUid != null ? Bukkit.getOfflinePlayer(mostDmgUid).getName() : "なし";
        int    mvpKills = mvpUid     != null ? matchStats.getKills(mvpUid) : 0;
        double maxDmg   = mostDmgUid != null ? matchStats.getDamage(mostDmgUid) : 0;

        for (UUID uid : all) {
            Player p = Bukkit.getPlayer(uid);
            if (p == null) continue;
            p.sendMessage(header);
            p.sendMessage("§6§l        試合結果レポート");
            p.sendMessage("§7結果: " + winStr);
            p.sendMessage("§7MVP: §e" + mvpName + " §7(" + mvpKills + " kill)");
            p.sendMessage("§7最多ダメージ: §e" + dmgName + String.format(" §7(%.1f dmg)", maxDmg));
            p.sendMessage(header);
            // 個人成績
            int myKills  = matchStats.getKills(uid);
            double myDmg = matchStats.getDamage(uid);
            p.sendMessage(String.format("§7あなたの成績: §fKill §e%d §f/ DMG §e%.1f", myKills, myDmg));
            p.sendMessage(header);
        }
    }

    public void returnAllToLobby() {
        // コンクリートを白に戻す
        resetCenterBlocks();

        // 設置ブロックのクリーンアップ（ビーコン・旗・TNT）
        if (currentMap != null && currentMap.getWorld() != null) {
            World w = Bukkit.getWorld(currentMap.getWorld());
            if (w != null) {
                // Domination beacons
                for (MapConfig.DomPoint dp : currentMap.getDominationPoints()) {
                    if (dp.getCenter().getBlock().getType() == Material.BEACON) {
                        dp.getCenter().getBlock().setType(Material.AIR);
                    }
                }
                // CTF banners
                if (currentMap.getRedFlagLocation() != null
                        && currentMap.getRedFlagLocation().getBlock().getType() == Material.RED_BANNER) {
                    currentMap.getRedFlagLocation().getBlock().setType(Material.AIR);
                }
                if (currentMap.getBlueFlagLocation() != null) {
                    Material m = currentMap.getBlueFlagLocation().getBlock().getType();
                    if (m == Material.CYAN_BANNER || m == Material.BLUE_BANNER) {
                        currentMap.getBlueFlagLocation().getBlock().setType(Material.AIR);
                    }
                }
                // Bomb TNT
                if (bombLoc != null && bombLoc.getBlock().getType() == Material.TNT) {
                    bombLoc.getBlock().setType(Material.AIR);
                }
            }
        }

        Location lobbySpawn = plugin.getLobbyManager().getLobbySpawn();

        for (UUID uid : getAllParticipantsAndSpectators()) {
            Player p = Bukkit.getPlayer(uid);
            if (p == null) continue;
            fullyRestorePlayer(p, lobbySpawn);
        }

        for (UUID uid : new HashSet<>(spectators)) {
            Player p = Bukkit.getPlayer(uid);
            if (p == null) continue;
            fullyRestorePlayer(p, lobbySpawn);
        }

        reset();
        plugin.getLobbyManager().onGameEnd();
    }

    /** プレイヤーをロビーに完全復帰させる（状態を全リセット） */
    private void fullyRestorePlayer(Player p, Location lobby) {
        p.setGameMode(org.bukkit.GameMode.ADVENTURE);
        p.teleport(lobby);
        p.getInventory().clear();

        // Restore max HP
        plugin.getSkillManager().restoreMaxHp(p);
        p.setInvulnerable(false);

        // ─ 体力・食料 ─
        var maxHpAttr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
        p.setHealth(maxHp);
        p.setFoodLevel(20);
        p.setSaturation(5f);
        p.setExhaustion(0f);

        // ─ ポーション効果をすべて除去 ─
        for (var effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }

        // ─ 炎・矢 ─
        p.setFireTicks(0);
        p.setArrowsInBody(0);

        // ─ 移動速度をデフォルトに戻す ─
        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.1f);

        // ─ エフェクトリセット（タイトル等） ─
        p.resetTitle();
    }

    private void reset() {
        plugin.getSkillManager().resetAll();
        currentGameMode = GameMode.BATTLE_ARENA;
        state = GameState.WAITING;
        redTeam.clear();
        blueTeam.clear();
        spectators.clear();
        kills.clear();
        deaths.clear();
        selectedKits.clear();
        playerKit.clear();
        noFallDamage.clear();
        deadPlayers.clear();
        currentMap = null;
        matchStats = new MatchStats();
        currentRound  = 0;
        roundWinsRed  = 0;
        roundWinsBlue = 0;
        plugin.getBotManager().clearAll();
        if (endingTask != null) { endingTask.cancel(); endingTask = null; }
        if (tdmTimerTask != null) { tdmTimerTask.cancel(); tdmTimerTask = null; }
        tdmKillsRed = 0;
        tdmKillsBlue = 0;
        if (domTimerTask != null) { domTimerTask.cancel(); domTimerTask = null; }
        domPointsRed = 0;
        domPointsBlue = 0;
        redFlagTaken = false; blueFlagTaken = false;
        redFlagCarrier = null; blueFlagCarrier = null;
        bombCleanup();
        bombRoundAttackerRed = true;
        cancelHoldTimer(); // ホールドタイマーが残っていればキャンセル
    }

    /** 試合中退出→復帰したプレイヤーをspectatorから参加者リストに戻す */
    public void rejoinPlayer(Player p) {
        spectators.remove(p.getUniqueId());
        // kills/deaths を維持したまま復帰
        kills.putIfAbsent(p.getUniqueId(), 0);
        deaths.putIfAbsent(p.getUniqueId(), 0);
    }

    public void forceStop() {
        if (state == GameState.WAITING) return;
        if (endingTask != null) { endingTask.cancel(); endingTask = null; }
        endMatch(null, WinCondition.ELIMINATION);
    }

    // ─────────────────────────────────────────────
    // 死亡処理
    // ─────────────────────────────────────────────

    public void onPlayerDied(Player victim, Player killer) { onPlayerDied(victim, killer, killer != null ? killer.getUniqueId() : null); }

    /** killerUuid: BOTがkillerの場合はBOTのUUID（killerはnull）、実プレイヤーの場合はkiller.getUniqueId()と同じ */
    public void onPlayerDied(Player victim, Player killer, UUID killerUuid) {
        if (state != GameState.IN_GAME) return;
        // 二重呼び出し防止
        if (deadPlayers.contains(victim.getUniqueId())) return;

        deaths.merge(victim.getUniqueId(), 1, Integer::sum);
        plugin.getStatsManager().addDeath(victim.getUniqueId());
        roundKills.put(victim.getUniqueId(), 0); // 被弾でストリークリセット

        TeamColor vt = getTeamOf(victim);
        String victimColor = vt != null ? vt.getColorCode() : "§f";
        String deathMsg;
        String killerDisplay = null;
        String killerColor = "§f";

        if (killer != null) {
            kills.merge(killer.getUniqueId(), 1, Integer::sum);
            matchStats.addKill(killer.getUniqueId());
            plugin.getStatsManager().addKill(killer.getUniqueId());
            TeamColor kt = getTeamOf(killer);
            killerColor = kt != null ? kt.getColorCode() : "§f";
            killerDisplay = killer.getName();
            deathMsg = "§8☠ " + killerColor + killer.getName() + " §7» " + victimColor + victim.getName();
        } else if (killerUuid != null && plugin.getBotManager().getBotTeam(killerUuid) != null) {
            matchStats.addKill(killerUuid);
            TeamColor kt = plugin.getBotManager().getBotTeam(killerUuid);
            killerColor = kt != null ? kt.getColorCode() : "§f";
            killerDisplay = "[BOT]";
            deathMsg = "§8☠ " + killerColor + "[BOT] §7» " + victimColor + victim.getName();
        } else {
            deathMsg = "§8☠ " + victimColor + victim.getName() + " §7が脱落";
        }

        // ─ チャット死亡ログ（1行のみ）─
        for (UUID uid : getAllParticipantsAndSpectators()) {
            Player pp = Bukkit.getPlayer(uid);
            if (pp != null) pp.sendMessage(deathMsg);
        }

        // ─ 派手演出 ─
        Location deathLoc = victim.getLocation().add(0, 1, 0);
        // 死亡地点に雷エフェクト + 爆発パーティクル
        victim.getWorld().strikeLightningEffect(victim.getLocation());
        victim.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, deathLoc, 4, 0.3, 0.3, 0.3, 0);
        victim.getWorld().spawnParticle(Particle.CRIT, deathLoc, 50, 0.5, 0.5, 0.5, 0.5);
        victim.getWorld().spawnParticle(Particle.CRIT_MAGIC, deathLoc, 30, 0.4, 0.4, 0.4, 0.3);
        victim.getWorld().spawnParticle(Particle.SMOKE_LARGE, deathLoc, 15, 0.2, 0.2, 0.2, 0.05);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.2f, 0.8f);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.4f);

        // 被害者タイトル
        if (killerDisplay != null) {
            victim.sendTitle("§c§l☠  YOU DIED", killerColor + "§l" + killerDisplay + " §7にやられた", 3, 50, 10);
        } else {
            victim.sendTitle("§c§l☠  YOU DIED", "§7脱落", 3, 50, 10);
        }
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1f, 0.5f);

        // キラー演出
        if (killer != null) {
            int rk = roundKills.merge(killer.getUniqueId(), 1, Integer::sum);
            String streakTitle = getStreakTitle(rk);

            if (rk == 1) {
                killer.sendTitle("§6§l⚔ KILL!", "§7" + victimColor + victim.getName() + " §7を倒した", 3, 30, 6);
                killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.6f);
                killer.spawnParticle(Particle.VILLAGER_HAPPY, killer.getLocation().add(0, 2, 0), 15, 0.4, 0.4, 0.4, 0.1);
            } else {
                // ストリーク: ド派手
                killer.sendTitle(streakTitle, "§e" + rk + " kills §7in a row!", 3, 45, 10);
                killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f + (rk - 2) * 0.15f);
                killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.8f);
                killer.spawnParticle(Particle.TOTEM, killer.getLocation().add(0, 1, 0), 60, 0.5, 1.0, 0.5, 0.5);
                killer.spawnParticle(Particle.CRIT, killer.getLocation().add(0, 1, 0), 40, 0.4, 0.4, 0.4, 0.4);

                // ストリーク3以上: 全員に花火+アナウンス
                if (rk >= 3) {
                    for (UUID uid : getAllParticipantsAndSpectators()) {
                        Player pp = Bukkit.getPlayer(uid);
                        if (pp != null) {
                            pp.sendMessage("§6§l★ " + killer.getName() + " §r" + streakTitle + " §8(" + rk + " kills)");
                            pp.playSound(pp.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.4f);
                        }
                    }
                    // キラー頭上に花火
                    spawnKillStreakFirework(killer, rk);
                }
            }
            // Special kill announcer for big plays
            if (rk >= 3) {
                announceBigPlay(killer, rk);
            }
        }

        // TDM/Domination/CTF: リスポーン + キルカウント
        if (currentGameMode == GameMode.TEAM_DEATHMATCH
                || currentGameMode == GameMode.DOMINATION
                || currentGameMode == GameMode.CAPTURE_THE_FLAG) {
            // Clear placements on death
            plugin.getSkillManager().clearPlayerPlacements(victim.getUniqueId());
            // TDMのみキルカウントと目標チェック
            if (currentGameMode == GameMode.TEAM_DEATHMATCH) {
                TeamColor kt2 = killer != null ? getTeamOf(killer) : null;
                if (killerUuid != null && plugin.getBotManager().getBotTeam(killerUuid) != null) {
                    kt2 = plugin.getBotManager().getBotTeam(killerUuid);
                }
                if (kt2 == TeamColor.RED) tdmKillsRed++;
                else if (kt2 == TeamColor.BLUE) tdmKillsBlue++;
                int target = plugin.getConfig().getInt("team_deathmatch.target_kills", 30);
                if (tdmKillsRed >= target) { endGame(TeamColor.RED, WinCondition.ELIMINATION); return; }
                if (tdmKillsBlue >= target) { endGame(TeamColor.BLUE, WinCondition.ELIMINATION); return; }
            }
            // 即スペクテーター化 → 3秒後にリスポーン
            Player finalV = victim;
            TeamColor vTeam = getTeamOf(finalV);
            addSpectator(finalV);  // immediately spectate at death location
            finalV.sendMessage("§7リスポーンまで §e3§7秒...");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (finalV.isOnline() && state == GameState.IN_GAME) {
                    roundRestorePlayer(finalV);
                    finalV.setGameMode(org.bukkit.GameMode.SURVIVAL);
                    try { finalV.spigot().respawn(); } catch (Exception ignored) {}
                    KitType kt2 = getPlayerKits().get(finalV.getUniqueId());
                    if (kt2 != null && vTeam != null) {
                        com.bloxarena.kit.KitBuilder.giveKit(finalV, kt2, vTeam, plugin);
                    }
                    if (vTeam != null && currentMap != null) teleportToSpawnZonePublic(finalV, currentMap, vTeam);
                    plugin.getSkillManager().refreshBurst(finalV);
                    finalV.sendMessage("§aリスポーン！");
                }
            }, 60L);  // 3 second respawn delay
        } else {
            deadPlayers.add(victim.getUniqueId());
            if (currentGameMode == GameMode.BATTLE_ARENA || currentGameMode == GameMode.BOMB_MISSION) {
                plugin.getSkillManager().clearPlayerPlacements(victim.getUniqueId());
            }
            final Player finalVictim = victim;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (deadPlayers.contains(finalVictim.getUniqueId())) {
                    addSpectator(finalVictim);
                }
            }, 1L);
        }

        // CTF: drop flag
        if (currentGameMode == GameMode.CAPTURE_THE_FLAG) {
            dropFlag(victim.getUniqueId());
        }

        // Bomb Mission: cancel defuse if defuser dies
        if (currentGameMode == GameMode.BOMB_MISSION && bombDefusing && victim.equals(bombDefuser)) {
            bombDefusing = false; bombDefuser = null;
            Bukkit.broadcastMessage("§c解除が中断されました！（解除者が死亡）");
        }

        if (currentGameMode != GameMode.BOMB_MISSION || !bombPlanted) {
            if (currentGameMode != GameMode.DOMINATION && currentGameMode != GameMode.CAPTURE_THE_FLAG) {
                Bukkit.getScheduler().runTaskLater(plugin, this::checkEliminationWin, 1L);
            }
        }
    }

    private String getStreakTitle(int kills) {
        return switch (kills) {
            case 2  -> "§e§lDOUBLE KILL!!";
            case 3  -> "§6§l🔥 TRIPLE KILL!";
            case 4  -> "§c§l⚡ QUADRA KILL!";
            case 5  -> "§4§l★ PENTA KILL ★";
            default -> "§4§l💀 RAMPAGE 💀";
        };
    }

    private void spawnKillStreakFirework(Player killer, int streak) {
        org.bukkit.Color color = switch (streak) {
            case 3  -> org.bukkit.Color.YELLOW;
            case 4  -> org.bukkit.Color.ORANGE;
            case 5  -> org.bukkit.Color.RED;
            default -> org.bukkit.Color.fromRGB(148, 0, 211); // 紫
        };
        Location loc = killer.getLocation().add(0, 2, 0);
        int count = Math.min(streak - 2, 4); // 最大4発
        for (int i = 0; i < count; i++) {
            final int delay = i * 3;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!killer.isOnline()) return;
                org.bukkit.entity.Firework fw = (org.bukkit.entity.Firework)
                    killer.getWorld().spawnEntity(
                        loc.clone().add((Math.random()-0.5)*2, 0, (Math.random()-0.5)*2),
                        org.bukkit.entity.EntityType.FIREWORK);
                org.bukkit.FireworkEffect effect = org.bukkit.FireworkEffect.builder()
                    .with(org.bukkit.FireworkEffect.Type.STAR)
                    .withColor(color, org.bukkit.Color.WHITE)
                    .withFade(org.bukkit.Color.YELLOW)
                    .withFlicker().withTrail().build();
                org.bukkit.inventory.meta.FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(effect);
                meta.setPower(0);
                fw.setFireworkMeta(meta);
                fw.detonate();
            }, delay);
        }
    }

    /**
     * BOT死亡時に呼ばれる。
     * killer は null の場合あり（落下死など）。
     */
    public void onBotDied(UUID botUuid, Player killer) {
        if (state != GameState.IN_GAME) return;
        if (killer != null) {
            kills.merge(killer.getUniqueId(), 1, Integer::sum);
            matchStats.addKill(killer.getUniqueId());
            plugin.getStatsManager().addKill(killer.getUniqueId());
            TeamColor bt = plugin.getBotManager().getBotTeam(botUuid);
            TeamColor kt = getTeamOf(killer);
            String botColor = bt != null ? bt.getColorCode() : "§f";
            String killerColor = kt != null ? kt.getColorCode() : "§f";
            String msg = botColor + "[BOT] §7が " + killerColor + killer.getName() + " §7にやられた！";
            for (UUID uid : getAllParticipantsAndSpectators()) {
                Player p = Bukkit.getPlayer(uid); if (p != null) p.sendMessage(msg);
            }
        }
        // 消去・チームから削除はBotManager側で行い、ここでは勝利チェックのみ
        Bukkit.getScheduler().runTaskLater(plugin, this::checkEliminationWin, 1L);
    }

        // ─────────────────────────────────────────────
    // キット選択
    // ─────────────────────────────────────────────

    /** チーム内で既にそのキットが選択されているか */
    public boolean isKitTakenInTeam(UUID playerUid, String kitName) {
        List<UUID> team = getTeamOf(playerUid);
        for (UUID uid : team) {
            if (kitName.equals(playerKit.get(uid))) return true;
        }
        return false;
    }

    public void setPlayerKit(UUID uid, String kitName) {
        playerKit.put(uid, kitName);
    }

    public String getPlayerKit(UUID uid) {
        return playerKit.get(uid);
    }

    // ─────────────────────────────────────────────
    // 爆破ミッション
    // ─────────────────────────────────────────────

    private void broadcastBombRoundInfo() {
        TeamColor attacker = bombRoundAttackerRed ? TeamColor.RED : TeamColor.BLUE;
        for (UUID uid : getAllParticipantsAndSpectators()) {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) {
                String role = (getTeamOf(pl) == attacker) ? "§c攻撃側: 爆弾を設置せよ！" : "§9守備側: 設置を阻止せよ！";
                pl.sendTitle(attacker.getColorCode() + "ラウンド " + currentRound, role, 5, 50, 10);
            }
        }
    }

    public void tryPlantBomb(Player p) {
        if (currentGameMode != GameMode.BOMB_MISSION) return;
        if (state != GameState.IN_GAME) return;
        if (bombPlanted) return;
        if (currentMap == null || currentMap.getBombSite() == null) return;
        TeamColor attacker = bombRoundAttackerRed ? TeamColor.RED : TeamColor.BLUE;
        if (getTeamOf(p) != attacker) { p.sendMessage("§cあなたは攻撃側ではありません。"); return; }
        if (p.getLocation().distance(currentMap.getBombSite()) > 3) {
            if (p.getLocation().distance(currentMap.getBombSite()) < 10) p.sendMessage("§c爆弾設置地点に近づいてください。");
            return;
        }

        int plantTime = plugin.getConfig().getInt("bomb_mission.plant_time_seconds", 5);
        p.sendMessage("§c§l爆弾設置中... §e" + plantTime + "秒");
        bombPlanted = true;
        bombLoc = currentMap.getBombSite().clone();
        new BukkitRunnable() {
            int progress = plantTime;
            @Override public void run() {
                if (!p.isOnline() || state != GameState.IN_GAME || p.getLocation().distance(bombLoc) > 3) {
                    p.sendMessage("§c設置が中断されました！");
                    bombPlanted = false;
                    bombLoc = null;
                    cancel();
                    return;
                }
                p.sendActionBar(net.kyori.adventure.text.Component.text("§c§l設置中... §e" + progress + "秒"));
                p.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, p.getLocation().add(0, 1.5, 0), 3, 0.3, 0.3, 0.3, 0);
                progress--;
                if (progress <= 0) {
                    cancel();
                    bombArmed(p);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void bombArmed(Player planter) {
        int fuse = plugin.getConfig().getInt("bomb_mission.bomb_fuse_seconds", 45);
        bombSecondsRemaining = fuse;
        Bukkit.broadcastMessage("§c§l💣 爆弾が設置されました！ §e" + fuse + "秒で爆発！");
        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 0.6f);
        }
        if (bombLoc != null && bombLoc.getWorld() != null) {
            bombLoc.getBlock().setType(Material.TNT);
        }

        bombTimerTask = new BukkitRunnable() {
            @Override public void run() {
                if (state != GameState.IN_GAME) { cancel(); return; }
                // Elimination check while bomb is ticking
                TeamColor attacker = bombRoundAttackerRed ? TeamColor.RED : TeamColor.BLUE;
                TeamColor defender = bombRoundAttackerRed ? TeamColor.BLUE : TeamColor.RED;
                if (getAliveCount(defender) == 0) {
                    cancel();
                    bombCleanup();
                    Bukkit.broadcastMessage("§c§l💥 防衛側全滅！ " + attacker.getDisplayName() + "チームの勝利！");
                    endGame(attacker, WinCondition.OBJECTIVE);
                    return;
                }
                bombSecondsRemaining--;
                if (bombSecondsRemaining <= 10 && bombSecondsRemaining > 0) {
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        if (isParticipant(pl) || isSpectator(pl))
                            pl.sendActionBar(net.kyori.adventure.text.Component.text(
                                "§c💣 爆発まで §e" + bombSecondsRemaining + "秒"));
                        pl.playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.8f);
                    }
                }
                if (bombSecondsRemaining <= 0) {
                    cancel();
                    bombExplode();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void bombExplode() {
        if (bombLoc != null && bombLoc.getWorld() != null) {
            bombLoc.getWorld().createExplosion(bombLoc, 8f, false, true);
            bombLoc.getBlock().setType(Material.AIR);
        }
        bombCleanup();
        TeamColor attacker = bombRoundAttackerRed ? TeamColor.RED : TeamColor.BLUE;
        Bukkit.broadcastMessage("§c§l💥 爆弾が爆発！ " + attacker.getDisplayName() + "チームの勝利！");
        endGame(attacker, WinCondition.OBJECTIVE);
    }

    public void tryDefuseBomb(Player p) {
        if (currentGameMode != GameMode.BOMB_MISSION) return;
        if (state != GameState.IN_GAME) return;
        if (!bombPlanted || bombLoc == null) return;
        TeamColor defender = bombRoundAttackerRed ? TeamColor.BLUE : TeamColor.RED;
        if (getTeamOf(p) != defender) { p.sendMessage("§cあなたは守備側ではありません。"); return; }
        if (p.getLocation().distance(bombLoc) > 3) {
            if (p.getLocation().distance(bombLoc) < 10) p.sendMessage("§c爆弾に近づいてください。");
            return;
        }

        int defuseTime = plugin.getConfig().getInt("bomb_mission.defuse_time_seconds", 7);
        bombDefusing = true;
        bombDefuser = p;
        p.sendMessage("§a§l解除中... §e" + defuseTime + "秒");
        new BukkitRunnable() {
            int progress = defuseTime;
            @Override public void run() {
                if (!p.isOnline() || state != GameState.IN_GAME || p.getLocation().distance(bombLoc) > 3) {
                    p.sendMessage("§c解除が中断されました！");
                    bombDefusing = false; bombDefuser = null;
                    cancel();
                    return;
                }
                p.sendActionBar(net.kyori.adventure.text.Component.text("§a§l解除中... §e" + progress + "秒"));
                progress--;
                if (progress <= 0) {
                    cancel();
                    bombDefused(p);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void bombDefused(Player defuser) {
        TeamColor defender = bombRoundAttackerRed ? TeamColor.BLUE : TeamColor.RED;
        Bukkit.broadcastMessage("§a§l🛡 爆弾が解除されました！ " + defender.getDisplayName() + "チームの勝利！");
        if (bombLoc != null && bombLoc.getWorld() != null) {
            bombLoc.getBlock().setType(Material.AIR);
        }
        bombCleanup();
        endGame(defender, WinCondition.OBJECTIVE);
    }

    private void bombCleanup() {
        bombPlanted = false;
        bombLoc = null;
        bombDefusing = false;
        bombDefuser = null;
        if (bombTimerTask != null) { bombTimerTask.cancel(); bombTimerTask = null; }
    }

    public boolean isBombPlanted() { return bombPlanted; }
    public Location getBombLoc() { return bombLoc; }

    // ─────────────────────────────────────────────
    // ユーティリティ
    // ─────────────────────────────────────────────

    public TeamColor getTeamOf(Player p) {
        UUID uid = p.getUniqueId();
        if (redTeam.contains(uid)) return TeamColor.RED;
        if (blueTeam.contains(uid)) return TeamColor.BLUE;
        return null;
    }


    /** getTeamOf(UUID) の別名 (スコアボード・リスナーから参照) */
    public TeamColor getTeam(UUID uid) {
        if (redTeam.contains(uid)) return TeamColor.RED;
        if (blueTeam.contains(uid)) return TeamColor.BLUE;
        return null;
    }

    public List<UUID> getTeamOf(UUID uid) {
        if (redTeam.contains(uid)) return redTeam;
        if (blueTeam.contains(uid)) return blueTeam;
        return Collections.emptyList();
    }

    public boolean isParticipant(Player p) {
        UUID uid = p.getUniqueId();
        return redTeam.contains(uid) || blueTeam.contains(uid);
    }

    public boolean isSpectator(Player p) {
        return spectators.contains(p.getUniqueId());
    }

    public void addSpectator(Player p) {
        spectators.add(p.getUniqueId());
        p.setGameMode(org.bukkit.GameMode.SPECTATOR);
        // 試合中ならアリーナ中心にTP
        if (currentMap != null && currentMap.getCenter() != null) {
            p.teleport(currentMap.getCenter().clone().add(0, 3, 0));
        }
    }

    public boolean isInCenterZone(Block block) {
        if (currentMap == null) return false;
        Location center = currentMap.getCenter();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int bx = block.getX(), by = block.getY(), bz = block.getZ();
        // cy = 白コンクリートが置かれるレイヤー = 設置・破壊対象 (5x5)
        return by == cy
            && bx >= cx - 2 && bx <= cx + 2
            && bz >= cz - 2 && bz <= cz + 2;
    }

    public boolean hasNoFallDamage(Player p) {
        return noFallDamage.remove(p.getUniqueId()); // 消費型
    }

    private List<UUID> getAllParticipants() {
        List<UUID> all = new ArrayList<>(redTeam);
        all.addAll(blueTeam);
        return all;
    }

    private List<UUID> getAllParticipantsAndSpectators() {
        List<UUID> all = getAllParticipants();
        all.addAll(spectators);
        return all;
    }

    private void broadcastTitle(String title, String sub, int fadeIn, int stay, int fadeOut) {
        for (UUID uid : getAllParticipantsAndSpectators()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) p.sendTitle(title, sub, fadeIn, stay, fadeOut);
        }
    }

    private void broadcastSound(Sound sound, float volume, float pitch) {
        for (UUID uid : getAllParticipantsAndSpectators()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }

    private void broadcastActionBar(String msg) {
        for (UUID uid : getAllParticipantsAndSpectators()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) p.sendActionBar(net.kyori.adventure.text.Component.text(msg));
        }
    }

    private String formatTime(long seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    // ─────────────────────────────────────────────
    // CTF
    // ─────────────────────────────────────────────

    private void updateCTF() {
        if (!redFlagTaken && redFlagDropTime > 0 && System.currentTimeMillis() - redFlagDropTime > 30_000) {
            resetRedFlag();
            Bukkit.broadcastMessage("§c赤の旗が自動回収されました。");
        }
        if (!blueFlagTaken && blueFlagDropTime > 0 && System.currentTimeMillis() - blueFlagDropTime > 30_000) {
            resetBlueFlag();
            Bukkit.broadcastMessage("§9青の旗が自動回収されました。");
        }
        if (redFlagCarrier != null) {
            Player carrier = Bukkit.getPlayer(redFlagCarrier);
            if (carrier != null && carrier.isOnline()) {
                carrier.getWorld().spawnParticle(Particle.REDSTONE, carrier.getLocation().add(0, 2.5, 0), 5, 0.3, 0.5, 0.3, new org.bukkit.Particle.DustOptions(org.bukkit.Color.BLUE, 1.5f));
                carrier.sendActionBar(net.kyori.adventure.text.Component.text("§9\uD83C\uDFF4 青の旗を持っています！自陣に持ち帰れ！"));
            }
        }
        if (blueFlagCarrier != null) {
            Player carrier = Bukkit.getPlayer(blueFlagCarrier);
            if (carrier != null && carrier.isOnline()) {
                carrier.getWorld().spawnParticle(Particle.REDSTONE, carrier.getLocation().add(0, 2.5, 0), 5, 0.3, 0.5, 0.3, new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
                carrier.sendActionBar(net.kyori.adventure.text.Component.text("§c\uD83C\uDFF4 赤の旗を持っています！自陣に持ち帰れ！"));
            }
        }
        for (UUID uid : getAllParticipantsAndSpectators()) {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) pl.sendActionBar(net.kyori.adventure.text.Component.text(
                "§c\uD83D\uDEA9 CTF §7| §c赤:" + ctfRedCaptures + " §9青:" + ctfBlueCaptures));
        }
    }

    public void tryPickupFlag(Player p) {
        if (currentGameMode != GameMode.CAPTURE_THE_FLAG) return;
        if (state != GameState.IN_GAME) return;
        if (System.currentTimeMillis() - inGameStartTime < 10_000) return;
        if (currentMap == null) return;
        TeamColor team = getTeamOf(p);
        if (team == null) return;

        Long pickupCd = ctfPickupCooldown.get(p.getUniqueId());
        if (pickupCd != null && System.currentTimeMillis() - pickupCd < 5_000) return;

        if (team == TeamColor.RED && !blueFlagTaken && currentMap.getBlueFlagLocation() != null) {
            if (p.getLocation().distance(currentMap.getBlueFlagLocation()) < 2) {
                blueFlagTaken = true;
                blueFlagCarrier = p.getUniqueId();
                blueFlagDropTime = -1;
                if (currentMap.getBlueFlagLocation().getBlock().getType() == Material.CYAN_BANNER) {
                    currentMap.getBlueFlagLocation().getBlock().setType(Material.AIR);
                }
                p.sendMessage("§9\uD83C\uDFF4 青の旗を奪取！自陣に持ち帰れ！");
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
            }
        }
        if (team == TeamColor.BLUE && !redFlagTaken && currentMap.getRedFlagLocation() != null) {
            if (p.getLocation().distance(currentMap.getRedFlagLocation()) < 2) {
                redFlagTaken = true;
                redFlagCarrier = p.getUniqueId();
                redFlagDropTime = -1;
                if (currentMap.getRedFlagLocation().getBlock().getType() == Material.RED_BANNER) {
                    currentMap.getRedFlagLocation().getBlock().setType(Material.AIR);
                }
                p.sendMessage("§c\uD83C\uDFF4 赤の旗を奪取！自陣に持ち帰れ！");
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
            }
        }

        if (team == TeamColor.RED && blueFlagCarrier != null && blueFlagCarrier.equals(p.getUniqueId())
                && currentMap.getRedReturnLocation() != null
                && p.getLocation().distance(currentMap.getRedReturnLocation()) < 3) {
            captureFlag(TeamColor.RED);
            return;
        }
        if (team == TeamColor.BLUE && redFlagCarrier != null && redFlagCarrier.equals(p.getUniqueId())
                && currentMap.getBlueReturnLocation() != null
                && p.getLocation().distance(currentMap.getBlueReturnLocation()) < 3) {
            captureFlag(TeamColor.BLUE);
            return;
        }
    }

    public void tryPickupDroppedFlag(Player p) {
        if (currentGameMode != GameMode.CAPTURE_THE_FLAG) return;
        if (state != GameState.IN_GAME) return;
        TeamColor team = getTeamOf(p);
        if (team == null) return;
        Location loc = p.getLocation();

        if (team == TeamColor.RED && !redFlagTaken && redFlagDropTime > 0 && currentMap != null
                && currentMap.getRedFlagLocation() != null && loc.distance(currentMap.getRedFlagLocation()) < 2) {
            resetRedFlag();
            p.sendMessage("§c赤の旗を回収しました。");
        }
        if (team == TeamColor.BLUE && !blueFlagTaken && blueFlagDropTime > 0 && currentMap != null
                && currentMap.getBlueFlagLocation() != null && loc.distance(currentMap.getBlueFlagLocation()) < 2) {
            resetBlueFlag();
            p.sendMessage("§9青の旗を回収しました。");
        }
    }

    private void captureFlag(TeamColor team) {
        if (team == TeamColor.RED) {
            ctfRedCaptures++;
            resetRedFlag();
            if (redFlagCarrier != null) ctfPickupCooldown.put(redFlagCarrier, System.currentTimeMillis());
            Bukkit.broadcastMessage("§c§l🚩 赤チームが旗を奪取！ §8(" + ctfRedCaptures + "/" + plugin.getConfig().getInt("capture_the_flag.captures_to_win", 3) + ")");
        } else {
            ctfBlueCaptures++;
            resetBlueFlag();
            if (blueFlagCarrier != null) ctfPickupCooldown.put(blueFlagCarrier, System.currentTimeMillis());
            Bukkit.broadcastMessage("§9§l🚩 青チームが旗を奪取！ §8(" + ctfBlueCaptures + "/" + plugin.getConfig().getInt("capture_the_flag.captures_to_win", 3) + ")");
        }
        int toWin = plugin.getConfig().getInt("capture_the_flag.captures_to_win", 3);
        if (ctfRedCaptures >= toWin) { endGame(TeamColor.RED, WinCondition.OBJECTIVE); }
        else if (ctfBlueCaptures >= toWin) { endGame(TeamColor.BLUE, WinCondition.OBJECTIVE); }
        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(pl.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }
    }

    private void dropFlag(UUID carrierUuid) {
        if (carrierUuid.equals(redFlagCarrier)) {
            redFlagCarrier = null;
            redFlagTaken = false;
            redFlagDropTime = System.currentTimeMillis();
            Player p = Bukkit.getPlayer(carrierUuid);
            if (p != null && currentMap != null) {
                Location dropLoc = findAirAbove(p.getLocation());
                currentMap.setRedFlagLocation(dropLoc);
                dropLoc.getBlock().setType(Material.RED_BANNER);
                p.sendMessage("§c赤の旗を落としました！");
            }
        }
        if (carrierUuid.equals(blueFlagCarrier)) {
            blueFlagCarrier = null;
            blueFlagTaken = false;
            blueFlagDropTime = System.currentTimeMillis();
            Player p = Bukkit.getPlayer(carrierUuid);
            if (p != null && currentMap != null) {
                Location dropLoc = findAirAbove(p.getLocation());
                currentMap.setBlueFlagLocation(dropLoc);
                dropLoc.getBlock().setType(Material.CYAN_BANNER);
                p.sendMessage("§9青の旗を落としました！");
            }
        }
    }

    private Location findAirAbove(Location base) {
        Location loc = base.clone().add(0, 1, 0);
        while (loc.getBlock().getType().isSolid() && loc.getY() < loc.getWorld().getMaxHeight() - 1) {
            loc = loc.add(0, 1, 0);
        }
        return loc;
    }

    private void resetRedFlag() {
        redFlagTaken = false;
        redFlagCarrier = null;
        redFlagDropTime = -1;
        if (currentMap != null && currentMap.getRedFlagLocation() != null) {
            currentMap.getRedFlagLocation().getBlock().setType(Material.RED_BANNER);
        }
    }

    private void resetBlueFlag() {
        blueFlagTaken = false;
        blueFlagCarrier = null;
        blueFlagDropTime = -1;
        if (currentMap != null && currentMap.getBlueFlagLocation() != null) {
            currentMap.getBlueFlagLocation().getBlock().setType(Material.CYAN_BANNER);
            if (currentMap.getBlueFlagLocation().getBlock().getType() == Material.AIR) {
                currentMap.getBlueFlagLocation().getBlock().setType(Material.CYAN_BANNER);
            }
        }
    }

    // ─────────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────────

    public GameState getState() { return state; }
    public MapConfig getCurrentMap() { return currentMap; }
    public int getCenterY() {
        if (currentMap == null || currentMap.getCenter() == null) return 0;
        return currentMap.getCenter().getBlockY();
    }
    public List<UUID> getRedTeam() { return redTeam; }
    public List<UUID> getBlueTeam() { return blueTeam; }
    public Set<UUID> getSpectators() { return spectators; }
    public int getCurrentRound()  { return currentRound; }
    public int getRoundWinsRed()  { return roundWinsRed; }
    public int getRoundWinsBlue() { return roundWinsBlue; }
    public int getWinsToWin()     { return WINS_TO_WIN; }
    public Map<UUID, Integer> getKills() { return kills; }
    public Map<UUID, Integer> getDeaths() { return deaths; }
    public MatchStats getMatchStats() { return matchStats; }

    public GameMode getCurrentGameMode() { return currentGameMode; }
    public int getTdmKillsRed()  { return tdmKillsRed; }
    public int getTdmKillsBlue() { return tdmKillsBlue; }
    public long getTdmStartTime() { return tdmStartTime; }

    public int getDomPointsRed() { return domPointsRed; }
    public int getDomPointsBlue() { return domPointsBlue; }

    public int getCtfRedCaptures() { return ctfRedCaptures; }
    public int getCtfBlueCaptures() { return ctfBlueCaptures; }

    /** スコアボード用: チームの生存人数 (プレイヤー + BOT) */
    public int getAliveCount(TeamColor team) {
        List<UUID> t = team == TeamColor.RED ? redTeam : blueTeam;
        int count = 0;
        for (UUID uid : t) {
            if (plugin.getBotManager().getBotTeam(uid) != null) {
                // BOT: BotManagerで生存チェック
                continue; // aliveCountはBotManager側で加算
            }
            Player p = Bukkit.getPlayer(uid);
            if (!deadPlayers.contains(uid)) count++;
        }
        count += plugin.getBotManager().getAliveBotCount(team);
        return count;
    }

    private void announceBigPlay(Player killer, int streak) {
        if (streak == 5) {
            Bukkit.broadcastMessage("§4§l☠ " + killer.getName() + " §cが PENTA KILL を達成！ §4☠");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.7f, 0.8f);
            }
        } else if (streak >= 3) {
            Bukkit.broadcastMessage("§6§l★ " + killer.getName() + " §eが " + streak + "連続キル！");
        }
    }

    /** スコアボード用: UUID→KitType マップ */
    public Map<UUID, KitType> getPlayerKits() {
        Map<UUID, KitType> result = new java.util.HashMap<>();
        for (var entry : playerKit.entrySet()) {
            try { result.put(entry.getKey(), KitType.valueOf(entry.getValue())); }
            catch (Exception ignored) {}
        }
        return result;
    }
}
