/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.bloxarena.kit;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameManager;
import com.bloxarena.game.TeamColor;
import com.bloxarena.kit.KitBuilder;
import com.bloxarena.kit.KitType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class KitSelectGUI {
    public static final String GUI_TITLE = "__hotbar__";
    private static final int KITS_PER_PAGE = 7;
    private final BloxArenaPlugin plugin;
    private final GameManager gm;
    private final NamespacedKey KIT_KEY;
    private final NamespacedKey NAV_KEY;
    private final Set<UUID> confirmed = new HashSet<UUID>();
    private final Set<UUID> allPlayers = new HashSet<UUID>();
    private final Map<UUID, Integer> pages = new HashMap<UUID, Integer>();
    private BukkitTask timeoutTask;
    private BukkitTask watchdogTask;
    private BukkitTask countdownTask;
    private boolean done = false;

    public KitSelectGUI(BloxArenaPlugin plugin, GameManager gm) {
        this.plugin = plugin;
        this.gm = gm;
        this.KIT_KEY = new NamespacedKey((Plugin)plugin, "ks_kit");
        this.NAV_KEY = new NamespacedKey((Plugin)plugin, "ks_nav");
    }

    public void openForAll(List<UUID> redTeam, List<UUID> blueTeam, int timeoutSeconds) {
        this.allPlayers.addAll(redTeam);
        this.allPlayers.addAll(blueTeam);
        for (UUID uid : this.allPlayers) {
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            this.renderHotbar(p);
        }
        this.watchdogTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (this.done) {
                this.watchdogTask.cancel();
                return;
            }
            for (UUID uid : this.allPlayers) {
                Player p;
                if (this.confirmed.contains(uid) || (p = Bukkit.getPlayer((UUID)uid)) == null || !p.isOnline() || this.hasHotbarRendered(p)) continue;
                this.renderHotbar(p);
            }
        }, 20L, 20L);
        int[] remaining = new int[]{timeoutSeconds};
        this.countdownTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (this.done) {
                this.countdownTask.cancel();
                return;
            }
            for (UUID uid : this.allPlayers) {
                Player p;
                if (this.confirmed.contains(uid) || (p = Bukkit.getPlayer((UUID)uid)) == null) continue;
                KitType heldKit = this.getHeldKit(p);
                String title = heldKit != null ? "\u00a7e" + heldKit.getName() : "\u00a7e\u30ad\u30c3\u30c8\u3092\u9078\u629e";
                p.sendTitle(title, "\u00a7e\u6b8b\u308a \u00a7c" + remaining[0] + "\u00a7e \u79d2", 0, 20, 0);
                if (heldKit != null) {
                    p.sendActionBar((Component)Component.text((String)("\u00a77" + heldKit.getDescription() + " \u00a7f" + heldKit.getLore())));
                } else {
                    p.sendActionBar((Component)Component.text((String)"\u00a77\u30db\u30c3\u30c8\u30d0\u30fc\u306e\u30ad\u30c3\u30c8\u3092\u6301\u3063\u3066\u78ba\u8a8d"));
                }
            }
            remaining[0] = remaining[0] - 1;
        }, 0L, 20L);
        this.timeoutTask = Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            for (UUID uid : this.allPlayers) {
                Player p;
                if (this.confirmed.contains(uid) || (p = Bukkit.getPlayer((UUID)uid)) == null) continue;
                this.autoSelectKit(p);
            }
            if (!this.done) {
                this.done = true;
                if (this.watchdogTask != null) {
                    this.watchdogTask.cancel();
                    this.watchdogTask = null;
                }
                if (this.countdownTask != null) {
                    this.countdownTask.cancel();
                    this.countdownTask = null;
                }
                this.giveKitsToAll();
                this.gm.onKitSelectDone();
            }
        }, (long)timeoutSeconds * 20L);
    }

    public void onInteract(Player p) {
        if (this.confirmed.contains(p.getUniqueId())) {
            return;
        }
        if (this.done) {
            return;
        }
        ItemStack held = p.getInventory().getItemInMainHand();
        if (held == null || held.getType() == Material.AIR) {
            return;
        }
        ItemMeta meta = held.getItemMeta();
        if (meta == null) {
            return;
        }
        String nav = (String)meta.getPersistentDataContainer().get(this.NAV_KEY, PersistentDataType.STRING);
        if (nav != null) {
            int page = this.pages.getOrDefault(p.getUniqueId(), 0);
            int maxPage = (KitType.values().length - 1) / 7;
            if ("PREV".equals(nav) && page > 0) {
                this.pages.put(p.getUniqueId(), page - 1);
            }
            if ("NEXT".equals(nav) && page < maxPage) {
                this.pages.put(p.getUniqueId(), page + 1);
            }
            this.renderHotbar(p);
            return;
        }
        String kitName = (String)meta.getPersistentDataContainer().get(this.KIT_KEY, PersistentDataType.STRING);
        if (kitName == null) {
            return;
        }
        KitType kit = this.findKit(kitName);
        if (kit == null) {
            return;
        }
        if (kit == KitType.SUPERIOR_MISTRAL && !p.getName().equals("Photon_wisemanjr")) {
            p.sendMessage("\u00a7c\u305d\u306e\u30ad\u30c3\u30c8\u306f\u9078\u629e\u3067\u304d\u307e\u305b\u3093\u3002");
            return;
        }
        if (this.gm.isKitTakenInTeam(p.getUniqueId(), kit.name())) {
            p.sendMessage("\u00a7c\u305d\u306e\u30ad\u30c3\u30c8\u306f\u30c1\u30fc\u30e0\u30e1\u30f3\u30d0\u30fc\u304c\u9078\u629e\u6e08\u307f\u3067\u3059\u3002");
            return;
        }
        this.gm.setPlayerKit(p.getUniqueId(), kit.name());
        this.plugin.getStatsManager().addKitPick(p.getUniqueId(), kit.name());
        this.confirmed.add(p.getUniqueId());
        TeamColor team = this.gm.getKitTeam(p.getUniqueId());
        if (team != null) {
            KitBuilder.giveKit(p, kit, team, this.plugin);
        }
        p.sendMessage("\u00a7a" + kit.getDisplayName() + " \u00a7a\u3092\u9078\u629e\u3057\u307e\u3057\u305f\uff01");
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
        this.refreshForTeam(p.getUniqueId());
        this.checkAllSelected();
    }

    public void onInventoryClose(Player p) {
    }

    private void renderHotbar(Player p) {
        int ki;
        int page = this.pages.getOrDefault(p.getUniqueId(), 0);
        KitType[] kits = KitType.values();
        int start = page * 7;
        int maxPage = (kits.length - 1) / 7;
        for (int slot = 0; slot < 9; ++slot) {
            p.getInventory().setItem(slot, null);
        }
        if (page > 0) {
            p.getInventory().setItem(0, this.navItem("\u00a7e\u00a7l\u25c0 \u524d\u306e\u30da\u30fc\u30b8", "PREV"));
        }
        for (int i = 0; i < 7 && (ki = start + i) < kits.length; ++i) {
            ItemStack kitStack = this.kitItem(p, kits[ki]);
            if (kitStack == null) continue;
            p.getInventory().setItem(i + 1, kitStack);
        }
        if (page < maxPage) {
            p.getInventory().setItem(8, this.navItem("\u00a7e\u00a7l\u6b21\u306e\u30da\u30fc\u30b8 \u25b6", "NEXT"));
        }
        if (!this.pages.containsKey(p.getUniqueId())) {
            p.sendMessage("\u00a76\u00a7l\u30ad\u30c3\u30c8\u3092\u9078\u629e: \u00a7f\u53f3\u30af\u30ea\u30c3\u30af\u3067\u9078\u629e \u00a77| \u00a7e\u30da\u30fc\u30b8: " + (page + 1) + "/" + (maxPage + 1));
        }
        this.pages.put(p.getUniqueId(), page);
    }

    private boolean hasHotbarRendered(Player p) {
        ItemStack item = p.getInventory().getItem(1);
        if (item == null || item.getItemMeta() == null) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(this.KIT_KEY, PersistentDataType.STRING);
    }

    private void refreshForTeam(UUID selectedBy) {
        TeamColor team = this.gm.getTeamOf(Bukkit.getPlayer((UUID)selectedBy));
        if (team == null) {
            return;
        }
        List<UUID> teammates = team == TeamColor.RED ? this.gm.getRedTeam() : this.gm.getBlueTeam();
        for (UUID uid : teammates) {
            Player p;
            if (this.confirmed.contains(uid) || (p = Bukkit.getPlayer((UUID)uid)) == null) continue;
            this.renderHotbar(p);
        }
    }

    private void checkAllSelected() {
        if (this.done) {
            return;
        }
        if (this.confirmed.containsAll(this.allPlayers)) {
            this.done = true;
            if (this.timeoutTask != null) {
                this.timeoutTask.cancel();
                this.timeoutTask = null;
            }
            if (this.watchdogTask != null) {
                this.watchdogTask.cancel();
                this.watchdogTask = null;
            }
            if (this.countdownTask != null) {
                this.countdownTask.cancel();
                this.countdownTask = null;
            }
            this.giveKitsToAll();
            this.gm.onKitSelectDone();
        }
    }

    private void giveKitsToAll() {
        for (UUID uid : this.allPlayers) {
            TeamColor team;
            Player p = Bukkit.getPlayer((UUID)uid);
            if (p == null) continue;
            String kitName = this.gm.getPlayerKit(uid);
            KitType kit = this.findKit(kitName);
            if (kit == null) {
                kit = KitType.BLADE;
            }
            if ((team = this.gm.getKitTeam(p.getUniqueId())) == null) continue;
            p.setItemOnCursor(null);
            KitBuilder.giveKit(p, kit, team, this.plugin);
        }
    }

    private void autoSelectKit(Player p) {
        if (this.confirmed.contains(p.getUniqueId())) {
            return;
        }
        for (KitType kit : KitType.values()) {
            if (kit == KitType.SUPERIOR_MISTRAL && !p.getName().equals("Photon_wisemanjr")) continue;
            if (this.gm.isKitTakenInTeam(p.getUniqueId(), kit.name())) continue;
            this.gm.setPlayerKit(p.getUniqueId(), kit.name());
            this.confirmed.add(p.getUniqueId());
            TeamColor t = this.gm.getKitTeam(p.getUniqueId());
            if (t != null) {
                KitBuilder.giveKit(p, kit, t, this.plugin);
            }
            p.sendMessage("\u00a7e\u81ea\u52d5\u9078\u629e: \u00a7a" + kit.getDisplayName());
            return;
        }
        this.gm.setPlayerKit(p.getUniqueId(), KitType.BLADE.name());
        this.confirmed.add(p.getUniqueId());
    }

    private ItemStack kitItem(Player p, KitType kit) {
        if (kit == KitType.SUPERIOR_MISTRAL && !p.getName().equals("Photon_wisemanjr")) {
            return null;
        }
        boolean taken = this.gm.isKitTakenInTeam(p.getUniqueId(), kit.name());
        Material mat = taken ? Material.BARRIER : this.iconMaterial(kit);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName((String)(taken ? "\u00a7c\u00a7m" + kit.getName() + " \u00a7c(\u9078\u629e\u6e08\u307f)" : kit.getDisplayName()));
        int mastery = this.plugin.getStatsManager().getKitMasteryLevel(p.getUniqueId(), kit.name());
        String rankName = this.plugin.getStatsManager().getKitMasteryRankName(mastery);
        meta.setLore(Arrays.asList("\u00a77" + kit.getDescription(), "\u00a7f" + kit.getLore(), "\u00a77\u30de\u30b9\u30bf\u30ea\u30fcLv." + mastery + " \u00a7e" + rankName, taken ? "\u00a7c\u9078\u629e\u4e0d\u53ef" : "\u00a7a\u25ba \u53f3\u30af\u30ea\u30c3\u30af\u3067\u9078\u629e"));
        if (!taken) {
            meta.getPersistentDataContainer().set(this.KIT_KEY, PersistentDataType.STRING, kit.name());
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack navItem(String name, String navValue) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName(name);
        meta.setLore(Collections.singletonList("\u00a77\u53f3\u30af\u30ea\u30c3\u30af\u3067\u30da\u30fc\u30b8\u5207\u308a\u66ff\u3048"));
        meta.getPersistentDataContainer().set(this.NAV_KEY, PersistentDataType.STRING, navValue);
        item.setItemMeta(meta);
        return item;
    }

    private Material iconMaterial(KitType kit) {
        return switch (kit) {
            case BLADE -> Material.DIAMOND_SWORD;
            case BREAKER -> Material.DIAMOND_AXE;
            case SCOUT -> Material.BOW;
            case FLASHER -> Material.GLOWSTONE_DUST;
            case ROCKETER -> Material.FIREWORK_ROCKET;
            case ALCHEMIST -> Material.BREWING_STAND;
            case TRAPPER -> Material.TRIPWIRE_HOOK;
            case GUARDIAN -> Material.TOTEM_OF_UNDYING;
            case NINJA -> Material.ENDER_PEARL;
            case BERSERKER -> Material.NETHERITE_AXE;
            case MEDIC -> Material.TIPPED_ARROW;
            case ENGINEER -> Material.IRON_PICKAXE;
            case SNIPER -> Material.CROSSBOW;
            case COUNTER -> Material.IRON_SWORD;
            case MARKSMAN -> Material.SPECTRAL_ARROW;
            case PYRO -> Material.BLAZE_POWDER;
            case LANCER -> Material.IRON_SWORD;
            case SUPPORTER -> Material.NETHER_WART;
            case JESTER -> Material.RABBIT_FOOT;
            case SUNDANCE -> Material.LIGHTNING_ROD;
            case VAMPIRE -> Material.REDSTONE;
            case BOMBER -> Material.TNT;
            case COOK -> Material.COOKED_BEEF;
            case WHIRLWIND -> Material.FEATHER;
            case NILGIRITAR -> Material.TRIDENT;
            case MISTRAL -> Material.WHITE_WOOL;
            case SWAPPER -> Material.ENDER_PEARL;
            case STICKER -> Material.FISHING_ROD;
            case DECOY -> Material.SKELETON_SKULL;
            case RESTRICTIONER -> Material.CHAIN;
            case TRANSPORTER -> Material.ENDER_EYE;
            case KREUTZ -> Material.BOOK;
            case PHANTOM -> Material.PHANTOM_MEMBRANE;
            case ANCHOR -> Material.LODESTONE;
            case RELEASER -> Material.FIRE_CHARGE;
            case GRANG -> Material.SHIELD;
            case NECRO -> Material.SKELETON_SKULL;
            case BULWARK -> Material.CRACKED_STONE_BRICKS;
            case TIMEKEEPER -> Material.CLOCK;
            case AEGIS -> Material.SHIELD;
            case HEXER -> Material.BREWING_STAND;
            case REFLECTOR -> Material.STONE;
            case GLACIES -> Material.PACKED_ICE;
            case SUPERIOR_MISTRAL -> Material.NETHER_STAR;
            default -> throw new IncompatibleClassChangeError();
        };
    }

    private KitType findKit(String name) {
        if (name == null) {
            return null;
        }
        try {
            return KitType.valueOf(name);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            for (KitType k : KitType.values()) {
                if (!k.getName().equals(name)) continue;
                return k;
            }
            return null;
        }
    }

    private KitType getHeldKit(Player p) {
        ItemStack held = p.getInventory().getItemInMainHand();
        if (held == null || held.getType() == Material.AIR || !held.hasItemMeta()) {
            return null;
        }
        String kitName = held.getItemMeta().getPersistentDataContainer().get(this.KIT_KEY, PersistentDataType.STRING);
        if (kitName == null) {
            return null;
        }
        return this.findKit(kitName);
    }
}

