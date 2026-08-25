/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Color
 *  org.bukkit.FireworkEffect
 *  org.bukkit.FireworkEffect$Type
 *  org.bukkit.Location
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.entity.Firework
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.meta.FireworkMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.bloxarena.util;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.TeamColor;
import com.bloxarena.game.WinCondition;
import com.bloxarena.map.MapConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Effects {
    public static void playBarrierRemovalEffect(MapConfig map, BloxArenaPlugin plugin) {
        final World world = Bukkit.getWorld((String)map.getWorld());
        if (world == null) {
            return;
        }
        final Location redCenter = Effects.zoneCenter(map.getRedSpawnMin(), map.getRedSpawnMax());
        final Location blueCenter = Effects.zoneCenter(map.getBlueSpawnMin(), map.getBlueSpawnMax());
        new BukkitRunnable(){
            int count = 3;

            public void run() {
                if (this.count-- <= 0) {
                    this.cancel();
                    Effects.spawnExplosion(world, redCenter);
                    Effects.spawnExplosion(world, blueCenter);
                    return;
                }
                world.spawnParticle(Particle.SMOKE_LARGE, redCenter, 30, 1.0, 0.5, 1.0, 0.05);
                world.spawnParticle(Particle.SMOKE_LARGE, blueCenter, 30, 1.0, 0.5, 1.0, 0.05);
            }
        }.runTaskTimer((Plugin)plugin, 0L, 10L);
    }

    private static void spawnExplosion(World world, Location loc) {
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 5, 1.0, 0.5, 1.0, 0.0);
    }

    private static Location zoneCenter(Location min, Location max) {
        return new Location(min.getWorld(), (min.getX() + max.getX()) / 2.0, (min.getY() + max.getY()) / 2.0, (min.getZ() + max.getZ()) / 2.0);
    }

    public static void playVictoryEffect(final TeamColor winner, WinCondition condition, final List<UUID> allUids, List<UUID> redTeam, List<UUID> blueTeam, MapConfig map, Map<UUID, Integer> kills, Map<UUID, Integer> deaths, BloxArenaPlugin plugin, final UUID ffaWinner) {
        Player p;
        final String conditionStr = condition == WinCondition.ELIMINATION ? "\u6bb2\u6ec5" : "\u30aa\u30d6\u30b8\u30a7\u30af\u30c8";
        final String winnerName = ffaWinner != null ? Bukkit.getOfflinePlayer((UUID)ffaWinner).getName() : null;
        final List<UUID> winTeam;
        final List<UUID> loseTeam;
        final Color winColor;
        final Color winColor2;
        if (winner != null) {
            winTeam = winner == TeamColor.RED ? redTeam : blueTeam;
            loseTeam = winner == TeamColor.RED ? blueTeam : redTeam;
            winColor = winner == TeamColor.RED ? Color.RED : Color.AQUA;
            winColor2 = winner == TeamColor.RED ? Color.ORANGE : Color.BLUE;
        } else if (ffaWinner != null) {
            winTeam = Collections.singletonList(ffaWinner);
            loseTeam = new ArrayList<UUID>(allUids);
            loseTeam.remove(ffaWinner);
            winColor = Color.YELLOW;
            winColor2 = Color.ORANGE;
        } else {
            winTeam = Collections.emptyList();
            loseTeam = new ArrayList<UUID>(allUids);
            winColor = Color.WHITE;
            winColor2 = Color.WHITE;
        }
        final World world = map != null ? Bukkit.getWorld((String)map.getWorld()) : null;
        final Location center = map != null && map.getCenter() != null ? map.getCenter().clone().add(0.0, 5.0, 0.0) : null;
        for (UUID uid : allUids) {
            Player p2 = Bukkit.getPlayer((UUID)uid);
            if (p2 == null) continue;
            p2.sendTitle("", "\u00a78\u00a7l\u2026", 0, 25, 5);
            p2.playSound(p2.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.5f);
        }
        new BukkitRunnable(){

            public void run() {
                for (UUID uid : allUids) {
                    boolean isWinner;
                    Player p = Bukkit.getPlayer((UUID)uid);
                    if (p == null) continue;
                    boolean bl = isWinner = ffaWinner != null ? uid.equals(ffaWinner) : winner != null && winTeam.contains(uid);
                    if (winner == null && ffaWinner == null) {
                        p.sendTitle("\u00a77\u00a7l\u5f15\u304d\u5206\u3051", "\u00a77\u4e21\u30c1\u30fc\u30e0\u8131\u843d", 5, 60, 15);
                        p.playSound(p.getLocation(), Sound.ENTITY_CREEPER_DEATH, 1.0f, 0.8f);
                        continue;
                    }
                    if (isWinner) {
                        String title = ffaWinner != null ? "\u00a76\u00a7l\u2605 \u52dd\u5229 \u2605" : winner.getColorCode() + "\u00a7l\u2605 \u52dd\u5229 \u2605";
                        p.sendTitle(title, "\u00a7e" + conditionStr + "\u3067\u52dd\u5229\uff01", 5, 70, 15);
                        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.2f);
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        continue;
                    }
                    String loseTitle = ffaWinner != null ? "\u00a7e" + (winnerName != null ? winnerName : "???") + " \u00a78\u306e\u52dd\u5229" : winner.getColorCode() + winner.getDisplayName() + " \u00a78\u30c1\u30fc\u30e0\u306e\u52dd\u5229";
                    p.sendTitle("\u00a78\u00a7l\u6557\u5317\u2026", loseTitle, 5, 70, 15);
                    p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 0.7f);
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.6f);
                }
                if (world != null && center != null) {
                    world.spawnParticle(Particle.EXPLOSION_HUGE, center, 3, 2.0, 1.0, 2.0, 0.0);
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
                }
            }
        }.runTaskLater((Plugin)plugin, 20L);
        if ((winner != null || ffaWinner != null) && world != null && center != null) {
            final FireworkEffect.Type[] types = new FireworkEffect.Type[]{FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.STAR, FireworkEffect.Type.BURST, FireworkEffect.Type.BALL};
            final Random rng = new Random();
            new BukkitRunnable(){
                int shot = 0;

                public void run() {
                    if (this.shot++ >= 16) {
                        this.cancel();
                        return;
                    }
                    double ox = (rng.nextDouble() - 0.5) * 20.0;
                    double oz = (rng.nextDouble() - 0.5) * 20.0;
                    Location fwLoc = center.clone().add(ox, -3.0, oz);
                    FireworkEffect.Type type = types[rng.nextInt(types.length)];
                    Color c1 = rng.nextBoolean() ? winColor : winColor2;
                    Color c2 = rng.nextBoolean() ? Color.WHITE : Color.YELLOW;
                    Firework fw = (Firework)world.spawn(fwLoc, Firework.class);
                    FireworkMeta meta = fw.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder().with(type).withColor(c1).withFade(c2).trail(rng.nextBoolean()).flicker(rng.nextBoolean()).build());
                    meta.setPower(1 + rng.nextInt(2));
                    fw.setFireworkMeta(meta);
                }
            }.runTaskTimer((Plugin)plugin, 40L, 12L);
            new BukkitRunnable(){
                int tick = 0;

                public void run() {
                    if (this.tick++ >= 10) {
                        this.cancel();
                        return;
                    }
                    for (UUID uid : winTeam) {
                        Player p = Bukkit.getPlayer((UUID)uid);
                        if (p == null) continue;
                        p.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, p.getLocation().add(0.0, 2.5, 0.0), 30, 0.5, 0.5, 0.5, 0.1);
                        p.getWorld().spawnParticle(Particle.TOTEM, p.getLocation().add(0.0, 1.0, 0.0), 15, 0.3, 0.5, 0.3, 0.05);
                    }
                }
            }.runTaskTimer((Plugin)plugin, 40L, 20L);
            new BukkitRunnable(){
                int tick = 0;

                public void run() {
                    if (this.tick++ >= 5) {
                        this.cancel();
                        return;
                    }
                    for (UUID uid : loseTeam) {
                        Player p = Bukkit.getPlayer((UUID)uid);
                        if (p == null) continue;
                        p.getWorld().spawnParticle(Particle.SMOKE_LARGE, p.getLocation().add(0.0, 1.0, 0.0), 12, 0.4, 0.4, 0.4, 0.02);
                    }
                }
            }.runTaskTimer((Plugin)plugin, 40L, 20L);
        }
        new BukkitRunnable(){

            public void run() {
                if (world != null && center != null) {
                    for (int i = 0; i < 5; ++i) {
                        Location bl = center.clone().add((Math.random() - 0.5) * 16.0, 0.0, (Math.random() - 0.5) * 16.0);
                        world.spawnParticle(Particle.EXPLOSION_HUGE, bl, 2, 0.0, 0.0, 0.0, 0.0);
                        world.playSound(bl, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.9f + (float)Math.random() * 0.4f);
                    }
                }
            }
        }.runTaskLater((Plugin)plugin, 240L);
        StringBuilder result = new StringBuilder();
        String sep = "\u00a78\u00a7m\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550";
        result.append(sep).append("\n");
        if (ffaWinner != null) {
            result.append("\u00a76\u00a7l\u2605 " + (winnerName != null ? winnerName : "???") + " " + conditionStr + "\u52dd\u5229\uff01 \u2605\n");
        } else {
            result.append((String)(winner != null ? winner.getColorCode() + "\u00a7l\u2605 " + winner.getDisplayName() + " \u30c1\u30fc\u30e0 " + conditionStr + "\u52dd\u5229\uff01 \u2605\n" : "\u00a77\u00a7l\u5f15\u304d\u5206\u3051\n"));
        }
        result.append(sep).append("\n");
        if (ffaWinner != null) {
            result.append("\u00a76\u2694 \u6700\u591a\u30ad\u30eb: ");
            for (UUID uid : allUids) {
                p = Bukkit.getPlayer((UUID)uid);
                if (p == null) continue;
                result.append("\u00a7f").append(p.getName()).append("\u00a77(").append(kills.getOrDefault(uid, 0)).append("K/").append(deaths.getOrDefault(uid, 0)).append("D) ");
            }
            result.append("\n").append(sep);
            Bukkit.broadcastMessage((String)result.toString());
            return;
        }
        result.append("\u00a7c\u2764 \u8d64: ");
        for (UUID uid : redTeam) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            result.append("\u00a7f").append(p.getName()).append("\u00a77(").append(kills.getOrDefault(uid, 0)).append("K/").append(deaths.getOrDefault(uid, 0)).append("D) ");
        }
        result.append("\n\u00a7b\u2744 \u9752: ");
        for (UUID uid : blueTeam) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            result.append("\u00a7f").append(p.getName()).append("\u00a77(").append(kills.getOrDefault(uid, 0)).append("K/").append(deaths.getOrDefault(uid, 0)).append("D) ");
        }
        result.append("\n").append(sep);
        Bukkit.broadcastMessage((String)result.toString());
    }
}

