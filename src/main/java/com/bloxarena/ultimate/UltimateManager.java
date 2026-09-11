package com.bloxarena.ultimate;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameManager;
import com.bloxarena.game.GameState;
import com.bloxarena.game.TeamColor;
import com.bloxarena.kit.KitBuilder;
import com.bloxarena.kit.KitRole;
import com.bloxarena.kit.KitType;
import com.bloxarena.skill.SkillManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
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
    private final Map<UUID, Long> lastTickTime = new HashMap<UUID, Long>();
    private final Map<UUID, Double> chargeFraction = new HashMap<UUID, Double>();
    private final Map<UUID, Location> swapperMark = new HashMap<UUID, Location>();
    private final Map<UUID, Location> rewindMark = new HashMap<UUID, Location>();
    private final Map<UUID, Long> glaciesHitCooldown = new HashMap<UUID, Long>();

    public UltimateManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
        this.skillManager = plugin.getSkillManager();
    }

    public void addCharge(UUID player, int amount) {
        int cur = this.charge.getOrDefault(player, 0);
        this.charge.put(player, Math.min(100, cur + amount));
    }

    public int getCharge(UUID player) {
        return this.charge.getOrDefault(player, 0);
    }

    public boolean canUltimate(UUID player) {
        return this.charge.getOrDefault(player, 0) >= 100;
    }

    public void resetAll() {
        this.charge.clear();
        this.lastTickTime.clear();
        this.chargeFraction.clear();
        this.swapperMark.clear();
        this.rewindMark.clear();
        this.glaciesHitCooldown.clear();
    }

    public void onDamageDealt(Player attacker, double dmg) {
        if (!participant(attacker)) {
            return;
        }
        KitType kit = this.gm.getPlayerKitType(attacker.getUniqueId());
        if (kit == null) {
            return;
        }
        int amount = 0;
        if (kit.getRole() == KitRole.DUELIST) {
            amount = (int)(dmg * 1.2);
        } else if (kit.getRole() == KitRole.INITIATOR) {
            amount = (int)(dmg * 0.6);
        }
        if (amount > 0) {
            this.addCharge(attacker.getUniqueId(), amount);
        }
    }

    public void onDamageTaken(Player victim, double dmg) {
        if (!participant(victim)) {
            return;
        }
        KitType kit = this.gm.getPlayerKitType(victim.getUniqueId());
        if (kit == null) {
            return;
        }
        int amount = 0;
        if (kit.getRole() == KitRole.DUELIST) {
            amount = (int)(dmg * 0.8);
        } else if (kit.getRole() == KitRole.SENTINEL) {
            amount = (int)(dmg * 1.5);
        }
        if (amount > 0) {
            this.addCharge(victim.getUniqueId(), amount);
        }
    }

    public void onSkillUsed(Player p) {
        if (!participant(p)) {
            return;
        }
        KitType kit = this.gm.getPlayerKitType(p.getUniqueId());
        if (kit == null) {
            return;
        }
        if (kit.getRole() == KitRole.INITIATOR) {
            this.addCharge(p.getUniqueId(), 12);
        } else if (kit.getRole() == KitRole.CONTROLLER) {
            this.addCharge(p.getUniqueId(), 4);
        }
    }

    public void onKill(Player killer) {
        if (!participant(killer)) {
            return;
        }
        this.addCharge(killer.getUniqueId(), 6);
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
        int c = this.getCharge(p.getUniqueId());
        return c >= 100 ? "\u00a7d\u26a1 ULT \u6e96\u5099\u5b8c\u4e86\uff01 \u00a77\u3057\u3083\u304c\u307f+\u5de6\u30af\u30ea\u30c3\u30af\u3067\u89e3\u653e" : "\u00a7d\u26a1 ULT \u00a7f[" + c + "%]";
    }

    public void activateUltimate(Player p) {
        if (!participant(p)) {
            return;
        }
        if (!this.canUltimate(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u26a1 ULT\u30c1\u30e3\u30fc\u30b8\u4e0d\u8db3 \u00a78\u00bb \u00a77\u73fe\u5728 " + this.getCharge(p.getUniqueId()) + "%/100%");
            return;
        }
        KitType kit = this.gm.getPlayerKitType(p.getUniqueId());
        if (kit == null) {
            return;
        }
        this.charge.put(p.getUniqueId(), 0);
        Bukkit.broadcastMessage((String)("\u00a7d\u26a1 \u00a7f" + p.getName() + " \u00a7d\u00a7l\u304c\u30a2\u30eb\u30c6\u30a3\u30e1\u30c3\u30c8\u300c" + this.ultimateName(kit) + "\u300d\u3092\u89e3\u653e\uff01"));
        this.playUltimateIntro(p, kit);
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
            case SUPERIOR_MISTRAL: this.skillManager.setSuperiorMistralUltimate(p); break;
            case ROCKETER: this.rocketerUltimate(p); break;
            case ALCHEMIST: this.alchemistUltimate(p); break;
            case ENGINEER: this.engineerUltimate(p); break;
            case RESTRICTIONER: this.restrictionerUltimate(p); break;
            case TRANSPORTER: this.skillManager.setTransporterUltimate(p); break;
            case KREUTZ: this.skillManager.kreutzFullOrder(p); break;
            case NECRO: this.skillManager.expandNecroArmy(p, 10); break;
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
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.5f);
        new BukkitRunnable(){
            int t = 0;
            Location cur = start.clone();
            public void run() {
                if (this.t++ > 60) {
                    this.cancel();
                    return;
                }
                this.cur.add(dir.clone().multiply(0.5));
                this.cur.getWorld().spawnParticle(Particle.SWEEP_ATTACK, this.cur, 3, 0.4, 0.2, 0.4, 0.0);
                for (Entity e : this.cur.getWorld().getNearbyEntities(this.cur, 1.2, 1.5, 1.2)) {
                    if (!(e instanceof Player) || !UltimateManager.this.isEnemy(p, (Player)e)) continue;
                    Player bladeVictim = (Player)e;
                    bladeVictim.damage(26.0, (Entity)p);
        UltimateManager.this.killEffect(bladeVictim);
                    bladeVictim.damage(0.5, (Entity)p);
                    UltimateManager.this.killEffect(bladeVictim);
                    this.cancel();
                    return;
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void breakerUltimate(final Player p) {
        final Vector dir = p.getLocation().getDirection().setY(0).normalize();
        final Location from = p.getLocation().clone();
        Location dest = from.clone();
        boolean blocked = false;
        for (double d = 1.0; d <= 15.0; d += 0.5) {
            Location check = from.clone().add(dir.clone().multiply(d));
            if (check.getBlock().getType().isSolid()) {
                dest = check.clone().add(dir.clone().multiply(-1.0));
                blocked = true;
                break;
            }
            dest = check;
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
                t.damage(26.0, (Entity)p);
                UltimateManager.this.killEffect(t);
                t.setVelocity(dir.clone().multiply(3.0).setY(0.8));
            }
        }
    }

    private void ninjaUltimate(Player p) {
        Player target = this.skillManager.getTargetInSight(p, 6);
        if (target == null) {
            p.sendMessage("\u00a7c\ud83c\udfaf \u5c04\u7a0b\u5185\u306b\u30bf\u30fc\u30b2\u30c3\u30c8\u306a\u3057");
            return;
        }
        Vector back = target.getLocation().getDirection().multiply(-1.0).setY(0).normalize();
        Location behind = target.getLocation().clone().add(back.clone().multiply(1.5));
        if (behind.getBlock().getType().isSolid()) {
            behind = target.getLocation().clone();
        }
        behind.setY(behind.getY() + 0.2);
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0.0, 1.0, 0.0), 20, 0.3, 0.8, 0.3, 0.1);
        p.teleport(behind);
        target.damage(26.0, (Entity)p);
        this.killEffect(target);
        p.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.5f);
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
                if (this.t++ > 60) {
                    this.cancel();
                    return;
                }
                this.cur.add(dir.clone().multiply(0.5));
                w.createExplosion(this.cur, 0.0f, false, false, (Entity)p);
                w.spawnParticle(Particle.FLAME, this.cur, 10, 0.6, 0.6, 0.6, 0.03);
                for (Entity e : w.getNearbyEntities(this.cur, 3.0, 2.0, 3.0)) {
                    if (!(e instanceof Player) || !UltimateManager.this.isEnemy(p, (Player)e)) continue;
                    Player bersVictim = (Player)e;
                    bersVictim.damage(26.0, (Entity)p);
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
                    for (Entity e : we.getNearbyEntities(this.cur, 3.0, 3.0, 3.0)) {
                        if (!(e instanceof Player) || !UltimateManager.this.isEnemy(p, (Player)e)) continue;
                        ((Player)e).damage(6.0, (Entity)p);
                    }
                    this.cancel();
                    return;
                }
                this.cur.add(dir.clone().multiply(0.6));
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
                    t2.setFireTicks(80);
                    Bukkit.getScheduler().runTaskLater((Plugin)UltimateManager.this.plugin, () -> {
                        if (t2.isOnline() && t2.getFireTicks() > 0) {
                            t2.damage(8.0, (Entity)p);
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
            ally.sendMessage("\u00a7c\u00a7l\u26a0 \u3042\u306a\u305f\u306f\u7206\u5f3e \u00a78\u00bb \u00a7710\u79d2\u5f8c\u306b\u7206\u767a\uff01");
            final Location loc = ally.getLocation().clone();
            final Player owner = p;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                World w = loc.getWorld();
                w.createExplosion(loc, 4.0f, false, false, (Entity)owner);
                w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                w.spawnParticle(Particle.EXPLOSION_HUGE, loc, 12, 2.0, 2.0, 2.0, 0.1);
                w.spawnParticle(Particle.LAVA, loc, 15, 1.5, 1.5, 1.5, 0.0);
                for (Entity e : w.getNearbyEntities(loc, 5.0, 3.0, 5.0)) {
                    if (!(e instanceof Player) || !UltimateManager.this.isEnemy(owner, (Player)e)) continue;
                    ((Player)e).damage(12.0, (Entity)owner);
                }
            }, 200L);
        }
    }

    private void scoutUltimate(Player p) {
        TeamColor team = this.gm.getTeamOf(p);
        for (Player t : Bukkit.getOnlinePlayers()) {
            if (!participant(t) || (team != null && this.gm.getTeamOf(t) == team)) continue;
            t.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, false, false));
        }
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
    }

    private void flasherUltimate(Player p) {
        TeamColor team = this.gm.getTeamOf(p);
        for (Player t : Bukkit.getOnlinePlayers()) {
            if (!participant(t) || (team != null && this.gm.getTeamOf(t) == team)) continue;
            t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 120, 0, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 120, 2, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 1, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 120, 0, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 120, 2, false, false));
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
        final Location start = p.getLocation().clone().add(dir.clone().multiply(2.0));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 0.5f);
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
                w.spawnParticle(Particle.SWEEP_ATTACK, this.cur, 8, 2.0, 2.0, 2.0, 0.05);
                for (Entity e : w.getNearbyEntities(this.cur, 3.0, 3.0, 3.0)) {
                    if (!(e instanceof Player) || !UltimateManager.this.isEnemy(p, (Player)e)) continue;
                    Player t2 = (Player)e;
                    t2.setVelocity(t2.getVelocity().add(new Vector(0.0, 0.5, 0.0)));
                    t2.damage(2.0, (Entity)p);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
    }

    private void nilgiritarUltimate(final Player p) {
        final Player target = this.skillManager.getTargetInSight(p, 15);
        if (target == null) {
            p.sendMessage("\u00a7c\ud83c\udfaf \u30bf\u30fc\u30b2\u30c3\u30c8\u306a\u3057");
            return;
        }
        Vector dir = target.getLocation().toVector().subtract(p.getLocation().toVector()).setY(0).normalize().multiply(2.0);
        p.setVelocity(dir.clone().setY(0.3));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.5f);
        final Player owner = p;
        for (int i = 0; i < 20; ++i) {
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (target.isOnline() && target.isValid()) {
                    target.damage(1.0, (Entity)owner);
                    target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0.0, 1.0, 0.0), 3, 0.2, 0.2, 0.2, 0.1);
                }
            }, (long)(i * 2));
        }
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
                    t2.damage(3.0, (Entity)p);
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
            w.playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
            w.spawnParticle(Particle.EXPLOSION_HUGE, target, 10, 3.0, 3.0, 3.0, 0.1);
            w.spawnParticle(Particle.LAVA, target, 20, 2.0, 2.0, 2.0, 0.0);
            for (Entity e : w.getNearbyEntities(target, 6.0, 4.0, 6.0)) {
                if (!(e instanceof Player) || !UltimateManager.this.isEnemy(owner, (Player)e)) continue;
                ((Player)e).damage(12.0, (Entity)owner);
                ((Player)e).setVelocity(((Player)e).getLocation().toVector().subtract(target.toVector()).normalize().multiply(2.0).setY(0.5));
            }
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
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, task::cancel, 600L);
    }

    private void engineerUltimate(final Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 254, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 127, false, false));
        p.sendMessage("\u00a76\u00a7l\u2726 \u30c8\u30ea\u30ac\u30fc\u30cf\u30c3\u30d4\u30fc\uff01");
        final Player owner = p;
        new BukkitRunnable(){
            int t = 0;
            public void run() {
                if (this.t++ > 100 || !participant(owner)) {
                    this.cancel();
                    return;
                }
                Player target = UltimateManager.this.skillManager.getTargetInSight(owner, 40);
                if (target == null) return;
                target.damage(2.0, (Entity)owner);
                target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0.0, 1.0, 0.0), 4, 0.2, 0.3, 0.2, 0.1);
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
        this.skillManager.setDeadlocked(target, 15000L);
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
            ally.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 254, false, false));
            ally.getWorld().spawnParticle(Particle.CRIT_MAGIC, ally.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.1);
            final Player fa = ally;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (fa.isOnline()) {
                    fa.setInvulnerable(false);
                }
            }, 200L);
        }
        p.sendMessage("\u00a7f\u00a7l\u2605 \u30da\u30eb\u30d5\u30a7\u30af\u30c6\u30f4\u30a7\u30eb\u30c8 \u00a78\u00bb \u00a77\u5473\u65b9\u7121\u6575\u5316\uff01");
    }

    private void medicUltimate(final Player p) {
        p.sendMessage("\u00a75\u00a7l\u30ea\u30a2\u30af\u30c6\u30a3\u30d6\u30d2\u30fc\u30eb \u00a78\u00bb \u00a7730\u79d2\u9593\u5473\u65b9\u518d\u751f\uff01");
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.6f);
        p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0.0, 1.5, 0.0), 10, 0.4, 0.6, 0.4, 0.0);
        final TeamColor team = this.gm.getTeamOf(p);
        new BukkitRunnable(){
            int t = 0;
            public void run() {
                if (this.t++ > 150 || !participant(p)) {
                    this.cancel();
                    return;
                }
                for (Player ally : Bukkit.getOnlinePlayers()) {
                    if (!participant(ally) || (team != null && UltimateManager.this.gm.getTeamOf(ally) != team)) continue;
                    ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 4, false, true));
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
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 205, 254, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 1, false, false));
        p.sendMessage("\u00a77\u00a7l\u2726 \u30d5\u30a1\u30f3\u30c8\u30e0\u30da\u30a4\u30f3 \u00a78\u00bb \u00a77\u970a\u4f53\u5316\u5927\u5e45\u5f37\u5316\uff01");
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
        w.createExplosion(loc, 6.0f, false, false, (Entity)p);
        w.spawnParticle(Particle.EXPLOSION_HUGE, loc, 12, 4.0, 3.0, 4.0, 0.1);
        w.spawnParticle(Particle.LAVA, loc, 20, 3.0, 2.0, 3.0, 0.0);
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.4f);
        for (Entity e : w.getNearbyEntities(loc, 8.0, 4.0, 8.0)) {
            if (!(e instanceof Player) || !isEnemy(p, (Player)e)) continue;
            Player t = (Player)e;
            t.damage(10.0, (Entity)p);
            t.setVelocity(t.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(2.5).setY(1.0));
        }
        p.sendMessage("\u00a76\u00a7l\u2726 \u30ab\u30bf\u30b9\u30c8\u30ed\u30d5\uff01");
    }

    private void aegisUltimate(Player p) {
        TeamColor team = this.gm.getTeamOf(p);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.4f);
        for (Player ally : Bukkit.getOnlinePlayers()) {
            if (!participant(ally) || (team != null && this.gm.getTeamOf(ally) != team)) continue;
            ally.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 0, false, true));
            ally.getWorld().spawnParticle(Particle.CRIT_MAGIC, ally.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.1);
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 2, false, true));
        p.sendMessage("\u00a7a\u00a7l\u2605 \u5b8c\u5168\u5e87\u62a0 \u00a78\u00bb \u00a77\u5473\u65b9\u88ab\u30c0\u30e1\u30fc\u30b8\u534a\u6e1b\uff01");
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
        if (other == null || self == null || other == self || !this.gm.isParticipant(other)) {
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
            case NINJA: return "\u5fcd\u6bba";
            case BERSERKER: return "\u30c7\u30c3\u30c9\u30ea\u30fc\u30ec\u30a4\u30f4";
            case SNIPER: return "\u30a2\u30a4\u30fb\u30aa\u30d6\u30fb\u30db\u30eb\u30b9";
            case COUNTER: return "\u81f4\u547d\u306e\u4e00\u6483";
            case PYRO: return "\u30a4\u30b0\u30ca\u30a4\u30c8\u30fb\u30de\u30ad\u30b7\u30de";
            case LANCER: return "\u30c0\u30a4\u30ca\u30e2\u30e9\u30f3\u30b9";
            case JESTER: return "\u30d0\u30a4\u30f3\u30c9\u30c8\u30ea\u30c3\u30af";
            case VAMPIRE: return "\u6e07\u671b";
            case BOMBER: return "\u7206\u5f3e\u9b54";
            case COOK: return "\u30d5\u30eb\u30b3\u30fc\u30b9";
            case GRANG: return "\u6a5f\u52d5\u8981\u585e";
            case SCOUT: return "\u30db\u30fc\u30af\u30a2\u30a4";
            case FLASHER: return "\u30b0\u30ed\u30fc\u30ea\u30a2\u30b9\u30d5\u30e9\u30c3\u30b7\u30e5";
            case MARKSMAN: return "\u30e1\u30ac\u30dc\u30eb\u30c8";
            case SUNDANCE: return "\u30ec\u30f4\u30a9\u30ea\u30e5\u30fc\u30b7\u30e7\u30f3";
            case SWAPPER: return "\u30bb\u30ec\u30af\u30c8\u30b9\u30ef\u30c3\u30d7";
            case STICKER: return "\u30df\u30cb\u30d6\u30e9\u30c3\u30af";
            case DECOY: return "\u30b4\u30fc\u30c8\u30a5\u30bc\u30ed";
            case WHIRLWIND: return "\u30b5\u30a4\u30af\u30ed\u30f3";
            case NILGIRITAR: return "\u30d1\u30a4\u30eb\u30c9\u30e9\u30a4\u30d0\u30fc";
            case MISTRAL: return "A Stranger I Remain";
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
            case AEGIS: return "\u5b8c\u5168\u5e87\u62a0";
            case REFLECTOR: return "\u30a4\u30aa\u30f3\u30df\u30e9\u30fc\u30fb\u30b7\u30fc\u30eb\u30c9";
            default: return "ALTMATE";
        }
    }
}
