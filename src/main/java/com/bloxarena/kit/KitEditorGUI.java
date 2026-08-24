/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package com.bloxarena.kit;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.TeamColor;
import com.bloxarena.kit.KitBuilder;
import com.bloxarena.kit.KitType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class KitEditorGUI {
    public static final String LIST_TITLE = "\u00a76\u00a7lKit Editor \u00a77- \u30ad\u30c3\u30c8\u4e00\u89a7";
    public static final String EDIT_PREFIX = "\u00a76\u00a7lEdit: \u00a7e";
    private final Map<UUID, KitType> editing = new HashMap<UUID, KitType>();
    private final BloxArenaPlugin plugin;
    private final NamespacedKey kitKey;

    public KitEditorGUI(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.kitKey = new NamespacedKey((Plugin)plugin, "kit_editor_kit");
    }

    public void openList(Player p) {
        Inventory inv = Bukkit.createInventory(null, (int)54, (String)LIST_TITLE);
        this.fill(inv, this.pane(Material.BLACK_STAINED_GLASS_PANE));
        KitType[] kits = KitType.values();
        int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32};
        for (int i = 0; i < kits.length && i < slots.length; ++i) {
            inv.setItem(slots[i], this.kitIcon(kits[i]));
        }
        inv.setItem(53, this.btn(Material.BARRIER, "\u00a7c\u9589\u3058\u308b", ""));
        p.openInventory(inv);
    }

    public void openEdit(Player p, KitType kit) {
        int i;
        this.editing.put(p.getUniqueId(), kit);
        Inventory inv = Bukkit.createInventory(null, (int)54, (String)(EDIT_PREFIX + kit.getDisplayName()));
        List<ItemStack> items = this.hasCustomItems(kit) ? this.loadItems(kit) : KitBuilder.getDefaultItems(kit, TeamColor.RED);
        for (i = 0; i < Math.min(items.size(), 36); ++i) {
            inv.setItem(i, items.get(i));
        }
        for (i = 36; i < 54; ++i) {
            inv.setItem(i, this.pane(Material.GRAY_STAINED_GLASS_PANE));
        }
        List<ItemStack> armor = this.loadArmor(kit);
        inv.setItem(37, armor.size() > 0 && armor.get(0) != null ? armor.get(0) : this.pane(Material.IRON_HELMET));
        inv.setItem(38, armor.size() > 1 && armor.get(1) != null ? armor.get(1) : this.pane(Material.IRON_CHESTPLATE));
        inv.setItem(39, armor.size() > 2 && armor.get(2) != null ? armor.get(2) : this.pane(Material.IRON_LEGGINGS));
        inv.setItem(40, armor.size() > 3 && armor.get(3) != null ? armor.get(3) : this.pane(Material.IRON_BOOTS));
        inv.setItem(41, armor.size() > 4 && armor.get(4) != null ? armor.get(4) : this.pane(Material.AIR));
        this.setCustomName(inv.getItem(37), "\u00a77Helmet (\u30af\u30ea\u30c3\u30af\u3057\u3066\u5909\u66f4)");
        this.setCustomName(inv.getItem(38), "\u00a77Chestplate (\u30af\u30ea\u30c3\u30af\u3057\u3066\u5909\u66f4)");
        this.setCustomName(inv.getItem(39), "\u00a77Leggings (\u30af\u30ea\u30c3\u30af\u3057\u3066\u5909\u66f4)");
        this.setCustomName(inv.getItem(40), "\u00a77Boots (\u30af\u30ea\u30c3\u30af\u3057\u3066\u5909\u66f4)");
        inv.setItem(36, this.infoSign(kit));
        inv.setItem(45, this.btn(Material.LIME_WOOL, "\u00a7a\u00a7l\u4fdd\u5b58", "\u00a77\u73fe\u5728\u306e\u30a2\u30a4\u30c6\u30e0\u914d\u7f6e\u3092\u4fdd\u5b58\u3057\u307e\u3059"));
        inv.setItem(47, this.btn(Material.YELLOW_WOOL, "\u00a7e\u00a7l\u30c7\u30d5\u30a9\u30eb\u30c8\u306b\u623b\u3059", "\u00a77\u30ab\u30b9\u30bf\u30de\u30a4\u30ba\u3092\u524a\u9664\u3057\u3066\u30c7\u30d5\u30a9\u30eb\u30c8\u306b\u623b\u3057\u307e\u3059"));
        inv.setItem(49, this.btn(Material.CYAN_WOOL, "\u00a7b\u00a7l\u4e00\u89a7\u306b\u623b\u308b", "\u00a77\u4fdd\u5b58\u305b\u305a\u4e00\u89a7\u306b\u623b\u308a\u307e\u3059"));
        inv.setItem(53, this.btn(Material.RED_WOOL, "\u00a7c\u00a7l\u9589\u3058\u308b", "\u00a77GUI\u3092\u9589\u3058\u307e\u3059"));
        p.openInventory(inv);
    }

    public boolean handleClick(InventoryClickEvent e) {
        HumanEntity humanEntity = e.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return false;
        }
        Player p = (Player)humanEntity;
        String title = e.getView().getTitle();
        if (LIST_TITLE.equals(title)) {
            String kitName;
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return true;
            }
            if (clicked.getType() == Material.BARRIER) {
                p.closeInventory();
                return true;
            }
            if (clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                return true;
            }
            if (clicked.getItemMeta() != null && !(kitName = clicked.getItemMeta().getPersistentDataContainer().getOrDefault(this.kitKey, PersistentDataType.STRING, "")).isEmpty()) {
                try {
                    this.openEdit(p, KitType.valueOf(kitName));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
                return true;
            }
            return true;
        }
        if (title.startsWith(EDIT_PREFIX)) {
            int slot = e.getRawSlot();
            if (slot >= 36 && slot < 54) {
                e.setCancelled(true);
                this.handleControlClick(p, slot, e.getView().getTitle());
                return true;
            }
            return false;
        }
        return false;
    }

    private void handleControlClick(Player p, int slot, String title) {
        KitType kit = this.editing.get(p.getUniqueId());
        if (kit == null) {
            return;
        }
        switch (slot) {
            case 45: {
                Inventory inv = p.getOpenInventory().getTopInventory();
                this.saveItems(kit, inv);
                p.sendMessage("\u00a7a\u00a7lKit Editor: \u00a7e" + kit.getDisplayName() + " \u00a7a\u3092\u4fdd\u5b58\u3057\u307e\u3057\u305f\u3002");
                p.closeInventory();
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.openEdit(p, kit), 1L);
                break;
            }
            case 47: {
                this.deleteItems(kit);
                p.sendMessage("\u00a7e\u00a7lKit Editor: \u00a7e" + kit.getDisplayName() + " \u00a7e\u3092\u30c7\u30d5\u30a9\u30eb\u30c8\u306b\u623b\u3057\u307e\u3057\u305f\u3002");
                p.closeInventory();
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.openEdit(p, kit), 1L);
                break;
            }
            case 49: {
                this.editing.remove(p.getUniqueId());
                this.openList(p);
                break;
            }
            case 53: {
                this.editing.remove(p.getUniqueId());
                p.closeInventory();
            }
        }
    }

    public void saveItems(KitType kit, Inventory inv) {
        FileConfiguration cfg = this.plugin.getConfig();
        String base = "kit_editor." + kit.name() + ".items.";
        cfg.set("kit_editor." + kit.name(), null);
        for (int i = 0; i < 36; ++i) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            cfg.set(base + i, (Object)item);
        }
        String armorBase = "kit_editor." + kit.name() + ".armor.";
        cfg.set("kit_editor." + kit.name() + ".armor", null);
        int[] armorSlots = new int[]{37, 38, 39, 40, 41};
        for (int i = 0; i < armorSlots.length; ++i) {
            ItemStack item = inv.getItem(armorSlots[i]);
            if (item == null || item.getType() == Material.AIR || this.isControlItem(item)) continue;
            cfg.set(armorBase + i, (Object)item);
        }
        this.plugin.saveConfig();
    }

    private boolean isControlItem(ItemStack item) {
        return item.getType().name().contains("STAINED_GLASS_PANE") || item.getType().name().endsWith("_WOOL");
    }

    public void deleteItems(KitType kit) {
        this.plugin.getConfig().set("kit_editor." + kit.name(), null);
        this.plugin.saveConfig();
    }

    public List<ItemStack> loadItems(KitType kit) {
        String base;
        FileConfiguration cfg = this.plugin.getConfig();
        if (!cfg.isConfigurationSection(base = "kit_editor." + kit.name() + ".items")) {
            return new ArrayList<ItemStack>();
        }
        ArrayList<ItemStack> items = new ArrayList<ItemStack>(Collections.nCopies(36, (ItemStack)null));
        for (String key : cfg.getConfigurationSection(base).getKeys(false)) {
            try {
                int slot = Integer.parseInt(key);
                ItemStack item = cfg.getItemStack(base + "." + key);
                if (slot < 0 || slot >= 36) continue;
                items.set(slot, item);
            }
            catch (NumberFormatException numberFormatException) {}
        }
        return items;
    }

    public List<ItemStack> loadArmor(KitType kit) {
        FileConfiguration cfg = this.plugin.getConfig();
        String base = "kit_editor." + kit.name() + ".armor";
        ArrayList<ItemStack> armor = new ArrayList<ItemStack>(Collections.nCopies(5, (ItemStack)null));
        if (!cfg.isConfigurationSection(base)) {
            return armor;
        }
        for (String key : cfg.getConfigurationSection(base).getKeys(false)) {
            try {
                int idx = Integer.parseInt(key);
                ItemStack item = cfg.getItemStack(base + "." + key);
                if (idx < 0 || idx >= 5) continue;
                armor.set(idx, item);
            }
            catch (NumberFormatException numberFormatException) {}
        }
        return armor;
    }

    public boolean hasCustomItems(KitType kit) {
        return this.plugin.getConfig().isConfigurationSection("kit_editor." + kit.name() + ".items");
    }

    private void fill(Inventory inv, ItemStack item) {
        for (int i = 0; i < inv.getSize(); ++i) {
            inv.setItem(i, item);
        }
    }

    private void setCustomName(ItemStack item, String name) {
        if (item == null) {
            return;
        }
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.setDisplayName(name);
            item.setItemMeta(m);
        }
    }

    private ItemStack pane(Material mat) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName(" ");
            i.setItemMeta(m);
        }
        return i;
    }

    private ItemStack btn(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName(name);
            if (!lore.isEmpty()) {
                m.setLore(List.of(lore));
            }
            i.setItemMeta(m);
        }
        return i;
    }

    private ItemStack kitIcon(KitType kit) {
        ItemStack i = new ItemStack(KitEditorGUI.kitMaterial(kit));
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName("\u00a7e\u00a7l" + kit.getDisplayName());
            m.setLore(List.of("\u00a77" + kit.getDescription(), "", this.hasCustomItems(kit) ? "\u00a7a\u2714 \u30ab\u30b9\u30bf\u30e0\u8a2d\u5b9a\u3042\u308a" : "\u00a77\u30c7\u30d5\u30a9\u30eb\u30c8\u8a2d\u5b9a", "\u00a7e\u30af\u30ea\u30c3\u30af\u3057\u3066\u7de8\u96c6"));
            m.getPersistentDataContainer().set(this.kitKey, PersistentDataType.STRING, kit.name());
            i.setItemMeta(m);
        }
        return i;
    }

    private ItemStack infoSign(KitType kit) {
        ItemStack i = new ItemStack(Material.OAK_SIGN);
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName("\u00a76\u00a7l" + kit.getDisplayName());
            m.setLore(List.of("\u00a77" + kit.getDescription(), "", "\u00a77\u30b9\u30ed\u30c3\u30c8 0-35 \u306b", "\u00a77\u30a2\u30a4\u30c6\u30e0\u3092\u81ea\u7531\u306b\u914d\u7f6e\u3057\u3001", "\u00a7a\u4fdd\u5b58\u30dc\u30bf\u30f3\u00a77\u3067\u78ba\u5b9a\u3002", "", this.hasCustomItems(kit) ? "\u00a7a\u2714 \u30ab\u30b9\u30bf\u30e0\u8a2d\u5b9a\u6e08\u307f" : "\u00a77\u672a\u30ab\u30b9\u30bf\u30de\u30a4\u30ba\uff08\u30c7\u30d5\u30a9\u30eb\u30c8\uff09"));
            i.setItemMeta(m);
        }
        return i;
    }

    public static Material iconMaterial(KitType kit) {
        return KitEditorGUI.kitMaterial(kit);
    }

    private static Material kitMaterial(KitType kit) {
        return switch (kit) {
            case BLADE -> Material.IRON_SWORD;
            case BREAKER -> Material.IRON_AXE;
            case SCOUT -> Material.FEATHER;
            case FLASHER -> Material.GLOWSTONE_DUST;
            case ROCKETER -> Material.TNT;
            case ALCHEMIST -> Material.GLASS_BOTTLE;
            case TRAPPER -> Material.STRING;
            case GUARDIAN -> Material.SHIELD;
            case NINJA -> Material.ENDER_PEARL;
            case BERSERKER -> Material.DIAMOND_SWORD;
            case MEDIC -> Material.SPLASH_POTION;
            case ENGINEER -> Material.IRON_PICKAXE;
            case SNIPER -> Material.CROSSBOW;
            case COUNTER -> Material.IRON_SWORD;
            case MARKSMAN -> Material.BOW;
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
            case BULWARK -> Material.IRON_BLOCK;
            case TIMEKEEPER -> Material.CLOCK;
            case AEGIS -> Material.SHIELD;
            case HEXER -> Material.ENCHANTING_TABLE;
            case REFLECTOR -> Material.LIGHT_BLUE_STAINED_GLASS;
            case GLACIES -> Material.PACKED_ICE;
            default -> throw new IncompatibleClassChangeError();
        };
    }
}

