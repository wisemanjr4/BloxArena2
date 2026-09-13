/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.Color
 *  org.bukkit.FireworkEffect
 *  org.bukkit.FireworkEffect$Type
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Particle$DustOptions
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.attribute.AttributeInstance
 *  org.bukkit.block.Block
 *  org.bukkit.boss.BarColor
 *  org.bukkit.boss.BarFlag
 *  org.bukkit.boss.BarStyle
 *  org.bukkit.boss.BossBar
 *  org.bukkit.entity.Arrow
 *  org.bukkit.entity.EnderPearl
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.Firework
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.SpectralArrow
 *  org.bukkit.entity.ThrownPotion
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.FireworkMeta
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.util.Vector
 */
package com.bloxarena.game;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameMode;
import com.bloxarena.game.GameState;
import com.bloxarena.game.TeamColor;
import com.bloxarena.game.WinCondition;
import com.bloxarena.kit.KitBuilder;
import com.bloxarena.kit.KitSelectGUI;
import com.bloxarena.kit.KitType;
import com.bloxarena.map.MapConfig;
import com.bloxarena.song.NbsPlayer;
import com.bloxarena.stats.MatchStats;
import com.bloxarena.stats.StatsManager;
import com.bloxarena.util.AnimatedText;
import com.bloxarena.util.Effects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class GameManager {
    private final BloxArenaPlugin plugin;
    private GameState state = GameState.WAITING;
    private MapConfig currentMap = null;
    private final List<UUID> redTeam = new ArrayList<UUID>();
    private final List<UUID> blueTeam = new ArrayList<UUID>();
    private final Set<UUID> spectators = new LinkedHashSet<UUID>();
    private final Set<UUID> noFallDamage = new HashSet<UUID>();
    private final Set<UUID> deadPlayers = new HashSet<UUID>();
    private final Set<UUID> centralZoneMembers = new HashSet<UUID>();
    private MatchStats matchStats = new MatchStats();
    private final Map<UUID, Integer> kills = new HashMap<UUID, Integer>();
    private final Map<UUID, Integer> deaths = new HashMap<UUID, Integer>();
    private final Map<UUID, Integer> roundKills = new HashMap<UUID, Integer>();
    private final Map<UUID, String> selectedKits = new HashMap<UUID, String>();
    private final Map<UUID, String> playerKit = new HashMap<UUID, String>();
    private BukkitTask endingTask = null;
    private BukkitTask holdTask = null;
    private TeamColor holdingTeam = null;
    private long inGameStartTime = 0L;
    private static final long OBJECTIVE_LOCK_MS = 120000L;
    private static final int HOLD_SECONDS = 15;
    private static final int WINS_TO_WIN = 3;
    private int currentRound = 0;
    private int roundWinsRed = 0;
    private int roundWinsBlue = 0;
    private GameMode currentGameMode = GameMode.BATTLE_ARENA;
    private BossBar preBattleBossBar = null;
    private int tdmKillsRed = 0;
    private int tdmKillsBlue = 0;
    private long tdmStartTime = 0L;
    private BukkitTask tdmTimerTask = null;
    private boolean bombPlanted = false;
    private Location bombLoc = null;
    private BukkitTask bombTimerTask = null;
    private int bombSecondsRemaining = 0;
    private boolean bombDefusing = false;
    private Player bombDefuser = null;
    private int defuseProgress = 0;
    private boolean bombRoundAttackerRed = true;
    private BukkitTask bombRoundTimerTask = null;
    private final Map<Integer, Float> domCapProgress = new HashMap<Integer, Float>();
    private final Map<Integer, TeamColor> domCapOwner = new HashMap<Integer, TeamColor>();
    private int domPointsRed = 0;
    private int domPointsBlue = 0;
    private BukkitTask domTimerTask = null;
    private BukkitTask gameTickTask = null;
    private NbsPlayer selectedBgm;
    private boolean redFlagTaken = false;
    private boolean blueFlagTaken = false;
    private UUID redFlagCarrier = null;
    private UUID blueFlagCarrier = null;
    private long redFlagDropTime = -1L;
    private long blueFlagDropTime = -1L;
    private Location redFlagDropLoc = null;
    private Location blueFlagDropLoc = null;
    private int ctfRedCaptures = 0;
    private int ctfBlueCaptures = 0;
    private final Map<Integer, Material> ctfOriginalBlocks = new HashMap<Integer, Material>();
    private final Map<UUID, Long> ctfPickupCooldown = new HashMap<UUID, Long>();
    private final Map<UUID, Boolean> ctfCarrierOnGround = new HashMap<UUID, Boolean>();
    private Location ctfRedFlagSpawn = null;
    private Location ctfBlueFlagSpawn = null;
    private int ctfRedTeamSize = 0;
    private int ctfBlueTeamSize = 0;
    private final Set<UUID> underdogPlayers = new HashSet<UUID>();
    private final Map<UUID, Long> underdogCooldown = new HashMap<UUID, Long>();

    public int getCtfRedTeamSize() {
        return this.ctfRedTeamSize;
    }

    public int getCtfBlueTeamSize() {
        return this.ctfBlueTeamSize;
    }

    public GameManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startGame(MapConfig map, GameMode mode, List<UUID> participants) {
        this.currentMap = map;
        this.currentGameMode = mode;
        if (this.currentGameMode == GameMode.BOMB_MISSION) {
            this.bombRoundAttackerRed = true;
        }
        Bukkit.broadcastMessage((String)("\u00a78\u00bb \u00a7d\u26a1 " + this.currentGameMode.getDisplayName() + " \u00a78\u00bb \u00a77" + this.currentGameMode.getDescription()));
        for (Player pl2 : Bukkit.getOnlinePlayers()) {
            pl2.playSound(pl2.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 2.0f);
        }
        for (UUID uid : participants) {
            Player pl = Bukkit.getPlayer((UUID)uid);
            if (pl == null) continue;
            AnimatedText.wave(this.plugin, List.of(pl), "\u26a1 " + this.currentGameMode.getDisplayName() + " \u26a1", "\u00a77" + this.currentGameMode.getDescription(), 10);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            String rules = switch (this.currentGameMode) {
                case BATTLE_ARENA -> "\u00a76\u00a7l\u2605 \u30eb\u30fc\u30eb \u00a78\u00bb \u00a7f\u6575\u3092\u6bb2\u6ec5\u3059\u308b\u304b\u3001\u4e2d\u592e\u30b3\u30f3\u30af\u30ea\u30fc\u30c8(25\u679a)\u3092\u81ea\u8272\u3067\u57cb\u3081\u306615\u79d2\u30db\u30fc\u30eb\u30c9\u305b\u3088\uff01BO3\u5148\u53d6\u5236\u3002";
                case TEAM_DEATHMATCH -> "\u00a76\u00a7l\u2605 \u30eb\u30fc\u30eb \u00a78\u00bb \u00a7f\u5236\u9650\u6642\u9593\u5185\u306b\u3088\u308a\u591a\u304f\u306e\u6575\u3092\u5012\u305b\uff01\u6b7b\u4ea1\u3057\u3066\u30823\u79d2\u3067\u30ea\u30b9\u30dd\u30fc\u30f3\u3002\u76ee\u6a1930\u30ad\u30eb\u5148\u53d6\u3067\u3082\u52dd\u5229\u3002";
                case BOMB_MISSION -> "\u00a76\u00a7l\u2605 \u30eb\u30fc\u30eb \u00a78\u00bb \u00a7f\u653b\u6483\u5074\u306f\u7206\u5f3e\u3092\u8a2d\u7f6e(5\u79d2)\u2192\u7206\u767a45\u79d2\u3002\u5b88\u5099\u5074\u306f\u89e3\u9664(7\u79d2)\u305b\u3088\uff01\u30e9\u30a6\u30f3\u30c9\u6bce\u306b\u653b\u5b88\u4ea4\u4ee3\u3002";
                case DOMINATION -> "\u00a76\u00a7l\u2605 \u30eb\u30fc\u30eb \u00a78\u00bb \u00a7f\u62e0\u70b9\u306b\u7acb\u3061\u7d9a\u3051\u3066\u5360\u9818\u305b\u3088\uff01\u5360\u9818\u62e0\u70b9\u304b\u3089\u6bce\u79d2\u30dd\u30a4\u30f3\u30c8\u7372\u5f97\u3002\u5148\u306b\u76ee\u6a19\u30dd\u30a4\u30f3\u30c8\u5230\u9054\u3067\u52dd\u5229\u3002";
                case CAPTURE_THE_FLAG -> "\u00a76\u00a7l\u2605 \u30eb\u30fc\u30eb \u00a78\u00bb \u00a7f\u6575\u9663\u306e\u65d7\u3092\u596a\u3044\u81ea\u9663\u306b\u6301\u3061\u5e30\u308c\uff01\u5148\u306b3\u56de\u596a\u53d6\u3067\u52dd\u5229\u3002\u6b7b\u4ea1\u6642\u306f\u65d7\u3092\u843d\u3068\u3059\u3002";
                default -> throw new IncompatibleClassChangeError();
            };
            Bukkit.broadcastMessage((String)rules);
        }, 60L);
        this.state = GameState.KIT_SELECT;
        this.currentRound = 1;
        this.roundWinsRed = 0;
        this.roundWinsBlue = 0;
        this.redTeam.clear();
        this.blueTeam.clear();
        this.kills.clear();
        this.deaths.clear();
        this.selectedKits.clear();
        this.playerKit.clear();
        this.noFallDamage.clear();
        this.deadPlayers.clear();
        this.underdogPlayers.clear();
        this.underdogCooldown.clear();
        this.assignTeams(participants);
        for (UUID uid : participants) {
            Player pp = Bukkit.getPlayer((UUID)uid);
            if (pp == null) continue;
            this.plugin.getSkillManager().restoreMaxHp(pp);
        }
        Bukkit.broadcastMessage((String)("\u00a76\u00a7l\u2605 MAP \u00a78\u00bb \u00a7e" + map.getDisplayName()));
        this.broadcastTeamAnnouncement();
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            for (UUID uid : participants) {
                Player pl = Bukkit.getPlayer((UUID)uid);
                if (pl == null || !pl.isOnline()) continue;
                TeamColor t = this.getTeamOf(pl);
                if (t == null) continue;
                pl.sendTitle(t.getColorCode() + "\u00a7l\u2605 \u3042\u306a\u305f\u306f " + t.getDisplayName() + " \u30c1\u30fc\u30e0", "\u00a77\u30ad\u30c3\u30c8\u9078\u629e\u753b\u9762\u3067\u6e96\u5099\u305b\u3088", 5, 40, 10);
            }
        }, 50L);
        this.initCenterBlocks(map);
        this.placeGates(map);
        this.plugin.getBotManager().spawnBotsForGame(this.redTeam, this.blueTeam, map);
        for (UUID uid : this.redTeam) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.kills.put(uid, 0);
            this.deaths.put(uid, 0);
            p.setGameMode(org.bukkit.GameMode.SURVIVAL);
            this.teleportToSpawnZonePublic(p, map, TeamColor.RED);
        }
        for (UUID uid : this.blueTeam) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.kills.put(uid, 0);
            this.deaths.put(uid, 0);
            p.setGameMode(org.bukkit.GameMode.SURVIVAL);
            this.teleportToSpawnZonePublic(p, map, TeamColor.BLUE);
        }
        int timeoutSeconds = this.plugin.getConfig().getInt("kit_select.timeout_seconds", 30);
        KitSelectGUI gui = new KitSelectGUI(this.plugin, this);
        this.plugin.getGameListeners().setActiveGUI(gui);
        gui.openForAll(this.redTeam, this.blueTeam, timeoutSeconds);
    }

    public void onKitSelectDone() {
        this.state = GameState.IN_GAME;
        this.inGameStartTime = System.currentTimeMillis();
        this.plugin.getUltimateManager().resetAll();
        this.ctfRedTeamSize = this.redTeam.size();
        this.ctfBlueTeamSize = this.blueTeam.size();
        this.applyUnderdogBonus();
        this.plugin.getScoreboardManager().start(this);
        this.gameTickTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, this::gameTickUpdate, 0L, 2L);
        if (this.selectedBgm != null) {
            Collection<Player> players = new ArrayList<>();
            for (UUID uid : this.getAllParticipants()) {
                Player p = Bukkit.getPlayer(uid);
                if (p == null) continue;
                players.add(p);
            }
            this.selectedBgm.play(players);
        }
        for (UUID uUID : this.redTeam) {
            Player p = Bukkit.getPlayer((UUID)uUID);
            if (p == null) continue;
            this.plugin.getSkillManager().refreshBurst(p);
        }
        for (UUID uUID : this.blueTeam) {
            Player p = Bukkit.getPlayer((UUID)uUID);
            if (p == null) continue;
            this.plugin.getSkillManager().refreshBurst(p);
        }
        if (this.currentGameMode == GameMode.DOMINATION) {
            this.domPointsRed = 0;
            this.domPointsBlue = 0;
            this.domCapProgress.clear();
            this.domCapOwner.clear();
            if (this.currentMap != null) {
                int idx = 0;
                for (MapConfig.DomPoint dp : this.currentMap.getDominationPoints()) {
                    this.domCapProgress.put(idx, Float.valueOf(0.0f));
                    this.domCapOwner.put(idx, null);
                    Location cloc = dp.getCenter();
                    if (cloc.getWorld() != null) {
                        cloc.getBlock().setType(Material.BEACON);
                    }
                    ++idx;
                }
            }
            int timeLimit = this.plugin.getConfig().getInt("domination.time_limit_seconds", 120);
            final int n = this.plugin.getConfig().getInt("domination.target_points", 100);
            this.domTimerTask = new BukkitRunnable(){

                public void run() {
                    if (GameManager.this.state != GameState.IN_GAME) {
                        this.cancel();
                        return;
                    }
                    GameManager.this.updateDomination(GameManager.this.currentMap, n);
                }
            }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
        }
        this.startCountdownBeforeBarrierRemoval();
        if (this.currentGameMode == GameMode.TEAM_DEATHMATCH) {
            this.tdmKillsRed = 0;
            this.tdmKillsBlue = 0;
            this.tdmStartTime = System.currentTimeMillis();
            final int timeLimit = this.plugin.getConfig().getInt("team_deathmatch.time_limit_seconds", 300);
            this.tdmTimerTask = new BukkitRunnable(){

                public void run() {
                    if (GameManager.this.state != GameState.IN_GAME) {
                        this.cancel();
                        return;
                    }
                    long elapsed = (System.currentTimeMillis() - GameManager.this.tdmStartTime) / 1000L;
                    long remaining = (long)timeLimit - elapsed;
                    if (remaining <= 0L) {
                        this.cancel();
                        TeamColor winner = GameManager.this.tdmKillsRed > GameManager.this.tdmKillsBlue ? TeamColor.RED : (GameManager.this.tdmKillsBlue > GameManager.this.tdmKillsRed ? TeamColor.BLUE : null);
                        GameManager.this.endGame(winner, WinCondition.ELIMINATION);
                    }
                }
            }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
        }
        if (this.currentGameMode == GameMode.BOMB_MISSION) {
            TeamColor attacker = this.bombRoundAttackerRed ? TeamColor.RED : TeamColor.BLUE;
            for (UUID uid : attacker == TeamColor.RED ? this.redTeam : this.blueTeam) {
                Player pl = Bukkit.getPlayer((UUID)uid);
                if (pl == null) continue;
                ItemStack bomb = new ItemStack(Material.TNT);
                ItemMeta m = bomb.getItemMeta();
                if (m != null) {
                    m.setDisplayName("\u00a7c\u00a7l\ud83d\udca3 \u7206\u5f3e \u00a78\u00bb \u00a77\u8a2d\u7f6e\u5730\u70b9\u3067\u53f3\u30af\u30ea\u30c3\u30af");
                    bomb.setItemMeta(m);
                }
                pl.getInventory().addItem(new ItemStack[]{bomb});
            }
            this.broadcastBombRoundInfo();
            this.bombRoundTimerTask = Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (this.state != GameState.IN_GAME) {
                    return;
                }
                if (!this.bombPlanted) {
                    TeamColor defender = this.bombRoundAttackerRed ? TeamColor.BLUE : TeamColor.RED;
                    Bukkit.broadcastMessage((String)("\u00a7e\u23f0 \u6642\u9593\u5207\u308c \u00a78\u00bb " + defender.getColorCode() + "\u00a7l" + defender.getDisplayName() + " \u00a7f\u30c1\u30fc\u30e0\u52dd\u5229\uff01"));
                    this.endGame(defender, WinCondition.OBJECTIVE);
                }
            }, (long)this.plugin.getConfig().getInt("bomb_mission.time_limit_seconds", 180) * 20L);
        }
        if (this.currentGameMode == GameMode.CAPTURE_THE_FLAG) {
            this.ctfRedCaptures = 0;
            this.ctfBlueCaptures = 0;
            this.redFlagTaken = false;
            this.blueFlagTaken = false;
            this.redFlagCarrier = null;
            this.blueFlagCarrier = null;
            this.redFlagDropTime = -1L;
            this.blueFlagDropTime = -1L;
            this.redFlagDropLoc = null;
            this.blueFlagDropLoc = null;
            if (this.currentMap != null) {
                if (this.currentMap.getRedFlagLocation() != null) {
                    Location rloc = this.currentMap.getRedFlagLocation();
                    this.ctfRedFlagSpawn = rloc.clone();
                    this.ctfOriginalBlocks.put(0, rloc.getBlock().getType());
                    rloc.getBlock().setType(Material.RED_BANNER);
                }
                if (this.currentMap.getBlueFlagLocation() != null) {
                    Location bloc = this.currentMap.getBlueFlagLocation();
                    this.ctfBlueFlagSpawn = bloc.clone();
                    this.ctfOriginalBlocks.put(1, bloc.getBlock().getType());
                    bloc.getBlock().setType(Material.CYAN_BANNER);
                }
            }
            new BukkitRunnable(){

                public void run() {
                    if (GameManager.this.state != GameState.IN_GAME) {
                        this.cancel();
                        return;
                    }
                    GameManager.this.updateCTF();
                }
            }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
        }
    }

    private void applyUnderdogBonus() {
        this.underdogPlayers.clear();
        this.underdogCooldown.clear();
        int red = this.redTeam.size();
        int blue = this.blueTeam.size();
        TeamColor disadvantaged = null;
        if (red >= 2 && blue == 1) {
            disadvantaged = TeamColor.BLUE;
        } else if (blue >= 2 && red == 1) {
            disadvantaged = TeamColor.RED;
        }
        if (disadvantaged == null) {
            return;
        }
        List<UUID> team = disadvantaged == TeamColor.RED ? this.redTeam : this.blueTeam;
        for (UUID uid : team) {
            this.underdogPlayers.add(uid);
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.sendMessage("\u00a7e\u00a7l\u2605 \u7d66\u4e0e\u30dc\u30fc\u30ca\u30b9 \u00a78\u00bb \u00a77\u4eba\u6570\u4e0d\u5229\u306e\u305f\u3081\u3001\u53d7\u5275\u6642\u306b\u518d\u751fIII\u309210\u79d2\u9593\u7372\u5f97\uff08\u72ec\u81eaCT22\u79d2\uff09");
        }
    }

    public boolean isUnderdog(UUID uid) {
        return this.underdogPlayers.contains(uid);
    }

    public boolean isUnderdogCooldownReady(UUID uid) {
        Long end = this.underdogCooldown.get(uid);
        if (end == null) {
            return true;
        }
        if (System.currentTimeMillis() >= end) {
            this.underdogCooldown.remove(uid);
            return true;
        }
        return false;
    }

    public void startUnderdogCooldown(UUID uid) {
        this.underdogCooldown.put(uid, System.currentTimeMillis() + 22000L);
    }

    private void gameTickUpdate() {
        if (this.state != GameState.IN_GAME) {
            return;
        }
        this.plugin.getSkillManager().fastUpdate();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!this.isParticipant(p) || this.isSpectator(p)) continue;
            this.updateCentralZone(p);
        }
    }

    public boolean isInCentralZone(Player p) {
        if (this.state != GameState.IN_GAME || this.currentMap == null || this.currentMap.getCenter() == null) {
            return false;
        }
        GameMode m = this.currentGameMode;
        if (m != GameMode.BATTLE_ARENA && m != GameMode.TEAM_DEATHMATCH) {
            return false;
        }
        if (!this.isParticipant(p) || this.isSpectator(p)) {
            return false;
        }
        Location c = this.currentMap.getCenter();
        Location l = p.getLocation();
        int cx = c.getBlockX();
        int cy = c.getBlockY();
        int cz = c.getBlockZ();
        int px = l.getBlockX();
        int py = l.getBlockY();
        int pz = l.getBlockZ();
        if (px < cx - 2 || px > cx + 2 || pz < cz - 2 || pz > cz + 2) {
            return false;
        }
        return py >= cy && py <= cy + 3;
    }

    private void updateCentralZone(Player p) {
        if (this.isInCentralZone(p)) {
            if (this.centralZoneMembers.add(p.getUniqueId())) {
                p.sendMessage("\u00a7c\u2694 \u4e2d\u592e\u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u5185 \u00a78\u00bb \u00a77\u30ac\u30fc\u30c9/\u7121\u6575\u4e0d\u53ef");
            }
            p.setCooldown(Material.SHIELD, 20);
            if (p.isBlocking()) {
                p.setShieldBlockingDelay(20);
            }
            if (p.isInvulnerable()) {
                p.setInvulnerable(false);
            }
            PotionEffect dr = p.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            if (dr != null && dr.getAmplifier() >= 254) {
                p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            }
        } else if (this.centralZoneMembers.remove(p.getUniqueId())) {
            if (!this.plugin.getSkillManager().isGuardBroken(p.getUniqueId())) {
                p.setShieldBlockingDelay(4);
            }
        }
    }

    private void updateDomination(MapConfig map, int targetPoints) {
        if (map == null) {
            return;
        }
        int idx = 0;
        int pointsPerSec = this.plugin.getConfig().getInt("domination.points_per_second", 2);
        for (MapConfig.DomPoint dp : map.getDominationPoints()) {
            TeamColor now;
            Location center = dp.getCenter();
            double radius = dp.getRadius();
            World w = center.getWorld();
            if (w == null) {
                ++idx;
                continue;
            }
            int redCount = 0;
            int blueCount = 0;
            for (Player pl : w.getPlayers()) {
                if (!this.isParticipant(pl) || this.isSpectator(pl) || pl.getLocation().distance(center) > radius) continue;
                if (this.redTeam.contains(pl.getUniqueId())) {
                    ++redCount;
                    continue;
                }
                if (!this.blueTeam.contains(pl.getUniqueId())) continue;
                ++blueCount;
            }
            float progress = this.domCapProgress.getOrDefault(idx, Float.valueOf(0.0f)).floatValue();
            if (redCount > 0 && blueCount == 0 && progress < 0.0f) {
                progress = 0.0f;
            }
            if (blueCount > 0 && redCount == 0 && progress > 0.0f) {
                progress = 0.0f;
            }
            if (redCount > blueCount) {
                if (progress <= -0.8f) {
                    progress = 0.0f;
                } else {
                    float ratio = this.ctfBlueTeamSize > 0 ? (float)this.ctfBlueTeamSize / (float)this.ctfRedTeamSize : 1.0f;
                    progress = Math.min(1.0f, progress + 0.05f * (float)(redCount - blueCount) * ratio);
                }
            } else if (blueCount > redCount) {
                if (progress >= 0.8f) {
                    progress = 0.0f;
                } else {
                    float ratio = this.ctfRedTeamSize > 0 ? (float)this.ctfRedTeamSize / (float)this.ctfBlueTeamSize : 1.0f;
                    progress = Math.max(-1.0f, progress - 0.05f * (float)(blueCount - redCount) * ratio);
                }
            }
            this.domCapProgress.put(idx, Float.valueOf(progress));
            TeamColor prev = this.domCapOwner.get(idx);
            now = progress >= 0.8f ? TeamColor.RED : (progress <= -0.8f ? TeamColor.BLUE : null);
            if (now != prev) {                this.domCapOwner.put(idx, now);
                if (now != null) {
                    Bukkit.broadcastMessage((String)(now.getColorCode() + "\u2605 \u62e0\u70b9" + (idx + 1) + " \u00a78\u00bb \u00a77\u5360\u9818\uff01"));
                }
            }
            if (center.getBlock().getType() == Material.BEACON) {
                Color c = Color.fromRGB((int)(progress > 0.0f ? (int)(255.0f * progress) : 0), (int)0, (int)(progress < 0.0f ? (int)(-255.0f * progress) : 0));
                w.spawnParticle(Particle.REDSTONE, center.clone().add(0.0, 1.0, 0.0), 5, 1.0, 0.5, 1.0, (Object)new Particle.DustOptions(c, 1.5f));
            }
            if (now == TeamColor.RED) {
                this.domPointsRed += pointsPerSec;
            } else if (now == TeamColor.BLUE) {
                this.domPointsBlue += pointsPerSec;
            }
            ++idx;
        }
        if (this.domPointsRed >= targetPoints) {
            this.endGame(TeamColor.RED, WinCondition.OBJECTIVE);
            return;
        }
        if (this.domPointsBlue >= targetPoints) {
            this.endGame(TeamColor.BLUE, WinCondition.OBJECTIVE);
            return;
        }
    }

    private void startCountdownBeforeBarrierRemoval() {
        this.broadcastSound(Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.2f);
        this.preBattleBossBar = Bukkit.createBossBar((String)"\u00a7c\u00a7l\u2694 \u6226\u95d8\u6e96\u5099\u4e2d...", (BarColor)BarColor.RED, (BarStyle)BarStyle.SOLID, (BarFlag[])new BarFlag[0]);
        for (UUID uid : this.getAllParticipantsAndSpectators()) {
            Player pl = Bukkit.getPlayer((UUID)uid);
            if (pl == null) continue;
            this.preBattleBossBar.addPlayer(pl);
        }
        this.playModeNameSweep();
    }

    private void playModeNameSweep() {
        final String mapName = this.currentMap != null ? this.currentMap.getDisplayName() : "???";
        final String modeName = this.currentGameMode.getDisplayName();
        final String colored = "\u00a76\u00a7l\u2694 " + modeName + " \u2694";
        final int extra = 18;
        final int steps = 60;
        new BukkitRunnable(){
            int t = 0;

            public void run() {
                if (this.t >= steps) {
                    this.cancel();
                    GameManager.this.beginPreBattleCountdown(mapName);
                    return;
                }
                int pad = (int)((steps - this.t) * (double)extra / steps) + 1;
                GameManager.this.broadcastTitle("\u00a7f" + GameManager.this.repeatSpaces(pad) + colored, "\u00a7e" + modeName + " \u00a78\u00bb \u00a77" + GameManager.this.currentGameMode.getDescription(), 0, 2, 0);
                ++this.t;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void beginPreBattleCountdown(final String mapName) {
        new BukkitRunnable(){
            int count = 5;

            public void run() {
                if (this.count > 0) {
                    String color = switch (this.count) {
                        case 5 -> "\u00a7c\u00a7l";
                        case 4 -> "\u00a76\u00a7l";
                        case 3 -> "\u00a7e\u00a7l";
                        case 2 -> "\u00a7a\u00a7l";
                        default -> "\u00a7b\u00a7l";
                    };
                    float pitch = 0.6f + (float)(5 - this.count) * 0.15f;
                    GameManager.this.broadcastTitle(color + this.count, "\u00a77" + mapName, 0, 22, 3);
                    GameManager.this.broadcastSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.2f, pitch);
                    GameManager.this.broadcastActionBar("\u00a7f\u6e96\u5099\u4e07\u7aef \u00a78\u00bb \u00a7e" + this.count + " \u00a7f\u79d2\u5f8c\u306b\u30b2\u30fc\u30c8\u958b\u653e\uff01");
                    if (GameManager.this.preBattleBossBar != null) {
                        GameManager.this.preBattleBossBar.setProgress((double)this.count / 5.0);
                        GameManager.this.preBattleBossBar.setTitle("\u00a7c\u00a7l\u2694 \u30b2\u30fc\u30c8\u958b\u653e \u00a78\u00bb \u00a7e" + this.count + "\u00a7c \u79d2");
                    }
                    --this.count;
                } else {
                    if (GameManager.this.preBattleBossBar != null) {
                        GameManager.this.preBattleBossBar.removeAll();
                        GameManager.this.preBattleBossBar = null;
                    }
                    this.cancel();
                    GameManager.this.removeGates(GameManager.this.currentMap);
                    GameManager.this.grantNoFallDamage();
                    GameManager.this.playFightSlam(mapName);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    private void playFightSlam(final String mapName) {
        final String[] letters = new String[]{"\u00a7c\u00a7lF", "\u00a7c\u00a7lI", "\u00a7c\u00a7lG", "\u00a7c\u00a7lH", "\u00a7c\u00a7lT", "\u00a7c\u00a7l!", "\u00a7c\u00a7l!"};
        final int maxGap = 6;
        final int steps = 14;
        new BukkitRunnable(){
            int t = 0;

            public void run() {
                if (this.t > steps) {
                    this.cancel();
                    GameManager.this.finishFightStart(mapName);
                    return;
                }
                int gap = maxGap - (int)((double)this.t / steps * maxGap);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < letters.length; ++i) {
                    if (i > 0) {
                        sb.append(GameManager.this.repeatSpaces(gap));
                    }
                    sb.append(letters[i]);
                }
                GameManager.this.broadcastTitle(sb.toString(), "\u00a7e" + mapName, 0, 2, 0);
                ++this.t;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void finishFightStart(final String mapName) {
        this.broadcastTitle("\u00a7c\u00a7l\u2694  FIGHT!!  \u2694", "\u00a7e" + mapName + " \u00a78\u00bb \u00a7f\u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u3092\u5236\u5727\u305b\u3088\uff01", 3, 50, 12);
        this.broadcastSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 1.0f);
        this.broadcastSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.3f);
        this.broadcastSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        this.broadcastActionBar("\u00a7c\u00a7l\u2694 FIGHT!! \u00a78\u00bb \u00a7e2\u5206\u5f8c\u306b\u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u89e3\u653e\uff01");
        for (UUID uid : this.getAllParticipants()) {
            Player pl = Bukkit.getPlayer((UUID)uid);
            if (pl == null || !pl.isOnline()) continue;
            pl.spawnParticle(Particle.VILLAGER_HAPPY, pl.getLocation().add(0.0, 1.2, 0.0), 20, 0.4, 0.5, 0.4, 0.2);
            pl.spawnParticle(Particle.FIREWORKS_SPARK, pl.getLocation().add(0.0, 2.0, 0.0), 25, 0.5, 0.5, 0.5, 0.1);
        }
        if (this.currentMap != null && this.currentMap.getCenter() != null) {
            this.currentMap.getCenter().getWorld().strikeLightningEffect(this.currentMap.getCenter());
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (GameManager.this.currentMap != null && GameManager.this.currentMap.getCenter() != null) {
                    GameManager.this.currentMap.getCenter().getWorld().strikeLightningEffect(GameManager.this.currentMap.getCenter());
                }
            }, 5L);
        }
    }

    private String repeatSpaces(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private void assignTeams(List<UUID> participants) {
        Player p;
        ArrayList<UUID> shuffled = new ArrayList<UUID>(participants);
        Random rng = new Random();
        for (int i = shuffled.size() - 1; i > 0; --i) {
            int j = rng.nextInt(i + 1);
            UUID tmp = (UUID)shuffled.get(i);
            shuffled.set(i, (UUID)shuffled.get(j));
            shuffled.set(j, tmp);
        }
        int half = shuffled.size() / 2;
        int redCount = half + (shuffled.size() % 2 == 1 && rng.nextBoolean() ? 1 : 0);
        for (int i = 0; i < shuffled.size(); ++i) {
            if (i < redCount) {
                this.redTeam.add((UUID)shuffled.get(i));
                continue;
            }
            this.blueTeam.add((UUID)shuffled.get(i));
        }
        StringBuilder log = new StringBuilder("\u00a78[BA] \u30c1\u30fc\u30e0\u632f\u308a\u5206\u3051: \u00a7c\u8d64=");
        for (UUID uid : this.redTeam) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            log.append(p.getName()).append(" ");
        }
        log.append("\u00a79\u9752=");
        for (UUID uid : this.blueTeam) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            log.append(p.getName()).append(" ");
        }
        Bukkit.getConsoleSender().sendMessage(log.toString());
    }

    private void broadcastTeamAnnouncement() {
        Player p;
        StringBuilder redNames = new StringBuilder();
        for (UUID uid : this.redTeam) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            if (redNames.length() > 0) {
                redNames.append("\u00a77, ");
            }
            redNames.append("\u00a7f").append(p.getName());
        }
        StringBuilder blueNames = new StringBuilder();
        for (UUID uid : this.blueTeam) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            if (blueNames.length() > 0) {
                blueNames.append("\u00a77, ");
            }
            blueNames.append("\u00a7f").append(p.getName());
        }
        Bukkit.broadcastMessage((String)("\u00a76\u00a7l\u2605 \u30c1\u30fc\u30e0\u632f\u308a\u5206\u3051 \u00a78\u00bb \u00a7c[RED] \u00a7f" + redNames + " \u00a77vs \u00a79[BLUE] \u00a7f" + blueNames));
        for (UUID uid : this.redTeam) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.sendTitle("\u00a7c\u00a7l\u2605 \u8d64\u30c1\u30fc\u30e0", "\u00a77\u547d\u3092\u6253\u3063\u3066\u6226\u3048\uff01", 5, 60, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
        for (UUID uid : this.blueTeam) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.sendTitle("\u00a79\u00a7l\u2605 \u9752\u30c1\u30fc\u30e0", "\u00a77\u547d\u3092\u6253\u3063\u3066\u6226\u3048\uff01", 5, 60, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    private void initCenterBlocks(MapConfig map) {
        World world = Bukkit.getWorld((String)map.getWorld());
        if (world == null) {
            return;
        }
        Location center = map.getCenter();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        for (int dx = -2; dx <= 2; ++dx) {
            for (int dz = -2; dz <= 2; ++dz) {
                world.getBlockAt(cx + dx, cy - 1, cz + dz).setType(Material.LIME_CONCRETE);
                world.getBlockAt(cx + dx, cy, cz + dz).setType(Material.WHITE_CONCRETE);
            }
        }
    }

    private void resetCenterBlocks() {
        if (this.currentMap == null) {
            return;
        }
        World world = Bukkit.getWorld((String)this.currentMap.getWorld());
        if (world == null) {
            return;
        }
        Location center = this.currentMap.getCenter();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        for (int dx = -2; dx <= 2; ++dx) {
            for (int dz = -2; dz <= 2; ++dz) {
                world.getBlockAt(cx + dx, cy, cz + dz).setType(Material.WHITE_CONCRETE);
            }
        }
        this.cancelHoldTimer();
    }

    public void placeGates(MapConfig map) {
        if (!map.hasGate()) {
            return;
        }
        Material mat = map.getGateMaterial();
        this.fillRegion(map.getRedGateMin(), map.getRedGateMax(), mat);
        this.fillRegion(map.getBlueGateMin(), map.getBlueGateMax(), mat);
    }

    public void removeGates(MapConfig map) {
        if (!map.hasGate()) {
            return;
        }
        this.fillRegion(map.getRedGateMin(), map.getRedGateMax(), Material.AIR);
        this.fillRegion(map.getBlueGateMin(), map.getBlueGateMax(), Material.AIR);
    }

    private void fillRegion(Location min, Location max, Material material) {
        if (min == null || max == null || min.getWorld() == null) {
            return;
        }
        World world = min.getWorld();
        int x1 = Math.min(min.getBlockX(), max.getBlockX());
        int y1 = Math.min(min.getBlockY(), max.getBlockY());
        int z1 = Math.min(min.getBlockZ(), max.getBlockZ());
        int x2 = Math.max(min.getBlockX(), max.getBlockX());
        int y2 = Math.max(min.getBlockY(), max.getBlockY());
        int z2 = Math.max(min.getBlockZ(), max.getBlockZ());
        for (int x = x1; x <= x2; ++x) {
            for (int y = y1; y <= y2; ++y) {
                for (int z = z1; z <= z2; ++z) {
                    world.getBlockAt(x, y, z).setType(material);
                }
            }
        }
    }

    private void grantNoFallDamage() {
        for (UUID uid : this.getAllParticipants()) {
            this.noFallDamage.add(uid);
        }
    }

    public void teleportToSpawnZonePublic(Player p, MapConfig map, TeamColor team) {
        Location min = team == TeamColor.RED ? map.getRedSpawnMin() : map.getBlueSpawnMin();
        Location max = team == TeamColor.RED ? map.getRedSpawnMax() : map.getBlueSpawnMax();
        double x = (double)(min.getBlockX() + max.getBlockX()) / 2.0 + 0.5;
        double y = (double)max.getBlockY() + 0.1;
        double z = (double)(min.getBlockZ() + max.getBlockZ()) / 2.0 + 0.5;
        p.teleport(new Location(min.getWorld(), x, y, z));
    }



    public void checkEliminationWin() {
        if (this.state != GameState.IN_GAME) {
            return;
        }
        long redAlive = this.getAliveCount(TeamColor.RED);
        long blueAlive = this.getAliveCount(TeamColor.BLUE);
        if (blueAlive == 0L && redAlive > 0L) {
            this.endGame(TeamColor.RED, WinCondition.ELIMINATION);
        } else if (redAlive == 0L && blueAlive > 0L) {
            this.endGame(TeamColor.BLUE, WinCondition.ELIMINATION);
        } else if (redAlive == 0L && blueAlive == 0L) {
            this.endGame(null, WinCondition.ELIMINATION);
        }
    }

    public void checkObjectiveWin(Location placedBlock) {
        if (this.state != GameState.IN_GAME) {
            return;
        }
        if (this.currentGameMode != GameMode.BATTLE_ARENA && this.currentGameMode != GameMode.TEAM_DEATHMATCH) {
            return;
        }
        if (this.currentMap == null) {
            return;
        }
        Location center = this.currentMap.getCenter();
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int checkY = placedBlock.getBlockY();
        World world = Bukkit.getWorld((String)this.currentMap.getWorld());
        if (world == null) {
            return;
        }
        int redCount = 0;
        int cyanCount = 0;
        for (int dx = -2; dx <= 2; ++dx) {
            for (int dz = -2; dz <= 2; ++dz) {
                Material mat = world.getBlockAt(cx + dx, checkY, cz + dz).getType();
                if (mat == Material.RED_CONCRETE) {
                    ++redCount;
                }
                if (mat != Material.CYAN_CONCRETE) continue;
                ++cyanCount;
            }
        }
        Bukkit.broadcastMessage((String)("\u00a76\u00a7l\u2694 \u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u4e89\u593a\u4e2d \u00a78\u00bb \u00a7c\u8d64 " + redCount + "/25 \u00a78| \u00a79\u9752 " + cyanCount + "/25"));
        Material first = null;
        boolean allSame = true;
        block2: for (int dx = -2; dx <= 2; ++dx) {
            for (int dz = -2; dz <= 2; ++dz) {
                Material mat = world.getBlockAt(cx + dx, checkY, cz + dz).getType();
                if (first == null) {
                    first = mat;
                    continue;
                }
                if (mat == first) continue;
                allSame = false;
                break block2;
            }
        }
        if (!allSame || first == null) {
            this.cancelHoldTimer();
            return;
        }
        TeamColor winner = null;
        if (first == Material.RED_CONCRETE) {
            winner = TeamColor.RED;
        }
        if (first == Material.CYAN_CONCRETE) {
            winner = TeamColor.BLUE;
        }
        if (winner == null) {
            return;
        }
        if (this.holdingTeam == winner && this.holdTask != null) {
            return;
        }
        this.cancelHoldTimer();
        this.holdingTeam = winner;
        final TeamColor fw = winner;
        this.broadcastTitle(winner.getColorCode() + "\u00a7l25\u679a\u5236\u5727\uff01", "\u00a7e15\u79d2\u30db\u30fc\u30eb\u30c9\u3067\u52dd\u5229\u305b\u3088\uff01", 5, 30, 5);
        Bukkit.broadcastMessage((String)(winner.getColorCode() + "\u00a7l\u2605 " + winner.getDisplayName() + " \u00a7f\u30c1\u30fc\u30e0\u304c25\u679a\u5236\u5727\uff01 \u00a7715\u79d2\u30db\u30fc\u30eb\u30c9\u3067\u52dd\u5229\uff01"));
        this.holdTask = new BukkitRunnable(){
            int rem = 15;

            public void run() {
                if (GameManager.this.state != GameState.IN_GAME) {
                    this.cancel();
                    return;
                }
                if (this.rem <= 0) {
                    this.cancel();
                    GameManager.this.endGame(fw, WinCondition.OBJECTIVE);
                    return;
                }
                GameManager.this.broadcastActionBar(fw.getColorCode() + "\u00a7l" + fw.getDisplayName() + " \u00a7e\u30db\u30fc\u30eb\u30c9\u4e2d \u00a78\u00bb \u00a7f" + this.rem + "\u00a7e\u79d2");
                --this.rem;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    public void onObjectiveBlockBroken() {
        if (this.holdTask != null) {
            this.cancelHoldTimer();
            Bukkit.broadcastMessage((String)"\u00a7c\u2694 \u30db\u30fc\u30eb\u30c9\u4e2d\u65ad \u00a78\u00bb \u00a77\u57fa\u76e4\u304c\u58ca\u3055\u308c\u305f\uff01");
        }
    }

    private void cancelHoldTimer() {
        if (this.holdTask != null) {
            this.holdTask.cancel();
            this.holdTask = null;
        }
        this.holdingTeam = null;
    }

    public boolean isObjectiveLocked() {
        return System.currentTimeMillis() - this.inGameStartTime < 120000L;
    }

    public void endGame(TeamColor winner, WinCondition condition) {
        if (this.currentGameMode == GameMode.TEAM_DEATHMATCH || this.currentGameMode == GameMode.DOMINATION || this.currentGameMode == GameMode.CAPTURE_THE_FLAG) {
            this.endMatch(winner, condition);
        } else {
            this.endRound(winner, condition);
        }
    }

    private void endRound(final TeamColor winner, final WinCondition condition) {
        World w;
        if (this.state == GameState.ENDING) {
            return;
        }
        this.state = GameState.ENDING;
        this.noFallDamage.clear();
        this.plugin.getScoreboardManager().stop();
        if (this.currentMap != null && this.currentMap.getCenter() != null && (w = this.currentMap.getCenter().getWorld()) != null) {
            w.getEntities().stream().filter(e -> e instanceof EnderPearl || e instanceof Firework || e instanceof ThrownPotion || e instanceof Arrow || e instanceof SpectralArrow).forEach(Entity::remove);
        }
        if (winner == TeamColor.RED) {
            ++this.roundWinsRed;
        } else if (winner == TeamColor.BLUE) {
            ++this.roundWinsBlue;
        }
        String roundResult = winner != null ? winner.getColorCode() + "\u00a7l" + winner.getDisplayName() + " \u00a7f\u30c1\u30fc\u30e0\u304c\u30e9\u30a6\u30f3\u30c9\u3092\u5236\u3057\u305f\uff01" : "\u00a77\u5f15\u304d\u5206\u3051";
        String scoreStr = "\u00a7c\u8d64 " + this.roundWinsRed + " \u00a77- \u00a79" + this.roundWinsBlue + " \u00a77\u9752";
        for (UUID uid : this.getAllParticipantsAndSpectators()) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.sendTitle(winner != null ? "\u00a7e\u00a7l\u2726 ROUND WIN \u2726" : "\u00a77\u00a7l\u2726 DRAW \u2726", roundResult + " \u00a78\u00bb " + scoreStr, 5, 50, 10);
            p.sendMessage("\u00a76\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
            p.sendMessage("\u00a76\u00a7l\u2605 \u30e9\u30a6\u30f3\u30c9 " + this.currentRound + " \u7d42\u4e86");
            p.sendMessage("\u00a77\u7d50\u679c \u00a78\u00bb " + roundResult);
            p.sendMessage("\u00a77\u30b9\u30b3\u30a2 \u00a78\u00bb " + scoreStr);
            p.sendMessage("\u00a76\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        }
        this.endingTask = this.roundWinsRed >= 3 || this.roundWinsBlue >= 3 ? new BukkitRunnable(){

            public void run() {
                GameManager.this.endMatch(winner, condition);
            }
        }.runTaskLater((Plugin)this.plugin, 80L) : new BukkitRunnable(){

            public void run() {
                GameManager.this.startNextRound();
            }
        }.runTaskLater((Plugin)this.plugin, 80L);
    }

    private void startNextRound() {
        Player p;
        ++this.currentRound;
        if (this.currentGameMode == GameMode.BOMB_MISSION) {
            this.bombRoundAttackerRed = !this.bombRoundAttackerRed;
            this.bombCleanup();
        }
        String scoreStr = "\u00a7c\u8d64 " + this.roundWinsRed + " \u00a77- \u00a79" + this.roundWinsBlue + " \u00a77\u9752";
        this.broadcastTitle("\u00a76\u00a7l\u2605 \u30e9\u30a6\u30f3\u30c9 " + this.currentRound + " \u2605", scoreStr, 10, 60, 10);
        for (UUID uid : this.getAllParticipantsAndSpectators()) {
            Player pl = Bukkit.getPlayer((UUID)uid);
            if (pl == null) continue;
            pl.sendMessage("\u00a76\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
            pl.sendMessage("\u00a76\u00a7l\u2605 \u30e9\u30a6\u30f3\u30c9 " + this.currentRound + " \u958b\u59cb\uff01");
            pl.sendMessage("\u00a77\u30b9\u30b3\u30a2 \u00a78\u00bb " + scoreStr);
            pl.sendMessage("\u00a76\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        }
        this.state = GameState.IN_GAME;
        this.noFallDamage.clear();
        this.deadPlayers.clear();
        this.centralZoneMembers.clear();
        this.matchStats = new MatchStats();
        this.deaths.clear();
        this.roundKills.clear();
        this.resetCenterBlocks();
        this.initCenterBlocks(this.currentMap);
        this.placeGates(this.currentMap);
        for (UUID uid : this.redTeam) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.roundRestorePlayer(p);
            this.teleportToSpawnZonePublic(p, this.currentMap, TeamColor.RED);
        }
        for (UUID uid : this.blueTeam) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.roundRestorePlayer(p);
            this.teleportToSpawnZonePublic(p, this.currentMap, TeamColor.BLUE);
        }
        this.spectators.clear();
        this.selectedKits.clear();
        this.playerKit.clear();
        this.plugin.getSkillManager().resetRound();
        this.state = GameState.KIT_SELECT;
        int timeoutSeconds = this.plugin.getConfig().getInt("kit_select.timeout_seconds", 30);
        KitSelectGUI gui = new KitSelectGUI(this.plugin, this);
        this.plugin.getGameListeners().setActiveGUI(gui);
        gui.openForAll(this.redTeam, this.blueTeam, timeoutSeconds);
        this.broadcastTitle("\u00a76\u00a7l\u2605 \u30e9\u30a6\u30f3\u30c9 " + this.currentRound, scoreStr, 5, 40, 10);
    }

    private void roundRestorePlayer(Player p) {
        this.deadPlayers.remove(p.getUniqueId());
        this.spectators.remove(p.getUniqueId());
        p.setInvulnerable(false);
        p.setGameMode(org.bukkit.GameMode.SURVIVAL);
        p.getInventory().clear();
        AttributeInstance attr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHp = attr != null ? attr.getValue() : 20.0;
        p.setHealth(maxHp);
        p.setFoodLevel(20);
        p.setSaturation(5.0f);
        p.setExhaustion(0.0f);
        for (PotionEffect eff : p.getActivePotionEffects()) {
            p.removePotionEffect(eff.getType());
        }
        p.setFireTicks(0);
        p.setArrowsInBody(0);
        p.setWalkSpeed(0.2f);
        p.resetTitle();
        this.plugin.getSkillManager().restoreMaxHp(p);
    }

    public KitType getPlayerKitType(UUID uid) {
        String name = this.playerKit.get(uid);
        if (name == null) {
            return null;
        }
        try {
            return KitType.valueOf(name);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void endMatch(TeamColor winner, WinCondition condition) {
        StatsManager sm = this.plugin.getStatsManager();
        List<UUID> loseTeam;
        List<UUID> winTeam = winner == TeamColor.RED ? this.redTeam : this.blueTeam;
        List<UUID> list = loseTeam = winner == TeamColor.RED ? this.blueTeam : this.redTeam;
        if (winner != null) {
            for (UUID uid : winTeam) {
                sm.addWin(uid);
            }
            for (UUID uid : loseTeam) {
                sm.addLoss(uid);
            }
        }
        sm.save();
        this.showMatchReport(winner);
        Effects.playVictoryEffect(winner, condition, this.getAllParticipantsAndSpectators(), this.redTeam, this.blueTeam, this.currentMap, this.kills, this.deaths, this.plugin, null);
        if (this.selectedBgm != null) {
            this.selectedBgm.stop();
        }
        this.endingTask = new BukkitRunnable(){

            public void run() {
                GameManager.this.returnAllToLobby();
                if (GameManager.this.plugin.getLobbyManager().isContinuousMode()) {
                    GameManager.this.plugin.getLobbyManager().startContinuousCountdown();
                }
            }
        }.runTaskLater((Plugin)this.plugin, 100L);
    }

    private void showMatchReport(TeamColor winner) {
        List<UUID> all = this.getAllParticipantsAndSpectators();
        String header = "\u00a76\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501";
        String winStr = winner != null ? winner.getColorCode() + "\u00a7l" + winner.getDisplayName() + " \u00a7f\u30c1\u30fc\u30e0\u52dd\u5229\uff01" : "\u00a77\u5f15\u304d\u5206\u3051";
        UUID mvpUid = this.matchStats.getMVP();
        UUID mostDmgUid = this.matchStats.getMostDamage();
        String mvpName = mvpUid != null ? Bukkit.getOfflinePlayer((UUID)mvpUid).getName() : "\u306a\u3057";
        String dmgName = mostDmgUid != null ? Bukkit.getOfflinePlayer((UUID)mostDmgUid).getName() : "\u306a\u3057";
        int mvpKills = mvpUid != null ? this.matchStats.getKills(mvpUid) : 0;
        double maxDmg = mostDmgUid != null ? this.matchStats.getDamage(mostDmgUid) : 0.0;
        for (UUID uid : all) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.sendMessage(header);
            p.sendMessage("\u00a76\u00a7l\u2605 \u8a66\u5408\u7d50\u679c\u30ec\u30dd\u30fc\u30c8");
            p.sendMessage("\u00a77\u7d50\u679c \u00a78\u00bb " + winStr);
            p.sendMessage("\u00a77MVP \u00a78\u00bb \u00a7e" + mvpName + " \u00a77(" + mvpKills + " kill)");
            p.sendMessage("\u00a77\u6700\u591a\u30c0\u30e1\u30fc\u30b8 \u00a78\u00bb \u00a7e" + dmgName + String.format(" \u00a77(%.1f dmg)", maxDmg));
            p.sendMessage(header);
            int myKills = this.matchStats.getKills(uid);
            double myDmg = this.matchStats.getDamage(uid);
            p.sendMessage(String.format("\u00a77\u3042\u306a\u305f\u306e\u6210\u7e3e \u00a78\u00bb \u00a7fKill \u00a7e%d \u00a78| \u00a7fDMG \u00a7e%.1f", myKills, myDmg));
            p.sendMessage(header);
        }
    }

    public void returnAllToLobby() {
        Player p;
        World w;
        this.resetCenterBlocks();
        if (this.currentMap != null && this.currentMap.getWorld() != null && (w = Bukkit.getWorld((String)this.currentMap.getWorld())) != null) {
            Material m;
            for (MapConfig.DomPoint dp : this.currentMap.getDominationPoints()) {
                if (dp.getCenter().getBlock().getType() != Material.BEACON) continue;
                dp.getCenter().getBlock().setType(Material.AIR);
            }
            if (this.currentMap.getRedFlagLocation() != null && this.currentMap.getRedFlagLocation().getBlock().getType() == Material.RED_BANNER) {
                this.currentMap.getRedFlagLocation().getBlock().setType(Material.AIR);
            }
            if (this.currentMap.getBlueFlagLocation() != null && ((m = this.currentMap.getBlueFlagLocation().getBlock().getType()) == Material.CYAN_BANNER || m == Material.BLUE_BANNER)) {
                this.currentMap.getBlueFlagLocation().getBlock().setType(Material.AIR);
            }
            if (this.bombLoc != null && this.bombLoc.getBlock().getType() == Material.TNT) {
                this.bombLoc.getBlock().setType(Material.AIR);
            }
        }
        Location lobbySpawn = this.plugin.getLobbyManager().getLobbySpawn();
        for (UUID uid : this.getAllParticipantsAndSpectators()) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.fullyRestorePlayer(p, lobbySpawn);
        }
        for (UUID uid : new HashSet<UUID>(this.spectators)) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.fullyRestorePlayer(p, lobbySpawn);
        }
        this.reset();
        this.plugin.getLobbyManager().onGameEnd();
    }

    private void fullyRestorePlayer(Player p, Location lobby) {
        p.setGameMode(org.bukkit.GameMode.ADVENTURE);
        p.teleport(lobby);
        p.getInventory().clear();
        this.plugin.getSkillManager().restoreMaxHp(p);
        p.setInvulnerable(false);
        AttributeInstance maxHpAttr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
        p.setHealth(maxHp);
        p.setFoodLevel(20);
        p.setSaturation(5.0f);
        p.setExhaustion(0.0f);
        for (PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
        p.setFireTicks(0);
        p.setArrowsInBody(0);
        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.1f);
        p.resetTitle();
    }

    private void reset() {
        this.plugin.getSkillManager().resetAll();
        this.currentGameMode = GameMode.BATTLE_ARENA;
        this.state = GameState.WAITING;
        this.redTeam.clear();
        this.blueTeam.clear();
        this.spectators.clear();
        this.kills.clear();
        this.deaths.clear();
        this.selectedKits.clear();
        this.playerKit.clear();
        this.noFallDamage.clear();
        this.deadPlayers.clear();
        this.centralZoneMembers.clear();
        this.currentMap = null;
        this.matchStats = new MatchStats();
        this.currentRound = 0;
        this.roundWinsRed = 0;
        this.roundWinsBlue = 0;
        if (this.selectedBgm != null) {
            this.selectedBgm.stop();
        }
        this.plugin.getBotManager().clearAll();
        if (this.endingTask != null) {
            this.endingTask.cancel();
            this.endingTask = null;
        }
        if (this.tdmTimerTask != null) {
            this.tdmTimerTask.cancel();
            this.tdmTimerTask = null;
        }
        this.tdmKillsRed = 0;
        this.tdmKillsBlue = 0;
        if (this.domTimerTask != null) {
            this.domTimerTask.cancel();
            this.domTimerTask = null;
        }
        if (this.gameTickTask != null) {
            this.gameTickTask.cancel();
            this.gameTickTask = null;
        }
        this.domPointsRed = 0;
        this.domPointsBlue = 0;
        this.redFlagTaken = false;
        this.blueFlagTaken = false;
        this.redFlagCarrier = null;
        this.blueFlagCarrier = null;
        this.bombCleanup();
        this.bombRoundAttackerRed = true;
        this.cancelHoldTimer();
    }

    public void rejoinPlayer(Player p) {
        this.spectators.remove(p.getUniqueId());
        this.kills.putIfAbsent(p.getUniqueId(), 0);
        this.deaths.putIfAbsent(p.getUniqueId(), 0);
    }

    public void forceStop() {
        if (this.state == GameState.WAITING) {
            return;
        }
        if (this.endingTask != null) {
            this.endingTask.cancel();
            this.endingTask = null;
        }
        this.endMatch(null, WinCondition.ELIMINATION);
    }

    public void onPlayerDied(Player victim, Player killer) {
        this.onPlayerDied(victim, killer, killer != null ? killer.getUniqueId() : null);
    }

    public void onPlayerDied(Player victim, Player killer, UUID killerUuid) {
        String deathMsg;
        if (this.state != GameState.IN_GAME) {
            return;
        }
        if (this.deadPlayers.contains(victim.getUniqueId())) {
            return;
        }
        this.deaths.merge(victim.getUniqueId(), 1, Integer::sum);
        this.plugin.getStatsManager().addDeath(victim.getUniqueId());
        this.roundKills.put(victim.getUniqueId(), 0);
        TeamColor vt = this.getTeamOf(victim);
        String victimColor = vt != null ? vt.getColorCode() : "\u00a7f";
        String killerDisplay = null;
        String killerColor = "\u00a7f";
        if (killer != null) {
            this.kills.merge(killer.getUniqueId(), 1, Integer::sum);
            this.plugin.getUltimateManager().onKill(killer);
            this.matchStats.addKill(killer.getUniqueId());
            this.plugin.getStatsManager().addKill(killer.getUniqueId());
            TeamColor kt = this.getTeamOf(killer);
            killerColor = kt != null ? ((TeamColor)((Object)kt)).getColorCode() : "\u00a7f";
            killerDisplay = killer.getName();
            deathMsg = "\u00a7c\u2694 " + killerColor + killer.getName() + " \u00a77\u304c " + victimColor + victim.getName() + " \u00a77\u3092\u6483\u7834";
        } else if (killerUuid != null && this.plugin.getBotManager().getBotTeam(killerUuid) != null) {
            this.matchStats.addKill(killerUuid);
            TeamColor kt = this.plugin.getBotManager().getBotTeam(killerUuid);
            killerColor = kt != null ? ((TeamColor)((Object)kt)).getColorCode() : "\u00a7f";
            killerDisplay = "[BOT]";
            deathMsg = "\u00a7c\u2694 " + killerColor + "[BOT] \u00a77\u304c " + victimColor + victim.getName() + " \u00a77\u3092\u6483\u7834";
        } else {
            deathMsg = "\u00a78\u2620 " + victimColor + victim.getName() + " \u00a77\u306f\u6230\u529b\u55aa\u5931";
        }
        for (UUID uid : this.getAllParticipantsAndSpectators()) {
            Player pp = Bukkit.getPlayer((UUID)uid);
            if (pp == null) continue;
            pp.sendMessage(deathMsg);
        }
        Location deathLoc = victim.getLocation().add(0.0, 1.0, 0.0);
        victim.getWorld().strikeLightningEffect(victim.getLocation());
        victim.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, deathLoc, 4, 0.3, 0.3, 0.3, 0.0);
        victim.getWorld().spawnParticle(Particle.CRIT, deathLoc, 50, 0.5, 0.5, 0.5, 0.5);
        victim.getWorld().spawnParticle(Particle.CRIT_MAGIC, deathLoc, 30, 0.4, 0.4, 0.4, 0.3);
        victim.getWorld().spawnParticle(Particle.SMOKE_LARGE, deathLoc, 15, 0.2, 0.2, 0.2, 0.05);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.2f, 0.8f);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.4f);
        if (killerDisplay != null) {
            AnimatedText.glitch(this.plugin, List.of(victim), "\u2620 YOU DIED", killerColor + killerDisplay + " \u306e\u653b\u6483\u306b\u3088\u308a\u6483\u7834", 10);
        } else {
            AnimatedText.glitch(this.plugin, List.of(victim), "\u2620 YOU DIED", "\u6230\u529b\u55aa\u5931", 10);
        }
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.5f);
        if (killer != null) {
            int rk = this.roundKills.merge(killer.getUniqueId(), 1, Integer::sum);
            String streakTitle = this.getStreakTitle(rk);
            if (rk == 1) {
                AnimatedText.wave(this.plugin, List.of(killer), "\u2694 KILL!", victimColor + victim.getName() + " \u3092\u6483\u7834", 8);
                killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.6f);
                killer.spawnParticle(Particle.VILLAGER_HAPPY, killer.getLocation().add(0.0, 2.0, 0.0), 15, 0.4, 0.4, 0.4, 0.1);
            } else {
                AnimatedText.pop(this.plugin, List.of(killer), streakTitle, "\u00a7e" + rk + " kills \u00a77in a row!", 10);
                killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f + (float)(rk - 2) * 0.15f);
                killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
                killer.spawnParticle(Particle.TOTEM, killer.getLocation().add(0.0, 1.0, 0.0), 60, 0.5, 1.0, 0.5, 0.5);
                killer.spawnParticle(Particle.CRIT, killer.getLocation().add(0.0, 1.0, 0.0), 40, 0.4, 0.4, 0.4, 0.4);
                if (rk >= 3) {
                    ArrayList<Player> watchers = new ArrayList<Player>();
                    for (UUID uid : this.getAllParticipantsAndSpectators()) {
                        Player pp = Bukkit.getPlayer((UUID)uid);
                        if (pp == null) continue;
                        pp.playSound(pp.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.4f);
                        watchers.add(pp);
                    }
                    this.spawnKillStreakFirework(killer, rk);
                    AnimatedText.scroll(this.plugin, watchers, "\u2694 " + killer.getName() + " \u304c " + rk + " \u9023\u7d9a\u30ad\u30eb\u4e2d\uff01", 60);
                }
            }
            if (rk == 5) {
                this.announceBigPlay(killer, rk);
            }
        }
        if (this.currentGameMode == GameMode.TEAM_DEATHMATCH || this.currentGameMode == GameMode.DOMINATION || this.currentGameMode == GameMode.CAPTURE_THE_FLAG) {
            this.plugin.getSkillManager().clearPlayerPlacements(victim.getUniqueId());
            if (this.currentGameMode == GameMode.TEAM_DEATHMATCH) {
                TeamColor kt2;
                TeamColor teamColor = kt2 = killer != null ? this.getTeamOf(killer) : null;
                if (killerUuid != null && this.plugin.getBotManager().getBotTeam(killerUuid) != null) {
                    kt2 = this.plugin.getBotManager().getBotTeam(killerUuid);
                }
                if (kt2 == null) {
                    TeamColor vc = this.getTeamOf(victim);
                    if (vc == TeamColor.RED) {
                        ++this.tdmKillsBlue;
                    } else if (vc == TeamColor.BLUE) {
                        ++this.tdmKillsRed;
                    }
                } else if (kt2 == TeamColor.RED) {
                    ++this.tdmKillsRed;
                } else if (kt2 == TeamColor.BLUE) {
                    ++this.tdmKillsBlue;
                }
                int target = this.plugin.getConfig().getInt("team_deathmatch.target_kills", 30);
                if (this.tdmKillsRed >= target) {
                    this.endGame(TeamColor.RED, WinCondition.ELIMINATION);
                    return;
                }
                if (this.tdmKillsBlue >= target) {
                    this.endGame(TeamColor.BLUE, WinCondition.ELIMINATION);
                    return;
                }
            }
            Player finalV = victim;
            TeamColor vTeam = this.getTeamOf(finalV);
            this.addSpectator(finalV);
            long respawnDelay = 60L;
            if (this.currentGameMode == GameMode.CAPTURE_THE_FLAG || this.currentGameMode == GameMode.DOMINATION) {
                long baseRespawn;
                double ratio;
                int enemySize;
                int ownSize = vTeam == TeamColor.RED ? this.ctfRedTeamSize : this.ctfBlueTeamSize;
                int n = enemySize = vTeam == TeamColor.RED ? this.ctfBlueTeamSize : this.ctfRedTeamSize;
                if (enemySize == 0) {
                    enemySize = 1;
                }
                if ((respawnDelay = (long)((ratio = (double)ownSize / (double)enemySize) * (double)(baseRespawn = this.currentGameMode == GameMode.CAPTURE_THE_FLAG ? 100L : 80L))) > 200L) {
                    respawnDelay = 200L;
                }
                if (respawnDelay < 40L) {
                    respawnDelay = 40L;
                }
            }
            finalV.sendMessage("\u00a77\u30ea\u30b9\u30dd\u30fc\u30f3 \u00a78\u00bb \u00a7e" + respawnDelay / 20L + " \u00a7f\u79d2");
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (finalV.isOnline() && this.state == GameState.IN_GAME) {
                    this.roundRestorePlayer(finalV);
                    finalV.setGameMode(org.bukkit.GameMode.SURVIVAL);
                    try {
                        finalV.spigot().respawn();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    KitType kt2 = this.getPlayerKits().get(finalV.getUniqueId());
                    if (kt2 != null && vTeam != null) {
                        KitBuilder.giveKit(finalV, kt2, vTeam, this.plugin);
                    }
                    if (vTeam != null && this.currentMap != null) {
                        this.teleportToSpawnZonePublic(finalV, this.currentMap, vTeam);
                    }
                    this.plugin.getSkillManager().refreshBurst(finalV);
                    finalV.sendMessage("\u00a7a\u26a1 \u30ea\u30b9\u30dd\u30fc\u30f3\uff01");
                }
            }, respawnDelay);
        } else {
            this.deadPlayers.add(victim.getUniqueId());
            if (this.currentGameMode == GameMode.BATTLE_ARENA || this.currentGameMode == GameMode.BOMB_MISSION) {
                this.plugin.getSkillManager().clearPlayerPlacements(victim.getUniqueId());
            }
            Player finalVictim = victim;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (this.deadPlayers.contains(finalVictim.getUniqueId())) {
                    this.addSpectator(finalVictim);
                }
            }, 1L);
        }
        if (this.currentGameMode == GameMode.CAPTURE_THE_FLAG) {
            this.dropFlag(victim.getUniqueId());
        }
        if (this.currentGameMode == GameMode.BOMB_MISSION && this.bombDefusing && victim.equals((Object)this.bombDefuser)) {
            this.bombDefusing = false;
            this.bombDefuser = null;
            Bukkit.broadcastMessage((String)"\u00a7c\u26a0 \u89e3\u9664\u4f5c\u696d\u4e2d\u65ad \u00a78\u00bb \u00a77\u89e3\u9664\u8005\u304c\u6b7b\u4ea1");
        }
        if (!(this.currentGameMode == GameMode.BOMB_MISSION && this.bombPlanted || this.currentGameMode == GameMode.DOMINATION || this.currentGameMode == GameMode.CAPTURE_THE_FLAG)) {
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, this::checkEliminationWin, 1L);
        }
    }

    private String getStreakTitle(int kills) {
        return switch (kills) {
            case 2 -> "\u00a7e\u00a7l\u2726 DOUBLE KILL";
            case 3 -> "\u00a76\u00a7l\u2726 TRIPLE KILL \u2726";
            case 4 -> "\u00a7c\u00a7l\u26a1 QUADRA KILL";
            case 5 -> "\u00a74\u00a7l\u2605 PENTA KILL \u2605";
            default -> "\u00a74\u00a7l\ud83d\udc80 RAMPAGE \ud83d\udc80";
        };
    }

    private void spawnKillStreakFirework(Player killer, int streak) {
        Color color = switch (streak) {
            case 3 -> Color.YELLOW;
            case 4 -> Color.ORANGE;
            case 5 -> Color.RED;
            default -> Color.fromRGB((int)148, (int)0, (int)211);
        };
        Location loc = killer.getLocation().add(0.0, 2.0, 0.0);
        int count = Math.min(streak - 2, 4);
        for (int i = 0; i < count; ++i) {
            int delay = i * 3;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (!killer.isOnline()) {
                    return;
                }
                Firework fw = (Firework)killer.getWorld().spawnEntity(loc.clone().add((Math.random() - 0.5) * 2.0, 0.0, (Math.random() - 0.5) * 2.0), EntityType.FIREWORK);
                FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(new Color[]{color, Color.WHITE}).withFade(Color.YELLOW).withFlicker().withTrail().build();
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(effect);
                meta.setPower(0);
                fw.setFireworkMeta(meta);
                fw.detonate();
            }, (long)delay);
        }
    }

    public void onBotDied(UUID botUuid, Player killer) {
        if (this.state != GameState.IN_GAME) {
            return;
        }
        if (killer != null) {
            this.kills.merge(killer.getUniqueId(), 1, Integer::sum);
            this.matchStats.addKill(killer.getUniqueId());
            this.plugin.getStatsManager().addKill(killer.getUniqueId());
            TeamColor bt = this.plugin.getBotManager().getBotTeam(botUuid);
            TeamColor kt = this.getTeamOf(killer);
            String botColor = bt != null ? bt.getColorCode() : "\u00a7f";
            String killerColor = kt != null ? kt.getColorCode() : "\u00a7f";
            String msg = "\u00a7c\u2694 " + botColor + "[BOT] \u00a77\u304c " + killerColor + killer.getName() + " \u00a77\u306b\u6483\u7834\u3055\u308c\u305f\uff01";
            for (UUID uid : this.getAllParticipantsAndSpectators()) {
                Player p = Bukkit.getPlayer((UUID)uid);
                if (p == null) continue;
                p.sendMessage(msg);
            }
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, this::checkEliminationWin, 1L);
    }

    public boolean isKitTakenInTeam(UUID playerUid, String kitName) {
        List<UUID> team = this.getTeamOf(playerUid);
        for (UUID uid : team) {
            if (!kitName.equals(this.playerKit.get(uid))) continue;
            return true;
        }
        return false;
    }

    public void setPlayerKit(UUID uid, String kitName) {
        this.playerKit.put(uid, kitName);
    }

    public String getPlayerKit(UUID uid) {
        return this.playerKit.get(uid);
    }

    private void broadcastBombRoundInfo() {
        TeamColor attacker = this.bombRoundAttackerRed ? TeamColor.RED : TeamColor.BLUE;
        for (UUID uid : this.getAllParticipantsAndSpectators()) {
            Player pl = Bukkit.getPlayer((UUID)uid);
            if (pl == null) continue;
            String role = this.getTeamOf(pl) == attacker ? "\u00a7c\u2694 \u653b\u6483\u5074 \u00a78\u00bb \u00a7f\u7206\u5f3e\u3092\u8a2d\u7f6e\u305b\u3088\uff01" : "\u00a79\u2694 \u5b88\u5099\u5074 \u00a78\u00bb \u00a7f\u8a2d\u7f6e\u3092\u963b\u6b62\u305b\u3088\uff01";
            AnimatedText.wave(this.plugin, List.of(pl), attacker.getColorCode() + "\u2605 \u30e9\u30a6\u30f3\u30c9 " + this.currentRound, role, 10);
        }
    }

    public void tryPlantBomb(final Player p) {
        TeamColor attacker;
        if (this.currentGameMode != GameMode.BOMB_MISSION) {
            return;
        }
        if (this.state != GameState.IN_GAME) {
            return;
        }
        if (this.bombPlanted) {
            return;
        }
        if (this.currentMap == null || this.currentMap.getBombSite() == null) {
            return;
        }
        TeamColor teamColor = attacker = this.bombRoundAttackerRed ? TeamColor.RED : TeamColor.BLUE;
        if (this.getTeamOf(p) != attacker) {
            p.sendMessage("\u00a7c\u2694 \u3042\u306a\u305f\u306f\u653b\u6483\u5074\u3067\u306f\u306a\u3044");
            return;
        }
        if (p.getLocation().distance(this.currentMap.getBombSite()) > 3.0) {
            if (p.getLocation().distance(this.currentMap.getBombSite()) < 10.0) {
                p.sendMessage("\u00a7c\ud83d\udca3 \u7206\u5f3e\u8a2d\u7f6e\u5730\u70b9\u306b\u8fd1\u3065\u3044\u3066\u304f\u3060\u3055\u3044");
            }
            return;
        }
        final int plantTime = this.plugin.getConfig().getInt("bomb_mission.plant_time_seconds", 5);
        p.sendMessage("\u00a7c\u00a7l\u7206\u5f3e\u8a2d\u7f6e\u4e2d... \u00a7e" + plantTime + "\u79d2");
        this.bombPlanted = true;
        this.bombLoc = this.currentMap.getBombSite().clone();
        new BukkitRunnable(){
            int progress;
            {
                this.progress = plantTime;
            }

            public void run() {
                if (!p.isOnline() || GameManager.this.state != GameState.IN_GAME || p.getLocation().distance(GameManager.this.bombLoc) > 3.0) {
                    p.sendMessage("\u00a7c\u26a0 \u7206\u5f3e\u8a2d\u7f6e\u304c\u4e2d\u65ad\u3055\u308c\u305f\uff01");
                    GameManager.this.bombPlanted = false;
                    GameManager.this.bombLoc = null;
                    this.cancel();
                    return;
                }
                p.sendActionBar((Component)Component.text((String)("\u00a7c\u00a7l\u8a2d\u7f6e\u4e2d... \u00a7e" + this.progress + "\u79d2")));
                p.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, p.getLocation().add(0.0, 1.5, 0.0), 3, 0.3, 0.3, 0.3, 0.0);
                --this.progress;
                if (this.progress <= 0) {
                    this.cancel();
                    GameManager.this.bombArmed(p);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    private void bombArmed(Player planter) {
        int fuse;
        this.bombSecondsRemaining = fuse = this.plugin.getConfig().getInt("bomb_mission.bomb_fuse_seconds", 45);
        Bukkit.broadcastMessage((String)("\u00a7c\u00a7l\ud83d\udca3 \u7206\u5f3e\u8a2d\u7f6e\u5b8c\u4e86 \u00a78\u00bb \u00a7e" + fuse + " \u79d2\u5f8c\u7206\u767a\uff01"));
        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 0.6f);
        }
        if (this.bombLoc != null && this.bombLoc.getWorld() != null) {
            this.bombLoc.getBlock().setType(Material.TNT);
        }
        this.bombTimerTask = new BukkitRunnable(){

            public void run() {
                TeamColor defender;
                if (GameManager.this.state != GameState.IN_GAME) {
                    this.cancel();
                    return;
                }
                TeamColor attacker = GameManager.this.bombRoundAttackerRed ? TeamColor.RED : TeamColor.BLUE;
                TeamColor teamColor = defender = GameManager.this.bombRoundAttackerRed ? TeamColor.BLUE : TeamColor.RED;
                if (GameManager.this.getAliveCount(defender) == 0) {
                    this.cancel();
                    GameManager.this.bombCleanup();
                    Bukkit.broadcastMessage((String)("\u00a7c\u00a7l\u2694 \u9632\u885b\u5074\u5168\u6ec5 \u00a78\u00bb " + attacker.getDisplayName() + " \u00a7f\u30c1\u30fc\u30e0\u52dd\u5229\uff01"));
                    GameManager.this.endGame(attacker, WinCondition.OBJECTIVE);
                    return;
                }
                --GameManager.this.bombSecondsRemaining;
                if (GameManager.this.bombSecondsRemaining <= 0) {
                    this.cancel();
                    GameManager.this.bombExplode();
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    private void bombExplode() {
        if (this.bombLoc != null && this.bombLoc.getWorld() != null) {
            this.bombLoc.getWorld().createExplosion(this.bombLoc, 8.0f, false, true);
            this.bombLoc.getBlock().setType(Material.AIR);
        }
        this.bombCleanup();
        TeamColor attacker = this.bombRoundAttackerRed ? TeamColor.RED : TeamColor.BLUE;
        Bukkit.broadcastMessage((String)("\u00a7c\u00a7l\ud83d\udca5 \u7206\u767a \u00a78\u00bb " + attacker.getDisplayName() + " \u00a7f\u30c1\u30fc\u30e0\u52dd\u5229\uff01"));
        this.endGame(attacker, WinCondition.OBJECTIVE);
    }

    public void tryDefuseBomb(final Player p) {
        TeamColor defender;
        if (this.currentGameMode != GameMode.BOMB_MISSION) {
            return;
        }
        if (this.state != GameState.IN_GAME) {
            return;
        }
        if (!this.bombPlanted || this.bombLoc == null) {
            return;
        }
        TeamColor teamColor = defender = this.bombRoundAttackerRed ? TeamColor.BLUE : TeamColor.RED;
        if (this.getTeamOf(p) != defender) {
            p.sendMessage("\u00a7c\u2694 \u3042\u306a\u305f\u306f\u5b88\u5099\u5074\u3067\u306f\u306a\u3044");
            return;
        }
        if (p.getLocation().distance(this.bombLoc) > 3.0) {
            if (p.getLocation().distance(this.bombLoc) < 10.0) {
                p.sendMessage("\u00a7c\ud83d\udca3 \u7206\u5f3e\u306b\u8fd1\u3065\u3044\u3066\u304f\u3060\u3055\u3044");
            }
            return;
        }
        if (p.getInventory().getItemInOffHand().getType() == Material.SHIELD) {
            p.sendMessage("\u00a7c\ud83d\udee1 \u76fe\u6301\u3061\u4e0d\u53ef \u00a78\u00bb \u00a77\u76fe\u3092\u5916\u3057\u3066\u89e3\u9664\u305b\u3088");
            return;
        }
        final int defuseTime = this.plugin.getConfig().getInt("bomb_mission.defuse_time_seconds", 7);
        this.bombDefusing = true;
        this.bombDefuser = p;
        p.sendMessage("\u00a7a\u00a7l\u89e3\u9664\u4e2d... \u00a7e" + defuseTime + "\u79d2");
        new BukkitRunnable(){
            int progress;
            {
                this.progress = defuseTime;
            }

            public void run() {
                if (!p.isOnline() || GameManager.this.state != GameState.IN_GAME || p.getLocation().distance(GameManager.this.bombLoc) > 3.0 || p.getInventory().getItemInOffHand().getType() == Material.SHIELD) {
                    p.sendMessage("\u00a7c\u26a0 \u89e3\u9664\u4f5c\u696d\u304c\u4e2d\u65ad\u3055\u308c\u305f\uff01");
                    GameManager.this.bombDefusing = false;
                    GameManager.this.bombDefuser = null;
                    this.cancel();
                    return;
                }
                p.sendActionBar((Component)Component.text((String)("\u00a7a\u00a7l\u89e3\u9664\u4e2d... \u00a7e" + this.progress + "\u79d2")));
                --this.progress;
                if (this.progress <= 0) {
                    this.cancel();
                    GameManager.this.bombDefused(p);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    private void bombDefused(Player defuser) {
        TeamColor defender = this.bombRoundAttackerRed ? TeamColor.BLUE : TeamColor.RED;
        Bukkit.broadcastMessage((String)("\u00a7a\u00a7l\ud83d\udee1 \u89e3\u9664\u6210\u529f \u00a78\u00bb " + defender.getDisplayName() + " \u00a7f\u30c1\u30fc\u30e0\u52dd\u5229\uff01"));
        if (this.bombLoc != null && this.bombLoc.getWorld() != null) {
            this.bombLoc.getBlock().setType(Material.AIR);
        }
        this.bombCleanup();
        this.endGame(defender, WinCondition.OBJECTIVE);
    }

    private void bombCleanup() {
        if (this.bombLoc != null && this.bombLoc.getWorld() != null) {
            this.bombLoc.getBlock().setType(Material.AIR);
        }
        this.bombPlanted = false;
        this.bombLoc = null;
        this.bombDefusing = false;
        this.bombDefuser = null;
        if (this.bombTimerTask != null) {
            this.bombTimerTask.cancel();
            this.bombTimerTask = null;
        }
        if (this.bombRoundTimerTask != null) {
            this.bombRoundTimerTask.cancel();
            this.bombRoundTimerTask = null;
        }
    }

    public boolean isBombPlanted() {
        return this.bombPlanted;
    }

    public long getInGameStartTime() {
        return this.inGameStartTime;
    }

    public UUID getBombDefuserUuid() {
        return this.bombDefuser != null ? this.bombDefuser.getUniqueId() : null;
    }

    public int getBombSecondsRemaining() {
        return this.bombSecondsRemaining;
    }

    public boolean isFlagCarrier(UUID uid) {
        return uid.equals(this.redFlagCarrier) || uid.equals(this.blueFlagCarrier);
    }

    public Location getBombLoc() {
        return this.bombLoc;
    }

    public TeamColor getTeamOf(Player p) {
        UUID uid = p.getUniqueId();
        if (this.redTeam.contains(uid)) {
            return TeamColor.RED;
        }
        if (this.blueTeam.contains(uid)) {
            return TeamColor.BLUE;
        }
        return null;
    }

    public TeamColor getTeam(UUID uid) {
        if (this.redTeam.contains(uid)) {
            return TeamColor.RED;
        }
        if (this.blueTeam.contains(uid)) {
            return TeamColor.BLUE;
        }
        return null;
    }

    public TeamColor getKitTeam(UUID uid) {
        if (this.redTeam.contains(uid)) {
            return TeamColor.RED;
        }
        if (this.blueTeam.contains(uid)) {
            return TeamColor.BLUE;
        }
        return null;
    }

    public List<UUID> getTeamOf(UUID uid) {
        if (this.redTeam.contains(uid)) {
            return this.redTeam;
        }
        if (this.blueTeam.contains(uid)) {
            return this.blueTeam;
        }
        return Collections.emptyList();
    }

    public boolean isParticipant(Player p) {
        UUID uid = p.getUniqueId();
        return this.redTeam.contains(uid) || this.blueTeam.contains(uid);
    }

    public boolean isSpectator(Player p) {
        return this.spectators.contains(p.getUniqueId());
    }

    public void addSpectator(Player p) {
        this.spectators.add(p.getUniqueId());
        p.setGameMode(org.bukkit.GameMode.SPECTATOR);
        if (this.currentMap != null && this.currentMap.getCenter() != null) {
            p.teleport(this.currentMap.getCenter().clone().add(0.0, 3.0, 0.0));
        }
    }

    public boolean isInCenterZone(Block block) {
        if (this.currentMap == null) {
            return false;
        }
        Location center = this.currentMap.getCenter();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();
        return by == cy && bx >= cx - 2 && bx <= cx + 2 && bz >= cz - 2 && bz <= cz + 2;
    }

    public boolean hasNoFallDamage(Player p) {
        return this.noFallDamage.remove(p.getUniqueId());
    }

    public void setNoFallDamage(UUID uid) {
        this.noFallDamage.add(uid);
    }

    public void clearNoFallDamage(UUID uid) {
        this.noFallDamage.remove(uid);
    }

    private List<UUID> getAllParticipants() {
        ArrayList<UUID> all = new ArrayList<UUID>(this.redTeam);
        all.addAll(this.blueTeam);
        return all;
    }

    private List<UUID> getAllParticipantsAndSpectators() {
        List<UUID> all = this.getAllParticipants();
        all.addAll(this.spectators);
        return all;
    }

    private void broadcastTitle(String title, String sub, int fadeIn, int stay, int fadeOut) {
        for (UUID uid : this.getAllParticipantsAndSpectators()) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.sendTitle(title, sub, fadeIn, stay, fadeOut);
        }
    }

    private void broadcastSound(Sound sound, float volume, float pitch) {
        for (UUID uid : this.getAllParticipantsAndSpectators()) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }

    private void broadcastActionBar(String msg) {
        for (UUID uid : this.getAllParticipantsAndSpectators()) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.sendActionBar((Component)Component.text((String)msg));
        }
    }

    private String formatTime(long seconds) {
        return String.format("%d:%02d", seconds / 60L, seconds % 60L);
    }

    private void updateCTF() {
        Vector v;
        Boolean prevOnGround;
        Player carrier;
        if (!this.redFlagTaken && this.redFlagDropTime > 0L && System.currentTimeMillis() - this.redFlagDropTime > 30000L) {
            this.resetRedFlag();
            Bukkit.broadcastMessage((String)"\u00a7c\ud83c\udff4 \u8d64\u306e\u65d7 \u00a78\u00bb \u00a77\u81ea\u52d5\u56de\u53ce\u3055\u308c\u305f");
        }
        if (!this.blueFlagTaken && this.blueFlagDropTime > 0L && System.currentTimeMillis() - this.blueFlagDropTime > 30000L) {
            this.resetBlueFlag();
            Bukkit.broadcastMessage((String)"\u00a79\ud83c\udff4 \u9752\u306e\u65d7 \u00a78\u00bb \u00a77\u81ea\u52d5\u56de\u53ce\u3055\u308c\u305f");
        }
        this.ctfCarrierOnGround.keySet().removeIf(uuid -> !uuid.equals(this.redFlagCarrier) && !uuid.equals(this.blueFlagCarrier));
        if (this.redFlagCarrier != null && (carrier = Bukkit.getPlayer((UUID)this.redFlagCarrier)) != null && carrier.isOnline()) {
            carrier.getWorld().spawnParticle(Particle.REDSTONE, carrier.getLocation().add(0.0, 2.5, 0.0), 5, 0.3, 0.5, 0.3, (Object)new Particle.DustOptions(Color.BLUE, 1.5f));
            carrier.sendActionBar((Component)Component.text((String)"\u00a79\ud83c\udff4 \u9752\u65d7\u6240\u6301 \u00a78\u00bb \u00a7f\u81ea\u9663\u306b\u6301\u3061\u5e30\u308c\uff01"));
            prevOnGround = this.ctfCarrierOnGround.get(this.redFlagCarrier);
            if (prevOnGround != null && prevOnGround.booleanValue() && !carrier.isOnGround()) {
                v = carrier.getVelocity();
                carrier.setVelocity(new Vector(0.0, v.getY(), 0.0));
            }
            this.ctfCarrierOnGround.put(this.redFlagCarrier, carrier.isOnGround());
            if (this.ctfRedTeamSize < this.ctfBlueTeamSize) {
                carrier.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 0, true, false));
                if (this.ctfRedTeamSize == 1) {
                    carrier.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 0, true, false));
                }
            }
        }
        if (this.blueFlagCarrier != null && (carrier = Bukkit.getPlayer((UUID)this.blueFlagCarrier)) != null && carrier.isOnline()) {
            carrier.getWorld().spawnParticle(Particle.REDSTONE, carrier.getLocation().add(0.0, 2.5, 0.0), 5, 0.3, 0.5, 0.3, (Object)new Particle.DustOptions(Color.RED, 1.5f));
            carrier.sendActionBar((Component)Component.text((String)"\u00a7c\ud83c\udff4 \u8d64\u65d7\u6240\u6301 \u00a78\u00bb \u00a7f\u81ea\u9663\u306b\u6301\u3061\u5e30\u308c\uff01"));
            prevOnGround = this.ctfCarrierOnGround.get(this.blueFlagCarrier);
            if (prevOnGround != null && prevOnGround.booleanValue() && !carrier.isOnGround()) {
                v = carrier.getVelocity();
                carrier.setVelocity(new Vector(0.0, v.getY(), 0.0));
            }
            this.ctfCarrierOnGround.put(this.blueFlagCarrier, carrier.isOnGround());
            if (this.ctfBlueTeamSize < this.ctfRedTeamSize) {
                carrier.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 0, true, false));
                if (this.ctfBlueTeamSize == 1) {
                    carrier.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 0, true, false));
                }
            }
        }
    }

    public void tryPickupFlag(Player p) {
        if (this.currentGameMode != GameMode.CAPTURE_THE_FLAG) {
            return;
        }
        if (this.state != GameState.IN_GAME) {
            return;
        }
        if (System.currentTimeMillis() - this.inGameStartTime < 10000L) {
            return;
        }
        if (this.currentMap == null) {
            return;
        }
        TeamColor team = this.getTeamOf(p);
        if (team == null) {
            return;
        }
        Long pickupCd = this.ctfPickupCooldown.get(p.getUniqueId());
        if (pickupCd != null && System.currentTimeMillis() - pickupCd < 5000L) {
            return;
        }
        if (team == TeamColor.RED && !this.redFlagTaken && this.currentMap.getRedFlagLocation() != null && p.getLocation().distance(this.currentMap.getRedFlagLocation()) < 2.0) {
            this.redFlagTaken = true;
            this.redFlagCarrier = p.getUniqueId();
            this.redFlagDropTime = -1L;
            if (this.currentMap.getRedFlagLocation().getBlock().getType() == Material.RED_BANNER) {
                this.currentMap.getRedFlagLocation().getBlock().setType(Material.AIR);
            }
            p.sendMessage("\u00a7c\ud83c\udff4 \u8d64\u65d7\u596a\u53d6\uff01 \u00a77\u81ea\u9663\u306b\u6301\u3061\u5e30\u308c\uff01");
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        }
        if (team == TeamColor.BLUE && !this.blueFlagTaken && this.currentMap.getBlueFlagLocation() != null && p.getLocation().distance(this.currentMap.getBlueFlagLocation()) < 2.0) {
            this.blueFlagTaken = true;
            this.blueFlagCarrier = p.getUniqueId();
            this.blueFlagDropTime = -1L;
            if (this.currentMap.getBlueFlagLocation().getBlock().getType() == Material.CYAN_BANNER) {
                this.currentMap.getBlueFlagLocation().getBlock().setType(Material.AIR);
            }
            p.sendMessage("\u00a79\ud83c\udff4 \u9752\u65d7\u596a\u53d6\uff01 \u00a77\u81ea\u9663\u306b\u6301\u3061\u5e30\u308c\uff01");
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        }
        if (team == TeamColor.RED && this.redFlagCarrier != null && this.redFlagCarrier.equals(p.getUniqueId()) && this.currentMap.getRedReturnLocation() != null && p.getLocation().distance(this.currentMap.getRedReturnLocation()) < 3.0) {
            this.captureFlag(TeamColor.RED);
            return;
        }
        if (team == TeamColor.BLUE && this.blueFlagCarrier != null && this.blueFlagCarrier.equals(p.getUniqueId()) && this.currentMap.getBlueReturnLocation() != null && p.getLocation().distance(this.currentMap.getBlueReturnLocation()) < 3.0) {
            this.captureFlag(TeamColor.BLUE);
            return;
        }
    }

    public void tryPickupDroppedFlag(Player p) {
        if (this.currentGameMode != GameMode.CAPTURE_THE_FLAG) {
            return;
        }
        if (this.state != GameState.IN_GAME) {
            return;
        }
        TeamColor team = this.getTeamOf(p);
        if (team == null) {
            return;
        }
        Location loc = p.getLocation();
        if (team == TeamColor.RED && !this.redFlagTaken && this.redFlagDropTime > 0L && this.redFlagDropLoc != null && loc.distance(this.redFlagDropLoc) < 2.0) {
            this.redFlagCarrier = p.getUniqueId();
            this.redFlagTaken = true;
            this.redFlagDropTime = -1L;
            if (this.redFlagDropLoc != null) {
                this.redFlagDropLoc.getBlock().setType(Material.AIR);
                this.redFlagDropLoc = null;
            }
            p.sendMessage("\u00a7c\ud83c\udff4 \u8d64\u65d7\u56de\u53ce \u00a78\u00bb \u00a77\u81ea\u9663\u306b\u6301\u3061\u5e30\u308c\uff01");
        }
        if (team == TeamColor.BLUE && !this.blueFlagTaken && this.blueFlagDropTime > 0L && this.blueFlagDropLoc != null && loc.distance(this.blueFlagDropLoc) < 2.0) {
            this.blueFlagCarrier = p.getUniqueId();
            this.blueFlagTaken = true;
            this.blueFlagDropTime = -1L;
            if (this.blueFlagDropLoc != null) {
                this.blueFlagDropLoc.getBlock().setType(Material.AIR);
                this.blueFlagDropLoc = null;
            }
            p.sendMessage("\u00a79\ud83c\udff4 \u9752\u65d7\u56de\u53ce \u00a78\u00bb \u00a77\u81ea\u9663\u306b\u6301\u3061\u5e30\u308c\uff01");
        }
    }

    private void captureFlag(TeamColor team) {
        if (team == TeamColor.RED) {
            ++this.ctfRedCaptures;
            if (this.redFlagCarrier != null) {
                this.ctfPickupCooldown.put(this.redFlagCarrier, System.currentTimeMillis());
            }
            this.redFlagCarrier = null;
            this.redFlagTaken = false;
            this.resetRedFlag();
            if (this.blueFlagCarrier != null) {
                Player bc = Bukkit.getPlayer((UUID)this.blueFlagCarrier);
                if (bc != null) {
                    bc.sendMessage("\u00a79\ud83c\udff4 \u76f8\u624b\u304c\u65d7\u596a\u53d6 \u00a78\u00bb \u00a77\u9752\u65d7\u304c\u30ea\u30bb\u30c3\u30c8\u3055\u308c\u305f");
                }
                this.resetBlueFlag();
            }
            Bukkit.broadcastMessage((String)("\u00a7c\u00a7l\ud83d\udea9 \u8d64\u30c1\u30fc\u30e0\u304c\u65d7\u3092\u596a\u53d6\uff01 \u00a78(" + this.ctfRedCaptures + "/" + this.plugin.getConfig().getInt("capture_the_flag.captures_to_win", 3) + ")"));
        } else {
            ++this.ctfBlueCaptures;
            if (this.blueFlagCarrier != null) {
                this.ctfPickupCooldown.put(this.blueFlagCarrier, System.currentTimeMillis());
            }
            this.blueFlagCarrier = null;
            this.blueFlagTaken = false;
            this.resetBlueFlag();
            if (this.redFlagCarrier != null) {
                Player rc = Bukkit.getPlayer((UUID)this.redFlagCarrier);
                if (rc != null) {
                    rc.sendMessage("\u00a7c\ud83c\udff4 \u76f8\u624b\u304c\u65d7\u596a\u53d6 \u00a78\u00bb \u00a77\u8d64\u65d7\u304c\u30ea\u30bb\u30c3\u30c8\u3055\u308c\u305f");
                }
                this.resetRedFlag();
            }
            Bukkit.broadcastMessage((String)("\u00a79\u00a7l\ud83d\udea9 \u9752\u30c1\u30fc\u30e0\u304c\u65d7\u3092\u596a\u53d6\uff01 \u00a78(" + this.ctfBlueCaptures + "/" + this.plugin.getConfig().getInt("capture_the_flag.captures_to_win", 3) + ")"));
        }
        int toWin = this.plugin.getConfig().getInt("capture_the_flag.captures_to_win", 3);
        if (this.ctfRedCaptures >= toWin) {
            this.endGame(TeamColor.RED, WinCondition.OBJECTIVE);
        } else if (this.ctfBlueCaptures >= toWin) {
            this.endGame(TeamColor.BLUE, WinCondition.OBJECTIVE);
        }
        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(pl.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    private void dropFlag(UUID carrierUuid) {
        Location dropLoc;
        Player p;
        if (carrierUuid.equals(this.redFlagCarrier)) {
            this.redFlagCarrier = null;
            this.redFlagTaken = false;
            this.redFlagDropTime = System.currentTimeMillis();
            p = Bukkit.getPlayer((UUID)carrierUuid);
            if (p != null && this.currentMap != null) {
                dropLoc = this.findAirAbove(p.getLocation());
                dropLoc.getBlock().setType(Material.RED_BANNER);
                this.redFlagDropLoc = dropLoc.clone();
                p.sendMessage("\u00a7c\ud83c\udff4 \u8d64\u65d7\u843d\u4e0b \u00a78\u00bb \u00a77\u65d7\u304c\u6226\u5834\u306b\u843d\u3061\u3066\u3044\u308b\uff01");
            }
        }
        if (carrierUuid.equals(this.blueFlagCarrier)) {
            this.blueFlagCarrier = null;
            this.blueFlagTaken = false;
            this.blueFlagDropTime = System.currentTimeMillis();
            p = Bukkit.getPlayer((UUID)carrierUuid);
            if (p != null && this.currentMap != null) {
                dropLoc = this.findAirAbove(p.getLocation());
                dropLoc.getBlock().setType(Material.CYAN_BANNER);
                this.blueFlagDropLoc = dropLoc.clone();
                p.sendMessage("\u00a79\ud83c\udff4 \u9752\u65d7\u843d\u4e0b \u00a78\u00bb \u00a77\u65d7\u304c\u6226\u5834\u306b\u843d\u3061\u3066\u3044\u308b\uff01");
            }
        }
    }

    private Location findAirAbove(Location base) {
        Location loc = base.clone().add(0.0, 1.0, 0.0);
        while (loc.getBlock().getType().isSolid() && loc.getY() < (double)(loc.getWorld().getMaxHeight() - 1)) {
            loc = loc.add(0.0, 1.0, 0.0);
        }
        return loc;
    }

    private void resetRedFlag() {
        this.redFlagTaken = false;
        this.redFlagCarrier = null;
        this.redFlagDropTime = -1L;
        if (this.redFlagDropLoc != null) {
            this.redFlagDropLoc.getBlock().setType(Material.AIR);
            this.redFlagDropLoc = null;
        }
        if (this.ctfRedFlagSpawn != null) {
            this.ctfRedFlagSpawn.getBlock().setType(Material.RED_BANNER);
        } else if (this.currentMap != null && this.currentMap.getRedFlagLocation() != null) {
            this.currentMap.getRedFlagLocation().getBlock().setType(Material.RED_BANNER);
        }
    }

    private void resetBlueFlag() {
        this.blueFlagTaken = false;
        this.blueFlagCarrier = null;
        this.blueFlagDropTime = -1L;
        if (this.blueFlagDropLoc != null) {
            this.blueFlagDropLoc.getBlock().setType(Material.AIR);
            this.blueFlagDropLoc = null;
        }
        if (this.ctfBlueFlagSpawn != null) {
            this.ctfBlueFlagSpawn.getBlock().setType(Material.CYAN_BANNER);
        } else if (this.currentMap != null && this.currentMap.getBlueFlagLocation() != null) {
            this.currentMap.getBlueFlagLocation().getBlock().setType(Material.CYAN_BANNER);
        }
    }

    public GameState getState() {
        return this.state;
    }

    public MapConfig getCurrentMap() {
        return this.currentMap;
    }

    public int getCenterY() {
        if (this.currentMap == null || this.currentMap.getCenter() == null) {
            return 0;
        }
        return this.currentMap.getCenter().getBlockY();
    }

    public List<UUID> getRedTeam() {
        return this.redTeam;
    }

    public List<UUID> getBlueTeam() {
        return this.blueTeam;
    }

    public Set<UUID> getSpectators() {
        return this.spectators;
    }

    public int getCurrentRound() {
        return this.currentRound;
    }

    public int getRoundWinsRed() {
        return this.roundWinsRed;
    }

    public int getRoundWinsBlue() {
        return this.roundWinsBlue;
    }

    public int getWinsToWin() {
        return 3;
    }

    public Map<UUID, Integer> getKills() {
        return this.kills;
    }

    public Map<UUID, Integer> getDeaths() {
        return this.deaths;
    }

    public MatchStats getMatchStats() {
        return this.matchStats;
    }

    public GameMode getCurrentGameMode() {
        return this.currentGameMode;
    }

    public int getTdmKillsRed() {
        return this.tdmKillsRed;
    }

    public int getTdmKillsBlue() {
        return this.tdmKillsBlue;
    }

    public long getTdmStartTime() {
        return this.tdmStartTime;
    }

    public int getDomPointsRed() {
        return this.domPointsRed;
    }

    public int getDomPointsBlue() {
        return this.domPointsBlue;
    }

    public int getCtfRedCaptures() {
        return this.ctfRedCaptures;
    }

    public int getCtfBlueCaptures() {
        return this.ctfBlueCaptures;
    }

    public NbsPlayer getSelectedBgm() {
        return this.selectedBgm;
    }

    public void setSelectedBgm(NbsPlayer bgm) {
        this.selectedBgm = bgm;
    }

    public void setSelectedBgmByName(String name) {
        for (NbsPlayer song : this.plugin.getSongs()) {
            if (!song.getName().equalsIgnoreCase(name)) continue;
            this.selectedBgm = song;
            return;
        }
    }

    public int getAliveCount(TeamColor team) {
        List<UUID> t = team == TeamColor.RED ? this.redTeam : this.blueTeam;
        int count = 0;
        for (UUID uid : t) {
            if (this.plugin.getBotManager().getBotTeam(uid) != null) continue;
            Player p = Bukkit.getPlayer((UUID)uid);
            if (this.deadPlayers.contains(uid)) continue;
            ++count;
        }
        return count += this.plugin.getBotManager().getAliveBotCount(team);
    }

    private void announceBigPlay(Player killer, int streak) {
        if (streak == 5) {
            Bukkit.broadcastMessage((String)("\u00a74\u00a7l\u2605 " + killer.getName() + " \u00a7c\u304c PENTA KILL \u3092\u9054\u6210\uff01 \u00a74\u2605"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.7f, 0.8f);
            }
        } else if (streak >= 3) {
            Bukkit.broadcastMessage((String)("\u00a76\u00a7l\u2605 " + killer.getName() + " \u00a7e\u304c " + streak + "\u9023\u7d9a\u30ad\u30eb\u3092\u7dcc\u652c\uff01"));
        }
    }

    public Map<UUID, KitType> getPlayerKits() {
        HashMap<UUID, KitType> result = new HashMap<UUID, KitType>();
        for (Map.Entry<UUID, String> entry : this.playerKit.entrySet()) {
            try {
                result.put(entry.getKey(), KitType.valueOf(entry.getValue()));
            }
            catch (Exception exception) {}
        }
        return result;
    }
}

