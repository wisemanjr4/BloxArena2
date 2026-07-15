package com.bloxarena.bot;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.TeamColor;
import com.bloxarena.map.MapConfig;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * テスト試合用BOTマネージャー。
 *
 * Citizens がサーバーに入っている場合: プレイヤーNPC として生成（外見・当たり判定が実プレイヤーと同一）
 * Citizens がない場合: Zombie エンティティにフォールバック
 *
 * ゲームロジック上の死亡処理はどちらも EntityDeathEvent / NPCDeathEvent 経由で統一。
 * キル帰属は GameListeners の lastDamager マップで管理するため、
 * getKiller() の戻り値に依存しない。
 */
public class BotManager {

    private final BloxArenaPlugin plugin;
    private final boolean hasCitizens;

    /** botUUID → LivingEntity (Zombie または Citizens NPC entity) */
    private final Map<UUID, LivingEntity> bots         = new LinkedHashMap<>();
    /** entity entityId → botUUID */
    private final Map<Integer, UUID>      entityToBot  = new HashMap<>();
    /** botUUID → TeamColor */
    private final Map<UUID, TeamColor>    botTeam      = new HashMap<>();
    /** Citizens NPC ID → botUUID (Citizens使用時のみ) */
    private final Map<Integer, UUID>      npcIdToBot   = new HashMap<>();
    /** ゲーム開始待ちBOTのUUID */
    private final List<UUID>              pendingBots  = new ArrayList<>();

    private BukkitTask aiTask;
    private int nameCounter = 0;

