/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent$Builder
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.Bukkit
 *  org.bukkit.Color
 *  org.bukkit.FluidCollisionMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Particle
 *  org.bukkit.Particle$DustOptions
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.block.Block
 *  org.bukkit.entity.ArmorStand
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.EvokerFangs
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.entity.Skeleton
 *  org.bukkit.entity.Snowball
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.CrossbowMeta
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.scoreboard.Scoreboard
 *  org.bukkit.scoreboard.Team
 *  org.bukkit.util.Vector
 */
package com.bloxarena.skill;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameManager;
import com.bloxarena.game.GameState;
import com.bloxarena.game.TeamColor;
import com.bloxarena.kit.KitBuilder;
import com.bloxarena.kit.KitRole;
import com.bloxarena.kit.KitType;
import com.bloxarena.map.MapConfig;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

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
    private final Set<UUID> burstUsed = new HashSet<UUID>();
    private final Map<UUID, Long> skillCooldowns = new HashMap<UUID, Long>();
    private final Set<UUID> parryActive = new HashSet<UUID>();
    private final Set<UUID> guardBroken = new HashSet<UUID>();
    private final Map<UUID, UUID> sniperTracker = new HashMap<UUID, UUID>();
    private final Map<UUID, Integer> sniperAimTick = new HashMap<UUID, Integer>();
    private final Map<UUID, Integer> sniperLostTick = new HashMap<UUID, Integer>();
    private int tickCounter = 0;
    private final Set<UUID> markedForDeath = new HashSet<UUID>();
    private final Map<UUID, Double> maxHpReduced = new HashMap<UUID, Double>();
    private final Map<UUID, Double> vampireGauge = new HashMap<UUID, Double>();
    private final Map<UUID, Boolean> vampireBloodMode = new HashMap<UUID, Boolean>();
    private final Map<UUID, Integer> sundanceRevolver = new HashMap<UUID, Integer>();
    private final List<TrapData> activeTraps = new ArrayList<TrapData>();
    private final List<MineData> activeMines = new ArrayList<MineData>();
    private final List<TurretData> activeTurrets = new ArrayList<TurretData>();
    private final Map<UUID, Location> portalA = new HashMap<UUID, Location>();
    private final Map<UUID, Location> portalB = new HashMap<UUID, Location>();
    private final List<Location> portalBlocks = new ArrayList<Location>();
    private final List<ReconData> activeRecons = new ArrayList<ReconData>();
    private final List<PulseData> activePulsers = new ArrayList<PulseData>();
    private final Map<UUID, Long> phantomEnd = new HashMap<UUID, Long>();
    private final Map<UUID, Location> anchorFields = new HashMap<UUID, Location>();
    private BukkitTask antiBetrayalTask = null;
    private final Map<UUID, Long> guardianEndTime = new HashMap<UUID, Long>();
    private final Map<UUID, Long> grangChargeStart = new HashMap<UUID, Long>();
    private final Map<UUID, Boolean> grangCharging = new HashMap<UUID, Boolean>();
    private final Map<UUID, List<Skeleton>> necroArmy = new HashMap<UUID, List<Skeleton>>();
    private final Set<UUID> deadlockedPlayers = new HashSet<UUID>();
    private final Map<UUID, Long> portalCooldowns = new HashMap<UUID, Long>();
    private final Map<UUID, Location> portalLastUsed = new HashMap<UUID, Location>();
    private final Map<UUID, ItemStack[]> storedArmor = new HashMap<UUID, ItemStack[]>();
    private final Set<UUID> gliderInAir = new HashSet<UUID>();
    private final Set<UUID> releaserMegaUsed = new HashSet<UUID>();
    private final Map<UUID, Long> nilgiritarMarks = new HashMap<UUID, Long>();
    private static final long NILGIRITAR_MARK_DURATION = 5000L;
    private final Map<UUID, Integer> comboCount = new HashMap<UUID, Integer>();
    private final Map<UUID, Long> comboLastHit = new HashMap<UUID, Long>();
    private final Map<UUID, Integer> sneakChargeTicks = new HashMap<UUID, Integer>();
    private final Map<UUID, Boolean> universalCharged = new HashMap<UUID, Boolean>();
    private final Map<UUID, String> kreutzCard = new HashMap<UUID, String>();
    private final Set<UUID> piercingRecently = new HashSet<UUID>();
    private final Map<UUID, Integer> marksmanBoltCount = new HashMap<UUID, Integer>();
    private final Map<UUID, Long> grangBurstCooldown = new HashMap<UUID, Long>();
    private final Map<UUID, List<Location>> activeWalls = new HashMap<UUID, List<Location>>();
    private final Map<UUID, Long> wallPlacedTime = new HashMap<UUID, Long>();
    private final Map<UUID, Deque<Snapshot>> timeSnapshots = new HashMap<UUID, Deque<Snapshot>>();
    private final Set<Entity> frozenProjectiles = new HashSet<Entity>();
    private final Map<UUID, Bond> activeBonds = new HashMap<UUID, Bond>();
    private final List<HexField> activeHexFields = new ArrayList<HexField>();
    private final Map<UUID, Long> mirrorEndTime = new HashMap<UUID, Long>();
    private final String[] KREUTZ_CARDS = new String[]{"\u30d5\u30a1\u30a4\u30a2\u30dc\u30fc\u30eb", "\u30a2\u30a4\u30b9\u30e9\u30f3\u30b9", "\u30b5\u30f3\u30c0\u30fc", "\u30b7\u30fc\u30eb\u30c9", "\u30d2\u30fc\u30eb", "\u30ab\u30fc\u30b9", "\u30b0\u30e9\u30d3\u30c6\u30a3", "\u30c1\u30a7\u30a4\u30f3", "\u30dd\u30a4\u30ba\u30f3\u30af\u30e9\u30a6\u30c9", "\u30b9\u30d4\u30fc\u30c9\u30d6\u30fc\u30b9\u30c8", "\u30ea\u30fc\u30d7", "\u30a6\u30a3\u30fc\u30af\u30cd\u30b9", "\u30de\u30a4\u30f3\u30c9", "\u30c1\u30a7\u30a4\u30f3\u30e9\u30a4\u30c8\u30cb\u30f3\u30b0", "\u30c6\u30ec\u30dd\u30fc\u30c8\u30c8\u30e9\u30c3\u30d7", "\u30d5\u30a1\u30f3\u30b0", "\u30d4\u30a2\u30c3\u30b7\u30f3\u30b0"};

    public SkillManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
        this.KEY_BURST = new NamespacedKey((Plugin)plugin, "burst_skill");
        this.KEY_SKILL = new NamespacedKey((Plugin)plugin, "kit_skill");
        this.KEY_TRAP = new NamespacedKey((Plugin)plugin, "ba_trap");
        this.KEY_MINE = new NamespacedKey((Plugin)plugin, "ba_mine");
        this.KEY_PORTAL = new NamespacedKey((Plugin)plugin, "ba_portal");
        this.KEY_RECON = new NamespacedKey((Plugin)plugin, "ba_recon");
        this.KEY_COOK = new NamespacedKey((Plugin)plugin, "ba_cook");
        this.KEY_VAMPIRE_SKILL = new NamespacedKey((Plugin)plugin, "vampire_skill");
        this.KEY_PULSE = new NamespacedKey((Plugin)plugin, "ba_pulse");
    }

    public void resetAll() {
        Player p;
        this.burstUsed.clear();
        this.skillCooldowns.clear();
        this.parryActive.clear();
        this.guardBroken.clear();
        this.sniperTracker.clear();
        this.sniperAimTick.clear();
        this.sniperLostTick.clear();
        this.markedForDeath.clear();
        this.maxHpReduced.clear();
        this.vampireGauge.clear();
        this.vampireBloodMode.clear();
        this.sundanceRevolver.clear();
        this.activeTraps.clear();
        this.activeMines.forEach(m -> m.entity.remove());
        this.activeMines.clear();
        this.activeTurrets.forEach(t -> t.entity.remove());
        this.activeTurrets.clear();
        this.portalA.clear();
        this.portalB.clear();
        this.portalBlocks.clear();
        this.activeRecons.forEach(r -> r.entity.remove());
        this.activeRecons.clear();
        this.activePulsers.clear();
        for (UUID uid : this.phantomEnd.keySet()) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
            p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        }
        this.phantomEnd.clear();
        this.anchorFields.clear();
        this.deadlockedPlayers.clear();
        this.guardianEndTime.clear();
        this.gliderInAir.clear();
        this.releaserMegaUsed.clear();
        this.comboCount.clear();
        this.comboLastHit.clear();
        this.sneakChargeTicks.clear();
        this.universalCharged.clear();
        this.portalCooldowns.clear();
        this.grangChargeStart.clear();
        this.grangCharging.clear();
        this.piercingRecently.clear();
        this.marksmanBoltCount.clear();
        this.grangBurstCooldown.clear();
        this.necroArmy.values().forEach(list -> list.forEach(s -> {
            if (s.isValid()) {
                s.remove();
            }
        }));
        this.necroArmy.clear();
        for (Player p2 : Bukkit.getOnlinePlayers()) {
            if (p2.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                p2.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
            }
            if (!(p2.getHealth() > 20.0)) continue;
            p2.setHealth(20.0);
        }
        for (UUID wu : new HashSet<UUID>(this.activeWalls.keySet())) {
            this.removeWall(wu);
        }
        this.timeSnapshots.clear();
        this.activeBonds.clear();
        this.frozenProjectiles.clear();
        this.activeHexFields.clear();
        this.mirrorEndTime.clear();
        if (this.antiBetrayalTask != null) {
            this.antiBetrayalTask.cancel();
            this.antiBetrayalTask = null;
        }
        for (UUID uid : new HashSet<UUID>(this.storedArmor.keySet())) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.showArmor(p);
        }
        this.storedArmor.clear();
    }

    public void resetRound() {
        Player p;
        this.burstUsed.clear();
        this.skillCooldowns.clear();
        this.parryActive.clear();
        this.guardBroken.clear();
        this.sniperAimTick.clear();
        this.sniperTracker.clear();
        this.markedForDeath.clear();
        this.sundanceRevolver.clear();
        this.activeTraps.clear();
        this.activeMines.forEach(m -> m.entity.remove());
        this.activeMines.clear();
        this.activeTurrets.forEach(t -> t.entity.remove());
        this.activeTurrets.clear();
        this.activeRecons.forEach(r -> r.entity.remove());
        this.activeRecons.clear();
        this.activePulsers.clear();
        this.guardianEndTime.clear();
        this.gliderInAir.clear();
        this.releaserMegaUsed.clear();
        this.deadlockedPlayers.clear();
        this.portalCooldowns.clear();
        this.comboCount.clear();
        this.comboLastHit.clear();
        this.sneakChargeTicks.clear();
        this.universalCharged.clear();
        this.portalA.clear();
        this.portalB.clear();
        this.portalBlocks.clear();
        this.grangChargeStart.clear();
        this.grangCharging.clear();
        this.piercingRecently.clear();
        this.marksmanBoltCount.clear();
        this.grangBurstCooldown.clear();
        this.necroArmy.values().forEach(list -> list.forEach(s -> {
            if (s.isValid()) {
                s.remove();
            }
        }));
        this.necroArmy.clear();
        this.anchorFields.clear();
        for (UUID uid : new HashSet<UUID>(this.storedArmor.keySet())) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.showArmor(p);
        }
        this.storedArmor.clear();
        for (UUID uid : new HashSet<UUID>(this.phantomEnd.keySet())) {
            p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
            p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            this.showArmor(p);
        }
        this.phantomEnd.clear();
        this.vampireGauge.clear();
        this.vampireBloodMode.clear();
        this.maxHpReduced.clear();
        for (UUID wu : new HashSet<UUID>(this.activeWalls.keySet())) {
            this.removeWall(wu);
        }
        this.timeSnapshots.clear();
        this.activeBonds.clear();
        this.frozenProjectiles.clear();
        this.activeHexFields.clear();
        this.mirrorEndTime.clear();
        for (Player p2 : Bukkit.getOnlinePlayers()) {
            p2.setInvulnerable(false);
            if (p2.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                p2.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
            }
            p2.getPersistentDataContainer().remove(new NamespacedKey((Plugin)this.plugin, "heavy_bolt"));
            p2.getPersistentDataContainer().remove(new NamespacedKey((Plugin)this.plugin, "mega_rocket"));
            p2.getPersistentDataContainer().remove(this.KEY_RECON);
            p2.getPersistentDataContainer().remove(this.KEY_PULSE);
            p2.getPersistentDataContainer().remove(this.KEY_VAMPIRE_SKILL);
        }
    }

    public void onRightClick(Player p, ItemStack held) {
        if (held == null || held.getItemMeta() == null) {
            return;
        }
        ItemMeta meta = held.getItemMeta();
        if (meta.getPersistentDataContainer().has(this.KEY_BURST, PersistentDataType.BYTE)) {
            this.useBurst(p);
            return;
        }
        if (this.gm.getPlayerKitType(p.getUniqueId()) == KitType.COOK && this.isSword(held)) {
            this.cookGenerateFood(p);
            return;
        }
        if (this.gm.getPlayerKitType(p.getUniqueId()) == KitType.LANCER && this.isSword(held)) {
            this.useKitSkill(p, KitType.LANCER.name());
            return;
        }
        if (this.gm.getPlayerKitType(p.getUniqueId()) == KitType.SUNDANCE && p.isSneaking() && held.getType() == Material.CROSSBOW) {
            this.sundanceSkill(p);
            return;
        }
        if (this.gm.getPlayerKitType(p.getUniqueId()) == KitType.COUNTER && p.isSneaking() && (held.getType() == Material.SHIELD || p.getInventory().getItemInOffHand().getType() == Material.SHIELD)) {
            this.onParryAttempt(p);
            return;
        }
        if (meta.getPersistentDataContainer().has(this.KEY_SKILL, PersistentDataType.STRING)) {
            String kitName = (String)meta.getPersistentDataContainer().get(this.KEY_SKILL, PersistentDataType.STRING);
            if ("TRANSPORTER".equals(kitName) && this.gm.getPlayerKitType(p.getUniqueId()) == KitType.TRANSPORTER) {
                this.placePortalB(p);
                return;
            }
            if ("RELEASER".equals(kitName) && p.isSneaking()) {
                this.releaserMegaBurst(p);
                return;
            }
            this.useKitSkill(p, kitName);
        }
        if (meta.getPersistentDataContainer().has(this.KEY_VAMPIRE_SKILL, PersistentDataType.STRING) && this.gm.getPlayerKitType(p.getUniqueId()) == KitType.VAMPIRE) {
            this.theosPadaAction(p, true);
        }
    }

    public void onClick(Player p, ItemStack held, boolean isLeft) {
        if (!isLeft || held == null || held.getItemMeta() == null) {
            return;
        }
        ItemMeta meta = held.getItemMeta();
        if (meta.getPersistentDataContainer().has(this.KEY_SKILL, PersistentDataType.STRING)) {
            String kitName = (String)meta.getPersistentDataContainer().get(this.KEY_SKILL, PersistentDataType.STRING);
            if ("TRANSPORTER".equals(kitName) && this.gm.getPlayerKitType(p.getUniqueId()) == KitType.TRANSPORTER) {
                this.placePortalA(p);
            }
            if ("WHIRLWIND".equals(kitName) && this.gm.getPlayerKitType(p.getUniqueId()) == KitType.WHIRLWIND) {
                this.whirlwindBall(p);
            }
            if ("COOK".equals(kitName) && this.gm.getPlayerKitType(p.getUniqueId()) == KitType.COOK) {
                this.cookThrowFood(p);
            }
            if ("TIMEKEEPER".equals(kitName) && this.gm.getPlayerKitType(p.getUniqueId()) == KitType.TIMEKEEPER) {
                this.timekeeperClockStop(p);
            }
        }
        if (meta.getPersistentDataContainer().has(this.KEY_VAMPIRE_SKILL, PersistentDataType.STRING) && this.gm.getPlayerKitType(p.getUniqueId()) == KitType.VAMPIRE && p.isSneaking()) {
            this.theosPadaAction(p, false);
        }
    }

    public void onSniperAimTick(Player p) {
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.SNIPER) {
            return;
        }
        if (p.getInventory().getItemInMainHand().getType() != Material.CROSSBOW) {
            this.sniperAimTick.remove(p.getUniqueId());
            this.sniperTracker.remove(p.getUniqueId());
            return;
        }
        if (!p.isOnGround() || !p.isSneaking()) {
            this.sniperAimTick.remove(p.getUniqueId());
            this.sniperTracker.remove(p.getUniqueId());
            this.sniperLostTick.remove(p.getUniqueId());
            return;
        }
        this.sniperLostTick.remove(p.getUniqueId());
        Player target = this.getTargetInSight(p, 30);
        UUID oldTarget = this.sniperTracker.get(p.getUniqueId());
        if (target == null) {
            if (oldTarget != null) {
                p.sendMessage("\u00a77\u72d9\u6483\u3092\u4e2d\u6b62\u3057\u307e\u3057\u305f");
            }
            this.sniperAimTick.remove(p.getUniqueId());
            this.sniperTracker.remove(p.getUniqueId());
            this.sniperLostTick.remove(p.getUniqueId());
            return;
        }
        this.sniperLostTick.remove(p.getUniqueId());
        UUID prevTarget = this.sniperTracker.get(p.getUniqueId());
        if (prevTarget == null || !prevTarget.equals(target.getUniqueId())) {
            this.sniperTracker.put(p.getUniqueId(), target.getUniqueId());
            this.sniperAimTick.put(p.getUniqueId(), 1);
            if (prevTarget == null) {
                p.sendMessage("\u00a77\u72d9\u6483\u773c: \u00a7f" + target.getName() + " \u00a77\u3092\u7167\u6e96\u4e2d...");
            }
            return;
        }
        int ticks = this.sniperAimTick.merge(p.getUniqueId(), 1, Integer::sum);
        if (ticks >= 7) {
            this.markedForDeath.add(target.getUniqueId());
            this.setCooldown(p.getUniqueId(), 7000L);
            p.sendMessage("\u00a7c\u00a7l\u72d9\u6483\u773c: \u00a7f" + target.getName() + " \u00a7c\u3092\u30de\u30fc\u30af\u3057\u307e\u3057\u305f\uff01\u6b21\u306e\u4e00\u6483\u3067\u5373\u6b7b\uff01");
            target.sendMessage("\u00a7c\u00a7l\u26a0 \u72d9\u6483\u3055\u308c\u3066\u3044\u307e\u3059\uff01");
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 0.5f);
            this.sniperAimTick.remove(p.getUniqueId());
            this.sniperTracker.remove(p.getUniqueId());
        }
    }

    public boolean onSniperHit(Player shooter, Player victim) {
        if (this.markedForDeath.contains(victim.getUniqueId()) && this.gm.getPlayerKitType(shooter.getUniqueId()) == KitType.SNIPER) {
            victim.setHealth(0.0);
            this.markedForDeath.remove(victim.getUniqueId());
            shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 2.0f);
            return true;
        }
        return false;
    }

    public void onParryAttempt(final Player p) {
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.COUNTER) {
            return;
        }
        if (!p.isSneaking()) {
            return;
        }
        if (this.isOnCooldown(p.getUniqueId())) {
            return;
        }
        if (this.parryActive.contains(p.getUniqueId())) {
            return;
        }
        this.parryActive.add(p.getUniqueId());
        p.sendActionBar((Component)Component.text((String)"\u00a79\ud83d\udee1 \u30d1\u30ea\u30a3\u53d7\u4ed8\u4e2d\uff012\u79d2"));
        new BukkitRunnable(){

            public void run() {
                SkillManager.this.parryActive.remove(p.getUniqueId());
                if (p.isOnline()) {
                    p.sendActionBar((Component)Component.text((String)"\u00a77\u30d1\u30ea\u30a3\u7d42\u4e86"));
                }
            }
        }.runTaskLater((Plugin)this.plugin, 40L);
    }

    public void tryUniversalGuardBreak(Player attacker, Player victim) {
        if (this.universalCharged.remove(attacker.getUniqueId()) == null) {
            return;
        }
        this.sneakChargeTicks.remove(attacker.getUniqueId());
        this.guardBroken.add(victim.getUniqueId());
        this.triggerGrangBurst(victim);
        victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.1);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.guardBroken.remove(victim.getUniqueId()), 60L);
        victim.getWorld().playSound(victim.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 0.8f);
        victim.sendMessage("\u00a7c\u00a7l\ud83d\udee1 \u30ac\u30fc\u30c9\u30d6\u30ec\u30a4\u30af\uff01\u00a77\u76f8\u624b\u306e\u6e9c\u3081\u653b\u6483\u30673\u79d2\u9593\u9632\u5177\u7121\u52b9");
        attacker.sendMessage("\u00a7e\u00a7l\u26a1 \u30ac\u30fc\u30c9\u30d6\u30ec\u30a4\u30af\u6210\u529f\uff01");
        attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.5f);
        this.plugin.getTutorialManager().checkGuardBreakUsed(attacker);
    }

    public void triggerGrangBurst(Player p) {
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.GRANG) {
            return;
        }
        if (!this.grangCharging.getOrDefault(p.getUniqueId(), false).booleanValue()) {
            return;
        }
        Long cd = this.grangBurstCooldown.get(p.getUniqueId());
        if (cd != null && System.currentTimeMillis() < cd) {
            return;
        }
        this.grangBurstCooldown.put(p.getUniqueId(), System.currentTimeMillis() + 15000L);
        this.grangChargeStart.remove(p.getUniqueId());
        this.grangCharging.put(p.getUniqueId(), false);
        World w = p.getWorld();
        w.createExplosion(p.getLocation(), 2.0f, false, false, (Entity)p);
        for (Entity e : w.getNearbyEntities(p.getLocation(), 2.0, 2.0, 2.0)) {
            Player target;
            if (!(e instanceof Player) || !this.gm.isParticipant(target = (Player)e) || this.gm.getTeamOf(target) == this.gm.getTeamOf(p)) continue;
            target.damage(5.0, (Entity)p);
        }
        p.sendMessage("\u00a77\u00a7l\u30b0\u30e9\u30f3\u30b0\u30d0\u30fc\u30b9\u30c8\uff01");
    }

    public boolean tryParryCounter(Player attacker, Player counter) {
        if (!this.parryActive.contains(counter.getUniqueId())) {
            return false;
        }
        if (this.guardBroken.contains(counter.getUniqueId())) {
            counter.sendMessage("\u00a7c\u30ac\u30fc\u30c9\u30d6\u30ec\u30a4\u30af\u4e2d\uff01");
            return false;
        }
        if (!counter.isBlocking()) {
            return false;
        }
        Vector dirToAttacker = attacker.getLocation().toVector().subtract(counter.getLocation().toVector()).setY(0).normalize();
        Vector facing = counter.getLocation().getDirection().setY(0).normalize();
        if (facing.lengthSquared() == 0.0 || dirToAttacker.lengthSquared() == 0.0) {
            return false;
        }
        if (dirToAttacker.dot(facing) < 0.4) {
            return false;
        }
        this.parryActive.remove(counter.getUniqueId());
        this.setCooldown(counter.getUniqueId(), 10000L);
        this.skillCooldowns.put(counter.getUniqueId(), System.currentTimeMillis() + 8000L);
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, 9, false, true));
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2, false, true));
        counter.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 0, false, true));
        attacker.sendMessage("\u00a7c\u00a7l\u92fc\u306e\u53cd\u5c04\uff01\u884c\u52d5\u4e0d\u80fd\uff01");
        counter.sendMessage("\u00a79\u00a7l\u30d1\u30ea\u30a3\u6210\u529f\uff01");
        counter.getWorld().playSound(counter.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.5f, 1.5f);
        counter.getWorld().spawnParticle(Particle.CRIT_MAGIC, counter.getLocation().add(0.0, 1.0, 0.0), 40, 0.5, 0.5, 0.5, 0.3);
        return true;
    }

    public void onGrangSneak(final Player p, boolean sneaking) {
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.GRANG) {
            return;
        }
        if (!p.isBlocking()) {
            return;
        }
        if (this.isOnCooldown(p.getUniqueId())) {
            if (sneaking) {
                p.sendMessage("\u00a7c\u30af\u30fc\u30eb\u30bf\u30a4\u30e0\u4e2d\uff01");
            }
            return;
        }
        if (sneaking) {
            if (!this.grangCharging.getOrDefault(p.getUniqueId(), false).booleanValue()) {
                this.grangChargeStart.put(p.getUniqueId(), System.currentTimeMillis());
                this.grangCharging.put(p.getUniqueId(), true);
                p.sendMessage("\u00a77\u00a7l\u30c1\u30e3\u30fc\u30b8\u958b\u59cb...");
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 0.3f, 0.5f);
            }
            long elapsed = System.currentTimeMillis() - this.grangChargeStart.getOrDefault(p.getUniqueId(), System.currentTimeMillis());
            int pct = (int)Math.min(100L, elapsed * 100L / 7000L);
            p.sendActionBar((Component)Component.text((String)("\u00a77\ud83d\udee1 \u30c1\u30e3\u30fc\u30b8 \u00a7e" + pct + "%")));
        } else {
            Long start = this.grangChargeStart.remove(p.getUniqueId());
            this.grangCharging.put(p.getUniqueId(), false);
            if (start == null) {
                return;
            }
            long chargeMs = Math.min(System.currentTimeMillis() - start, 7000L);
            double power = (double)chargeMs / 7000.0;
            double distance = 3.0 + power * 10.0;
            int cd = (int)(5000.0 + power * 8000.0);
            this.setCooldown(p.getUniqueId(), cd);
            Vector dir = p.getLocation().getDirection().normalize().multiply(distance * 0.15).setY(0.2);
            p.setVelocity(dir);
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.8f + (float)power * 0.4f);
            if (power >= 0.95) {
                UUID uid = p.getUniqueId();
                p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 4, false, false));
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    Player pl = Bukkit.getPlayer((UUID)uid);
                    if (pl != null && pl.isOnline()) {
                        pl.getWorld().createExplosion(pl.getLocation(), 3.0f, false, false, (Entity)pl);
                        pl.sendMessage("\u00a77\u00a7l\u6700\u5927\u30c1\u30e3\u30fc\u30b8\u70b8\u88c2\uff01");
                    }
                }, 10L);
            }
            World w = p.getWorld();
            new BukkitRunnable(){
                int t = 0;

                public void run() {
                    if (this.t++ > 20 || !p.isOnline()) {
                        this.cancel();
                        return;
                    }
                    for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 2.0, 2.0, 2.0)) {
                        Player target;
                        if (!(e instanceof Player) || !SkillManager.this.gm.isParticipant(target = (Player)e) || SkillManager.this.gm.getTeamOf(target) == SkillManager.this.gm.getTeamOf(p)) continue;
                        target.damage(7.0, (Entity)p);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 9, false, false));
                        target.setVelocity(p.getLocation().getDirection().normalize().multiply(0.5).setY(0.3));
                    }
                }
            }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
        }
    }

    public void onVampireDamageDealt(Player p, double dmg) {
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.VAMPIRE) {
            return;
        }
        if (this.vampireBloodMode.getOrDefault(p.getUniqueId(), false).booleanValue()) {
            return;
        }
        double g = this.vampireGauge.getOrDefault(p.getUniqueId(), 0.0) + dmg;
        this.vampireGauge.put(p.getUniqueId(), Math.min(g, 80.0));
        this.updateVampireStage(p, g);
    }

    public void onVampireDamaged(Player p, double dmg) {
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.VAMPIRE) {
            return;
        }
        if (!this.vampireBloodMode.getOrDefault(p.getUniqueId(), false).booleanValue()) {
            return;
        }
        double g = this.vampireGauge.getOrDefault(p.getUniqueId(), 0.0) - dmg;
        if (g <= 0.0) {
            this.vampireBloodMode.put(p.getUniqueId(), false);
            this.vampireGauge.put(p.getUniqueId(), 0.0);
            this.applyVampireStage(p, 0);
            return;
        }
        this.vampireGauge.put(p.getUniqueId(), g);
    }

    public void onVampireAttack(Player p) {
    }

    public void onCrossbowShoot(final Player p) {
        CrossbowMeta cm;
        ItemMeta itemMeta;
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.SUNDANCE) {
            return;
        }
        ItemStack xbow = p.getInventory().getItemInMainHand();
        if (xbow != null && xbow.getType() == Material.CROSSBOW && (itemMeta = xbow.getItemMeta()) instanceof CrossbowMeta && !(cm = (CrossbowMeta)itemMeta).hasChargedProjectiles()) {
            return;
        }
        UUID uid = p.getUniqueId();
        int remaining = this.sundanceRevolver.getOrDefault(uid, 0);
        if (remaining > 0) {
            this.sundanceRevolver.put(uid, --remaining);
            if (remaining <= 0) {
                this.sundanceRevolver.remove(uid);
                Long curCd = this.skillCooldowns.get(uid);
                long baseCd = curCd != null && curCd > System.currentTimeMillis() ? curCd : System.currentTimeMillis();
                this.skillCooldowns.put(uid, baseCd + 9000L);
                p.sendMessage("\u00a7b\u30ea\u30dc\u30eb\u30d3\u30f3\u30b0\u7d42\u4e86\uff01CT 9\u79d2");
            } else {
                new BukkitRunnable(){

                    public void run() {
                        CrossbowMeta cm2;
                        ItemMeta itemMeta;
                        ItemStack xb;
                        if (p.isOnline() && (xb = p.getInventory().getItemInMainHand()) != null && xb.getType() == Material.CROSSBOW && (itemMeta = xb.getItemMeta()) instanceof CrossbowMeta && !(cm2 = (CrossbowMeta)itemMeta).hasChargedProjectiles()) {
                            cm2.addChargedProjectile(new ItemStack(Material.ARROW));
                            xb.setItemMeta((ItemMeta)cm2);
                        }
                    }
                }.runTaskLater((Plugin)this.plugin, 1L);
            }
        }
    }

    public void onPortalWalk(Player p, Location loc) {
        this.checkPortalTeleport(p, loc);
    }

    public void onFlashBangLand(Location loc, Player shooter) {
        loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0.0, 0.0, 0.0, 0.0);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2.0f);
        for (Player t : loc.getWorld().getPlayers()) {
            if (!(t.getLocation().distance(loc) <= 6.0) || !this.gm.isParticipant(t)) continue;
            if (this.gm.getTeamOf(t) == this.gm.getTeamOf(shooter)) {
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 2, false, false));
                continue;
            }
            t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 2, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false));
            t.getWorld().spawnParticle(Particle.SPELL_MOB, t.getLocation().add(0.0, 2.2, 0.0), 5, 0.2, 0.2, 0.2, 0.0);
        }
    }

    public void onGrappleHit(Snowball ball, Player victim, Player shooter) {
        if (this.gm.getTeamOf(victim) == this.gm.getTeamOf(shooter)) {
            return;
        }
        victim.damage(2.0, (Entity)shooter);
        victim.setVelocity(new Vector(0.0, 4.0, 0.0));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (victim.isOnline()) {
                Vector pull = shooter.getEyeLocation().toVector().subtract(victim.getLocation().toVector());
                double dist = pull.length();
                pull.normalize().multiply(Math.min(2.5, dist * 0.08));
                victim.setVelocity(pull);
            }
        }, 2L);
        shooter.sendMessage("\u00a73\u30b0\u30e9\u30c3\u30d7\u30eb\u30d2\u30c3\u30c8\uff01\u8ddd\u96e2: \u00a7f" + String.format("%.1f", victim.getLocation().distance(shooter.getLocation())) + "m");
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, 9, false, false));
    }

    public void onMegaRocketHit(Location loc, Player shooter, Snowball rocket) {
        float power = this.rocketHitPower(rocket, loc, 2.0f);
        loc.getWorld().createExplosion(loc, 0.0f, false, false, (Entity)shooter);
        for (Entity e2 : loc.getWorld().getNearbyEntities(loc, 6.0, 4.0, 6.0)) {
            Player t;
            if (!(e2 instanceof Player) || !this.gm.isParticipant(t = (Player)e2) || this.gm.getTeamOf(t) == this.gm.getTeamOf(shooter)) continue;
            t.damage((double)(power * 8.0f), (Entity)shooter);
            t.setVelocity(t.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(2.0).setY(0.5));
            t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, true));
        }
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
    }

    public float rocketHitPower(Snowball rocket, Location explodeLoc, float maxPower) {
        Double ox = (Double)rocket.getPersistentDataContainer().get(new NamespacedKey((Plugin)this.plugin, "rocket_origin_x"), PersistentDataType.DOUBLE);
        Double oy = (Double)rocket.getPersistentDataContainer().get(new NamespacedKey((Plugin)this.plugin, "rocket_origin_y"), PersistentDataType.DOUBLE);
        Double oz = (Double)rocket.getPersistentDataContainer().get(new NamespacedKey((Plugin)this.plugin, "rocket_origin_z"), PersistentDataType.DOUBLE);
        if (ox == null || oy == null || oz == null) {
            return maxPower;
        }
        double dist = new Location(explodeLoc.getWorld(), ox.doubleValue(), oy.doubleValue(), oz.doubleValue()).distance(explodeLoc);
        float scale = (float)Math.min(1.0, dist / 6.0);
        return maxPower * Math.max(0.15f, scale);
    }

    public void onTheosPadaHit(Snowball ball, Player shooter, Entity hit) {
        Player t;
        if (hit instanceof Player && this.gm.isParticipant(t = (Player)hit) && this.gm.getTeamOf(t) != this.gm.getTeamOf(shooter)) {
            double heal = Math.min(t.getHealth(), t.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            double g = this.vampireGauge.getOrDefault(shooter.getUniqueId(), 0.0) + heal;
            this.vampireGauge.put(shooter.getUniqueId(), Math.min(g, 80.0));
            this.updateVampireStage(shooter, g);
            shooter.sendMessage("\u00a74\u00a7l\u30c6\u30aa\u30b9\u30d1\u30fc\u30c0\u5438\u53ce\uff01\u00a77\u30b2\u30fc\u30b8+" + String.format("%.0f", heal));
        }
    }

    public void marksmanHeavyBoltHit(Player shooter, Player victim) {
        if (!shooter.getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "heavy_bolt"), PersistentDataType.BYTE)) {
            return;
        }
        shooter.getPersistentDataContainer().remove(new NamespacedKey((Plugin)this.plugin, "heavy_bolt"));
        victim.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "hp_reduced"), PersistentDataType.BYTE, Byte.valueOf((byte)1));
        if (victim.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            double currentMax = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            this.maxHpReduced.putIfAbsent(victim.getUniqueId(), currentMax);
            int applied = this.marksmanBoltCount.merge(victim.getUniqueId(), 3, Integer::sum);
            applied = Math.min(applied, 12);
            this.marksmanBoltCount.put(victim.getUniqueId(), applied);
            double original = this.maxHpReduced.get(victim.getUniqueId()).doubleValue();
            double newMax = Math.max(1.0, original - (double)applied);
            victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMax);
            if (victim.getHealth() > newMax) {
                victim.setHealth(newMax);
            }
        }
        victim.sendMessage("\u00a7c\u00a7lHP\u4e0a\u9650\u304c\u4f4e\u4e0b\u3057\u307e\u3057\u305f\uff01\uff08\u30e9\u30a6\u30f3\u30c9\u7d42\u4e86\u307e\u3067\uff09");
    }

    public void restoreMaxHp(Player p) {
        if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            double current = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            if (this.maxHpReduced.containsKey(p.getUniqueId())) {
                p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.maxHpReduced.remove(p.getUniqueId()).doubleValue());
            } else if (current != 20.0) {
                p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
            }
        }
    }

    public void scoutReconShot(Player p, Location hitLoc) {
        if (!p.getPersistentDataContainer().has(this.KEY_RECON, PersistentDataType.BYTE)) {
            return;
        }
        p.getPersistentDataContainer().remove(this.KEY_RECON);
        hitLoc.getWorld().playSound(hitLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);
        ArmorStand as = (ArmorStand)hitLoc.getWorld().spawn(hitLoc, ArmorStand.class, a -> {
            a.setInvisible(true);
            a.setMarker(true);
            a.setCustomName("\u00a7a\ud83d\udd0d \u30ea\u30b3\u30f3");
            a.setCustomNameVisible(true);
        });
        this.activeRecons.add(new ReconData(as, this.gm.getTeamOf(p)));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            as.remove();
            this.activeRecons.removeIf(r -> r.entity.equals((Object)as));
        }, 600L);
        p.sendMessage("\u00a7a\u30ea\u30b3\u30f3\u8a2d\u7f6e\uff0130\u79d2\u9593\u7d22\u6575");
        this.scoutPulseOnRecon(p, hitLoc);
    }

    private void scoutPulseOnRecon(Player owner, Location pulseLoc) {
        for (Player p : pulseLoc.getWorld().getPlayers()) {
            if (!this.gm.isParticipant(p) || this.gm.getTeamOf(p) == this.gm.getTeamOf(owner) || p.getLocation().distance(pulseLoc) > 14.0) continue;
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1, false, false));
            p.damage(5.0, (Entity)owner);
        }
    }

    public void scoutPulseShot(Player p, Location hitLoc) {
        if (!p.getPersistentDataContainer().has(this.KEY_PULSE, PersistentDataType.BYTE)) {
            return;
        }
        p.getPersistentDataContainer().remove(this.KEY_PULSE);
        this.activePulsers.add(new PulseData(hitLoc.clone(), this.gm.getTeamOf(p)));
        p.sendMessage("\u00a7c\u30d1\u30eb\u30b5\u30fc\u8a2d\u7f6e\uff0130\u79d2\u7bc4\u56f2\u30c0\u30e1\u30fc\u30b8");
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.activePulsers.removeIf(pd -> pd.loc.equals((Object)hitLoc)), 600L);
    }

    public void onDecoyHit(Entity decoy, Player attacker) {
        if (!decoy.getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "decoy"), PersistentDataType.BYTE)) {
            return;
        }
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, false, false));
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1, false, false));
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 1, false, false));
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false));
        attacker.sendMessage("\u00a78\u00a7l\u7f60\u3060\uff01\u00a77\u56ee\u3092\u653b\u6483\u3057\u3066\u5f31\u4f53\u5316\uff0b\u767a\u5149\u3057\u305f\uff01");
    }

    public void onPhantomDamaged(Player p) {
        if (!this.phantomEnd.containsKey(p.getUniqueId())) {
            return;
        }
        Long endTime = this.phantomEnd.get(p.getUniqueId());
        if (endTime != null && System.currentTimeMillis() < endTime) {
            this.phantomEnd.remove(p.getUniqueId());
            p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
            p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            this.showArmor(p);
            p.sendMessage("\u00a7c\u970a\u4f53\u5316\u89e3\u9664\uff01");
        }
    }

    public void checkPortalTeleport(Player p, Location to) {
        Location lastPortal = this.portalLastUsed.get(p.getUniqueId());
        if (lastPortal != null && to.distance(lastPortal) < 2.5) {
            return;
        }
        this.portalLastUsed.remove(p.getUniqueId());
        for (UUID uid : this.portalA.keySet()) {
            Location a = this.portalA.get(uid);
            Location b = this.portalB.get(uid);
            if (a == null || b == null) continue;
            if (to.distance(a) < 1.5) {
                if (this.gm.isFlagCarrier(p.getUniqueId())) {
                    p.sendMessage("\u00a7c\u65d7\u3092\u6301\u3063\u3066\u3044\u308b\u9593\u306f\u30dd\u30fc\u30bf\u30eb\u3092\u4f7f\u7528\u3067\u304d\u307e\u305b\u3093\uff01");
                    return;
                }
                p.teleport(b.clone().add(0.0, 1.0, 0.0));
                this.portalLastUsed.put(p.getUniqueId(), b.clone());
                p.getWorld().playSound(b, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                if (this.gm.getPlayerKitType(p.getUniqueId()) == KitType.TRANSPORTER && uid.equals(p.getUniqueId())) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, false, false));
                }
                return;
            }
            if (!(to.distance(b) < 1.5)) continue;
            if (this.gm.isFlagCarrier(p.getUniqueId())) {
                p.sendMessage("\u00a7c\u65d7\u3092\u6301\u3063\u3066\u3044\u308b\u9593\u306f\u30dd\u30fc\u30bf\u30eb\u3092\u4f7f\u7528\u3067\u304d\u307e\u305b\u3093\uff01");
                return;
            }
            p.teleport(a.clone().add(0.0, 1.0, 0.0));
            this.portalLastUsed.put(p.getUniqueId(), a.clone());
            p.getWorld().playSound(a, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            if (this.gm.getPlayerKitType(p.getUniqueId()) == KitType.TRANSPORTER && uid.equals(p.getUniqueId())) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, false, false));
            }
            return;
        }
    }

    public boolean isGuardBroken(UUID uid) {
        return this.guardBroken.contains(uid);
    }

    public boolean tryMarkPiercing(UUID uid) {
        if (this.piercingRecently.contains(uid)) {
            return false;
        }
        this.piercingRecently.add(uid);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.piercingRecently.remove(uid), 20L);
        return true;
    }

    public boolean isVampireBloodMode(UUID uid) {
        return this.vampireBloodMode.getOrDefault(uid, false);
    }

    public double getVampireGauge(UUID uid) {
        return this.vampireGauge.getOrDefault(uid, 0.0);
    }

    public boolean isDeadlocked(UUID uid) {
        return this.deadlockedPlayers.contains(uid);
    }

    public void initVampireDebuffs(Player p) {
        if (this.gm.getPlayerKitType(p.getUniqueId()) == KitType.VAMPIRE) {
            this.applyVampireStage(p, 0);
        }
    }

    public void cookHit(Location loc, Player thrower, Material mat) {
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 1, 0.0, 0.0, 0.0, 0.0);
        for (Entity e : loc.getWorld().getNearbyEntities(loc, 2.0, 2.0, 2.0)) {
            Player t;
            if (!(e instanceof Player) || !this.gm.isParticipant(t = (Player)e) || t == thrower) continue;
            this.applyCookBuff(t, mat);
            t.sendMessage(this.gm.getTeamOf(t) == this.gm.getTeamOf(thrower) ? "\u00a7a\u5473\u65b9\u304b\u3089\u6599\u7406\u304c\u547d\u4e2d\uff01" : "\u00a7c\u6599\u7406\u304c\u547d\u4e2d\uff01");
        }
    }

    public void refreshBurst(Player p) {
        this.burstUsed.remove(p.getUniqueId());
        this.releaserMegaUsed.remove(p.getUniqueId());
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.RELEASER) {
            p.getInventory().addItem(new ItemStack[]{KitBuilder.makeBurstItem()});
        }
    }

    public void updateTurrets() {
        for (TurretData t : new ArrayList<TurretData>(this.activeTurrets)) {
            if (!t.entity.isValid()) {
                this.activeTurrets.remove(t);
                continue;
            }
            Location loc = t.entity.getLocation();
            Player nearest = null;
            double nearestDist = 225.0;
            for (Player target : loc.getWorld().getPlayers()) {
                double dist;
                if (!this.gm.isParticipant(target) || this.gm.isSpectator(target) || this.gm.getTeamOf(target) == t.ownerTeam || (dist = target.getLocation().distanceSquared(loc)) > 225.0 || !(dist < nearestDist)) continue;
                nearest = target;
                nearestDist = dist;
            }
            if (nearest == null) continue;
            t.entity.setTarget(nearest);
        }
    }

    public void refreshReleaserMega(Player p) {
        this.releaserMegaUsed.remove(p.getUniqueId());
    }

    public void update() {
        Location loc;
        ++this.tickCounter;
        if (this.antiBetrayalTask == null) {
            this.antiBetrayalTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, this::forceTargetEnemies, 0L, 1L);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.gm.getPlayerKitType(player.getUniqueId()) != KitType.SNIPER || !this.gm.isParticipant(player)) continue;
            this.onSniperAimTick(player);
        }
        for (UUID uUID : new HashSet<UUID>(this.gliderInAir)) {
            Player p = Bukkit.getPlayer((UUID)uUID);
            if (p == null || !this.gm.isParticipant(p)) {
                this.gliderInAir.remove(uUID);
                continue;
            }
            if (!p.isOnGround()) continue;
            this.gliderInAir.remove(uUID);
            this.setCooldown(uUID, 12000L);
            p.sendMessage("\u00a7a\u7740\u5730\uff01\u30af\u30fc\u30eb\u30bf\u30a4\u30e0\u958b\u59cb\uff0812\u79d2\uff09");
        }
        for (ReconData reconData : new ArrayList<ReconData>(this.activeRecons)) {
            if (!reconData.entity.isValid()) {
                this.activeRecons.remove(reconData);
                continue;
            }
            Location reconLoc = reconData.entity.getLocation();
            if (this.tickCounter % 3 == 0) {
                reconLoc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, reconLoc.clone().add(0.0, 0.3, 0.0), 1, 0.1, 0.05, 0.1, 0.0);
            }
            for (Player p : reconLoc.getWorld().getPlayers()) {
                if (!this.gm.isParticipant(p) || this.gm.getTeamOf(p) == reconData.ownerTeam) continue;
                double dist = p.getLocation().distance(reconLoc);
                boolean inLos14 = dist <= 14.0 && this.hasLineOfSight(reconLoc.clone().add(0.0, 1.0, 0.0), p.getEyeLocation());
                boolean inClose5 = dist <= 5.0;
                boolean invisible = p.hasPotionEffect(PotionEffectType.INVISIBILITY);
                if (invisible) {
                    if (!inLos14 || !inClose5) continue;
                    p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 0, false, false));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1, false, false));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, 1, false, false));
                    continue;
                }
                if (inLos14) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 0, false, false));
                    for (MineData mine : this.activeMines) {
                        if (mine.entity.getLocation().getWorld() != reconLoc.getWorld() || !(mine.entity.getLocation().distance(reconLoc) <= 14.0)) continue;
                        mine.entity.getLocation().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, mine.entity.getLocation().clone().add(0.0, 0.3, 0.0), 3, 0.1, 0.1, 0.1, 0.0);
                    }
                    for (TrapData trap : this.activeTraps) {
                        if (trap.loc.getWorld() != reconLoc.getWorld() || !(trap.loc.distance(reconLoc) <= 14.0)) continue;
                        trap.loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, trap.loc.clone().add(0.0, 0.3, 0.0), 3, 0.1, 0.1, 0.1, 0.0);
                    }
                }
                if (inClose5) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 0, false, false));
                }
                if (!inLos14 || !inClose5) continue;
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, 1, false, false));
            }
        }
        for (PulseData pulseData : new ArrayList<PulseData>(this.activePulsers)) {
            loc = pulseData.loc;
            if (loc.getWorld() == null) {
                this.activePulsers.remove(pulseData);
                continue;
            }
            loc.getWorld().spawnParticle(Particle.WAX_OFF, loc.clone().add(0.5, 1.0, 0.5), 8, 2.5, 1.0, 2.5, 0.0);
            for (Player p : loc.getWorld().getPlayers()) {
                if (!(p.getLocation().distance(loc) <= 5.0) || !this.gm.isParticipant(p) || this.gm.getTeamOf(p) == pulseData.ownerTeam) continue;
                p.damage(0.5);
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false));
            }
        }
        for (TrapData trapData : this.activeTraps) {
            if (trapData.loc.getWorld() == null || this.tickCounter % 3 != 0) continue;
            trapData.loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, trapData.loc.clone().add(0.0, 0.15, 0.0), 1, 0.05, 0.02, 0.05, 0.0);
        }
        for (MineData mineData : this.activeMines) {
            if (mineData.entity.getWorld() == null || this.tickCounter % 3 != 0) continue;
            mineData.entity.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, mineData.entity.getLocation().clone().add(0.0, 0.15, 0.0), 1, 0.05, 0.02, 0.05, 0.0);
        }
        for (Map.Entry entry : this.anchorFields.entrySet()) {
            loc = (Location)entry.getValue();
            if (loc == null || loc.getWorld() == null) continue;
            for (Player p : loc.getWorld().getPlayers()) {
                if (!(p.getLocation().distance(loc) <= 8.0) || !this.gm.isParticipant(p) || this.gm.isSpectator(p) || this.gm.getTeamOf(p) == this.gm.getTeam((UUID)entry.getKey())) continue;
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 4, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 0, false, true));
                if (!(p.getVelocity().getY() > 0.0)) continue;
                p.setVelocity(p.getVelocity().setY(-0.1));
            }
        }
        for (Map.Entry entry : this.vampireBloodMode.entrySet()) {
            if (!((Boolean)entry.getValue()).booleanValue()) continue;
            UUID uid = (UUID)entry.getKey();
            double gauge = this.vampireGauge.getOrDefault(uid, 0.0) - 0.3;
            if (gauge <= 0.0) {
                this.vampireBloodMode.put(uid, false);
                this.applyVampireStage(Bukkit.getPlayer((UUID)uid), 0);
                continue;
            }
            this.vampireGauge.put(uid, gauge);
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.updateVampireStage(p, gauge);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!this.gm.isParticipant(player) || this.gm.isSpectator(player)) continue;
            if (this.guardBroken.contains(player.getUniqueId())) {
                this.sneakChargeTicks.remove(player.getUniqueId());
                this.universalCharged.remove(player.getUniqueId());
                continue;
            }
            if (player.isSneaking()) {
                int ticks = this.sneakChargeTicks.merge(player.getUniqueId(), 1, Integer::sum);
                if (ticks >= 1 && ticks < 2) {
                    this.universalCharged.put(player.getUniqueId(), true);
                    player.sendMessage("\u00a7e\u00a7l\u26a1 \u30ac\u30fc\u30c9\u30d6\u30ec\u30a4\u30af\u30c1\u30e3\u30fc\u30b8\u5b8c\u4e86\uff01\u6b21\u306e\u653b\u6483\u3067\u76f8\u624b\u306e\u76fe\u3092\u7834\u58ca");
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.5f, 1.5f);
                    continue;
                }
                if (ticks >= 1 || ticks % 5 != 0) continue;
                player.sendActionBar((Component)Component.text((String)("\u00a7e\u26a1 \u30c1\u30e3\u30fc\u30b8\u4e2d... \u00a77" + (1 - ticks) + "/1\u79d2")));
                continue;
            }
            Integer prev = this.sneakChargeTicks.remove(player.getUniqueId());
            if (prev == null || prev >= 1) continue;
            this.universalCharged.remove(player.getUniqueId());
        }
        this.updateKitActionBars();
        long now = System.currentTimeMillis();
        new ArrayList<Map.Entry<UUID, Long>>(this.comboLastHit.entrySet()).forEach(e2 -> {
            if (now - e2.getValue() > 3000L) {
                this.comboCount.remove(e2.getKey());
                this.comboLastHit.remove(e2.getKey());
            }
        });
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!this.gm.isParticipant(player) || this.gm.getPlayerKitType(player.getUniqueId()) != KitType.TIMEKEEPER) {
                continue;
            }
            this.timeSnapshots.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<Snapshot>()).addFirst(new Snapshot(player.getLocation().clone(), player.getHealth()));
            Deque<Snapshot> snaps = this.timeSnapshots.get(player.getUniqueId());
            while (snaps.size() > 5) {
                snaps.removeLast();
            }
        }
        for (Entity e : new ArrayList<Entity>(this.frozenProjectiles)) {
            if (!e.isValid()) {
                this.frozenProjectiles.remove(e);
                continue;
            }
            e.setVelocity(new Vector(0.0, 0.0, 0.0));
            e.getWorld().spawnParticle(Particle.CRIT_MAGIC, e.getLocation(), 2, 0.1, 0.1, 0.1, 0.0);
        }
        for (Map.Entry<UUID, Bond> entry : new HashMap<UUID, Bond>(this.activeBonds).entrySet()) {
            UUID allyUid = entry.getKey();
            Bond bond = entry.getValue();
            if (System.currentTimeMillis() > bond.endTime) {
                this.activeBonds.remove(allyUid);
                continue;
            }
            Player ally = Bukkit.getPlayer(allyUid);
            Player owner = Bukkit.getPlayer(bond.ownerUid);
            if (ally == null || owner == null || !ally.isOnline() || !owner.isOnline() || owner.getWorld() != ally.getWorld() || owner.getLocation().distance(ally.getLocation()) > 15.0) {
                this.activeBonds.remove(allyUid);
                continue;
            }
            Location a = owner.getEyeLocation();
            Location b = ally.getEyeLocation();
            for (double t = 0.0; t <= 1.0; t += 0.1) {
                Location pl = a.clone().add(b.toVector().subtract(a.toVector()).multiply(t));
                owner.getWorld().spawnParticle(Particle.REDSTONE, pl, 1, 0.0, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.RED, 1.0f));
            }
        }
        this.activeHexFields.removeIf(f -> System.currentTimeMillis() > f.endTime);
        for (Player sm : Bukkit.getOnlinePlayers()) {
            if (!this.gm.isParticipant(sm) || this.gm.isSpectator(sm) || this.gm.getPlayerKitType(sm.getUniqueId()) != KitType.SUPERIOR_MISTRAL) {
                continue;
            }
            if (sm.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null && sm.getHealth() < sm.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) {
                sm.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 0, false, true));
            }
            sm.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1, false, true));
        }
        for (HexField field : this.activeHexFields) {
            if (field.center.getWorld() == null) {
                continue;
            }
            for (int i = 0; i < 4; ++i) {
                double angle = Math.toRadians((double)(this.tickCounter * 2 % 360) + i * 90.0);
                Location pl = field.center.clone().add(Math.cos(angle) * 5.0, 1.0, Math.sin(angle) * 5.0);
                field.center.getWorld().spawnParticle(Particle.SPELL_MOB, pl, 2, 0.1, 0.1, 0.1, 0.0);
            }
            field.center.getWorld().spawnParticle(Particle.PORTAL, field.center.clone().add(0.0, 1.0, 0.0), 5, 4.0, 0.5, 4.0, 0.0);
        }
        for (UUID uid : new HashSet<UUID>(this.mirrorEndTime.keySet())) {
            Player p2 = Bukkit.getPlayer(uid);
            if (p2 == null || !p2.isOnline()) {
                this.mirrorEndTime.remove(uid);
                continue;
            }
            if (System.currentTimeMillis() < this.mirrorEndTime.get(uid)) {
                p2.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1, false, true));
                p2.getWorld().spawnParticle(Particle.CRIT_MAGIC, p2.getLocation().add(0.0, 1.5, 0.0), 5, 0.3, 0.3, 0.3, 0.0);
            }
        }
    }

    private void forceTargetEnemies() {
        for (Map.Entry<UUID, List<Skeleton>> entry : this.necroArmy.entrySet()) {
            UUID ownerUid = entry.getKey();
            TeamColor ownerTeam = this.gm.getTeam(ownerUid);
            if (ownerTeam == null) {
                for (Skeleton skel : entry.getValue()) {
                    if (!skel.isValid()) continue;
                    skel.setTarget(null);
                }
                continue;
            }
            TeamColor enemyTeam = ownerTeam == TeamColor.RED ? TeamColor.BLUE : TeamColor.RED;
            for (Skeleton skel : entry.getValue()) {
                if (!skel.isValid()) continue;
                Player nearest = null;
                double nearestDist = 900.0;
                for (Player target : skel.getWorld().getPlayers()) {
                    double d;
                    if (!this.gm.isParticipant(target) || this.gm.isSpectator(target) || this.gm.getTeamOf(target) != enemyTeam || !((d = target.getLocation().distanceSquared(skel.getLocation())) < nearestDist)) continue;
                    nearestDist = d;
                    nearest = target;
                }
                skel.setTarget(null);
                if (nearest == null) continue;
                skel.setTarget(nearest);
            }
        }
        for (TurretData t : this.activeTurrets) {
            if (!t.entity.isValid()) continue;
            Player nearest = null;
            double nearestDist = 900.0;
            for (Player target : t.entity.getWorld().getPlayers()) {
                double d;
                if (!this.gm.isParticipant(target) || this.gm.isSpectator(target) || this.gm.getTeamOf(target) == t.ownerTeam || !((d = target.getLocation().distanceSquared(t.entity.getLocation())) < nearestDist)) continue;
                nearestDist = d;
                nearest = target;
            }
            t.entity.setTarget(null);
            if (nearest == null) continue;
            t.entity.setTarget(nearest);
        }
    }

    public void fastUpdate() {
        block0: for (TrapData t : this.activeTraps) {
            if (System.currentTimeMillis() - t.placeTime < 1000L) continue;
            TeamColor ownerTeam = this.gm.getTeam(t.owner);
            for (Player p : t.loc.getWorld().getPlayers()) {
                if (!(p.getLocation().distance(t.loc) <= 3.0) || !this.gm.isParticipant(p) || this.gm.getTeamOf(p) == ownerTeam || t.triggered) continue;
                this.triggerTrap(t, p);
                continue block0;
            }
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.SNIPER || !this.gm.isParticipant(p) || this.markedForDeath.size() <= 0 || p.isOnGround() && p.isSneaking() && p.getInventory().getItemInMainHand().getType() == Material.CROSSBOW) continue;
            this.markedForDeath.clear();
            p.sendMessage("\u00a77\u72d9\u6483\u773c\u304c\u89e3\u9664\u3055\u308c\u307e\u3057\u305f");
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!this.guardBroken.contains(p.getUniqueId())) continue;
            p.setCooldown(Material.SHIELD, 20);
            if (p.isBlocking()) {
                p.setShieldBlockingDelay(20);
            }
        }
    }

    private void updateKitActionBars() {
        block14: for (Player p : Bukkit.getOnlinePlayers()) {
            KitType kit;
            if (!this.gm.isParticipant(p) || this.gm.isSpectator(p) || (kit = this.gm.getPlayerKitType(p.getUniqueId())) == null) continue;
            if (this.guardBroken.contains(p.getUniqueId())) {
                p.sendActionBar((Component)Component.text((String)"\u00a7c\ud83d\udee1 \u30ac\u30fc\u30c9\u30d6\u30ec\u30a4\u30af\u4e2d\uff01"));
                continue;
            }
            if (this.universalCharged.getOrDefault(p.getUniqueId(), false).booleanValue()) {
                p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)"\u00a7e\u26a1 \u30ac\u30fc\u30c9\u30d6\u30ec\u30a4\u30af\u30c1\u30e3\u30fc\u30b8\u5b8c\u4e86\uff01")));
                continue;
            }
            switch (kit) {
                case SNIPER: {
                    Integer ticks = this.sniperAimTick.get(p.getUniqueId());
                    UUID targetUid = this.sniperTracker.get(p.getUniqueId());
                    if (ticks != null && targetUid != null && ticks > 0) {
                        Player target = Bukkit.getPlayer((UUID)targetUid);
                        String targetName = target != null ? target.getName() : "???";
                        int pct = Math.min(100, ticks * 20);
                        String bar = "\u00a7a" + "\u2588".repeat(pct / 10) + "\u00a77" + "\u2588".repeat(10 - pct / 10);
                        int remaining = Math.max(0, 7 - ticks);
                        p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)("\u00a7c\ud83c\udfaf \u72d9\u6483\u4e2d: " + targetName + " \u00a78[" + bar + "\u00a78] \u00a7e" + remaining + "s"))));
                        break;
                    }
                    this.sendCTOnly(p);
                    break;
                }
                case BOMBER: {
                    long mines = this.activeMines.stream().filter(m -> m.owner.equals(p.getUniqueId())).count();
                    if (mines > 0L) {
                        p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)("\u00a7c\ud83d\udca3 \u5730\u96f7 x" + mines))));
                        break;
                    }
                    this.sendCTOnly(p);
                    break;
                }
                case SUNDANCE: {
                    int shots = this.sundanceRevolver.getOrDefault(p.getUniqueId(), 0);
                    if (shots > 0) {
                        p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)("\u00a7b\u26a1 \u30ea\u30dc\u30eb\u30d3\u30f3\u30b0 \u00a7f" + shots + "/5"))));
                        break;
                    }
                    this.sendCTOnly(p);
                    break;
                }
                case GUARDIAN: {
                    Long end = this.guardianEndTime.get(p.getUniqueId());
                    if (end != null && System.currentTimeMillis() < end) {
                        long rem = (end - System.currentTimeMillis()) / 1000L;
                        p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)("\u00a7f\ud83d\udee1 \u9244\u58c1 \u00a7e\u6b8b\u308a " + rem + "\u79d2"))));
                        break;
                    }
                    this.sendCTOnly(p);
                    break;
                }
                case TRAPPER: {
                    long traps = this.activeTraps.stream().filter(t -> t.owner.equals(p.getUniqueId()) && !t.triggered).count();
                    if (traps > 0L) {
                        p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)("\u00a73\ud83e\udea4 \u7f60 x" + traps + "/2"))));
                        break;
                    }
                    this.sendCTOnly(p);
                    break;
                }
                case PHANTOM: {
                    Long end = this.phantomEnd.get(p.getUniqueId());
                    if (end != null && System.currentTimeMillis() < end) {
                        long rem = (end - System.currentTimeMillis()) / 1000L;
                        p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)("\u00a77\ud83d\udc7b \u970a\u4f53\u5316 \u00a7e\u6b8b\u308a " + rem + "\u79d2"))));
                        break;
                    }
                    this.sendCTOnly(p);
                    break;
                }
                case ANCHOR: {
                    if (this.anchorFields.containsKey(p.getUniqueId())) {
                        p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)"\u00a79\u2693 \u78c1\u5834\u5c55\u958b\u4e2d \u00a77| \u534a\u5f848m \u6e1b\u901f+\u5f31\u4f53+\u7d99\u7d9a\u30c0\u30e1")));
                        break;
                    }
                    this.sendCTOnly(p);
                    break;
                }
                case ENGINEER: {
                    long turrets = this.activeTurrets.stream().filter(t -> t.owner.equals(p.getUniqueId())).count();
                    if (turrets > 0L) {
                        p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)("\u00a76\ud83d\udd27 \u30bf\u30ec\u30c3\u30c8 x" + turrets))));
                        break;
                    }
                    this.sendCTOnly(p);
                    break;
                }
                case SCOUT: {
                    long recons = this.activeRecons.stream().filter(r -> r.ownerTeam == this.gm.getTeamOf(p)).count();
                    if (recons > 0L) {
                        p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)("\u00a7a\ud83d\udce1 \u30ea\u30b3\u30f3 x" + recons))));
                        break;
                    }
                    this.sendCTOnly(p);
                    break;
                }
                case COUNTER: {
                    if (this.parryActive.contains(p.getUniqueId())) {
                        p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)"\u00a79\ud83d\udee1 \u30d1\u30ea\u30a3\u53d7\u4ed8\u4e2d\uff01")));
                        break;
                    }
                    this.sendCTOnly(p);
                    break;
                }
                case GRANG: {
                    Boolean charging = this.grangCharging.get(p.getUniqueId());
                    if (charging != null && charging.booleanValue()) {
                        Long start = this.grangChargeStart.get(p.getUniqueId());
                        if (start == null) continue block14;
                        long elapsed = System.currentTimeMillis() - start;
                        int pct = (int)Math.min(100L, elapsed * 100L / 7000L);
                        p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)("\u00a77\ud83d\udee1 \u30c1\u30e3\u30fc\u30b8 \u00a7e" + pct + "%"))));
                        break;
                    }
                    this.sendCTOnly(p);
                    break;
                }
                case VAMPIRE: {
                    double gauge = this.vampireGauge.getOrDefault(p.getUniqueId(), 0.0);
                    int stage = gauge >= 55.0 ? 4 : (gauge >= 40.0 ? 3 : (gauge >= 25.0 ? 2 : (gauge >= 10.0 ? 1 : 0)));
                    boolean inBlood = this.vampireBloodMode.getOrDefault(p.getUniqueId(), false);
                    String bar = this.gaugeBar((int)gauge, 80);
                    p.sendActionBar(this.ctAppend(p, (Component)Component.text((String)((inBlood ? "\u00a74\u00a7l\u2694 \u30d6\u30e9\u30c3\u30c9" : "\u00a77\u30c9\u30ec\u30a4\u30f3") + " S" + stage + " \u00a78[\u00a7a" + bar + "\u00a78] \u00a7f" + String.format("%.0f/80", gauge)))));
                    break;
                }
                default: {
                    this.sendCTOnly(p);
                }
            }
        }
    }

    private String gaugeBar(int val, int max) {
        int bars = Math.min(10, Math.max(0, val * 10 / max));
        return "\u00a7a" + "\u2588".repeat(bars) + "\u00a77" + "\u2588".repeat(10 - bars);
    }

    private void sendCTOnly(Player p) {
        float rem;
        Long cd = this.skillCooldowns.get(p.getUniqueId());
        if (cd != null && (rem = (float)(cd - System.currentTimeMillis()) / 1000.0f) > 0.0f) {
            p.sendActionBar((Component)Component.text((String)("\u00a7fCT " + String.format("%.1f", Float.valueOf(rem)) + "s")));
        }
    }

    private Component ctAppend(Player p, Component base) {
        float rem;
        Long cd = this.skillCooldowns.get(p.getUniqueId());
        if (cd != null && (rem = (float)(cd - System.currentTimeMillis()) / 1000.0f) > 0.0f) {
            return ((TextComponent.Builder)((TextComponent.Builder)Component.text().append(base)).append((Component)Component.text((String)(" \u00a77| \u00a7fCT " + String.format("%.1f", Float.valueOf(rem)) + "s")))).build();
        }
        return base;
    }

    private void useKitSkill(Player p, String kitName) {
        KitType kit;
        try {
            kit = KitType.valueOf(kitName);
        }
        catch (Exception e) {
            return;
        }
        if (this.gm.getPlayerKitType(p.getUniqueId()) != kit) {
            return;
        }
        if (this.deadlockedPlayers.contains(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u30c7\u30c3\u30c9\u30ed\u30c3\u30af\u4e2d\u306f\u30b9\u30ad\u30eb\u3092\u4f7f\u7528\u3067\u304d\u307e\u305b\u3093\uff01");
            return;
        }
        if (this.inEnemyHexField(p)) {
            p.sendActionBar((Component)Component.text((String)"\u00a75\u00a7l\u546a\u7e1b\u9818\u57df\u5185\uff01\u30b9\u30ad\u30eb\u4f7f\u7528\u4e0d\u53ef"));
            return;
        }
        if (this.gm.isFlagCarrier(p.getUniqueId()) && (kit == KitType.BREAKER || kit == KitType.NINJA || kit == KitType.JESTER || kit == KitType.KREUTZ || kit == KitType.VAMPIRE)) {
            p.sendMessage("\u00a7c\u65d7\u3092\u6301\u3063\u3066\u3044\u308b\u9593\u306f\u79fb\u52d5\u30b9\u30ad\u30eb\u3092\u4f7f\u7528\u3067\u304d\u307e\u305b\u3093\uff01");
            return;
        }
        if (this.isOnCooldown(p.getUniqueId())) {
            Long cd = this.skillCooldowns.get(p.getUniqueId());
            float remainF = cd != null ? (float)(cd - System.currentTimeMillis()) / 1000.0f : 0.0f;
            p.sendMessage("\u00a7c\u30b9\u30ad\u30eb\u30af\u30fc\u30eb\u30bf\u30a4\u30e0\u4e2d\uff01 \u00a77\u6b8b\u308a\u00a7f" + String.format("%.1f", Float.valueOf(remainF)) + "\u00a77\u79d2");
            return;
        }
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.8f);
        switch (kit) {
            case BLADE: {
                this.bladeSkill(p);
                break;
            }
            case BREAKER: {
                this.breakerSkill(p);
                break;
            }
            case NINJA: {
                this.ninjaSkill(p);
                break;
            }
            case BERSERKER: {
                this.berserkerSkill(p);
                break;
            }
            case COUNTER: {
                this.onParryAttempt(p);
                break;
            }
            case PYRO: {
                this.pyroSkill(p);
                break;
            }
            case LANCER: {
                this.lancerSkill(p);
                break;
            }
            case JESTER: {
                this.jesterSkill(p);
                break;
            }
            case VAMPIRE: {
                this.vampireToggle(p);
                break;
            }
            case BOMBER: {
                this.bomberSkill(p);
                break;
            }
            case COOK: {
                this.cookSkill(p);
                break;
            }
            case SCOUT: {
                this.scoutSkill(p);
                break;
            }
            case WHIRLWIND: {
                this.whirlwindGust(p);
                break;
            }
            case NILGIRITAR: {
                this.nilgiritarSkill(p);
                break;
            }
            case MISTRAL: {
                this.mistralSkill(p);
                break;
            }
            case SUPERIOR_MISTRAL: {
                this.superiorMistralSkill(p);
                break;
            }
            case FLASHER: {
                this.flasherSkill(p);
                break;
            }
            case MARKSMAN: {
                this.marksmanSkill(p);
                break;
            }
            case SUNDANCE: {
                break;
            }
            case ROCKETER: {
                this.rocketerFire(p);
                break;
            }
            case ALCHEMIST: {
                this.alchemistSkill(p);
                break;
            }
            case ENGINEER: {
                this.engineerSkill(p);
                break;
            }
            case TRAPPER: {
                this.trapperSkill(p);
                break;
            }
            case GUARDIAN: {
                this.guardianSkill(p);
                break;
            }
            case MEDIC: {
                this.medicSkill(p);
                break;
            }
            case SUPPORTER: {
                this.supporterSkill(p);
                break;
            }
            case RESTRICTIONER: {
                this.restrictionerSkill(p);
                break;
            }
            case KREUTZ: {
                this.kreutzSkill(p);
                break;
            }
            case SWAPPER: {
                this.swapperSkill(p);
                break;
            }
            case STICKER: {
                this.stickerSkill(p);
                break;
            }
            case DECOY: {
                this.decoySkill(p);
                break;
            }
            case PHANTOM: {
                this.phantomSkill(p);
                break;
            }
            case ANCHOR: {
                this.anchorSkill(p);
                break;
            }
            case RELEASER: {
                this.releaseSkill(p);
                break;
            }
            case GRANG: {
                break;
            }
            case NECRO: {
                this.necroSkill(p);
                break;
            }
            case BULWARK: {
                this.bulwarkSkill(p);
                break;
            }
            case TIMEKEEPER: {
                this.timekeeperSkill(p);
                break;
            }
            case AEGIS: {
                this.aegisSkill(p);
                break;
            }
            case HEXER: {
                this.hexerSkill(p);
                break;
            }
            case REFLECTOR: {
                this.reflectorSkill(p);
                break;
            }
            case GLACIES: {
                this.glaciesSkill(p);
                break;
            }
        }
        this.plugin.getTutorialManager().checkSkillUsed(p);
    }

    public void clearSniperMarkOnShoot(Player p) {
        if (!this.markedForDeath.isEmpty() && this.gm.getPlayerKitType(p.getUniqueId()) == KitType.SNIPER) {
            this.markedForDeath.clear();
            this.sniperAimTick.remove(p.getUniqueId());
            this.sniperTracker.remove(p.getUniqueId());
        }
    }

    private void useBurst(Player p) {
        if (this.deadlockedPlayers.contains(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u30c7\u30c3\u30c9\u30ed\u30c3\u30af\u4e2d\u306f\u30d0\u30fc\u30b9\u30c8\u3092\u4f7f\u7528\u3067\u304d\u307e\u305b\u3093\uff01");
            return;
        }
        if (this.inEnemyHexField(p)) {
            p.sendActionBar((Component)Component.text((String)"\u00a75\u00a7l\u546a\u7e1b\u9818\u57df\u5185\uff01\u30d0\u30fc\u30b9\u30c8\u4f7f\u7528\u4e0d\u53ef"));
            return;
        }
        if (this.burstUsed.contains(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u30d0\u30fc\u30b9\u30c8\u306f\u3053\u306e\u30e9\u30a6\u30f3\u30c9\u3067\u306f\u4f7f\u7528\u6e08\u307f\u3067\u3059\u3002");
            return;
        }
        if (this.gm.isFlagCarrier(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u65d7\u3092\u6301\u3063\u3066\u3044\u308b\u9593\u306f\u30d0\u30fc\u30b9\u30c8\u3092\u4f7f\u7528\u3067\u304d\u307e\u305b\u3093\uff01");
            return;
        }
        this.burstUsed.add(p.getUniqueId());
        p.getInventory().remove(Material.HEART_OF_THE_SEA);
        this.plugin.getTutorialManager().checkBurstUsed(p);
        KitType kit = this.gm.getPlayerKitType(p.getUniqueId());
        KitRole role = kit != null ? kit.getRole() : KitRole.DUELIST;
        Location loc = p.getLocation();
        World w = p.getWorld();
        if (w == null) {
            return;
        }
        p.setInvulnerable(true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 10, 1, false, true));
        w.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 2.0f, 2.0f);
        p.sendMessage("\u00a7c\u00a7l\u30d0\u30fc\u30b9\u30c8\u767a\u52d5\uff01");
        double range = 6.0;
        double knockback = 1.5;
        if (role == KitRole.INITIATOR) {
            range = 7.0;
        } else if (role == KitRole.CONTROLLER) {
            range = 8.0;
        } else if (role == KitRole.DUELIST) {
            knockback = 1.8;
        }
        final double fRange = range;
        final double fKnockback = knockback;
        final KitRole fRole = role;
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            p.removePotionEffect(PotionEffectType.LEVITATION);
            p.setInvulnerable(false);
            int selfWeaknessAmp = 1;
            if (fRole == KitRole.SENTINEL) {
                selfWeaknessAmp = 0;
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1, false, true));
            }
            if (fRole == KitRole.DUELIST) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1, false, true));
            } else if (fRole == KitRole.INITIATOR) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 1, false, true));
            }
            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, selfWeaknessAmp, false, true));
            Location loc2 = p.getLocation();
            w.createExplosion(loc2, 0.0f, false, false, (Entity)p);
            w.spawnParticle(Particle.EXPLOSION_LARGE, loc2, 8, 1.5, 1.0, 1.5, 0.1);
            w.spawnParticle(Particle.CLOUD, loc2.clone().add(0.0, 1.0, 0.0), 30, 2.0, 1.0, 2.0, 0.05);
            w.playSound(loc2, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
            for (Entity e : w.getNearbyEntities(loc2, fRange, 3.0, fRange)) {
                Player target;
                if (!(e instanceof Player) || !this.gm.isParticipant(target = (Player)e) || this.gm.getTeamOf(target) == this.gm.getTeamOf(p)) continue;
                target.damage(3.0, (Entity)p);
                target.setVelocity(target.getLocation().toVector().subtract(loc2.toVector()).normalize().multiply(fKnockback).setY(0.5));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1, false, true));
                if (fRole == KitRole.CONTROLLER) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 1, false, true));
                }
            }
            this.gm.setNoFallDamage(p.getUniqueId());
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.gm.clearNoFallDamage(p.getUniqueId()), 100L);
        }, 10L);
    }

    private void bladeSkill(Player p) {
        if (!p.isSneaking()) {
            p.sendMessage("\u00a7c\u3057\u3083\u304c\u307f\u306a\u304c\u3089\u4f7f\u7528\u3057\u3066\u304f\u3060\u3055\u3044");
            return;
        }
        this.setCooldown(p.getUniqueId(), 10000L);
        World w = p.getWorld();
        Location loc = p.getLocation();
        w.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.7f);
        w.spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0.0, 1.0, 0.0), 5, 2.0, 0.5, 2.0, 0.0);
        for (Entity e : w.getNearbyEntities(loc, 3.0, 2.0, 3.0)) {
            Player t;
            if (!(e instanceof Player) || !this.gm.isParticipant(t = (Player)e) || this.gm.getTeamOf(t) == this.gm.getTeamOf(p)) continue;
            t.damage(3.0, (Entity)p);
            t.setVelocity(new Vector(0.0, 0.8, 0.0));
            t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, true));
            t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1, false, true));
            if (this.gm.getTeamOf(t) != this.gm.getTeamOf(p)) {
                Long theirCd = this.skillCooldowns.get(t.getUniqueId());
                long base = theirCd != null && theirCd > System.currentTimeMillis() ? theirCd : System.currentTimeMillis();
                this.skillCooldowns.put(t.getUniqueId(), base + 5000L);
            }
            this.guardBroken.add(t.getUniqueId());
            this.triggerGrangBurst(t);
            t.getWorld().spawnParticle(Particle.CRIT, t.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.1);
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.guardBroken.remove(t.getUniqueId()), 60L);
        }
    }

    private void breakerSkill(final Player p) {
        this.setCooldown(p.getUniqueId(), 10000L);
        Vector dir = p.getLocation().getDirection().normalize().multiply(2.0).setY(0.3);
        p.setVelocity(dir);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.6f);
        new BukkitRunnable(){
            int t = 0;

            public void run() {
                if (this.t++ > 10 || !p.isOnline()) {
                    this.cancel();
                    return;
                }
                Location cur = p.getLocation();
                p.getWorld().spawnParticle(Particle.CRIT, cur, 5, 0.3, 0.3, 0.3, 0.0);
                for (Entity e : cur.getWorld().getNearbyEntities(cur, 2.0, 2.0, 2.0)) {
                    Player target;
                    if (!(e instanceof Player) || !SkillManager.this.gm.isParticipant(target = (Player)e) || SkillManager.this.gm.getTeamOf(target) == SkillManager.this.gm.getTeamOf(p)) continue;
                    target.damage(4.0, (Entity)p);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1, false, true));
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
    }

    private void ninjaSkill(Player p) {
        if (!p.getInventory().contains(Material.ENDER_PEARL)) {
            p.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ENDER_PEARL)});
        }
        this.setCooldown(p.getUniqueId(), 18000L);
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 160, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 160, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1, false, false));
        this.hideArmor(p);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (p.isOnline()) {
                this.showArmor(p);
                p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                p.removePotionEffect(PotionEffectType.SPEED);
            }
        }, 160L);
        p.sendMessage("\u00a72\u00a7l\u96a0\u5f62\uff01");
    }

    private void berserkerSkill(final Player p) {
        this.setCooldown(p.getUniqueId(), 14000L);
        final Vector dir = p.getLocation().getDirection().normalize();
        final Location cur = p.getLocation().clone();
        final World w = cur.getWorld();
        if (w == null) {
            return;
        }
        p.sendMessage("\u00a74\u00a7l\u6012\u6d9b\u7206\u7815\uff01");
        new BukkitRunnable(){
            int t = 0;

            public void run() {
                if (this.t++ > 10) {
                    this.cancel();
                    return;
                }
                cur.add(dir.clone().multiply(1.5));
                w.createExplosion(cur, 0.0f, false, false, (Entity)p);
                w.spawnParticle(Particle.FLAME, cur, 10, 0.5, 0.5, 0.5, 0.03);
                for (Entity e2 : w.getNearbyEntities(cur, 2.5, 2.0, 2.5)) {
                    Player target2;
                    if (!(e2 instanceof Player) || !SkillManager.this.gm.isParticipant(target2 = (Player)e2) || SkillManager.this.gm.getTeamOf(target2) == SkillManager.this.gm.getTeamOf(p)) continue;
                    target2.damage(6.0, (Entity)p);
                    target2.setVelocity(dir.clone().multiply(1.5 * (1.0 - (double)this.t * 0.08)).setY(0.4 * (1.0 - (double)this.t * 0.08)));
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 2L, 2L);
    }

    private void pyroSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 15000L);
        World w = p.getWorld();
        Location loc = p.getLocation();
        w.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.5f);
        for (Entity e : w.getNearbyEntities(loc, 5.0, 3.0, 5.0)) {
            Player t;
            if (!(e instanceof Player) || !this.gm.isParticipant(t = (Player)e) || this.gm.getTeamOf(t) == this.gm.getTeamOf(p)) continue;
            if (t.getFireTicks() > 0) {
                t.damage(12.0, (Entity)p);
            } else {
                t.setFireTicks(80);
            }
            t.getWorld().spawnParticle(Particle.FLAME, t.getLocation().add(0.0, 1.0, 0.0), 20, 0.3, 0.3, 0.3, 0.05);
        }
    }

    private void lancerSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 2000L);
        Vector dir = p.getLocation().getDirection().normalize();
        Location start = p.getEyeLocation();
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.5f);
        p.sendMessage("\u00a7b\u00a7l\u523a\u7a81\uff01");
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            Location end = start.clone().add(dir.clone().multiply(5.0));
            p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, end, 3, 0.3, 0.3, 0.3, 0.0);
            p.getWorld().spawnParticle(Particle.CRIT, start.clone().add(dir.clone().multiply(2.5)), 5, 0.1, 0.1, 0.1, 0.05);
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.5f);
            for (double d = 0.0; d <= 5.0; d += 0.3) {
                Location check = start.clone().add(dir.clone().multiply(d));
                for (Entity e : check.getWorld().getNearbyEntities(check, 0.75, 0.75, 0.75)) {
                    Player t;
                    if (!(e instanceof Player) || !this.gm.isParticipant(t = (Player)e) || this.gm.getTeamOf(t) == this.gm.getTeamOf(p)) continue;
                    if (t.isBlocking()) {
                        t.damage(5.0, (Entity)p);
                        t.setCooldown(Material.SHIELD, 20);
                        t.getWorld().playSound(t.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 0.8f);
                        t.sendMessage("\u00a7c\u76fe\u8d8a\u3057\u306b5\u30c0\u30e1\u30fc\u30b8\u8cab\u901a\uff01");
                    } else {
                        t.damage(9.0, (Entity)p);
                    }
                    t.setVelocity(dir.clone().multiply(1.5).setY(0.3));
                    Long cur = this.skillCooldowns.get(p.getUniqueId());
                    if (cur != null) {
                        this.skillCooldowns.put(p.getUniqueId(), cur.longValue() - 1000L);
                    }
                    return;
                }
            }
        }, 5L);
    }

    private void jesterSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 10000L);
        p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 200, 2, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, false));
        p.sendMessage("\u00a7e\u00a7l\u9053\u5316\u306e\u75be\u8d70\uff01");
    }

    private void theosPadaSkill(Player p) {
        this.theosPadaAction(p, true);
    }

    public void markWithWindHole(Player victim) {
        this.nilgiritarMarks.put(victim.getUniqueId(), System.currentTimeMillis() + 5000L);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
        victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.8f);
        victim.getWorld().spawnParticle(Particle.SWEEP_ATTACK, victim.getLocation().add(0.0, 1.0, 0.0), 3, 0.5, 0.5, 0.5, 0.0);
        victim.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, victim.getLocation().add(0.0, 1.5, 0.0), 20, 0.3, 0.3, 0.3, 0.1);
        victim.sendActionBar((Component)Component.text((String)"\u00a7f\ud83c\udf00 \u98a8\u7a74\u30de\u30fc\u30af\uff01\u00a77\uff08\u76fe\u8cab\u901a\u53ef\u80fd\uff09"));
    }

    public boolean tryPierceShield(Player attacker, Player victim) {
        Long mark = this.nilgiritarMarks.get(victim.getUniqueId());
        if (mark == null || System.currentTimeMillis() > mark) {
            this.nilgiritarMarks.remove(victim.getUniqueId());
            return false;
        }
        this.nilgiritarMarks.remove(victim.getUniqueId());
        victim.getWorld().playSound(victim.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 0.8f);
        victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.3, 0.3, 0.1);
        victim.sendActionBar((Component)Component.text((String)"\u00a7c\ud83d\udee1 \u76fe\u8cab\u901a\uff01"));
        attacker.sendActionBar((Component)Component.text((String)"\u00a7f\ud83c\udf00 \u98a8\u7a74\u8cab\u901a\uff01"));
        return true;
    }

    private void nilgiritarSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 15000L);
        p.sendMessage("\u00a7f\ud83c\udf00 \u98a8\u7a74\u611f\u77e5\uff01\u00a77\u8fd1\u304f\u306e\u6575\u3092\u63a2\u77e5");
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);
        p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, p.getLocation().add(0.0, 1.0, 0.0), 8, 2.0, 0.5, 2.0, 0.0);
        Location loc = p.getLocation();
        for (Entity e : loc.getWorld().getNearbyEntities(loc, 6.0, 4.0, 6.0)) {
            Player t;
            if (!(e instanceof Player) || !this.gm.isParticipant(t = (Player)e) || this.gm.getTeamOf(t) == this.gm.getTeamOf(p)) continue;
            t.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, false, true));
        }
    }

    private void kreutzSkill(final Player p) {
        boolean needsTarget;
        if (p.isSneaking()) {
            if (this.isOnCooldown(p.getUniqueId())) {
                return;
            }
            this.setCooldown(p.getUniqueId(), 2000L);
            String card = this.KREUTZ_CARDS[new Random().nextInt(this.KREUTZ_CARDS.length)];
            this.kreutzCard.put(p.getUniqueId(), card);
            p.sendMessage("\u00a75\u00a7l\ud83c\udccf " + card + " \u00a77\u3092\u5f15\u3044\u305f\uff01");
            return;
        }
        String card = this.kreutzCard.get(p.getUniqueId());
        if (card == null) {
            p.sendMessage("\u00a7c\u30ab\u30fc\u30c9\u3092\u5f15\u3044\u3066\u3044\u307e\u305b\u3093\uff01\u3057\u3083\u304c\u307f\u53f3\u30af\u30ea\u3067\u30c9\u30ed\u30fc");
            return;
        }
        Player target = this.getTargetInSight(p, 20);
        switch (card) {
            case "\u30a2\u30a4\u30b9\u30e9\u30f3\u30b9": 
            case "\u30b5\u30f3\u30c0\u30fc": 
            case "\u30ab\u30fc\u30b9": 
            case "\u30c1\u30a7\u30a4\u30f3": 
            case "\u30a6\u30a3\u30fc\u30af\u30cd\u30b9": 
            case "\u30de\u30a4\u30f3\u30c9": 
            case "\u30d4\u30a2\u30c3\u30b7\u30f3\u30b0": {
                needsTarget = true;
                break;
            }
            default: {
                needsTarget = false;
            }
        }
        if (needsTarget && target == null) {
            p.sendMessage("\u00a7c\u5bfe\u8c61\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\uff01\u30ab\u30fc\u30c9\u306f\u6d88\u8cbb\u3055\u308c\u307e\u305b\u3093");
            return;
        }
        this.kreutzCard.remove(p.getUniqueId());
        p.sendMessage("\u00a75\u00a7l\ud83c\udccf " + card + " \u00a77\u3092\u5531\u3048\u305f\uff01");
        switch (card) {
            case "\u30d5\u30a1\u30a4\u30a2\u30dc\u30fc\u30eb": {
                final Snowball b = (Snowball)p.launchProjectile(Snowball.class);
                b.setVelocity(p.getLocation().getDirection().normalize().multiply(1.0));
                b.setGlowing(true);
                b.setCustomName("kreutzFireball");
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.5f);
                new BukkitRunnable(){
                    int t = 0;

                    public void run() {
                        if (this.t++ > 40 || !b.isValid()) {
                            this.cancel();
                            return;
                        }
                        b.getWorld().spawnParticle(Particle.FLAME, b.getLocation(), 2, 0.2, 0.2, 0.2, 0.01);
                        Player nearest = null;
                        double nd = 36.0;
                        for (Player t2 : b.getWorld().getPlayers()) {
                            double d;
                            if (!SkillManager.this.gm.isParticipant(t2) || SkillManager.this.gm.getTeamOf(t2) == SkillManager.this.gm.getTeamOf(p) || !((d = t2.getLocation().distanceSquared(b.getLocation())) < nd)) continue;
                            nd = d;
                            nearest = t2;
                        }
                        if (nearest != null && nd < 4.0) {
                            b.getWorld().createExplosion(b.getLocation(), 0.0f, false, false, (Entity)p);
                            b.getWorld().spawnParticle(Particle.FLAME, b.getLocation(), 10, 0.3, 0.3, 0.3, 0.02);
                            for (Entity e2 : b.getLocation().getWorld().getNearbyEntities(b.getLocation(), 2.0, 1.5, 2.0)) {
                                Player t2;
                                if (!(e2 instanceof Player) || !SkillManager.this.gm.isParticipant(t2 = (Player)e2) || SkillManager.this.gm.getTeamOf(t2) == SkillManager.this.gm.getTeamOf(p)) continue;
                                t2.damage(4.0, (Entity)p);
                                t2.setFireTicks(40);
                            }
                            b.remove();
                            this.cancel();
                            return;
                        }
                        if (nearest != null) {
                            Vector toTarget = nearest.getLocation().toVector().subtract(b.getLocation().toVector()).normalize().multiply(0.3);
                            b.setVelocity(b.getVelocity().add(toTarget).normalize().multiply(1.0));
                        }
                    }
                }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
                break;
            }
            case "\u30a2\u30a4\u30b9\u30e9\u30f3\u30b9": {
                if (target == null) break;
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 160, 3, false, true));
                target.damage(7.0, (Entity)p);
                target.getWorld().spawnParticle(Particle.SNOW_SHOVEL, target.getLocation().add(0.0, 1.0, 0.0), 15, 0.3, 0.3, 0.3, 0.05);
                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
                break;
            }
            case "\u30b5\u30f3\u30c0\u30fc": {
                if (target == null) break;
                target.getWorld().strikeLightningEffect(target.getLocation());
                target.damage(6.0, (Entity)p);
                target.setFireTicks(40);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 60, 10, false, true));
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                break;
            }
            case "\u30b7\u30fc\u30eb\u30c9": {
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 240, 4, false, true));
                p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, p.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 1.0, 0.5, 0.1);
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 2.0f);
                break;
            }
            case "\u30d2\u30fc\u30eb": {
                if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                    p.setHealth(Math.min(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(), p.getHealth() + 10.0));
                }
                p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0.0, 1.5, 0.0), 5, 0.3, 0.3, 0.3, 0.0);
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
                break;
            }
            case "\u30ab\u30fc\u30b9": {
                if (target == null) break;
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 160, 2, false, true));
                target.getWorld().spawnParticle(Particle.SPELL_MOB_AMBIENT, target.getLocation().add(0.0, 1.0, 0.0), 20, 0.3, 0.3, 0.3, 0.0);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.5f, 0.3f);
                break;
            }
            case "\u30b0\u30e9\u30d3\u30c6\u30a3": {
                for (Entity e2 : p.getWorld().getNearbyEntities(p.getLocation(), 8.0, 3.0, 8.0)) {
                    Player t2;
                    if (!(e2 instanceof Player) || !this.gm.isParticipant(t2 = (Player)e2) || this.gm.getTeamOf(t2) == this.gm.getTeamOf(p)) continue;
                    t2.setVelocity(p.getLocation().toVector().subtract(t2.getLocation().toVector()).normalize().multiply(2.5).setY(0.8));
                    t2.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 3, false, true));
                }
                p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), 30, 4.0, 1.0, 4.0, 0.05);
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
                break;
            }
            case "\u30c1\u30a7\u30a4\u30f3": {
                if (target == null) break;
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 3, false, true));
                target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 80, -10, false, true));
                target.damage(5.0, (Entity)p);
                target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.3, 0.3, 0.1);
                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_CHAIN_BREAK, 1.0f, 0.8f);
                break;
            }
            case "\u30dd\u30a4\u30ba\u30f3\u30af\u30e9\u30a6\u30c9": {
                Location cl = p.getLocation();
                for (Entity e2 : cl.getWorld().getNearbyEntities(cl, 5.0, 2.0, 5.0)) {
                    Player t2;
                    if (!(e2 instanceof Player) || !this.gm.isParticipant(t2 = (Player)e2) || this.gm.getTeamOf(t2) == this.gm.getTeamOf(p)) continue;
                    t2.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 120, 2, false, true));
                }
                cl.getWorld().spawnParticle(Particle.SPELL_MOB, cl, 40, 5.0, 1.0, 5.0, 0.02);
                cl.getWorld().playSound(cl, Sound.ENTITY_SPIDER_DEATH, 0.5f, 0.5f);
                break;
            }
            case "\u30b9\u30d4\u30fc\u30c9\u30d6\u30fc\u30b9\u30c8": {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2, false, true));
                p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0.0, 0.5, 0.0), 10, 0.3, 0.3, 0.3, 0.05);
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
                break;
            }
            case "\u30ea\u30fc\u30d7": {
                Vector ld = p.getLocation().getDirection().normalize().multiply(4.0).setY(0.8);
                p.setVelocity(ld);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, true));
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.5f);
                break;
            }
            case "\u30a6\u30a3\u30fc\u30af\u30cd\u30b9": {
                if (target == null) break;
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 160, 1, false, true));
                target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0.0, 1.0, 0.0), 5, 0.3, 0.3, 0.3, 0.0);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_IRON_GOLEM_DAMAGE, 0.5f, 0.5f);
                break;
            }
            case "\u30de\u30a4\u30f3\u30c9": {
                if (target == null) break;
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0, false, true));
                target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0, false, true));
                target.getWorld().spawnParticle(Particle.SPELL_WITCH, target.getLocation().add(0.0, 1.5, 0.0), 20, 0.3, 0.3, 0.3, 0.05);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.3f, 1.0f);
                break;
            }
            case "\u30c1\u30a7\u30a4\u30f3\u30e9\u30a4\u30c8\u30cb\u30f3\u30b0": {
                for (Entity e2 : p.getWorld().getNearbyEntities(p.getLocation(), 10.0, 3.0, 10.0)) {
                    Player t2;
                    if (!(e2 instanceof Player) || !this.gm.isParticipant(t2 = (Player)e2) || this.gm.getTeamOf(t2) == this.gm.getTeamOf(p)) continue;
                    t2.getWorld().strikeLightningEffect(t2.getLocation());
                    t2.damage(7.0, (Entity)p);
                }
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 0.5f);
                break;
            }
            case "\u30c6\u30ec\u30dd\u30fc\u30c8\u30c8\u30e9\u30c3\u30d7": {
                Location tl = p.getLocation();
                TrapData tpTrap = new TrapData(p.getUniqueId(), tl);
                tpTrap.isTeleport = true;
                this.activeTraps.add(tpTrap);
                tl.getWorld().spawnParticle(Particle.PORTAL, tl, 20, 0.5, 0.5, 0.5, 0.05);
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2.0f);
                p.sendMessage("\u00a75\u00a7l\u30c6\u30ec\u30dd\u30fc\u30c8\u30c8\u30e9\u30c3\u30d7\u8a2d\u7f6e\uff01\u00a77\u89e6\u308c\u305f\u6575\u304c\u81ea\u5206\u306e\u5143\u306bTP");
                break;
            }
            case "\u30d5\u30a1\u30f3\u30b0": {
                final Location start = p.getEyeLocation();
                final Vector dir = p.getLocation().getDirection().normalize();
                new BukkitRunnable(){
                    int t = 0;

                    public void run() {
                        if (this.t++ > 20) {
                            this.cancel();
                            return;
                        }
                        Location loc = start.clone().add(dir.clone().multiply(this.t));
                        loc.getWorld().spawn(loc, EvokerFangs.class);
                    }
                }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);
                break;
            }
            case "\u30d4\u30a2\u30c3\u30b7\u30f3\u30b0": {
                if (target == null) break;
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, false, true));
                target.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "kreutz_pierced"), PersistentDataType.BYTE, (byte)1);
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> target.getPersistentDataContainer().remove(new NamespacedKey((Plugin)this.plugin, "kreutz_pierced")), 200L);
                p.sendMessage("\u00a75\u00a7l\u30d4\u30a2\u30c3\u30b7\u30f3\u30b0\uff01\u00a77" + target.getName() + "\u3092\u8cab\u901a\u30de\u30fc\u30af\uff0810\u79d2\uff09");
                target.sendMessage("\u00a75\u00a7l\u8cab\u901a\u30de\u30fc\u30af\u3055\u308c\u3066\u3044\u307e\u3059\uff01");
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 2.0f);
                break;
            }
            default: {
                p.sendMessage("\u00a7c\u00a7l\u30a8\u30e9\u30fc\uff1a\u7121\u52b9\u306a\u30ab\u30fc\u30c9");
            }
        }
    }

    private void vampireToggle(Player p) {
        boolean inBlood = this.vampireBloodMode.getOrDefault(p.getUniqueId(), false) == false;
        this.vampireBloodMode.put(p.getUniqueId(), inBlood);
        double gauge = this.vampireGauge.getOrDefault(p.getUniqueId(), 0.0);
        this.applyVampireStage(p, gauge >= 55.0 ? 4 : (gauge >= 40.0 ? 3 : (gauge >= 25.0 ? 2 : (gauge >= 10.0 ? 1 : 0))));
        p.sendMessage(inBlood ? "\u00a74\u00a7l\u30d6\u30e9\u30c3\u30c9\u30e2\u30fc\u30c9\u8d77\u52d5\uff01" : "\u00a77\u30c9\u30ec\u30a4\u30f3\u30e2\u30fc\u30c9\u306b\u623b\u308a\u307e\u3057\u305f");
    }

    private void bomberSkill(Player p) {
        if (!p.isSneaking()) {
            this.detonateMines(p);
            return;
        }
        Location loc = p.getLocation().getBlock().getLocation().add(0.5, 0.0, 0.5);
        for (MineData existing : new ArrayList<MineData>(this.activeMines)) {
            if (!existing.owner.equals(p.getUniqueId())) continue;
            existing.entity.remove();
            this.activeMines.remove(existing);
        }
        this.setCooldown(p.getUniqueId(), 2000L);
        loc.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(0.0, 0.1, 0.0), 3, 0.1, 0.1, 0.1, 0.01);
        loc.getWorld().playSound(loc, Sound.BLOCK_STONE_PLACE, 0.3f, 0.5f);
        ArmorStand as = (ArmorStand)loc.getWorld().spawn(loc, ArmorStand.class, a -> {
            a.setInvisible(true);
            a.setMarker(true);
            a.setSmall(true);
            a.getPersistentDataContainer().set(this.KEY_MINE, PersistentDataType.BYTE, (byte)1);
        });
        this.activeMines.add(new MineData(as, p.getUniqueId()));
        p.sendMessage("\u00a7c\u00a7l\u5730\u96f7\u8a2d\u7f6e\uff01\u00a77\u53f3\u30af\u30ea\u3067\u8d77\u7206");
    }

    public void detonateMines(Player p) {
        long ready = this.activeMines.stream().filter(m -> m.owner.equals(p.getUniqueId())).count();
        if (ready == 0L) {
            p.sendMessage("\u00a7c\u8d77\u7206\u3067\u304d\u308b\u5730\u96f7\u304c\u3042\u308a\u307e\u305b\u3093\u3002");
            return;
        }
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.5f);
        this.setCooldown(p.getUniqueId(), 7000L);
        p.sendMessage("\u00a7c\u00a7l\u8d77\u7206\uff01");
        for (MineData m2 : new ArrayList<MineData>(this.activeMines)) {
            if (!m2.owner.equals(p.getUniqueId())) continue;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (m2.entity.isValid()) {
                    m2.entity.getLocation().getWorld().playSound(m2.entity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                    Location loc = m2.entity.getLocation();
                    loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1, 0.0, 0.0, 0.0, 0.1);
                    for (Entity e : loc.getWorld().getNearbyEntities(loc, 5.0, 3.0, 5.0)) {
                        Player target;
                        if (!(e instanceof Player) || !this.gm.isParticipant(target = (Player)e) || this.gm.getTeamOf(target) == this.gm.getTeamOf(p)) continue;
                        double dist = loc.distance(target.getLocation());
                        double dmg = 20.0 * Math.max(0.5, 1.0 - dist / 5.0);
                        int slowDuration = (int)(40.0 * Math.max(0.25, 1.0 - dist / 5.0));
                        target.damage(dmg, (Entity)p);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowDuration, 0, false, true));
                    }
                    m2.entity.remove();
                    this.activeMines.remove(m2);
                }
            }, 16L);
        }
    }

    private void gliderSkill(Player p) {
    }

    private void scoutSkill(Player p) {
        if (p.isSneaking()) {
            this.setCooldown(p.getUniqueId(), 8000L);
            p.getPersistentDataContainer().set(this.KEY_PULSE, PersistentDataType.BYTE, (byte)1);
            p.sendMessage("\u00a7c\u00a7l\u30d1\u30eb\u30b9\u30dc\u30eb\u30c8\u6e96\u5099\u5b8c\u4e86\uff01");
        } else {
            this.setCooldown(p.getUniqueId(), 10000L);
            p.getPersistentDataContainer().set(this.KEY_RECON, PersistentDataType.BYTE, (byte)1);
            p.sendMessage("\u00a7a\u00a7l\u30ea\u30b3\u30f3\u30dc\u30eb\u30c8\u6e96\u5099\u5b8c\u4e86\uff01");
        }
    }

    private void flasherSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 10000L);
        Snowball ball = (Snowball)p.launchProjectile(Snowball.class);
        ball.setVelocity(p.getLocation().getDirection().normalize().multiply(1.2));
        ball.setCustomName("flashBang");
        ball.setGlowing(true);
        p.sendMessage("\u00a7b\u00a7l\u30d5\u30e9\u30c3\u30b7\u30e5\u30d0\u30f3\uff01");
    }

    private void marksmanSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 12000L);
        p.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "heavy_bolt"), PersistentDataType.BYTE, (byte)1);
        p.sendMessage("\u00a7c\u00a7l\u30d8\u30f4\u30a3\u30fc\u30dc\u30eb\u30c8\u6e96\u5099\u5b8c\u4e86\uff01");
    }

    private void sundanceSkill(Player p) {
        if (this.sundanceRevolver.containsKey(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u307e\u3060\u30ea\u30dc\u30eb\u30d3\u30f3\u30b0\u4e2d\u3067\u3059\uff01");
            return;
        }
        if (this.isOnCooldown(p.getUniqueId())) {
            long remain = (this.skillCooldowns.get(p.getUniqueId()) - System.currentTimeMillis()) / 1000L;
            p.sendMessage("\u00a7c\u30ea\u30dc\u30eb\u30d3\u30f3\u30b0CT: \u00a77\u6b8b\u308a\u00a7f" + remain + "\u00a77\u79d2");
            return;
        }
        this.sundanceRevolver.put(p.getUniqueId(), 5);
        Long currentCd = this.skillCooldowns.get(p.getUniqueId());
        long newCd = (currentCd != null && currentCd > System.currentTimeMillis() ? currentCd : System.currentTimeMillis()) + 7000L;
        this.skillCooldowns.put(p.getUniqueId(), newCd);
        ItemStack xbow = p.getInventory().getItemInMainHand();
        if (xbow != null && xbow.getType() == Material.CROSSBOW) {
            ItemMeta m = xbow.getItemMeta();
            m.setDisplayName("\u00a7b\u00a7l\u30ea\u30dc\u30eb\u30d3\u30f3\u30b0\u30fb\u30af\u30ed\u30b9\u30dc\u30a6 [5/5]");
            xbow.setItemMeta(m);
            ItemMeta fresh = xbow.getItemMeta();
            if (fresh instanceof CrossbowMeta) {
                CrossbowMeta cm = (CrossbowMeta)fresh;
                cm.setChargedProjectiles(null);
                cm.addChargedProjectile(new ItemStack(Material.ARROW));
                xbow.setItemMeta((ItemMeta)cm);
            }
        }
        p.sendMessage("\u00a7b\u00a7l\u30ea\u30dc\u30eb\u30d3\u30f3\u30b0\u30fb\u30af\u30ed\u30b9\u30dc\u30a6\u8d77\u52d5\uff015\u767a\u81ea\u52d5\u88c5\u586b");
    }

    private void rocketerSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 25000L);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.5f);
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 1.5f);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            final Snowball rocket = (Snowball)p.launchProjectile(Snowball.class);
            rocket.setVelocity(p.getLocation().getDirection().normalize().multiply(1.2));
            rocket.setCustomName("megaRocket");
            rocket.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "mega_rocket"), PersistentDataType.BYTE, (byte)1);
            rocket.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "rocket_origin_x"), PersistentDataType.DOUBLE, p.getLocation().getX());
            rocket.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "rocket_origin_y"), PersistentDataType.DOUBLE, p.getLocation().getY());
            rocket.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "rocket_origin_z"), PersistentDataType.DOUBLE, p.getLocation().getZ());
            rocket.setGlowing(true);
            rocket.setGravity(false);
            p.sendMessage("\u00a7e\u00a7l\u30e1\u30ac\u30ed\u30b1\u30c3\u30c8\u767a\u5c04\uff01");
            new BukkitRunnable(){
                int ticks = 0;

                public void run() {
                    if (this.ticks++ > 20 || !rocket.isValid()) {
                        this.cancel();
                        return;
                    }
                    rocket.getWorld().spawnParticle(Particle.FLAME, rocket.getLocation(), 3, 0.2, 0.2, 0.2, 0.02);
                }
            }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (rocket.isValid()) {
                    this.megaRocketExplode(rocket.getLocation(), p);
                    rocket.remove();
                }
            }, 20L);
        }, 8L);
    }

    private void megaRocketExplode(Location loc, Player shooter) {
        World w = loc.getWorld();
        w.createExplosion(loc, 2.0f, false, false, (Entity)shooter);
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
    }

    private float rocketPower(Location launch, Location explode, float maxPower) {
        double dist = launch.distance(explode);
        return Math.max(maxPower * 0.2f, Math.min(maxPower, (float)(dist / 10.0) * maxPower));
    }

    private void rocketerFire(Player p) {
        if (p.isSneaking()) {
            this.rocketerSkill(p);
        } else {
            this.rocketerMicro(p);
        }
    }

    private void rocketerMicro(final Player p) {
        this.setCooldown(p.getUniqueId(), 8000L);
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 1.5f);
        final Snowball rocket = (Snowball)p.launchProjectile(Snowball.class);
        rocket.setVelocity(p.getLocation().getDirection().normalize().multiply(0.8));
        rocket.setCustomName("microRocket");
        rocket.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "micro_rocket"), PersistentDataType.BYTE, (byte)1);
        rocket.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "rocket_origin_x"), PersistentDataType.DOUBLE, p.getLocation().getX());
        rocket.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "rocket_origin_y"), PersistentDataType.DOUBLE, p.getLocation().getY());
        rocket.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "rocket_origin_z"), PersistentDataType.DOUBLE, p.getLocation().getZ());
        rocket.setGravity(false);
        rocket.setGlowing(true);
        p.sendMessage("\u00a7e\u00a7l\u8a98\u5c0e\u30ed\u30b1\u30c3\u30c8\uff01");
        new BukkitRunnable(){
            int ticks = 0;

            public void run() {
                if (this.ticks++ > 20 || !rocket.isValid()) {
                    this.cancel();
                    return;
                }
                rocket.getWorld().spawnParticle(Particle.FLAME, rocket.getLocation(), 1, 0.1, 0.1, 0.1, 0.01);
                Player nearest = null;
                double nd = 36.0;
                for (Player t : rocket.getWorld().getPlayers()) {
                    double d;
                    if (!SkillManager.this.gm.isParticipant(t) || SkillManager.this.gm.getTeamOf(t) == SkillManager.this.gm.getTeamOf(p) || !((d = t.getLocation().distanceSquared(rocket.getLocation())) < 36.0) || !(d < nd)) continue;
                    nd = d;
                    nearest = t;
                }
                if (nearest != null) {
                    Vector toTarget = nearest.getLocation().toVector().subtract(rocket.getLocation().toVector()).normalize().multiply(0.3);
                    rocket.setVelocity(rocket.getVelocity().add(toTarget).normalize().multiply(0.8));
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (rocket.isValid()) {
                if (rocket.getLocation().distance(p.getLocation()) <= 4.0) {
                    rocket.remove();
                    return;
                }
                float power = this.rocketHitPower(rocket, rocket.getLocation(), 2.0f);
                rocket.getWorld().createExplosion(rocket.getLocation(), 0.0f, false, false, (Entity)p);
                for (Entity e2 : rocket.getLocation().getWorld().getNearbyEntities(rocket.getLocation(), 3.0, 2.0, 3.0)) {
                    Player t;
                    if (!(e2 instanceof Player) || !this.gm.isParticipant(t = (Player)e2) || this.gm.getTeamOf(t) == this.gm.getTeamOf(p)) continue;
                    t.damage((double)(power * 5.0f), (Entity)p);
                    t.setVelocity(t.getLocation().toVector().subtract(rocket.getLocation().toVector()).normalize().multiply(1.5).setY(0.3));
                }
                rocket.remove();
            }
        }, 20L);
    }

    private void alchemistSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 15000L);
        KitBuilder.refillAlchemistPotions(p);
        p.sendMessage("\u00a7d\u00a7l\u518d\u8abf\u5408\uff01");
    }

    private void engineerSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 15000L);
        TeamColor myTeam = this.gm.getTeamOf(p);
        int enemyCount = myTeam == TeamColor.RED ? this.gm.getCtfBlueTeamSize() : this.gm.getCtfRedTeamSize();
        int maxTurrets = Math.max(1, enemyCount);
        if (this.activeTurrets.stream().filter(t -> t.owner.equals(p.getUniqueId())).count() >= (long)maxTurrets) {
            for (TurretData t2 : new ArrayList<TurretData>(this.activeTurrets)) {
                if (!t2.owner.equals(p.getUniqueId())) continue;
                t2.entity.remove();
                this.activeTurrets.remove(t2);
                break;
            }
        }
        Location loc = p.getLocation().add(p.getLocation().getDirection().multiply(2)).add(0, 0.6, 0);
        Skeleton skel = (Skeleton)loc.getWorld().spawn(loc, Skeleton.class, s -> {
            s.setAI(false);
            s.setRemoveWhenFarAway(false);
            s.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
            s.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
            s.getEquipment().setHelmetDropChance(0.0f);
            s.setCustomName("\u00a76[\u30bf\u30ec\u30c3\u30c8]");
            s.setCustomNameVisible(true);
        });
        skel.setAI(true);
        this.activeTurrets.add(new TurretData(skel, p.getUniqueId(), this.gm.getTeamOf(p)));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (skel.isValid()) {
                skel.remove();
                this.activeTurrets.removeIf(t -> t.entity.equals((Object)skel));
            }
        }, 1800L);
        p.sendMessage("\u00a76\u00a7l\u30ec\u30fc\u30b6\u30fc\u30bf\u30ec\u30c3\u30c8\u8a2d\u7f6e\uff01");
    }

    private void trapperSkill(Player p) {
        TrapData oldest;
        this.setCooldown(p.getUniqueId(), 8000L);
        if (this.activeTraps.stream().filter(t -> t.owner.equals(p.getUniqueId())).count() >= 2L && (oldest = (TrapData)this.activeTraps.stream().filter(t -> t.owner.equals(p.getUniqueId())).findFirst().orElse(null)) != null) {
            this.activeTraps.remove(oldest);
        }
        Location loc = p.getLocation().clone();
        this.activeTraps.add(new TrapData(p.getUniqueId(), loc));
        p.sendMessage("\u00a73\u00a7l\u7f60\u8a2d\u7f6e\uff01\u6700\u59272\u500b");
    }

    private void triggerTrap(TrapData t, Player victim) {
        t.triggered = true;
        if (t.isTeleport) {
            Player owner = Bukkit.getPlayer((UUID)t.owner);
            if (victim != null && victim.isOnline() && owner != null && owner.isOnline()) {
                victim.teleport(owner.getLocation());
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                victim.getWorld().spawnParticle(Particle.PORTAL, victim.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.05);
                owner.sendMessage("\u00a75\u00a7l\u30c6\u30ec\u30dd\u30fc\u30c8\u30c8\u30e9\u30c3\u30d7\u767a\u52d5\uff01\u00a77" + victim.getName() + "\u304c\u8ee2\u9001\u3055\u308c\u307e\u3057\u305f");
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, 9, false, true));
                victim.sendMessage("\u00a75\u00a7l\u30c6\u30ec\u30dd\u30fc\u30c8\u30c8\u30e9\u30c3\u30d7\uff01\u00a77\u7f60\u306b\u89e6\u308c\u3066\u8ee2\u9001\u3055\u308c\u305f");
            }
            this.activeTraps.remove(t);
            return;
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            t.loc.getWorld().playSound(t.loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            t.loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, t.loc, 2, 0.5, 0.5, 0.5, 0.1);
            Player trapOwner = Bukkit.getPlayer((UUID)t.owner);
            for (Entity e : t.loc.getWorld().getNearbyEntities(t.loc, 3.0, 2.0, 3.0)) {
                Player tgt;
                if (!(e instanceof Player) || !this.gm.isParticipant(tgt = (Player)e) || this.gm.getTeamOf(tgt) == this.gm.getTeamOf(trapOwner)) continue;
                double dist = t.loc.distance(tgt.getLocation());
                double dmg = 12.0 * Math.max(0.75, 1.0 - dist / 3.0);
                tgt.damage(dmg, (Entity)trapOwner);
            }
            if (victim != null && victim.isOnline()) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 1, false, false));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1, false, false));
                victim.sendMessage("\u00a7c\u7f60\u304c\u7206\u767a\u3057\u305f\uff01");
            }
            this.activeTraps.remove(t);
        }, 6L);
    }

    private void guardianSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 30000L);
        p.setInvulnerable(true);
        this.guardianEndTime.put(p.getUniqueId(), System.currentTimeMillis() + 7000L);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 140, 1, false, false));
        p.sendMessage("\u00a7f\u00a7l\u9244\u58c1\uff017\u79d2\u9593\u7121\u6575");
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            p.setInvulnerable(false);
            this.guardianEndTime.remove(p.getUniqueId());
            p.sendMessage("\u00a77\u9244\u58c1\u7d42\u4e86");
        }, 140L);
    }

    private void medicSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 20000L);
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation(), 10.0, 5.0, 10.0)) {
            Player t;
            if (!(e instanceof Player) || !this.gm.isParticipant(t = (Player)e) || this.gm.getTeamOf(t) != this.gm.getTeamOf(p)) continue;
            t.setHealth(Math.min(t.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(), t.getHealth() + 5.0));
            t.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1, false, false));
            t.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 2, false, false));
            t.getWorld().spawnParticle(Particle.HEART, t.getLocation().add(0.0, 1.5, 0.0), 3, 0.3, 0.3, 0.3, 0.0);
        }
        p.sendMessage("\u00a75\u00a7l\u30d5\u30a3\u30fc\u30eb\u30c9\u30b1\u30a2\uff01");
    }

    private void supporterSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 12000L);
        KitBuilder.refillSupporterPotions(p);
        p.sendMessage("\u00a7a\u00a7l\u518d\u8abf\u9054\uff01");
    }

    private void restrictionerSkill(Player p) {
        PotionEffectType[] effs;
        Player target = this.getTargetInSight(p, 5);
        if (target == null || this.gm.getTeamOf(target) == this.gm.getTeamOf(p)) {
            p.sendMessage("\u00a7c\u5c04\u7a0b\u5185\u306b\u30bf\u30fc\u30b2\u30c3\u30c8\u304c\u3044\u307e\u305b\u3093\u3002");
            return;
        }
        this.setCooldown(p.getUniqueId(), 20000L);
        Location mid = p.getLocation().add(target.getLocation()).multiply(0.5);
        p.teleport(mid);
        target.teleport(mid);
        for (PotionEffectType et : effs = new PotionEffectType[]{PotionEffectType.SLOW, PotionEffectType.WEAKNESS, PotionEffectType.SLOW_DIGGING, PotionEffectType.BLINDNESS}) {
            p.addPotionEffect(new PotionEffect(et, 100, et == PotionEffectType.SLOW_DIGGING ? 100 : (et == PotionEffectType.BLINDNESS ? 0 : 10), false, false));
            target.addPotionEffect(new PotionEffect(et, 100, et == PotionEffectType.SLOW_DIGGING ? 100 : (et == PotionEffectType.BLINDNESS ? 0 : 10), false, false));
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, -10, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, -10, false, false));
        this.deadlockedPlayers.add(p.getUniqueId());
        this.deadlockedPlayers.add(target.getUniqueId());
        p.sendMessage("\u00a78\u00a7l\u30c7\u30c3\u30c9\u30ed\u30c3\u30af\uff01");
        target.sendMessage("\u00a78\u00a7l\u62d8\u675f\u3055\u308c\u305f\uff01");
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            this.deadlockedPlayers.remove(p.getUniqueId());
            this.deadlockedPlayers.remove(target.getUniqueId());
        }, 100L);
    }

    private void placePortalA(Player p) {
        Location loc = p.getLocation().getBlock().getLocation().add(0.5, 0.0, 0.5);
        TeamColor team = this.gm.getTeamOf(p);
        Location oldA = this.portalA.put(p.getUniqueId(), loc);
        if (oldA != null) {
            this.portalBlocks.remove(oldA);
        }
        this.portalBlocks.add(loc.clone());
        this.startPortalParticles(loc, team);
        p.sendMessage("\u00a73\u00a7l\u30dd\u30fc\u30bf\u30ebA\uff08\u5165\u53e3\uff09\u8a2d\u7f6e\uff01");
    }

    private void placePortalB(Player p) {
        if (!this.portalA.containsKey(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u5148\u306b\u5de6\u30af\u30ea\u30c3\u30af\u3067\u30dd\u30fc\u30bf\u30ebA\u3092\u8a2d\u7f6e\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
            return;
        }
        this.setCooldown(p.getUniqueId(), 15000L);
        Location loc = p.getLocation().getBlock().getLocation().add(0.5, 0.0, 0.5);
        TeamColor team = this.gm.getTeamOf(p);
        if (loc.distance(this.portalA.get(p.getUniqueId())) < 3.0) {
            p.sendMessage("\u00a7c\u8fd1\u3059\u304e\u307e\u3059");
            return;
        }
        Location oldB = this.portalB.remove(p.getUniqueId());
        if (oldB != null) {
            this.portalBlocks.remove(oldB);
        }
        this.portalB.put(p.getUniqueId(), loc);
        this.portalBlocks.add(loc.clone());
        this.startPortalParticles(loc, team);
        p.sendMessage("\u00a73\u00a7l\u30dd\u30fc\u30bf\u30ebB\uff08\u51fa\u53e3\uff09\u8a2d\u7f6e\uff01");
    }

    private void startPortalParticles(final Location loc, TeamColor team) {
        Color color = team == TeamColor.RED ? Color.RED : Color.AQUA;
        final Particle.DustOptions dust = new Particle.DustOptions(color, 1.5f);
        new BukkitRunnable(){

            public void run() {
                if (SkillManager.this.portalBlocks.contains(loc) && loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0.5, 1.0, 0.5), 8, 0.4, 0.8, 0.4, 0.1);
                    loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(0.5, 0.3, 0.5), 5, 0.3, 0.3, 0.3, (Object)dust);
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 10L);
    }

    private void mimicSkill(Player p) {
        Player target = this.getTargetInSight(p, 20);
        if (target == null) {
            p.sendMessage("\u00a7c\u5c04\u7a0b\u5185\u306b\u30bf\u30fc\u30b2\u30c3\u30c8\u304c\u3044\u307e\u305b\u3093\u3002");
            return;
        }
        KitType targetKit = this.gm.getPlayerKitType(target.getUniqueId());
        if (targetKit == null || "MIMIC".equals(targetKit.name())) {
            p.sendMessage("\u00a7c\u30b3\u30d4\u30fc\u4e0d\u53ef");
            return;
        }
        this.setCooldown(target.getUniqueId(), 5000L);
        p.sendMessage("\u00a75\u00a7l" + target.getName() + " \u306e\u30b9\u30ad\u30eb\u3092\u30b3\u30d4\u30fc\uff01");
        target.sendMessage("\u00a75\u30b9\u30ad\u30eb\u30b3\u30d4\u30fc\u3055\u308c\u305f\uff01\u30af\u30fc\u30eb\u30bf\u30a4\u30e05\u79d2");
        switch (targetKit) {
            case MARKSMAN: {
                this.mimicCopyMarksman(p, target);
                break;
            }
            case SCOUT: {
                this.mimicCopyScout(p, target);
                break;
            }
            case SUNDANCE: {
                this.mimicCopySundance(p, target);
                break;
            }
            case SNIPER: {
                this.mimicCopySniper(p, target);
                break;
            }
            default: {
                KitType originalKit = this.gm.getPlayerKitType(p.getUniqueId());
                this.gm.setPlayerKit(p.getUniqueId(), targetKit.name());
                this.useKitSkill(p, targetKit.name());
                this.gm.setPlayerKit(p.getUniqueId(), originalKit.name());
            }
        }
        this.setCooldown(p.getUniqueId(), 25000L);
    }

    private void mimicCopyMarksman(Player p, Player target) {
        if (target.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            double cm = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            this.maxHpReduced.putIfAbsent(target.getUniqueId(), cm);
            target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.max(1.0, cm - 10.0));
            if (target.getHealth() > cm - 10.0) {
                target.setHealth(cm - 10.0);
            }
        }
        target.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "hp_reduced"), PersistentDataType.BYTE, (byte)1);
        target.sendMessage("\u00a7c\u00a7lHP\u4e0a\u9650\u304c10\u4f4e\u4e0b\u3057\u307e\u3057\u305f\uff01");
        p.sendMessage("\u00a7c\u00a7l\u30d8\u30f4\u30a3\u30fc\u30dc\u30eb\u30c8\u30b3\u30d4\u30fc\uff01");
    }

    private void mimicCopyScout(Player p, Player target) {
        ArmorStand as = (ArmorStand)target.getWorld().spawn(target.getLocation().add(0.0, 1.0, 0.0), ArmorStand.class, a -> {
            a.setInvisible(true);
            a.setMarker(true);
            a.setCustomName("\u00a7a\ud83d\udd0d \u30ea\u30b3\u30f3");
            a.setCustomNameVisible(true);
        });
        this.activeRecons.add(new ReconData(as, this.gm.getTeamOf(p)));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            as.remove();
            this.activeRecons.removeIf(r -> r.entity.equals((Object)as));
        }, 1200L);
        p.sendMessage("\u00a7a\u30ea\u30b3\u30f3\u8a2d\u7f6e\uff01");
        target.sendMessage("\u00a7c\u00a7l\u30ea\u30b3\u30f3\u3092\u8a2d\u7f6e\u3055\u308c\u305f\uff01");
    }

    private void mimicCopySundance(Player p, Player target) {
        p.sendMessage("\u00a7b\u00a7l\u2744 \u30ea\u30dc\u30eb\u30d0\u30fc6\u9023\u5c04\u30b3\u30d4\u30fc\uff01");
        for (int i = 0; i < 6; ++i) {
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (target.isValid() && p.isOnline()) {
                    target.damage(2.0, (Entity)p);
                    target.getWorld().spawnParticle(Particle.CRIT_MAGIC, target.getLocation().add(0.0, 1.0, 0.0), 5, 0.3, 0.3, 0.3, 0.0);
                }
            }, (long)i * 3L);
        }
    }

    private void mimicCopySniper(Player p, Player target) {
        this.markedForDeath.add(target.getUniqueId());
        p.sendMessage("\u00a7c\u00a7l\u72d9\u6483\u773c: \u00a7f" + target.getName() + " \u00a7c\u3092\u30de\u30fc\u30af\u3057\u307e\u3057\u305f\uff01");
        target.sendMessage("\u00a7c\u00a7l\u26a0 \u72d9\u6483\u3055\u308c\u3066\u3044\u307e\u3059\uff01");
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 0.5f);
    }

    private void swapperSkill(Player p) {
        Player target = this.getTargetInSight(p, 15);
        if (target == null) {
            p.sendMessage("\u00a7c\u5c04\u7a0b\u5185\u306b\u30bf\u30fc\u30b2\u30c3\u30c8\u304c\u3044\u307e\u305b\u3093\u3002");
            return;
        }
        this.setCooldown(p.getUniqueId(), 18000L);
        Location pLoc = p.getLocation().clone();
        Location tLoc = target.getLocation().clone();
        p.teleport(tLoc);
        target.teleport(pLoc);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, 9, false, true));
    }

    private void stickerSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 12000L);
        Snowball ball = (Snowball)p.launchProjectile(Snowball.class);
        ball.setVelocity(p.getLocation().getDirection().normalize().multiply(2.0));
        ball.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "grapple"), PersistentDataType.BYTE, (byte)1);
        p.sendMessage("\u00a73\u00a7l\u30b0\u30e9\u30c3\u30d7\u30eb\uff01");
    }

    private void decoySkill(final Player p) {
        this.setCooldown(p.getUniqueId(), 10000L);
        Location center = p.getLocation();
        ItemStack[] decoyArmor = new ItemStack[]{p.getInventory().getHelmet(), p.getInventory().getChestplate(), p.getInventory().getLeggings(), p.getInventory().getBoots()};
        this.hideArmor(p);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.showArmor(p), 120L);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 0, false, false));
        for (int i = 0; i < 8; ++i) {
            double angle = (double)i * Math.PI / 4.0;
            Location sl = center.clone().add(Math.cos(angle) * 2.0, 0.0, Math.sin(angle) * 2.0);
            Skeleton skel = (Skeleton)center.getWorld().spawn(sl, Skeleton.class, s -> {
                s.setAI(false);
                s.setRemoveWhenFarAway(false);
                s.setSilent(true);
                s.setCustomName(p.getName());
                s.setCustomNameVisible(true);
                s.getEquipment().setHelmet(decoyArmor[0]);
                s.getEquipment().setChestplate(decoyArmor[1]);
                s.getEquipment().setLeggings(decoyArmor[2]);
                s.getEquipment().setBoots(decoyArmor[3]);
                s.getEquipment().setHelmetDropChance(0.0f);
                s.getEquipment().setChestplateDropChance(0.0f);
                s.getEquipment().setLeggingsDropChance(0.0f);
                s.getEquipment().setBootsDropChance(0.0f);
                ItemStack stick = new ItemStack(Material.STICK);
                stick.addEnchantment(Enchantment.KNOCKBACK, 1);
                s.getEquipment().setItemInMainHand(stick);
                s.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999, 9, false, false));
            });
            skel.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "decoy"), PersistentDataType.BYTE, (byte)1);
            skel.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "decoy_owner"), PersistentDataType.STRING, p.getUniqueId().toString());
            final Skeleton finalSkel = skel;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (finalSkel.isValid()) {
                    finalSkel.setAI(true);
                }
            }, 2L);
            new BukkitRunnable(){

                public void run() {
                    Player tp;
                    if (!finalSkel.isValid()) {
                        this.cancel();
                        return;
                    }
                    LivingEntity tgt = finalSkel.getTarget();
                    if (tgt instanceof Player && SkillManager.this.gm.getTeamOf(tp = (Player)tgt) == SkillManager.this.gm.getTeamOf(p)) {
                        finalSkel.setTarget(null);
                    }
                }
            }.runTaskTimer((Plugin)this.plugin, 40L, 40L);
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> skel.remove(), 160L);
        }
        p.sendMessage("\u00a78\u00a7l\u30c7\u30b3\u30a4\u5c55\u958b\uff01");
    }

    private void phantomSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 18000L);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 125, 255, true, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 0, false, false));
        long endTime = System.currentTimeMillis() + 6000L;
        this.phantomEnd.put(p.getUniqueId(), endTime);
        p.sendMessage("\u00a77\u00a7l\u970a\u4f53\u5316\uff016\u79d2\u9593\u900f\u660e\uff0b\u7121\u6575");
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 0.5f);
        this.hideArmor(p);
        UUID uid = p.getUniqueId();
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            Player pl = Bukkit.getPlayer((UUID)uid);
            if (pl != null && this.phantomEnd.containsKey(uid) && System.currentTimeMillis() >= this.phantomEnd.get(uid)) {
                pl.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                pl.removePotionEffect(PotionEffectType.INVISIBILITY);
                pl.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                this.showArmor(pl);
                this.phantomEnd.remove(uid);
                pl.sendMessage("\u00a77\u970a\u4f53\u5316\u306e\u52b9\u679c\u304c\u5207\u308c\u307e\u3057\u305f");
            }
        }, 120L);
    }

    private void anchorSkill(Player p) {
        this.setCooldown(p.getUniqueId(), 20000L);
        this.anchorFields.put(p.getUniqueId(), p.getLocation().clone());
        p.sendMessage("\u00a79\u00a7l\u78c1\u5834\u5c55\u958b\uff0115\u79d2\u9593 \u534a\u5f848m\u306e\u6575\u3092\u6e1b\u901f\uff0b\u5f31\u4f53\u5316\uff0b\u7d99\u7d9a\u30c0\u30e1");
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);
        final UUID uid = p.getUniqueId();
        new BukkitRunnable(){
            int count = 0;

            public void run() {
                if (this.count++ > 7 || !SkillManager.this.anchorFields.containsKey(uid)) {
                    this.cancel();
                    return;
                }
                Location loc = SkillManager.this.anchorFields.get(uid);
                if (loc == null || loc.getWorld() == null) {
                    this.cancel();
                    return;
                }
                for (Player t : loc.getWorld().getPlayers()) {
                    if (!(t.getLocation().distance(loc) <= 8.0) || !SkillManager.this.gm.isParticipant(t) || SkillManager.this.gm.isSpectator(t) || SkillManager.this.gm.getTeamOf(t) == SkillManager.this.gm.getTeam(uid)) continue;
                    t.damage(1.5, (Entity)Bukkit.getPlayer((UUID)uid));
                }
                loc.getWorld().spawnParticle(Particle.PORTAL, loc, 30, 8.0, 1.0, 8.0, 0.02);
            }
        }.runTaskTimer((Plugin)this.plugin, 40L, 40L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            this.anchorFields.remove(p.getUniqueId());
            p.sendMessage("\u00a77\u78c1\u5834\u7d42\u4e86");
        }, 300L);
    }

    private void whirlwindGust(Player p) {
        this.setCooldown(p.getUniqueId(), 10000L);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 60, 1, false, false));
        final Vector dir = p.getLocation().getDirection().normalize();
        final Location start = p.getEyeLocation().add(dir.clone().multiply(2));
        p.sendMessage("\u00a7f\u00a7l\u6c17\u6d41\u7832\uff01");
        new BukkitRunnable(){
            int ticks = 0;
            Location cur = start.clone();

            public void run() {
                if (this.ticks++ > 40) {
                    this.cancel();
                    return;
                }
                this.cur.add(dir.clone().multiply(0.5));
                this.cur.getWorld().spawnParticle(Particle.CLOUD, this.cur, 8, 1.5, 1.0, 1.5, 0.02);
                for (Entity e : this.cur.getWorld().getNearbyEntities(this.cur, 3.0, 3.0, 3.0)) {
                    Player tp;
                    LivingEntity le;
                    Vector push;
                    if (e instanceof Projectile) {
                        Projectile pr = (Projectile)e;
                        push = dir.clone().multiply(0.3);
                        pr.setVelocity(pr.getVelocity().add(push));
                        continue;
                    }
                    if (!(e instanceof LivingEntity) || !(le = (LivingEntity)e).isValid() || e instanceof Player && !SkillManager.this.gm.isParticipant(tp = (Player)e)) continue;
                    push = dir.clone().multiply(0.2);
                    push.setY(Math.min(push.getY(), 0.1));
                    if (this.ticks % 2 != 0) continue;
                    le.setVelocity(push);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
    }

    private void mistralSkill(Player p) {
        if (this.gm.isFlagCarrier(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u65d7\u3092\u6301\u3063\u3066\u3044\u308b\u9593\u306f\u70c8\u98a8\u7832\u3092\u4f7f\u7528\u3067\u304d\u307e\u305b\u3093\uff01");
            return;
        }
        this.setCooldown(p.getUniqueId(), 15000L);
        final Vector dir = p.getLocation().getDirection().normalize();
        final Location start = p.getEyeLocation().add(dir.clone().multiply(1.5));
        final World w = start.getWorld();
        if (w == null) {
            return;
        }
        p.sendMessage("\u00a7f\u00a7l\u70c8\u98a8\u7832\uff01");
        new BukkitRunnable(){
            int ticks = 0;
            Location cur = start.clone();

            public void run() {
                if (this.ticks++ > 100) {
                    this.cancel();
                    return;
                }
                this.cur.add(dir.clone().multiply(0.3));
                w.spawnParticle(Particle.CLOUD, this.cur, 3, 0.3, 0.5, 0.3, 0.1);
                w.playSound(this.cur, Sound.ENTITY_PHANTOM_FLAP, 0.3f, 2.0f);
                for (Entity e : w.getNearbyEntities(this.cur, 1.35, 2.7, 1.35)) {
                    Player tp2;
                    Player tp;
                    LivingEntity le;
                    if (e instanceof Projectile) {
                        Projectile pr = (Projectile)e;
                        pr.setVelocity(dir.clone().multiply(1.5));
                        continue;
                    }
                    if (!(e instanceof LivingEntity) || !(le = (LivingEntity)e).isValid() || e instanceof Player && !SkillManager.this.gm.isParticipant(tp = (Player)e)) continue;
                    Vector push = dir.clone().multiply(0.69);
                    if (e instanceof Player && SkillManager.this.gm.isFlagCarrier((tp2 = (Player)e).getUniqueId())) {
                        push.setY(0);
                    }
                    le.setVelocity(push);
                    le.getWorld().spawnParticle(Particle.SWEEP_ATTACK, le.getLocation().add(0.0, 1.0, 0.0), 1, 0.2, 0.2, 0.2, 0.0);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void superiorMistralSkill(Player p) {
        if (this.gm.isFlagCarrier(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u65d7\u3092\u6301\u3063\u3066\u3044\u308b\u9593\u306f\u7a76\u6975\u70c8\u98a8\u7832\u3092\u4f7f\u7528\u3067\u304d\u307e\u305b\u3093\uff01");
            return;
        }
        this.setCooldown(p.getUniqueId(), 5000L);
        final Vector dir = p.getLocation().getDirection().normalize();
        final Location start = p.getEyeLocation().add(dir.clone().multiply(1.5));
        final World w = start.getWorld();
        if (w == null) {
            return;
        }
        p.sendMessage("\u00a76\u00a7l\ud83c\udf2c \u7a76\u6975\u70c8\u98a8\u7832\uff01");
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_SWOOP, 1.0f, 0.5f);
        new BukkitRunnable(){
            int ticks = 0;
            Location cur = start.clone();

            public void run() {
                if (this.ticks++ > 100) {
                    this.cancel();
                    return;
                }
                this.cur.add(dir.clone().multiply(0.4));
                w.spawnParticle(Particle.CLOUD, this.cur, 12, 1.5, 1.5, 1.5, 0.2);
                w.spawnParticle(Particle.SWEEP_ATTACK, this.cur, 8, 1.5, 1.5, 1.5, 0.1);
                if (this.ticks % 3 == 0) {
                    w.playSound(this.cur, Sound.ENTITY_PHANTOM_FLAP, 0.5f, 0.5f);
                }
                for (Entity e : w.getNearbyEntities(this.cur, 1.5, 1.5, 1.5)) {
                    Player tp;
                    LivingEntity le;
                    if (e instanceof Projectile) {
                        Projectile pr = (Projectile)e;
                        pr.setVelocity(dir.clone().multiply(2.5).add(new Vector(0.0, 0.3, 0.0)));
                        continue;
                    }
                    if (!(e instanceof LivingEntity) || !(le = (LivingEntity)e).isValid() || e instanceof Player && !SkillManager.this.gm.isParticipant(tp = (Player)e)) continue;
                    Vector push = dir.clone().multiply(1.0).setY(0.35);
                    if (e instanceof Player && SkillManager.this.gm.isFlagCarrier(((Player)e).getUniqueId())) {
                        push.setY(0);
                    }
                    le.setVelocity(push);
                    le.getWorld().spawnParticle(Particle.SWEEP_ATTACK, le.getLocation().add(0.0, 1.0, 0.0), 8, 0.5, 0.5, 0.5, 0.1);
                    if (e instanceof Player) {
                        Player t = (Player)e;
                        if (SkillManager.this.gm.getTeamOf(t) != SkillManager.this.gm.getTeamOf(p)) {
                            t.damage(4.0, (Entity)p);
                            t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0, false, true));
                            t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0, false, true));
                        }
                    }
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    public void whirlwindBall(final Player p) {
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.WHIRLWIND) {
            return;
        }
        if (this.isOnCooldown(p.getUniqueId())) {
            Long cd = this.skillCooldowns.get(p.getUniqueId());
            long remain = cd != null ? (cd - System.currentTimeMillis()) / 1000L : 0L;
            p.sendMessage("\u00a7c\u30b9\u30ad\u30eb\u30af\u30fc\u30eb\u30bf\u30a4\u30e0\u4e2d\uff01 \u00a77\u6b8b\u308a\u00a7f" + remain + "\u00a77\u79d2");
            return;
        }
        this.setCooldown(p.getUniqueId(), 7000L);
        final Snowball ball = (Snowball)p.launchProjectile(Snowball.class);
        ball.setVelocity(p.getLocation().getDirection().normalize().multiply(0.8));
        ball.setCustomName("whirlwindBall");
        ball.setGlowing(true);
        p.sendMessage("\u00a7f\u00a7l\u65cb\u98a8\u5f3e\uff01");
        new BukkitRunnable(){
            int ticks = 0;

            public void run() {
                Vector toTarget;
                if (this.ticks++ > 60 || !ball.isValid()) {
                    this.cancel();
                    return;
                }
                ball.getWorld().spawnParticle(Particle.SWEEP_ATTACK, ball.getLocation(), 5, 0.5, 0.5, 0.5, 0.0);
                Player nearest = null;
                double nd = 36.0;
                for (Player t : ball.getWorld().getPlayers()) {
                    double d;
                    if (!SkillManager.this.gm.isParticipant(t) || SkillManager.this.gm.getTeamOf(t) == SkillManager.this.gm.getTeamOf(p) || !((d = t.getLocation().distanceSquared(ball.getLocation())) < 36.0) || !(d < nd)) continue;
                    nd = d;
                    nearest = t;
                }
                if (nearest != null) {
                    nearest.setVelocity(new Vector(0.0, 1.5, 0.0));
                    nearest.damage(4.0, (Entity)p);
                    nearest.sendMessage("\u00a7f\u00a7l\u65cb\u98a8\u5f3e\u304c\u76f4\u6483\uff01");
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60, 2, false, false));
                    ball.remove();
                    this.cancel();
                    return;
                }
                Vector vector = toTarget = nearest != null ? nearest.getLocation().toVector().subtract(ball.getLocation().toVector()).normalize().multiply(0.3) : new Vector();
                if (toTarget.lengthSquared() > 0.0) {
                    ball.setVelocity(ball.getVelocity().add(toTarget).normalize().multiply(0.8));
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
    }

    public void releaserMegaBurst(Player p) {
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.RELEASER) {
            return;
        }
        if (this.releaserMegaUsed.contains(p.getUniqueId())) {
            p.sendMessage("\u00a7c\u8d85\u5927\u89e3\u653e\u306f\u3053\u306e\u30e9\u30a6\u30f3\u30c9\u4f7f\u7528\u6e08\u307f\u3067\u3059\u3002");
            return;
        }
        this.releaserMegaUsed.add(p.getUniqueId());
        Location loc = p.getLocation();
        World w = p.getWorld();
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        w.spawnParticle(Particle.EXPLOSION_HUGE, loc, 8, 2.0, 2.0, 2.0, 0.0);
        for (Entity e : w.getNearbyEntities(loc, 6.0, 3.0, 6.0)) {
            Player t;
            if (!(e instanceof Player) || !this.gm.isParticipant(t = (Player)e) || this.gm.getTeamOf(t) == this.gm.getTeamOf(p)) continue;
            t.damage(8.0, (Entity)p);
            t.setVelocity(t.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(2.5).setY(1.0));
            t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 2, false, true));
            t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2, false, true));
        }
        p.sendMessage("\u00a7e\u00a7l\ud83d\udca2 \u8d85\u5927\u89e3\u653e\uff01");
    }

    private void releaseSkill(Player p) {
        if (this.isOnCooldown(p.getUniqueId())) {
            Long cd = this.skillCooldowns.get(p.getUniqueId());
            long remain = cd != null ? (cd - System.currentTimeMillis()) / 1000L : 0L;
            p.sendMessage("\u00a7c\u30ea\u30ea\u30fc\u30b9CT\u4e2d\uff01 \u00a77\u6b8b\u308a\u00a7f" + remain + "\u00a77\u79d2");
            return;
        }
        this.setCooldown(p.getUniqueId(), 8000L);
        Location loc = p.getLocation();
        World w = p.getWorld();
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.7f);
        w.spawnParticle(Particle.EXPLOSION_LARGE, loc, 5, 1.5, 1.0, 1.5, 0.05);
        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0, false, true));
        for (Entity e : w.getNearbyEntities(loc, 6.0, 2.0, 6.0)) {
            Player t;
            if (!(e instanceof Player) || !this.gm.isParticipant(t = (Player)e) || this.gm.getTeamOf(t) == this.gm.getTeamOf(p)) continue;
            t.damage(4.0, (Entity)p);
            t.setVelocity(t.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.5).setY(0.4));
            t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1, false, true));
        }
        p.sendMessage("\u00a7e\u30ea\u30ea\u30fc\u30b9\uff01");
    }

    public void theosPadaAction(Player p, boolean isRightClick) {
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.VAMPIRE) {
            return;
        }
        double gauge = this.vampireGauge.getOrDefault(p.getUniqueId(), 0.0);
        if (isRightClick) {
            if (this.isOnCooldown(p.getUniqueId())) {
                return;
            }
            this.setCooldown(p.getUniqueId(), 7000L);
            Snowball ball = (Snowball)p.launchProjectile(Snowball.class);
            ball.setVelocity(p.getLocation().getDirection().normalize().multiply(1.2));
            ball.setCustomName("theos_pada");
            ball.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "theos_pada"), PersistentDataType.BYTE, (byte)1);
            p.sendMessage("\u00a74\u00a7l\u30c6\u30aa\u30b9\u30d1\u30fc\u30c0\uff08\u5438\u53ce\u5f3e\uff09\uff01");
        } else {
            if (gauge < 5.0) {
                p.sendMessage("\u00a7c\u5438\u8840\u30b2\u30fc\u30b8\u304c\u8db3\u308a\u307e\u305b\u3093\uff08\u5fc5\u8981:5\uff09");
                return;
            }
            this.vampireGauge.put(p.getUniqueId(), gauge - 5.0);
            Player target = this.getTargetInSight(p, 30);
            if (target == null) {
                p.sendMessage("\u00a7c\u5c04\u7a0b\u5185\u306b\u30bf\u30fc\u30b2\u30c3\u30c8\u304c\u3044\u307e\u305b\u3093\u3002");
                return;
            }
            target.damage(5.0, (Entity)p);
            target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.3, 0.3, 0.1);
            p.sendMessage("\u00a74\u00a7l\u30c6\u30aa\u30b9\u30d1\u30fc\u30c0\uff08\u7834\u58ca\u5149\u7dda\uff09\uff01\u00a77\u30b2\u30fc\u30b8-5");
            target.sendMessage("\u00a74\u00a7l\u30c6\u30aa\u30b9\u30d1\u30fc\u30c0\u304c\u76f4\u6483\uff01");
        }
    }

    public void onComboHit(Player attacker, Player victim) {
        if (victim.isBlocking()) {
            return;
        }
        if (this.comboCount.getOrDefault(victim.getUniqueId(), 0) > 0) {
            int victimCombo = this.comboCount.remove(victim.getUniqueId());
            this.comboLastHit.remove(victim.getUniqueId());
            attacker.showTitle(Title.title((Component)Component.empty(), (Component)Component.text((String)"\u00a7c\u00a7l\u2015\u2015COUNTER                    "), (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofMillis(1500L), (Duration)Duration.ofMillis(500L))));
            attacker.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, attacker.getLocation().add(0.0, 1.0, 0.0), 3, 0.5, 0.5, 0.5, 0.1);
            attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 2.0f);
        }
        this.comboLastHit.put(attacker.getUniqueId(), System.currentTimeMillis());
        int combo = this.comboCount.merge(attacker.getUniqueId(), 1, Integer::sum);
        if (combo >= 2) {
            String comboText = "\u00a7e\u00a7l" + combo + " COMBO !                    ";
            attacker.showTitle(Title.title((Component)Component.empty(), (Component)Component.text((String)comboText), (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofMillis(1000L), (Duration)Duration.ofMillis(300L))));
            int particleCount = Math.min(combo * 2, 30);
            attacker.getWorld().spawnParticle(Particle.FLAME, attacker.getLocation().add(0.0, 1.0, 0.0), particleCount, 0.3, 0.3, 0.3, 0.02);
            if (combo >= 5) {
                attacker.getWorld().spawnParticle(Particle.SPELL_WITCH, attacker.getLocation().add(0.0, 2.0, 0.0), 10, 0.5, 0.5, 0.5, 0.05);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            }
            if (combo >= 10) {
                attacker.getWorld().spawnParticle(Particle.PORTAL, attacker.getLocation().add(0.0, 1.0, 0.0), 20, 0.8, 1.0, 0.8, 0.05);
                attacker.getWorld().playSound(attacker.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
        }
    }

    private boolean isSword(ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.getType() == Material.WOODEN_SWORD || item.getType() == Material.STONE_SWORD || item.getType() == Material.IRON_SWORD || item.getType() == Material.DIAMOND_SWORD;
    }

    private void cookGenerateFood(Player p) {
        if (this.isOnCooldown(p.getUniqueId())) {
            return;
        }
        int existing = 0;
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null || item.getItemMeta() == null || !item.getItemMeta().getPersistentDataContainer().has(this.KEY_COOK, PersistentDataType.STRING)) continue;
            existing += item.getAmount();
        }
        if (existing >= 3) {
            p.sendMessage("\u00a7c\u98df\u6750\u306f\u6700\u59273\u500b\u307e\u3067\u3057\u304b\u6301\u3066\u307e\u305b\u3093\uff01\u30b9\u30ad\u30eb\u661f\u3067\u6d88\u8cbb\u3057\u3066\u304f\u3060\u3055\u3044");
            return;
        }
        this.setCooldown(p.getUniqueId(), 1000L);
        Material[] all = new Material[]{Material.COOKED_BEEF, Material.COOKED_CHICKEN, Material.GOLDEN_CARROT, Material.COOKED_PORKCHOP, Material.PUMPKIN_PIE, Material.BREAD, Material.HONEY_BOTTLE, Material.BEETROOT_SOUP, Material.ROTTEN_FLESH, Material.SPIDER_EYE, Material.POISONOUS_POTATO, Material.PUFFERFISH, Material.CHICKEN, Material.PORKCHOP, Material.BEEF, Material.MUTTON, Material.COD, Material.SALMON};
        Material mat = all[new Random().nextInt(all.length)];
        ItemStack food = new ItemStack(mat);
        ItemMeta meta = food.getItemMeta();
        meta.getPersistentDataContainer().set(this.KEY_COOK, PersistentDataType.STRING, mat.name());
        meta.setDisplayName(this.getCookLabel(mat));
        meta.setLore(List.of("\u00a77\u30b9\u30ad\u30eb\u661f\u3067\u4f7f\u7528"));
        food.setItemMeta(meta);
        p.getInventory().addItem(new ItemStack[]{food});
        p.sendMessage("\u00a76" + this.getCookLabel(mat) + " \u00a76\u3092\u624b\u306b\u5165\u308c\u305f\uff01");
    }

    private void cookSkill(Player p) {
        List<Material> foods = this.findAllCookFoods(p);
        if (foods.isEmpty()) {
            p.sendMessage("\u00a7c\u98df\u6750\u3092\u6301\u3063\u3066\u3044\u307e\u305b\u3093\uff01\u5263\u3092\u53f3\u30af\u30ea\u30c3\u30af\u3067\u5165\u624b");
            return;
        }
        this.setCooldown(p.getUniqueId(), 1000L);
        for (Material mat : foods) {
            this.applyCookBuff(p, mat);
        }
        p.sendMessage("\u00a76\u00a7l\u98df\u6750" + foods.size() + "\u7a2e\u3092\u4e00\u6c17\u306b\u4f7f\u7528\uff01\uff08\u81ea\u5206\uff09");
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.2f);
    }

    private void cookThrowFood(Player p) {
        List<Material> foods = this.findAllCookFoods(p);
        if (foods.isEmpty()) {
            p.sendMessage("\u00a7c\u98df\u6750\u3092\u6301\u3063\u3066\u3044\u307e\u305b\u3093\uff01\u5263\u3092\u53f3\u30af\u30ea\u30c3\u30af\u3067\u5165\u624b");
            return;
        }
        this.setCooldown(p.getUniqueId(), 1000L);
        int count = foods.size();
        for (Material mat : foods) {
            Snowball ball = (Snowball)p.launchProjectile(Snowball.class);
            ball.setVelocity(p.getLocation().getDirection().normalize().multiply(1.5).add(new Vector(Math.random() * 0.4 - 0.2, Math.random() * 0.2, Math.random() * 0.4 - 0.2)));
            ball.setCustomName("cookThrow");
            ball.getPersistentDataContainer().set(this.KEY_COOK, PersistentDataType.STRING, mat.name());
        }
        p.sendMessage("\u00a76\u00a7l\u98df\u6750" + count + "\u7a2e\u3092\u307e\u3068\u3081\u3066\u6295\u3052\u305f\uff01");
    }

    private List<Material> findAllCookFoods(Player p) {
        ArrayList<Material> foods = new ArrayList<Material>();
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null || item.getItemMeta() == null || !item.getItemMeta().getPersistentDataContainer().has(this.KEY_COOK, PersistentDataType.STRING)) continue;
            String matName = (String)item.getItemMeta().getPersistentDataContainer().get(this.KEY_COOK, PersistentDataType.STRING);
            foods.add(Material.valueOf((String)matName));
            item.setAmount(0);
        }
        return foods;
    }

    private String getCookLabel(Material mat) {
        return switch (mat) {
            case COOKED_BEEF -> "\u00a76\u30b9\u30c6\u30fc\u30ad \u00a77[\u00a7c\u529bI 13s\u00a77]";
            case COOKED_CHICKEN -> "\u00a7f\u9d8f\u8089 \u00a77[\u00a7b\u901f\u5ea6II 13s\u00a77]";
            case GOLDEN_CARROT -> "\u00a7e\u91d1\u30cb\u30f3\u30b8\u30f3 \u00a77[\u00a7d\u518d\u751fII 11s\u00a77]";
            case COOKED_PORKCHOP -> "\u00a7d\u8c5a\u8089 \u00a77[\u00a77\u8010\u6027II 13s\u00a77]";
            case PUMPKIN_PIE -> "\u00a76\u30d1\u30f3\u30d7\u30ad\u30f3\u30d1\u30a4 \u00a77[\u00a7e\u5438\u53ceIII 23s\u00a77]";
            case BREAD -> "\u00a7e\u30d1\u30f3 \u00a77[\u00a76\u6e80\u8179\u56de\u5fa9\u00a77]";
            case HONEY_BOTTLE -> "\u00a76\u30cf\u30c1\u30df\u30c4 \u00a77[\u00a7a\u8df3\u8e8dII 11s\u00a77]";
            case BEETROOT_SOUP -> "\u00a7c\u30d3\u30fc\u30c8\u30eb\u30fc\u30c8\u30b9\u30fc\u30d7 \u00a77[\u00a77\u8010\u706b 23s\u00a77]";
            case ROTTEN_FLESH -> "\u00a78\u8150\u8089 \u00a77[\u00a7c\u7a7a\u8179II 10s\u00a77]";
            case SPIDER_EYE -> "\u00a75\u8718\u86db\u306e\u76ee \u00a77[\u00a72\u6bd2II 8s\u00a77]";
            case POISONOUS_POTATO -> "\u00a7a\u9752\u304f\u306a\u3063\u305f\u30b8\u30e3\u30ac\u30a4\u30e2 \u00a77[\u00a7d\u5410\u6c17 10s\u00a77]";
            case PUFFERFISH -> "\u00a7e\u30d5\u30b0 \u00a77[\u00a78\u8870\u5f31+\u5410\u6c17 5s\u00a77]";
            case CHICKEN -> "\u00a7f\u751f\u9d8f\u8089 \u00a77[\u00a77\u5f31\u4f53\u5316II 10s\u00a77]";
            case PORKCHOP -> "\u00a7d\u751f\u8c5a\u8089 \u00a77[\u00a77\u920d\u8db3II 10s\u00a77]";
            case BEEF -> "\u00a7c\u751f\u725b\u8089 \u00a77[\u00a78\u63a1\u6398\u4f4e\u4e0bII 15s\u00a77]";
            case MUTTON -> "\u00a75\u751f\u7f8a\u8089 \u00a77[\u00a70\u76f2\u76ee 3s\u00a77]";
            case COD -> "\u00a7b\u751f\u9c48 \u00a77[\u00a74\u5373\u6642\u30c0\u30e1 4\u2764\u00a77]";
            case SALMON -> "\u00a7d\u751f\u9bad \u00a77[\u00a7f\u6d6e\u904a 3s\u00a77]";
            default -> "\u00a77\u6599\u7406";
        };
    }

    private void applyCookBuff(Player p, Material mat) {
        switch (mat) {
            case COOKED_BEEF: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 260, 0, false, true));
                break;
            }
            case COOKED_CHICKEN: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 260, 1, false, true));
                break;
            }
            case GOLDEN_CARROT: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 220, 1, false, true));
                break;
            }
            case COOKED_PORKCHOP: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 260, 1, false, true));
                break;
            }
            case PUMPKIN_PIE: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 460, 2, false, true));
                break;
            }
            case BREAD: {
                p.setFoodLevel(20);
                p.setSaturation(10.0f);
                break;
            }
            case HONEY_BOTTLE: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 220, 1, false, true));
                break;
            }
            case BEETROOT_SOUP: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 460, 0, false, true));
                break;
            }
            case ROTTEN_FLESH: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 200, 1, false, true));
                break;
            }
            case SPIDER_EYE: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 160, 1, false, true));
                break;
            }
            case POISONOUS_POTATO: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0, false, true));
                break;
            }
            case PUFFERFISH: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0, false, true));
                break;
            }
            case CHICKEN: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1, false, true));
                break;
            }
            case PORKCHOP: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 1, false, true));
                break;
            }
            case BEEF: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 300, 1, false, true));
                break;
            }
            case MUTTON: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, true));
                break;
            }
            case COD: {
                p.damage(8.0);
                break;
            }
            case SALMON: {
                p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 0, false, true));
            }
        }
    }

    private void updateVampireStage(Player p, double gauge) {
        int stage = gauge >= 55.0 ? 4 : (gauge >= 40.0 ? 3 : (gauge >= 25.0 ? 2 : (gauge >= 10.0 ? 1 : 0)));
        this.applyVampireStage(p, stage);
    }

    private void applyVampireStage(Player p, int stage) {
        if (p == null) {
            return;
        }
        boolean inBlood = this.vampireBloodMode.getOrDefault(p.getUniqueId(), false);
        if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            double hp = switch (stage) {
                case 4 -> 40.0;
                case 3 -> 26.0;
                case 2 -> 20.0;
                case 1 -> 16.0;
                default -> 14.0;
            };
            p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
            if (p.getHealth() > hp) {
                p.setHealth(hp);
            }
        }
        p.removePotionEffect(PotionEffectType.WEAKNESS);
        p.removePotionEffect(PotionEffectType.SLOW);
        p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        p.removePotionEffect(PotionEffectType.REGENERATION);
        p.removePotionEffect(PotionEffectType.SPEED);
        if (inBlood && stage == 4) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2, false, false));
            if (!this.gm.isFlagCarrier(p.getUniqueId())) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
            }
        } else if (inBlood && stage >= 2) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, false, false));
        } else if (!inBlood && stage <= 1) {
            if (stage == 0) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 0, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, false, false));
            } else {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, false, false));
            }
        }
    }

    private void necroSkill(Player p) {
        if (this.gm.getPlayerKitType(p.getUniqueId()) != KitType.NECRO) {
            return;
        }
        List<Skeleton> army = this.necroArmy.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<Skeleton>());
        if (army.isEmpty()) {
            this.spawnNecroArmy(p, army);
            return;
        }
        if (p.isSneaking()) {
            for (Skeleton s : army) {
                if (!s.isValid()) continue;
                s.getPathfinder().moveTo(p.getLocation(), 1.2);
            }
            p.sendMessage("\u00a78\u90e8\u968a\u547c\u3073\u623b\u3057\u4e2d...");
        } else {
            Block targetBlock = p.getTargetBlockExact(30, FluidCollisionMode.NEVER);
            if (targetBlock == null) {
                p.sendMessage("\u00a7c\u76ee\u6a19\u5730\u70b9\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                return;
            }
            Location targetLoc = targetBlock.getLocation().add(0.5, 0.0, 0.5);
            for (Skeleton s : army) {
                if (!s.isValid()) continue;
                s.getPathfinder().moveTo(targetLoc, 1.2);
            }
            p.sendMessage("\u00a78\u90e8\u968a\u79fb\u52d5\u4e2d...");
        }
    }

    private void spawnNecroArmy(final Player p, List<Skeleton> army) {
        TeamColor enemyTeam = this.gm.getTeamOf(p) == TeamColor.RED ? TeamColor.BLUE : TeamColor.RED;
        Location base = p.getLocation();
        for (int i = 0; i < 3; ++i) {
            Skeleton skel;
            Location spawnLoc = base.clone().add((double)((i - 1) * 2), 0.0, 0.0);
            int idx = i;
            Skeleton finalSkel = skel = (Skeleton)p.getWorld().spawn(spawnLoc, Skeleton.class, s -> {
                s.setAI(false);
                s.setRemoveWhenFarAway(false);
                if (idx == 0) {
                    s.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
                } else {
                    s.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
                }
                s.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                s.getEquipment().setHelmetDropChance(0.0f);
                s.getEquipment().setItemInMainHandDropChance(0.0f);
                s.setCustomName("\u00a78[" + p.getName() + "\u306e\u5175]");
                s.setCustomNameVisible(true);
            });
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (!finalSkel.isValid()) {
                    return;
                }
                finalSkel.setAI(true);
                for (Player target : p.getWorld().getPlayers()) {
                    if (this.gm.getTeamOf(target) != enemyTeam || !this.gm.isParticipant(target) || this.gm.isSpectator(target)) continue;
                    finalSkel.setTarget((LivingEntity)target);
                    break;
                }
            }, 2L);
            army.add(skel);
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            String teamName = "necro_" + (this.gm.getTeamOf(p) == TeamColor.RED ? "red" : "blue");
            Team team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
                team.setAllowFriendlyFire(false);
            }
            team.addEntry(skel.getUniqueId().toString());
        }
        p.sendMessage("\u00a78\u00a7l\ud83d\udc80 \u30b9\u30b1\u30eb\u30c8\u30f33\u90e8\u968a\u3092\u53ec\u559a\uff01");
        new BukkitRunnable(){

            public void run() {
                if (!p.isOnline() || SkillManager.this.gm.getState() != GameState.IN_GAME) {
                    this.cancel();
                    return;
                }
                List<Skeleton> current = SkillManager.this.necroArmy.get(p.getUniqueId());
                if (current == null) {
                    this.cancel();
                    return;
                }
                for (int i = current.size() - 1; i >= 0; --i) {
                    if (current.get(i).isValid()) continue;
                    current.remove(i);
                }
                while (current.size() < 3) {
                    Location spawnLoc = p.getLocation().clone().add(Math.random() * 4.0 - 2.0, 0.0, Math.random() * 4.0 - 2.0);
                    boolean hasBow = current.stream().anyMatch(s -> s.isValid() && s.getEquipment().getItemInMainHand().getType() == Material.BOW);
                    boolean needBow = !hasBow;
                    Skeleton skel = (Skeleton)p.getWorld().spawn(spawnLoc, Skeleton.class, s -> {
                        s.setAI(false);
                        s.setRemoveWhenFarAway(false);
                        if (needBow) {
                            s.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
                        } else {
                            s.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
                        }
                        s.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                        s.getEquipment().setHelmetDropChance(0.0f);
                        s.setCustomName("\u00a78[" + p.getName() + "\u306e\u5175]");
                        s.setCustomNameVisible(true);
                    });
                    current.add(skel);
                    Skeleton finalSkel = skel;
                    Bukkit.getScheduler().runTaskLater((Plugin)SkillManager.this.plugin, () -> {
                        if (!finalSkel.isValid()) {
                            return;
                        }
                        finalSkel.setAI(true);
                        TeamColor et = SkillManager.this.gm.getTeamOf(p) == TeamColor.RED ? TeamColor.BLUE : TeamColor.RED;
                        for (Player target : p.getWorld().getPlayers()) {
                            if (SkillManager.this.gm.getTeamOf(target) != et || !SkillManager.this.gm.isParticipant(target) || SkillManager.this.gm.isSpectator(target)) continue;
                            finalSkel.setTarget((LivingEntity)target);
                            break;
                        }
                    }, 2L);
                    Scoreboard board2 = Bukkit.getScoreboardManager().getMainScoreboard();
                    String teamName2 = "blox_" + (SkillManager.this.gm.getTeamOf(p) == TeamColor.RED ? "red" : "blue");
                    Team team2 = board2.getTeam(teamName2);
                    if (team2 == null) {
                        team2 = board2.registerNewTeam(teamName2);
                        team2.setAllowFriendlyFire(false);
                    }
                    team2.addEntry(skel.getUniqueId().toString());
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 400L, 400L);
    }

    private void bulwarkSkill(Player p) {
        UUID uid = p.getUniqueId();
        if (this.activeWalls.containsKey(uid)) {
            long placedTime = this.wallPlacedTime.getOrDefault(uid, System.currentTimeMillis());
            long elapsed = (System.currentTimeMillis() - placedTime) / 1000L;
            if (elapsed >= 8L) {
                this.setCooldown(uid, 10000L);
            }
            this.removeWall(uid);
            p.sendMessage("\u00a77[\u30d6\u30eb\u30ef\u30fc\u30af] \u58c1\u3092\u89e3\u9664\u3057\u307e\u3057\u305f");
            return;
        }
        this.setCooldown(uid, 20000L);
        World w = p.getWorld();
        if (w == null) {
            return;
        }
        TeamColor team = this.gm.getTeamOf(p);
        Material mat = team == TeamColor.RED ? Material.RED_TERRACOTTA : Material.LIGHT_BLUE_TERRACOTTA;
        Vector dir = p.getLocation().getDirection().setY(0).normalize();
        if (dir.lengthSquared() < 0.01) {
            dir = new Vector(0.0, 0.0, 1.0);
        }
        Location base = p.getLocation().getBlock().getLocation().clone().add(0.5, 0.0, 0.5).add(dir.clone().multiply(2.0));
        Vector side = dir.clone().crossProduct(new Vector(0.0, 1.0, 0.0)).normalize();
        List<Location> placed = new ArrayList<Location>();
        if (p.isSneaking()) {
            for (int dx = -2; dx <= 2; ++dx) {
                for (int dz = -2; dz <= 2; ++dz) {
                    Location loc = base.clone().add(side.clone().multiply(dx)).add(dir.clone().multiply(dz));
                    if (this.canPlaceWall(loc, p)) {
                        loc.getBlock().setType(mat);
                        placed.add(loc.clone());
                    }
                }
            }
            p.sendMessage("\u00a77[\u30d6\u30eb\u30ef\u30fc\u30af] \u5e73\u9762\u5c55\u958b\uff01");
        } else {
            for (int dx = -2; dx <= 2; ++dx) {
                for (int dy = 0; dy <= 2; ++dy) {
                    Location loc = base.clone().add(side.clone().multiply(dx)).add(0.0, (double)dy, 0.0);
                    if (this.canPlaceWall(loc, p)) {
                        loc.getBlock().setType(mat);
                        placed.add(loc.clone());
                    }
                }
            }
            p.sendMessage("\u00a77[\u30d6\u30eb\u30ef\u30fc\u30af] \u30a6\u30a9\u30fc\u30eb\u5c55\u958b\uff01");
        }
        this.activeWalls.put(uid, placed);
        this.wallPlacedTime.put(uid, System.currentTimeMillis());
        w.playSound(p.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.removeWall(uid), 200L);
    }

    private boolean canPlaceWall(Location loc, Player p) {
        if (loc.getBlock().getType() != Material.AIR) {
            return false;
        }
        for (Entity e : loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
            if (e instanceof Player && this.gm.isParticipant((Player)e)) {
                return false;
            }
        }
        MapConfig map = this.gm.getCurrentMap();
        if (map != null && map.getCenter() != null) {
            Location c = map.getCenter();
            if (c.getWorld() == loc.getWorld() && Math.abs(loc.getX() - c.getX()) < 4.0 && Math.abs(loc.getZ() - c.getZ()) < 4.0 && Math.abs(loc.getY() - c.getY()) < 4.0) {
                return false;
            }
        }
        return true;
    }

    private void removeWall(UUID uid) {
        List<Location> blocks = this.activeWalls.remove(uid);
        this.wallPlacedTime.remove(uid);
        if (blocks != null) {
            for (Location loc : blocks) {
                if (loc.getWorld() != null && loc.getBlock().getType().name().endsWith("_TERRACOTTA")) {
                    loc.getBlock().setType(Material.AIR);
                }
            }
        }
    }

    private void timekeeperSkill(Player p) {
        if (this.isOnCooldown(p.getUniqueId())) {
            return;
        }
        this.setCooldown(p.getUniqueId(), 30000L);
        Deque<Snapshot> snaps = this.timeSnapshots.get(p.getUniqueId());
        if (snaps == null || snaps.isEmpty()) {
            p.sendMessage("\u00a77[\u30bf\u30a4\u30e0\u30ad\u30fc\u30d1\u30fc] \u30ea\u30ef\u30a4\u30f3\u30c9\u3059\u308b\u30b9\u30ca\u30c3\u30d7\u30b7\u30e7\u30c3\u30c8\u304c\u3042\u308a\u307e\u305b\u3093");
            return;
        }
        Snapshot snap = snaps.peekLast();
        if (snap == null || snap.loc == null || snap.loc.getWorld() == null) {
            p.sendMessage("\u00a77[\u30bf\u30a4\u30e0\u30ad\u30fc\u30d1\u30fc] \u30ea\u30ef\u30a4\u30f3\u30c9\u30dd\u30a4\u30f3\u30c8\u304c\u7121\u52b9\u3067\u3059");
            return;
        }
        p.teleport(snap.loc.clone());
        double max = p.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null ? p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() : 20.0;
        p.setHealth(Math.min(max + 10.0, snap.health));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 1.0, 0.5, 0.1);
        p.sendMessage("\u00a7b\u00a7l\u30ea\u30ef\u30a4\u30f3\u30c9\uff01");
    }

    private void timekeeperClockStop(Player p) {
        if (this.isOnCooldown(p.getUniqueId())) {
            return;
        }
        this.setCooldown(p.getUniqueId(), 15000L);
        World w = p.getWorld();
        List<Entity> toFreeze = new ArrayList<Entity>();
        for (Entity e : w.getNearbyEntities(p.getLocation(), 6.0, 6.0, 6.0)) {
            if (e instanceof Projectile && e.isValid()) {
                toFreeze.add(e);
            }
        }
        for (Entity e : toFreeze) {
            this.frozenProjectiles.add(e);
        }
        UUID uid = p.getUniqueId();
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            for (Entity e : toFreeze) {
                if (e.isValid()) {
                    this.frozenProjectiles.remove(e);
                    e.setVelocity(e.getVelocity().multiply(1.5));
                } else {
                    this.frozenProjectiles.remove(e);
                }
            }
        }, 100L);
        w.spawnParticle(Particle.SPELL_INSTANT, p.getLocation().add(0.0, 1.0, 0.0), 30, 6.0, 3.0, 6.0, 0.1);
        w.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.5f);
        p.sendMessage("\u00a7b\u00a7l\u30af\u30ed\u30c3\u30af\u30b9\u30c8\u30c3\u30d7\uff01\u00a77\u5468\u56f26m\u306e\u5f3e\u306e\u6642\u9593\u3092\u6b62\u3081\u305f");
    }

    private void aegisSkill(Player p) {
        if (this.isOnCooldown(p.getUniqueId())) {
            return;
        }
        Player ally = null;
        double best = 225.0;
        for (Entity e : p.getNearbyEntities(15.0, 15.0, 15.0)) {
            Player t;
            if (!(e instanceof Player) || !this.gm.isParticipant(t = (Player)e) || t == p || this.gm.getTeamOf(t) != this.gm.getTeamOf(p)) continue;
            double d = p.getLocation().distanceSquared(t.getLocation());
            if (d > 225.0 || d >= best) continue;
            best = d;
            ally = t;
        }
        if (ally == null) {
            p.sendMessage("\u00a7c\u30dc\u30f3\u30c9\u3067\u304d\u308b\u53cb\u65b9\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\uff08\u534a\u5f8415m\u5185\uff09");
            return;
        }
        this.setCooldown(p.getUniqueId(), 22000L);
        long endTime = System.currentTimeMillis() + 8000L;
        this.activeBonds.put(ally.getUniqueId(), new Bond(p.getUniqueId(), endTime));
        p.sendMessage("\u00a7f\u00a7l\u30ac\u30fc\u30c7\u30a3\u30a2\u30f3\u30dc\u30f3\u30c9\uff01 \u00a77" + ally.getName() + " \u306e\u53d7\u3051\u305f\u30c0\u30e1\u30fc\u30b8\u306e50%\u3092\u80a9\u4ee3\u308f\u308a\u3057\u307e\u3059\uff088\u79d2\u9593\uff09");
        ally.sendMessage("\u00a7f" + p.getName() + " \u304c\u3042\u306a\u305f\u306b\u30ac\u30fc\u30c7\u30a3\u30a2\u30f3\u30dc\u30f3\u30c9\u3092\u7d50\u3093\u3067\u3044\u307e\u3059\uff088\u79d2\u9593\uff09");
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
    }

    public Player getBondOwner(UUID victimUid) {
        Bond bond = this.activeBonds.get(victimUid);
        if (bond == null || System.currentTimeMillis() > bond.endTime) {
            this.activeBonds.remove(victimUid);
            return null;
        }
        Player owner = Bukkit.getPlayer(bond.ownerUid);
        if (owner == null) {
            return null;
        }
        Player victim = Bukkit.getPlayer(victimUid);
        if (victim != null && owner.getWorld() == victim.getWorld() && owner.getLocation().distance(victim.getLocation()) > 15.0) {
            this.activeBonds.remove(victimUid);
            return null;
        }
        return owner;
    }

    private void hexerSkill(Player p) {
        if (this.isOnCooldown(p.getUniqueId())) {
            return;
        }
        this.setCooldown(p.getUniqueId(), 24000L);
        Vector dir = p.getLocation().getDirection().normalize();
        Location center = p.getEyeLocation().clone().add(dir.clone().multiply(5.0));
        center.setY(p.getLocation().getY());
        this.activeHexFields.add(new HexField(center.clone(), p.getUniqueId(), System.currentTimeMillis() + 12000L));
        p.sendMessage("\u00a75\u00a7l\u546a\u7e1b\u9818\u57df\uff01\u00a77\u534a\u5f845m\u5185\u306e\u6575\u306f\u30b9\u30ad\u30eb\u30fb\u30d0\u30fc\u30b9\u30c8\u4f7f\u7528\u4e0d\u53ef\uff0812\u79d2\u9593\uff09");
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
    }

    private boolean inEnemyHexField(Player p) {
        for (HexField field : this.activeHexFields) {
            if (System.currentTimeMillis() > field.endTime) {
                continue;
            }
            if (field.owner.equals(p.getUniqueId())) {
                continue;
            }
            TeamColor ownerTeam = this.gm.getTeam(field.owner);
            if (ownerTeam != null && ownerTeam == this.gm.getTeamOf(p)) {
                continue;
            }
            if (field.center.getWorld() == p.getWorld() && field.center.distance(p.getLocation()) <= 5.0) {
                return true;
            }
        }
        return false;
    }

    private void reflectorSkill(Player p) {
        if (this.isOnCooldown(p.getUniqueId())) {
            return;
        }
        this.setCooldown(p.getUniqueId(), 20000L);
        this.mirrorEndTime.put(p.getUniqueId(), System.currentTimeMillis() + 3000L);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1, false, true));
        p.sendMessage("\u00a7d\u00a7l\u30df\u30e9\u30fc\u30b9\u30bf\u30f3\u30b9\uff01\u00a77\u767a\u5c04\u7269\u3092\u53cd\u5c04\u3057\u307e\u3059\uff083\u79d2\u9593\uff09");
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        UUID uid = p.getUniqueId();
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.mirrorEndTime.remove(uid), 60L);
    }

    public boolean isMirrorActive(UUID uid) {
        Long end = this.mirrorEndTime.get(uid);
        if (end != null && System.currentTimeMillis() < end) {
            return true;
        }
        this.mirrorEndTime.remove(uid);
        return false;
    }

    private void glaciesSkill(Player p) {
        if (this.isOnCooldown(p.getUniqueId())) {
            return;
        }
        this.setCooldown(p.getUniqueId(), 14000L);
        Vector dir = p.getLocation().getDirection().normalize();
        World w = p.getWorld();
        w.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);
        w.spawnParticle(Particle.SNOW_SHOVEL, p.getEyeLocation(), 30, 0.2, 0.2, 0.2, 0.2);
        for (Player t : w.getPlayers()) {
            if (t == p || !this.gm.isParticipant(t) || this.gm.getTeamOf(t) == this.gm.getTeamOf(p) || this.gm.isSpectator(t)) {
                continue;
            }
            Vector toTarget = t.getLocation().toVector().subtract(p.getLocation().toVector());
            double dist = toTarget.length();
            if (dist > 6.0) {
                continue;
            }
            Vector norm = toTarget.clone().normalize();
            if (dir.dot(norm) < Math.cos(Math.toRadians(45.0))) {
                continue;
            }
            t.damage(3.0, (Entity)p);
            t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 2, false, true));
            t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1, false, true));
            t.getWorld().spawnParticle(Particle.SNOW_SHOVEL, t.getLocation().add(0.0, 1.0, 0.0), 20, 0.3, 0.5, 0.3, 0.1);
        }
        p.sendMessage("\u00a7b\u00a7l\u30d5\u30ed\u30b9\u30c8\u30b9\u30c8\u30e9\u30a4\u30af\uff01");
    }

    private void setCooldown(UUID uid, long millis) {
        this.skillCooldowns.put(uid, System.currentTimeMillis() + millis);
    }

    private boolean isOnCooldown(UUID uid) {
        Long cd = this.skillCooldowns.get(uid);
        return cd != null && System.currentTimeMillis() < cd;
    }

    private void hideArmor(Player p) {
        ItemStack[] armor = new ItemStack[]{p.getInventory().getHelmet(), p.getInventory().getChestplate(), p.getInventory().getLeggings(), p.getInventory().getBoots()};
        this.storedArmor.put(p.getUniqueId(), armor);
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);
    }

    private void showArmor(Player p) {
        ItemStack[] armor = this.storedArmor.remove(p.getUniqueId());
        if (armor != null) {
            ItemStack boots = armor[0];
            armor[0] = armor[3];
            armor[3] = boots;
            ItemStack legs = armor[1];
            armor[1] = armor[2];
            armor[2] = legs;
            p.getInventory().setArmorContents(armor);
        }
    }

    private Player getTargetInSight(Player p, int maxDist) {
        Vector dir = p.getEyeLocation().getDirection();
        for (Entity e : p.getNearbyEntities((double)maxDist, (double)maxDist, (double)maxDist)) {
            Vector toTarget;
            Player t;
            if (!(e instanceof Player) || !this.gm.isParticipant(t = (Player)e) || this.gm.getTeamOf(t) == this.gm.getTeamOf(p) || dir.dot(toTarget = t.getEyeLocation().toVector().subtract(p.getEyeLocation().toVector()).normalize()) < 0.85 || !this.hasLineOfSight(p.getEyeLocation(), t.getEyeLocation())) continue;
            return t;
        }
        return null;
    }

    private boolean hasLineOfSight(Location from, Location to) {
        Vector dir = to.toVector().subtract(from.toVector());
        int dist = (int)dir.length();
        dir.normalize();
        for (int i = 0; i < dist; ++i) {
            Location check = from.clone().add(dir.clone().multiply(i));
            if (!check.getBlock().getType().isOccluding()) continue;
            return false;
        }
        return true;
    }

    public void clearPlayerPlacements(UUID uid) {
        List<Skeleton> army;
        for (MineData m : new ArrayList<MineData>(this.activeMines)) {
            if (!m.owner.equals(uid)) continue;
            m.entity.remove();
            this.activeMines.remove(m);
        }
        for (TurretData t2 : new ArrayList<TurretData>(this.activeTurrets)) {
            if (!t2.owner.equals(uid)) continue;
            t2.entity.remove();
            this.activeTurrets.remove(t2);
        }
        this.activeTraps.removeIf(t -> t.owner.equals(uid));
        TeamColor team = this.gm.getTeam(uid);
        if (team != null) {
            this.activeRecons.removeIf(r -> {
                if (r.ownerTeam == team) {
                    r.entity.remove();
                    return true;
                }
                return false;
            });
        }
        Location a = this.portalA.remove(uid);
        Location b = this.portalB.remove(uid);
        if (a != null) {
            this.portalBlocks.remove(a);
        }
        if (b != null) {
            this.portalBlocks.remove(b);
        }
        if ((army = this.necroArmy.remove(uid)) != null) {
            for (Skeleton s : army) {
                if (!s.isValid()) continue;
                s.remove();
            }
        }
        this.storedArmor.remove(uid);
        this.phantomEnd.remove(uid);
        this.nilgiritarMarks.remove(uid);
        this.removeWall(uid);
        this.timeSnapshots.remove(uid);
        this.activeBonds.entrySet().removeIf(en -> en.getValue().ownerUid.equals(uid) || en.getKey().equals(uid));
        this.activeHexFields.removeIf(f -> f.owner.equals(uid));
        this.frozenProjectiles.removeIf(e -> {
            if (!e.isValid()) {
                return true;
            }
            ProjectileSource src = e instanceof Projectile ? ((Projectile)e).getShooter() : null;
            return src instanceof Player && ((Player)src).getUniqueId().equals(uid);
        });
        this.mirrorEndTime.remove(uid);
    }

    static class ReconData {
        final ArmorStand entity;
        final TeamColor ownerTeam;

        ReconData(ArmorStand a, TeamColor t) {
            this.entity = a;
            this.ownerTeam = t;
        }
    }

    static class PulseData {
        final Location loc;
        final TeamColor ownerTeam;

        PulseData(Location l, TeamColor t) {
            this.loc = l;
            this.ownerTeam = t;
        }
    }

    static class TurretData {
        final Skeleton entity;
        final UUID owner;
        final TeamColor ownerTeam;

        TurretData(Skeleton e, UUID o, TeamColor t) {
            this.entity = e;
            this.owner = o;
            this.ownerTeam = t;
        }
    }

    static class MineData {
        final ArmorStand entity;
        final UUID owner;

        MineData(ArmorStand e, UUID o) {
            this.entity = e;
            this.owner = o;
        }
    }

    static class TrapData {
        final UUID owner;
        final Location loc;
        boolean triggered;
        boolean isTeleport;
        long placeTime;

        TrapData(UUID o, Location l) {
            this.owner = o;
            this.loc = l;
            this.isTeleport = false;
            this.placeTime = System.currentTimeMillis();
        }
    }

    static class Snapshot {
        final Location loc;
        final double health;

        Snapshot(Location loc, double health) {
            this.loc = loc;
            this.health = health;
        }
    }

    static class Bond {
        final UUID ownerUid;
        final long endTime;

        Bond(UUID ownerUid, long endTime) {
            this.ownerUid = ownerUid;
            this.endTime = endTime;
        }
    }

    static class HexField {
        final Location center;
        final UUID owner;
        final long endTime;

        HexField(Location center, UUID owner, long endTime) {
            this.center = center;
            this.owner = owner;
            this.endTime = endTime;
        }
    }
}

