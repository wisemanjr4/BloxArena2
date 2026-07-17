package com.bloxarena.skill;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameManager;
import com.bloxarena.game.TeamColor;
import com.bloxarena.kit.KitBuilder;
import com.bloxarena.kit.KitType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import net.kyori.adventure.text.Component;

import java.util.*;

public class SkillManager {

    private final BloxArenaPlugin plugin;
    private final GameManager gm;

    private final NamespacedKey KEY_BURST;
    private final NamespacedKey KEY_SKILL;
    private final NamespacedKey KEY_TRAP;
    private final NamespacedKey KEY_MINE;
    private final NamespacedKey KEY_PORTAL;
    private final NamespacedKey KEY_RECON;
    private final NamespacedKey KEY_COOK;
    private final NamespacedKey KEY_VAMPIRE_SKILL;
    private final NamespacedKey KEY_PULSE;

    private final Set<UUID> burstUsed = new HashSet<>();
    private final Map<UUID, Long> skillCooldowns = new HashMap<>();
    private final Set<UUID> parryActive = new HashSet<>();
    private final Map<UUID, UUID> sniperTracker = new HashMap<>();
    private final Map<UUID, Integer> sniperAimTick = new HashMap<>();
    private final Set<UUID> markedForDeath = new HashSet<>();
    private final Map<UUID, Double> maxHpReduced = new HashMap<>();
    private final Map<UUID, Double> vampireGauge = new HashMap<>();
    private final Map<UUID, Boolean> vampireBloodMode = new HashMap<>();
    private final Map<UUID, Integer> sundanceRevolver = new HashMap<>();

    private final List<TrapData> activeTraps = new ArrayList<>();
    private final List<MineData> activeMines = new ArrayList<>();
    private final List<TurretData> activeTurrets = new ArrayList<>();
    private final Map<UUID, Location> portalA = new HashMap<>();
    private final Map<UUID, Location> portalB = new HashMap<>();
    private final List<Location> portalBlocks = new ArrayList<>();
    private final List<ReconData> activeRecons = new ArrayList<>();
    private final List<PulseData> activePulsers = new ArrayList<>();
    private final Map<UUID, Long> phantomEnd = new HashMap<>();
    private final Map<UUID, Location> anchorFields = new HashMap<>();
    private BukkitTask antiBetrayalTask = null;
    private final Map<UUID, Long> guardianEndTime = new HashMap<>();
    private final Map<UUID, Long> grangChargeStart = new HashMap<>();
    private final Map<UUID, Boolean> grangCharging = new HashMap<>();
    private final Map<UUID, List<Skeleton>> necroArmy = new HashMap<>();
    private final Set<UUID> deadlockedPlayers = new HashSet<>();
    private final Map<UUID, Long> portalCooldowns = new HashMap<>();
    private final Map<UUID, Location> portalLastUsed = new HashMap<>();
    private final Map<UUID, ItemStack[]> storedArmor = new HashMap<>();
    private final Set<UUID> gliderInAir = new HashSet<>();
    private final Set<UUID> releaserMegaUsed = new HashSet<>();

    static class ReconData {
        final ArmorStand entity;
        final TeamColor ownerTeam;
        ReconData(ArmorStand a, TeamColor t) { entity = a; ownerTeam = t; }
    }
    static class PulseData {
        final Location loc;
        final TeamColor ownerTeam;
        PulseData(Location l, TeamColor t) { loc = l; ownerTeam = t; }
    }
    static class TrapData {
        final UUID owner;
        final Location loc;
        boolean triggered;
        TrapData(UUID o, Location l) { owner = o; loc = l; }
    }
    static class TurretData {
        final Skeleton entity;
        final UUID owner;
        final TeamColor ownerTeam;
        TurretData(Skeleton e, UUID o, TeamColor t) { entity = e; owner = o; ownerTeam = t; }
    }
    static class MineData {
        final ArmorStand entity;
        final UUID owner;
        MineData(ArmorStand e, UUID o) { entity = e; owner = o; }
    }

