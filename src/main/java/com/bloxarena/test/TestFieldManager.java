package com.bloxarena.test;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.kit.KitBuilder;
import com.bloxarena.kit.KitType;
import com.bloxarena.game.TeamColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TestFieldManager {

    private final BloxArenaPlugin plugin;
    private final Set<UUID> testers = new HashSet<>();
    private final Map<UUID, Location> returnLocations = new HashMap<>();
    private final Map<UUID, KitType> testKits = new HashMap<>();
    private final List<Zombie> dummies = new ArrayList<>();
    private org.bukkit.scheduler.BukkitTask skillUpdateTask = null;
    private Location testSpawn;
    private Location testAreaMin;
    private Location testAreaMax;
    private int dummyCount = 3;
    private World testWorld;

    public TestFieldManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
        if (cfg.contains("test_field.spawn.x")) {
            String worldName = cfg.getString("test_field.spawn.world", "world");
            testWorld = Bukkit.getWorld(worldName);
            testSpawn = new Location(testWorld,
                cfg.getDouble("test_field.spawn.x", 0),
                cfg.getDouble("test_field.spawn.y", 64),
                cfg.getDouble("test_field.spawn.z", 0));
            dummyCount = cfg.getInt("test_field.dummy_count", 3);
        }
        if (cfg.contains("test_field.area.min.x")) {
            testAreaMin = new Location(testWorld,
                cfg.getDouble("test_field.area.min.x"),
                cfg.getDouble("test_field.area.min.y"),
                cfg.getDouble("test_field.area.min.z"));
            testAreaMax = new Location(testWorld,
                cfg.getDouble("test_field.area.max.x"),
                cfg.getDouble("test_field.area.max.y"),
                cfg.getDouble("test_field.area.max.z"));
        }
    }

    public void setArea(Location min, Location max) {
        this.testAreaMin = min.clone();
        this.testAreaMax = max.clone();
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
        cfg.set("test_field.area.world", min.getWorld().getName());
        cfg.set("test_field.area.min.x", min.getX());
        cfg.set("test_field.area.min.y", min.getY());
        cfg.set("test_field.area.min.z", min.getZ());
        cfg.set("test_field.area.max.x", max.getX());
        cfg.set("test_field.area.max.y", max.getY());
        cfg.set("test_field.area.max.z", max.getZ());
        plugin.saveConfig();
    }

    public boolean hasArea() { return testAreaMin != null && testAreaMax != null; }

    public boolean isInArea(Location loc) {
        if (testAreaMin == null || testAreaMax == null) return true;
        if (loc.getWorld() != testAreaMin.getWorld()) return false;
        return loc.getX() >= Math.min(testAreaMin.getX(), testAreaMax.getX())
            && loc.getX() <= Math.max(testAreaMin.getX(), testAreaMax.getX())
            && loc.getY() >= Math.min(testAreaMin.getY(), testAreaMax.getY())
            && loc.getY() <= Math.max(testAreaMin.getY(), testAreaMax.getY())
            && loc.getZ() >= Math.min(testAreaMin.getZ(), testAreaMax.getZ())
            && loc.getZ() <= Math.max(testAreaMin.getZ(), testAreaMax.getZ());
    }

    public boolean isActive() { return testSpawn != null; }

    public void enter(Player p) {
        if (testSpawn == null) {
            p.sendMessage("§cテスト場が設定されていません。config.ymlのtest_fieldを設定してください。");
            return;
        }
        if (testers.contains(p.getUniqueId())) {
            p.sendMessage("§cすでにテスト場に入っています。 §f/ba test leave §cで退出");
            return;
        }
        returnLocations.put(p.getUniqueId(), p.getLocation().clone());
        testers.add(p.getUniqueId());
        p.setGameMode(org.bukkit.GameMode.SURVIVAL);
        p.teleport(testSpawn.clone().add(Math.random()*2-1, 0, Math.random()*2-1));
        p.getInventory().clear();
        p.setHealth(20);
        p.setFoodLevel(20);
        for (var eff : p.getActivePotionEffects()) p.removePotionEffect(eff.getType());
        p.sendMessage("§a§lテスト場に入りました！");
        p.sendMessage("§7§f/ba kits §7でキット一覧 → クリックで即装備");
        p.sendMessage("§7ダミー §f" + dummyCount + "体 §7が自動復活します");
        p.sendMessage("§7§f/ba test leave §7で退出（状態リセット）");
        // Spawn dummies if first tester
        if (dummies.isEmpty()) spawnDummies();
        // Start skill update task for passive skills (sniper, vampire, etc.)
        if (skillUpdateTask == null) {
            skillUpdateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (testers.isEmpty()) return;
                plugin.getSkillManager().update();
                plugin.getSkillManager().updateTurrets();
            }, 0L, 20L);
        }
    }

    public void leave(Player p) {
        testers.remove(p.getUniqueId());
        testKits.remove(p.getUniqueId());
        returnLocations.remove(p.getUniqueId());
        // Fully restore player
        p.setGameMode(org.bukkit.GameMode.SURVIVAL);
        p.getInventory().clear();
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(5f);
        p.setWalkSpeed(0.2f);
        p.setInvulnerable(false);
        p.setFireTicks(0);
        for (var eff : p.getActivePotionEffects()) p.removePotionEffect(eff.getType());
        // Teleport to lobby spawn
        Location lobby = plugin.getLobbyManager().getLobbySpawn();
        if (lobby != null) p.teleport(lobby);
        p.sendMessage("§aテスト場から退出しました。状態をリセットしました。");
        if (testers.isEmpty()) { clearDummies(); if (skillUpdateTask != null) { skillUpdateTask.cancel(); skillUpdateTask = null; } }
    }

    public void giveTestKit(Player p, KitType kit) {
        if (!testers.contains(p.getUniqueId())) return;
        testKits.put(p.getUniqueId(), kit);
        plugin.getGameManager().setPlayerKit(p.getUniqueId(), kit.name());
        KitBuilder.giveKit(p, kit, TeamColor.RED, plugin);
        p.sendMessage("§a" + kit.getDisplayName() + " §aを装備しました！");
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
    }

    public boolean isTester(Player p) { return testers.contains(p.getUniqueId()); }

    public void quit(Player p) {
        testers.remove(p.getUniqueId());
        testKits.remove(p.getUniqueId());
        returnLocations.remove(p.getUniqueId());
        if (testers.isEmpty()) { clearDummies(); if (skillUpdateTask != null) { skillUpdateTask.cancel(); skillUpdateTask = null; } }
    }

    private void spawnDummies() {
        clearDummies();
        List<Location> spawnPoints = new ArrayList<>();
        for (int i = 0; i < dummyCount; i++) {
            final int idx = i;
            Location loc = testSpawn.clone().add((i - dummyCount/2) * 3, 0, 5);
            spawnPoints.add(loc.clone());
            Zombie z = testWorld.spawn(loc, Zombie.class, zombie -> {
                zombie.setCustomName("§e§l訓練用ダミー " + (idx + 1));
                zombie.setCustomNameVisible(true);
                zombie.setBaby(false);
                zombie.setRemoveWhenFarAway(false);
                zombie.setShouldBurnInDay(false);
                zombie.setAI(true);
                zombie.setSilent(false);
                // Reduce speed so it barely moves but can be knocked back
                var speed = zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                if (speed != null) speed.setBaseValue(0.05);
                // Reduce attack damage to near zero
                var atk = zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                if (atk != null) atk.setBaseValue(0.5);
                // High HP pool for testing
                var hp = zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (hp != null) hp.setBaseValue(200.0);
                zombie.setHealth(200.0);
                // Clear equipment so it can't pick up items
                zombie.getEquipment().clear();
                zombie.setCanPickupItems(false);
            });
            dummies.add(z);
        }
        // Tether + respawn + regen loop
        new BukkitRunnable() {
            @Override public void run() {
                if (testers.isEmpty()) { cancel(); return; }
                for (int i = 0; i < dummies.size(); i++) {
                    final int idx2 = i;
                    Zombie z = dummies.get(i);
                    Location sp = spawnPoints.get(i);
                    // Respawn dead dummies
                    if (!z.isValid() || z.isDead()) {
                        z.remove();
                        Zombie newZ = testWorld.spawn(sp, Zombie.class, zombie -> {
                            zombie.setCustomName("§e§l訓練用ダミー " + (idx2 + 1));
                            zombie.setCustomNameVisible(true);
                            zombie.setBaby(false);
                            zombie.setRemoveWhenFarAway(false);
                            zombie.setShouldBurnInDay(false);
                            zombie.setAI(true);
                            zombie.setSilent(false);
                            var speed = zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                            if (speed != null) speed.setBaseValue(0.05);
                            var atk = zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                            if (atk != null) atk.setBaseValue(0.5);
                            var hp = zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                            if (hp != null) hp.setBaseValue(200.0);
                            zombie.setHealth(200.0);
                            zombie.getEquipment().clear();
                            zombie.setCanPickupItems(false);
                        });
                        dummies.set(i, newZ);
                        continue;
                    }
                    // Tether: teleport back if too far from spawn
                    if (z.getLocation().distance(sp) > 10) {
                        z.teleport(sp);
                    }
                    // Regen: heal slowly
                    if (z.getHealth() < 200.0) {
                        z.setHealth(Math.min(200.0, z.getHealth() + 2.0));
                    }
                    // Remove fire ticks for clean testing
                    z.setFireTicks(0);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void clearDummies() {
        for (Zombie z : dummies) { if (z.isValid()) z.remove(); }
        dummies.clear();
    }

    public boolean isDummy(Entity e) { return e instanceof Zombie z && dummies.contains(z); }
}
