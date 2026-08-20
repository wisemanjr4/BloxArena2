/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Mob
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Zombie
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.bloxarena.bot;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameManager;
import com.bloxarena.game.TeamColor;
import com.bloxarena.map.MapConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BotManager {
    private final BloxArenaPlugin plugin;
    private final boolean hasCitizens;
    private final Map<UUID, LivingEntity> bots = new LinkedHashMap<UUID, LivingEntity>();
    private final Map<Integer, UUID> entityToBot = new HashMap<Integer, UUID>();
    private final Map<UUID, TeamColor> botTeam = new HashMap<UUID, TeamColor>();
    private final Map<Integer, UUID> npcIdToBot = new HashMap<Integer, UUID>();
    private final List<UUID> pendingBots = new ArrayList<UUID>();
    private BukkitTask aiTask;
    private int nameCounter = 0;

    public BotManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.hasCitizens = Bukkit.getPluginManager().isPluginEnabled("Citizens");
        if (this.hasCitizens) {
            plugin.getLogger().info("[BAII WoNG] Citizens \u3092\u691c\u51fa\u3057\u307e\u3057\u305f\u3002BOT\u306f\u30d7\u30ec\u30a4\u30e4\u30fcNPC\u3068\u3057\u3066\u30b9\u30dd\u30fc\u30f3\u3057\u307e\u3059\u3002");
        } else {
            plugin.getLogger().info("[BAII WoNG] Citizens \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002BOT\u306fZombie\u30a8\u30f3\u30c6\u30a3\u30c6\u30a3\u3092\u4f7f\u7528\u3057\u307e\u3059\u3002");
        }
    }

    public int addPendingBots(int count) {
        int added = 0;
        for (int i = 0; i < count; ++i) {
            this.pendingBots.add(UUID.randomUUID());
            ++added;
        }
        return added;
    }

    public void spawnBotsForGame(List<UUID> redTeam, List<UUID> blueTeam, MapConfig map) {
        if (this.pendingBots.isEmpty()) {
            return;
        }
        boolean assignRed = redTeam.size() <= blueTeam.size();
        for (UUID uuid : new ArrayList<UUID>(this.pendingBots)) {
            Location spawnMax;
            TeamColor team;
            TeamColor teamColor = team = assignRed ? TeamColor.RED : TeamColor.BLUE;
            if (team == TeamColor.RED) {
                redTeam.add(uuid);
            } else {
                blueTeam.add(uuid);
            }
            this.botTeam.put(uuid, team);
            assignRed = !assignRed;
            Location spawnMin = team == TeamColor.RED ? map.getRedSpawnMin() : map.getBlueSpawnMin();
            Location location = spawnMax = team == TeamColor.RED ? map.getRedSpawnMax() : map.getBlueSpawnMax();
            if (spawnMin == null || spawnMax == null) continue;
            ++this.nameCounter;
            String name = (team == TeamColor.RED ? "\u00a7c" : "\u00a79") + "[BOT] \u00a7f" + this.nameCounter;
            Location loc = this.randomInZone(spawnMin, spawnMax);
            if (this.hasCitizens) {
                this.spawnCitizensNPC(uuid, name, loc, team);
                continue;
            }
            this.spawnZombie(uuid, name, loc, team);
        }
        this.pendingBots.clear();
        this.startAI();
    }

    private void spawnCitizensNPC(UUID uuid, String displayName, Location loc, TeamColor team) {
        try {
            Class<?> apiClass = Class.forName("net.citizensnpcs.api.CitizensAPI");
            Object registry = apiClass.getMethod("getNPCRegistry", new Class[0]).invoke(null, new Object[0]);
            Class<?> registryClass = Class.forName("net.citizensnpcs.api.npc.NPCRegistry");
            Object npc = registryClass.getMethod("createNPC", EntityType.class, String.class).invoke(registry, EntityType.PLAYER, displayName);
            Class<?> npcClass = Class.forName("net.citizensnpcs.api.npc.NPC");
            npcClass.getMethod("spawn", Location.class).invoke(npc, loc);
            Object entity = npcClass.getMethod("getEntity", new Class[0]).invoke(npc, new Object[0]);
            int npcId = (Integer)npcClass.getMethod("getId", new Class[0]).invoke(npc, new Object[0]);
            if (entity instanceof LivingEntity) {
                LivingEntity le = (LivingEntity)entity;
                if (le.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                    le.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
                }
                le.setHealth(20.0);
                Material wool = team == TeamColor.RED ? Material.RED_WOOL : Material.CYAN_WOOL;
                le.getEquipment().setHelmet(new ItemStack(wool));
                le.getEquipment().setHelmetDropChance(0.0f);
                this.bots.put(uuid, le);
                this.entityToBot.put(le.getEntityId(), uuid);
                this.npcIdToBot.put(npcId, uuid);
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("[BAII WoNG] Citizens NPC \u30b9\u30dd\u30fc\u30f3\u5931\u6557\u3001Zombie\u306b\u30d5\u30a9\u30fc\u30eb\u30d0\u30c3\u30af: " + e.getMessage());
            this.spawnZombie(uuid, displayName, loc, team);
        }
    }

    private void spawnZombie(UUID uuid, String displayName, Location loc, TeamColor team) {
        World world = loc.getWorld();
        if (world == null) {
            return;
        }
        Zombie z = (Zombie)world.spawnEntity(loc, EntityType.ZOMBIE);
        z.setCustomName(displayName);
        z.setCustomNameVisible(true);
        z.setBaby(false);
        z.setRemoveWhenFarAway(false);
        z.setShouldBurnInDay(false);
        z.setCanPickupItems(false);
        if (z.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            z.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        }
        z.setHealth(20.0);
        if (z.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            z.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        }
        z.getEquipment().setItemInMainHandDropChance(0.0f);
        z.getEquipment().setHelmetDropChance(0.0f);
        Material wool = team == TeamColor.RED ? Material.RED_WOOL : Material.CYAN_WOOL;
        z.getEquipment().setHelmet(new ItemStack(wool));
        this.bots.put(uuid, (LivingEntity)z);
        this.entityToBot.put(z.getEntityId(), uuid);
    }

    private void startAI() {
        if (this.aiTask != null) {
            this.aiTask.cancel();
        }
        this.aiTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, this::tickAI, 40L, 40L);
    }

    private void tickAI() {
        GameManager gm = this.plugin.getGameManager();
        for (Map.Entry<UUID, LivingEntity> entry : new ArrayList<Map.Entry<UUID, LivingEntity>>(this.bots.entrySet())) {
            TeamColor myTeam;
            UUID botUuid = entry.getKey();
            LivingEntity entity = entry.getValue();
            if (!entity.isValid() || entity.isDead() || (myTeam = this.botTeam.get(botUuid)) == null) continue;
            Player nearest = null;
            double nearestDist = Double.MAX_VALUE;
            for (Player p : entity.getWorld().getPlayers()) {
                double d;
                if (!gm.isParticipant(p) || p.getGameMode() == GameMode.SPECTATOR || gm.getTeamOf(p) == myTeam || !((d = p.getLocation().distanceSquared(entity.getLocation())) < nearestDist)) continue;
                nearestDist = d;
                nearest = p;
            }
            if (nearest == null) continue;
            if (this.hasCitizens) {
                try {
                    Class<?> apiClass = Class.forName("net.citizensnpcs.api.CitizensAPI");
                    Object registry = apiClass.getMethod("getNPCRegistry", new Class[0]).invoke(null, new Object[0]);
                    Class<?> regClass = Class.forName("net.citizensnpcs.api.npc.NPCRegistry");
                    Object npc = regClass.getMethod("getNPC", Entity.class).invoke(registry, entity);
                    if (npc != null) {
                        Class<?> npcClass = Class.forName("net.citizensnpcs.api.npc.NPC");
                        Object nav = npcClass.getMethod("getNavigator", new Class[0]).invoke(npc, new Object[0]);
                        nav.getClass().getMethod("setTarget", LivingEntity.class, Boolean.TYPE).invoke(nav, nearest, true);
                        if (!(nearestDist < 3.0)) continue;
                        nearest.damage(1.5, (Entity)entity);
                        continue;
                    }
                }
                catch (Exception apiClass) {
                    // empty catch block
                }
            }
            if (entity instanceof Mob) {
                Mob mob = (Mob)entity;
                mob.setTarget(nearest);
            }
            if (!(nearestDist < 4.0)) continue;
            nearest.damage(2.0, (Entity)entity);
        }
    }

    public boolean onEntityDeath(Entity entity, Player killer) {
        UUID botUuid = this.entityToBot.remove(entity.getEntityId());
        if (botUuid == null) {
            return false;
        }
        this.bots.remove(botUuid);
        this.botTeam.remove(botUuid);
        this.npcIdToBot.values().remove(botUuid);
        this.plugin.getGameManager().onBotDied(botUuid, killer);
        return true;
    }

    public boolean onNPCDeath(int npcId, Player killer) {
        UUID botUuid = this.npcIdToBot.remove(npcId);
        if (botUuid == null) {
            return false;
        }
        LivingEntity le = this.bots.remove(botUuid);
        if (le != null) {
            this.entityToBot.remove(le.getEntityId());
        }
        this.botTeam.remove(botUuid);
        this.plugin.getGameManager().onBotDied(botUuid, killer);
        return true;
    }

    public boolean isBot(Entity entity) {
        return this.entityToBot.containsKey(entity.getEntityId());
    }

    public UUID getBotUuid(Entity e) {
        return this.entityToBot.get(e.getEntityId());
    }

    public void clearAll() {
        if (this.aiTask != null) {
            this.aiTask.cancel();
            this.aiTask = null;
        }
        for (LivingEntity e : this.bots.values()) {
            if (!e.isValid()) continue;
            if (this.hasCitizens) {
                try {
                    Class<?> apiClass = Class.forName("net.citizensnpcs.api.CitizensAPI");
                    Object registry = apiClass.getMethod("getNPCRegistry", new Class[0]).invoke(null, new Object[0]);
                    Class<?> regClass = Class.forName("net.citizensnpcs.api.npc.NPCRegistry");
                    Object npc = regClass.getMethod("getNPC", Entity.class).invoke(registry, e);
                    if (npc != null) {
                        npc.getClass().getMethod("destroy", new Class[0]).invoke(npc, new Object[0]);
                        continue;
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            e.remove();
        }
        this.bots.clear();
        this.entityToBot.clear();
        this.botTeam.clear();
        this.npcIdToBot.clear();
        this.pendingBots.clear();
        this.nameCounter = 0;
    }

    public int getPendingCount() {
        return this.pendingBots.size();
    }

    public List<UUID> getPendingBots() {
        return Collections.unmodifiableList(this.pendingBots);
    }

    public int getTotalBotCount() {
        return this.bots.size();
    }

    public TeamColor getBotTeam(UUID uuid) {
        return this.botTeam.get(uuid);
    }

    public boolean isUsingCitizens() {
        return this.hasCitizens;
    }

    public int getAliveBotCount(TeamColor team) {
        return (int)this.botTeam.entrySet().stream().filter(e -> e.getValue() == team).filter(e -> {
            LivingEntity le = this.bots.get(e.getKey());
            return le != null && le.isValid() && !le.isDead();
        }).count();
    }

    private Location randomInZone(Location min, Location max) {
        Random r = new Random();
        double x = min.getX() + r.nextDouble() * Math.abs(max.getX() - min.getX());
        double y = Math.max(min.getY(), max.getY());
        double z = min.getZ() + r.nextDouble() * Math.abs(max.getZ() - min.getZ());
        return new Location(min.getWorld(), x, y, z);
    }
}

