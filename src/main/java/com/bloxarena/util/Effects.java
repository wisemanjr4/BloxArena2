package com.bloxarena.util;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.TeamColor;
import com.bloxarena.game.WinCondition;
import com.bloxarena.map.MapConfig;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Effects {

    /** バリア消滅演出 */
    public static void playBarrierRemovalEffect(MapConfig map, BloxArenaPlugin plugin) {
        World world = Bukkit.getWorld(map.getWorld());
        if (world == null) return;

        Location redCenter = zoneCenter(map.getRedSpawnMin(), map.getRedSpawnMax());
        Location blueCenter = zoneCenter(map.getBlueSpawnMin(), map.getBlueSpawnMax());

        // 消滅直前: SMOKE_LARGE を0.5秒×3回
        new BukkitRunnable() {
            int count = 3;
            @Override
            public void run() {
                if (count-- <= 0) {
                    cancel();
                    // バリア消滅と同時に爆発エフェクト
                    spawnExplosion(world, redCenter);
                    spawnExplosion(world, blueCenter);
                    return;
                }
                world.spawnParticle(Particle.SMOKE_LARGE, redCenter, 30, 1, 0.5, 1, 0.05);
                world.spawnParticle(Particle.SMOKE_LARGE, blueCenter, 30, 1, 0.5, 1, 0.05);
            }
        }.runTaskTimer(plugin, 0L, 10L); // 0.5秒間隔
    }

    private static void spawnExplosion(World world, Location loc) {
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
        world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 5, 1, 0.5, 1, 0);
    }

    private static Location zoneCenter(Location min, Location max) {
        return new Location(
            min.getWorld(),
            (min.getX() + max.getX()) / 2,
            (min.getY() + max.getY()) / 2,
            (min.getZ() + max.getZ()) / 2
        );
    }

    /** 試合終了演出（多段フェーズ） */
    public static void playVictoryEffect(
            TeamColor winner,
            WinCondition condition,
            List<UUID> allUids,
            List<UUID> redTeam,
            List<UUID> blueTeam,
            MapConfig map,
            Map<UUID, Integer> kills,
            Map<UUID, Integer> deaths,
            BloxArenaPlugin plugin
    ) {
        String conditionStr = condition == WinCondition.ELIMINATION ? "殲滅" : "オブジェクト";
        List<UUID> winTeam  = winner == TeamColor.RED ? redTeam : blueTeam;
        List<UUID> loseTeam = winner == TeamColor.RED ? blueTeam : redTeam;
        Color winColor = winner == TeamColor.RED ? Color.RED : Color.AQUA;
        Color winColor2 = winner == TeamColor.RED ? Color.ORANGE : Color.BLUE;

        World world = map != null ? Bukkit.getWorld(map.getWorld()) : null;
        Location center = (map != null && map.getCenter() != null) ? map.getCenter().clone().add(0, 5, 0) : null;

        // ── フェーズ1（即時）: 溜め演出 ──
        for (UUID uid : allUids) {
            Player p = Bukkit.getPlayer(uid);
            if (p == null) continue;
            p.sendTitle("", "§8§l…", 0, 25, 5);
            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.5f);
        }

        // ── フェーズ2（1秒後）: チーム名ドーン ──
        new BukkitRunnable() { @Override public void run() {
            for (UUID uid : allUids) {
                Player p = Bukkit.getPlayer(uid);
                if (p == null) continue;
                boolean isWinner = winner != null && winTeam.contains(uid);

                if (winner == null) {
                    p.sendTitle("§7§l引き分け", "§7両チーム脱落", 5, 60, 15);
                    p.playSound(p.getLocation(), Sound.ENTITY_CREEPER_DEATH, 1f, 0.8f);
                } else if (isWinner) {
                    p.sendTitle(winner.getColorCode() + "§l★ 勝利 ★", "§e" + conditionStr + "で勝利！", 5, 70, 15);
                    p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.2f);
                    p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                } else {
                    p.sendTitle("§8§l敗北…", winner.getColorCode() + winner.getDisplayName() + " §8チームの勝利", 5, 70, 15);
                    p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 0.7f);
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1f, 0.6f);
                }
            }
            // 稲妻エフェクト（アリーナ中心）
            if (world != null && center != null) {
                world.spawnParticle(Particle.EXPLOSION_HUGE, center, 3, 2, 1, 2, 0);
                world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.8f);
            }
        }}.runTaskLater(plugin, 20L);

        // ── フェーズ3（2〜12秒）: 花火ショー ──
        if (winner != null && world != null && center != null) {
            FireworkEffect.Type[] types = {
                FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.STAR,
                FireworkEffect.Type.BURST,      FireworkEffect.Type.BALL
            };
            java.util.Random rng = new java.util.Random();
            new BukkitRunnable() {
                int shot = 0;
                @Override public void run() {
                    if (shot++ >= 16) { cancel(); return; }
                    // ランダムオフセットで打ち上げ
                    double ox = (rng.nextDouble() - 0.5) * 20;
                    double oz = (rng.nextDouble() - 0.5) * 20;
                    Location fwLoc = center.clone().add(ox, -3, oz);
                    FireworkEffect.Type type = types[rng.nextInt(types.length)];
                    Color c1 = rng.nextBoolean() ? winColor : winColor2;
                    Color c2 = rng.nextBoolean() ? Color.WHITE : Color.YELLOW;
                    Firework fw = world.spawn(fwLoc, Firework.class);
                    FireworkMeta meta = fw.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder()
                        .with(type).withColor(c1).withFade(c2)
                        .trail(rng.nextBoolean()).flicker(rng.nextBoolean()).build());
                    meta.setPower(1 + rng.nextInt(2));
                    fw.setFireworkMeta(meta);
                }
            }.runTaskTimer(plugin, 40L, 12L); // 2秒後から0.6秒ごと×16発

            // 勝者にパーティクルシャワー（毎秒）
            new BukkitRunnable() {
                int tick = 0;
                @Override public void run() {
                    if (tick++ >= 10) { cancel(); return; }
                    for (UUID uid : winTeam) {
                        Player p = Bukkit.getPlayer(uid);
                        if (p == null) continue;
                        p.getWorld().spawnParticle(Particle.FIREWORKS_SPARK,
                            p.getLocation().add(0, 2.5, 0), 30, 0.5, 0.5, 0.5, 0.1);
                        p.getWorld().spawnParticle(Particle.TOTEM,
                            p.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
                    }
                }
            }.runTaskTimer(plugin, 40L, 20L);

            // 敗者にスモーク
            new BukkitRunnable() {
                int tick = 0;
                @Override public void run() {
                    if (tick++ >= 5) { cancel(); return; }
                    for (UUID uid : loseTeam) {
                        Player p = Bukkit.getPlayer(uid);
                        if (p == null) continue;
                        p.getWorld().spawnParticle(Particle.SMOKE_LARGE,
                            p.getLocation().add(0, 1, 0), 12, 0.4, 0.4, 0.4, 0.02);
                    }
                }
            }.runTaskTimer(plugin, 40L, 20L);
        }

        // ── フェーズ4（12秒）: 最終爆発 ──
        new BukkitRunnable() { @Override public void run() {
            if (world != null && center != null) {
                for (int i = 0; i < 5; i++) {
                    Location bl = center.clone().add(
                        (Math.random()-0.5)*16, 0, (Math.random()-0.5)*16);
                    world.spawnParticle(Particle.EXPLOSION_HUGE, bl, 2, 0, 0, 0, 0);
                    world.playSound(bl, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.9f + (float)Math.random()*0.4f);
                }
            }
        }}.runTaskLater(plugin, 240L);

        // ── 全体チャット（即時）──
        StringBuilder result = new StringBuilder();
        String sep = "§8§m══════════════════════════";
        result.append(sep).append("\n");
        result.append(winner != null
            ? winner.getColorCode() + "§l★ " + winner.getDisplayName() + " チーム " + conditionStr + "勝利！ ★\n"
            : "§7§l引き分け\n");
        result.append(sep).append("\n");
        result.append("§c❤ 赤: ");
        for (UUID uid : redTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) result.append("§f").append(p.getName())
                .append("§7(").append(kills.getOrDefault(uid, 0)).append("K/")
                .append(deaths.getOrDefault(uid, 0)).append("D) ");
        }
        result.append("\n§b❄ 青: ");
        for (UUID uid : blueTeam) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) result.append("§f").append(p.getName())
                .append("§7(").append(kills.getOrDefault(uid, 0)).append("K/")
                .append(deaths.getOrDefault(uid, 0)).append("D) ");
        }
        result.append("\n").append(sep);
        Bukkit.broadcastMessage(result.toString());
    }
}
