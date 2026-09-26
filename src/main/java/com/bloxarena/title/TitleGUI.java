package com.bloxarena.title;

import com.bloxarena.BloxArenaPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TitleGUI implements Listener {

    private final BloxArenaPlugin plugin;
    private final BATitleManager tm;

    public TitleGUI(BloxArenaPlugin plugin, BATitleManager tm) {
        this.plugin = plugin;
        this.tm = tm;
        Bukkit.getPluginManager().registerEvents((Listener)this, (org.bukkit.plugin.Plugin)plugin);
    }

    static final class Holder implements InventoryHolder {
        final int page;

        Holder(int p) {
            this.page = p;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public void open(Player p, int page) {
        List<BATitle> list = new ArrayList<BATitle>();
        for (BATitle t : this.tm.getTitles().values()) {
            if (!t.isManualOnly()) {
                list.add(t);
            }
        }
        Inventory inv = Bukkit.createInventory(new Holder(page), 54, "\u00a78\u79f0\u53f7\u4e00\u89a7 " + (page + 1));
        for (int i = 0; i < 45 && page * 45 + i < list.size(); ++i) {
            inv.setItem(i, this.icon(list.get(page * 45 + i), p));
        }
        inv.setItem(45, this.named(Material.BARRIER, "\u00a7c\u79f0\u53f7\u3092\u5916\u3059", List.of("\u00a77\u73fe\u5728\u306e\u79f0\u53f7\u3092\u5916\u3057\u307e\u3059")));
        if (page > 0) {
            inv.setItem(48, this.named(Material.ARROW, "\u00a77\u524d\u306e\u30da\u30fc\u30b8"));
        }
        long got = list.stream().filter(t -> this.tm.data(p).unlocked.contains(t.id)).count();
        int total = list.size();
        int pct = total == 0 ? 100 : (int)Math.round((double)got * 100.0 / (double)total);
        inv.setItem(49, this.named(Material.BOOK, "\u00a76\u89e3\u653e\u72b6\u6cc1 " + pct + "% \u00a78(" + got + "/" + total + ")", List.of("\u00a77\u30af\u30ea\u30c3\u30af\u3067\u9589\u3058\u308b")));
        if ((page + 1) * 45 < list.size()) {
            inv.setItem(50, this.named(Material.ARROW, "\u00a77\u6b21\u306e\u30da\u30fc\u30b8"));
        }
        p.openInventory(inv);
    }

    private ItemStack icon(BATitle t, Player viewer) {
        boolean unlocked = this.tm.data(viewer).unlocked.contains(t.id);
        boolean selected = t.id.equals(this.tm.data(viewer).selected);
        Material mat;
        String name;
        List<String> lore = new ArrayList<String>();
        if (!unlocked && t.hidden) {
            mat = Material.BEDROCK;
            name = "\u00a78???";
            lore.add("\u00a77\u96a0\u3057\u5b9f\u7e3e");
        } else if (selected) {
            mat = Material.GOLD_INGOT;
            name = "\u00a7e\u00a7l\u25b6 " + t.display;
            lore.add("\u00a76\u88c5\u7740\u4e2d");
            this.addConditionLore(t, viewer, lore, "\u00a78\u30fb\u00a7a\u2714 ");
        } else if (unlocked) {
            mat = Material.NAME_TAG;
            name = t.display;
            lore.add("\u00a7a\u2714 \u89e3\u653e\u6e08\u307f \u00a77- \u00a7e\u30af\u30ea\u30c3\u30af\u3067\u88c5\u7740");
            this.addConditionLore(t, viewer, lore, "\u00a78\u30fb\u00a7a\u2714 ");
        } else {
            mat = Material.GRAY_DYE;
            name = "\u00a78???";
            if (!t.description.isEmpty()) {
                lore.add("\u00a77" + t.description);
            }
            this.addConditionLore(t, viewer, lore, "\u00a78\u30fb");
        }
        return this.glowIfSelected(this.named(mat, name, lore), selected);
    }

    private void addConditionLore(BATitle t, Player p, List<String> lore, String prefix) {
        for (int i = 0; i < t.conditions.size(); ++i) {
            BATitle.Cond c = t.conditions.get(i);
            int cur = this.tm.progressOf(p, t, i);
            lore.add(prefix + c.describe() + " \u00a77(" + cur + "/" + c.count + ")");
        }
    }

    private ItemStack glowIfSelected(ItemStack it, boolean glow) {
        if (glow) {
            it.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            ItemMeta m = it.getItemMeta();
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            it.setItemMeta(m);
        }
        return it;
    }

    private ItemStack named(Material mat, String name) {
        return this.named(mat, name, List.of());
    }

    private ItemStack named(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        m.setLore(new ArrayList<String>(lore));
        it.setItemMeta(m);
        return it;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof Holder h)) {
            return;
        }
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) {
            return;
        }
        if (e.getRawSlot() < 0) {
            return;
        }
        if (e.getRawSlot() == 45) {
            this.tm.data(p).selected = null;
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
            this.tm.refreshTabName(p);
            this.tm.save(p);
            this.open(p, h.page);
        } else if (e.getRawSlot() == 48 && h.page > 0) {
            this.open(p, h.page - 1);
        } else if (e.getRawSlot() == 49) {
            p.closeInventory();
        } else if (e.getRawSlot() == 50) {
            this.open(p, h.page + 1);
        } else if (e.getRawSlot() < 45) {
            List<BATitle> all = new ArrayList<BATitle>();
            for (BATitle t : this.tm.getTitles().values()) {
                if (!t.isManualOnly()) {
                    all.add(t);
                }
            }
            int idx = h.page * 45 + e.getRawSlot();
            if (idx >= all.size()) {
                return;
            }
            BATitle t = all.get(idx);
            if (!this.tm.data(p).unlocked.contains(t.id)) {
                return;
            }
            this.tm.data(p).selected = t.id;
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
            this.tm.refreshTabName(p);
            this.tm.save(p);
            this.open(p, h.page);
        }
    }
}
