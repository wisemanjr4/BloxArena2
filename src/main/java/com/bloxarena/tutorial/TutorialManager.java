package com.bloxarena.tutorial;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.kit.KitType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import java.time.Duration;
import java.util.*;

public class TutorialManager {
    private final BloxArenaPlugin plugin;
    private final Set<UUID> inTutorial = new HashSet<>();
    private final Map<UUID, Integer> tutorialStep = new HashMap<>();
    private final Map<UUID, BukkitTask> tutorialTasks = new HashMap<>();
    private static final String[] STEPS = {
        "ようこそ！右クリックで次へ",
        "キットを選んでみよう！\n/ba kits でキット一覧を開く",
        "選んだキットのスキルを使ってみよう！\nスキル星を右クリック",
        "バーストを使ってみよう！\nハートオブザシーを右クリック",
        "ガードブレイクを使ってみよう！\n1秒しゃがんで溜めてから攻撃！",
        "おめでとう！チュートリアル完了！\n/ba test leave で退出"
    };
    private static final String[] STEP_ACTION_BARS = {
        "§a§lチュートリアル §7| §f右クリックで開始",
        "§a§lチュートリアル §7| §f/ba kits でキットを選択してください",
        "§a§lチュートリアル §7| §fスキルアイテムを右クリック！",
        "§a§lチュートリアル §7| §fバーストを使ってみよう！",
        "§a§lチュートリアル §7| §fしゃがんで溜めてから敵を攻撃！",
        "§a§lチュートリアル §7| §fチュートリアル完了！/ba tutorial leave"
    };

    public TutorialManager(BloxArenaPlugin plugin) { this.plugin = plugin; }

    public void start(Player p) {
        if (inTutorial.contains(p.getUniqueId())) return;
        inTutorial.add(p.getUniqueId());
        tutorialStep.put(p.getUniqueId(), 0);
        p.teleport(plugin.getTestFieldManager().getSpawn());
        p.getInventory().clear();
        plugin.getTestFieldManager().enter(p);
        p.getInventory().setItem(8, com.bloxarena.kit.KitBuilder.makeBurstItem());
        showStep(p, 0);
        startActionBarLoop(p);
    }

    public void advance(Player p) {
        if (!inTutorial.contains(p.getUniqueId())) return;
        int step = tutorialStep.getOrDefault(p.getUniqueId(), 0);
        if (step >= STEPS.length - 1) { finish(p); return; }
        step++;
        tutorialStep.put(p.getUniqueId(), step);
        showStep(p, step);
        if (step == 1 && plugin.getGameManager().getPlayerKitType(p.getUniqueId()) != null) {
            p.sendMessage("§aキットを既に選択しています！次のステップに進みます。");
            advance(p);
        }
    }

    public void stop(Player p) {
        inTutorial.remove(p.getUniqueId());
        tutorialStep.remove(p.getUniqueId());
        BukkitTask task = tutorialTasks.remove(p.getUniqueId());
        if (task != null) task.cancel();
        plugin.getTestFieldManager().leave(p);
        p.sendMessage("§7チュートリアルを終了しました。");
    }

    public boolean isInTutorial(UUID uid) { return inTutorial.contains(uid); }
    public int getStep(UUID uid) { return tutorialStep.getOrDefault(uid, 0); }

    public void checkSkillUsed(Player p) {
        if (!inTutorial.contains(p.getUniqueId())) return;
        if (tutorialStep.getOrDefault(p.getUniqueId(), -1) == 2) advance(p);
    }

    public void checkBurstUsed(Player p) {
        if (!inTutorial.contains(p.getUniqueId())) return;
        if (tutorialStep.getOrDefault(p.getUniqueId(), -1) == 3) advance(p);
    }

    public void checkGuardBreakUsed(Player p) {
        if (!inTutorial.contains(p.getUniqueId())) return;
        if (tutorialStep.getOrDefault(p.getUniqueId(), -1) == 4) advance(p);
    }

    private void showStep(Player p, int step) {
        if (step < 0 || step >= STEPS.length) return;
        String[] lines = STEPS[step].split("\n");
        String title = step == 0 ? "§6§lBloxArena II" : step == 5 ? "§a§lチュートリアル完了！" : "§6§lStep " + (step) + "/5";
        p.showTitle(Title.title(
            Component.text(title),
            Component.text(lines[0]),
            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))
        ));
        if (lines.length > 1) p.sendMessage("§e" + lines[1]);
    }

    private void startActionBarLoop(Player p) {
        BukkitTask task = new BukkitRunnable() {
            @Override public void run() {
                if (!inTutorial.contains(p.getUniqueId())) { cancel(); return; }
                int step = tutorialStep.getOrDefault(p.getUniqueId(), 0);
                if (step >= 0 && step < STEP_ACTION_BARS.length) {
                    p.sendActionBar(Component.text(STEP_ACTION_BARS[step]));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        tutorialTasks.put(p.getUniqueId(), task);
    }

    private void finish(Player p) {
        inTutorial.remove(p.getUniqueId());
        tutorialStep.remove(p.getUniqueId());
        BukkitTask task = tutorialTasks.remove(p.getUniqueId());
        if (task != null) task.cancel();
        p.showTitle(Title.title(
            Component.text("§a§lチュートリアル完了！"),
            Component.text("§fテスト場で自由に練習しよう"),
            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))
        ));
        p.sendMessage("§aチュートリアルを完了しました！テスト場で引き続き練習できます。§7/ba test leave で退出");
    }
}
