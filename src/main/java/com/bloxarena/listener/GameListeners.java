package com.bloxarena.listener;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameManager;
import com.bloxarena.game.GameState;
import com.bloxarena.kit.KitEditorGUI;
import com.bloxarena.kit.KitSelectGUI;
import com.bloxarena.kit.KitType;
import com.bloxarena.lobby.LobbyManager;
import com.bloxarena.map.MapConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;

public class GameListeners implements Listener {

    private final BloxArenaPlugin plugin;
    private final GameManager gm;
    private final LobbyManager lm;
    private KitSelectGUI activeGUI;
    private final java.util.Map<java.util.UUID, Long> disconnectedAt = new java.util.HashMap<>();
    private static final long RECONNECT_WINDOW_MS = 3 * 60 * 1000L;
    private final java.util.Map<java.util.UUID, java.util.UUID> lastDamager = new java.util.HashMap<>();

    public GameListeners(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.gm = plugin.getGameManager();
        this.lm = plugin.getLobbyManager();
    }

    public void setActiveGUI(KitSelectGUI gui) { this.activeGUI = gui; }
    public void clearLastDamager() { this.lastDamager.clear(); }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        // Test field OOB
        Player p = e.getPlayer();
        if (plugin.getTestFieldManager().isTester(p)) {
            if (!e.hasChangedPosition()) return;
            if (plugin.getTestFieldManager().hasArea()
                    && !plugin.getTestFieldManager().isInArea(e.getTo())) {
                p.teleport(e.getFrom());
                p.sendMessage("§cテストエリア外には出られません。/ba test leave で退出");
            }
            return; // skip all normal lobby/game logic
        }
        if (!e.hasChangedBlock()) return;
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;
        boolean wasIn = lm.isInWaitingArea(from);
        boolean isIn  = lm.isInWaitingArea(to);
        if (!wasIn && isIn)  lm.onPlayerEnter(p);
        else if (wasIn && !isIn) lm.onPlayerExit(p);
        if (p.isOp()) return;
        if (gm.getState() == com.bloxarena.game.GameState.IN_GAME && gm.isParticipant(p) && !gm.isSpectator(p)) {
            com.bloxarena.map.MapConfig map = gm.getCurrentMap();
            if (map != null && map.hasOob() && !isInBounds(to, map.getOobMin(), map.getOobMax())) {
                p.sendMessage("§c⚠ area outside! eliminated.");
                com.bloxarena.game.GameMode mode = gm.getCurrentGameMode();
                boolean respawnMode = mode == com.bloxarena.game.GameMode.TEAM_DEATHMATCH
                        || mode == com.bloxarena.game.GameMode.DOMINATION
                        || mode == com.bloxarena.game.GameMode.CAPTURE_THE_FLAG;
                Location safe = respawnMode ? from : (map.getLobby() != null ? map.getLobby() : from);
                p.teleport(safe);
                gm.onPlayerDied(p, null, null);
            }
        }
        if (gm.getState() == com.bloxarena.game.GameState.WAITING && lm.hasLobbyOob()
                && lm.getLobbySpawn() != null && !isInBounds(to, lm.getLobbyOobMin(), lm.getLobbyOobMax())) {
            p.teleport(lm.getLobbySpawn());
        }
    }

    private boolean isInBounds(org.bukkit.Location loc, org.bukkit.Location min, org.bukkit.Location max) {
        if (loc == null || min == null || max == null) return true;
        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        return x >= Math.min(min.getX(), max.getX()) && x <= Math.max(min.getX(), max.getX())
            && y >= Math.min(min.getY(), max.getY()) && y <= Math.max(min.getY(), max.getY())
            && z >= Math.min(min.getZ(), max.getZ()) && z <= Math.max(min.getZ(), max.getZ());
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("bloxarena.admin")) return;
        // SPECTATOR・SURVIVALへの変更は許可（ゲームロジックが使用）
        if (e.getNewGameMode() == GameMode.SPECTATOR) return;
        if (e.getNewGameMode() == GameMode.SURVIVAL) return;
        // CREATIVEへの変更のみブロック
        GameState state = gm.getState();
        if (state == GameState.WAITING && lm.getWaitingPlayers().contains(p.getUniqueId())) {
            if (e.getNewGameMode() == GameMode.CREATIVE) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        if (!gm.isParticipant(victim)) return;
        e.setDeathMessage(null);
        e.getDrops().clear();
        e.setDroppedExp(0);
        java.util.UUID lastKillerUuid = lastDamager.remove(victim.getUniqueId());
        // Clean entries where this victim was someone else's last damager
        lastDamager.entrySet().removeIf(ent -> ent.getValue().equals(victim.getUniqueId()));
        Player resolvedKiller = lastKillerUuid != null ? Bukkit.getPlayer(lastKillerUuid) : victim.getKiller();
        gm.onPlayerDied(victim, resolvedKiller, lastKillerUuid);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!gm.isParticipant(p)) return;
        if (gm.getState() != GameState.IN_GAME) return;
        MapConfig map = gm.getCurrentMap();
        if (map != null && map.getCenter() != null) {
            e.setRespawnLocation(map.getCenter().clone().add(0, 5, 0));
        }
        // リスポーン後はGameManager側の1tickタスクでSPECTATOR化済みのため何もしない
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (gm.getState() == GameState.WAITING) { e.setCancelled(true); return; }
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && gm.hasNoFallDamage(p)) {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 0.8f);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.isOp()) return;
        // IN_GAME以外でもセンターゾーンは保護
        if (gm.getState() != GameState.IN_GAME) {
            if (gm.isInCenterZone(e.getBlock())) e.setCancelled(true);
            return;
        }
        if (!gm.isParticipant(p)) { e.setCancelled(true); return; }
        if (!gm.isInCenterZone(e.getBlock())) { e.setCancelled(true); p.sendMessage("§c中央コンクリート以外は破壊できません。"); return; }
        // 3分ロック中は操作不可
        if (gm.isObjectiveLocked()) {
            e.setCancelled(true);
            p.sendMessage("§c⏳ オブジェクトはまだロック中です！（試合開始2分後に解放）");
            return;
        }
        int cy = gm.getCenterY();
        if (e.getBlock().getY() != cy) { e.setCancelled(true); return; }
        Material mat = e.getBlock().getType();
        if (mat == Material.RED_CONCRETE || mat == Material.CYAN_CONCRETE || mat == Material.WHITE_CONCRETE) {
            e.setDropItems(false);
            gm.onObjectiveBlockBroken(); // ホールドタイマーリセット
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (gm.getState() != GameState.IN_GAME) return;
        Player p = e.getPlayer();
        if (!gm.isParticipant(p)) { e.setCancelled(true); return; }
        if (!gm.isInCenterZone(e.getBlockPlaced())) {
            if (!p.isOp()) e.setCancelled(true);
            return;
        }
        // 3分ロック中は操作不可
        if (gm.isObjectiveLocked() && !p.isOp()) {
            e.setCancelled(true);
            p.sendMessage("§c⏳ オブジェクトはまだロック中です！（試合開始2分後に解放）");
            return;
        }
        Material placed = e.getBlockPlaced().getType();
        if (placed != Material.RED_CONCRETE && placed != Material.CYAN_CONCRETE) {
            if (!p.isOp()) e.setCancelled(true);
            return;
        }
        Location loc = e.getBlockPlaced().getLocation();
        Bukkit.getScheduler().runTaskLater(plugin, () -> gm.checkObjectiveWin(loc), 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // KIT_SELECT中はインベントリ操作をすべてブロック（ホットバーはそのまま使用）
        if (gm.getState() == com.bloxarena.game.GameState.KIT_SELECT) {
            e.setCancelled(true);
            return;
        }
        String title = e.getView().getTitle();
        if (title.equals(KitEditorGUI.LIST_TITLE) || title.startsWith(KitEditorGUI.EDIT_PREFIX)) {
            plugin.getKitEditorGUI().handleClick(e);
        } else if (title.equals(com.bloxarena.kit.KitInfoGUI.LIST_TITLE)
                || title.startsWith(com.bloxarena.kit.KitInfoGUI.DETAIL_PREFIX)) {
            plugin.getKitInfoGUI().handleClick(e);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        // KIT_SELECT中とIN_GAME中はアイテムドロップ禁止
        if (gm.getState() == GameState.KIT_SELECT || gm.getState() == GameState.IN_GAME) {
            if (gm.isParticipant(e.getPlayer())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent e) {
        // KIT_SELECT中はオフハンドへの移動禁止
        if (gm.getState() == GameState.KIT_SELECT) e.setCancelled(true);
    }

    /** ホットバーキット選択: 右クリックで選択・ページ切り替え (メインハンドのみ) */
    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent e) {
        if (e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;

        // ─ KIT_SELECT state: route ANY interaction to GUI (Bedrock compatible) ─
        if (gm.getState() == GameState.KIT_SELECT) {
            Player p = e.getPlayer();
            if (!gm.isParticipant(p)) return;
            e.setCancelled(true);
            if (activeGUI != null) activeGUI.onInteract(p);
            return;
        }

        org.bukkit.event.block.Action action = e.getAction();
        boolean isRight = action == org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                       || action == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
        boolean isLeft  = action == org.bukkit.event.block.Action.LEFT_CLICK_AIR
                       || action == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
        if (!isRight && !isLeft) return;
        Player p = e.getPlayer();

        // ─ KIT_SELECT state: route ANY interaction to GUI (Bedrock compatible) ─
        if (gm.getState() == GameState.KIT_SELECT) {
            if (!gm.isParticipant(p)) return;
            e.setCancelled(true);
            if (activeGUI != null) activeGUI.onInteract(p);
            return;
        }

        // Test field mode: skip normal game logic, but allow skill/burst items
        if (plugin.getTestFieldManager().isTester(p)) {
            org.bukkit.inventory.ItemStack held = p.getInventory().getItemInMainHand();
            // Allow bows and crossbows to function normally
            if (held.getType() == Material.BOW || held.getType() == Material.CROSSBOW
                    || held.getType() == Material.SHIELD || held.getType() == Material.ENDER_PEARL) {
                return; // don't cancel, let vanilla handle
            }
            if (held != null && held.getItemMeta() != null
                    && (held.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "kit_skill"), org.bukkit.persistence.PersistentDataType.STRING)
                     || held.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "burst_skill"), org.bukkit.persistence.PersistentDataType.BYTE))) {
                plugin.getSkillManager().onRightClick(p, held);
            }
            e.setCancelled(true);
            return;
        }

        // ─ キット一覧ガイドアイテム（WAITING状態でも使用可） ─
        if (gm.getState() == GameState.WAITING
                && plugin.getKitInfoGUI().isGuideItem(p.getInventory().getItemInMainHand())) {
            e.setCancelled(true);
            plugin.getKitInfoGUI().openList(p);
            return;
        }

        // ─ キット選択（KIT_SELECT状態のみ） ─
        if (gm.getState() == GameState.KIT_SELECT) {
            if (!gm.isParticipant(p)) return;
            e.setCancelled(true);
            if (activeGUI != null) activeGUI.onInteract(p);
            return;
        }

        // ─ スキル発動（IN_GAME状態のみ） ─
        if (gm.getState() == GameState.IN_GAME && gm.isParticipant(p) && !gm.isSpectator(p)) {
            org.bukkit.inventory.ItemStack held = p.getInventory().getItemInMainHand();
            if (isRight) plugin.getSkillManager().onRightClick(p, held);
            plugin.getSkillManager().onClick(p, held, isLeft);
            // Sundance revolver crossbow tracking (right-click only)
            if (isRight && p.getInventory().getItemInMainHand().getType() == Material.CROSSBOW) {
                plugin.getSkillManager().onCrossbowShoot(p);
            }
            // CTF flag pickup
            if (gm.getCurrentGameMode() == com.bloxarena.game.GameMode.CAPTURE_THE_FLAG) {
                if (e.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
                        && e.getClickedBlock() != null
                        && (e.getClickedBlock().getType() == Material.RED_BANNER
                         || e.getClickedBlock().getType() == Material.CYAN_BANNER)) {
                    e.setCancelled(true);
                }
                gm.tryPickupFlag(p);
                gm.tryPickupDroppedFlag(p);
            }

            // Bomb mission plant/defuse
            if (gm.getCurrentGameMode() == com.bloxarena.game.GameMode.BOMB_MISSION) {
                if (e.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK || e.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                    gm.tryPlantBomb(p);
                    gm.tryDefuseBomb(p);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent e) {
        // ホットバー方式ではInventoryClose不要（no-op）
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        // Test field: prevent tester damage, allow dummy damage
        if (e.getEntity() instanceof Player victim && plugin.getTestFieldManager().isTester(victim)) {
            e.setCancelled(true); return;
        }
        // Dummy damage tracking
        if (e.getEntity() instanceof org.bukkit.entity.Zombie z && plugin.getTestFieldManager().isDummy(z)) {
            // Damage allowed, show HP
            if (e.getDamager() instanceof Player p) {
                p.sendActionBar(net.kyori.adventure.text.Component.text(
                    "§eダミー残HP: §c" + String.format("%.0f", z.getHealth() - e.getFinalDamage()) + "/100"));
            }
            return;
        }
        if (gm.getState() != GameState.IN_GAME) return;
        if (!(e.getEntity() instanceof Player victim)) return;
        com.bloxarena.game.TeamColor victimTeam = gm.getTeamOf(victim);
        if (e.getDamager() instanceof Player attacker) {
            if (victimTeam != null && victimTeam == gm.getTeamOf(attacker)) {
                e.setCancelled(true); return;
            }
        } else if (plugin.getBotManager().isBot(e.getDamager())) {
            java.util.UUID botUuid = plugin.getBotManager().getBotUuid(e.getDamager());
            com.bloxarena.game.TeamColor botTeam = plugin.getBotManager().getBotTeam(botUuid);
            if (botTeam != null && botTeam == victimTeam) {
                e.setCancelled(true); return;
            }
        }
        java.util.UUID killerUuid = null;
        if (e.getDamager() instanceof Player damager) {
            killerUuid = damager.getUniqueId();
            gm.getMatchStats().addDamage(killerUuid, e.getFinalDamage());
            plugin.getStatsManager().addDamage(killerUuid, e.getFinalDamage());
        } else if (plugin.getBotManager().isBot(e.getDamager())) {
            killerUuid = plugin.getBotManager().getBotUuid(e.getDamager());
        }
        if (killerUuid != null) {
            lastDamager.put(victim.getUniqueId(), killerUuid);
        }

        // Counter parry: cancel damage + counter
        if (e.getDamager() instanceof Player attacker && victimTeam != null) {
            if (plugin.getSkillManager().tryParryCounter(attacker, victim)) {
                e.setCancelled(true);
                return;
            }
        }

        if (plugin.getSkillManager().isGuardBroken(victim.getUniqueId()) && victim.isBlocking()) {
            victim.setCooldown(Material.SHIELD, 60);
        }

        if (e.getDamager() instanceof Player attacker && victimTeam != null
                && gm.getTeamOf(attacker) != victimTeam) {
            plugin.getSkillManager().tryUniversalGuardBreak(attacker, victim);
        }

        if (e.getDamager() instanceof Player attacker && victimTeam != null
                && gm.getPlayerKitType(attacker.getUniqueId()) == KitType.NILGIRITAR) {
            if (plugin.getSkillManager().tryPierceShield(attacker, victim)) {
                victim.setCooldown(Material.SHIELD, 20);
                victim.damage(3.0, attacker);
            }
        }

        // デコイ攻撃検知
        if (e.getDamager() instanceof Player damager && e.getEntity() instanceof org.bukkit.entity.Skeleton skeleton
                && skeleton.getPersistentDataContainer().has(new NamespacedKey(plugin, "decoy"), org.bukkit.persistence.PersistentDataType.BYTE)) {
            plugin.getSkillManager().onDecoyHit(skeleton, damager);
        }

        // ヒットエフェクト（クリティカルパーティクル＋サウンド）
        org.bukkit.Location loc = victim.getLocation().add(0, 1, 0);
        victim.getWorld().spawnParticle(org.bukkit.Particle.CRIT, loc, 8, 0.2, 0.2, 0.2, 0.15);
        victim.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_PLAYER_HURT, 0.6f, 1.1f);

        // Vampire damage + attack tracking
        if (e.getDamager() instanceof Player damager) {
            plugin.getSkillManager().onVampireDamageDealt(damager, e.getFinalDamage());
            plugin.getSkillManager().onVampireAttack(damager);
        }
        plugin.getSkillManager().onVampireDamaged(victim, e.getFinalDamage());

        if (e.getDamager() instanceof Player attacker && victimTeam != null
                && gm.getTeamOf(attacker) != victimTeam) {
            plugin.getSkillManager().onComboHit(attacker, victim);
        }

        // VAMPIRE Stage 4 lifesteal: heal on damage dealt
        if (e.getDamager() instanceof Player damager && gm.getPlayerKitType(damager.getUniqueId()) == KitType.VAMPIRE) {
            boolean inBlood = plugin.getSkillManager().isVampireBloodMode(damager.getUniqueId());
            double gauge = plugin.getSkillManager().getVampireGauge(damager.getUniqueId());
            if (inBlood && gauge >= 60) {
                damager.setHealth(Math.min(damager.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getBaseValue(), damager.getHealth() + 1));
            }
        }

        // Sniper mark kill check (for arrow projectiles)
        if (e.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player shooter) {
            if (plugin.getSkillManager().onSniperHit(shooter, victim)) {
                lastDamager.put(victim.getUniqueId(), shooter.getUniqueId());
                e.setCancelled(true);
                return;
            }
        }

        if (e.getDamager() instanceof Player attacker && gm.getPlayerKitType(attacker.getUniqueId()) == KitType.KREUTZ
                && victim.getPersistentDataContainer().has(new NamespacedKey(plugin, "kreutz_pierced"), org.bukkit.persistence.PersistentDataType.BYTE)) {
            java.util.UUID attackerUid = attacker.getUniqueId();
            if (plugin.getSkillManager().getPiercingRecently().contains(attackerUid)) return;
            plugin.getSkillManager().getPiercingRecently().add(attackerUid);
            victim.damage(2.0, attacker);
            victim.getPersistentDataContainer().remove(new NamespacedKey(plugin, "kreutz_pierced"));
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getSkillManager().getPiercingRecently().remove(attackerUid), 2L);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        org.bukkit.entity.LivingEntity entity = e.getEntity();
        // Test dummy: clear drops
        if (plugin.getTestFieldManager().isDummy(entity)) {
            e.getDrops().clear();
            e.setDroppedExp(0);
            return;
        }
        if (plugin.getBotManager().isBot(entity)) {
            e.getDrops().clear();
            e.setDroppedExp(0);
            Player killer = entity.getKiller();
            plugin.getBotManager().onEntityDeath(entity, killer);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        if (gm.isSpectator(sender)) {
            // スペクテーター同士 + ゲーム外プレイヤーのみに届ける（生存者には見えない）
            e.setCancelled(true);
            String specMsg = "§8[spec] §7" + sender.getName() + "§8: §7" + e.getMessage();
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player r : Bukkit.getOnlinePlayers()) {
                    if (gm.isSpectator(r) || !gm.isParticipant(r)) {
                        r.sendMessage(specMsg);
                    }
                }
            });
            return;
        }
        if (gm.getState() == com.bloxarena.game.GameState.IN_GAME && gm.isParticipant(sender)) {
            String msg = e.getMessage();
            com.bloxarena.game.TeamColor team = gm.getTeamOf(sender);
            if (msg.startsWith(".")) {
                e.setMessage(msg.substring(1).trim());
                e.setFormat("§7[全体] §f" + sender.getName() + "§7: §f%2$s");
            } else {
                // チームチャット: イベントはキャンセルせず、受信者のみフィルタしてLunaChatの変換を通す
                String prefix = team != null ? team.getColorCode() + "[" + team.getDisplayName() + "] §f" : "§7";
                e.setFormat(prefix + sender.getName() + "§7: §f%2$s");
                e.getRecipients().removeIf(r -> {
                    com.bloxarena.game.TeamColor rTeam = gm.getTeamOf(r);
                    return !(rTeam == team || gm.isSpectator(r) || r.equals(sender));
                });
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(org.bukkit.event.entity.ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!gm.isParticipant(shooter)) return;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Snowball) return;
        if (!(e.getEntity().getShooter() instanceof Player shooter)) return;
        if (!gm.isParticipant(shooter)) return;
        if (gm.getState() != GameState.IN_GAME) return;

        // Scout recon bolt
        if (e.getHitBlock() != null) {
            plugin.getSkillManager().scoutReconShot(shooter, e.getHitBlock().getLocation().add(0, 1, 0));
            plugin.getSkillManager().scoutPulseShot(shooter, e.getHitBlock().getLocation().add(0, 1, 0));
            // Clear sniper mark + marksman bolt on miss
            plugin.getSkillManager().clearSniperMarkOnShoot(shooter);
            shooter.getPersistentDataContainer().remove(new NamespacedKey(plugin, "heavy_bolt"));
        }
        // Marksman heavy bolt
        if (e.getHitEntity() instanceof Player victim && gm.isParticipant(victim)) {
            plugin.getSkillManager().marksmanHeavyBoltHit(shooter, victim);
            e.getEntity().remove(); // Cancel arrow damage - HP already reduced by bolt
        }
    }

    @EventHandler
    public void onSnowballLand(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Snowball ball)) return;
        if (!(ball.getShooter() instanceof Player shooter)) return;
        if (!gm.isParticipant(shooter)) return;

        if ("flashBang".equals(ball.getCustomName())) {
            plugin.getSkillManager().onFlashBangLand(e.getEntity().getLocation(), shooter);
        }
        if (ball.getPersistentDataContainer().has(new NamespacedKey(plugin, "grapple"), org.bukkit.persistence.PersistentDataType.BYTE)
                && e.getHitEntity() instanceof Player victim) {
            plugin.getSkillManager().onGrappleHit(ball, victim, shooter);
        }
        // Mega rocket
        if (ball.getPersistentDataContainer().has(new NamespacedKey(plugin, "mega_rocket"), org.bukkit.persistence.PersistentDataType.BYTE)) {
            plugin.getSkillManager().onMegaRocketHit(e.getEntity().getLocation(), shooter, ball);
            e.getEntity().remove();
        }
        // Micro rocket
        if (ball.getPersistentDataContainer().has(new NamespacedKey(plugin, "micro_rocket"), org.bukkit.persistence.PersistentDataType.BYTE)) {
            float power = plugin.getSkillManager().rocketHitPower(ball, e.getEntity().getLocation(), 1.5f);
            e.getEntity().getLocation().getWorld().createExplosion(e.getEntity().getLocation(), 0f, false, false, shooter);for(Entity e2:e.getEntity().getLocation().getWorld().getNearbyEntities(e.getEntity().getLocation(),3,2,3)){if(e2 instanceof Player t&&gm.isParticipant(t)&&gm.getTeamOf(t)!=gm.getTeamOf(shooter)){t.damage(power*3,shooter);}}
            e.getEntity().remove();
        }
        // Theos Pada (vampire)
        else if (ball.getPersistentDataContainer().has(new NamespacedKey(plugin, "theos_pada"), org.bukkit.persistence.PersistentDataType.BYTE)) {
            if (e.getHitEntity() != null) {
                plugin.getSkillManager().onTheosPadaHit(ball, shooter, e.getHitEntity());
            }
            e.getEntity().remove();
        }
        // Cook food throw
        else if (ball.getPersistentDataContainer().has(new NamespacedKey(plugin, "ba_cook"), org.bukkit.persistence.PersistentDataType.STRING)) {
            String matName = ball.getPersistentDataContainer().get(new NamespacedKey(plugin, "ba_cook"), org.bukkit.persistence.PersistentDataType.STRING);
            if (matName != null) {
                plugin.getSkillManager().cookHit(e.getEntity().getLocation(), shooter, org.bukkit.Material.valueOf(matName));
            }
            e.getEntity().remove();
        }
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        if (!gm.isParticipant(p) || gm.isSpectator(p)) return;
        if (gm.getState() != GameState.IN_GAME) return;
        if (e.isSneaking()) plugin.getSkillManager().onParryAttempt(p);
        plugin.getSkillManager().onGrangSneak(p, e.isSneaking());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPhantomHurt(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player p)) return;
        if (e.getDamage() > 0) plugin.getSkillManager().onPhantomDamaged(p);
    }

    @EventHandler
    public void onPortalMove(PlayerMoveEvent e) {
        if (!e.hasChangedBlock()) return;
        Player p = e.getPlayer();
        if (gm.getState() != GameState.IN_GAME) return;
        if (!gm.isParticipant(p) || gm.isSpectator(p)) return;
        plugin.getSkillManager().checkPortalTeleport(p, e.getTo());
    }

    @EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        Player p = e.getPlayer();
        // ゲームが終わっているのに SPECTATOR のまま残っていたら復帰させる
        if (!p.isOp() && p.getGameMode() == org.bukkit.GameMode.SPECTATOR
                && gm.getState() == GameState.WAITING) {
            p.setGameMode(org.bukkit.GameMode.ADVENTURE);
        }
        Long quitTime = disconnectedAt.remove(p.getUniqueId());
        if (quitTime != null) {
            long elapsed = System.currentTimeMillis() - quitTime;
            // タイムアウト超過 or 試合終了後 → インベントリクリアしてロビーへ
            if (elapsed > RECONNECT_WINDOW_MS || gm.getState() != GameState.IN_GAME) {
                p.getInventory().clear();
                p.setGameMode(org.bukkit.GameMode.ADVENTURE);
                Location lobby = lm.getLobbySpawn();
                if (lobby != null) p.teleport(lobby);
                p.sendMessage("§7試合から離脱しました。");
                return;
            }
            // 再接続: キットを再支給
        if (gm.getState() == GameState.IN_GAME && gm.isParticipant(p) && !gm.isSpectator(p)) {
            if (System.currentTimeMillis() - gm.getInGameStartTime() < 2000) return;
                com.bloxarena.kit.KitType kit = gm.getPlayerKits().get(p.getUniqueId());
                com.bloxarena.game.TeamColor team = gm.getTeam(p.getUniqueId());
                if (kit != null && team != null) {
                    p.getInventory().clear();
                    com.bloxarena.kit.KitBuilder.giveKit(p, kit, team, plugin);
                    p.sendMessage("§a再接続しました。キットを再支給しました。");
                }
            }
        }
    }

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (gm.isParticipant(p)) {
            disconnectedAt.put(p.getUniqueId(), System.currentTimeMillis());
        }
        if (lm != null) lm.onPlayerQuit(p);
    }

    @EventHandler
    public void onPearlTeleport(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
        Player p = e.getPlayer();
        if (!gm.isParticipant(p) || gm.isSpectator(p)) return;
        MapConfig map = gm.getCurrentMap();
        if (map != null && map.hasOob() && !isInBounds(e.getTo(), map.getOobMin(), map.getOobMax())) {
            e.setCancelled(true);
            p.sendMessage("§cエリア外へのテレポートは禁止されています。");
        }
    }

    @EventHandler
    public void onContainerOpen(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!gm.isParticipant(e.getPlayer())) return;
        if (gm.getSpectators().contains(e.getPlayer().getUniqueId())) return;
        if (e.getClickedBlock() != null && e.getClickedBlock().getType().name().contains("CHEST")
                || e.getClickedBlock() != null && e.getClickedBlock().getType().name().contains("BARREL")
                || e.getClickedBlock() != null && e.getClickedBlock().getType().name().contains("SHULKER")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§c試合中のチェスト利用は禁止されています。");
        }
    }

    @EventHandler
    public void onItemFrameInteract(PlayerInteractEntityEvent e) {
        if (!gm.isParticipant(e.getPlayer())) return;
        if (gm.isSpectator(e.getPlayer())) return;
        if (e.getRightClicked() instanceof ItemFrame || e.getRightClicked() instanceof org.bukkit.entity.GlowItemFrame) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpectatorDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p && gm.isSpectator(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpectatorInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (gm.isSpectator(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpectatorPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p && gm.isSpectator(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {
        if (e.getView().getPlayer() instanceof Player p && gm.isParticipant(p) && !gm.isSpectator(p)) {
            e.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();
        if (gm.isParticipant(p) && !gm.isSpectator(p)) {
            Inventory inv = e.getInventory();
            if (inv.getType() == InventoryType.WORKBENCH 
                    || inv.getType() == InventoryType.GRINDSTONE
                    || inv.getType() == InventoryType.ANVIL
                    || inv.getType() == InventoryType.SMITHING
                    || inv.getType() == InventoryType.ENCHANTING
                    || inv.getType() == InventoryType.BREWING
                    || inv.getType() == InventoryType.BEACON
                    || inv.getType() == InventoryType.FURNACE) {
                e.setCancelled(true);
                p.sendMessage("§c試合中の作業台・設備の利用は禁止されています。");
            }
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        if (!gm.isParticipant(e.getPlayer())) return;
        if (gm.isSpectator(e.getPlayer())) return;
        if (e.getRightClicked() instanceof org.bukkit.entity.Villager
                || e.getRightClicked() instanceof org.bukkit.entity.AbstractVillager
                || e.getRightClicked() instanceof org.bukkit.entity.Allay) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onConsume(org.bukkit.event.player.PlayerItemConsumeEvent e) {
        if (e.getItem().getItemMeta() != null
                && e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "ba_cook"), org.bukkit.persistence.PersistentDataType.STRING)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cこの食材は直接食べられません。右クリで使用 / 左クリで投擲してください。");
        }
    }

    @EventHandler
    public void onBowShoot(org.bukkit.event.entity.EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player p && plugin.getSkillManager().isDeadlocked(p.getUniqueId())) {
            e.setCancelled(true);
            p.sendMessage("§cデッドロック中は武器を使用できません！");
        }
    }

}