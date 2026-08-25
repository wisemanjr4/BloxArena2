/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.attribute.AttributeInstance
 *  org.bukkit.boss.BarColor
 *  org.bukkit.boss.BarFlag
 *  org.bukkit.boss.BarStyle
 *  org.bukkit.boss.BossBar
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 */
package com.bloxarena.lobby;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameMode;
import com.bloxarena.game.GameState;
import com.bloxarena.map.MapConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class LobbyManager {
    private final BloxArenaPlugin plugin;
    private World waitingWorld;
    private Location waitingMin;
    private Location waitingMax;
    private Location lobbySpawn;
    private final Set<UUID> waitingPlayers = new LinkedHashSet<UUID>();
    private BukkitTask countdownTask = null;
    private int currentCountdown = -1;
    private boolean continuousMode = false;
    private Location lobbyOobMin = null;
    private Location lobbyOobMax = null;
    private BossBar countdownBossBar = null;
    private static final int THRESHOLD_4 = 4;
    private static final int THRESHOLD_6 = 6;
    private final Map<UUID, Integer> pendingVotes = new HashMap<UUID, Integer>();
    private List<Object[]> voteOptions = new ArrayList<Object[]>();
    private BukkitTask voteTask = null;
    private List<UUID> voteParticipants = new ArrayList<UUID>();

    public LobbyManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        FileConfiguration cfg = this.plugin.getConfig();
        String worldName = cfg.getString("lobby.waiting_area.world", "world");
        this.waitingWorld = Bukkit.getWorld((String)worldName);
        this.waitingMin = this.readLoc(cfg, "lobby.waiting_area.min", worldName);
        this.waitingMax = this.readLoc(cfg, "lobby.waiting_area.max", worldName);
        this.lobbySpawn = this.readLoc(cfg, "lobby.spawn", worldName);
        if (cfg.contains("lobby.oob.min.x")) {
            this.lobbyOobMin = this.readLoc(cfg, "lobby.oob.min", worldName);
            this.lobbyOobMax = this.readLoc(cfg, "lobby.oob.max", worldName);
        }
    }

    private Location readLoc(FileConfiguration cfg, String path, String worldName) {
        World w = Bukkit.getWorld((String)worldName);
        double x = cfg.getDouble(path + ".x", 0.0);
        double y = cfg.getDouble(path + ".y", 64.0);
        double z = cfg.getDouble(path + ".z", 0.0);
        return new Location(w, x, y, z);
    }

    public void onPlayerEnter(Player p) {
        AttributeInstance attr;
        if (this.plugin.getGameManager().getState() != GameState.WAITING) {
            if (!this.plugin.getGameManager().isSpectator(p)) {
                this.plugin.getGameManager().addSpectator(p);
                p.sendMessage("\u00a7e\u8a66\u5408\u304c\u9032\u884c\u4e2d\u3067\u3059\u3002\u89b3\u6226\u30e2\u30fc\u30c9\u3078\u79fb\u884c\u3057\u307e\u3057\u305f\u3002");
            }
            return;
        }
        if (!p.hasPermission("bloxarena.admin")) {
            p.setGameMode(org.bukkit.GameMode.ADVENTURE);
        }
        double maxHp = (attr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH)) != null ? attr.getValue() : 20.0;
        p.setHealth(maxHp);
        p.setFoodLevel(20);
        p.setSaturation(5.0f);
        for (PotionEffect eff : p.getActivePotionEffects()) {
            p.removePotionEffect(eff.getType());
        }
        if (this.waitingPlayers.add(p.getUniqueId())) {
            p.sendMessage("\u00a7a\u5f85\u6a5f\u30a8\u30ea\u30a2\u306b\u53c2\u52a0\u3057\u307e\u3057\u305f\u3002 \u00a77(" + this.waitingPlayers.size() + "\u4eba\u5f85\u6a5f\u4e2d)");
            p.getInventory().setItem(8, this.plugin.getKitInfoGUI().makeGuideItem());
            this.updateCountdown();
        }
    }

    public void onPlayerExit(Player p) {
        if (this.waitingPlayers.remove(p.getUniqueId())) {
            if (this.plugin.getGameManager().getState() == GameState.WAITING) {
                if (!p.hasPermission("bloxarena.admin")) {
                    p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                }
                p.sendMessage("\u00a7c\u5f85\u6a5f\u30a8\u30ea\u30a2\u304b\u3089\u9000\u51fa\u3057\u307e\u3057\u305f\u3002");
            }
            this.updateCountdown();
        }
    }

    public void onPlayerQuit(Player p) {
        this.waitingPlayers.remove(p.getUniqueId());
        this.updateCountdown();
    }

    public boolean isInWaitingArea(Location loc) {
        if (this.waitingMin == null || this.waitingMax == null) {
            return false;
        }
        if (this.waitingMin.getWorld() == null) {
            return false;
        }
        if (loc.getWorld() == null || !this.waitingMin.getWorld().equals((Object)loc.getWorld())) {
            return false;
        }
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return x >= Math.min(this.waitingMin.getX(), this.waitingMax.getX()) && x <= Math.max(this.waitingMin.getX(), this.waitingMax.getX()) && y >= Math.min(this.waitingMin.getY(), this.waitingMax.getY()) && y <= Math.max(this.waitingMin.getY(), this.waitingMax.getY()) && z >= Math.min(this.waitingMin.getZ(), this.waitingMax.getZ()) && z <= Math.max(this.waitingMin.getZ(), this.waitingMax.getZ());
    }

    private void updateCountdown() {
        int count = this.waitingPlayers.size();
        if (count < 2) {
            this.cancelCountdown();
            return;
        }
        int newTime = this.getCountdownTime(count);
        if (this.countdownTask == null) {
            this.startCountdown(newTime);
        } else if (newTime < this.currentCountdown) {
            this.cancelCountdown();
            this.broadcastWaiting("\u00a7e\u4eba\u6570\u5897\u52a0\uff01\u30ab\u30a6\u30f3\u30c8\u30c0\u30a6\u30f3\u3092 \u00a7a" + newTime + "\u79d2 \u00a7e\u306b\u77ed\u7e2e\u3057\u307e\u3059");
            this.startCountdown(newTime);
        }
    }

    private int getCountdownTime(int count) {
        if (count >= 4) {
            return 30;
        }
        return 45;
    }

    private void startCountdown(final int seconds) {
        this.currentCountdown = seconds;
        String timeStr = seconds >= 60 ? seconds / 60 + "\u5206" : seconds + "\u79d2";
        this.broadcastWaiting("\u00a7a\u8a66\u5408\u304c \u00a7e" + timeStr + " \u00a7a\u5f8c\u306b\u958b\u59cb\u3057\u307e\u3059");
        this.countdownTask = new BukkitRunnable(){
            int remaining;
            {
                this.remaining = seconds;
            }

            public void run() {
                if (LobbyManager.this.waitingPlayers.size() < 2) {
                    this.cancel();
                    LobbyManager.this.countdownTask = null;
                    LobbyManager.this.currentCountdown = -1;
                    return;
                }
                LobbyManager.this.currentCountdown = this.remaining;
                if (LobbyManager.this.countdownBossBar != null) {
                    LobbyManager.this.countdownBossBar.setProgress((double)this.remaining / (double)seconds);
                    LobbyManager.this.countdownBossBar.setTitle("\u00a7e\u00a7l\u8a66\u5408\u958b\u59cb\u307e\u3067 \u00a76" + this.remaining + "\u00a7e \u79d2");
                }
                for (UUID uid : LobbyManager.this.waitingPlayers) {
                    Player p = Bukkit.getPlayer((UUID)uid);
                    if (p == null) continue;
                    p.sendActionBar((Component)Component.text((String)("\u00a7e\u8a66\u5408\u958b\u59cb\u307e\u3067: \u00a7a" + this.remaining + "\u79d2")));
                }
                if (this.remaining == 10) {
                    LobbyManager.this.broadcastTitle("\u00a7e\u2694 \u307e\u3082\u306a\u304f\u958b\u59cb\uff01", "");
                    LobbyManager.this.broadcastSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                } else if (this.remaining <= 3 && this.remaining > 0) {
                    LobbyManager.this.broadcastTitle("\u00a7c" + this.remaining, "");
                    LobbyManager.this.broadcastSound(Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                }
                if (this.remaining <= 0) {
                    if (LobbyManager.this.countdownBossBar != null) {
                        LobbyManager.this.countdownBossBar.removeAll();
                        LobbyManager.this.countdownBossBar = null;
                    }
                    this.cancel();
                    LobbyManager.this.countdownTask = null;
                    LobbyManager.this.currentCountdown = -1;
                    LobbyManager.this.launchGame();
                    return;
                }
                --this.remaining;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
        this.countdownBossBar = Bukkit.createBossBar((String)"\u00a7e\u00a7l\u8a66\u5408\u958b\u59cb\u307e\u3067...", (BarColor)BarColor.YELLOW, (BarStyle)BarStyle.SOLID, (BarFlag[])new BarFlag[0]);
        for (UUID uid : this.waitingPlayers) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.countdownBossBar.addPlayer(p);
        }
    }

    private void cancelCountdown() {
        if (this.countdownTask != null) {
            this.countdownTask.cancel();
            this.countdownTask = null;
        }
        if (this.countdownBossBar != null) {
            this.countdownBossBar.removeAll();
            this.countdownBossBar = null;
        }
        this.currentCountdown = -1;
    }

    private void launchGame() {
        ArrayList<UUID> participants = new ArrayList<UUID>(this.waitingPlayers);
        this.waitingPlayers.clear();
        participants.removeIf(uid -> Bukkit.getPlayer((UUID)uid) == null);
        if (participants.size() < 2) {
            this.broadcastWaiting("\u00a7c\u4eba\u6570\u304c\u8db3\u308a\u306a\u3044\u305f\u3081\u8a66\u5408\u3092\u958b\u59cb\u3067\u304d\u307e\u305b\u3093\u3002");
            return;
        }
        this.startVoting(participants);
    }

    private void startVoting(final List<UUID> participants) {
        this.cancelVoting();
        this.voteParticipants = new ArrayList<UUID>(participants);
        this.pendingVotes.clear();
        this.voteOptions.clear();
        Set<String> usedCombos = new LinkedHashSet<String>();
        for (int attempt = 0; attempt < 30 && this.voteOptions.size() < 3; ++attempt) {
            GameMode mode = GameMode.random(participants.size());
            MapConfig map = this.plugin.getMapManager().selectMap(mode);
            if (map == null) {
                continue;
            }
            String key = map.getId() + "|" + mode.name();
            if (!usedCombos.add(key)) {
                continue;
            }
            this.voteOptions.add(new Object[]{map, mode});
        }
        if (this.voteOptions.isEmpty()) {
            this.broadcastWaiting("\u00a7c\u4f7f\u7528\u53ef\u80fd\u306a\u30de\u30c3\u30d7\u304c\u3042\u308a\u307e\u305b\u3093\u3002config.yml \u3092\u78ba\u8a8d\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        for (UUID uid : participants) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.sendMessage("\u00a76\u00a7l[\u6295\u7968] \u00a77\u30de\u30c3\u30d7/\u30e2\u30fc\u30c9\u3092\u9078\u3093\u3067\u306d\uff01 \u00a78(/ba vote <1|2|3>)");
            for (int i = 0; i < this.voteOptions.size(); ++i) {
                Object[] opt = this.voteOptions.get(i);
                MapConfig mc = (MapConfig)opt[0];
                GameMode gm = (GameMode)opt[1];
                p.sendMessage("\u00a7e" + (i + 1) + ". \u00a7f" + mc.getDisplayName() + " \u00a77- " + gm.getDisplayName());
            }
        }
        this.broadcastWaiting("\u00a7e20\u79d2\u3067\u7d50\u679c\u767a\u8868\uff01 \u00a77\u6295\u7968\u3057\u306a\u3044\u5834\u5408\u306f\u30e9\u30f3\u30c0\u30e0\u306b\u306a\u308a\u307e\u3059\u3002");
        this.voteTask = new BukkitRunnable(){
            public void run() {
                LobbyManager.this.finishVoting();
            }
        }.runTaskLater((Plugin)this.plugin, 400L);
    }

    private void finishVoting() {
        this.voteTask = null;
        if (this.voteParticipants.isEmpty()) {
            return;
        }
        int[] counts = new int[this.voteOptions.size()];
        for (Integer choice : this.pendingVotes.values()) {
            if (choice >= 1 && choice <= this.voteOptions.size()) {
                int n = choice - 1;
                counts[n] = counts[n] + 1;
            }
        }
        int best = -1;
        int bestCount = 0;
        boolean tie = false;
        for (int i = 0; i < counts.length; ++i) {
            if (counts[i] > bestCount) {
                bestCount = counts[i];
                best = i;
                tie = false;
            } else if (counts[i] == bestCount && bestCount > 0) {
                tie = true;
            }
        }
        Object[] chosen;
        if (best < 0 || bestCount == 0 || tie) {
            chosen = this.voteOptions.get(new Random().nextInt(this.voteOptions.size()));
            this.broadcastWaiting("\u00a7e\u6295\u7968\u4e0d\u8db3\u307e\u305f\u306f\u5f15\u304d\u5206\u3051\u306e\u305f\u3081\u3001\u30e9\u30f3\u30c0\u30e0\u3067\u6c7a\u5b9a\u3057\u307e\u3057\u305f\u3002");
        } else {
            chosen = this.voteOptions.get(best);
            this.broadcastWaiting("\u00a7a\u6295\u7968\u7d50\u679c: \u9078\u629e\u3055\u308c\u305f\u306e\u306f \u00a7e" + (best + 1) + "\u756a\u76ee \u00a7a\u3067\u3059\uff01");
        }
        MapConfig map = (MapConfig)chosen[0];
        GameMode mode = (GameMode)chosen[1];
        List<UUID> participants = new ArrayList<UUID>(this.voteParticipants);
        this.voteParticipants.clear();
        this.pendingVotes.clear();
        this.voteOptions.clear();
        if (map == null) {
            this.broadcastWaiting("\u00a7c\u4f7f\u7528\u53ef\u80fd\u306a\u30de\u30c3\u30d7\u304c\u3042\u308a\u307e\u305b\u3093\u3002");
            return;
        }
        this.plugin.getGameManager().startGame(map, mode, participants);
    }

    private void cancelVoting() {
        if (this.voteTask != null) {
            this.voteTask.cancel();
            this.voteTask = null;
        }
        this.pendingVotes.clear();
        this.voteParticipants.clear();
        this.voteOptions.clear();
    }

    public boolean castVote(Player p, int choice) {
        if (this.voteTask == null || this.voteParticipants.isEmpty()) {
            return false;
        }
        if (choice < 1 || choice > this.voteOptions.size()) {
            p.sendMessage("\u00a7c1\u301c" + this.voteOptions.size() + "\u306e\u6295\u7968\u756a\u53f7\u3092\u6307\u5b9a\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
            return false;
        }
        if (!this.voteParticipants.contains(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u73fe\u5728\u6295\u7968\u3067\u304d\u307e\u305b\u3093\u3002");
            return false;
        }
        this.pendingVotes.put(p.getUniqueId(), choice);
        p.sendMessage("\u00a7a\u6295\u7968\u3057\u307e\u3057\u305f: \u00a7e" + choice + "\u756a\u76ee");
        return true;
    }

    public Location getLobbyOobMin() {
        return this.lobbyOobMin;
    }

    public Location getLobbyOobMax() {
        return this.lobbyOobMax;
    }

    public boolean hasLobbyOob() {
        return this.lobbyOobMin != null && this.lobbyOobMax != null;
    }

    public void setLobbyOob(Location min, Location max) {
        this.lobbyOobMin = min.clone();
        this.lobbyOobMax = max.clone();
        FileConfiguration cfg = this.plugin.getConfig();
        this.saveLoc(cfg, "lobby.oob.min.", min);
        this.saveLoc(cfg, "lobby.oob.max.", max);
        this.plugin.saveConfig();
    }

    public void onGameEnd() {
    }

    public boolean isContinuousMode() {
        return this.continuousMode;
    }

    public void setContinuousMode(boolean v) {
        this.continuousMode = v;
        this.broadcastWaiting(v ? "\u00a7a\u9023\u7d9a\u8a66\u5408\u30e2\u30fc\u30c9\u304c\u6709\u52b9\u306b\u306a\u308a\u307e\u3057\u305f\u3002\u8a66\u5408\u7d42\u4e86\u5f8c\u306b\u81ea\u52d5\u3067\u6b21\u306e\u8a66\u5408\u304c\u59cb\u307e\u308a\u307e\u3059\u3002" : "\u00a77\u9023\u7d9a\u8a66\u5408\u30e2\u30fc\u30c9\u304c\u7121\u52b9\u306b\u306a\u308a\u307e\u3057\u305f\u3002");
    }

    public void startContinuousCountdown() {
        if (this.countdownTask != null) {
            this.countdownTask.cancel();
        }
        final int delay = 15;
        this.broadcastWaiting("\u00a7e\u6b21\u306e\u8a66\u5408\u307e\u3067 \u00a76" + delay + "\u79d2...");
        this.countdownBossBar = Bukkit.createBossBar((String)"\u00a7e\u00a7l\u8a66\u5408\u958b\u59cb\u307e\u3067...", (BarColor)BarColor.YELLOW, (BarStyle)BarStyle.SOLID, (BarFlag[])new BarFlag[0]);
        for (UUID uid : this.waitingPlayers) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.countdownBossBar.addPlayer(p);
        }
        this.countdownTask = new BukkitRunnable(){
            int t;
            {
                this.t = delay;
            }

            public void run() {
                if (LobbyManager.this.plugin.getGameManager().getState() != GameState.WAITING) {
                    this.cancel();
                    return;
                }
                --this.t;
                if (LobbyManager.this.countdownBossBar != null) {
                    LobbyManager.this.countdownBossBar.setProgress((double)this.t / (double)delay);
                    LobbyManager.this.countdownBossBar.setTitle("\u00a7e\u00a7l\u8a66\u5408\u958b\u59cb\u307e\u3067 \u00a76" + this.t + "\u00a7e \u79d2");
                }
                if (this.t <= 0) {
                    if (LobbyManager.this.countdownBossBar != null) {
                        LobbyManager.this.countdownBossBar.removeAll();
                        LobbyManager.this.countdownBossBar = null;
                    }
                    this.cancel();
                    LobbyManager.this.countdownTask = null;
                    ArrayList<UUID> participants = new ArrayList<UUID>(LobbyManager.this.getWaitingPlayers());
                    if (participants.size() < 2) {
                        LobbyManager.this.broadcastWaiting("\u00a7c\u4eba\u6570\u304c\u8db3\u308a\u306a\u3044\u305f\u3081\u6b21\u306e\u8a66\u5408\u3092\u958b\u59cb\u3067\u304d\u307e\u305b\u3093\u3002");
                        return;
                    }
                    GameMode mode = GameMode.random(participants.size());
                    MapConfig map = LobbyManager.this.plugin.getMapManager().selectMap(mode);
                    if (map == null) {
                        LobbyManager.this.broadcastWaiting("\u00a7c\u4f7f\u7528\u53ef\u80fd\u306a\u30de\u30c3\u30d7\u304c\u3042\u308a\u307e\u305b\u3093\u3002");
                        return;
                    }
                    LobbyManager.this.plugin.getGameManager().startGame(map, mode, participants);
                } else if (this.t <= 5 || this.t % 5 == 0) {
                    LobbyManager.this.broadcastWaiting("\u00a7e\u6b21\u306e\u8a66\u5408\u307e\u3067 \u00a76" + this.t + "\u79d2...");
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 20L, 20L);
    }

    private void saveLoc(FileConfiguration cfg, String prefix, Location loc) {
        cfg.set(prefix + "x", (Object)loc.getX());
        cfg.set(prefix + "y", (Object)loc.getY());
        cfg.set(prefix + "z", (Object)loc.getZ());
    }

    private void broadcastWaiting(String msg) {
        for (UUID uid : this.waitingPlayers) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.sendMessage(msg);
        }
    }

    private void broadcastTitle(String title, String sub) {
        for (UUID uid : this.waitingPlayers) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.sendTitle(title, sub, 5, 30, 5);
        }
    }

    private void broadcastSound(Sound sound, float vol, float pitch) {
        for (UUID uid : this.waitingPlayers) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.playSound(p.getLocation(), sound, vol, pitch);
        }
    }

    public void setWaitingAreaMin(Location loc) {
        this.waitingMin = loc.clone();
        this.plugin.getConfig().set("lobby.waiting_area.world", (Object)loc.getWorld().getName());
        this.plugin.getConfig().set("lobby.waiting_area.min.x", (Object)loc.getBlockX());
        this.plugin.getConfig().set("lobby.waiting_area.min.y", (Object)loc.getBlockY());
        this.plugin.getConfig().set("lobby.waiting_area.min.x", (Object)loc.getBlockX());
        this.plugin.getConfig().set("lobby.waiting_area.min.y", (Object)loc.getBlockY());
        this.plugin.getConfig().set("lobby.waiting_area.min.z", (Object)loc.getBlockZ());
        this.plugin.saveConfig();
    }

    public void setWaitingAreaMax(Location loc) {
        this.waitingMax = loc.clone();
        this.plugin.getConfig().set("lobby.waiting_area.max.x", (Object)loc.getBlockX());
        this.plugin.getConfig().set("lobby.waiting_area.max.y", (Object)loc.getBlockY());
        this.plugin.getConfig().set("lobby.waiting_area.max.z", (Object)loc.getBlockZ());
        this.plugin.saveConfig();
    }

    public void setLobbySpawn(Location loc) {
        this.lobbySpawn = loc.clone();
        this.plugin.getConfig().set("lobby.spawn.world", (Object)loc.getWorld().getName());
        this.plugin.getConfig().set("lobby.spawn.x", (Object)loc.getX());
        this.plugin.getConfig().set("lobby.spawn.y", (Object)loc.getY());
        this.plugin.getConfig().set("lobby.spawn.z", (Object)loc.getZ());
        this.plugin.saveConfig();
    }

    public Location getLobbySpawn() {
        return this.lobbySpawn;
    }

    public Location getWaitingMin() {
        return this.waitingMin;
    }

    public Location getWaitingMax() {
        return this.waitingMax;
    }

    public World getWaitingWorld() {
        return this.waitingWorld;
    }

    public Set<UUID> getWaitingPlayers() {
        return Collections.unmodifiableSet(this.waitingPlayers);
    }

    public int getCurrentCountdown() {
        return this.currentCountdown;
    }
}

