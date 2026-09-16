package com.bloxarena.ultimate;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameManager;
import com.bloxarena.game.GameMode;
import com.bloxarena.game.GameState;
import com.bloxarena.game.TeamColor;
import com.bloxarena.kit.KitBuilder;
import com.bloxarena.kit.KitRole;
import com.bloxarena.kit.KitType;
import com.bloxarena.skill.SkillManager;
import com.bloxarena.util.AnimatedText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class UltimateManager {
    private final BloxArenaPlugin plugin;
    private final GameManager gm;
    private final SkillManager skillManager;
    private final Map<UUID, Integer> charge = new HashMap<UUID, Integer>();
    private final Map<UUID, String> roundCarryKit = new HashMap<UUID, String>();
    private final Map<UUID, Integer> roundCarryCharge = new HashMap<UUID, Integer>();
    private final Map<UUID, Long> lastTickTime = new HashMap<UUID, Long>();
    private final Map<UUID, Double> chargeFraction = new HashMap<UUID, Double>();
    private final Map<UUID, Location> swapperMark = new HashMap<UUID, Location>();
    private final Map<UUID, Location> rewindMark = new HashMap<UUID, Location>();
    private final Map<UUID, Long> glaciesHitCooldown = new HashMap<UUID, Long>();
    private final Map<UUID, Long> chargeLockUntil = new HashMap<UUID, Long>();
    private long universalLockUntil = 0L;

    public UltimateManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
        this.skillManager = plugin.getSkillManager();
    }

    public void addCharge(UUID player, int amount) {
        long now = System.currentTimeMillis();
        if (now < this.universalLockUntil || now < this.chargeLockUntil.getOrDefault(player, 0L)) {
            return;
        }
        if (amount <= 0) {
            return;
        }
        GameMode mode = this.gm.getCurrentGameMode();
        if (mode == GameMode.TEAM_DEATHMATCH || mode == GameMode.CAPTURE_THE_FLAG) {
            amount = (int)Math.round((double)amount * 1.5);
        } else if (mode == GameMode.BATTLE_ARENA) {
            amount = (int)Math.round((double)amount * 0.5);
        }
        KitType kit = this.gm.getPlayerKitType(player);
        int required = this.requiredCharge(kit);
        int cur = this.charge.getOrDefault(player, 0);
        int next = Math.min(required, cur + amount);
        this.charge.put(player, next);
        if (cur < required && next >= required) {
            Player ready = Bukkit.getPlayer((UUID)player);
            if (ready != null && ready.isOnline()) {
                AnimatedText.slideIn(this.plugin, List.of(ready), "\u26a1 ULT READY \u26a1 \u3057\u3083\u304c\u307f+\u5de6\u30af\u30ea\u30c3\u30af\u3067\u89e3\u653e", 40);
                ready.playSound(ready.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.7f);
            }
        }
    }

    public int requiredCharge(KitType kit) {
        if (kit == null) {
            return 100;
        }
        switch (kit) {
            case NINJA: return 150;
            case VAMPIRE: return 160;
            case BREAKER: return 150;
            case BERSERKER: return 140;
            case SNIPER: return 140;
            case SUNDANCE: return 150;
            case LANCER: return 110;
            case ENGINEER: return 130;
            case GUARDIAN: return 130;
            case MARKSMAN: return 120;
            case SWAPPER: return 120;
            case TRANSPORTER: return 120;
            case NECRO: return 130;
            case HEXER: return 120;
            case SUPPORTER: return 120;
            case PHANTOM: return 130;
            case BOMBER: return 110;
            case SCOUT: return 110;
            case BLADE: return 90;
            case COOK: return 100;
            case FLASHER: return 100;
            case STICKER: return 100;
            case DECOY: return 100;
            case ROCKETER: return 100;
            case ALCHEMIST: return 140;
            case ANCHOR: return 100;
            case TRAPPER: return 100;
            case GLACIES: return 100;
            case MEDIC: return 100;
            case PYRO: return 100;
            case JESTER: return 90;
            case RESTRICTIONER: return 90;
            case TRINGID: return 150;
            case SUPERIOR_MISTRAL: return 90;
            case AEGIS: return 90;
            case COUNTER: return 110;
            case KREUTZ: return 110;
            case NILGIRITAR: return 80;
            case RELEASER: return 80;
            case BULWARK: return 80;
            case GRANG: return 70;
            case WHIRLWIND: return 80;
            case MISTRAL: return 60;
            case TIMEKEEPER: return 60;
            case REFLECTOR: return 60;
            default: return 100;
        }
    }

    public int getCharge(UUID player) {
        return this.charge.getOrDefault(player, 0);
    }

    public int getChargePercent(UUID player) {
        int req = this.requiredCharge(this.gm.getPlayerKitType(player));
        if (req <= 0) {
            return 100;
        }
        return Math.min(100, (int)Math.round((double)this.getCharge(player) * 100.0 / (double)req));
    }

    public boolean canUltimate(UUID player) {
        return this.charge.getOrDefault(player, 0) >= this.requiredCharge(this.gm.getPlayerKitType(player));
    }

    public void resetAll() {
        this.charge.clear();
        this.lastTickTime.clear();
        this.chargeFraction.clear();
        this.swapperMark.clear();
        this.rewindMark.clear();
        this.glaciesHitCooldown.clear();
        this.chargeLockUntil.clear();
        this.universalLockUntil = System.currentTimeMillis() + 5000L;
        for (Map.Entry<UUID, String> e : this.roundCarryKit.entrySet()) {
            if (e.getValue().equals(this.gm.getPlayerKit(e.getKey()))) {
                Integer carried = this.roundCarryCharge.get(e.getKey());
                if (carried != null && carried > 0) {
                    this.charge.put(e.getKey(), carried);
                }
            }
        }
        this.roundCarryKit.clear();
        this.roundCarryCharge.clear();
    }

    public void captureRoundCarry(Map<UUID, String> kits) {
        this.roundCarryKit.clear();
        this.roundCarryCharge.clear();
        for (Map.Entry<UUID, String> e : kits.entrySet()) {
            this.roundCarryKit.put(e.getKey(), e.getValue());
            this.roundCarryCharge.put(e.getKey(), this.charge.getOrDefault(e.getKey(), 0));
        }
    }

    public void clearRoundCarry() {
        this.roundCarryKit.clear();
        this.roundCarryCharge.clear();
    }

    public void addDamageCharge(Player p, double damage) {
        if (!participant(p) || damage <= 0.0) {
            return;
        }
        KitType kit = this.gm.getPlayerKitType(p.getUniqueId());
        if (kit == null) {
            return;
        }
        double pts = damage * 0.3;
        GameMode mode = this.gm.getCurrentGameMode();
        if (mode == GameMode.TEAM_DEATHMATCH || mode == GameMode.CAPTURE_THE_FLAG) {
            pts *= 1.5;
        }
        int cur = this.charge.getOrDefault(p.getUniqueId(), 0);
        int max = this.requiredCharge(kit);
        this.charge.put(p.getUniqueId(), Math.min(max, cur + (int)pts));
    }

    public void tickCharge(Player p) {
        if (!participant(p)) {
            return;
        }
        KitType kit = this.gm.getPlayerKitType(p.getUniqueId());
        long now = System.currentTimeMillis();
        long last = this.lastTickTime.getOrDefault(p.getUniqueId(), now);
        this.lastTickTime.put(p.getUniqueId(), now);
        long elapsed = now - last;
        if (elapsed <= 0L) {
            return;
        }
        double rate = 2.0;
        double gained = elapsed / 1000.0 * rate + this.chargeFraction.getOrDefault(p.getUniqueId(), 0.0);
        int whole = (int)gained;
        this.chargeFraction.put(p.getUniqueId(), gained - (double)whole);
        if (whole > 0) {
            this.addCharge(p.getUniqueId(), whole);
        }
    }

    public String getUltimateBarText(Player p) {
        int pct = this.getChargePercent(p.getUniqueId());
        return pct >= 100 ? "\u00a7d\u26a1 ULT \u6e96\u5099\u5b8c\u4e86\uff01 \u00a77\u3057\u3083\u304c\u307f+\u5de6\u30af\u30ea\u30c3\u30af\u3067\u89e3\u653e" : "\u00a7d\u26a1 ULT \u00a7f[" + pct + "%]";
    }

    public void activateUltimate(Player p) {
        if (!participant(p)) {
            return;
        }
        if (!this.canUltimate(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u26a1 ULT\u30c1\u30e3\u30fc\u30b8\u4e0d\u8db3 \u00a78\u00bb \u00a77\u73fe\u5728 " + this.getChargePercent(p.getUniqueId()) + "%");
            return;
        }
        KitType kit = this.gm.getPlayerKitType(p.getUniqueId());
        if (kit == null) {
            return;
        }
        this.charge.put(p.getUniqueId(), 0);
        this.chargeLockUntil.put(p.getUniqueId(), System.currentTimeMillis() + 5000L);
        Bukkit.broadcastMessage((String)("\u00a7d\u26a1 \u00a7f" + p.getName() + " \u00a7d\u00a7l\u304c\u30a2\u30eb\u30c6\u30a3\u30e1\u30c3\u30c8\u300c" + this.ultimateName(kit) + "\u300d\u3092\u89e3\u653e\uff01"));
        this.playUltimateIntro(p, kit);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (p.isOnline() && this.gm.isParticipant(p) && !this.gm.isSpectator(p)) {
                this.applyUltimate(p, kit);
            }
        }, 15L);
    }

    private void applyUltimate(Player p, KitType kit) {
        switch (kit) {
            case BLADE: this.bladeUltimate(p); break;
            case BREAKER: this.breakerUltimate(p); break;
            case NINJA: this.ninjaUltimate(p); break;
            case BERSERKER: this.berserkerUltimate(p); break;
            case SNIPER: this.skillManager.setSniperUltimate(p); break;
            case COUNTER: this.skillManager.setCounterUltimate(p); break;
            case PYRO: this.pyroUltimate(p); break;
            case LANCER: this.skillManager.setLancerUltimate(p); break;
            case JESTER: this.jesterUltimate(p); break;
            case VAMPIRE: this.skillManager.setVampireUltimate(p); break;
            case BOMBER: this.bomberUltimate(p); break;
            case COOK: this.skillManager.grantFullCourse(p); break;
            case GRANG: this.skillManager.setGrangUltimate(p); break;
            case SCOUT: this.scoutUltimate(p); break;
            case FLASHER: this.flasherUltimate(p); break;
            case MARKSMAN: this.skillManager.setMarksmanUltimate(p); break;
            case SUNDANCE: this.skillManager.setSundanceUltimate(p); break;
            case SWAPPER: this.swapperUltimate(p); break;
            case STICKER: this.stickerUltimate(p); break;
            case DECOY: this.decoyUltimate(p); break;
            case WHIRLWIND: this.whirlwindUltimate(p); break;
            case NILGIRITAR: this.nilgiritarUltimate(p); break;
            case MISTRAL: this.mistralUltimate(p); break;
            case TRINGID: this.skillManager.setTringidUltimate(p); break;
            case SUPERIOR_MISTRAL: this.skillManager.setSuperiorMistralUltimate(p); break;
            case ROCKETER: this.rocketerUltimate(p); break;
            case ALCHEMIST: this.alchemistUltimate(p); break;
            case ENGINEER: this.engineerUltimate(p); break;
            case RESTRICTIONER: this.restrictionerUltimate(p); break;
            case TRANSPORTER: this.skillManager.setTransporterUltimate(p); break;
            case KREUTZ: this.skillManager.kreutzFullOrder(p); break;
            case NECRO: this.skillManager.expandNecroArmy(p, 6); break;
            case TIMEKEEPER: this.timekeeperUltimate(p); break;
            case HEXER: this.hexerUltimate(p); break;
            case GLACIES: this.skillManager.setGlaciesUltimate(p); break;
            case TRAPPER: this.skillManager.setTrapperUltimate(p); break;
            case GUARDIAN: this.guardianUltimate(p); break;
            case MEDIC: this.medicUltimate(p); break;
            case SUPPORTER: this.supporterUltimate(p); break;
            case PHANTOM: this.phantomUltimate(p); break;
            case ANCHOR: this.anchorUltimate(p); break;
            case RELEASER: this.releaserUltimate(p); break;
            case BULWARK: this.skillManager.bulwarkUltimate(p); break;
            case AEGIS: this.aegisUltimate(p); break;
            case REFLECTOR: this.reflectorUltimate(p); break;
        }
    }

    private void bladeUltimate(final Player p) {
        final Vector dir = p.getLocation().getDirection().normalize();
        final Location start = p.getLocation().clone().add(0.0, 0.5, 0.0);
        final double maxRange = 39.0;
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.2f, 0.6f);
        p.sendMessage("\u00a7c\u00a7l\u30c7\u30a3\u30e1\u30f3\u30b7\u30e7\u30f3\u30fb\u30d6\u30ec\u30a4\u30af \u8d77\u52d5\u2026 \u00a77(\u767a\u5c04\u307e\u30671\u79d2)");
        new BukkitRunnable(){
            int t = 0;

            public void run() {
                if (this.t++ >= 20) {
                    this.cancel();
                    UltimateManager.this.bladeUltimateFire(p, start, dir, maxRange);
                    return;
                }
                for (double d = 1.0; d <= maxRange; d += 1.0) {
                    Location wp = start.clone().add(dir.clone().multiply(d));
                    wp.getWorld().spawnParticle(Particle.REDSTONE, wp, 2, 0.15, 0.15, 0.15, 0.0, new Particle.DustOptions(Color.RED, 1.8f));
                }
                if (this.t % 4 == 0) {
                    for (Player en : start.getWorld().getPlayers()) {
                        if (!UltimateManager.this.isEnemy(p, en)) continue;
                        if (UltimateManager.this.distanceToSegment(en.getLocation().clone().add(0.0, 1.0, 0.0), start, dir, maxRange) <= 3.0) {
                            en.playSound(en.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.9f, 1.9f);
                        }
                    }
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void bladeUltimateFire(Player p, Location start, Vector dir, double maxRange) {
        for (double d = 0.5; d <= maxRange; d += 0.5) {
            Location check = start.clone().add(dir.clone().multiply(d));
            check.getWorld().spawnParticle(Particle.SWEEP_ATTACK, check, 2, 0.3, 0.2, 0.3, 0.0);
        }
        p.getWorld().playSound(start, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.5f);
        for (double d = 0.0; d <= maxRange; d += 0.5) {
            Location check = start.clone().add(dir.clone().multiply(d));
            for (Entity e : check.getWorld().getNearbyEntities(check, 1.2, 1.5, 1.2)) {
                if (!(e instanceof Player) || !this.isEnemy(p, (Player)e)) continue;
                Player bladeVictim = (Player)e;
                bladeVictim.damage(15.0, (Entity)p);
                this.killEffect(bladeVictim);
                bladeVictim.damage(0.5, (Entity)p);
                this.killEffect(bladeVictim);
                return;
            }
        }
    }

    private double distanceToSegment(Location point, Location origin, Vector dir, double maxRange) {
        Vector toPoint = point.toVector().subtract(origin.toVector());
        double proj = toPoint.dot(dir);
        if (proj < 0.0 || proj > maxRange) {
            Vector endVec = origin.clone().add(dir.clone().multiply(maxRange)).toVector();
            return Math.min(point.toVector().distance(origin.toVector()), point.toVector().distance(endVec));
        }
        Vector closest = origin.toVector().add(dir.clone().multiply(proj));
        return point.toVector().distance(closest);
    }

    private void breakerUltimate(final Player p) {
        final Vector dir = p.getLocation().getDirection().setY(0).normalize();
        final Location from = p.getLocation().clone();
        Location dest = from.clone();
        boolean blocked = false;
        for (double d = 1.0; d <= 15.0; d += 0.5) {
            Location check = from.clone().add(dir.clone().multiply(d)).add(0.0, 1.0, 0.0);
            Material type = check.getBlock().getType();
            if (type.isSolid()) {
                dest = check.clone().add(dir.clone().multiply(-1.0)).add(0.0, -1.0, 0.0);
                blocked = true;
                break;
            }
            dest = check.clone().add(0.0, -1.0, 0.0);
        }
        if (blocked) {
            dest = from.clone().add(dir.clone().multiply(Math.max(0.0, from.distance(dest) - 1.0)));
        }
        p.teleport(dest.clone().add(0.0, 0.0, 0.0));
        p.getWorld().playSound(dest, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.7f);
        for (double d = 0.0; d <= from.distance(dest) + 1.0; d += 0.4) {
            Location check = from.clone().add(dir.clone().multiply(d));
            for (Entity e : check.getWorld().getNearbyEntities(check, 1.0, 2.0, 1.0)) {
                if (!(e instanceof Player) || !UltimateManager.this.isEnemy(p, (Player)e)) continue;
                Player t = (Player)e;
                t.damage(20.0, (Entity)p);
                UltimateManager.this.killEffect(t);
                t.setVelocity(dir.clone().multiply(3.0).setY(0.8));
            }
        }
    }

    private void ninjaUltimate(Player p) {
        Location origin = p.getLocation().clone();
        this.refillEnderPearls(p, 4);
        p.sendMessage("\u00a77\u30a8\u30f3\u30c0\u30fc\u30d1\u30fc\u30eb\u3092\u88dc\u5145\u3057\u305f\uff01");
        this.skillManager.spawnDecoy(p, origin);
        this.skillManager.hideArmor(p);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 160, 1, false, false));
        p.getWorld().spawnParticle(Particle.PORTAL, origin.clone().add(0.0, 1.0, 0.0), 40, 0.4, 0.9, 0.4, 0.1);
        p.getWorld().playSound(origin, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 1.4f);
        Player aimer = this.findAimer(p, 15.0);
        if (aimer != null) {
            Vector back = aimer.getLocation().getDirection().multiply(-1.0).setY(0).normalize();
            Location behind = aimer.getLocation().clone().add(back.clone().multiply(1.5));
            if (behind.getBlock().getType().isSolid() || behind.clone().add(0.0, 1.0, 0.0).getBlock().getType().isSolid()) {
                behind = aimer.getLocation().clone();
            }
            behind.setY(behind.getY() + 0.2);
            behind.setDirection(aimer.getLocation().getDirection());
            p.teleport(behind);
            p.getWorld().spawnParticle(Particle.PORTAL, behind.clone().add(0.0, 1.0, 0.0), 40, 0.4, 0.9, 0.4, 0.1);
            p.getWorld().playSound(behind, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 1.4f);
            p.sendMessage("\u00a75\u00a7l\u5f71\u6e21\u308a\uff01 \u00a77\u76f8\u624b\u306e\u80cc\u5f8c\u3078\u8ee2\u79fb\u3057\u305f");
        } else {
            p.sendMessage("\u00a75\u00a7l\u5f71\u6e21\u308a\uff01 \u00a77\u72d9\u3063\u3066\u3044\u308b\u6575\u304c\u3044\u306a\u3044\u305f\u3081\u305d\u306e\u5834\u306b\u6b62\u307e\u3063\u305f");
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (p.isOnline()) {
                this.skillManager.showArmor(p);
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }, 100L);
    }

    private void refillEnderPearls(Player p, int amount) {
        int have = 0;
        for (ItemStack is : p.getInventory().getContents()) {
            if (is != null && is.getType() == Material.ENDER_PEARL) {
                have += is.getAmount();
            }
        }
        if (have < amount) {
            p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, amount - have));
        }
    }

    private Player findAimer(Player self, double range) {
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : self.getNearbyEntities(range, range, range)) {
            if (!(e instanceof Player)) continue;
            Player other = (Player)e;
            if (!this.isEnemy(self, other)) continue;
            double dist = other.getLocation().distance(self.getLocation());
            if (dist > range || dist >= bestDist) continue;
            Vector toSelf = self.getEyeLocation().toVector().subtract(other.getEyeLocation().toVector());
            if (toSelf.lengthSquared() < 1.0E-4) continue;
            if (other.getEyeLocation().getDirection().dot(toSelf.normalize()) < 0.9) continue;
            if (!this.skillManager.hasLineOfSight(other.getEyeLocation(), self.getEyeLocation())) continue;
            best = other;
            bestDist = dist;
        }
        return best;
    }

    private void berserkerUltimate(final Player p) {
        final Vector dir = p.getLocation().getDirection().normalize();
        final Location start = p.getLocation().clone();
        final World w = start.getWorld();
        if (w == null) {
            return;
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.6f);
        new BukkitRunnable(){
            int t = 0;
            Location cur = start.clone();
            public void run() {
                if (this.t++ > 115) {
                    this.cancel();
                    return;
                }
                if (this.t <= 15) {
                    w.spawnParticle(Particle.FLAME, start.clone().add(0.0, 1.0, 0.0), 12, 0.4, 0.6, 0.4, 0.02);
                    w.spawnParticle(Particle.SMOKE_NORMAL, start.clone().add(0.0, 0.5, 0.0), 5, 0.3, 0.3, 0.3, 0.01);
                    return;
                }
                this.cur.add(dir.clone().multiply(0.3));
                w.createExplosion(this.cur, 0.0f, false, false, (Entity)p);
                w.spawnParticle(Particle.FLAME, this.cur, 10, 0.6, 0.6, 0.6, 0.03);
                for (Player bersVictim : UltimateManager.this.areaExplosion(p, this.cur, 3.5, 3.5, 20.0, 1.8, 0.5, false)) {
                    UltimateManager.this.killEffect(bersVictim);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void pyroUltimate(final Player p) {
        final Vector dir = p.getLocation().getDirection().normalize();
        final Location start = p.getLocation().clone().add(dir.clone().multiply(2.0));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.5f);
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0.0, 1.0, 0.0), 40, 0.5, 1.0, 0.5, 0.1);
        p.getWorld().spawnParticle(Particle.LAVA, p.getLocation().add(0.0, 1.0, 0.0), 10, 0.5, 0.8, 0.5, 0.0);
        new BukkitRunnable(){
            int t = 0;
            Location cur = start.clone();
            public void run() {
                if (this.t++ > 60) {
                    World we = this.cur.getWorld();
                    we.playSound(this.cur, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
                    we.spawnParticle(Particle.EXPLOSION_HUGE, this.cur, 5, 1.0, 1.0, 1.0, 0.1);
                    we.spawnParticle(Particle.FLAME, this.cur, 60, 2.0, 2.0, 2.0, 0.05);
                    we.spawnParticle(Particle.LAVA, this.cur, 20, 1.5, 1.5, 1.5, 0.0);
                    for (Entity e : we.getNearbyEntities(this.cur, 4.0, 4.0, 4.0)) {
                        if (!(e instanceof Player) || !UltimateManager.this.isEnemy(p, (Player)e)) continue;
                        ((Player)e).damage(10.0, (Entity)p);
                    }
                    this.cancel();
                    return;
                }
                this.cur.add(dir.clone().multiply(0.9));
                World w = this.cur.getWorld();
                for (int dx = -2; dx <= 2; ++dx) {
                    for (int dy = -2; dy <= 2; ++dy) {
                        for (int dz = -2; dz <= 2; ++dz) {
                            w.spawnParticle(Particle.FLAME, this.cur.clone().add(dx, dy + 0.5, dz), 1, 0.2, 0.2, 0.2, 0.01);
                        }
                    }
                }
                w.spawnParticle(Particle.LAVA, this.cur.clone().add(0.0, 1.0, 0.0), 2, 1.5, 1.5, 1.5, 0.0);
                for (Entity e : w.getNearbyEntities(this.cur, 2.5, 2.5, 2.5)) {
                    if (!(e instanceof Player) || !UltimateManager.this.isEnemy(p, (Player)e)) continue;
                    Player t2 = (Player)e;
                    t2.setFireTicks(100);
                    Bukkit.getScheduler().runTaskLater((Plugin)UltimateManager.this.plugin, () -> {
                        if (t2.isOnline() && t2.getFireTicks() > 0) {
                            t2.damage(12.0, (Entity)p);
                        }
                    }, 20L);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
    }

    private void jesterUltimate(final Player p) {
        Snowball ball = (Snowball)p.launchProjectile(Snowball.class);
        ball.setVelocity(p.getLocation().getDirection().normalize().multiply(1.4));
        ball.setCustomName("jesterBind");
        ball.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "jester_bind"), PersistentDataType.BYTE, (byte)1);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.4f);
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0.0, 1.0, 0.0), 20, 0.4, 0.8, 0.4, 0.1);
        p.sendMessage("\u00a7e\u00a7l\u2726 \u30d0\u30a4\u30f3\u30c9\u30c8\u30ea\u30c3\u30af\uff01");
    }

    private void bomberUltimate(final Player p) {
        TeamColor team = this.gm.getTeamOf(p);
        for (UUID uid : this.gm.getPlayerKits().keySet()) {
            Player ally = Bukkit.getPlayer(uid);
            if (ally == null || !participant(ally)) continue;
            if (team != null && this.gm.getTeamOf(ally) != team) continue;
            ally.sendMessage("\u00a7c\u00a7l\u26a0 \u3042\u306a\u305f\u306f\u7206\u5f3e \u00a78\u00bb \u00a775\u79d2\u5f8c\u306b\u7206\u767a\uff01");
            final Player owner = p;
            new BukkitRunnable(){
                int t = 5;

                public void run() {
                    if (this.t <= 0) {
                        this.cancel();
                        Location now = ally.getLocation().clone();
                        World w = now.getWorld();
                        UltimateManager.this.areaExplosion(owner, now, 7.0, 7.0, 20.0, 1.0, 0.75, false);
                        w.spawnParticle(Particle.EXPLOSION_HUGE, now, 12, 2.0, 2.0, 2.0, 0.1);
                        w.spawnParticle(Particle.LAVA, now, 15, 1.5, 1.5, 1.5, 0.0);
                        return;
                    }
                    if (ally.isOnline()) {
                        ally.sendActionBar(net.kyori.adventure.text.Component.text(("\u00a7c\u00a7l\ud83d\udca3 \u7206\u767a\u307e\u3067 " + this.t + "\u79d2")));
                        ally.playSound(ally.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.0f + (float)(5 - this.t) * 0.15f);
                    }
                    ally.getLocation().getWorld().spawnParticle(Particle.FLAME, ally.getLocation().add(0.0, 0.5, 0.0), 5, 0.3, 0.3, 0.3, 0.01);
                    --this.t;
                }
            }.runTaskTimer((Plugin)this.plugin, 20L, 20L);
        }
    }

    private void scoutUltimate(Player p) {
        TeamColor team = this.gm.getTeamOf(p);
        for (Player t : Bukkit.getOnlinePlayers()) {
            if (!participant(t) || (team != null && this.gm.getTeamOf(t) == team)) continue;
            t.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 300, 0, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0, false, false));
        }
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
    }

    private void flasherUltimate(Player p) {
        TeamColor team = this.gm.getTeamOf(p);
        for (Player t : Bukkit.getOnlinePlayers()) {
            if (!participant(t) || (team != null && this.gm.getTeamOf(t) == team)) continue;
            t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 140, 0, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 2, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 0, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 80, 65, false, false));
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 2.0f);
    }

    private void swapperUltimate(Player p) {
        Block b = p.getTargetBlockExact(50, FluidCollisionMode.NEVER);
        Location mark = b != null ? b.getLocation().add(0.5, 1.0, 0.5) : p.getLocation().clone();
        this.swapperMark.put(p.getUniqueId(), mark);
        this.skillManager.setSwapperMark(p, mark);
        mark.getWorld().spawnParticle(Particle.PORTAL, mark, 30, 0.5, 1.0, 0.5, 0.1);
        p.getWorld().playSound(mark, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        p.sendMessage("\u00a75\u00a7l\u30de\u30fc\u30af\u8a18\u9332 \u00a78\u00bb \u00a77\u30bb\u30ec\u30af\u30c8\u30b9\u30ef\u30c3\u30d7\u6e96\u5099\u5b8c\u4e86");
    }

    private void stickerUltimate(final Player p) {
        final Location center = p.getLocation().clone();
        p.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
        new BukkitRunnable(){
            int t = 0;
            public void run() {
                if (this.t++ > 60) {
                    this.cancel();
                    return;
                }
                center.getWorld().spawnParticle(Particle.PORTAL, center.clone().add(0.0, 1.0, 0.0), 10, 6.0, 2.0, 6.0, 0.02);
                for (Entity e : center.getWorld().getNearbyEntities(center, 8.0, 4.0, 8.0)) {
                    if (!(e instanceof Player) || !UltimateManager.this.isEnemy(p, (Player)e)) continue;
                    Player t2 = (Player)e;
                    Vector pull = center.toVector().subtract(t2.getLocation().toVector()).normalize().multiply(0.4);
                    t2.setVelocity(t2.getVelocity().add(pull).setY(0.2));
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void decoyUltimate(Player p) {
        int range = 12;
        for (Player t : p.getWorld().getPlayers()) {
            if (!participant(t) || t.getLocation().distance(p.getLocation()) > (double)range) continue;
            t.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 105, 255, false, false));
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 0.8f);
    }

    private void whirlwindUltimate(final Player p) {
        final Vector dir = p.getLocation().getDirection().normalize();
        final Vector lift = new Vector(0.0, 0.6, 0.0);
        final Location start = p.getLocation().clone().add(dir.clone().multiply(2.0));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 0.5f);
        new BukkitRunnable(){
            int t = 0;
            int hits = 0;
            Location cur = start.clone();
            public void run() {
                if (this.t++ > 100) {
                    this.cancel();
                    return;
                }
                this.cur.add(dir.clone().multiply(0.15 * Math.pow(1.25, (double)this.hits)));
                World w = this.cur.getWorld();
                w.spawnParticle(Particle.SWEEP_ATTACK, this.cur, 8, 2.0, 2.0, 2.0, 0.05);
                if (this.t % 20 == 0) {
                    for (Entity ally : w.getNearbyEntities(this.cur, 3.0, 3.0, 3.0)) {
                        if (!(ally instanceof Player)) continue;
                        Player ap = (Player)ally;
                        if (!UltimateManager.this.gm.isParticipant(ap) || UltimateManager.this.gm.isSpectator(ap) || UltimateManager.this.isEnemy(p, ap)) continue;
                        ap.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, false, true));
                    }
                }
                for (Entity e : w.getNearbyEntities(this.cur, 3.0, 3.0, 3.0)) {
                    if (!(e instanceof Player) || !UltimateManager.this.isEnemy(p, (Player)e)) continue;
                    Player t2 = (Player)e;
                    t2.setVelocity(new Vector(t2.getVelocity().getX(), 0.6, t2.getVelocity().getZ()));
                    t2.damage(2.0, (Entity)p);
                    ++this.hits;
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
    }

    private void nilgiritarUltimate(final Player p) {
        final Player aim = this.skillManager.getTargetInSight(p, 15);
        final Vector dir = aim != null ? aim.getLocation().toVector().subtract(p.getLocation().toVector()).setY(0).normalize() : p.getLocation().getDirection().setY(0).normalize();
        p.setVelocity(dir.clone().multiply(1.9).setY(0.35));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.5f, 1.4f);
        AnimatedText.pop(this.plugin, List.of(p), "\u30d1\u30a4\u30eb\u30c9\u30e9\u30a4\u30d0\u30fc", "\u7a81\u9032\uff01\u547d\u4e2d\u3055\u305b\u308d\uff01", 8);
        final Player owner = p;
        new BukkitRunnable() {
            int t = 0;

            public void run() {
                if (this.t++ > 20 || !owner.isOnline()) {
                    this.cancel();
                    return;
                }
                owner.getWorld().spawnParticle(Particle.CLOUD, owner.getLocation(), 5, 0.3, 0.2, 0.3, 0.02);
                owner.getWorld().spawnParticle(Particle.CRIT, owner.getLocation().add(0.0, 1.0, 0.0), 4, 0.3, 0.3, 0.3, 0.05);
                for (Entity e : owner.getNearbyEntities(1.8, 1.6, 1.8)) {
                    if (!(e instanceof Player)) continue;
                    Player victim = (Player)e;
                    if (!UltimateManager.this.isEnemy(owner, victim)) continue;
                    this.cancel();
                    UltimateManager.this.pileDriver(owner, victim);
                    return;
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void pileDriver(final Player owner, final Player victim) {
        victim.setVelocity(new Vector(0, 0, 0));
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 1.6f);
        AnimatedText.glitch(this.plugin, List.of(victim), "\u00a7c\u00a7l\u30d1\u30a4\u30eb\u30c9\u30e9\u30a4\u30d0\u30fc", "\u9023\u6483\u3092\u53d7\u3051\u3066\u3044\u308b\uff01", 10);
        new BukkitRunnable() {
            int hit = 0;

            public void run() {
                if (this.hit++ >= 20) {
                    this.cancel();
                    victim.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, victim.getLocation().add(0.0, 1.0, 0.0), 2, 0.3, 0.3, 0.3, 0.0);
                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
                    victim.setVelocity(new Vector(0, 0.6, 0).add(owner.getLocation().getDirection().setY(0).normalize().multiply(1.0)));
                    return;
                }
                if (!victim.isOnline() || !victim.isValid()) {
                    this.cancel();
                    return;
                }
                victim.setVelocity(new Vector(0, 0, 0));
                victim.setNoDamageTicks(0);
                victim.damage(0.01, (Entity)owner);
                victim.setHealth(Math.max(0.001, victim.getHealth() - 0.75));
                victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0.0, 1.0, 0.0), 6, 0.3, 0.4, 0.3, 0.15);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.7f, 1.2f + (float)this.hit * 0.03f);
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void mistralUltimate(final Player p) {
        final Vector dir = p.getLocation().getDirection().normalize();
        final Location start = p.getLocation().clone().add(dir.clone().multiply(2.0));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 0.8f);
        new BukkitRunnable(){
            int t = 0;
            Location cur = start.clone();
            public void run() {
                if (this.t++ > 60) {
                    this.cancel();
                    return;
                }
                this.cur.add(dir.clone().multiply(0.3));
                World w = this.cur.getWorld();
                w.spawnParticle(Particle.CLOUD, this.cur, 8, 3.0, 3.0, 3.0, 0.1);
                for (Entity e : w.getNearbyEntities(this.cur, 4.0, 3.0, 4.0)) {
                    if (!(e instanceof Player) || !UltimateManager.this.isEnemy(p, (Player)e)) continue;
                    Player t2 = (Player)e;
                    t2.setVelocity(t2.getLocation().toVector().subtract(this.cur.toVector()).normalize().multiply(2.0).setY(0.5));
                    t2.damage(4.0, (Entity)p);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void rocketerUltimate(final Player p) {
        Block b = p.getTargetBlockExact(50, FluidCollisionMode.NEVER);
        final Location target = b != null ? b.getLocation().add(0.5, 0.5, 0.5) : p.getLocation().clone();
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.5f);
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0.0, 1.0, 0.0), 20, 0.3, 0.3, 0.3, 0.1);
        p.sendMessage("\u00a76\u00a7l\u30e1\u30ac\u30df\u30b5\u30a4\u30eb\u767a\u5c04 \u00a78\u00bb \u00a7710\u79d2\u5f8c\u7740\u5f3e\uff01");
        final Player owner = p;
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            World w = target.getWorld();
            w.spawnParticle(Particle.EXPLOSION_HUGE, target, 10, 3.0, 3.0, 3.0, 0.1);
            w.spawnParticle(Particle.LAVA, target, 20, 2.0, 2.0, 2.0, 0.0);
            UltimateManager.this.areaExplosion(owner, target, 10.0, 10.0, 26.0, 2.0, 0.30, true);
        }, 200L);
    }

    private void alchemistUltimate(final Player p) {
        p.sendMessage("\u00a7d\u00a7l\u88fd\u85ac\u30d5\u30a3\u30fc\u30d0\u30fc\uff01\u30dd\u30fc\u30b7\u30e7\u30f3\u304c\u6e1b\u308a\u307e\u305b\u3093\uff01");
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
        p.getWorld().spawnParticle(Particle.SPELL_WITCH, p.getLocation().add(0.0, 1.0, 0.0), 30, 0.4, 0.8, 0.4, 0.05);
        final UUID uid = p.getUniqueId();
        BukkitTask task = new BukkitRunnable(){
            public void run() {
                Player pl = Bukkit.getPlayer(uid);
                if (pl == null || !pl.isOnline() || !participant(pl)) {
                    this.cancel();
                    return;
                }
                KitBuilder.refillAlchemistPotions(pl);
            }
        }.runTaskTimer((Plugin)this.plugin, 20L, 40L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, task::cancel, 300L);
    }

    private void engineerUltimate(final Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 254, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 251, false, false));
        p.sendMessage("\u00a76\u00a7l\u2726 \u30c8\u30ea\u30ac\u30fc\u30cf\u30c3\u30d4\u30fc\uff01");
        final Player owner = p;
        new BukkitRunnable(){
            int t = 0;
            public void run() {
                if (this.t++ > 100 || !participant(owner)) {
                    this.cancel();
                    return;
                }
                Vector dir = owner.getLocation().getDirection().normalize();
                dir.add(new Vector((Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.1, (Math.random() - 0.5) * 0.3));
                Snowball bullet = (Snowball)owner.launchProjectile(Snowball.class);
                bullet.setVelocity(dir.normalize().multiply(2.5));
                bullet.setGravity(false);
                bullet.setCustomName("engineerBullet");
                bullet.getPersistentDataContainer().set(new NamespacedKey((Plugin)UltimateManager.this.plugin, "engineer_bullet"), PersistentDataType.BYTE, (byte)1);
                owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.6f, 1.5f);
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
    }

    private void restrictionerUltimate(Player p) {
        Player target = this.skillManager.getTargetInSight(p, 5);
        if (target == null) {
            p.sendMessage("\u00a7c\ud83c\udfaf \u5c04\u7a0b\u5185\u306b\u30bf\u30fc\u30b2\u30c3\u30c8\u306a\u3057");
            return;
        }
        this.skillManager.setDeadlocked(target, 2500L);
        target.getWorld().spawnParticle(Particle.CRIT_MAGIC, target.getLocation().add(0.0, 1.0, 0.0), 30, 0.4, 0.8, 0.4, 0.1);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.5f);
        target.sendMessage("\u00a78\u00a7l\u26a0 \u30de\u30b9\u30ed\u30c3\u30af\u3055\u308c\u305f\uff01");
        p.sendMessage("\u00a78\u00a7l\u2726 \u30de\u30b9\u30ed\u30c3\u30af\u6210\u529f\uff01");
    }

    private void timekeeperUltimate(Player p) {
        Player target = this.skillManager.getTargetInSight(p, 20);
        if (target == null) {
            p.sendMessage("\u00a7c\ud83c\udfaf \u30bf\u30fc\u30b2\u30c3\u30c8\u306a\u3057");
            return;
        }
        final Location mark = target.getLocation().clone();
        final Player victim = target;
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.7f);
        target.getWorld().spawnParticle(Particle.PORTAL, mark.clone().add(0.0, 1.0, 0.0), 20, 0.4, 0.8, 0.4, 0.05);
        target.sendMessage("\u00a7b\u00a7l\u30ea\u30ef\u30a4\u30f3\u30c9\u30de\u30fc\u30af \u00a78\u00bb \u00a7710\u79d2\u5f8c\u623b\u3055\u308c\u308b\uff01");
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (victim.isOnline() && victim.isValid()) {
                victim.teleport(mark);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
                victim.getWorld().spawnParticle(Particle.PORTAL, victim.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 1.0, 0.5, 0.1);
            }
        }, 200L);
    }

    private void hexerUltimate(Player p) {
        Vector facing = p.getLocation().getDirection().setY(0).normalize();
        int count = 0;
        for (Player t : p.getWorld().getPlayers()) {
            if (!participant(t) || !isEnemy(p, t)) continue;
            if (t.getLocation().distance(p.getLocation()) > 10.0) continue;
            Vector toT = t.getLocation().toVector().subtract(p.getLocation().toVector()).setY(0).normalize();
            if (facing.dot(toT) < -0.2) continue;
            this.skillManager.setDeadlocked(t, 30000L);
            t.getWorld().spawnParticle(Particle.SPELL_WITCH, t.getLocation().add(0.0, 1.0, 0.0), 15, 0.3, 0.6, 0.3, 0.05);
            t.sendMessage("\u00a75\u00a7l\u546a\u7e1b\uff01\u30b9\u30ad\u30eb\u4f7f\u7528\u4e0d\u53ef\uff01");
            ++count;
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 0.6f);
        p.sendMessage("\u00a75\u00a7l\u546a\u7e1b\u9818\u57df \u00a78\u00bb \u00a7f" + count + " \u4eba\u3092\u546a\u3044\u305f\uff01");
    }

    private void guardianUltimate(Player p) {
        TeamColor team = this.gm.getTeamOf(p);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        p.getWorld().spawnParticle(Particle.CRIT_MAGIC, p.getLocation().add(0.0, 1.0, 0.0), 20, 1.0, 1.0, 1.0, 0.1);
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 15.0, 15.0, 15.0)) {
            if (!(e instanceof Player) || (team != null && this.gm.getTeamOf((Player)e) != team)) continue;
            Player ally = (Player)e;
            ally.setInvulnerable(true);
            ally.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 140, 254, false, false));
            ally.getWorld().spawnParticle(Particle.CRIT_MAGIC, ally.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.1);
            final Player fa = ally;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (fa.isOnline()) {
                    fa.setInvulnerable(false);
                }
            }, 140L);
        }
        p.sendMessage("\u00a7f\u00a7l\u2605 \u30da\u30eb\u30d5\u30a7\u30af\u30c6\u30f4\u30a7\u30eb\u30c8 \u00a78\u00bb \u00a77\u5473\u65b9\u7121\u6575\u5316\uff01");
    }

    private void medicUltimate(final Player p) {
        p.sendMessage("\u00a75\u00a7l\u30ea\u30a2\u30af\u30c6\u30a3\u30d6\u30d2\u30fc\u30eb \u00a78\u00bb \u00a7716\u79d2\u9593\u5473\u65b9\u518d\u751f\uff01");
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.6f);
        p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0.0, 1.5, 0.0), 10, 0.4, 0.6, 0.4, 0.0);
        final TeamColor team = this.gm.getTeamOf(p);
        new BukkitRunnable(){
            int t = 0;
            public void run() {
                if (this.t++ > 16 || !participant(p)) {
                    this.cancel();
                    return;
                }
                for (Player ally : Bukkit.getOnlinePlayers()) {
                    if (!participant(ally) || team == null || UltimateManager.this.gm.getTeamOf(ally) != team) continue;
                    ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 2, false, true));
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    private void supporterUltimate(Player p) {
        TeamColor team = this.gm.getTeamOf(p);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        for (Player ally : Bukkit.getOnlinePlayers()) {
            if (!participant(ally) || (team != null && this.gm.getTeamOf(ally) != team)) continue;
            ally.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 0, false, true));
            ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 0, false, true));
            ally.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 0, false, true));
            ally.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 1, false, true));
            ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1, false, true));
            ally.getWorld().spawnParticle(Particle.CRIT_MAGIC, ally.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.1);
        }
        p.sendMessage("\u00a7a\u00a7l\u2605 \u30d5\u30eb\u30a2\u30fc\u30de\u30fc \u00a78\u00bb \u00a77\u5473\u65b9\u57fa\u790e\u30d0\u30d5\uff01");
    }

    private void phantomUltimate(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1200, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1205, 254, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1200, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1200, 1, false, false));
        p.sendMessage("\u00a77\u00a7l\u2726 \u30d5\u30a1\u30f3\u30c8\u30e0\u30da\u30a4\u30f3 \u00a78\u00bb \u00a7760\u79d2\u9593\u5f37\u5316\uff01");
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 0.8f);
    }

    private void anchorUltimate(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0, false, false));
        final Location center = p.getLocation().clone();
        new BukkitRunnable(){
            int t = 0;
            public void run() {
                if (this.t++ > 40) {
                    this.cancel();
                    return;
                }
                center.getWorld().spawnParticle(Particle.PORTAL, center, 20, 8.0, 2.0, 8.0, 0.02);
                for (Entity e : center.getWorld().getNearbyEntities(center, 8.0, 4.0, 8.0)) {
                    if (!(e instanceof Player) || !UltimateManager.this.isEnemy(p, (Player)e)) continue;
                    Player t2 = (Player)e;
                    t2.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 4, false, true));
                    t2.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0, false, true));
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 10L);
        p.sendMessage("\u00a79\u00a7l\u2726 \u30a2\u30f3\u30d3\u30eb\u30be\u30fc\u30f3\uff01");
    }

    private void releaserUltimate(Player p) {
        Location loc = p.getLocation();
        World w = p.getWorld();
        w.spawnParticle(Particle.EXPLOSION_HUGE, loc, 12, 4.0, 3.0, 4.0, 0.1);
        w.spawnParticle(Particle.LAVA, loc, 20, 3.0, 2.0, 3.0, 0.0);
        this.areaExplosion(p, loc, 9.0, 9.0, 20.0, 2.5, 0.50, false);
        p.sendMessage("\u00a76\u00a7l\u2726 \u30ab\u30bf\u30b9\u30c8\u30ed\u30d5\uff01");
    }

    private void aegisUltimate(Player p) {
        TeamColor team = this.gm.getTeamOf(p);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.4f);
        for (Player ally : Bukkit.getOnlinePlayers()) {
            if (!participant(ally) || (team != null && this.gm.getTeamOf(ally) != team)) continue;
            ally.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 0, false, true));
            ally.getWorld().spawnParticle(Particle.CRIT_MAGIC, ally.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.1);
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 300, 2, false, true));
        p.sendMessage("\u00a7a\u00a7l\u2605 \u5b8c\u5168\u5e87\u8b77 \u00a78\u00bb \u00a77\u5473\u65b9\u88ab\u30c0\u30e1\u30fc\u30b8\u534a\u6e1b\uff01");
    }

    private void reflectorUltimate(Player p) {
        this.skillManager.setMirrorUltimate(p);
        TeamColor team = this.gm.getTeamOf(p);
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 10.0, 10.0, 10.0)) {
            if (!(e instanceof Player) || (team != null && this.gm.getTeamOf((Player)e) != team)) continue;
            ((Player)e).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, true));
        }
        final Location loc = p.getLocation().clone();
        final Vector dir = p.getLocation().getDirection().setY(0).normalize();
        new BukkitRunnable(){
            int t = 0;
            public void run() {
                if (this.t++ > 60) {
                    this.cancel();
                    return;
                }
                for (double d = 1.0; d <= 8.0; d += 0.5) {
                    Location pl = loc.clone().add(dir.clone().multiply(d));
                    pl.getWorld().spawnParticle(Particle.CRIT_MAGIC, pl.clone().add(0.0, 1.0, 0.0), 1, 0.2, 0.6, 0.2, 0.0);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
    }

    private boolean participant(Player p) {
        return p != null && p.isOnline() && this.gm.isParticipant(p) && !this.gm.isSpectator(p) && this.gm.getState() == GameState.IN_GAME;
    }

    public void areaExplosion(Player owner, Location center, double rx, double ry, double dmg, double kb) {
        this.areaExplosion(owner, center, rx, ry, dmg, kb, 1.0, true);
    }

    /*
     * edgeFalloff = 爆発判定の末端でのダメージ倍率(例:0.5なら末端は半分)。高いほど減衰しない
     * pierceTerrain = false の場合、中心との間に遮蔽ブロックがある相手には当たらない
     */
    public List<Player> areaExplosion(Player owner, Location center, double rx, double ry, double dmg, double kb, double edgeFalloff, boolean pierceTerrain) {
        ArrayList<Player> hit = new ArrayList<Player>();
        World w = center.getWorld();
        if (w == null) {
            return hit;
        }
        w.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        for (Entity e : w.getNearbyEntities(center, rx, ry, rx)) {
            if (!(e instanceof Player) || !this.isEnemy(owner, (Player)e)) continue;
            Player t = (Player)e;
            if (!pierceTerrain && !this.explosionReaches(center, t)) continue;
            double ratio = Math.min(1.0, center.distance(t.getLocation()) / rx);
            double scaled = dmg * (edgeFalloff + (1.0 - edgeFalloff) * (1.0 - ratio));
            t.damage(scaled, (Entity)owner);
            if (kb > 0.0) {
                t.setVelocity(t.getLocation().toVector().subtract(center.toVector()).normalize().multiply(kb).setY(0.5));
            }
            hit.add(t);
        }
        return hit;
    }

    private boolean explosionReaches(Location center, Player t) {
        Location from = center.clone().add(0.0, 0.5, 0.0);
        Location to = t.getLocation().clone().add(0.0, 1.0, 0.0);
        Vector dir = to.toVector().subtract(from.toVector());
        double dist = dir.length();
        if (dist < 2.0) {
            return true;
        }
        dir.normalize();
        for (double d = 1.0; d <= dist - 1.0; d += 0.5) {
            if (from.clone().add(dir.clone().multiply(d)).getBlock().getType().isOccluding()) {
                return false;
            }
        }
        return true;
    }

    private void playUltimateIntro(Player p, KitType kit) {
        Sound sound = Sound.ENTITY_WITHER_SPAWN;
        float pitch = 0.5f;
        KitRole role = kit.getRole();
        if (role == KitRole.DUELIST) {
            sound = Sound.ENTITY_ENDER_DRAGON_GROWL;
            pitch = 0.8f;
        } else if (role == KitRole.INITIATOR) {
            sound = Sound.ENTITY_RAVAGER_ROAR;
            pitch = 1.2f;
        } else if (role == KitRole.SENTINEL) {
            sound = Sound.BLOCK_BEACON_ACTIVATE;
            pitch = 0.6f;
        } else if (role == KitRole.CONTROLLER) {
            sound = Sound.ENTITY_EVOKER_CAST_SPELL;
            pitch = 0.6f;
        }
        p.getWorld().playSound(p.getLocation(), sound, 1.2f, pitch);
        p.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, p.getLocation().add(0.0, 1.0, 0.0), 3, 0.5, 0.8, 0.5, 0.05);
        p.getWorld().spawnParticle(Particle.CRIT_MAGIC, p.getLocation().add(0.0, 1.0, 0.0), 30, 0.6, 1.0, 0.6, 0.1);
    }

    private void killEffect(Player victim) {
        victim.getWorld().spawnParticle(Particle.SWEEP_ATTACK, victim.getLocation().add(0.0, 1.0, 0.0), 6, 0.5, 0.6, 0.5, 0.0);
        victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0.0, 1.0, 0.0), 20, 0.3, 0.6, 0.3, 0.15);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.7f);
    }

    private boolean isEnemy(Player self, Player other) {
        if (other == null || self == null || other == self || !this.gm.isParticipant(other) || this.gm.isSpectator(other)) {
            return false;
        }
        TeamColor my = this.gm.getTeamOf(self);
        TeamColor ot = this.gm.getTeamOf(other);
        return my == null || ot == null || my != ot;
    }

    private String ultimateName(KitType kit) {
        switch (kit) {
            case BLADE: return "\u30c7\u30a3\u30e1\u30f3\u30b7\u30e7\u30f3\u30fb\u30d6\u30ec\u30a4\u30af";
            case BREAKER: return "\u30d1\u30ef\u30fc\u30bf\u30c3\u30af\u30eb";
            case NINJA: return "\u5f71\u6e21\u308a";
            case BERSERKER: return "\u30c7\u30c3\u30c9\u30ea\u30fc\u30ec\u30a4\u30f4";
            case SNIPER: return "\u30a2\u30a4\u30fb\u30aa\u30d6\u30fb\u30db\u30eb\u30b9";
            case COUNTER: return "\u30aa\u30fc\u30d0\u30fc\u30a2\u30af\u30bb\u30e9\u30ec\u30fc\u30b7\u30e7\u30f3";
            case PYRO: return "\u30a4\u30b0\u30ca\u30a4\u30c8\u30fb\u30de\u30ad\u30b7\u30de";
            case LANCER: return "\u30c0\u30a4\u30ca\u30e2\u30e9\u30f3\u30b9";
            case JESTER: return "\u30d0\u30a4\u30f3\u30c9\u30c8\u30ea\u30c3\u30af";
            case VAMPIRE: return "\u6e07\u671b";
            case BOMBER: return "\"\u7206\u5f3e\u9b54\"\u306b\u6c17\u3092\u3064\u3051\u308d\u3088";
            case COOK: return "\u30d5\u30eb\u30b3\u30fc\u30b9";
            case GRANG: return "\u6a5f\u52d5\u8981\u585e";
            case SCOUT: return "\u30db\u30fc\u30af\u30a2\u30a4";
            case FLASHER: return "\u30b0\u30ed\u30fc\u30ea\u30a2\u30b9\u30d5\u30e9\u30c3\u30b7\u30e5";
            case MARKSMAN: return "\u30e1\u30ac\u30dc\u30eb\u30c8";
            case SUNDANCE: return "\u4ffa\u306e\u30ea\u30ed\u30fc\u30c9\u306f\"\u30ec\u30f4\u30a9\u30ea\u30e5\u30fc\u30b7\u30e7\u30f3\"\u3060\uff01";
            case SWAPPER: return "\u30bb\u30ec\u30af\u30c8\u30b9\u30ef\u30c3\u30d7";
            case STICKER: return "\u30df\u30cb\u30d6\u30e9\u30c3\u30af";
            case DECOY: return "\u30b4\u30fc\u30c8\u30a5\u30bc\u30ed";
            case WHIRLWIND: return "\u30b5\u30a4\u30af\u30ed\u30f3";
            case NILGIRITAR: return "\u30d1\u30a4\u30eb\u30c9\u30e9\u30a4\u30d0\u30fc";
            case MISTRAL: return "A Stranger I Remain";
            case TRINGID: return "\u30ea\u30f3\u30ab\u30fc\u30cd\u30a4\u30b7\u30e7\u30f3\u30dd\u30fc\u30eb";
            case SUPERIOR_MISTRAL: return "\u30d5\u30e9\u30f3\u30b9\u306e\u51b7\u305f\u3044\u7a81\u98a8";
            case ROCKETER: return "\u30e1\u30ac\u30df\u30b5\u30a4\u30eb";
            case ALCHEMIST: return "\u88fd\u85ac\u30d5\u30a3\u30fc\u30d0\u30fc";
            case ENGINEER: return "\u30c8\u30ea\u30ac\u30fc\u30cf\u30c3\u30d4\u30fc";
            case RESTRICTIONER: return "\u30de\u30b9\u30ed\u30c3\u30af";
            case TRANSPORTER: return "\u30dd\u30fc\u30bf\u30eb\u30ac\u30f3";
            case KREUTZ: return "\u30d5\u30eb\u30aa\u30fc\u30c0\u30fc";
            case NECRO: return "\u30bd\u30f3\u30b0\u30aa\u30d6\u30b6\u30c7\u30c3\u30c9";
            case TIMEKEEPER: return "\u30ea\u30ef\u30a4\u30f3\u30c9";
            case HEXER: return "\u30a2\u30a4\u30fb\u30aa\u30d6\u30fb\u30d8\u30af\u30b9";
            case GLACIES: return "\u30af\u30e9\u30a4\u30aa\u30ad\u30cd\u30b7\u30b9";
            case TRAPPER: return "\u5b8c\u5168\u72af\u7f6a";
            case GUARDIAN: return "\u30da\u30eb\u30d5\u30a7\u30af\u30c6\u30f4\u30a7\u30eb\u30c8";
            case MEDIC: return "\u30ea\u30a2\u30af\u30c6\u30a3\u30d6\u30d2\u30fc\u30eb";
            case SUPPORTER: return "\u30d5\u30eb\u30a2\u30fc\u30de\u30fc";
            case PHANTOM: return "\u30d5\u30a1\u30f3\u30c8\u30e0\u30da\u30a4\u30f3";
            case ANCHOR: return "\u30a2\u30f3\u30d3\u30eb\u30be\u30fc\u30f3";
            case RELEASER: return "\u30ab\u30bf\u30b9\u30c8\u30ed\u30d5";
            case BULWARK: return "\u8d85\u30fb\u7121\u6575\u8981\u585e";
            case AEGIS: return "\u5b8c\u5168\u5e87\u8b77";
            case REFLECTOR: return "\u30a4\u30aa\u30f3\u30df\u30e9\u30fc\u30fb\u30b7\u30fc\u30eb\u30c9";
            default: return "ALTMATE";
        }
    }
}
