package com.bloxarena.scoreboard;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameState;
import com.bloxarena.game.TeamColor;
import com.bloxarena.kit.KitType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 試合中サイドバースコアボード。
 * 毎秒更新: 赤/青残人数、経過時間、自分のキット。
 * バグ修正: 共有ボードではキット名が全員同じになるため、プレイヤーごとに個別ボードを使用。
 */
public class ScoreboardManager {

    private final BloxArenaPlugin plugin;
    private BukkitTask task;
    private long startTime;
    // プレイヤーごとの個別スコアボード (チームカラー + サイドバー)
    private final Map<UUID, org.bukkit.scoreboard.Scoreboard> playerBoards = new HashMap<>();

    public ScoreboardManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() { start(plugin.getGameManager()); }

    public void start(com.bloxarena.game.GameManager gm) {
        startTime = System.currentTimeMillis();
        setupPlayerBoards(gm);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::update, 0L, 20L);
    }

    /** プレイヤーごとに個別スコアボードを作成し、チームカラーを設定 */
    private void setupPlayerBoards(com.bloxarena.game.GameManager gm) {
        playerBoards.clear();

        java.util.List<UUID> allUids = new java.util.ArrayList<>();
        allUids.addAll(gm.getRedTeam());
        allUids.addAll(gm.getBlueTeam());

        for (UUID uid : allUids) {
            Player p = Bukkit.getPlayer(uid);
            if (p == null) continue;
            org.bukkit.scoreboard.Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

            // 赤チーム設定
            Team redTeam = board.registerNewTeam("blox_red");
            redTeam.setColor(org.bukkit.ChatColor.RED);
            redTeam.setPrefix("§c");
            redTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            redTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

            // 青チーム設定
            Team blueTeam = board.registerNewTeam("blox_blue");
            blueTeam.setColor(org.bukkit.ChatColor.AQUA);
            blueTeam.setPrefix("§b");
            blueTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            blueTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

            // 全参加者をチームに登録
            for (UUID rid : gm.getRedTeam()) {
                Player rp = Bukkit.getPlayer(rid);
                if (rp != null) redTeam.addEntry(rp.getName());
            }
            for (UUID bid : gm.getBlueTeam()) {
                Player bp = Bukkit.getPlayer(bid);
                if (bp != null) blueTeam.addEntry(bp.getName());
            }

            p.setScoreboard(board);
            playerBoards.put(uid, board);
        }
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
        playerBoards.clear();
        for (Player p : Bukkit.getOnlinePlayers()) clearBoard(p);
    }

    private void update() {
        var gm = plugin.getGameManager();
        if (gm.getState() != GameState.IN_GAME) { stop(); return; }

        int redAlive   = gm.getAliveCount(TeamColor.RED);
        int blueAlive  = gm.getAliveCount(TeamColor.BLUE);
        long elapsed   = (System.currentTimeMillis() - startTime) / 1000;
        String time    = String.format("%02d:%02d", elapsed / 60, elapsed % 60);
        int round      = gm.getCurrentRound();
        int winsRed    = gm.getRoundWinsRed();
        int winsBlue   = gm.getRoundWinsBlue();
        int winsToWin  = gm.getWinsToWin();

        Map<UUID, KitType> kitMap = gm.getPlayerKits();

        // スキル管理の定期更新（ヴァンパイア・スナイパー・タレット等）
        plugin.getSkillManager().update();
        plugin.getSkillManager().updateTurrets();

        boolean isTDM = gm.getCurrentGameMode() == com.bloxarena.game.GameMode.TEAM_DEATHMATCH;
        if (isTDM) {
            redAlive  = gm.getTdmKillsRed();
            blueAlive = gm.getTdmKillsBlue();
            long tdmElapsed = (System.currentTimeMillis() - gm.getTdmStartTime()) / 1000;
            int timeLimit = plugin.getConfig().getInt("team_deathmatch.time_limit_seconds", 300);
            time = tdmElapsed + "/" + timeLimit + "s";
        }

        if (gm.getCurrentGameMode() == com.bloxarena.game.GameMode.DOMINATION) {
            redAlive = gm.getDomPointsRed();
            blueAlive = gm.getDomPointsBlue();
            time = String.format("%dpts", (System.currentTimeMillis() - startTime) / 1000);
        }

        // 人数差バフ適用
        applyOutnumberedBuff(gm, redAlive, blueAlive);

        for (Player p : Bukkit.getOnlinePlayers()) {
            KitType kit = kitMap.get(p.getUniqueId());
            TeamColor team = gm.getTeam(p.getUniqueId());
            setBoard(p, redAlive, blueAlive, time, kit, team, round, winsRed, winsBlue, winsToWin);
        }

        String modeStr = gm.getCurrentGameMode().getDisplayName();
        for (UUID uid : gm.getRedTeam()) {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) {
                KitType kt = gm.getPlayerKits().get(uid);
                if (kt == KitType.VAMPIRE || kt == KitType.SNIPER || kt == KitType.BOMBER
                        || kt == KitType.SUNDANCE || kt == KitType.GUARDIAN || kt == KitType.TRAPPER
                        || kt == KitType.PHANTOM || kt == KitType.ANCHOR || kt == KitType.ENGINEER
                        || kt == KitType.SCOUT || kt == KitType.COUNTER) continue;
                // Bomb countdown takes priority over generic bar
                if (gm.getCurrentGameMode() == com.bloxarena.game.GameMode.BOMB_MISSION && gm.isBombPlanted()) continue;
                pl.sendActionBar(net.kyori.adventure.text.Component.text(
                    "§c● 赤 §7| §d" + modeStr + " §7| §e" + time + " §7| §cRed:" + redAlive + " §9Blue:" + blueAlive));
            }
        }
        for (UUID uid : gm.getBlueTeam()) {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) {
                KitType kt = gm.getPlayerKits().get(uid);
                if (kt == KitType.VAMPIRE || kt == KitType.SNIPER || kt == KitType.BOMBER
                        || kt == KitType.SUNDANCE || kt == KitType.GUARDIAN || kt == KitType.TRAPPER
                        || kt == KitType.PHANTOM || kt == KitType.ANCHOR || kt == KitType.ENGINEER
                        || kt == KitType.SCOUT || kt == KitType.COUNTER) continue;
                if (gm.getCurrentGameMode() == com.bloxarena.game.GameMode.BOMB_MISSION && gm.isBombPlanted()) continue;
                pl.sendActionBar(net.kyori.adventure.text.Component.text(
                    "§9● 青 §7| §d" + modeStr + " §7| §e" + time + " §7| §cRed:" + redAlive + " §9Blue:" + blueAlive));
            }
        }
    }

    /** 人数差バフ: 少人数チームにスピードI + 体力回復I */
    private void applyOutnumberedBuff(com.bloxarena.game.GameManager gm, int redAlive, int blueAlive) {
        int diff = Math.abs(redAlive - blueAlive);
        if (diff < 1) return;
        com.bloxarena.game.TeamColor minTeam = (redAlive < blueAlive) ? com.bloxarena.game.TeamColor.RED : com.bloxarena.game.TeamColor.BLUE;
        int amplifier = Math.min(diff - 1, 1);
        int duration = 30 * 20;
        java.util.List<UUID> team = minTeam == com.bloxarena.game.TeamColor.RED ? gm.getRedTeam() : gm.getBlueTeam();
        for (UUID uid : team) {
            org.bukkit.entity.Player p = Bukkit.getPlayer(uid);
            if (p == null || p.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED, duration, amplifier, true, false));
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION, duration, 0, true, false));
        }
    }

    private void setBoard(Player p, int red, int blue, String time, KitType kit, TeamColor team,
                          int round, int winsRed, int winsBlue, int winsToWin) {
        // プレイヤーごとの個別ボードを使用（共有ボードだとキット名が全員同じになるバグの修正）
        org.bukkit.scoreboard.Scoreboard board = playerBoards.get(p.getUniqueId());
        if (board == null) return; // 参加者でないプレイヤー（観客等）はスキップ
        if (p.getScoreboard() != board) p.setScoreboard(board);

        Objective obj = board.getObjective("bloxarena");
        if (obj == null) {
            try {
                obj = board.registerNewObjective("bloxarena", Criteria.DUMMY, "§6§lBAII WoNG");
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            } catch (Exception e) { return; }
        }

        clearLines(board);

        boolean isTDM = plugin.getGameManager().getCurrentGameMode() == com.bloxarena.game.GameMode.TEAM_DEATHMATCH;
        boolean isDom = plugin.getGameManager().getCurrentGameMode() == com.bloxarena.game.GameMode.DOMINATION;
        int line = 16;
        score(board, obj, "§r§f ", line--);
        // Map name
        var currentMap = plugin.getGameManager().getCurrentMap();
        if (currentMap != null) {
            String mapName = currentMap.getDisplayName() != null ? currentMap.getDisplayName() : currentMap.getId();
            score(board, obj, "§eMAP: §f" + mapName, line--);
        }
        if (!isTDM) {
            score(board, obj, "§6ラウンド §f" + round, line--);
        }
        score(board, obj, "§d" + plugin.getGameManager().getCurrentGameMode().getDisplayName(), line--);
        if (!isTDM) {
            score(board, obj, "§c●".repeat(winsRed) + "§7○".repeat(winsToWin - winsRed)
                + " §7vs §9" + "●".repeat(winsBlue) + "§7○".repeat(winsToWin - winsBlue), line--);
        }
        score(board, obj, "§r§f  ", line--);
        score(board, obj, "§c赤: §f" + red + (isTDM ? "キル" : isDom ? "pts" : "人"), line--);
        score(board, obj, "§9青: §f" + blue + (isTDM ? "キル" : isDom ? "pts" : "人"), line--);
        score(board, obj, "§r§f   ", line--);
        score(board, obj, "§7経過: §f" + time, line--);
        score(board, obj, "§r§f    ", line--);
        if (kit != null)  score(board, obj, "§eキット: §f" + kit.getDisplayName(), line--);
        if (team != null) score(board, obj, "§7チーム: " + team.getColorCode() + team.getDisplayName(), line--);
        score(board, obj, "§r§f     ", line--);
    }


    private void score(org.bukkit.scoreboard.Scoreboard board, Objective obj, String entry, int value) {
        try {
            Score s = obj.getScore(entry);
            s.setScore(value);
        } catch (Exception ignored) {}
    }

    private void clearLines(org.bukkit.scoreboard.Scoreboard board) {
        Objective obj = board.getObjective("bloxarena");
        if (obj == null) return;
        for (String entry : board.getEntries()) {
            if (board.getEntryTeam(entry) == null) {
                board.resetScores(entry);
            }
        }
    }

    private void clearBoard(Player p) {
        try {
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        } catch (Exception ignored) {}
    }
}
