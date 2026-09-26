/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 */
package com.bloxarena.tutorial;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.kit.KitBuilder;
import com.bloxarena.test.TestFieldManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TutorialManager {
    private final BloxArenaPlugin plugin;
    private final Set<UUID> inTutorial = new HashSet<UUID>();
    private final Map<UUID, Integer> tutorialStep = new HashMap<UUID, Integer>();
    private final Map<UUID, BukkitTask> tutorialTasks = new HashMap<UUID, BukkitTask>();
    private static final String[] STEPS = new String[]{"\u3088\u3046\u3053\u305d\uff01\u53f3\u30af\u30ea\u30c3\u30af\u3067\u6b21\u3078", "\u30ad\u30c3\u30c8\u3092\u9078\u3093\u3067\u307f\u3088\u3046\uff01\n/ba kits \u2192 \u30af\u30ea\u30c3\u30af\u3067\u88c5\u5099\uff01", "\u9078\u3093\u3060\u30ad\u30c3\u30c8\u306e\u30b9\u30ad\u30eb\u3092\u4f7f\u3063\u3066\u307f\u3088\u3046\uff01\n\u30b9\u30ad\u30eb\u661f\u3092\u53f3\u30af\u30ea\u30c3\u30af\n\u9032\u307e\u306a\u3044\u5834\u5408\u306f /ba tutorial next", "\u30d0\u30fc\u30b9\u30c8\u3092\u4f7f\u3063\u3066\u307f\u3088\u3046\uff01\n\u30cf\u30fc\u30c8\u30aa\u30d6\u30b6\u30b7\u30fc\u3092\u53f3\u30af\u30ea\u30c3\u30af\n\u9032\u307e\u306a\u3044\u5834\u5408\u306f /ba tutorial next", "\u30ac\u30fc\u30c9\u30d6\u30ec\u30a4\u30af\u3092\u4f7f\u3063\u3066\u307f\u3088\u3046\uff01\n3\u79d2\u3057\u3083\u304c\u3093\u3067\u6e9c\u3081\u3066\u304b\u3089\u653b\u6483\uff01\n\u9032\u307e\u306a\u3044\u5834\u5408\u306f /ba tutorial next", "\u304a\u3081\u3067\u3068\u3046\uff01\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u5b8c\u4e86\uff01\n/ba tutorial leave \u3067\u9000\u51fa"};
    private static final String[] STEP_ACTION_BARS = new String[]{"\u00a7a\u00a7l\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb \u00a77| \u00a7f\u53f3\u30af\u30ea\u30c3\u30af\u3067\u958b\u59cb", "\u00a7a\u00a7l\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb \u00a77| \u00a7f/ba kits \u3067\u30ad\u30c3\u30c8\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044", "\u00a7a\u00a7l\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb \u00a77| \u00a7f\u30b9\u30ad\u30eb\u30a2\u30a4\u30c6\u30e0\u3092\u53f3\u30af\u30ea\u30c3\u30af\uff01", "\u00a7a\u00a7l\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb \u00a77| \u00a7f\u30d0\u30fc\u30b9\u30c8\u3092\u4f7f\u3063\u3066\u307f\u3088\u3046\uff01", "\u00a7a\u00a7l\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb \u00a77| \u00a7f\u3057\u3083\u304c\u3093\u3067\u6e9c\u3081\u3066\u304b\u3089\u6575\u3092\u653b\u6483\uff01", "\u00a7a\u00a7l\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb \u00a77| \u00a7f\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u5b8c\u4e86\uff01/ba tutorial leave"};

    public TutorialManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(Player p) {
        if (this.inTutorial.contains(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u9032\u884c\u4e2d\u3067\u3059\u3002 \u00a7f/ba tutorial stop \u00a7c\u3067\u4e2d\u65ad\u3067\u304d\u307e\u3059\u3002");
            return;
        }
        if (this.plugin.getGameManager().isParticipant(p) || this.plugin.getGameManager().isSpectator(p)) {
            p.sendMessage("\u00a7c\u8a66\u5408\u4e2d\u306f\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u3092\u958b\u59cb\u3067\u304d\u307e\u305b\u3093\u3002");
            return;
        }
        TestFieldManager tf = this.plugin.getTestFieldManager();
        if (!tf.isActive()) {
            p.sendMessage("\u00a7c\u30c6\u30b9\u30c8\u5834\u304c\u8a2d\u5b9a\u3055\u308c\u3066\u3044\u307e\u305b\u3093\u3002 \u00a7f/ba test setup \u00a7c\u3067\u8a2d\u5b9a\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        this.plugin.getLobbyManager().onPlayerExit(p);
        this.plugin.getGameManager().setPlayerKit(p.getUniqueId(), null);
        this.inTutorial.add(p.getUniqueId());
        this.tutorialStep.put(p.getUniqueId(), 0);
        if (!tf.isTester(p)) {
            tf.enter(p);
        }
        Location spawn = this.getTutorialSpawn();
        if (spawn == null) {
            spawn = tf.getSpawn();
        }
        p.setGameMode(GameMode.SURVIVAL);
        p.teleport(spawn);
        p.getInventory().clear();
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.getInventory().setItem(8, KitBuilder.makeBurstItem());
        this.showStep(p, 0);
        this.startActionBarLoop(p);
    }

    private Location getTutorialSpawn() {
        FileConfiguration cfg = this.plugin.getConfig();
        if (!cfg.contains("tutorial.spawn.x")) {
            return null;
        }
        World w = Bukkit.getWorld(cfg.getString("tutorial.spawn.world", "world"));
        if (w == null) {
            return null;
        }
        return new Location(w, cfg.getDouble("tutorial.spawn.x"), cfg.getDouble("tutorial.spawn.y"), cfg.getDouble("tutorial.spawn.z"));
    }

    public void advance(Player p) {
        if (!this.inTutorial.contains(p.getUniqueId())) {
            return;
        }
        int step = this.tutorialStep.getOrDefault(p.getUniqueId(), 0);
        if (step >= STEPS.length - 1) {
            this.finish(p);
            return;
        }
        this.tutorialStep.put(p.getUniqueId(), ++step);
        this.showStep(p, step);
        if (step == 1 && this.plugin.getGameManager().getPlayerKitType(p.getUniqueId()) != null) {
            p.sendMessage("\u00a7a\u30ad\u30c3\u30c8\u3092\u65e2\u306b\u9078\u629e\u3057\u3066\u3044\u307e\u3059\uff01\u6b21\u306e\u30b9\u30c6\u30c3\u30d7\u306b\u9032\u307f\u307e\u3059\u3002");
            this.advance(p);
        }
    }

    public void stop(Player p) {
        if (!this.inTutorial.contains(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u306b\u53c2\u52a0\u3057\u3066\u3044\u307e\u305b\u3093\u3002");
            return;
        }
        this.cleanup(p);
        this.plugin.getTestFieldManager().leave(p);
        p.sendMessage("\u00a77\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u3092\u7d42\u4e86\u3057\u307e\u3057\u305f\u3002");
    }

    private void cleanup(Player p) {
        this.inTutorial.remove(p.getUniqueId());
        this.tutorialStep.remove(p.getUniqueId());
        BukkitTask task = this.tutorialTasks.remove(p.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    public void handleQuit(UUID uid) {
        this.inTutorial.remove(uid);
        this.tutorialStep.remove(uid);
        BukkitTask task = this.tutorialTasks.remove(uid);
        if (task != null) {
            task.cancel();
        }
    }

    public boolean isInTutorial(UUID uid) {
        return this.inTutorial.contains(uid);
    }

    public int getStep(UUID uid) {
        return this.tutorialStep.getOrDefault(uid, 0);
    }

    public void checkKitSelected(Player p) {
        if (!this.inTutorial.contains(p.getUniqueId())) {
            return;
        }
        if (this.tutorialStep.get(p.getUniqueId()) == 1) {
            this.advance(p);
        }
    }

    public void checkSkillUsed(Player p) {
        if (!this.inTutorial.contains(p.getUniqueId())) {
            return;
        }
        if (this.tutorialStep.get(p.getUniqueId()) == 2) {
            this.advance(p);
        }
    }

    public void checkBurstUsed(Player p) {
        if (!this.inTutorial.contains(p.getUniqueId())) {
            return;
        }
        if (this.tutorialStep.get(p.getUniqueId()) == 3) {
            this.advance(p);
        }
    }

    public void checkGuardBreakUsed(Player p) {
        if (!this.inTutorial.contains(p.getUniqueId())) {
            return;
        }
        if (this.tutorialStep.get(p.getUniqueId()) == 4) {
            this.advance(p);
        }
    }

    private void showStep(Player p, int step) {
        if (step == 3) {
            this.plugin.getSkillManager().clearBurstUsed(p.getUniqueId());
            p.getInventory().setItem(8, KitBuilder.makeBurstItem());
        }
        String[] lines = STEPS[step].split("\n");
        String title = step == 0 ? "\u00a76\u00a7lBloxArena II" : (step == 5 ? "\u00a7a\u00a7l\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u5b8c\u4e86\uff01" : "\u00a76\u00a7lStep " + step + "/5");
        p.showTitle(Title.title((Component)Component.text((String)title), (Component)Component.text((String)lines[0]), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(500L), (Duration)Duration.ofMillis(3000L), (Duration)Duration.ofMillis(500L))));
        if (lines.length > 1) {
            p.sendMessage("\u00a7e" + lines[1]);
        }
    }

    private void startActionBarLoop(final Player p) {
        BukkitTask task = new BukkitRunnable(){

            public void run() {
                if (!TutorialManager.this.inTutorial.contains(p.getUniqueId())) {
                    this.cancel();
                    return;
                }
                int step = TutorialManager.this.tutorialStep.getOrDefault(p.getUniqueId(), 0);
                p.sendActionBar((Component)Component.text((String)STEP_ACTION_BARS[step]));
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
        this.tutorialTasks.put(p.getUniqueId(), task);
    }

    private void finish(Player p) {
        this.cleanup(p);
        p.showTitle(Title.title((Component)Component.text((String)"\u00a7a\u00a7l\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u5b8c\u4e86\uff01"), (Component)Component.text((String)"\u00a7f\u30c6\u30b9\u30c8\u5834\u3067\u81ea\u7531\u306b\u7df4\u7fd2\u3057\u3088\u3046"), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(500L), (Duration)Duration.ofMillis(3000L), (Duration)Duration.ofMillis(500L))));
        p.sendMessage("\u00a7a\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u3092\u5b8c\u4e86\u3057\u307e\u3057\u305f\uff01\u30c6\u30b9\u30c8\u5834\u3067\u5f15\u304d\u7d9a\u304d\u7df4\u7fd2\u3067\u304d\u307e\u3059\u3002\u00a77/ba tutorial leave \u3067\u9000\u51fa");
    }
}

