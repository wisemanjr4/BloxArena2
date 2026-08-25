/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.entity.AbstractVillager
 *  org.bukkit.entity.Allay
 *  org.bukkit.entity.Arrow
 *  org.bukkit.entity.EnderPearl
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.GlowItemFrame
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.ItemFrame
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.entity.Skeleton
 *  org.bukkit.entity.Snowball
 *  org.bukkit.entity.Villager
 *  org.bukkit.entity.Zombie
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityDamageEvent$DamageCause
 *  org.bukkit.event.entity.EntityDeathEvent
 *  org.bukkit.event.entity.EntityPickupItemEvent
 *  org.bukkit.event.entity.EntityShootBowEvent
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.event.entity.ProjectileHitEvent
 *  org.bukkit.event.entity.ProjectileLaunchEvent
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryOpenEvent
 *  org.bukkit.event.inventory.InventoryType
 *  org.bukkit.event.inventory.PrepareItemCraftEvent
 *  org.bukkit.event.player.AsyncPlayerChatEvent
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerGameModeChangeEvent
 *  org.bukkit.event.player.PlayerInteractEntityEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerItemConsumeEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerRespawnEvent
 *  org.bukkit.event.player.PlayerSwapHandItemsEvent
 *  org.bukkit.event.player.PlayerTeleportEvent
 *  org.bukkit.event.player.PlayerTeleportEvent$TeleportCause
 *  org.bukkit.event.player.PlayerToggleSneakEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.projectiles.ProjectileSource
 */
package com.bloxarena.listener;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameManager;
import com.bloxarena.game.GameMode;
import com.bloxarena.game.GameState;
import com.bloxarena.game.TeamColor;
import com.bloxarena.kit.KitBuilder;
import com.bloxarena.kit.KitSelectGUI;
import com.bloxarena.kit.KitType;
import com.bloxarena.lobby.LobbyManager;
import com.bloxarena.map.MapConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

