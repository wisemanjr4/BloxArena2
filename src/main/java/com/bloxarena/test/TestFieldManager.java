/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.attribute.AttributeInstance
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Zombie
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 */
package com.bloxarena.test;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.TeamColor;
import com.bloxarena.kit.KitBuilder;
import com.bloxarena.kit.KitType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TestFieldManager {
    private final BloxArenaPlugin plugin;
    private final Set<UUID> testers = new HashSet<UUID>();
    private final Map<UUID, Location> returnLocations = new HashMap<UUID, Location>();
    private final Map<UUID, KitType> testKits = new HashMap<UUID, KitType>();
    private final List<Zombie> dummies = new ArrayList<Zombie>();
    private BukkitTask skillUpdateTask = null;
    private BukkitTask dummyTask = null;
    private Location testSpawn;
    private Location testAreaMin;
    private Location testAreaMax;
    private int dummyCount = 3;
    private World testWorld;

    public TestFieldManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        FileConfiguration cfg = this.plugin.getConfig();
        if (cfg.contains("test_field.spawn.x")) {
            String worldName = cfg.getString("test_field.spawn.world", "world");
            this.testWorld = Bukkit.getWorld((String)worldName);
            this.testSpawn = new Location(this.testWorld, cfg.getDouble("test_field.spawn.x", 0.0), cfg.getDouble("test_field.spawn.y", 64.0), cfg.getDouble("test_field.spawn.z", 0.0));
            this.dummyCount = cfg.getInt("test_field.dummy_count", 3);
        }
        if (cfg.contains("test_field.area.min.x")) {
            this.testAreaMin = new Location(this.testWorld, cfg.getDouble("test_field.area.min.x"), cfg.getDouble("test_field.area.min.y"), cfg.getDouble("test_field.area.min.z"));
            this.testAreaMax = new Location(this.testWorld, cfg.getDouble("test_field.area.max.x"), cfg.getDouble("test_field.area.max.y"), cfg.getDouble("test_field.area.max.z"));
        }
    }

    public void setArea(Location min, Location max) {
        this.testAreaMin = min.clone();
        this.testAreaMax = max.clone();
        FileConfiguration cfg = this.plugin.getConfig();
        cfg.set("test_field.area.world", (Object)min.getWorld().getName());
        cfg.set("test_field.area.min.x", (Object)min.getX());
        cfg.set("test_field.area.min.y", (Object)min.getY());
        cfg.set("test_field.area.min.z", (Object)min.getZ());
        cfg.set("test_field.area.max.x", (Object)max.getX());
        cfg.set("test_field.area.max.y", (Object)max.getY());
        cfg.set("test_field.area.max.z", (Object)max.getZ());
        this.plugin.saveConfig();
    }

    public boolean hasArea() {
        return this.testAreaMin != null && this.testAreaMax != null;
    }

    public boolean isInArea(Location loc) {
        if (this.testAreaMin == null || this.testAreaMax == null) {
            return true;
        }
        if (loc.getWorld() != this.testAreaMin.getWorld()) {
            return false;
        }
        return loc.getX() >= Math.min(this.testAreaMin.getX(), this.testAreaMax.getX()) && loc.getX() <= Math.max(this.testAreaMin.getX(), this.testAreaMax.getX()) && loc.getY() >= Math.min(this.testAreaMin.getY(), this.testAreaMax.getY()) && loc.getY() <= Math.max(this.testAreaMin.getY(), this.testAreaMax.getY()) && loc.getZ() >= Math.min(this.testAreaMin.getZ(), this.testAreaMax.getZ()) && loc.getZ() <= Math.max(this.testAreaMin.getZ(), this.testAreaMax.getZ());
    }

    public boolean isActive() {
        return this.testSpawn != null;
    }

    public Location getSpawn() {
        return this.testSpawn;
    }

    public void enter(Player p) {
        if (this.testSpawn == null) {
            p.sendMessage("\u00a7c\u30c6\u30b9\u30c8\u5834\u304c\u8a2d\u5b9a\u3055\u308c\u3066\u3044\u307e\u305b\u3093\u3002config.yml\u306etest_field\u3092\u8a2d\u5b9a\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        if (this.testers.contains(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u3059\u3067\u306b\u30c6\u30b9\u30c8\u5834\u306b\u5165\u3063\u3066\u3044\u307e\u3059\u3002 \u00a7f/ba test leave \u00a7c\u3067\u9000\u51fa");
            return;
        }
        this.returnLocations.put(p.getUniqueId(), p.getLocation().clone());
        this.testers.add(p.getUniqueId());
        p.setGameMode(GameMode.SURVIVAL);
        p.teleport(this.testSpawn.clone().add(Math.random() * 2.0 - 1.0, 0.0, Math.random() * 2.0 - 1.0));
        p.getInventory().clear();
        p.setHealth(20.0);
        p.setFoodLevel(20);
        for (PotionEffect eff : p.getActivePotionEffects()) {
            p.removePotionEffect(eff.getType());
        }
        p.sendMessage("\u00a7a\u00a7l\u30c6\u30b9\u30c8\u5834\u306b\u5165\u308a\u307e\u3057\u305f\uff01");
        p.sendMessage("\u00a77\u00a7f/ba kits \u00a77\u3067\u30ad\u30c3\u30c8\u4e00\u89a7 \u2192 \u30af\u30ea\u30c3\u30af\u3067\u5373\u88c5\u5099");
        p.sendMessage("\u00a77\u30c0\u30df\u30fc \u00a7f" + this.dummyCount + "\u4f53 \u00a77\u304c\u81ea\u52d5\u5fa9\u6d3b\u3057\u307e\u3059");
        p.sendMessage("\u00a77\u00a7f/ba test leave \u00a77\u3067\u9000\u51fa\uff08\u72b6\u614b\u30ea\u30bb\u30c3\u30c8\uff09");
        if (this.dummies.isEmpty()) {
            this.spawnDummies();
        }
        if (this.skillUpdateTask == null) {
            this.skillUpdateTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
                if (this.testers.isEmpty()) {
                    return;
                }
                this.plugin.getSkillManager().update();
                this.plugin.getSkillManager().updateTurrets();
            }, 0L, 20L);
        }
    }

    public void leave(Player p) {
        this.testers.remove(p.getUniqueId());
        this.testKits.remove(p.getUniqueId());
        this.returnLocations.remove(p.getUniqueId());
        p.setGameMode(GameMode.SURVIVAL);
        p.getInventory().clear();
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setSaturation(5.0f);
        p.setWalkSpeed(0.2f);
        p.setInvulnerable(false);
        p.setFireTicks(0);
        for (PotionEffect eff : p.getActivePotionEffects()) {
            p.removePotionEffect(eff.getType());
        }
        Location lobby = this.plugin.getLobbyManager().getLobbySpawn();
        if (lobby != null) {
            p.teleport(lobby);
        }
        p.sendMessage("\u00a7a\u30c6\u30b9\u30c8\u5834\u304b\u3089\u9000\u51fa\u3057\u307e\u3057\u305f\u3002\u72b6\u614b\u3092\u30ea\u30bb\u30c3\u30c8\u3057\u307e\u3057\u305f\u3002");
        if (this.testers.isEmpty()) {
            this.clearDummies();
            if (this.skillUpdateTask != null) {
                this.skillUpdateTask.cancel();
                this.skillUpdateTask = null;
            }
        }
    }

    public void giveTestKit(Player p, KitType kit) {
        if (!this.testers.contains(p.getUniqueId())) {
            return;
        }
        this.testKits.put(p.getUniqueId(), kit);
        this.plugin.getGameManager().setPlayerKit(p.getUniqueId(), kit.name());
        KitBuilder.giveKit(p, kit, TeamColor.RED, this.plugin);
        p.sendMessage("\u00a7a" + kit.getDisplayName() + " \u00a7a\u3092\u88c5\u5099\u3057\u307e\u3057\u305f\uff01");
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
    }

    public boolean isTester(Player p) {
        return this.testers.contains(p.getUniqueId());
    }

    public void quit(Player p) {
        this.testers.remove(p.getUniqueId());
        this.testKits.remove(p.getUniqueId());
        this.returnLocations.remove(p.getUniqueId());
        if (this.testers.isEmpty()) {
            this.clearDummies();
            if (this.skillUpdateTask != null) {
                this.skillUpdateTask.cancel();
                this.skillUpdateTask = null;
            }
        }
    }

    private void spawnDummies() {
        if (this.dummyTask != null) {
            this.dummyTask.cancel();
            this.dummyTask = null;
        }
        this.clearDummies();
        final ArrayList<Location> spawnPoints = new ArrayList<Location>();
        for (int i = 0; i < this.dummyCount; ++i) {
            int idx = i;
            Location loc = this.testSpawn.clone().add((double)((i - this.dummyCount / 2) * 3), 0.0, 5.0);
            spawnPoints.add(loc.clone());
            Zombie z = (Zombie)this.testWorld.spawn(loc, Zombie.class, zombie -> {
                AttributeInstance hp;
                AttributeInstance atk;
                zombie.setCustomName("\u00a7e\u00a7l\u8a13\u7df4\u7528\u30c0\u30df\u30fc " + (idx + 1));
                zombie.setCustomNameVisible(true);
                zombie.setBaby(false);
                zombie.setRemoveWhenFarAway(false);
                zombie.setShouldBurnInDay(false);
                zombie.setAI(true);
                zombie.setSilent(false);
                AttributeInstance speed = zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                if (speed != null) {
                    speed.setBaseValue(0.05);
                }
                if ((atk = zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)) != null) {
                    atk.setBaseValue(0.5);
                }
                if ((hp = zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH)) != null) {
                    hp.setBaseValue(200.0);
                }
                zombie.setHealth(200.0);
                zombie.getEquipment().clear();
                zombie.setCanPickupItems(false);
            });
            this.dummies.add(z);
        }
        this.dummyTask = new BukkitRunnable(){

            public void run() {
                if (TestFieldManager.this.testers.isEmpty()) {
                    this.cancel();
                    return;
                }
                for (int i = 0; i < TestFieldManager.this.dummies.size(); ++i) {
                    int idx2 = i;
                    Zombie z = TestFieldManager.this.dummies.get(i);
                    Location sp = (Location)spawnPoints.get(i);
                    if (!z.isValid() || z.isDead()) {
                        z.remove();
                        Zombie newZ = (Zombie)TestFieldManager.this.testWorld.spawn(sp, Zombie.class, zombie -> {
                            AttributeInstance hp;
                            AttributeInstance atk;
                            zombie.setCustomName("\u00a7e\u00a7l\u8a13\u7df4\u7528\u30c0\u30df\u30fc " + (idx2 + 1));
                            zombie.setCustomNameVisible(true);
                            zombie.setBaby(false);
                            zombie.setRemoveWhenFarAway(false);
                            zombie.setShouldBurnInDay(false);
                            zombie.setAI(true);
                            zombie.setSilent(false);
                            AttributeInstance speed = zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                            if (speed != null) {
                                speed.setBaseValue(0.05);
                            }
                            if ((atk = zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)) != null) {
                                atk.setBaseValue(0.5);
                            }
                            if ((hp = zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH)) != null) {
                                hp.setBaseValue(200.0);
                            }
                            zombie.setHealth(200.0);
                            zombie.getEquipment().clear();
                            zombie.setCanPickupItems(false);
                        });
                        TestFieldManager.this.dummies.set(i, newZ);
                        continue;
                    }
                    if (z.getLocation().distance(sp) > 10.0) {
                        z.teleport(sp);
                    }
                    if (z.getHealth() < 200.0) {
                        z.setHealth(Math.min(200.0, z.getHealth() + 2.0));
                    }
                    z.setFireTicks(0);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 20L, 20L);
    }

    private void clearDummies() {
        if (this.dummyTask != null) {
            this.dummyTask.cancel();
            this.dummyTask = null;
        }
        for (Zombie z : this.dummies) {
            if (!z.isValid()) continue;
            z.remove();
        }
        this.dummies.clear();
    }

    public boolean isDummy(Entity e) {
        Zombie z;
        return e instanceof Zombie && this.dummies.contains(z = (Zombie)e);
    }
}