    public SkillManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
        this.KEY_BURST = new NamespacedKey(plugin, "burst_skill");
        this.KEY_SKILL = new NamespacedKey(plugin, "kit_skill");
        this.KEY_TRAP = new NamespacedKey(plugin, "ba_trap");
        this.KEY_MINE = new NamespacedKey(plugin, "ba_mine");
        this.KEY_PORTAL = new NamespacedKey(plugin, "ba_portal");
        this.KEY_RECON = new NamespacedKey(plugin, "ba_recon");
        this.KEY_COOK = new NamespacedKey(plugin, "ba_cook");
        this.KEY_VAMPIRE_SKILL = new NamespacedKey(plugin, "vampire_skill");
        this.KEY_PULSE = new NamespacedKey(plugin, "ba_pulse");
    }

    public void resetAll() {
        burstUsed.clear(); skillCooldowns.clear(); parryActive.clear();
        sniperTracker.clear(); sniperAimTick.clear(); markedForDeath.clear();
        maxHpReduced.clear(); vampireGauge.clear(); vampireBloodMode.clear();
        sundanceRevolver.clear();
        activeTraps.clear();
        activeMines.forEach(m -> m.entity.remove()); activeMines.clear();
        activeTurrets.forEach(t -> t.entity.remove()); activeTurrets.clear();
        portalA.clear(); portalB.clear(); portalBlocks.clear();
        activeRecons.forEach(r -> r.entity.remove()); activeRecons.clear();
        activePulsers.clear();
        for (UUID uid : phantomEnd.keySet()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) { p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE); p.removePotionEffect(PotionEffectType.INVISIBILITY); }
        }
        phantomEnd.clear(); anchorFields.clear(); deadlockedPlayers.clear();
        guardianEndTime.clear(); gliderInAir.clear(); releaserMegaUsed.clear();
        portalCooldowns.clear(); grangChargeStart.clear(); grangCharging.clear();
        necroArmy.values().forEach(list -> list.forEach(s -> { if (s.isValid()) s.remove(); }));
        necroArmy.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null)
                p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
            if (p.getHealth() > 20.0) p.setHealth(20.0);
        }
        if (antiBetrayalTask != null) { antiBetrayalTask.cancel(); antiBetrayalTask = null; }
        for (UUID uid : new HashSet<>(storedArmor.keySet())) { Player p = Bukkit.getPlayer(uid); if (p != null) showArmor(p); }
        storedArmor.clear();
    }

    public void resetRound() {
        burstUsed.clear(); skillCooldowns.clear(); parryActive.clear();
        sniperAimTick.clear(); sniperTracker.clear(); markedForDeath.clear();
        sundanceRevolver.clear();
        activeTraps.clear(); activeMines.forEach(m -> m.entity.remove()); activeMines.clear();
        activeTurrets.forEach(t -> t.entity.remove()); activeTurrets.clear();
        activeRecons.forEach(r -> r.entity.remove()); activeRecons.clear();
        activePulsers.clear(); guardianEndTime.clear(); gliderInAir.clear();
        releaserMegaUsed.clear(); deadlockedPlayers.clear(); portalCooldowns.clear();
        portalA.clear(); portalB.clear(); portalBlocks.clear();
        grangChargeStart.clear(); grangCharging.clear();
        necroArmy.values().forEach(list -> list.forEach(s -> { if (s.isValid()) s.remove(); }));
        necroArmy.clear(); anchorFields.clear();
        for (UUID uid : new HashSet<>(storedArmor.keySet())) { Player p = Bukkit.getPlayer(uid); if (p != null) showArmor(p); }
        storedArmor.clear();
        for (UUID uid : new HashSet<>(phantomEnd.keySet())) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) { p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE); p.removePotionEffect(PotionEffectType.INVISIBILITY); showArmor(p); }
        }
        phantomEnd.clear(); vampireGauge.clear(); vampireBloodMode.clear(); maxHpReduced.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setInvulnerable(false);
            if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null)
                p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
            p.getPersistentDataContainer().remove(new NamespacedKey(plugin, "heavy_bolt"));
            p.getPersistentDataContainer().remove(new NamespacedKey(plugin, "mega_rocket"));
            p.getPersistentDataContainer().remove(KEY_RECON);
            p.getPersistentDataContainer().remove(KEY_PULSE);
            p.getPersistentDataContainer().remove(KEY_VAMPIRE_SKILL);
        }
    }

    // ─── Event forwarders ───

    public void onRightClick(Player p, ItemStack held) {
        if (held == null || held.getItemMeta() == null) return;
        ItemMeta meta = held.getItemMeta();
        if (meta.getPersistentDataContainer().has(KEY_BURST, PersistentDataType.BYTE)) { useBurst(p); return; }
        // COOK: sword right-click = generate food
        if (gm.getPlayerKitType(p.getUniqueId()) == KitType.COOK && isSword(held)) {
            cookGenerateFood(p); return;
        }
        // SUNDANCE: sneak + right-click crossbow = activate revolver
        if (gm.getPlayerKitType(p.getUniqueId()) == KitType.SUNDANCE && p.isSneaking()
                && held.getType() == Material.CROSSBOW) {
            sundanceSkill(p); return;
        }
        if (meta.getPersistentDataContainer().has(KEY_SKILL, PersistentDataType.STRING)) {
            String kitName = meta.getPersistentDataContainer().get(KEY_SKILL, PersistentDataType.STRING);
            if ("TRANSPORTER".equals(kitName) && gm.getPlayerKitType(p.getUniqueId()) == KitType.TRANSPORTER) { placePortalB(p); return; }
            if ("RELEASER".equals(kitName) && p.isSneaking()) { releaserMegaBurst(p); return; }
            useKitSkill(p, kitName);
        }
        if (meta.getPersistentDataContainer().has(KEY_VAMPIRE_SKILL, PersistentDataType.STRING) && gm.getPlayerKitType(p.getUniqueId()) == KitType.VAMPIRE) {
            theosPadaAction(p, true);
        }
    }

    public void onClick(Player p, ItemStack held, boolean isLeft) {
        if (!isLeft || held == null || held.getItemMeta() == null) return;
        ItemMeta meta = held.getItemMeta();
        if (meta.getPersistentDataContainer().has(KEY_SKILL, PersistentDataType.STRING)) {
            String kitName = meta.getPersistentDataContainer().get(KEY_SKILL, PersistentDataType.STRING);
            if ("TRANSPORTER".equals(kitName) && gm.getPlayerKitType(p.getUniqueId()) == KitType.TRANSPORTER) { placePortalA(p); }
            if ("WHIRLWIND".equals(kitName) && gm.getPlayerKitType(p.getUniqueId()) == KitType.WHIRLWIND) { whirlwindBall(p); }
            if ("COOK".equals(kitName) && gm.getPlayerKitType(p.getUniqueId()) == KitType.COOK) { cookThrowFood(p); }
        }
        if (meta.getPersistentDataContainer().has(KEY_VAMPIRE_SKILL, PersistentDataType.STRING) && gm.getPlayerKitType(p.getUniqueId()) == KitType.VAMPIRE) {
            if (p.isSneaking()) theosPadaAction(p, false);
        }
    }

    public void onSniperAimTick(Player p) {
        if (gm.getPlayerKitType(p.getUniqueId()) != KitType.SNIPER) return;
        if (p.getInventory().getItemInMainHand().getType() != Material.CROSSBOW) { sniperAimTick.remove(p.getUniqueId()); sniperTracker.remove(p.getUniqueId()); return; }
        Player target = getTargetInSight(p, 50); UUID oldTarget = sniperTracker.get(p.getUniqueId());
        if (target == null) { if (oldTarget != null) p.sendMessage("§7狙撃を中止しました"); sniperAimTick.remove(p.getUniqueId()); sniperTracker.remove(p.getUniqueId()); return; }
        UUID prevTarget = sniperTracker.get(p.getUniqueId());
        if (prevTarget == null || !prevTarget.equals(target.getUniqueId())) { sniperTracker.put(p.getUniqueId(), target.getUniqueId()); sniperAimTick.put(p.getUniqueId(), 1); return; }
        int ticks = sniperAimTick.merge(p.getUniqueId(), 1, Integer::sum);
        if (ticks >= 5) {
            markedForDeath.add(target.getUniqueId());
            p.sendMessage("§c§l狙撃眼: §f" + target.getName() + " §cをマークしました！次の一撃で即死！");
            target.sendMessage("§c§l⚠ 狙撃されています！");
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BELL_USE, 1f, 0.5f);
            sniperAimTick.remove(p.getUniqueId()); sniperTracker.remove(p.getUniqueId());
        }
    }

    public boolean onSniperHit(Player shooter, Player victim) {
        if (markedForDeath.contains(victim.getUniqueId()) && gm.getPlayerKitType(shooter.getUniqueId()) == KitType.SNIPER) {
            victim.setHealth(0); markedForDeath.remove(victim.getUniqueId());
            shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 2f);
            return true;
        }
        return false;
    }

    public void onParryAttempt(Player p) {
        if (gm.getPlayerKitType(p.getUniqueId()) != KitType.COUNTER) return;
        if (!p.isSneaking()) return;
        if (isOnCooldown(p.getUniqueId())) return;
        parryActive.add(p.getUniqueId());
        new BukkitRunnable() { @Override public void run() { parryActive.remove(p.getUniqueId()); } }.runTaskLater(plugin, 10L);
    }

    public boolean tryParryCounter(Player attacker, Player counter) {
        if (!parryActive.contains(counter.getUniqueId())) return false;
        if (!counter.isBlocking()) return false;
        Vector dirToAttacker = attacker.getLocation().toVector().subtract(counter.getLocation().toVector()).setY(0).normalize();
        Vector facing = counter.getLocation().getDirection().setY(0).normalize();
        if (facing.lengthSquared() == 0 || dirToAttacker.lengthSquared() == 0) return false;
        if (dirToAttacker.dot(facing) < 0.4) return false;
        parryActive.remove(counter.getUniqueId());
        setCooldown(counter.getUniqueId(), 15_000L);
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 10, false, true));
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 2, false, true));
        attacker.sendMessage("§c§l鋼の反射！行動不能！");
        counter.sendMessage("§9§lパリィ成功！");
        counter.getWorld().playSound(counter.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.5f, 1.5f);
        counter.getWorld().spawnParticle(Particle.CRIT_MAGIC, counter.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.3);
        return true;
    }

    public void onGrangSneak(Player p, boolean sneaking) {
        if (gm.getPlayerKitType(p.getUniqueId()) != KitType.GRANG) return;
        if (!p.isBlocking()) return;
        if (isOnCooldown(p.getUniqueId())) { if (sneaking) p.sendMessage("§cクールタイム中！"); return; }
        if (sneaking) {
            if (!grangCharging.getOrDefault(p.getUniqueId(), false)) { grangChargeStart.put(p.getUniqueId(), System.currentTimeMillis()); grangCharging.put(p.getUniqueId(), true); p.sendMessage("§7§lチャージ開始..."); p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 0.3f, 0.5f); }
            long elapsed = System.currentTimeMillis() - grangChargeStart.getOrDefault(p.getUniqueId(), System.currentTimeMillis());
            int pct = (int) Math.min(100, elapsed * 100 / 10_000);
            p.sendActionBar(Component.text("§7🛡 チャージ §e" + pct + "%"));
        } else {
            Long start = grangChargeStart.remove(p.getUniqueId()); grangCharging.put(p.getUniqueId(), false);
            if (start == null) return;
            long chargeMs = Math.min(System.currentTimeMillis() - start, 10_000);
            double power = chargeMs / 10_000.0;
            double damage = 3.0 + power * 7.0; double distance = 3.0 + power * 10.0;
            int cd = (int) (5_000 + power * 8_000);
            setCooldown(p.getUniqueId(), cd);
            Vector dir = p.getLocation().getDirection().normalize().multiply(distance * 0.15).setY(0.2);
            p.setVelocity(dir);
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 0.8f + (float)power * 0.4f);
            if (power >= 0.95) {
                UUID uid = p.getUniqueId();
                Bukkit.getScheduler().runTaskLater(plugin, () -> { Player pl = Bukkit.getPlayer(uid); if (pl != null && pl.isOnline()) { pl.getWorld().createExplosion(pl.getLocation(), 2f, false, false, pl); pl.sendMessage("§7§l最大チャージ炸裂！"); } }, 40L);
            }
            World w = p.getWorld();
            new BukkitRunnable() { int t = 0; @Override public void run() { if (t++ > 20 || !p.isOnline()) { cancel(); return; }
                for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 2, 2, 2)) {
                    if (!(e instanceof Player target) || !gm.isParticipant(target) || gm.getTeamOf(target) == gm.getTeamOf(p)) continue;
                    target.damage(damage * 0.1, p);
                    target.setVelocity(p.getLocation().getDirection().normalize().multiply(0.5).setY(0.3));
                }
            } }.runTaskTimer(plugin, 0L, 2L);
        }
    }

    public void onVampireDamageDealt(Player p, double dmg) {
        if (gm.getPlayerKitType(p.getUniqueId()) != KitType.VAMPIRE) return;
        if (vampireBloodMode.getOrDefault(p.getUniqueId(), false)) return;
        double g = vampireGauge.getOrDefault(p.getUniqueId(), 0.0) + dmg;
        vampireGauge.put(p.getUniqueId(), Math.min(g, 80));
        updateVampireStage(p, g);
    }

    public void onVampireDamaged(Player p, double dmg) {
        if (gm.getPlayerKitType(p.getUniqueId()) != KitType.VAMPIRE) return;
        if (!vampireBloodMode.getOrDefault(p.getUniqueId(), false)) return;
        double g = vampireGauge.getOrDefault(p.getUniqueId(), 0.0) - dmg;
        if (g <= 0) { vampireBloodMode.put(p.getUniqueId(), false); vampireGauge.put(p.getUniqueId(), 0.0); applyVampireStage(p, 0); return; }
        vampireGauge.put(p.getUniqueId(), g);
    }

    public void onVampireAttack(Player p) { /* handled in onVampireDamageDealt */ }

    public void onCrossbowShoot(Player p) {
        if (gm.getPlayerKitType(p.getUniqueId()) != KitType.SUNDANCE) return;
        ItemStack xbow = p.getInventory().getItemInMainHand();
        if (xbow != null && xbow.getType() == Material.CROSSBOW && xbow.getItemMeta() instanceof CrossbowMeta cm && !cm.hasChargedProjectiles()) return;
        UUID uid = p.getUniqueId();
        int remaining = sundanceRevolver.getOrDefault(uid, 0);
        if (remaining > 0) {
            remaining--; sundanceRevolver.put(uid, remaining);
            if (remaining <= 0) { sundanceRevolver.remove(uid); setCooldown(uid, 7_000L); p.sendMessage("§bリボルビング終了！CT 7秒"); }
            else { new BukkitRunnable() { @Override public void run() { if (p.isOnline()) { ItemStack xb = p.getInventory().getItemInMainHand(); if (xb != null && xb.getType() == Material.CROSSBOW && xb.getItemMeta() instanceof CrossbowMeta cm2 && !cm2.hasChargedProjectiles()) { cm2.addChargedProjectile(new ItemStack(Material.ARROW)); xb.setItemMeta(cm2); } } } }.runTaskLater(plugin, 1L); }
        }
    }

    public void onPortalWalk(Player p, Location loc) { checkPortalTeleport(p, loc); }

    public void onFlashBangLand(Location loc, Player shooter) {
        for (Player t : loc.getWorld().getPlayers()) {
            if (t.getLocation().distance(loc) <= 4 && gm.isParticipant(t)) { t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false)); t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1, false, false)); }
        }
    }

    public void onGrappleHit(Snowball ball, Player victim, Player shooter) {
        if (gm.getTeamOf(victim) == gm.getTeamOf(shooter)) return;
        victim.setVelocity(shooter.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize().multiply(3.0));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false));
    }

    public void onMegaRocketHit(Location loc, Player shooter, Snowball rocket) {
        float power = rocketHitPower(rocket, loc, 2f);
        loc.getWorld().createExplosion(loc, 0f, false, false, shooter);for(Entity e2:loc.getWorld().getNearbyEntities(loc,4,3,4)){if(e2 instanceof Player t&&gm.isParticipant(t)&&gm.getTeamOf(t)!=gm.getTeamOf(shooter)){t.damage(power*4,shooter);t.setVelocity(t.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(2.0).setY(0.5));}}
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.6f);
    }

    public float rocketHitPower(Snowball rocket, Location explodeLoc, float maxPower) {
        Double ox = rocket.getPersistentDataContainer().get(new NamespacedKey(plugin, "rocket_origin_x"), PersistentDataType.DOUBLE);
        Double oy = rocket.getPersistentDataContainer().get(new NamespacedKey(plugin, "rocket_origin_y"), PersistentDataType.DOUBLE);
        Double oz = rocket.getPersistentDataContainer().get(new NamespacedKey(plugin, "rocket_origin_z"), PersistentDataType.DOUBLE);
        if (ox == null || oy == null || oz == null) return maxPower;
        double dist = new Location(explodeLoc.getWorld(), ox, oy, oz).distance(explodeLoc);
        float scale = (float) Math.min(1.0, dist / 10.0);
        return Math.max(maxPower * 0.15f, maxPower * scale);
    }

    public void onTheosPadaHit(Snowball ball, Player shooter, Entity hit) {
        if (hit instanceof Player t && gm.isParticipant(t) && gm.getTeamOf(t) != gm.getTeamOf(shooter)) {
            double heal = Math.min(t.getHealth(), t.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            double g = vampireGauge.getOrDefault(shooter.getUniqueId(), 0.0) + heal;
            vampireGauge.put(shooter.getUniqueId(), Math.min(g, 80));
            updateVampireStage(shooter, g);
            shooter.sendMessage("§4§lテオスパーダ吸収！§7ゲージ+" + String.format("%.0f", heal));
        }
    }

    public void marksmanHeavyBoltHit(Player shooter, Player victim) {
        if (!shooter.getPersistentDataContainer().has(new NamespacedKey(plugin, "heavy_bolt"), PersistentDataType.BYTE)) return;
        shooter.getPersistentDataContainer().remove(new NamespacedKey(plugin, "heavy_bolt"));
        victim.getPersistentDataContainer().set(new NamespacedKey(plugin, "hp_reduced"), PersistentDataType.BYTE, (byte) 1);
        if (victim.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            double currentMax = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            maxHpReduced.putIfAbsent(victim.getUniqueId(), currentMax);
            victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.max(1, currentMax - 10));
            if (victim.getHealth() > currentMax - 10) victim.setHealth(currentMax - 10);
        }
        victim.sendMessage("§c§lHP上限が10低下しました！（ラウンド終了まで）");
    }

    public void restoreMaxHp(Player p) {
        if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            double current = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            if (maxHpReduced.containsKey(p.getUniqueId())) { p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHpReduced.remove(p.getUniqueId())); }
            else if (current != 20.0) { p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0); }
        }
    }

    public void scoutReconShot(Player p, Location hitLoc) {
        if (!p.getPersistentDataContainer().has(KEY_RECON, PersistentDataType.BYTE)) return;
        p.getPersistentDataContainer().remove(KEY_RECON);
        ArmorStand as = hitLoc.getWorld().spawn(hitLoc, ArmorStand.class, a -> { a.setInvisible(true); a.setMarker(true); a.setCustomName("§a🔍 リコン"); a.setCustomNameVisible(true); });
        activeRecons.add(new ReconData(as, gm.getTeamOf(p)));
        Bukkit.getScheduler().runTaskLater(plugin, () -> { as.remove(); activeRecons.removeIf(r -> r.entity.equals(as)); }, 1200L);
        p.sendMessage("§aリコン設置！60秒間索敵");
    }

    public void scoutPulseShot(Player p, Location hitLoc) {
        if (!p.getPersistentDataContainer().has(KEY_PULSE, PersistentDataType.BYTE)) return;
        p.getPersistentDataContainer().remove(KEY_PULSE);
        activePulsers.add(new PulseData(hitLoc.clone(), gm.getTeamOf(p)));
        p.sendMessage("§cパルサー設置！30秒範囲ダメージ");
        Bukkit.getScheduler().runTaskLater(plugin, () -> activePulsers.removeIf(pd -> pd.loc.equals(hitLoc)), 600L);
    }

    public void onDecoyHit(Entity decoy, Player attacker) {
        if (!decoy.getPersistentDataContainer().has(new NamespacedKey(plugin, "decoy"), PersistentDataType.BYTE)) return;
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, false, false));
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1, false, false));
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 1, false, false));
        attacker.sendMessage("§8§l罠だ！§7囮を攻撃して弱体化＋発光した！");
    }

    public void onPhantomDamaged(Player p) {
        if (!phantomEnd.containsKey(p.getUniqueId())) return;
        Long endTime = phantomEnd.get(p.getUniqueId());
        if (endTime != null && System.currentTimeMillis() < endTime) {
            phantomEnd.remove(p.getUniqueId());
            p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
            showArmor(p); p.sendMessage("§c霊体化解除！");
        }
    }

    public void checkPortalTeleport(Player p, Location to) {
        Location lastPortal = portalLastUsed.get(p.getUniqueId());
        if (lastPortal != null && to.distance(lastPortal) < 2.5) return;
        portalLastUsed.remove(p.getUniqueId());
        for (UUID uid : portalA.keySet()) {
            Location a = portalA.get(uid), b = portalB.get(uid);
            if (a == null || b == null) continue;
            if (to.distance(a) < 1.5) { p.teleport(b.clone().add(0, 1, 0)); portalLastUsed.put(p.getUniqueId(), b.clone()); p.getWorld().playSound(b, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f); return; }
            if (to.distance(b) < 1.5) { p.teleport(a.clone().add(0, 1, 0)); portalLastUsed.put(p.getUniqueId(), a.clone()); p.getWorld().playSound(a, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f); return; }
        }
    }

    public boolean isVampireBloodMode(UUID uid) { return vampireBloodMode.getOrDefault(uid, false); }
    public double getVampireGauge(UUID uid) { return vampireGauge.getOrDefault(uid, 0.0); }
    public boolean isDeadlocked(UUID uid) { return deadlockedPlayers.contains(uid); }
    public void initVampireDebuffs(Player p) { if (gm.getPlayerKitType(p.getUniqueId()) == KitType.VAMPIRE) applyVampireStage(p, 0); }
    public void cookHit(Location loc, Player thrower, Material mat) {
        for (Entity e : loc.getWorld().getNearbyEntities(loc, 2, 2, 2)) {
            if (e instanceof Player t && gm.isParticipant(t) && t != thrower) {
                applyCookBuff(t, mat);
                t.sendMessage(gm.getTeamOf(t) == gm.getTeamOf(thrower) ? "§a味方から料理が命中！" : "§c料理が命中！");
            }
        }
    }
    public void refreshBurst(Player p) {
        burstUsed.remove(p.getUniqueId()); releaserMegaUsed.remove(p.getUniqueId());
        if (gm.getPlayerKitType(p.getUniqueId()) != KitType.RELEASER) p.getInventory().setItem(8, KitBuilder.makeBurstItem());
    }
    public void updateTurrets() {
        for (TurretData t : new ArrayList<>(activeTurrets)) {
            if (!t.entity.isValid()) { activeTurrets.remove(t); continue; }
            Location loc = t.entity.getLocation(); Player nearest = null; double nearestDist = 225;
            for (Player target : loc.getWorld().getPlayers()) {
                if (!gm.isParticipant(target) || gm.isSpectator(target)) continue;
                if (gm.getTeamOf(target) == t.ownerTeam) continue;
                double dist = target.getLocation().distanceSquared(loc); if (dist > 225) continue;
                if (dist < nearestDist) { nearest = target; nearestDist = dist; }
            }
            if (nearest != null) t.entity.setTarget(nearest);
        }
    }
    public void refreshReleaserMega(Player p) { releaserMegaUsed.remove(p.getUniqueId()); }

    // ─── Update loop ───

    public void update() {
        if (antiBetrayalTask == null) antiBetrayalTask = Bukkit.getScheduler().runTaskTimer(plugin, this::forceTargetEnemies, 0L, 1L);
        for (Player p : Bukkit.getOnlinePlayers()) { if (gm.getPlayerKitType(p.getUniqueId()) == KitType.SNIPER && gm.isParticipant(p)) onSniperAimTick(p); }
        for (UUID uid : new HashSet<>(gliderInAir)) { Player p = Bukkit.getPlayer(uid); if (p == null || !gm.isParticipant(p)) { gliderInAir.remove(uid); continue; } if (p.isOnGround()) { gliderInAir.remove(uid); setCooldown(uid, 12_000L); p.sendMessage("§a着地！クールタイム開始（12秒）"); } }
        for (ReconData r : new ArrayList<>(activeRecons)) {
            if (!r.entity.isValid()) { activeRecons.remove(r); continue; }
            for (Player p : r.entity.getWorld().getPlayers()) {
                if (p.getLocation().distance(r.entity.getLocation()) <= 7 && gm.isParticipant(p) && gm.getTeamOf(p) != r.ownerTeam) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, false, false));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, false));
                }
            }
        }
        for (PulseData pd : new ArrayList<>(activePulsers)) {
            Location loc = pd.loc; if (loc.getWorld() == null) { activePulsers.remove(pd); continue; }
            loc.getWorld().spawnParticle(Particle.WAX_OFF, loc.clone().add(0.5, 1, 0.5), 8, 2.5, 1, 2.5, 0);
            for (Player p : loc.getWorld().getPlayers()) {
                if (p.getLocation().distance(loc) <= 5 && gm.isParticipant(p) && gm.getTeamOf(p) != pd.ownerTeam) {
                    p.damage(0.5); p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false));
                }
            }
        }
        for (TrapData t : activeTraps) {
            TeamColor ownerTeam = gm.getTeam(t.owner);
            Player owner = Bukkit.getPlayer(t.owner);
            // Show particles to trap owner
            if (owner != null && t.loc.getWorld() != null) {
                owner.spawnParticle(Particle.ENCHANTMENT_TABLE, t.loc.clone().add(0.5, 0.2, 0.5), 3, 0.2, 0.1, 0.2, 0.02);
            }
            for (Player p : t.loc.getWorld().getPlayers()) {
                if (p.getLocation().distance(t.loc) <= 3 && gm.isParticipant(p) && gm.getTeamOf(p) != ownerTeam && !t.triggered) { triggerTrap(t, p); break; }
            }
        }
        for (Map.Entry<UUID, Location> e : anchorFields.entrySet()) {
            Location loc = e.getValue(); if (loc == null || loc.getWorld() == null) continue;
            for (Player p : loc.getWorld().getPlayers()) {
                if (p.getLocation().distance(loc) <= 12 && gm.isParticipant(p) && !gm.isSpectator(p) && gm.getTeamOf(p) != gm.getTeam(e.getKey())) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 8, false, true));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 2, false, true));
                    // Prevent jumping by capping Y velocity
                    if (p.getVelocity().getY() > 0) p.setVelocity(p.getVelocity().setY(-0.1));
                }
            }
        }
        for (Map.Entry<UUID, Boolean> e : vampireBloodMode.entrySet()) {
            if (!e.getValue()) continue;
            UUID uid = e.getKey(); double gauge = vampireGauge.getOrDefault(uid, 0.0) - 0.3;
            if (gauge <= 0) { vampireBloodMode.put(uid, false); applyVampireStage(Bukkit.getPlayer(uid), 0); continue; }
            vampireGauge.put(uid, gauge); Player p = Bukkit.getPlayer(uid); if (p != null) updateVampireStage(p, gauge);
        }
        updateKitActionBars();
    }

    private void forceTargetEnemies() {
        for (Map.Entry<UUID, List<Skeleton>> entry : necroArmy.entrySet()) {
            UUID ownerUid = entry.getKey(); TeamColor ownerTeam = gm.getTeam(ownerUid);
            if (ownerTeam == null) continue;
            TeamColor enemyTeam = ownerTeam == TeamColor.RED ? TeamColor.BLUE : TeamColor.RED;
            for (Skeleton skel : entry.getValue()) {
                if (!skel.isValid()) continue; Player nearest = null; double nearestDist = 900;
                for (Player target : skel.getWorld().getPlayers()) {
                    if (!gm.isParticipant(target) || gm.isSpectator(target)) continue;
                    if (gm.getTeamOf(target) != enemyTeam) continue;
                    double d = target.getLocation().distanceSquared(skel.getLocation());
                    if (d < nearestDist) { nearestDist = d; nearest = target; }
                }
                skel.setTarget(null); if (nearest != null) skel.setTarget(nearest);
            }
        }
        for (TurretData t : activeTurrets) {
            if (!t.entity.isValid()) continue; Player nearest = null; double nearestDist = 900;
            for (Player target : t.entity.getWorld().getPlayers()) {
                if (!gm.isParticipant(target) || gm.isSpectator(target)) continue;
                if (gm.getTeamOf(target) == t.ownerTeam) continue;
                double d = target.getLocation().distanceSquared(t.entity.getLocation());
                if (d < nearestDist) { nearestDist = d; nearest = target; }
            }
            t.entity.setTarget(null); if (nearest != null) t.entity.setTarget(nearest);
        }
    }

    private void updateKitActionBars() {
        // Suppress kit action bars during Domination/CTF/TDM (game-mode info takes priority)
        var mode = gm.getCurrentGameMode();
        if (mode == com.bloxarena.game.GameMode.DOMINATION
                || mode == com.bloxarena.game.GameMode.CAPTURE_THE_FLAG
                || mode == com.bloxarena.game.GameMode.TEAM_DEATHMATCH) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!gm.isParticipant(p) || gm.isSpectator(p)) continue;
            KitType kit = gm.getPlayerKitType(p.getUniqueId()); if (kit == null) continue;
            switch (kit) {
                case SNIPER -> {
                    Integer ticks = sniperAimTick.get(p.getUniqueId()); UUID targetUid = sniperTracker.get(p.getUniqueId());
                    if (ticks != null && targetUid != null && ticks > 0) {
                        Player target = Bukkit.getPlayer(targetUid); String targetName = target != null ? target.getName() : "???";
                        int pct = Math.min(100, ticks * 20); String bar = "§a" + "█".repeat(pct / 10) + "§7" + "█".repeat(10 - pct / 10);
                        int remaining = Math.max(0, 5 - ticks);
                        p.sendActionBar(Component.text("§c🎯 狙撃中: " + targetName + " §8[" + bar + "§8] §e" + remaining + "s"));
                    }
                }
                case BOMBER -> { long mines = activeMines.stream().filter(m -> m.owner.equals(p.getUniqueId())).count(); if (mines > 0) p.sendActionBar(Component.text("§c💣 地雷 x" + mines)); }
                case SUNDANCE -> { int shots = sundanceRevolver.getOrDefault(p.getUniqueId(), 0); if (shots > 0) p.sendActionBar(Component.text("§b⚡ リボルビング §f" + shots + "/6")); }
                case GUARDIAN -> { Long end = guardianEndTime.get(p.getUniqueId()); if (end != null && System.currentTimeMillis() < end) { long rem = (end - System.currentTimeMillis()) / 1000; p.sendActionBar(Component.text("§f🛡 鉄壁 §e残り " + rem + "秒")); } }
                case TRAPPER -> { long traps = activeTraps.stream().filter(t -> t.owner.equals(p.getUniqueId()) && !t.triggered).count(); if (traps > 0) p.sendActionBar(Component.text("§3🪤 罠 x" + traps + "/2")); }
                case PHANTOM -> { Long end = phantomEnd.get(p.getUniqueId()); if (end != null && System.currentTimeMillis() < end) { long rem = (end - System.currentTimeMillis()) / 1000; p.sendActionBar(Component.text("§7👻 霊体化 §e残り " + rem + "秒")); } }
                case ANCHOR -> { if (anchorFields.containsKey(p.getUniqueId())) p.sendActionBar(Component.text("§9⚓ 磁場展開中 §7| 半径12m完全拘束＋継続ダメ")); }
                case ENGINEER -> { long turrets = activeTurrets.stream().filter(t -> t.owner.equals(p.getUniqueId())).count(); if (turrets > 0) p.sendActionBar(Component.text("§6🔧 タレット x" + turrets)); }
                case SCOUT -> { long recons = activeRecons.stream().filter(r -> r.ownerTeam == gm.getTeamOf(p)).count(); if (recons > 0) p.sendActionBar(Component.text("§a📡 リコン x" + recons)); }
                case COUNTER -> { if (parryActive.contains(p.getUniqueId())) p.sendActionBar(Component.text("§9🛡 パリィ受付中！")); }
                case GRANG -> { Boolean charging = grangCharging.get(p.getUniqueId()); if (charging != null && charging) { Long start = grangChargeStart.get(p.getUniqueId()); if (start != null) { long elapsed = System.currentTimeMillis() - start; int pct = (int) Math.min(100, elapsed * 100 / 10_000); p.sendActionBar(Component.text("§7🛡 チャージ §e" + pct + "%")); } } }
                case VAMPIRE -> { double gauge = vampireGauge.getOrDefault(p.getUniqueId(), 0.0); int stage = gauge >= 55 ? 4 : gauge >= 40 ? 3 : gauge >= 25 ? 2 : gauge >= 10 ? 1 : 0; boolean inBlood = vampireBloodMode.getOrDefault(p.getUniqueId(), false); String bar = gaugeBar((int)gauge, 80); p.sendActionBar(Component.text((inBlood ? "§4§l⚔ ブラッド" : "§7ドレイン") + " S" + stage + " §8[§a" + bar + "§8] §f" + String.format("%.0f/80", gauge))); }
                default -> {}
            }
        }
    }

    private String gaugeBar(int val, int max) { int bars = Math.min(10, Math.max(0, val * 10 / max)); return "§a" + "█".repeat(bars) + "§7" + "█".repeat(10 - bars); }

    // ─── Skill dispatcher ───

    private void useKitSkill(Player p, String kitName) {
        KitType kit; try { kit = KitType.valueOf(kitName); } catch (Exception e) { return; }
        if (gm.getPlayerKitType(p.getUniqueId()) != kit) return;
        if (deadlockedPlayers.contains(p.getUniqueId())) { p.sendMessage("§cデッドロック中はスキルを使用できません！"); return; }
        if (isOnCooldown(p.getUniqueId())) { Long cd = skillCooldowns.get(p.getUniqueId()); long remain = cd != null ? (cd - System.currentTimeMillis()) / 1000 : 0; p.sendMessage("§cスキルクールタイム中！ §7残り§f" + remain + "§7秒"); return; }
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.8f);
        switch (kit) {
            case BLADE -> bladeSkill(p);
            case BREAKER -> breakerSkill(p);
            case NINJA -> ninjaSkill(p);
            case BERSERKER -> berserkerSkill(p);
            case COUNTER -> {}
            case PYRO -> pyroSkill(p);
            case JESTER -> jesterSkill(p);
            case VAMPIRE -> vampireToggle(p);
            case BOMBER -> bomberSkill(p);
            case COOK -> cookSkill(p);
            case SCOUT -> scoutSkill(p);
            case WHIRLWIND -> whirlwindGust(p);
            case NILGIRITAR -> {}
            case FLASHER -> flasherSkill(p);
            case MARKSMAN -> marksmanSkill(p);
            case SUNDANCE -> {} // activated via sneak+right-click crossbow
            case ROCKETER -> rocketerFire(p);
            case ALCHEMIST -> alchemistSkill(p);
            case ENGINEER -> engineerSkill(p);
            case TRAPPER -> trapperSkill(p);
            case GUARDIAN -> guardianSkill(p);
            case MEDIC -> medicSkill(p);
            case SUPPORTER -> supporterSkill(p);
            case RESTRICTIONER -> restrictionerSkill(p);
            case KREUTZ -> kreutzSkill(p);
            case MIMIC -> mimicSkill(p);
            case SWAPPER -> swapperSkill(p);
            case STICKER -> stickerSkill(p);
            case DECOY -> decoySkill(p);
            case PHANTOM -> phantomSkill(p);
            case ANCHOR -> anchorSkill(p);
            case RELEASER -> releaseSkill(p);
            case GRANG -> {}
            case NECRO -> necroSkill(p);
            default -> {}
        }
    }

    public void clearSniperMarkOnShoot(Player p) {
        if (!markedForDeath.isEmpty() && gm.getPlayerKitType(p.getUniqueId()) == KitType.SNIPER) {
            markedForDeath.clear();
            sniperAimTick.remove(p.getUniqueId());
            sniperTracker.remove(p.getUniqueId());
        }
    }

    private void useBurst(Player p) {
        if (deadlockedPlayers.contains(p.getUniqueId())) { p.sendMessage("§cデッドロック中はバーストを使用できません！"); return; }
        if (burstUsed.contains(p.getUniqueId())) { p.sendMessage("§cバーストはこのラウンドでは使用済みです。"); return; }
        burstUsed.add(p.getUniqueId()); p.getInventory().setItem(8, null);
        Location loc = p.getLocation(); World w = p.getWorld();
        // Float up + invincible
        p.setInvulnerable(true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 10, 1, false, true));
        w.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 2f, 2f);
        p.sendMessage("§c§lバースト発動！");
        // Delayed burst after 0.5s
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            p.removePotionEffect(PotionEffectType.LEVITATION);
            p.setInvulnerable(false);
            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1, false, true));
            Location loc2 = p.getLocation();
            w.createExplosion(loc2, 0f, false, false, p);
            w.spawnParticle(Particle.EXPLOSION_LARGE, loc2, 8, 1.5, 1, 1.5, 0.1);
            w.spawnParticle(Particle.CLOUD, loc2.clone().add(0, 1, 0), 30, 2, 1, 2, 0.05);
            w.playSound(loc2, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
            for (Entity e : w.getNearbyEntities(loc2, 4, 3, 4)) {
                if (!(e instanceof Player target) || !gm.isParticipant(target) || gm.getTeamOf(target) == gm.getTeamOf(p)) continue;
                target.damage(3.0, p); target.setVelocity(target.getLocation().toVector().subtract(loc2.toVector()).normalize().multiply(1.5).setY(0.5));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1, false, true));
            }
            // No fall damage
            gm.setNoFallDamage(p.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> gm.clearNoFallDamage(p.getUniqueId()), 100L);
        }, 10L);
    }

    // ─── Kit Skills (abbreviated - keeping full implementations) ───

    private void bladeSkill(Player p) { if (!p.isSneaking()) { p.sendMessage("§cしゃがみながら使用してください"); return; } setCooldown(p.getUniqueId(), 10_000L); World w = p.getWorld(); Location loc = p.getLocation(); w.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.7f); w.spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 1, 0), 5, 2, 0.5, 2, 0); for (Entity e : w.getNearbyEntities(loc, 3, 2, 3)) { if (!(e instanceof Player t) || !gm.isParticipant(t) || gm.getTeamOf(t) == gm.getTeamOf(p)) continue; t.damage(3.0, p); t.setVelocity(new Vector(0, 0.8, 0)); t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, true)); t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1, false, true)); } }

    private void breakerSkill(Player p) { setCooldown(p.getUniqueId(), 14_000L); Vector dir = p.getLocation().getDirection().normalize().multiply(2.0).setY(0.3); p.setVelocity(dir); p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.6f); new BukkitRunnable() { int t=0; @Override public void run() { if (t++>10 || !p.isOnline()) { cancel(); return; } Location cur = p.getLocation(); p.getWorld().spawnParticle(Particle.CRIT, cur, 5, 0.3, 0.3, 0.3, 0); for (Entity e : cur.getWorld().getNearbyEntities(cur, 2, 2, 2)) { if (!(e instanceof Player target) || !gm.isParticipant(target) || gm.getTeamOf(target)==gm.getTeamOf(p)) continue; target.damage(4.0, p); target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1, false, true)); } } }.runTaskTimer(plugin, 0L, 2L); }

    private void ninjaSkill(Player p) { setCooldown(p.getUniqueId(), 18_000L); p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 160, 0, false, false)); p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1, false, false)); hideArmor(p); Bukkit.getScheduler().runTaskLater(plugin, () -> { if (p.isOnline()) { showArmor(p); p.removePotionEffect(PotionEffectType.INVISIBILITY); p.removePotionEffect(PotionEffectType.SPEED); } }, 160L); p.sendMessage("§2§l隠形！"); }

    private void berserkerSkill(Player p) { setCooldown(p.getUniqueId(), 14_000L); Vector dir = p.getLocation().getDirection().normalize(); Location cur = p.getLocation().clone(); p.sendMessage("§4§l怒涛爆砕！"); new BukkitRunnable() { int t=0; @Override public void run() { if(t++>10) { cancel(); return; } cur.add(dir.clone().multiply(1.5)); cur.getWorld().spawnParticle(Particle.FLAME, cur, 10, 0.5, 0.5, 0.5, 0.03); if(t%2==0) { for(Entity e2:cur.getWorld().getNearbyEntities(cur,2.5,2,2.5)){if(e2 instanceof Player target2&&gm.isParticipant(target2)&&gm.getTeamOf(target2)!=gm.getTeamOf(p)){target2.damage(4.0,p);target2.setVelocity(dir.clone().multiply(1.5).setY(0.4));}} } } }.runTaskTimer(plugin, 2L, 2L); }

    private void pyroSkill(Player p) { setCooldown(p.getUniqueId(), 15_000L); World w = p.getWorld(); Location loc = p.getLocation(); w.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.5f); for (Entity e : w.getNearbyEntities(loc, 5, 3, 5)) { if (!(e instanceof Player t) || !gm.isParticipant(t) || gm.getTeamOf(t)==gm.getTeamOf(p)) continue; if (t.getFireTicks()>0) t.damage(12.0, p); else t.setFireTicks(80); t.getWorld().spawnParticle(Particle.FLAME, t.getLocation().add(0,1,0), 20, 0.3, 0.3, 0.3, 0.05); } }

    private void jesterSkill(Player p) { setCooldown(p.getUniqueId(), 12_000L); p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1, false, false)); p.sendMessage("§e§l道化の疾走！"); }

    private void theosPadaSkill(Player p) { theosPadaAction(p, true); }

    // ─── KREUTZ ───
    private final Map<UUID, String> kreutzCard = new HashMap<>();
    private final String[] KREUTZ_CARDS = {"ファイアボール","アイスランス","サンダー","シールド","ヒール","カース","グラビティ","チェイン","ポイズンクラウド","スピードブースト","リープ","ウィークネス","マインド","チェインライトニング","テレポートトラップ"};
    private void kreutzSkill(Player p) {
        if (p.isSneaking()) { if (isOnCooldown(p.getUniqueId())) return; setCooldown(p.getUniqueId(), 3_000L); String card = KREUTZ_CARDS[new java.util.Random().nextInt(KREUTZ_CARDS.length)]; kreutzCard.put(p.getUniqueId(), card); p.sendMessage("§5§l🃏 " + card + " §7を引いた！"); return; }
        String card = kreutzCard.remove(p.getUniqueId());
        if (card == null) { p.sendMessage("§cカードを引いていません！しゃがみ右クリでドロー"); return; }
        Player target = getTargetInSight(p, 20);
        p.sendMessage("§5§l🃏 " + card + " §7を唱えた！");
        switch (card) {
            case "ファイアボール" -> { Snowball b = p.launchProjectile(Snowball.class); b.setVelocity(p.getLocation().getDirection().normalize().multiply(1.5)); b.setGlowing(true); }
            case "アイスランス" -> { if(target!=null){target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,100,3,false,true));target.damage(4.0,p);} }
            case "サンダー" -> { if(target!=null){target.getWorld().strikeLightning(target.getLocation());target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,60,10,false,true));} }
            case "シールド" -> { p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,60,4,false,true)); }
            case "ヒール" -> { if(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)!=null)p.setHealth(Math.min(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(),p.getHealth()+8)); }
            case "カース" -> { if(target!=null)target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,200,2,false,true)); }
            case "グラビティ" -> { for(Entity e2:p.getWorld().getNearbyEntities(p.getLocation(),6,3,6)){if(e2 instanceof Player t2&&gm.isParticipant(t2)&&gm.getTeamOf(t2)!=gm.getTeamOf(p)){t2.setVelocity(p.getLocation().toVector().subtract(t2.getLocation().toVector()).normalize().multiply(2.0).setY(0.5));}} }
            case "チェイン" -> { if(target!=null){target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,100,10,false,true));target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,100,-10,false,true));} }
            case "ポイズンクラウド" -> { Location cl=p.getLocation(); for(Entity e2:cl.getWorld().getNearbyEntities(cl,4,2,4)){if(e2 instanceof Player t2&&gm.isParticipant(t2)&&gm.getTeamOf(t2)!=gm.getTeamOf(p)){t2.addPotionEffect(new PotionEffect(PotionEffectType.POISON,100,1,false,true));}} cl.getWorld().spawnParticle(Particle.SPELL_MOB,cl,30,4,1,4,0); }
            case "スピードブースト" -> { p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,200,2,false,true)); }
            case "リープ" -> { Vector ld=p.getLocation().getDirection().normalize().multiply(3.0).setY(0.5); p.setVelocity(ld); }
            case "ウィークネス" -> { if(target!=null)target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,100,3,false,true)); }
            case "マインド" -> { if(target!=null){target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,100,0,false,true));target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,100,100,false,true));} }
            case "チェインライトニング" -> { for(Entity e2:p.getWorld().getNearbyEntities(p.getLocation(),10,3,10)){if(e2 instanceof Player t2&&gm.isParticipant(t2)&&gm.getTeamOf(t2)!=gm.getTeamOf(p)){t2.getWorld().strikeLightningEffect(t2.getLocation());t2.damage(3.0,p);}} }
            case "テレポートトラップ" -> { Location tl=p.getLocation(); activeTraps.add(new TrapData(p.getUniqueId(),tl)); p.sendMessage("§5§lテレポートトラップ設置！"); }
            default -> p.sendMessage("§c§lエラー：無効なカード");
        }
    }

    private void vampireToggle(Player p) { boolean inBlood = !vampireBloodMode.getOrDefault(p.getUniqueId(), false); vampireBloodMode.put(p.getUniqueId(), inBlood); double gauge = vampireGauge.getOrDefault(p.getUniqueId(), 0.0); applyVampireStage(p, gauge>=55?4:gauge>=40?3:gauge>=25?2:gauge>=10?1:0); p.sendMessage(inBlood ? "§4§lブラッドモード起動！" : "§7ドレインモードに戻りました"); }

    private void bomberSkill(Player p) {
        if (p.isSneaking()) { detonateMines(p); return; }
        setCooldown(p.getUniqueId(), 12_000L); Location loc = p.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
        for (MineData existing : activeMines) { if (existing.owner.equals(p.getUniqueId()) && existing.entity.getLocation().distance(loc) < 5) { p.sendMessage("§c既存の地雷に近すぎます！（最低5m間隔）"); return; } }
        ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class, a -> { a.setInvisible(true); a.setMarker(true); a.setSmall(true); a.getPersistentDataContainer().set(KEY_MINE, PersistentDataType.BYTE, (byte)1); });
        activeMines.add(new MineData(as, p.getUniqueId()));
        p.sendMessage("§c§l地雷設置！§7しゃがみ右クリで起爆");
    }

    public void detonateMines(Player p) {
        long ready = activeMines.stream().filter(m -> m.owner.equals(p.getUniqueId())).count();
        if (ready == 0) { p.sendMessage("§c起爆できる地雷がありません。"); return; }
        setCooldown(p.getUniqueId(), 3_000L);
        for (MineData m : new ArrayList<>(activeMines)) {
            if (!m.owner.equals(p.getUniqueId())) continue;
            m.entity.getWorld().createExplosion(m.entity.getLocation(), 3f, false, false, p);
            m.entity.remove(); activeMines.remove(m);
        }
        p.sendMessage("§c§l起爆！");
    }

    private void gliderSkill(Player p) { /* retired */ }

    private void scoutSkill(Player p) {
        if (p.isSneaking()) { setCooldown(p.getUniqueId(), 8_000L); p.getPersistentDataContainer().set(KEY_PULSE, PersistentDataType.BYTE, (byte)1); p.sendMessage("§c§lパルスボルト準備完了！"); }
        else { setCooldown(p.getUniqueId(), 5_000L); p.getPersistentDataContainer().set(KEY_RECON, PersistentDataType.BYTE, (byte)1); p.sendMessage("§a§lリコンボルト準備完了！"); }
    }

    private void flasherSkill(Player p) { setCooldown(p.getUniqueId(), 10_000L); Snowball ball = p.launchProjectile(Snowball.class); ball.setVelocity(p.getLocation().getDirection().normalize().multiply(1.2)); ball.setCustomName("flashBang"); p.sendMessage("§b§lフラッシュバン！"); }

    private void marksmanSkill(Player p) { setCooldown(p.getUniqueId(), 12_000L); p.getPersistentDataContainer().set(new NamespacedKey(plugin, "heavy_bolt"), PersistentDataType.BYTE, (byte)1); p.sendMessage("§c§lヘヴィーボルト準備完了！"); }

    private void sundanceSkill(Player p) {
        if (sundanceRevolver.containsKey(p.getUniqueId())) { p.sendMessage("§cまだリボルビング中です！"); return; }
        if (isOnCooldown(p.getUniqueId())) { long remain = (skillCooldowns.get(p.getUniqueId())-System.currentTimeMillis())/1000; p.sendMessage("§cリボルビングCT: §7残り§f"+remain+"§7秒"); return; }
        sundanceRevolver.put(p.getUniqueId(), 6);
        ItemStack xbow = p.getInventory().getItemInMainHand();
        if (xbow != null && xbow.getType() == Material.CROSSBOW) {
            ItemMeta m = xbow.getItemMeta(); m.setDisplayName("§b§lリボルビング・クロスボウ [6/6]"); xbow.setItemMeta(m);
            ItemMeta fresh = xbow.getItemMeta();
            if (fresh instanceof CrossbowMeta cm) { cm.setChargedProjectiles(null); cm.addChargedProjectile(new ItemStack(Material.ARROW)); xbow.setItemMeta(cm); }
        }
        p.sendMessage("§b§lリボルビング・クロスボウ起動！6発自動装填");
    }

    private void rocketerSkill(Player p) {
        setCooldown(p.getUniqueId(), 40_000L); Snowball rocket = p.launchProjectile(Snowball.class);
        rocket.setVelocity(p.getLocation().getDirection().normalize().multiply(1.2)); rocket.setCustomName("megaRocket");
        rocket.getPersistentDataContainer().set(new NamespacedKey(plugin, "mega_rocket"), PersistentDataType.BYTE, (byte)1);
        rocket.getPersistentDataContainer().set(new NamespacedKey(plugin, "rocket_origin_x"), PersistentDataType.DOUBLE, p.getLocation().getX());
        rocket.getPersistentDataContainer().set(new NamespacedKey(plugin, "rocket_origin_y"), PersistentDataType.DOUBLE, p.getLocation().getY());
        rocket.getPersistentDataContainer().set(new NamespacedKey(plugin, "rocket_origin_z"), PersistentDataType.DOUBLE, p.getLocation().getZ());
        rocket.setGlowing(true); rocket.setGravity(false); p.sendMessage("§e§lメガロケット発射！");
        new BukkitRunnable() { int ticks=0; @Override public void run() { if(ticks++>50||!rocket.isValid()){cancel();return;} rocket.getWorld().spawnParticle(Particle.FLAME,rocket.getLocation(),3,0.2,0.2,0.2,0.02); } }.runTaskTimer(plugin,0L,1L);
        Bukkit.getScheduler().runTaskLater(plugin,()->{if(rocket.isValid()){megaRocketExplode(rocket.getLocation(),p);rocket.remove();}},50L);
    }

    private void megaRocketExplode(Location loc, Player shooter) { World w = loc.getWorld(); w.createExplosion(loc, 2f, false, false, shooter); w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.6f); }

    private float rocketPower(Location launch, Location explode, float maxPower) {
        double dist = launch.distance(explode);
        return Math.max(maxPower * 0.2f, Math.min(maxPower, (float)(dist / 10.0) * maxPower));
    }

    private void rocketerFire(Player p) { if (p.isSneaking()) rocketerSkill(p); else rocketerMicro(p); }

    private void rocketerMicro(Player p) { setCooldown(p.getUniqueId(), 8_000L); Snowball rocket = p.launchProjectile(Snowball.class); rocket.setVelocity(p.getLocation().getDirection().normalize().multiply(2.0)); rocket.setCustomName("microRocket"); rocket.getPersistentDataContainer().set(new NamespacedKey(plugin, "micro_rocket"), PersistentDataType.BYTE, (byte)1); rocket.getPersistentDataContainer().set(new NamespacedKey(plugin, "rocket_origin_x"), PersistentDataType.DOUBLE, p.getLocation().getX()); rocket.getPersistentDataContainer().set(new NamespacedKey(plugin, "rocket_origin_y"), PersistentDataType.DOUBLE, p.getLocation().getY()); rocket.getPersistentDataContainer().set(new NamespacedKey(plugin, "rocket_origin_z"), PersistentDataType.DOUBLE, p.getLocation().getZ()); rocket.setGravity(false); rocket.setGlowing(true); p.sendMessage("§e§lマイクロロケット！"); new BukkitRunnable(){int ticks=0;@Override public void run(){if(ticks++>30||!rocket.isValid()){cancel();return;}rocket.getWorld().spawnParticle(Particle.FLAME,rocket.getLocation(),1,0.1,0.1,0.1,0.01);}}.runTaskTimer(plugin,0L,1L); Bukkit.getScheduler().runTaskLater(plugin,()->{if(rocket.isValid()){float power = rocketHitPower(rocket, rocket.getLocation(), 1.5f); rocket.getWorld().createExplosion(rocket.getLocation(), 0f, false, false, p);for(Entity e2:rocket.getLocation().getWorld().getNearbyEntities(rocket.getLocation(),3,2,3)){if(e2 instanceof Player t&&gm.isParticipant(t)&&gm.getTeamOf(t)!=gm.getTeamOf(p)){t.damage(power*3,p);t.setVelocity(t.getLocation().toVector().subtract(rocket.getLocation().toVector()).normalize().multiply(1.5).setY(0.3));}} rocket.remove();}},30L); }

    private void alchemistSkill(Player p) { setCooldown(p.getUniqueId(), 10_000L); KitBuilder.refillAlchemistPotions(p); p.sendMessage("§d§l再調合！"); }

    private void engineerSkill(Player p) { setCooldown(p.getUniqueId(), 20_000L); if (activeTurrets.stream().filter(t->t.owner.equals(p.getUniqueId())).count() >= 2) { for (TurretData t : new ArrayList<>(activeTurrets)) { if(t.owner.equals(p.getUniqueId())){t.entity.remove();activeTurrets.remove(t);break;} } } Location loc = p.getLocation().add(p.getLocation().getDirection().multiply(2)); Skeleton skel = loc.getWorld().spawn(loc, Skeleton.class, s->{ s.setAI(false); s.setRemoveWhenFarAway(false); s.getEquipment().setItemInMainHand(new ItemStack(Material.BOW)); s.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET)); s.getEquipment().setHelmetDropChance(0f); s.setCustomName("§6[タレット]"); s.setCustomNameVisible(true); }); skel.setAI(true); activeTurrets.add(new TurretData(skel, p.getUniqueId(), gm.getTeamOf(p))); Bukkit.getScheduler().runTaskLater(plugin, () -> { if(skel.isValid()) { skel.remove(); activeTurrets.removeIf(t->t.entity.equals(skel)); } }, 1800L); p.sendMessage("§6§lレーザータレット設置！"); }

    private void trapperSkill(Player p) { setCooldown(p.getUniqueId(), 8_000L); if (activeTraps.stream().filter(t->t.owner.equals(p.getUniqueId())).count()>=2) { TrapData oldest = activeTraps.stream().filter(t->t.owner.equals(p.getUniqueId())).findFirst().orElse(null); if(oldest!=null) activeTraps.remove(oldest); } Location loc = p.getLocation().getBlock().getLocation().add(0.5,0,0.5); activeTraps.add(new TrapData(p.getUniqueId(), loc)); p.sendMessage("§3§l罠設置！最大2個"); }

    private void triggerTrap(TrapData t, Player victim) { t.triggered = true; t.loc.getWorld().playSound(t.loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f); t.loc.getWorld().spawnParticle(Particle.WAX_ON, t.loc.clone().add(0.5, 0.2, 0.5), 15, 0.5, 0.2, 0.5, 0.1); Bukkit.getScheduler().runTaskLater(plugin, () -> { t.loc.getWorld().createExplosion(t.loc, 2f, false, false, null); if (victim != null && victim.isOnline()) { victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false)); victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 1, false, false)); victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1, false, false)); victim.sendMessage("§c罠が爆発した！"); } activeTraps.remove(t); }, 16L); }

    private void guardianSkill(Player p) { setCooldown(p.getUniqueId(), 30_000L); p.setInvulnerable(true); guardianEndTime.put(p.getUniqueId(), System.currentTimeMillis()+7_000L); p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 140, 1, false, false)); p.sendMessage("§f§l鉄壁！7秒間無敵"); Bukkit.getScheduler().runTaskLater(plugin, ()->{ p.setInvulnerable(false); guardianEndTime.remove(p.getUniqueId()); p.sendMessage("§7鉄壁終了"); }, 140L); }

    private void medicSkill(Player p) { setCooldown(p.getUniqueId(), 15_000L); for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 8, 4, 8)) { if (e instanceof Player t && gm.isParticipant(t) && gm.getTeamOf(t)==gm.getTeamOf(p)) { t.setHealth(Math.min(t.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(), t.getHealth()+5)); t.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 2, false, false)); } } p.sendMessage("§5§lフィールドケア！"); }

    private void supporterSkill(Player p) { setCooldown(p.getUniqueId(), 10_000L); KitBuilder.refillSupporterPotions(p); p.sendMessage("§a§l再調達！"); }

    private void restrictionerSkill(Player p) {
        Player target = getTargetInSight(p,5); if(target==null||gm.getTeamOf(target)==gm.getTeamOf(p)){p.sendMessage("§c射程内にターゲットがいません。");return;}
        setCooldown(p.getUniqueId(),20_000L);
        Location mid = p.getLocation().add(target.getLocation()).multiply(0.5); p.teleport(mid); target.teleport(mid);
        PotionEffectType[] effs = {PotionEffectType.SLOW, PotionEffectType.WEAKNESS, PotionEffectType.SLOW_DIGGING, PotionEffectType.BLINDNESS};
        for(PotionEffectType et : effs) { p.addPotionEffect(new PotionEffect(et, 100, et==PotionEffectType.SLOW_DIGGING?100:et==PotionEffectType.BLINDNESS?0:10, false, false)); target.addPotionEffect(new PotionEffect(et, 100, et==PotionEffectType.SLOW_DIGGING?100:et==PotionEffectType.BLINDNESS?0:10, false, false)); }
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, -10, false, false)); target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, -10, false, false));
        deadlockedPlayers.add(p.getUniqueId()); deadlockedPlayers.add(target.getUniqueId());
        p.sendMessage("§8§lデッドロック！"); target.sendMessage("§8§l拘束された！");
        Bukkit.getScheduler().runTaskLater(plugin, ()->{ deadlockedPlayers.remove(p.getUniqueId()); deadlockedPlayers.remove(target.getUniqueId()); }, 100L);
    }

    private void placePortalA(Player p) { Location loc = p.getLocation().getBlock().getLocation().add(0.5,0,0.5); TeamColor team = gm.getTeamOf(p); portalA.put(p.getUniqueId(), loc); portalBlocks.add(loc.clone()); startPortalParticles(loc, team); p.sendMessage("§3§lポータルA（入口）設置！"); }

    private void placePortalB(Player p) { if(!portalA.containsKey(p.getUniqueId())){p.sendMessage("§c先に左クリックでポータルAを設置してください。");return;} setCooldown(p.getUniqueId(),15_000L); Location loc = p.getLocation().getBlock().getLocation().add(0.5,0,0.5); TeamColor team = gm.getTeamOf(p); if(loc.distance(portalA.get(p.getUniqueId()))<3){p.sendMessage("§c近すぎます");return;} Location oldB = portalB.remove(p.getUniqueId()); if(oldB!=null) portalBlocks.remove(oldB); portalB.put(p.getUniqueId(), loc); portalBlocks.add(loc.clone()); startPortalParticles(loc, team); p.sendMessage("§3§lポータルB（出口）設置！"); }

    private void startPortalParticles(Location loc, TeamColor team) { Color color = team==TeamColor.RED?Color.RED:Color.AQUA; Particle.DustOptions dust = new Particle.DustOptions(color, 1.5f); new BukkitRunnable(){@Override public void run(){if(portalBlocks.contains(loc)&&loc.getWorld()!=null){loc.getWorld().spawnParticle(Particle.PORTAL,loc.clone().add(0.5,1,0.5),8,0.4,0.8,0.4,0.1);loc.getWorld().spawnParticle(Particle.REDSTONE,loc.clone().add(0.5,0.3,0.5),5,0.3,0.3,0.3,dust);}else cancel();}}.runTaskTimer(plugin,0L,10L); }

    private void mimicSkill(Player p) {
        Player target = getTargetInSight(p,20);
        if(target==null){p.sendMessage("§c射程内にターゲットがいません。");return;}
        KitType targetKit = gm.getPlayerKitType(target.getUniqueId());
        if(targetKit==null||targetKit==KitType.MIMIC){p.sendMessage("§cコピー不可");return;}
        setCooldown(target.getUniqueId(),5_000L); p.sendMessage("§5§l"+target.getName()+" のスキルをコピー！"); target.sendMessage("§5スキルコピーされた！クールタイム5秒");
        switch(targetKit){
            case MARKSMAN->mimicCopyMarksman(p,target);
            case SCOUT->mimicCopyScout(p,target);
            case SUNDANCE->mimicCopySundance(p,target);
            case SNIPER->mimicCopySniper(p,target);
            default->{KitType originalKit=gm.getPlayerKitType(p.getUniqueId());gm.setPlayerKit(p.getUniqueId(),targetKit.name());useKitSkill(p,targetKit.name());gm.setPlayerKit(p.getUniqueId(),originalKit.name());}
        }
        setCooldown(p.getUniqueId(),25_000L);
    }

    private void mimicCopyMarksman(Player p, Player target) { if(target.getAttribute(Attribute.GENERIC_MAX_HEALTH)!=null){double cm=target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();maxHpReduced.putIfAbsent(target.getUniqueId(),cm);target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.max(1,cm-10));if(target.getHealth()>cm-10)target.setHealth(cm-10);} target.getPersistentDataContainer().set(new NamespacedKey(plugin,"hp_reduced"),PersistentDataType.BYTE,(byte)1); target.sendMessage("§c§lHP上限が10低下しました！"); p.sendMessage("§c§lヘヴィーボルトコピー！"); }
    private void mimicCopyScout(Player p, Player target) { ArmorStand as=target.getWorld().spawn(target.getLocation().add(0,1,0),ArmorStand.class,a->{a.setInvisible(true);a.setMarker(true);a.setCustomName("§a🔍 リコン");a.setCustomNameVisible(true);}); activeRecons.add(new ReconData(as,gm.getTeamOf(p))); Bukkit.getScheduler().runTaskLater(plugin,()->{as.remove();activeRecons.removeIf(r->r.entity.equals(as));},1200L); p.sendMessage("§aリコン設置！"); target.sendMessage("§c§lリコンを設置された！"); }
    private void mimicCopySundance(Player p, Player target) { p.sendMessage("§b§l❄ リボルバー6連射コピー！"); for(int i=0;i<6;i++){Bukkit.getScheduler().runTaskLater(plugin,()->{if(target.isValid()&&p.isOnline()){target.damage(2.0,p);target.getWorld().spawnParticle(Particle.CRIT_MAGIC,target.getLocation().add(0,1,0),5,0.3,0.3,0.3,0);}},i*3L);} }
    private void mimicCopySniper(Player p, Player target) { markedForDeath.add(target.getUniqueId()); p.sendMessage("§c§l狙撃眼: §f"+target.getName()+" §cをマークしました！"); target.sendMessage("§c§l⚠ 狙撃されています！"); target.getWorld().playSound(target.getLocation(),Sound.BLOCK_BELL_USE,1f,0.5f); }

    private void swapperSkill(Player p) { Player target = getTargetInSight(p,15); if(target==null){p.sendMessage("§c射程内にターゲットがいません。");return;} setCooldown(p.getUniqueId(),18_000L); Location pLoc=p.getLocation().clone(); Location tLoc=target.getLocation().clone(); p.teleport(tLoc); target.teleport(pLoc); }

    private void stickerSkill(Player p) { setCooldown(p.getUniqueId(),14_000L); Snowball ball = p.launchProjectile(Snowball.class); ball.setVelocity(p.getLocation().getDirection().normalize().multiply(1.5)); ball.getPersistentDataContainer().set(new NamespacedKey(plugin,"grapple"),PersistentDataType.BYTE,(byte)1); p.sendMessage("§3§lグラップル！"); }

    private void decoySkill(Player p) { setCooldown(p.getUniqueId(),18_000L); Location center=p.getLocation(); p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,160,0,false,false)); hideArmor(p); Bukkit.getScheduler().runTaskLater(plugin,()->showArmor(p),160L); for(int i=0;i<8;i++){double angle=i*Math.PI/4; Location sl=center.clone().add(Math.cos(angle)*2,0,Math.sin(angle)*2); Skeleton skel=center.getWorld().spawn(sl,Skeleton.class,s->{s.setAI(false);s.setRemoveWhenFarAway(false);s.setSilent(true);s.setCustomName(p.getName());s.setCustomNameVisible(true);s.getEquipment().setHelmet(p.getInventory().getHelmet());s.getEquipment().setChestplate(p.getInventory().getChestplate());s.getEquipment().setLeggings(p.getInventory().getLeggings());s.getEquipment().setBoots(p.getInventory().getBoots());s.getEquipment().setHelmetDropChance(0f);s.getEquipment().setChestplateDropChance(0f);s.getEquipment().setLeggingsDropChance(0f);s.getEquipment().setBootsDropChance(0f);}); skel.getPersistentDataContainer().set(new NamespacedKey(plugin,"decoy"),PersistentDataType.BYTE,(byte)1); Bukkit.getScheduler().runTaskLater(plugin,()->skel.remove(),160L); } p.sendMessage("§8§lデコイ展開！"); }

    private void phantomSkill(Player p) { setCooldown(p.getUniqueId(),22_000L); p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,120,0,false,false)); p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,125,255,true,false)); long endTime=System.currentTimeMillis()+6_000L; phantomEnd.put(p.getUniqueId(),endTime); p.sendMessage("§7§l霊体化！6秒間透明＋無敵"); p.getWorld().playSound(p.getLocation(),Sound.ENTITY_PHANTOM_AMBIENT,1f,0.5f); hideArmor(p); UUID uid=p.getUniqueId(); Bukkit.getScheduler().runTaskLater(plugin,()->{Player pl=Bukkit.getPlayer(uid);if(pl!=null&&phantomEnd.containsKey(uid)&&System.currentTimeMillis()>=phantomEnd.get(uid)){pl.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);pl.removePotionEffect(PotionEffectType.INVISIBILITY);showArmor(pl);phantomEnd.remove(uid);pl.sendMessage("§7霊体化の効果が切れました");}},120L); }

    private void anchorSkill(Player p) { setCooldown(p.getUniqueId(),25_000L); anchorFields.put(p.getUniqueId(),p.getLocation().clone()); p.sendMessage("§9§l磁場展開！25秒間 半径12mの敵を完全拘束＋持続ダメージ"); p.getWorld().playSound(p.getLocation(),Sound.BLOCK_BEACON_ACTIVATE,1f,0.5f); UUID uid=p.getUniqueId(); new BukkitRunnable(){int count=0;@Override public void run(){if(count++>12||!anchorFields.containsKey(uid)){cancel();return;}Location loc=anchorFields.get(uid);if(loc==null||loc.getWorld()==null){cancel();return;}for(Player t:loc.getWorld().getPlayers()){if(t.getLocation().distance(loc)<=12&&gm.isParticipant(t)&&!gm.isSpectator(t)&&gm.getTeamOf(t)!=gm.getTeam(uid)){t.damage(3.0,Bukkit.getPlayer(uid));}}loc.getWorld().spawnParticle(Particle.PORTAL,loc,40,12,1,12,0.02);}}.runTaskTimer(plugin,40L,40L); Bukkit.getScheduler().runTaskLater(plugin,()->{anchorFields.remove(p.getUniqueId());p.sendMessage("§7磁場終了");},500L); }

    private void whirlwindGust(Player p) { setCooldown(p.getUniqueId(),12_000L); Vector dir=p.getLocation().getDirection().normalize(); Location start=p.getEyeLocation().add(dir.clone().multiply(2)); p.sendMessage("§f§l気流砲！"); new BukkitRunnable(){int ticks=0;Location cur=start.clone();@Override public void run(){if(ticks++>140){cancel();return;}cur.add(dir.clone().multiply(0.5));cur.getWorld().spawnParticle(Particle.CLOUD,cur,8,1.5,1,1.5,0.02);for(Entity e:cur.getWorld().getNearbyEntities(cur,2,2,2)){if(!(e instanceof LivingEntity le)||!le.isValid())continue;if(e instanceof Player tp&&!gm.isParticipant(tp))continue;Vector push=dir.clone().multiply(0.2);push.setY(Math.min(push.getY(),0.1));if(ticks%2==0)le.setVelocity(push);}}}.runTaskTimer(plugin,0L,2L); }

    public void whirlwindBall(Player p) { if(gm.getPlayerKitType(p.getUniqueId())!=KitType.WHIRLWIND)return; if(isOnCooldown(p.getUniqueId())){Long cd=skillCooldowns.get(p.getUniqueId());long remain=cd!=null?(cd-System.currentTimeMillis())/1000:0;p.sendMessage("§cスキルクールタイム中！ §7残り§f"+remain+"§7秒");return;} setCooldown(p.getUniqueId(),12_000L); Snowball ball=p.launchProjectile(Snowball.class);ball.setVelocity(p.getLocation().getDirection().normalize().multiply(0.8));ball.setCustomName("whirlwindBall");ball.setGlowing(true);p.sendMessage("§f§l旋風弾！");new BukkitRunnable(){int ticks=0;@Override public void run(){if(ticks++>60||!ball.isValid()){cancel();return;}ball.getWorld().spawnParticle(Particle.SWEEP_ATTACK,ball.getLocation(),5,0.5,0.5,0.5,0);Player nearest=null;double nd=36;for(Player t:ball.getWorld().getPlayers()){if(!gm.isParticipant(t)||gm.getTeamOf(t)==gm.getTeamOf(p))continue;double d=t.getLocation().distanceSquared(ball.getLocation());if(d<6&&d<nd){nd=d;nearest=t;}}if(nearest!=null){nearest.setVelocity(new Vector(0,1.5,0));nearest.damage(3.0,p);nearest.sendMessage("§f§l旋風弾が直撃！");ball.remove();cancel();return;}Vector toTarget=nearest!=null?nearest.getLocation().toVector().subtract(ball.getLocation().toVector()).normalize().multiply(0.3):new Vector();if(toTarget.lengthSquared()>0)ball.setVelocity(ball.getVelocity().add(toTarget).normalize().multiply(0.8));}}.runTaskTimer(plugin,0L,2L); }

    public void releaserMegaBurst(Player p) { if(gm.getPlayerKitType(p.getUniqueId())!=KitType.RELEASER)return; if(releaserMegaUsed.contains(p.getUniqueId())){p.sendMessage("§c超大解放はこのラウンド使用済みです。");return;} releaserMegaUsed.add(p.getUniqueId()); Location loc=p.getLocation();World w=p.getWorld();w.playSound(loc,Sound.ENTITY_GENERIC_EXPLODE,2f,0.5f);w.spawnParticle(Particle.EXPLOSION_HUGE,loc,8,2,2,2,0);for(Entity e:w.getNearbyEntities(loc,6,3,6)){if(!(e instanceof Player t)||!gm.isParticipant(t)||gm.getTeamOf(t)==gm.getTeamOf(p))continue;t.damage(8.0,p);t.setVelocity(t.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(2.5).setY(1.0));t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,120,2,false,true));t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,100,2,false,true));}p.sendMessage("§e§l💢 超大解放！"); }

    private void releaseSkill(Player p) { if(isOnCooldown(p.getUniqueId())){Long cd=skillCooldowns.get(p.getUniqueId());long remain=cd!=null?(cd-System.currentTimeMillis())/1000:0;p.sendMessage("§cリリースCT中！ §7残り§f"+remain+"§7秒");return;} setCooldown(p.getUniqueId(),8_000L);Location loc=p.getLocation();World w=p.getWorld();w.playSound(loc,Sound.ENTITY_GENERIC_EXPLODE,1f,0.7f);w.spawnParticle(Particle.EXPLOSION_LARGE,loc,5,1.5,1,1.5,0.05);p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,40,0,false,true));for(Entity e:w.getNearbyEntities(loc,4,2,4)){if(!(e instanceof Player t)||!gm.isParticipant(t)||gm.getTeamOf(t)==gm.getTeamOf(p))continue;t.damage(4.0,p);t.setVelocity(t.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.5).setY(0.4));t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,60,1,false,true));}p.sendMessage("§eリリース！"); }

    public void theosPadaAction(Player p, boolean isRightClick) { if(gm.getPlayerKitType(p.getUniqueId())!=KitType.VAMPIRE)return; double gauge=vampireGauge.getOrDefault(p.getUniqueId(),0.0); if(isRightClick){if(isOnCooldown(p.getUniqueId()))return;setCooldown(p.getUniqueId(),10_000L);Snowball ball=p.launchProjectile(Snowball.class);ball.setVelocity(p.getLocation().getDirection().normalize().multiply(0.5));ball.setCustomName("theos_pada");ball.getPersistentDataContainer().set(new NamespacedKey(plugin,"theos_pada"),PersistentDataType.BYTE,(byte)1);p.sendMessage("§4§lテオスパーダ（吸収弾）！");}else{if(gauge<5){p.sendMessage("§c吸血ゲージが足りません（必要:5）");return;}vampireGauge.put(p.getUniqueId(),gauge-5);Player target=getTargetInSight(p,30);if(target==null){p.sendMessage("§c射程内にターゲットがいません。");return;}target.damage(5.0,p);target.getWorld().spawnParticle(Particle.SWEEP_ATTACK,target.getLocation().add(0,1,0),10,0.3,0.3,0.3,0.1);p.sendMessage("§4§lテオスパーダ（破壊光線）！§7ゲージ-5");target.sendMessage("§4§lテオスパーダが直撃！");} }

    private boolean isSword(ItemStack item) {
        if (item == null) return false;
        return item.getType() == Material.WOODEN_SWORD || item.getType() == Material.STONE_SWORD
            || item.getType() == Material.IRON_SWORD || item.getType() == Material.DIAMOND_SWORD;
    }

    private void cookGenerateFood(Player p) {
        if (isOnCooldown(p.getUniqueId())) return;
        setCooldown(p.getUniqueId(), 1_000L);
        Material[] all = {Material.COOKED_BEEF, Material.COOKED_CHICKEN, Material.GOLDEN_CARROT,
                Material.COOKED_PORKCHOP, Material.PUMPKIN_PIE, Material.BREAD, Material.HONEY_BOTTLE,
                Material.BEETROOT_SOUP, Material.ROTTEN_FLESH, Material.SPIDER_EYE, Material.POISONOUS_POTATO,
                Material.PUFFERFISH, Material.CHICKEN, Material.PORKCHOP, Material.BEEF, Material.MUTTON,
                Material.COD, Material.SALMON};
        Material mat = all[new java.util.Random().nextInt(all.length)];
        ItemStack food = new ItemStack(mat); ItemMeta meta = food.getItemMeta();
        meta.getPersistentDataContainer().set(KEY_COOK, PersistentDataType.STRING, mat.name());
        meta.setDisplayName(getCookLabel(mat)); meta.setLore(List.of("§7スキル星で使用"));
        food.setItemMeta(meta); p.getInventory().addItem(food);
        p.sendMessage("§6" + getCookLabel(mat) + " §6を手に入れた！");
    }

    private void cookSkill(Player p) {
        java.util.List<Material> foods = findAllCookFoods(p);
        if (foods.isEmpty()) { p.sendMessage("§c食材を持っていません！剣を右クリックで入手"); return; }
        setCooldown(p.getUniqueId(), 1_000L);
        for (Material mat : foods) { applyCookBuff(p, mat); }
        p.sendMessage("§6§l食材" + foods.size() + "種を一気に使用！（自分）");
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1f, 1.2f);
    }

    private void cookThrowFood(Player p) {
        java.util.List<Material> foods = findAllCookFoods(p);
        if (foods.isEmpty()) { p.sendMessage("§c食材を持っていません！剣を右クリックで入手"); return; }
        setCooldown(p.getUniqueId(), 1_000L);
        // Throw all as a cluster
        int count = foods.size();
        for (Material mat : foods) {
            Snowball ball = p.launchProjectile(Snowball.class);
            ball.setVelocity(p.getLocation().getDirection().normalize().multiply(1.5).add(new Vector(Math.random()*0.4-0.2, Math.random()*0.2, Math.random()*0.4-0.2)));
            ball.setCustomName("cookThrow");
            ball.getPersistentDataContainer().set(KEY_COOK, PersistentDataType.STRING, mat.name());
        }
        p.sendMessage("§6§l食材" + count + "種をまとめて投げた！");
    }

    private java.util.List<Material> findAllCookFoods(Player p) {
        java.util.List<Material> foods = new java.util.ArrayList<>();
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getItemMeta() != null
                    && item.getItemMeta().getPersistentDataContainer().has(KEY_COOK, PersistentDataType.STRING)) {
                String matName = item.getItemMeta().getPersistentDataContainer().get(KEY_COOK, PersistentDataType.STRING);
                foods.add(Material.valueOf(matName));
                item.setAmount(0); // Remove all at once
            }
        }
        return foods;
    }

    private String getCookLabel(Material mat) { return switch(mat){ case COOKED_BEEF->"§6ステーキ §7[§c力III 10s§7]"; case COOKED_CHICKEN->"§f鶏肉 §7[§b速度III 10s§7]"; case GOLDEN_CARROT->"§e金ニンジン §7[§d再生III 8s§7]"; case COOKED_PORKCHOP->"§d豚肉 §7[§7耐性III 10s§7]"; case PUMPKIN_PIE->"§6パンプキンパイ §7[§e吸収III 20s§7]"; case BREAD->"§eパン §7[§6満腹回復§7]"; case HONEY_BOTTLE->"§6ハチミツ §7[§a跳躍IV 8s§7]"; case BEETROOT_SOUP->"§cビートルートスープ §7[§7耐火 20s§7]"; case ROTTEN_FLESH->"§8腐肉 §7[§c空腹IV 10s§7]"; case SPIDER_EYE->"§5蜘蛛の目 §7[§2毒III 8s§7]"; case POISONOUS_POTATO->"§a青くなったジャガイモ §7[§d吐気 5s§7]"; case PUFFERFISH->"§eフグ §7[§8衰弱+吐気 5s§7]"; case CHICKEN->"§f生鶏肉 §7[§7弱体化III 10s§7]"; case PORKCHOP->"§d生豚肉 §7[§7鈍足III 10s§7]"; case BEEF->"§c生牛肉 §7[§8採掘低下III 15s§7]"; case MUTTON->"§5生羊肉 §7[§0盲目 3s§7]"; case COD->"§b生鱈 §7[§4即時ダメ 4❤§7]"; case SALMON->"§d生鮭 §7[§f浮遊 3s§7]"; default->"§7料理"; }; }

    private void applyCookBuff(Player p, Material mat) { switch(mat){ case COOKED_BEEF->p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,200,2,false,true)); case COOKED_CHICKEN->p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,200,2,false,true)); case GOLDEN_CARROT->p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,160,2,false,true)); case COOKED_PORKCHOP->p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,200,2,false,true)); case PUMPKIN_PIE->p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,400,2,false,true)); case BREAD->{p.setFoodLevel(20);p.setSaturation(10f);} case HONEY_BOTTLE->p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,160,3,false,true)); case BEETROOT_SOUP->p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,400,0,false,true)); case ROTTEN_FLESH->p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER,200,3,false,true)); case SPIDER_EYE->p.addPotionEffect(new PotionEffect(PotionEffectType.POISON,160,2,false,true)); case POISONOUS_POTATO->p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,100,0,false,true)); case PUFFERFISH->{p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,100,2,false,true));p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,100,0,false,true));} case CHICKEN->p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,200,2,false,true)); case PORKCHOP->p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,200,2,false,true)); case BEEF->p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,300,2,false,true)); case MUTTON->p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,60,0,false,true)); case COD->p.damage(8.0); case SALMON->p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,60,0,false,true)); } }


    // ─── Vampire Stage System ───

    private void updateVampireStage(Player p, double gauge) { int stage = gauge>=55?4:gauge>=40?3:gauge>=25?2:gauge>=10?1:0; applyVampireStage(p, stage); }

    private void applyVampireStage(Player p, int stage) {
        if(p==null)return; boolean inBlood=vampireBloodMode.getOrDefault(p.getUniqueId(),false);
        if(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)!=null){ double hp=switch(stage){case 4->40;case 3->26;case 2->20;case 1->16;default->14;}; p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp); if(p.getHealth()>hp)p.setHealth(hp); }
        p.removePotionEffect(PotionEffectType.WEAKNESS);p.removePotionEffect(PotionEffectType.SLOW);p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);p.removePotionEffect(PotionEffectType.REGENERATION);p.removePotionEffect(PotionEffectType.SPEED);
        if(inBlood&&stage==4){p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,2,false,false));p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,Integer.MAX_VALUE,2,false,false));p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1,false,false));}
        else if(inBlood&&stage>=2){p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,0,false,false));}
        else if(!inBlood){if(stage<=1){if(stage==0){p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,Integer.MAX_VALUE,0,false,false));p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,Integer.MAX_VALUE,1,false,false));}else{p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,Integer.MAX_VALUE,0,false,false));}}}
    }

    // ─── Necro ───

    private void necroSkill(Player p) { if(gm.getPlayerKitType(p.getUniqueId())!=KitType.NECRO)return; List<Skeleton> army=necroArmy.computeIfAbsent(p.getUniqueId(),k->new ArrayList<>()); if(army.isEmpty()){spawnNecroArmy(p,army);return;} if(p.isSneaking()){for(Skeleton s:army){if(!s.isValid())continue;s.getPathfinder().moveTo(p.getLocation(),1.2);}p.sendMessage("§8部隊呼び戻し中...");}else{org.bukkit.block.Block targetBlock=p.getTargetBlockExact(30,FluidCollisionMode.NEVER);if(targetBlock==null){p.sendMessage("§c目標地点が見つかりません。");return;}Location targetLoc=targetBlock.getLocation().add(0.5,0,0.5);for(Skeleton s:army){if(!s.isValid())continue;s.getPathfinder().moveTo(targetLoc,1.2);}p.sendMessage("§8部隊移動中...");} }

    private void spawnNecroArmy(Player p, List<Skeleton> army) { TeamColor enemyTeam=gm.getTeamOf(p)==TeamColor.RED?TeamColor.BLUE:TeamColor.RED;Location base=p.getLocation();for(int i=0;i<3;i++){Location spawnLoc=base.clone().add((i-1)*2,0,0);final int idx=i;Skeleton skel=p.getWorld().spawn(spawnLoc,Skeleton.class,s->{s.setAI(false);s.setRemoveWhenFarAway(false);if(idx==0)s.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));else s.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));s.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));s.getEquipment().setHelmetDropChance(0f);s.getEquipment().setItemInMainHandDropChance(0f);s.setCustomName("§8["+p.getName()+"の兵]");s.setCustomNameVisible(true);});Skeleton finalSkel=skel;Bukkit.getScheduler().runTaskLater(plugin,()->{if(!finalSkel.isValid())return;finalSkel.setAI(true);for(Player target:p.getWorld().getPlayers()){if(gm.getTeamOf(target)==enemyTeam&&gm.isParticipant(target)&&!gm.isSpectator(target)){finalSkel.setTarget(target);break;}}},2L);army.add(skel);org.bukkit.scoreboard.Scoreboard board=Bukkit.getScoreboardManager().getMainScoreboard();String teamName="blox_"+(gm.getTeamOf(p)==TeamColor.RED?"red":"blue");org.bukkit.scoreboard.Team team=board.getTeam(teamName);if(team==null){team=board.registerNewTeam(teamName);team.setAllowFriendlyFire(false);}team.addEntry(skel.getUniqueId().toString());}p.sendMessage("§8§l💀 スケルトン3部隊を召喚！");new BukkitRunnable(){@Override public void run(){if(!p.isOnline()||gm.getState()!=com.bloxarena.game.GameState.IN_GAME){cancel();return;}List<Skeleton> current=necroArmy.get(p.getUniqueId());if(current==null){cancel();return;}for(int i=current.size()-1;i>=0;i--){if(!current.get(i).isValid())current.remove(i);}while(current.size()<3){Location spawnLoc=p.getLocation().clone().add(Math.random()*4-2,0,Math.random()*4-2);boolean hasBow=current.stream().anyMatch(s->s.isValid()&&s.getEquipment().getItemInMainHand().getType()==Material.BOW);final boolean needBow=!hasBow;Skeleton skel=p.getWorld().spawn(spawnLoc,Skeleton.class,s->{s.setAI(false);s.setRemoveWhenFarAway(false);if(needBow)s.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));else s.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));s.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));s.getEquipment().setHelmetDropChance(0f);s.setCustomName("§8["+p.getName()+"の兵]");s.setCustomNameVisible(true);});current.add(skel);Skeleton finalSkel=skel;Bukkit.getScheduler().runTaskLater(plugin,()->{if(!finalSkel.isValid())return;finalSkel.setAI(true);TeamColor et=gm.getTeamOf(p)==TeamColor.RED?TeamColor.BLUE:TeamColor.RED;for(Player target:p.getWorld().getPlayers()){if(gm.getTeamOf(target)==et&&gm.isParticipant(target)&&!gm.isSpectator(target)){finalSkel.setTarget(target);break;}}},2L);org.bukkit.scoreboard.Scoreboard board2=Bukkit.getScoreboardManager().getMainScoreboard();String teamName2="blox_"+(gm.getTeamOf(p)==TeamColor.RED?"red":"blue");org.bukkit.scoreboard.Team team2=board2.getTeam(teamName2);if(team2==null){team2=board2.registerNewTeam(teamName2);team2.setAllowFriendlyFire(false);}team2.addEntry(skel.getUniqueId().toString());}}}.runTaskTimer(plugin,400L,400L);}

    // ─── Utilities ───

    private void setCooldown(UUID uid, long millis) { skillCooldowns.put(uid, System.currentTimeMillis() + millis); }
    private boolean isOnCooldown(UUID uid) { Long cd = skillCooldowns.get(uid); return cd != null && System.currentTimeMillis() < cd; }

    private void hideArmor(Player p) { ItemStack[] armor = new ItemStack[4]; armor[0]=p.getInventory().getHelmet();armor[1]=p.getInventory().getChestplate();armor[2]=p.getInventory().getLeggings();armor[3]=p.getInventory().getBoots(); storedArmor.put(p.getUniqueId(), armor); p.getInventory().setHelmet(null);p.getInventory().setChestplate(null);p.getInventory().setLeggings(null);p.getInventory().setBoots(null); }
    private void showArmor(Player p) { ItemStack[] armor = storedArmor.remove(p.getUniqueId()); if (armor != null) p.getInventory().setArmorContents(armor); }

    private Player getTargetInSight(Player p, int maxDist) { for (Entity e : p.getNearbyEntities(maxDist, maxDist, maxDist)) { if (!(e instanceof Player t) || !gm.isParticipant(t) || gm.getTeamOf(t) == gm.getTeamOf(p)) continue; if (hasLineOfSight(p.getEyeLocation(), t.getEyeLocation())) return t; } return null; }

    private boolean hasLineOfSight(Location from, Location to) { Vector dir = to.toVector().subtract(from.toVector()); int dist = (int) dir.length(); dir.normalize(); for (int i = 0; i < dist; i++) { Location check = from.clone().add(dir.clone().multiply(i)); if (check.getBlock().getType().isOccluding()) return false; } return true; }

    public void clearPlayerPlacements(UUID uid) { for(MineData m:new ArrayList<>(activeMines)){if(m.owner.equals(uid)){m.entity.remove();activeMines.remove(m);}} for(TurretData t:new ArrayList<>(activeTurrets)){if(t.owner.equals(uid)){t.entity.remove();activeTurrets.remove(t);}} activeTraps.removeIf(t->t.owner.equals(uid)); TeamColor team=gm.getTeam(uid); if(team!=null){activeRecons.removeIf(r->{if(r.ownerTeam==team){r.entity.remove();return true;}return false;});} Location a=portalA.remove(uid);Location b=portalB.remove(uid);if(a!=null)portalBlocks.remove(a);if(b!=null)portalBlocks.remove(b); List<Skeleton> army=necroArmy.remove(uid);if(army!=null){for(Skeleton s:army){if(s.isValid())s.remove();}} storedArmor.remove(uid);phantomEnd.remove(uid); }
}