public class GameListeners
implements Listener {
    private final BloxArenaPlugin plugin;
    private final GameManager gm;
    private final LobbyManager lm;
    private KitSelectGUI activeGUI;
    private final Map<UUID, Long> disconnectedAt = new HashMap<UUID, Long>();
    private static final long RECONNECT_WINDOW_MS = 180000L;
    private final Map<UUID, UUID> lastDamager = new HashMap<UUID, UUID>();

    public GameListeners(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
        this.lm = plugin.getLobbyManager();
    }

    public void setActiveGUI(KitSelectGUI gui) {
        this.activeGUI = gui;
    }

    public void clearLastDamager() {
        this.lastDamager.clear();
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        MapConfig map;
        Player p = e.getPlayer();
        if (this.plugin.getTestFieldManager().isTester(p)) {
            if (!e.hasChangedPosition()) {
                return;
            }
            if (this.plugin.getTestFieldManager().hasArea() && !this.plugin.getTestFieldManager().isInArea(e.getTo())) {
                p.teleport(e.getFrom());
                p.sendMessage("\u00a7c\u30c6\u30b9\u30c8\u30a8\u30ea\u30a2\u5916\u306b\u306f\u51fa\u3089\u308c\u307e\u305b\u3093\u3002/ba test leave \u3067\u9000\u51fa");
            }
            return;
        }
        if (!e.hasChangedBlock()) {
            return;
        }
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) {
            return;
        }
        boolean wasIn = this.lm.isInWaitingArea(from);
        boolean isIn = this.lm.isInWaitingArea(to);
        if (!wasIn && isIn) {
            this.lm.onPlayerEnter(p);
        } else if (wasIn && !isIn) {
            this.lm.onPlayerExit(p);
        }
        if (this.plugin.getOobImmunePlayers().contains(p.getUniqueId())) {
            return;
        }
        if (this.gm.getState() == GameState.IN_GAME && this.gm.isParticipant(p) && !this.gm.isSpectator(p) && (map = this.gm.getCurrentMap()) != null && map.hasOob() && !this.isInBounds(to, map.getOobMin(), map.getOobMax())) {
            boolean respawnMode;
            p.sendMessage("\u00a7c\u26a0 area outside! eliminated.");
            GameMode mode = this.gm.getCurrentGameMode();
            boolean bl = respawnMode = mode == GameMode.TEAM_DEATHMATCH || mode == GameMode.DOMINATION || mode == GameMode.CAPTURE_THE_FLAG;
            Location safe = respawnMode ? from : (map.getLobby() != null ? map.getLobby() : from);
            p.teleport(safe);
            this.gm.onPlayerDied(p, null, null);
        }
        if (this.gm.getState() == GameState.WAITING && this.lm.hasLobbyOob() && this.lm.getLobbySpawn() != null && !this.isInBounds(to, this.lm.getLobbyOobMin(), this.lm.getLobbyOobMax())) {
            p.teleport(this.lm.getLobbySpawn());
        }
    }

    private boolean isInBounds(Location loc, Location min, Location max) {
        if (loc == null || min == null || max == null) {
            return true;
        }
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return x >= Math.min(min.getX(), max.getX()) && x <= Math.max(min.getX(), max.getX()) && y >= Math.min(min.getY(), max.getY()) && y <= Math.max(min.getY(), max.getY()) && z >= Math.min(min.getZ(), max.getZ()) && z <= Math.max(min.getZ(), max.getZ());
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("bloxarena.admin")) {
            return;
        }
        if (e.getNewGameMode() == org.bukkit.GameMode.SPECTATOR) {
            return;
        }
        if (e.getNewGameMode() == org.bukkit.GameMode.SURVIVAL) {
            return;
        }
        GameState state = this.gm.getState();
        if (state == GameState.WAITING && this.lm.getWaitingPlayers().contains(p.getUniqueId()) && e.getNewGameMode() == org.bukkit.GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        if (!this.gm.isParticipant(victim)) {
            return;
        }
        e.setDeathMessage(null);
        e.getDrops().clear();
        e.setDroppedExp(0);
        UUID lastKillerUuid = this.lastDamager.remove(victim.getUniqueId());
        this.lastDamager.entrySet().removeIf(ent -> ((UUID)ent.getValue()).equals(victim.getUniqueId()));
        Player resolvedKiller = lastKillerUuid != null ? Bukkit.getPlayer((UUID)lastKillerUuid) : victim.getKiller();
        this.gm.onPlayerDied(victim, resolvedKiller, lastKillerUuid);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!this.gm.isParticipant(p)) {
            return;
        }
        if (this.gm.getState() != GameState.IN_GAME) {
            return;
        }
        MapConfig map = this.gm.getCurrentMap();
        if (map != null && map.getCenter() != null) {
            e.setRespawnLocation(map.getCenter().clone().add(0.0, 5.0, 0.0));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player p = (Player)entity;
        if (this.gm.getState() == GameState.WAITING) {
            e.setCancelled(true);
            return;
        }
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && this.gm.hasNoFallDamage(p)) {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 0.8f);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.isOp()) {
            return;
        }
        if (this.gm.getState() != GameState.IN_GAME) {
            if (this.gm.isInCenterZone(e.getBlock())) {
                e.setCancelled(true);
            }
            return;
        }
        if (!this.gm.isParticipant(p)) {
            e.setCancelled(true);
            return;
        }
        if (!this.gm.isInCenterZone(e.getBlock())) {
            e.setCancelled(true);
            p.sendMessage("\u00a7c\u4e2d\u592e\u30b3\u30f3\u30af\u30ea\u30fc\u30c8\u4ee5\u5916\u306f\u7834\u58ca\u3067\u304d\u307e\u305b\u3093\u3002");
            return;
        }
        if (this.gm.isObjectiveLocked()) {
            e.setCancelled(true);
            p.sendMessage("\u00a7c\u23f3 \u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u306f\u307e\u3060\u30ed\u30c3\u30af\u4e2d\u3067\u3059\uff01\uff08\u8a66\u5408\u958b\u59cb2\u5206\u5f8c\u306b\u89e3\u653e\uff09");
            return;
        }
        int cy = this.gm.getCenterY();
        if (e.getBlock().getY() != cy) {
            e.setCancelled(true);
            return;
        }
        Material mat = e.getBlock().getType();
        if (mat == Material.RED_CONCRETE || mat == Material.CYAN_CONCRETE || mat == Material.WHITE_CONCRETE) {
            e.setDropItems(false);
            this.gm.onObjectiveBlockBroken();
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (this.gm.getState() != GameState.IN_GAME) {
            return;
        }
        Player p = e.getPlayer();
        if (!this.gm.isParticipant(p)) {
            e.setCancelled(true);
            return;
        }
        if (!this.gm.isInCenterZone(e.getBlockPlaced())) {
            if (!p.isOp()) {
                e.setCancelled(true);
            }
            return;
        }
        if (this.gm.isObjectiveLocked() && !p.isOp()) {
            e.setCancelled(true);
            p.sendMessage("\u00a7c\u23f3 \u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u306f\u307e\u3060\u30ed\u30c3\u30af\u4e2d\u3067\u3059\uff01\uff08\u8a66\u5408\u958b\u59cb2\u5206\u5f8c\u306b\u89e3\u653e\uff09");
            return;
        }
        Material placed = e.getBlockPlaced().getType();
        if (placed != Material.RED_CONCRETE && placed != Material.CYAN_CONCRETE) {
            if (!p.isOp()) {
                e.setCancelled(true);
            }
            return;
        }
        Location loc = e.getBlockPlaced().getLocation();
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.gm.checkObjectiveWin(loc), 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (this.gm.getState() == GameState.KIT_SELECT) {
            e.setCancelled(true);
            return;
        }
        String title = e.getView().getTitle();
        if (title.equals("\u00a76\u00a7lKit Editor \u00a77- \u30ad\u30c3\u30c8\u4e00\u89a7") || title.startsWith("\u00a76\u00a7lEdit: \u00a7e")) {
            this.plugin.getKitEditorGUI().handleClick(e);
        } else if (title.equals("\u00a76\u00a7l\u30ad\u30c3\u30c8\u4e00\u89a7") || title.startsWith("\u00a76\u00a7l")) {
            this.plugin.getKitInfoGUI().handleClick(e);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if ((this.gm.getState() == GameState.KIT_SELECT || this.gm.getState() == GameState.IN_GAME) && this.gm.isParticipant(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent e) {
        if (this.gm.getState() == GameState.KIT_SELECT) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        boolean isLeft;
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (this.gm.getState() == GameState.KIT_SELECT) {
            Player p = e.getPlayer();
            if (!this.gm.isParticipant(p)) {
                return;
            }
            e.setCancelled(true);
            if (this.activeGUI != null) {
                this.activeGUI.onInteract(p);
            }
            return;
        }
        Action action = e.getAction();
        boolean isRight = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        boolean bl = isLeft = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        if (!isRight && !isLeft) {
            return;
        }
        Player p = e.getPlayer();
        if (this.gm.getState() == GameState.KIT_SELECT) {
            if (!this.gm.isParticipant(p)) {
                return;
            }
            e.setCancelled(true);
            if (this.activeGUI != null) {
                this.activeGUI.onInteract(p);
            }
            return;
        }
        if (this.plugin.getTestFieldManager().isTester(p)) {
            ItemStack held;
            if (isRight && this.plugin.getTutorialManager().isInTutorial(p.getUniqueId()) && this.plugin.getTutorialManager().getStep(p.getUniqueId()) == 0) {
                this.plugin.getTutorialManager().advance(p);
            }
            if ((held = p.getInventory().getItemInMainHand()).getType() == Material.BOW || held.getType() == Material.CROSSBOW || held.getType() == Material.SHIELD || held.getType() == Material.ENDER_PEARL) {
                return;
            }
            if (held != null && held.getItemMeta() != null && (held.getItemMeta().getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "kit_skill"), PersistentDataType.STRING) || held.getItemMeta().getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "burst_skill"), PersistentDataType.BYTE))) {
                this.plugin.getSkillManager().onRightClick(p, held);
            }
            e.setCancelled(true);
            return;
        }
        if (this.gm.getState() == GameState.WAITING && this.plugin.getKitInfoGUI().isGuideItem(p.getInventory().getItemInMainHand())) {
            e.setCancelled(true);
            this.plugin.getKitInfoGUI().openList(p);
            return;
        }
        if (this.gm.getState() == GameState.KIT_SELECT) {
            if (!this.gm.isParticipant(p)) {
                return;
            }
            e.setCancelled(true);
            if (this.activeGUI != null) {
                this.activeGUI.onInteract(p);
            }
            return;
        }
        if (this.gm.getState() == GameState.IN_GAME && this.gm.isParticipant(p) && !this.gm.isSpectator(p)) {
            if (System.currentTimeMillis() - this.gm.getInGameStartTime() < 1000L) {
                return;
            }
            ItemStack held = p.getInventory().getItemInMainHand();
            if (isRight) {
                this.plugin.getSkillManager().onRightClick(p, held);
            }
            this.plugin.getSkillManager().onClick(p, held, isLeft);
            if (isRight && p.getInventory().getItemInMainHand().getType() == Material.CROSSBOW) {
                this.plugin.getSkillManager().onCrossbowShoot(p);
            }
            if (this.gm.getCurrentGameMode() == GameMode.CAPTURE_THE_FLAG) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && (e.getClickedBlock().getType() == Material.RED_BANNER || e.getClickedBlock().getType() == Material.CYAN_BANNER)) {
                    e.setCancelled(true);
                }
                this.gm.tryPickupFlag(p);
                this.gm.tryPickupDroppedFlag(p);
            }
            if (this.gm.getCurrentGameMode() == GameMode.BOMB_MISSION && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)) {
                this.gm.tryPlantBomb(p);
                this.gm.tryDefuseBomb(p);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        Player attacker;
        Entity entity;
        Arrow arrow;
        Entity entity2;
        Player damager;
        Entity entity3;
        Player damager2;
        Player attacker2;
        Object botTeam;
        Entity p;
        Player victim;
        Entity entity4 = e.getEntity();
        if (entity4 instanceof Player) {
            victim = (Player)entity4;
            if (this.plugin.getTestFieldManager().isTester(victim)) {
                e.setCancelled(true);
                return;
            }
        }
        if ((entity4 = e.getEntity()) instanceof Zombie) {
            Zombie z = (Zombie)entity4;
            if (this.plugin.getTestFieldManager().isDummy((Entity)z)) {
                Entity entity5 = e.getDamager();
                if (entity5 instanceof Player) {
                    p = (Player)entity5;
                    p.sendActionBar((Component)Component.text((String)("\u00a7e\u30c0\u30df\u30fc\u6b8bHP: \u00a7c" + String.format("%.0f", z.getHealth() - e.getFinalDamage()) + "/100")));
                }
                return;
            }
        }
        if (this.gm.getState() != GameState.IN_GAME) {
            if (this.gm.getState() == GameState.KIT_SELECT && e.getEntity() instanceof Player) {
                e.setCancelled(true);
            }
            return;
        }
        p = e.getEntity();
        if (!(p instanceof Player)) {
            return;
        }
        victim = (Player)p;
        TeamColor victimTeam = this.gm.getTeamOf(victim);
        Entity entity6 = e.getDamager();
        if (entity6 instanceof Player) {
            Player attacker3 = (Player)entity6;
            if (victimTeam != null && victimTeam == this.gm.getTeamOf(attacker3)) {
                e.setCancelled(true);
                return;
            }
        } else if (this.plugin.getBotManager().isBot(e.getDamager())) {
            UUID botUuid = this.plugin.getBotManager().getBotUuid(e.getDamager());
            botTeam = this.plugin.getBotManager().getBotTeam(botUuid);
            if (botTeam != null && botTeam == victimTeam) {
                e.setCancelled(true);
                return;
            }
        }
        Entity noCombatDamager = e.getDamager();
        Player noCombatAttacker = null;
        if (noCombatDamager instanceof Player) {
            noCombatAttacker = (Player)noCombatDamager;
        } else if (noCombatDamager instanceof Projectile && ((Projectile)noCombatDamager).getShooter() instanceof Player) {
            noCombatAttacker = (Player)((Projectile)noCombatDamager).getShooter();
        }
        if (noCombatAttacker != null && this.gm.isInFFANoCombatWindow(noCombatAttacker.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        UUID killerUuid = null;
        if (this.plugin.getGameManager().getPlayerKitType(victim.getUniqueId()) == KitType.MEDIC && (e.getDamager() instanceof Player || e.getDamager() instanceof Arrow) && victim.hasPotionEffect(PotionEffectType.REGENERATION)) {
            victim.removePotionEffect(PotionEffectType.REGENERATION);
            victim.sendMessage("\u00a7c\u653b\u6483\u3092\u53d7\u3051\u305f\u305f\u3081\u56de\u5fa9\u304c\u4e2d\u65ad\u3055\u308c\u307e\u3057\u305f");
        }
        if ((botTeam = e.getDamager()) instanceof Player) {
            attacker2 = (Player)botTeam;
            if (victimTeam != null && this.gm.getTeamOf(attacker2) != victimTeam) {
                this.plugin.getSkillManager().onComboHit(attacker2, victim);
            }
        }
        if ((botTeam = e.getDamager()) instanceof Player) {
            damager2 = (Player)botTeam;
            killerUuid = damager2.getUniqueId();
            this.gm.getMatchStats().addDamage(killerUuid, e.getFinalDamage());
            this.plugin.getStatsManager().addDamage(killerUuid, e.getFinalDamage());
        } else if (this.plugin.getBotManager().isBot(e.getDamager())) {
            killerUuid = this.plugin.getBotManager().getBotUuid(e.getDamager());
        }
        if (killerUuid != null) {
            this.lastDamager.put(victim.getUniqueId(), killerUuid);
        }
        if ((botTeam = e.getDamager()) instanceof Player) {
            attacker2 = (Player)botTeam;
            if (victimTeam != null && this.plugin.getSkillManager().tryParryCounter(attacker2, victim)) {
                e.setCancelled(true);
                return;
            }
        }
        if (this.plugin.getSkillManager().isGuardBroken(victim.getUniqueId())) {
            victim.setCooldown(Material.SHIELD, 60);
        }
        if (this.gm.getPlayerKitType(victim.getUniqueId()) == KitType.REFLECTOR && this.plugin.getSkillManager().isMirrorActive(victim.getUniqueId()) && e.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile)e.getDamager();
            if (proj.getShooter() instanceof Player) {
                Player src = (Player)proj.getShooter();
                if (victimTeam != null && victimTeam != this.gm.getTeamOf(src)) {
                    e.setCancelled(true);
                    src.damage(e.getDamage(), (Entity)victim);
                    src.setVelocity(src.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize().multiply(1.2).setY(0.2));
                    src.getWorld().spawnParticle(Particle.CRIT_MAGIC, src.getLocation().add(0.0, 1.0, 0.0), 20, 0.3, 0.3, 0.3, 0.1);
                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
                    victim.sendMessage("\u00a7d\u00a7l\u53cd\u5c04\uff01");
                    return;
                }
            }
        }
        if ((botTeam = e.getDamager()) instanceof Player) {
            attacker2 = (Player)botTeam;
            if (victimTeam != null && this.gm.getTeamOf(attacker2) != victimTeam) {
                this.plugin.getSkillManager().tryUniversalGuardBreak(attacker2, victim);
            }
        }
        if ((botTeam = e.getDamager()) instanceof Player) {
            attacker2 = (Player)botTeam;
            if (victimTeam != null && this.gm.getPlayerKitType(attacker2.getUniqueId()) == KitType.NILGIRITAR && this.plugin.getSkillManager().tryPierceShield(attacker2, victim)) {
                victim.setCooldown(Material.SHIELD, 20);
                victim.damage(3.0, (Entity)attacker2);
            }
        }
        if ((entity3 = e.getDamager()) instanceof Player) {
            Skeleton skeleton;
            damager2 = (Player)entity3;
            entity3 = e.getEntity();
            if (entity3 instanceof Skeleton && (skeleton = (Skeleton)entity3).getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "decoy"), PersistentDataType.BYTE)) {
                this.plugin.getSkillManager().onDecoyHit((Entity)skeleton, damager2);
            }
        }
        Location loc = victim.getLocation().add(0.0, 1.0, 0.0);
        victim.getWorld().spawnParticle(Particle.CRIT, loc, 8, 0.2, 0.2, 0.2, 0.15);
        victim.getWorld().playSound(loc, Sound.ENTITY_PLAYER_HURT, 0.6f, 1.1f);
        entity3 = e.getDamager();
        if (entity3 instanceof Player) {
            damager = (Player)entity3;
            this.plugin.getSkillManager().onVampireDamageDealt(damager, e.getFinalDamage());
            this.plugin.getSkillManager().onVampireAttack(damager);
        }
        this.plugin.getSkillManager().onVampireDamaged(victim, e.getFinalDamage());
        entity3 = e.getDamager();
        if (entity3 instanceof Player && this.gm.getPlayerKitType((damager = (Player)entity3).getUniqueId()) == KitType.VAMPIRE) {
            boolean inBlood = this.plugin.getSkillManager().isVampireBloodMode(damager.getUniqueId());
            double gauge = this.plugin.getSkillManager().getVampireGauge(damager.getUniqueId());
            if (inBlood && gauge >= 60.0) {
                damager.setHealth(Math.min(damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(), damager.getHealth() + 1.0));
            }
        }
        if ((entity2 = e.getDamager()) instanceof Arrow && (arrow = (Arrow)entity2).getShooter() instanceof Player) {
            Player shooter = (Player)arrow.getShooter();
            if (this.plugin.getSkillManager().onSniperHit(shooter, victim)) {
                this.lastDamager.put(victim.getUniqueId(), shooter.getUniqueId());
                e.setCancelled(true);
                return;
            }
        }
        if ((entity = e.getDamager()) instanceof Player && this.gm.getPlayerKitType((attacker = (Player)entity).getUniqueId()) == KitType.KREUTZ && victim.getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "kreutz_pierced"), PersistentDataType.BYTE)) {
            if (this.plugin.getSkillManager().tryMarkPiercing(victim.getUniqueId())) {
                victim.damage(2.0, (Entity)attacker);
                victim.getPersistentDataContainer().remove(new NamespacedKey((Plugin)this.plugin, "kreutz_pierced"));
                attacker.sendMessage("\u00a75\u00a7l\u30d4\u30a2\u30c3\u30b7\u30f3\u30b0\uff01\u00a77\u9632\u5177\u8cab\u901a+2\u30c0\u30e1\u30fc\u30b8\uff01");
            }
        }
        Player bondOwner = this.plugin.getSkillManager().getBondOwner(victim.getUniqueId());
        if (bondOwner != null && victim != bondOwner) {
            double original = e.getDamage();
            double redirected = original / 2.0;
            e.setDamage(original - redirected);
            Entity damageSource = e.getDamager();
            bondOwner.damage(redirected, damageSource);
            bondOwner.getWorld().spawnParticle(Particle.REDSTONE, bondOwner.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.0, new Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (this.plugin.getTestFieldManager().isDummy((Entity)entity)) {
            e.getDrops().clear();
            e.setDroppedExp(0);
            return;
        }
        if (this.plugin.getBotManager().isBot((Entity)entity)) {
            e.getDrops().clear();
            e.setDroppedExp(0);
            Player killer = entity.getKiller();
            this.plugin.getBotManager().onEntityDeath((Entity)entity, killer);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        if (this.gm.isSpectator(sender)) {
            e.setCancelled(true);
            String specMsg = "\u00a78[spec] \u00a77" + sender.getName() + "\u00a78: \u00a77" + e.getMessage();
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                for (Player r : Bukkit.getOnlinePlayers()) {
                    if (!this.gm.isSpectator(r) && this.gm.isParticipant(r)) continue;
                    r.sendMessage(specMsg);
                }
            });
            return;
        }
        if (this.gm.getState() == GameState.IN_GAME && this.gm.isParticipant(sender)) {
            String msg = e.getMessage();
            TeamColor team = this.gm.getTeamOf(sender);
            if (msg.startsWith(".")) {
                e.setMessage(msg.substring(1).trim());
                e.setFormat("\u00a77[\u5168\u4f53] \u00a7f" + sender.getName() + "\u00a77: \u00a7f%2$s");
            } else {
                String prefix = team != null ? team.getColorCode() + "[" + team.getDisplayName() + "] \u00a7f" : "\u00a77";
                e.setFormat(prefix + sender.getName() + "\u00a77: \u00a7f%2$s");
                e.getRecipients().removeIf(r -> {
                    TeamColor rTeam = this.gm.getTeamOf((Player)r);
                    return rTeam != team && !this.gm.isSpectator((Player)r) && !r.equals((Object)sender);
                });
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        Player ps;
        Projectile projectile;
        projectile = e.getEntity();
        if (projectile instanceof EnderPearl && projectile.getShooter() instanceof Player && this.gm.isFlagCarrier((ps = (Player) projectile.getShooter()).getUniqueId())) {
            e.setCancelled(true);
            ps.sendMessage("\u00a7c\u65d7\u3092\u6301\u3063\u3066\u3044\u308b\u9593\u306f\u30a8\u30f3\u30c0\u30fc\u30d1\u30fc\u30eb\u3092\u4f7f\u7528\u3067\u304d\u307e\u305b\u3093\uff01");
            return;
        }
        projectile = e.getEntity();
        if (!(projectile instanceof Arrow)) {
            return;
        }
        Arrow arrow = (Arrow)projectile;
        ProjectileSource projectileSource = arrow.getShooter();
        if (!(projectileSource instanceof Player)) {
            return;
        }
        Player shooter = (Player)projectileSource;
        if (!this.gm.isParticipant(shooter)) {
            return;
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Player victim;
        Entity entity;
        if (e.getEntity() instanceof Snowball) {
            return;
        }
        ProjectileSource projectileSource = e.getEntity().getShooter();
        if (!(projectileSource instanceof Player)) {
            return;
        }
        Player shooter = (Player)projectileSource;
        if (!this.gm.isParticipant(shooter)) {
            return;
        }
        if (this.gm.getState() != GameState.IN_GAME) {
            return;
        }
        if (e.getHitBlock() != null) {
            this.plugin.getSkillManager().scoutReconShot(shooter, e.getHitBlock().getLocation().add(0.0, 1.0, 0.0));
            this.plugin.getSkillManager().scoutPulseShot(shooter, e.getHitBlock().getLocation().add(0.0, 1.0, 0.0));
            this.plugin.getSkillManager().clearSniperMarkOnShoot(shooter);
            shooter.getPersistentDataContainer().remove(new NamespacedKey((Plugin)this.plugin, "heavy_bolt"));
        }
        if ((entity = e.getHitEntity()) instanceof Player && this.gm.isParticipant(victim = (Player)entity)) {
            if (this.gm.getPlayerKitType(shooter.getUniqueId()) == KitType.NILGIRITAR) {
                this.plugin.getSkillManager().markWithWindHole(victim);
            }
            this.plugin.getSkillManager().marksmanHeavyBoltHit(shooter, victim);
            e.getEntity().remove();
        }
    }

    @EventHandler
    public void onSnowballLand(ProjectileHitEvent e) {
        Object object;
        Projectile projectile = e.getEntity();
        if (!(projectile instanceof Snowball)) {
            return;
        }
        Snowball ball = (Snowball)projectile;
        ProjectileSource projectileSource = ball.getShooter();
        if (!(projectileSource instanceof Player)) {
            return;
        }
        Player shooter = (Player)projectileSource;
        if (!this.gm.isParticipant(shooter)) {
            return;
        }
        if ("flashBang".equals(ball.getCustomName())) {
            this.plugin.getSkillManager().onFlashBangLand(e.getEntity().getLocation(), shooter);
        }
        if (ball.getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "grapple"), PersistentDataType.BYTE) && (object = e.getHitEntity()) instanceof Player) {
            Player victim = (Player)object;
            this.plugin.getSkillManager().onGrappleHit(ball, victim, shooter);
        }
        if (ball.getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "mega_rocket"), PersistentDataType.BYTE)) {
            this.plugin.getSkillManager().onMegaRocketHit(e.getEntity().getLocation(), shooter, ball);
            e.getEntity().remove();
        }
        if (ball.getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "micro_rocket"), PersistentDataType.BYTE)) {
            float power = this.plugin.getSkillManager().rocketHitPower(ball, e.getEntity().getLocation(), 2.0f);
            e.getEntity().getLocation().getWorld().createExplosion(e.getEntity().getLocation(), 0.0f, false, false, (Entity)shooter);
            for (Entity e2 : e.getEntity().getLocation().getWorld().getNearbyEntities(e.getEntity().getLocation(), 3.0, 2.0, 3.0)) {
                Player t;
                if (!(e2 instanceof Player) || !this.gm.isParticipant(t = (Player)e2) || this.gm.getTeamOf(t) == this.gm.getTeamOf(shooter)) continue;
                t.damage((double)(power * 5.0f), (Entity)shooter);
            }
            e.getEntity().remove();
        } else if (ball.getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "theos_pada"), PersistentDataType.BYTE)) {
            if (e.getHitEntity() != null) {
                this.plugin.getSkillManager().onTheosPadaHit(ball, shooter, e.getHitEntity());
            }
            e.getEntity().remove();
        } else if (ball.getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "ba_cook"), PersistentDataType.STRING)) {
            String matName = (String)ball.getPersistentDataContainer().get(new NamespacedKey((Plugin)this.plugin, "ba_cook"), PersistentDataType.STRING);
            if (matName != null) {
                this.plugin.getSkillManager().cookHit(e.getEntity().getLocation(), shooter, Material.valueOf((String)matName));
            }
            e.getEntity().remove();
        }
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        if (!this.gm.isParticipant(p) || this.gm.isSpectator(p)) {
            return;
        }
        if (this.gm.getState() != GameState.IN_GAME) {
            return;
        }
        if (e.isSneaking()) {
            this.plugin.getSkillManager().onParryAttempt(p);
        }
        this.plugin.getSkillManager().onGrangSneak(p, e.isSneaking());
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPhantomHurt(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player p = (Player)entity;
        if (e.getDamage() > 0.0) {
            this.plugin.getSkillManager().onPhantomDamaged(p);
        }
    }

    @EventHandler
    public void onPortalMove(PlayerMoveEvent e) {
        if (!e.hasChangedBlock()) {
            return;
        }
        Player p = e.getPlayer();
        if (this.gm.getState() != GameState.IN_GAME) {
            return;
        }
        if (!this.gm.isParticipant(p) || this.gm.isSpectator(p)) {
            return;
        }
        this.plugin.getSkillManager().checkPortalTeleport(p, e.getTo());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Long quitTime;
        Player p = e.getPlayer();
        if (!p.isOp() && p.getGameMode() == org.bukkit.GameMode.SPECTATOR && this.gm.getState() == GameState.WAITING) {
            p.setGameMode(org.bukkit.GameMode.ADVENTURE);
        }
        if ((quitTime = this.disconnectedAt.remove(p.getUniqueId())) != null) {
            long elapsed = System.currentTimeMillis() - quitTime;
            if (elapsed > 180000L || this.gm.getState() != GameState.IN_GAME) {
                p.getInventory().clear();
                p.setGameMode(org.bukkit.GameMode.ADVENTURE);
                Location lobby = this.lm.getLobbySpawn();
                if (lobby != null) {
                    p.teleport(lobby);
                }
                p.sendMessage("\u00a77\u8a66\u5408\u304b\u3089\u96e2\u8131\u3057\u307e\u3057\u305f\u3002");
                return;
            }
            if (this.gm.getState() == GameState.IN_GAME && this.gm.isParticipant(p) && !this.gm.isSpectator(p)) {
                long sinceGameStart = System.currentTimeMillis() - this.gm.getInGameStartTime();
                if (sinceGameStart < 1000L) {
                    return;
                }
                KitType kit = this.gm.getPlayerKits().get(p.getUniqueId());
                TeamColor team = this.gm.getTeam(p.getUniqueId());
                if (kit != null && team != null) {
                    p.getInventory().clear();
                    KitBuilder.giveKit(p, kit, team, this.plugin);
                    p.sendMessage("\u00a7a\u518d\u63a5\u7d9a\u3057\u307e\u3057\u305f\u3002\u30ad\u30c3\u30c8\u3092\u518d\u652f\u7d66\u3057\u307e\u3057\u305f\u3002");
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (this.gm.isParticipant(p)) {
            this.disconnectedAt.put(p.getUniqueId(), System.currentTimeMillis());
        }
        if (this.lm != null) {
            this.lm.onPlayerQuit(p);
        }
    }

    @EventHandler
    public void onPearlTeleport(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }
        Player p = e.getPlayer();
        if (!this.gm.isParticipant(p) || this.gm.isSpectator(p)) {
            return;
        }
        MapConfig map = this.gm.getCurrentMap();
        if (map != null && map.hasOob() && !this.isInBounds(e.getTo(), map.getOobMin(), map.getOobMax())) {
            e.setCancelled(true);
            p.sendMessage("\u00a7c\u30a8\u30ea\u30a2\u5916\u3078\u306e\u30c6\u30ec\u30dd\u30fc\u30c8\u306f\u7981\u6b62\u3055\u308c\u3066\u3044\u307e\u3059\u3002");
        }
    }

    @EventHandler
    public void onContainerOpen(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!this.gm.isParticipant(e.getPlayer())) {
            return;
        }
        if (this.gm.getSpectators().contains(e.getPlayer().getUniqueId())) {
            return;
        }
        if (e.getClickedBlock() != null && e.getClickedBlock().getType().name().contains("CHEST") || e.getClickedBlock() != null && e.getClickedBlock().getType().name().contains("BARREL") || e.getClickedBlock() != null && e.getClickedBlock().getType().name().contains("SHULKER")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("\u00a7c\u8a66\u5408\u4e2d\u306e\u30c1\u30a7\u30b9\u30c8\u5229\u7528\u306f\u7981\u6b62\u3055\u308c\u3066\u3044\u307e\u3059\u3002");
        }
    }

    @EventHandler
    public void onItemFrameInteract(PlayerInteractEntityEvent e) {
        if (!this.gm.isParticipant(e.getPlayer())) {
            return;
        }
        if (this.gm.isSpectator(e.getPlayer())) {
            return;
        }
        if (e.getRightClicked() instanceof ItemFrame || e.getRightClicked() instanceof GlowItemFrame) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpectatorDamage(EntityDamageByEntityEvent e) {
        Player p;
        Entity entity = e.getDamager();
        if (entity instanceof Player && this.gm.isSpectator(p = (Player)entity)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpectatorInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (this.gm.isSpectator(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpectatorPickup(EntityPickupItemEvent e) {
        Player p;
        LivingEntity livingEntity = e.getEntity();
        if (livingEntity instanceof Player && this.gm.isSpectator(p = (Player)livingEntity)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {
        Player p;
        HumanEntity humanEntity = e.getView().getPlayer();
        if (humanEntity instanceof Player && this.gm.isParticipant(p = (Player)humanEntity) && !this.gm.isSpectator(p)) {
            e.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Inventory inv;
        Player p = (Player)e.getPlayer();
        if (this.gm.isParticipant(p) && !this.gm.isSpectator(p) && ((inv = e.getInventory()).getType() == InventoryType.WORKBENCH || inv.getType() == InventoryType.GRINDSTONE || inv.getType() == InventoryType.ANVIL || inv.getType() == InventoryType.SMITHING || inv.getType() == InventoryType.ENCHANTING || inv.getType() == InventoryType.BREWING || inv.getType() == InventoryType.BEACON || inv.getType() == InventoryType.FURNACE)) {
            e.setCancelled(true);
            p.sendMessage("\u00a7c\u8a66\u5408\u4e2d\u306e\u4f5c\u696d\u53f0\u30fb\u8a2d\u5099\u306e\u5229\u7528\u306f\u7981\u6b62\u3055\u308c\u3066\u3044\u307e\u3059\u3002");
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        if (!this.gm.isParticipant(e.getPlayer())) {
            return;
        }
        if (this.gm.isSpectator(e.getPlayer())) {
            return;
        }
        if (e.getRightClicked() instanceof Villager || e.getRightClicked() instanceof AbstractVillager || e.getRightClicked() instanceof Allay) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (e.getItem().getItemMeta() != null && e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "ba_cook"), PersistentDataType.STRING)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("\u00a7c\u3053\u306e\u98df\u6750\u306f\u76f4\u63a5\u98df\u3079\u3089\u308c\u307e\u305b\u3093\u3002\u53f3\u30af\u30ea\u3067\u4f7f\u7528 / \u5de6\u30af\u30ea\u3067\u6295\u64f2\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent e) {
        LivingEntity livingEntity = e.getEntity();
        if (livingEntity instanceof Player) {
            Player p = (Player)livingEntity;
            if (this.plugin.getSkillManager().isDeadlocked(p.getUniqueId())) {
                e.setCancelled(true);
                p.sendMessage("\u00a7c\u30c7\u30c3\u30c9\u30ed\u30c3\u30af\u4e2d\u306f\u6b66\u5668\u3092\u4f7f\u7528\u3067\u304d\u307e\u305b\u3093\uff01");
            }
        }
    }
}

