package com.bloxarena.scoreboard;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameManager;
import com.bloxarena.game.GameMode;
import com.bloxarena.game.GameState;
import com.bloxarena.game.TeamColor;
import com.bloxarena.game.WinCondition;
import com.bloxarena.kit.KitType;
import com.bloxarena.map.MapConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardManager {
    private static final int LINE_COUNT = 15;
    private final BloxArenaPlugin plugin;
    private BukkitTask task;
    private BukkitTask logicTask;
    private long startTime;
    private boolean suddenDeathAnnounced = false;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<UUID, Scoreboard>();

    public ScoreboardManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        this.start(this.plugin.getGameManager());
    }

    public void start(GameManager gm) {
        this.startTime = System.currentTimeMillis();
        this.suddenDeathAnnounced = false;
        this.setupPlayerBoards(gm);
        this.logicTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            this.plugin.getSkillManager().update();
            this.plugin.getSkillManager().updateTurrets();
        }, 0L, 20L);
        this.task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, this::update, 0L, 4L);
    }

    private void setupPlayerBoards(GameManager gm) {
        this.playerBoards.clear();
        ArrayList<UUID> allUids = new ArrayList<UUID>();
        allUids.addAll(gm.getRedTeam());
        allUids.addAll(gm.getBlueTeam());
        for (UUID uid : allUids) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Team redTeam = board.registerNewTeam("blox_red");
            redTeam.setColor(ChatColor.RED);
            redTeam.setPrefix("\u00a7c");
            redTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            redTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            Team blueTeam = board.registerNewTeam("blox_blue");
            blueTeam.setColor(ChatColor.AQUA);
            blueTeam.setPrefix("\u00a7b");
            blueTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            blueTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            Team necroRed = board.registerNewTeam("necro_red");
            necroRed.setAllowFriendlyFire(false);
            Team necroBlue = board.registerNewTeam("necro_blue");
            necroBlue.setAllowFriendlyFire(false);
            for (UUID rid : gm.getRedTeam()) {
                Player rp = Bukkit.getPlayer((UUID)rid);
                if (rp == null) continue;
                redTeam.addEntry(rp.getName());
            }
            for (UUID bid : gm.getBlueTeam()) {
                Player bp = Bukkit.getPlayer((UUID)bid);
                if (bp == null) continue;
                blueTeam.addEntry(bp.getName());
            }
            this.prepareBoard(board);
            p.setScoreboard(board);
            this.playerBoards.put(uid, board);
        }
    }

    private void prepareBoard(Scoreboard board) {
        try {
            Objective obj = board.registerNewObjective("bloxarena", Criteria.DUMMY, "\u00a76\u00a7lBAII WoNG");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            for (int i = 0; i < LINE_COUNT; ++i) {
                String entry = ChatColor.values()[i].toString() + ChatColor.RESET;
                Team line = board.registerNewTeam("line" + i);
                line.addEntry(entry);
                obj.getScore(entry).setScore(LINE_COUNT - i);
                line.setPrefix("");
                line.setSuffix("");
            }
        }
        catch (Exception e) {
        }
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        if (this.logicTask != null) {
            this.logicTask.cancel();
            this.logicTask = null;
        }
        this.playerBoards.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.clearBoard(p);
        }
    }

    private void update() {
        GameManager gm = this.plugin.getGameManager();
        if (gm.getState() != GameState.IN_GAME) {
            this.stop();
            return;
        }
        int redAlive = gm.getAliveCount(TeamColor.RED);
        int blueAlive = gm.getAliveCount(TeamColor.BLUE);
        long elapsed = (System.currentTimeMillis() - this.startTime) / 1000L;
        boolean isCTF = gm.getCurrentGameMode() == GameMode.CAPTURE_THE_FLAG;
        if (isCTF && elapsed >= 600L && !this.suddenDeathAnnounced) {
            int blueCaps;
            this.suddenDeathAnnounced = true;
            int redCaps = gm.getCtfRedCaptures();
            if (redCaps > (blueCaps = gm.getCtfBlueCaptures())) {
                Bukkit.broadcastMessage((String)("\u00a7c\u00a7l\u26a1 \u5236\u9650\u6642\u9593\u7d42\u4e86\uff01\u00a7c\u8d64\u30c1\u30fc\u30e0\u306e\u52dd\u5229\uff01 \u00a77(" + redCaps + "-" + blueCaps + ")"));
                gm.endGame(TeamColor.RED, WinCondition.OBJECTIVE);
            } else if (blueCaps > redCaps) {
                Bukkit.broadcastMessage((String)("\u00a79\u00a7l\u26a1 \u5236\u9650\u6642\u9593\u7d42\u4e86\uff01\u00a79\u9752\u30c1\u30fc\u30e0\u306e\u52dd\u5229\uff01 \u00a77(" + redCaps + "-" + blueCaps + ")"));
                gm.endGame(TeamColor.BLUE, WinCondition.OBJECTIVE);
            } else {
                Bukkit.broadcastMessage((String)("\u00a7e\u00a7l\u26a1 \u5236\u9650\u6642\u9593\u7d42\u4e86\uff01\u00a7e\u5f15\u304d\u5206\u3051\uff01 \u00a77(" + redCaps + "-" + blueCaps + ")"));
                gm.endGame(null, WinCondition.OBJECTIVE);
            }
            return;
        }
        if (elapsed >= 360L) {
            if (!this.suddenDeathAnnounced && !isCTF) {
                this.suddenDeathAnnounced = true;
                Bukkit.broadcastMessage((String)"\u00a74\u00a7l\u26a1 \u30b5\u30c9\u30f3\u30c7\u30b9\u7a81\u5165\uff01\u00a7c\u8a66\u5408\u6642\u95936\u5206\u7d4c\u904e\u3002\u5168\u54e1\u306b\u653b\u6483\u529b\u4e0a\u6607255\u4ed8\u4e0e\uff01");
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!gm.isParticipant(p) || gm.isSpectator(p)) continue;
                p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, false, false));
                if (isCTF) continue;
                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, 254, false, false));
            }
        }
        String time = String.format("%02d:%02d", elapsed / 60L, elapsed % 60L);
        int round = gm.getCurrentRound();
        int winsRed = gm.getRoundWinsRed();
        int winsBlue = gm.getRoundWinsBlue();
        int winsToWin = gm.getWinsToWin();
        Map<UUID, KitType> kitMap = gm.getPlayerKits();
        boolean isTDM = gm.getCurrentGameMode() == GameMode.TEAM_DEATHMATCH;
        if (isTDM) {
            redAlive = gm.getTdmKillsRed();
            blueAlive = gm.getTdmKillsBlue();
            long tdmElapsed = (System.currentTimeMillis() - gm.getTdmStartTime()) / 1000L;
            int timeLimit = this.plugin.getConfig().getInt("team_deathmatch.time_limit_seconds", 300);
            time = tdmElapsed + "/" + timeLimit + "s";
        }
        if (gm.getCurrentGameMode() == GameMode.DOMINATION) {
            redAlive = gm.getDomPointsRed();
            blueAlive = gm.getDomPointsBlue();
            time = String.format("%dpts", (System.currentTimeMillis() - this.startTime) / 1000L);
        }
        if (gm.getCurrentGameMode() == GameMode.CAPTURE_THE_FLAG) {
            redAlive = gm.getCtfRedCaptures();
            blueAlive = gm.getCtfBlueCaptures();
        }
        if (gm.getCurrentGameMode() == GameMode.BOMB_MISSION) {
            if (gm.isBombPlanted()) {
                time = "\u00a7c\ud83d\udca3 " + gm.getBombSecondsRemaining() + "s";
            } else {
                int limit = this.plugin.getConfig().getInt("bomb_mission.time_limit_seconds", 180);
                time = "\u23f1 " + (limit - (int)elapsed) + "/" + limit + "s";
            }
        }
        this.plugin.getSkillManager().updateKitActionBars();
        this.applyOutnumberedBuff(gm, redAlive, blueAlive);
        for (Player p : Bukkit.getOnlinePlayers()) {
            KitType kit = kitMap.get(p.getUniqueId());
            TeamColor team = gm.getTeam(p.getUniqueId());
            this.setBoard(p, redAlive, blueAlive, time, kit, team, round, winsRed, winsBlue, winsToWin);
        }
    }

    private void applyOutnumberedBuff(GameManager gm, int redAlive, int blueAlive) {
        GameMode mode = gm.getCurrentGameMode();
        if (mode == GameMode.BATTLE_ARENA || mode == GameMode.BOMB_MISSION) {
            int diff = Math.abs(redAlive - blueAlive);
            if (diff < 1) {
                return;
            }
            TeamColor minTeam = redAlive < blueAlive ? TeamColor.RED : TeamColor.BLUE;
            int amplifier = 0;
            int duration = 600;
            List<UUID> team = minTeam == TeamColor.RED ? gm.getRedTeam() : gm.getBlueTeam();
            for (UUID uid : team) {
                Player p = Bukkit.getPlayer((UUID)uid);
                if (p == null || p.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier, true, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 0, true, false));
            }
            return;
        }
        int redStart = gm.getCtfRedTeamSize();
        int blueStart = gm.getCtfBlueTeamSize();
        if (redStart <= 0 || blueStart <= 0) {
            return;
        }
        int diff = redStart - blueStart;
        if (diff == 0) {
            return;
        }
        TeamColor minTeam = diff > 0 ? TeamColor.BLUE : TeamColor.RED;
        List<UUID> team = minTeam == TeamColor.RED ? gm.getRedTeam() : gm.getBlueTeam();
        for (UUID uid : team) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null || p.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, false));
        }
    }

    private void setBoard(Player p, int red, int blue, String time, KitType kit, TeamColor team, int round, int winsRed, int winsBlue, int winsToWin) {
        Scoreboard board = this.playerBoards.get(p.getUniqueId());
        if (board == null) {
            return;
        }
        Objective obj = board.getObjective("bloxarena");
        if (obj == null) {
            return;
        }
        GameManager gm = this.plugin.getGameManager();
        if (p.getScoreboard() != board) {
            p.setScoreboard(board);
        }
        boolean isTDM = gm.getCurrentGameMode() == GameMode.TEAM_DEATHMATCH;
        boolean isDom = gm.getCurrentGameMode() == GameMode.DOMINATION;
        boolean isCTF = gm.getCurrentGameMode() == GameMode.CAPTURE_THE_FLAG;
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("\u00a76\u00a7l\u00a7m\u2501\u2501\u2501\u2501 BAII WoNG \u2501\u2501\u2501\u2501\u00a7r");
        MapConfig currentMap = gm.getCurrentMap();
        if (currentMap != null) {
            String mapName = currentMap.getDisplayName() != null ? currentMap.getDisplayName() : currentMap.getId();
            lines.add("\u00a7eMAP \u00a77\u00bb \u00a7f" + mapName);
        }
        if (!(isTDM || isCTF)) {
            lines.add("\u00a76\u30e9\u30a6\u30f3\u30c9 \u00a77\u00bb \u00a7f" + round);
        }
        lines.add("\u00a7d" + gm.getCurrentGameMode().getDisplayName());
        if (!(isTDM || isCTF)) {
            lines.add("\u00a7c" + "\u25cf".repeat(winsRed) + "\u00a77" + "\u25cb".repeat(winsToWin - winsRed) + " \u00a77vs \u00a79" + "\u25cf".repeat(winsBlue) + "\u00a77" + "\u25cb".repeat(winsToWin - winsBlue));
        }
        lines.add("\u00a7r");
        String unit = isTDM ? "\u30ad\u30eb" : (isDom ? "pts" : (isCTF ? "\u596a\u53d6" : "\u4eba"));
        lines.add("\u00a7c\u8d64: \u00a7f" + red + unit);
        lines.add("\u00a79\u9752: \u00a7f" + blue + unit);
        lines.add("\u00a7r");
        lines.add("\u00a77\u7d4c\u904e: \u00a7f" + time);
        lines.add("\u00a7r");
        if (kit != null) {
            lines.add("\u00a7e\u30ad\u30c3\u30c8: \u00a7f" + kit.getDisplayName());
        }
        if (team != null) {
            lines.add("\u00a77\u30c1\u30fc\u30e0: " + team.getColorCode() + team.getDisplayName());
        }
        int ultCharge = this.plugin.getUltimateManager().getCharge(p.getUniqueId());
        if (this.plugin.getUltimateManager().canUltimate(p.getUniqueId())) {
            lines.add("\u00a7d\u00a7l\u26a1 ULT READY!");
        } else {
            lines.add("\u00a7dULT \u00a7f[" + ultCharge + "%]");
        }
        lines.add("\u00a77\u00a7m\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u00a7r");
        this.applyLines(board, obj, lines);
    }

    private void applyLines(Scoreboard board, Objective obj, List<String> lines) {
        for (int i = 0; i < LINE_COUNT; ++i) {
            Team line = board.getTeam("line" + i);
            if (line == null) continue;
            String entry = ChatColor.values()[i].toString() + ChatColor.RESET;
            String text = i < lines.size() ? lines.get(i) : "";
            if (text.isEmpty()) {
                line.setPrefix("");
                line.setSuffix("");
                board.resetScores(entry);
                continue;
            }
            try {
                obj.getScore(entry).setScore(LINE_COUNT - i);
            }
            catch (Exception e) {
            }
            this.setLineText(line, text);
        }
    }

    private void setLineText(Team line, String text) {
        if (text.length() <= 16) {
            line.setPrefix(text);
            line.setSuffix("");
            return;
        }
        int cut = 16;
        if (text.charAt(15) == '\u00a7') {
            cut = 15;
        }
        String prefix = text.substring(0, cut);
        String suffix = ChatColor.getLastColors(prefix) + text.substring(cut);
        line.setPrefix(prefix);
        line.setSuffix(suffix);
    }

    private void clearBoard(Player p) {
        try {
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        catch (Exception e) {
        }
    }
}