    public BotManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.hasCitizens = Bukkit.getPluginManager().isPluginEnabled("Citizens");
        if (hasCitizens) {
            plugin.getLogger().info("[BAII WoNG] Citizens を検出しました。BOTはプレイヤーNPCとしてスポーンします。");
        } else {
            plugin.getLogger().info("[BAII WoNG] Citizens が見つかりません。BOTはZombieエンティティを使用します。");
        }
    }

    // ─── BOT追加 ───

    public int addPendingBots(int count) {
        int added = 0;
        for (int i = 0; i < count; i++) {
            pendingBots.add(UUID.randomUUID());
            added++;
        }
        return added;
    }

    /** ゲーム開始時にBOTを実体化してチームへ分配 */
    public void spawnBotsForGame(List<UUID> redTeam, List<UUID> blueTeam, MapConfig map) {
        if (pendingBots.isEmpty()) return;

        boolean assignRed = redTeam.size() <= blueTeam.size();
        for (UUID uuid : new ArrayList<>(pendingBots)) {
            TeamColor team = assignRed ? TeamColor.RED : TeamColor.BLUE;
            if (team == TeamColor.RED) redTeam.add(uuid);
            else                       blueTeam.add(uuid);
            botTeam.put(uuid, team);
            assignRed = !assignRed;

            Location spawnMin = team == TeamColor.RED ? map.getRedSpawnMin() : map.getBlueSpawnMin();
            Location spawnMax = team == TeamColor.RED ? map.getRedSpawnMax() : map.getBlueSpawnMax();
            if (spawnMin == null || spawnMax == null) continue;

            nameCounter++;
            String name = (team == TeamColor.RED ? "§c" : "§9") + "[BOT] §f" + nameCounter;
            Location loc = randomInZone(spawnMin, spawnMax);

            if (hasCitizens) spawnCitizensNPC(uuid, name, loc, team);
            else             spawnZombie(uuid, name, loc, team);
        }
        pendingBots.clear();
        startAI();
    }

    // ─── Citizens NPC ───

    /** Citizens API をリフレクション経由で呼ぶ（コンパイル時依存なし）。失敗時はZombieにフォールバック。 */
    private void spawnCitizensNPC(UUID uuid, String displayName, Location loc, TeamColor team) {
        try {
            Class<?> apiClass      = Class.forName("net.citizensnpcs.api.CitizensAPI");
            Object   registry      = apiClass.getMethod("getNPCRegistry").invoke(null);
            Class<?> registryClass = Class.forName("net.citizensnpcs.api.npc.NPCRegistry");
            Object   npc           = registryClass.getMethod("createNPC", EntityType.class, String.class)
                                         .invoke(registry, EntityType.PLAYER, displayName);
            Class<?> npcClass = Class.forName("net.citizensnpcs.api.npc.NPC");
            npcClass.getMethod("spawn", Location.class).invoke(npc, loc);

            Object entity = npcClass.getMethod("getEntity").invoke(npc);
            int    npcId  = (int) npcClass.getMethod("getId").invoke(npc);

            if (entity instanceof LivingEntity le) {
                if (le.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null)
                    le.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
                le.setHealth(20.0);
                Material wool = team == TeamColor.RED ? Material.RED_WOOL : Material.CYAN_WOOL;
                le.getEquipment().setHelmet(new ItemStack(wool));
                le.getEquipment().setHelmetDropChance(0f);

                bots.put(uuid, le);
                entityToBot.put(le.getEntityId(), uuid);
                npcIdToBot.put(npcId, uuid);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[BAII WoNG] Citizens NPC スポーン失敗、Zombieにフォールバック: " + e.getMessage());
            spawnZombie(uuid, displayName, loc, team);
        }
    }

    // ─── Zombie (フォールバック) ───

    private void spawnZombie(UUID uuid, String displayName, Location loc, TeamColor team) {
        World world = loc.getWorld();
        if (world == null) return;

        Zombie z = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
        z.setCustomName(displayName);
        z.setCustomNameVisible(true);
        z.setBaby(false);
        z.setRemoveWhenFarAway(false);
        z.setShouldBurnInDay(false);
        z.setCanPickupItems(false);

        if (z.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null)
            z.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        z.setHealth(20.0);
        if (z.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null)
            z.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.20);

        z.getEquipment().setItemInMainHandDropChance(0f);
        z.getEquipment().setHelmetDropChance(0f);
        Material wool = team == TeamColor.RED ? Material.RED_WOOL : Material.CYAN_WOOL;
        z.getEquipment().setHelmet(new ItemStack(wool));

        bots.put(uuid, z);
        entityToBot.put(z.getEntityId(), uuid);
    }

    // ─── AI (共通) ───

    private void startAI() {
        if (aiTask != null) aiTask.cancel();
        aiTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickAI, 40L, 40L);
    }

    private void tickAI() {
        var gm = plugin.getGameManager();
        for (Map.Entry<UUID, LivingEntity> entry : new ArrayList<>(bots.entrySet())) {
            UUID botUuid = entry.getKey();
            LivingEntity entity = entry.getValue();
            if (!entity.isValid() || entity.isDead()) continue;

            TeamColor myTeam = botTeam.get(botUuid);
            if (myTeam == null) continue;

            // 最寄りの敵プレイヤーを探す
            Player nearest = null;
            double nearestDist = Double.MAX_VALUE;
            for (Player p : entity.getWorld().getPlayers()) {
                if (!gm.isParticipant(p)) continue;          // 試合参加者以外は無視
                if (p.getGameMode() == GameMode.SPECTATOR) continue;
                if (gm.getTeamOf(p) == myTeam) continue;
                double d = p.getLocation().distanceSquared(entity.getLocation());
                if (d < nearestDist) { nearestDist = d; nearest = p; }
            }
            if (nearest == null) continue;

            // Citizens NPC の場合はナビゲーターで追跡（リフレクション）
            if (hasCitizens) {
                try {
                    Class<?> apiClass  = Class.forName("net.citizensnpcs.api.CitizensAPI");
                    Object   registry  = apiClass.getMethod("getNPCRegistry").invoke(null);
                    Class<?> regClass  = Class.forName("net.citizensnpcs.api.npc.NPCRegistry");
                    Object   npc       = regClass.getMethod("getNPC", Entity.class).invoke(registry, entity);
                    if (npc != null) {
                        Class<?> npcClass = Class.forName("net.citizensnpcs.api.npc.NPC");
                        Object   nav      = npcClass.getMethod("getNavigator").invoke(npc);
                        nav.getClass().getMethod("setTarget", org.bukkit.entity.LivingEntity.class, boolean.class)
                           .invoke(nav, nearest, true);
                        if (nearestDist < 3.0) nearest.damage(1.5, entity);
                        continue;
                    }
                } catch (Exception ignored) {}
            }

            // Zombie の場合: ターゲット + 近接補助ダメージ
            if (entity instanceof Mob mob) mob.setTarget(nearest);
            if (nearestDist < 4.0) nearest.damage(2.0, entity);
        }
    }

    // ─── 死亡処理 ───

    /** EntityDeathEvent / NPCDeathEvent から呼ぶ */
    public boolean onEntityDeath(Entity entity, Player killer) {
        UUID botUuid = entityToBot.remove(entity.getEntityId());
        if (botUuid == null) return false;
        bots.remove(botUuid);
        botTeam.remove(botUuid);
        // Citizens NPC IDの逆引きエントリも削除
        npcIdToBot.values().remove(botUuid);
        plugin.getGameManager().onBotDied(botUuid, killer);
        return true;
    }

    /** Citizens NPCDeathEvent 用 (NPC ID で引く) */
    public boolean onNPCDeath(int npcId, Player killer) {
        UUID botUuid = npcIdToBot.remove(npcId);
        if (botUuid == null) return false;
        LivingEntity le = bots.remove(botUuid);
        if (le != null) entityToBot.remove(le.getEntityId());
        botTeam.remove(botUuid);
        plugin.getGameManager().onBotDied(botUuid, killer);
        return true;
    }

    public boolean isBot(Entity entity) { return entityToBot.containsKey(entity.getEntityId()); }
    public UUID   getBotUuid(Entity e)  { return entityToBot.get(e.getEntityId()); }

    // ─── クリーンアップ ───

    public void clearAll() {
        if (aiTask != null) { aiTask.cancel(); aiTask = null; }

        for (LivingEntity e : bots.values()) {
            if (!e.isValid()) continue;
            if (hasCitizens) {
                try {
                    Class<?> apiClass = Class.forName("net.citizensnpcs.api.CitizensAPI");
                    Object   registry = apiClass.getMethod("getNPCRegistry").invoke(null);
                    Class<?> regClass = Class.forName("net.citizensnpcs.api.npc.NPCRegistry");
                    Object   npc      = regClass.getMethod("getNPC", Entity.class).invoke(registry, e);
                    if (npc != null) {
                        npc.getClass().getMethod("destroy").invoke(npc);
                        continue;
                    }
                } catch (Exception ignored) {}
            }
            e.remove();
        }
        bots.clear();
        entityToBot.clear();
        botTeam.clear();
        npcIdToBot.clear();
        pendingBots.clear();
        nameCounter = 0;
    }

    // ─── 情報 ───

    public int       getPendingCount()               { return pendingBots.size(); }
    public List<UUID> getPendingBots()               { return Collections.unmodifiableList(pendingBots); }
    public int       getTotalBotCount()              { return bots.size(); }
    public TeamColor getBotTeam(UUID uuid)           { return botTeam.get(uuid); }
    public boolean   isUsingCitizens()               { return hasCitizens; }

    public int getAliveBotCount(TeamColor team) {
        return (int) botTeam.entrySet().stream()
            .filter(e -> e.getValue() == team)
            .filter(e -> { LivingEntity le = bots.get(e.getKey()); return le != null && le.isValid() && !le.isDead(); })
            .count();
    }

    // ─── ユーティリティ ───

    private Location randomInZone(Location min, Location max) {
        Random r = new Random();
        double x = min.getX() + r.nextDouble() * Math.abs(max.getX() - min.getX());
        double y = Math.max(min.getY(), max.getY());
        double z = min.getZ() + r.nextDouble() * Math.abs(max.getZ() - min.getZ());
        return new Location(min.getWorld(), x, y, z);
    }
}
