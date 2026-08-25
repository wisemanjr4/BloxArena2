/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
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
import com.bloxarena.kit.KitEditorGUI;
import com.bloxarena.kit.KitType;
import com.bloxarena.song.NbsPlayer;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class KitInfoGUI {
    public static final String LIST_TITLE = "\u00a76\u00a7l\u30ad\u30c3\u30c8\u4e00\u89a7";
    public static final String DETAIL_PREFIX = "\u00a76\u00a7l";
    public static final String TUTORIAL_TITLE = "\u00a7a\u00a7l\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb";
    private final BloxArenaPlugin plugin;
    private final NamespacedKey kitKey;
    private final NamespacedKey tutorialKey;
    private final NamespacedKey tutorialPrevKey;
    private final NamespacedKey tutorialNextKey;
    private final NamespacedKey tutorialStartKey;
    private final NamespacedKey bgmSelectKey;
    private final NamespacedKey bgmSongKey;

    public KitInfoGUI(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.kitKey = new NamespacedKey((Plugin)plugin, "kit_info_kit");
        this.tutorialKey = new NamespacedKey((Plugin)plugin, "kit_tutorial");
        this.tutorialPrevKey = new NamespacedKey((Plugin)plugin, "tutorial_prev");
        this.tutorialNextKey = new NamespacedKey((Plugin)plugin, "tutorial_next");
        this.tutorialStartKey = new NamespacedKey((Plugin)plugin, "tutorial_start");
        this.bgmSelectKey = new NamespacedKey((Plugin)plugin, "bgm_select");
        this.bgmSongKey = new NamespacedKey((Plugin)plugin, "bgm_song");
    }

    public ItemStack makeGuideItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a76\u00a7l\u30ad\u30c3\u30c8\u4e00\u89a7 \u00a77(\u53f3\u30af\u30ea\u30c3\u30af)");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add("\u00a77\u53f3\u30af\u30ea\u30c3\u30af\u2192\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\uff06\u30ad\u30c3\u30c8\u4e00\u89a7");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "kit_guide"), PersistentDataType.BYTE, (byte)1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isGuideItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey((Plugin)this.plugin, "kit_guide"), PersistentDataType.BYTE);
    }

    public void openList(Player p) {
        Inventory inv = Bukkit.createInventory(null, (int)54, (String)LIST_TITLE);
        KitType[] kits = KitType.values();
        for (int i = 0; i < kits.length && i < 45; ++i) {
            if (kits[i] == KitType.SUPERIOR_MISTRAL && !p.getName().equals("Photon_wisemanjr")) continue;
            inv.setItem(i, this.makeKitIcon(kits[i]));
        }
        ItemStack bgmBtn = new ItemStack(Material.JUKEBOX);
        ItemMeta bgmMeta = bgmBtn.getItemMeta();
        if (bgmMeta != null) {
            bgmMeta.setDisplayName("\u00a7d\u00a7lBGM\u9078\u629e \u00a77(\u30af\u30ea\u30c3\u30af)");
            ArrayList<String> bgmLore = new ArrayList<String>();
            bgmLore.add("\u00a77\u8a66\u5408\u4e2d\u306eBGM\u3092\u9078\u3076");
            NbsPlayer curBgm = this.plugin.getGameManager().getSelectedBgm();
            bgmLore.add(curBgm != null ? "\u00a7a\u9078\u629e\u4e2d: \u00a7f" + curBgm.getName() : "\u00a77\u9078\u629e\u4e2d: \u306a\u3057");
            bgmMeta.setLore(bgmLore);
            bgmMeta.getPersistentDataContainer().set(this.bgmSelectKey, PersistentDataType.BYTE, (byte)1);
            bgmBtn.setItemMeta(bgmMeta);
        }
        inv.setItem(48, bgmBtn);
        ItemStack tutorial = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta tm = tutorial.getItemMeta();
        if (tm != null) {
            tm.setDisplayName("\u00a7a\u00a7l\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb \u00a77(\u30af\u30ea\u30c3\u30af)");
            ArrayList<String> tlore = new ArrayList<String>();
            tlore.add("\u00a77\u30b2\u30fc\u30e0\u306e\u904a\u3073\u65b9\u3092\u78ba\u8a8d");
            tm.setLore(tlore);
            tm.getPersistentDataContainer().set(this.tutorialKey, PersistentDataType.BYTE, (byte)1);
            tutorial.setItemMeta(tm);
        }
        inv.setItem(49, tutorial);
        ItemStack tutorialStart = new ItemStack(Material.NETHER_STAR);
        ItemMeta tsm = tutorialStart.getItemMeta();
        if (tsm != null) {
            tsm.setDisplayName("\u00a76\u00a7l\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u958b\u59cb \u00a77(\u30af\u30ea\u30c3\u30af)");
            ArrayList<String> tsl = new ArrayList<String>();
            tsl.add("\u00a77\u30c6\u30b9\u30c8\u5834\u3067\u64cd\u4f5c\u3092\u5b66\u3076");
            tsm.setLore(tsl);
            tsm.getPersistentDataContainer().set(this.tutorialStartKey, PersistentDataType.BYTE, (byte)1);
            tutorialStart.setItemMeta(tsm);
        }
        inv.setItem(47, tutorialStart);
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        if (cm != null) {
            cm.setDisplayName("\u00a7c\u2716 \u9589\u3058\u308b");
            close.setItemMeta(cm);
        }
        inv.setItem(53, close);
        p.openInventory(inv);
    }

    public void openDetail(Player p, KitType kit) {
        this.openKitDetail(p, kit);
    }

    public void openKitDetail(Player p, KitType kit) {
        ItemStack back;
        ItemMeta bm;
        Inventory inv = Bukkit.createInventory(null, (int)54, (String)(DETAIL_PREFIX + kit.getDisplayName()));
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        if (paneMeta != null) {
            paneMeta.setDisplayName(" ");
            pane.setItemMeta(paneMeta);
        }
        for (int i = 0; i < 54; ++i) {
            if (i == 13 || i == 22 || i >= 29 && i <= 33 || i == 49 || i == 53) continue;
            inv.setItem(i, pane.clone());
        }
        ItemStack icon = new ItemStack(KitEditorGUI.iconMaterial(kit));
        ItemMeta iconMeta = icon.getItemMeta();
        if (iconMeta != null) {
            iconMeta.setDisplayName(kit.getDisplayName());
            List<String> iconLore = new ArrayList<String>();
            iconLore.add("\u00a77\u5f79\u5272: " + kit.getRole().getName());
            iconLore.add("\u00a77" + kit.getDescription());
            iconMeta.setLore(iconLore);
            icon.setItemMeta(iconMeta);
        }
        inv.setItem(13, icon);
        ItemStack skillInfo = new ItemStack(Material.BOOK);
        ItemMeta skillMeta = skillInfo.getItemMeta();
        if (skillMeta != null) {
            skillMeta.setDisplayName("\u00a76\u00a7l\u30b9\u30ad\u30eb\u8a73\u7d30");
            List<String> skillLore = this.buildSkillLore(kit);
            skillMeta.setLore(skillLore);
            skillInfo.setItemMeta(skillMeta);
        }
        inv.setItem(22, skillInfo);
        List<ItemStack> items = KitBuilder.getDefaultItems(kit, TeamColor.RED);
        int slot = 29;
        for (ItemStack item : items) {
            if (slot > 33) break;
            if (item == null || item.getType() == Material.AIR) continue;
            inv.setItem(slot++, item.clone());
        }
        if ((bm = (back = new ItemStack(Material.ARROW)).getItemMeta()) != null) {
            bm.setDisplayName("\u00a77\u00a7l\u2190 \u30ad\u30c3\u30c8\u4e00\u89a7\u306b\u623b\u308b");
            back.setItemMeta(bm);
        }
        inv.setItem(49, back);
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        if (cm != null) {
            cm.setDisplayName("\u00a7c\u2716 \u9589\u3058\u308b");
            close.setItemMeta(cm);
        }
        inv.setItem(53, close);
        p.openInventory(inv);
    }

    private List<String> buildSkillLore(KitType kit) {
        ArrayList<String> lore = new ArrayList<String>();
        switch (kit) {
            case COOK: {
                lore.add("\u00a76\u00a7l\u6599\u7406\u4eba\u30b9\u30ad\u30eb - \u98df\u6750\u4e00\u89a7");
                lore.add("");
                lore.add("\u00a7a\u00a7l\u3010\u30d0\u30d5\u98df\u6750\u3011");
                lore.add("\u00a76\u30b9\u30c6\u30fc\u30ad \u00a77\u2192 \u653b\u6483\u529b\u4e0a\u6607III");
                lore.add("\u00a7f\u9d8f\u8089 \u00a77\u2192 \u901f\u5ea6\u4e0a\u6607III");
                lore.add("\u00a7e\u91d1\u30cb\u30f3\u30b8\u30f3 \u00a77\u2192 \u518d\u751fIII");
                lore.add("\u00a7d\u8c5a\u8089 \u00a77\u2192 \u8010\u6027III");
                lore.add("\u00a76\u30d1\u30f3\u30d7\u30ad\u30f3\u30d1\u30a4 \u00a77\u2192 \u885d\u6483\u5438\u53ceIII");
                lore.add("\u00a7e\u30d1\u30f3 \u00a77\u2192 \u6e80\u8179\u56de\u5fa9");
                lore.add("\u00a76\u30cf\u30c1\u30df\u30c4 \u00a77\u2192 \u8df3\u8e8d\u529bIV");
                lore.add("\u00a7c\u30d3\u30fc\u30c8\u30eb\u30fc\u30c8\u30b9\u30fc\u30d7 \u00a77\u2192 \u8010\u706b20s");
                lore.add("");
                lore.add("\u00a7c\u00a7l\u3010\u30c7\u30d0\u30d5\u98df\u6750\u3011");
                lore.add("\u00a78\u8150\u8089 \u00a77\u2192 \u7a7a\u8179IV");
                lore.add("\u00a75\u8718\u86db\u306e\u76ee \u00a77\u2192 \u6bd2III");
                lore.add("\u00a7a\u6bd2\u30b8\u30e3\u30ac\u30a4\u30e2 \u00a77\u2192 \u5410\u6c17");
                lore.add("\u00a7e\u30d5\u30b0 \u00a77\u2192 \u8870\u5f31+\u5410\u6c17");
                lore.add("\u00a7f\u751f\u9d8f\u8089 \u00a77\u2192 \u5f31\u4f53\u5316III");
                lore.add("\u00a7d\u751f\u8c5a\u8089 \u00a77\u2192 \u920d\u8db3III");
                lore.add("\u00a7c\u751f\u725b\u8089 \u00a77\u2192 \u63a1\u6398\u4f4e\u4e0bIII");
                lore.add("\u00a75\u751f\u7f8a\u8089 \u00a77\u2192 \u76f2\u76ee");
                lore.add("\u00a7b\u751f\u9c48 \u00a77\u2192 \u5373\u6642\u30c0\u30e14\u2764");
                lore.add("\u00a7d\u751f\u9bad \u00a77\u2192 \u6d6e\u904a");
                lore.add("");
                lore.add("\u00a77\u5263\u53f3\u30af\u30ea\u3067\u98df\u6750\u53d6\u5f97 | \u661f\u53f3\u30af\u30ea\u3067\u4e00\u6c17\u4f7f\u7528/\u6295\u64f2");
                break;
            }
            case KREUTZ: {
                lore.add("\u00a75\u00a7l\u9b54\u6cd5\u30ab\u30fc\u30c9\u4e00\u89a7 (17\u7a2e)");
                lore.add("");
                lore.add("\u00a7c\u30d5\u30a1\u30a4\u30a2\u30dc\u30fc\u30eb \u00a77\u2192 \u8ffd\u5c3e\u706b\u7403(\u7206\u767a+\u708e\u4e0a)");
                lore.add("\u00a7b\u30a2\u30a4\u30b9\u30e9\u30f3\u30b9 \u00a77\u2192 \u5bfe\u8c61\u306b7\u30c0\u30e1+\u920d\u8db3IV");
                lore.add("\u00a7e\u30b5\u30f3\u30c0\u30fc \u00a77\u2192 \u5bfe\u8c61\u306b6\u30c0\u30e1+\u96f7+\u63a1\u6398\u4f4e\u4e0b");
                lore.add("\u00a7f\u30b7\u30fc\u30eb\u30c9 \u00a77\u2192 \u81ea\u8eab\u306b\u885d\u6483\u5438\u53ceV");
                lore.add("\u00a7d\u30d2\u30fc\u30eb \u00a77\u2192 \u81ea\u8eab\u309210HP\u56de\u5fa9");
                lore.add("\u00a78\u30ab\u30fc\u30b9 \u00a77\u2192 \u5bfe\u8c61\u306b\u63a1\u6398\u4f4e\u4e0bIII");
                lore.add("\u00a75\u30b0\u30e9\u30d3\u30c6\u30a3 \u00a77\u2192 \u5468\u56f2\u6575\u3092\u5f15\u304d\u5bc4\u305b+\u920d\u8db3");
                lore.add("\u00a73\u30c1\u30a7\u30a4\u30f3 \u00a77\u2192 \u5bfe\u8c61\u306b5\u30c0\u30e1+\u920d\u8db3+\u8df3\u8e8d\u4f4e\u4e0b");
                lore.add("\u00a72\u30dd\u30a4\u30ba\u30f3\u30af\u30e9\u30a6\u30c9 \u00a77\u2192 \u5468\u56f2\u6575\u306b\u6bd2III");
                lore.add("\u00a7b\u30b9\u30d4\u30fc\u30c9\u30d6\u30fc\u30b9\u30c8 \u00a77\u2192 \u81ea\u8eab\u306b\u901f\u5ea6III");
                lore.add("\u00a7a\u30ea\u30fc\u30d7 \u00a77\u2192 \u524d\u65b9\u306b\u5927\u30b8\u30e3\u30f3\u30d7");
                lore.add("\u00a77\u30a6\u30a3\u30fc\u30af\u30cd\u30b9 \u00a77\u2192 \u5bfe\u8c61\u306b\u5f31\u4f53\u5316II");
                lore.add("\u00a7d\u30de\u30a4\u30f3\u30c9 \u00a77\u2192 \u5bfe\u8c61\u306b\u76f2\u76ee+\u5410\u6c17");
                lore.add("\u00a7e\u30c1\u30a7\u30a4\u30f3\u30e9\u30a4\u30c8\u30cb\u30f3\u30b0 \u00a77\u2192 \u5468\u56f2\u6575\u306b7\u30c0\u30e1+\u96f7");
                lore.add("\u00a75\u30c6\u30ec\u30dd\u30fc\u30c8\u30c8\u30e9\u30c3\u30d7 \u00a77\u2192 \u89e6\u308c\u305f\u6575\u304c\u81ea\u5206\u306bTP");
                lore.add("\u00a7c\u30d5\u30a1\u30f3\u30b0 \u00a77\u2192 \u524d\u65b9\u306b\u7259\u306e\u9023\u7d9a\u653b\u6483");
                lore.add("\u00a76\u30d4\u30a2\u30c3\u30b7\u30f3\u30b0 \u00a77\u2192 \u5bfe\u8c61\u309210\u79d2\u8cab\u901a\u30de\u30fc\u30af");
                lore.add("");
                lore.add("\u00a77\u3057\u3083\u304c\u307f\u53f3\u30af\u30ea\u2192\u30ab\u30fc\u30c9\u30c9\u30ed\u30fc | \u53f3\u30af\u30ea\u2192\u5531\u3048\u308b");
                break;
            }
            case VAMPIRE: {
                lore.add("\u00a74\u00a7l\u5438\u8840\u30b9\u30c6\u30fc\u30b8 (4\u6bb5\u968e)");
                lore.add("");
                lore.add("\u00a77S0: HP14 \u00a77\u5f31\u4f53\u5316+\u920d\u8db3II(\u521d\u671f)");
                lore.add("\u00a77S1: HP16 \u00a77\u920d\u8db3I(\u30b2\u30fc\u30b810~)");
                lore.add("\u00a77S2: HP20 \u00a77\u30c7\u30d0\u30d5\u89e3\u9664(\u30b2\u30fc\u30b825~)");
                lore.add("\u00a74S3: HP26 \u00a74\u30d6\u30e9\u30c3\u30c9\u6642 \u653b\u6483\u529bI(\u30b2\u30fc\u30b840~)");
                lore.add("\u00a74S4: HP40 \u00a74\u30d6\u30e9\u30c3\u30c9\u6642 \u653b\u6483\u529bIII+\u518d\u751fIII+\u901f\u5ea6II(\u30b2\u30fc\u30b855~)");
                lore.add("");
                lore.add("\u00a77\u30c9\u30ec\u30a4\u30f3\u30e2\u30fc\u30c9: \u30c0\u30e1\u3067\u30b2\u30fc\u30b8\u6e9c\u3081/\u88ab\u30c0\u30e1\u3067\u6e1b\u5c11");
                lore.add("\u00a77\u30d6\u30e9\u30c3\u30c9\u30e2\u30fc\u30c9: \u30b2\u30fc\u30b8\u6d88\u8cbb\u3067\u5f37\u5316\u72b6\u614b");
                lore.add("\u00a77\u661f\u53f3\u30af\u30ea\u2192\u30e2\u30fc\u30c9\u5207\u66ff | \u5263\u53f3\u30af\u30ea\u2192\u885d\u6483\u6ce2");
                break;
            }
            case ROCKETER: {
                lore.add("\u00a7e\u00a7l\u30e1\u30ac\u30ed\u30b1\u30c3\u30c8");
                lore.add("");
                lore.add("\u00a77\u30af\u30ed\u30b9\u30dc\u30a6\u3092\u69cb\u3048\u3066\u3057\u3083\u304c\u307f\u53f3\u30af\u30ea");
                lore.add("\u00a77\u5927\u7206\u767a\u30ed\u30b1\u30c3\u30c8\u5f3e\u3092\u767a\u5c04");
                lore.add("\u00a77\u7740\u5f3e\u5730\u70b9\u3067\u5e83\u7bc4\u56f2\u7206\u767a");
                lore.add("\u00a77\u30a8\u30ea\u30a2\u5236\u5727\u306b\u6700\u9069\u306a\u9060\u8ddd\u96e2\u7832\u6483\u578b");
                break;
            }
            case ALCHEMIST: {
                lore.add("\u00a7d\u00a7l\u518d\u8abf\u5408");
                lore.add("");
                lore.add("\u00a77\u661f\u53f3\u30af\u30ea\u21924\u7a2e\u30dd\u30fc\u30b7\u30e7\u30f3\u88dc\u5145");
                lore.add("\u00a77\u920d\u5316+\u5373\u6642\u30c0\u30e1+\u6bd2+\u5f31\u4f53\u5316\u306e\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5");
                lore.add("\u00a77\u30dd\u30fc\u30b7\u30e7\u30f3\u3092\u5e83\u7bc4\u56f2\u306b\u6492\u3044\u3066\u5236\u5727");
                lore.add("\u00a77\u7a7a\u9593\u3054\u3068\u652f\u914d\u3059\u308b\u5316\u5b66\u6226\u95d8\u578b");
                break;
            }
            case SUPPORTER: {
                lore.add("\u00a7a\u00a7l\u518d\u8abf\u9054");
                lore.add("");
                lore.add("\u00a77\u661f\u53f3\u30af\u30ea\u2192\u30d0\u30d5\u30dd\u30fc\u30b7\u30e7\u30f3\u88dc\u5145");
                lore.add("\u00a77\u529b+\u518d\u751f+\u5373\u6642\u56de\u5fa9/\u901f\u5ea6+\u885d\u6483\u5438\u53ce\u306e2\u30d1\u30bf\u30fc\u30f3");
                lore.add("\u00a77\u5473\u65b9\u306b\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5\u30dd\u30fc\u30b7\u30e7\u30f3\u3092\u914d\u5e03");
                lore.add("\u00a77\u6226\u5834\u3092\u652f\u3048\u308b\u5f8c\u65b9\u652f\u63f4\u578b");
                break;
            }
            default: {
                lore.add("\u00a76\u00a7l\u30b9\u30ad\u30eb");
                lore.add("\u00a77" + kit.getDescription());
            }
        }
        return lore;
    }

    public void handleClick(InventoryClickEvent e) {
        block29: {
            e.setCancelled(true);
            HumanEntity humanEntity = e.getWhoClicked();
            if (!(humanEntity instanceof Player)) {
                return;
            }
            Player p = (Player)humanEntity;
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }
            String title = e.getView().getTitle();
            if (title.equals(LIST_TITLE)) {
                if (clicked.getType() == Material.BARRIER) {
                    p.closeInventory();
                    return;
                }
                if (clicked.getItemMeta() == null) {
                    return;
                }
                if (clicked.getItemMeta().getPersistentDataContainer().has(this.bgmSelectKey, PersistentDataType.BYTE)) {
                    this.openBgmList(p);
                    return;
                }
                if (clicked.getItemMeta().getPersistentDataContainer().has(this.tutorialKey, PersistentDataType.BYTE)) {
                    this.openTutorial(p, 0);
                    return;
                }
                if (clicked.getItemMeta().getPersistentDataContainer().has(this.tutorialStartKey, PersistentDataType.BYTE)) {
                    p.closeInventory();
                    this.plugin.getTutorialManager().start(p);
                    return;
                }
                String kitName = (String)clicked.getItemMeta().getPersistentDataContainer().getOrDefault(this.kitKey, PersistentDataType.STRING, "");
                if (!kitName.isEmpty()) {
                    try {
                        KitType kit = KitType.valueOf(kitName);
                        if (e.isShiftClick()) {
                            this.plugin.getTestFieldManager().giveTestKit(p, kit);
                            break block29;
                        }
                        this.openDetail(p, kit);
                    }
                    catch (IllegalArgumentException kit) {}
                }
            } else if (title.startsWith(DETAIL_PREFIX)) {
                if (e.getSlot() == 49) {
                    this.openList(p);
                } else if (clicked.getType() == Material.BARRIER) {
                    p.closeInventory();
                }
            } else if (title.equals(TUTORIAL_TITLE)) {
                if (clicked.getItemMeta() == null) {
                    return;
                }
                ItemMeta meta = clicked.getItemMeta();
                if (clicked.getType() == Material.BARRIER) {
                    this.openList(p);
                    return;
                }
                Integer prevPage = (Integer)meta.getPersistentDataContainer().get(this.tutorialPrevKey, PersistentDataType.INTEGER);
                if (prevPage != null) {
                    this.openTutorial(p, prevPage - 1);
                    return;
                }
                Integer nextPage = (Integer)meta.getPersistentDataContainer().get(this.tutorialNextKey, PersistentDataType.INTEGER);
                if (nextPage != null) {
                    this.openTutorial(p, nextPage + 1);
                    return;
                }
            } else if (title.equals("\u00a7d\u00a7lBGM\u9078\u629e")) {
                if (clicked.getItemMeta() == null) {
                    return;
                }
                if (clicked.getType() == Material.ARROW) {
                    this.openList(p);
                    return;
                }
                if (clicked.getType() == Material.BARRIER) {
                    this.plugin.getGameManager().setSelectedBgm(null);
                    p.sendMessage("\u00a7aBGM\u3092\u7121\u52b9\u306b\u3057\u307e\u3057\u305f\u3002");
                    p.closeInventory();
                    return;
                }
                String songName = (String)clicked.getItemMeta().getPersistentDataContainer().getOrDefault(this.bgmSongKey, PersistentDataType.STRING, "");
                if (!songName.isEmpty()) {
                    this.plugin.getGameManager().setSelectedBgmByName(songName);
                    p.sendMessage("\u00a7aBGM\u3092 \u00a7e" + songName + " \u00a7a\u306b\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                    p.closeInventory();
                }
            }
        }
    }

    public void openTutorial(Player p, int page) {
        ItemStack close;
        ItemMeta cm;
        Inventory inv = Bukkit.createInventory(null, (int)54, (String)TUTORIAL_TITLE);
        String[] pages = new String[]{"\u00a76\u00a7l=== \u3088\u3046\u3053\u305d ===\n\u00a77BloxArena II WoNG \u3078\u3088\u3046\u3053\u305d\uff01\n\u00a775v5\u306e\u30c1\u30fc\u30e0\u6226\u95d8\u30b2\u30fc\u30e0\u3067\u3059\n\u00a7734\u7a2e\u985e\u306e\u30ad\u30c3\u30c8\u304b\u3089\u9078\u629e\u3057\n\u00a77\u30c1\u30fc\u30e0\u3067\u5354\u529b\u3057\u3066\u52dd\u5229\u3092\u76ee\u6307\u305b\uff01\n\u00a77\u25b6 \u6b21\u306e\u30da\u30fc\u30b8\u304b\u3089\u5404\u30e2\u30fc\u30c9\u8aac\u660e\n\u00a77\u25b6 \u5de6\u306e\u77e2\u5370\u3067\u623b\u308b / \u53f3\u3067\u9032\u3080", "\u00a76\u00a7l=== \u30b2\u30fc\u30e0\u30e2\u30fc\u30c9\u8aac\u660e ===\n\u00a7c\u30d0\u30c8\u30eb\u30a2\u30ea\u30fc\u30ca: \u00a77\u6bb2\u6ec5or\u5236\u5727 3\u5148\u53d6\n\u00a7eTDM: \u00a77\u30ad\u30eb\u6570\u52dd\u8ca0 \u5373\u30ea\u30b9\u30dd\u30fc\u30f3\n\u00a74\u7206\u7834\u30df\u30c3\u30b7\u30e7\u30f3: \u00a77\u7206\u5f3e\u8a2d\u7f6e/\u89e3\u9664\n\u00a79\u5360\u9818\u6226: \u00a77\u62e0\u70b9\u5236\u5727 100pts\u5148\u53d6\n\u00a7bCTF: \u00a77\u6575\u65d7\u596a\u53d6 3\u56de\u5148\u53d6\n\u00a77\u25b6 \u6b21\u306e\u30da\u30fc\u30b8\u304b\u3089\u5404\u30e2\u30fc\u30c9\u8a73\u7d30", "\u00a7c\u00a7l=== \u30d0\u30c8\u30eb\u30a2\u30ea\u30fc\u30ca ===\n\u00a77\u30fb3\u30e9\u30a6\u30f3\u30c9\u5148\u53d6\u3067\u52dd\u5229\n\u00a77\u30fb\u6575\u6bb2\u6ec5\u3067\u30e9\u30a6\u30f3\u30c9\u52dd\u5229\n\u00a77\u30fb\u30ea\u30b9\u30dd\u30fc\u30f3\u306a\u3057\n\u00a77\u30fb\u4e2d\u592e\u30b3\u30f3\u30af\u30ea\u30fc\u30c825\u679a\u5236\u5727+15\u79d2\n\u00a77  \u30db\u30fc\u30eb\u30c9\u3067\u3082\u52dd\u5229\n\u00a77\u30fb\u30c1\u30fc\u30e0\u5354\u529b\u304c\u30ab\u30ae\uff01\n\u00a77\u30fb1\u30e9\u30a6\u30f3\u30c9\u306b1\u56de\u30d0\u30fc\u30b9\u30c8\u767a\u52d5\u53ef\u80fd", "\u00a7e\u00a7l=== TDM ===\n\u00a77\u30fb\u5236\u9650\u6642\u95935\u5206\u9593\n\u00a77\u30fb\u30ad\u30eb\u6570\u3067\u52dd\u8ca0\n\u00a77\u30fb\u6b7b\u4ea1\u3057\u3066\u3082\u30ea\u30b9\u30dd\u30fc\u30f3\u3042\u308a\n\u00a77\u30fb30\u30ad\u30eb\u5148\u53d6\u3067\u3082\u52dd\u5229\n\u00a77\u30fb\u7a4d\u6975\u7684\u306b\u653b\u3081\u308d\uff01\u30c7\u30b9\u3092\u6e1b\u3089\u305b\uff01", "\u00a74\u00a7l=== \u7206\u7834\u30df\u30c3\u30b7\u30e7\u30f3 ===\n\u00a77\u30fb\u653b\u6483\u5074\u306f\u7206\u5f3e\u3092\u8a2d\u7f6e(\u30af\u30ea\u30c3\u30af)\n\u00a77\u30fb\u8a2d\u7f6e\u5f8c45\u79d2\u8010\u3048\u308c\u3070\u8d77\u7206\n\u00a77\u30fb\u5b88\u5099\u5074\u306f\u7206\u5f3e\u89e3\u9664(7\u79d2)\u3067\u52dd\u5229\n\u00a77\u30fb\u6bb2\u6ec5\u3067\u3082\u52dd\u5229\u53ef\u80fd\n\u00a77\u30fb\u30e9\u30a6\u30f3\u30c9\u6bce\u306b\u653b\u5b88\u4ea4\u4ee3\n\u00a77\u30fb\u30ea\u30b9\u30dd\u30fc\u30f3\u306a\u3057", "\u00a79\u00a7l=== \u5360\u9818\u6226 ===\n\u00a77\u30fb\u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u3092\u5360\u9818\u305b\u3088\n\u00a77\u30fb\u62e0\u70b9\u306b\u8fd1\u304f\u306b\u7559\u307e\u308a\u7d9a\u3051\u308b\n\u00a77\u30fb\u5360\u9818\u62e0\u70b9\u304b\u3089\u6bce\u79d2\u30dd\u30a4\u30f3\u30c8\u7372\u5f97\n\u00a77\u30fb100pts\u5148\u53d6\u3067\u52dd\u5229\n\u00a77\u30fb\u30ea\u30b9\u30dd\u30fc\u30f3\u3042\u308a\n\u00a77\u30fb\u5473\u65b9\u3068\u9023\u643a\u3057\u3066\u596a\u53d6\u305b\u3088\uff01", "\u00a7b\u00a7l=== CTF ===\n\u00a77\u30fb\u6575\u9663\u306e\u65d7\u3092\u596a\u53d6\u305b\u3088\n\u00a77\u30fb\u81ea\u9663\u306b\u6301\u3061\u5e30\u308b\u30681\u30dd\u30a4\u30f3\u30c8\n\u00a77\u30fb3\u56de\u5148\u53d6\u3067\u52dd\u5229\n\u00a77\u30fb\u65d7\u306b\u8fd1\u3065\u3044\u3066\u30af\u30ea\u30c3\u30af\u3067\u53d6\u5f97\n\u00a77\u30fb\u6b7b\u4ea1\u6642\u306b\u65d7\u30c9\u30ed\u30c3\u30d7\n\u00a77\u30fb\u843d\u3061\u305f\u65d7\u306f\u62fe\u5f97\u53ef\u80fd(5\u79d2\u9593\u9694)\n\u00a77\u30fb\u5236\u9650\u6642\u959310\u5206/\u540c\u6570\u3067\u5f15\u304d\u5206\u3051", "\u00a76\u00a7l=== \u30ad\u30c3\u30c8\u30b7\u30b9\u30c6\u30e0 ===\n\u00a77\u30fb\u516834\u7a2e\u985e\u306e\u30ad\u30c3\u30c8\n\u00a7cDuelist: \u00a77\u524d\u7dda\u3067\u306e\u6226\u95d8\u7279\u5316\n\u00a7eInitiator: \u00a77\u6226\u95d8\u306e\u8d77\u70b9\u3092\u4f5c\u308b\n\u00a79Controller: \u00a77\u30a8\u30ea\u30a2\u5236\u5727/\u59a8\u5bb3\n\u00a72Sentinel: \u00a77\u5473\u65b9\u652f\u63f4/\u9632\u885b\n\u00a77\u30fb\u5404\u30ad\u30c3\u30c8\u56fa\u6709\u306e\u30b9\u30ad\u30eb\u3092\u6301\u3064\n\u00a77\u30fb\u5f79\u5272\u3092\u7406\u89e3\u3057\u3066\u9023\u643a\u305b\u3088\uff01", "\u00a76\u00a7l=== \u30b9\u30ad\u30eb\u767a\u52d5\u65b9\u6cd5 ===\n\u00a77\u30fb\u30a4\u30f3\u30d9\u30f3\u30c8\u30ea\u5185\u306e\u00a7f\u30b9\u30ad\u30eb\u661f\n\u00a77  (\u30cd\u30b6\u30fc\u30b9\u30bf\u30fc)\u3092\u53f3\u30af\u30ea\u30c3\u30af\n\u00a77\u30fb\u4e00\u90e8\u30ad\u30c3\u30c8\u306f\u5263/\u65a7/\u5f13\u3092\n\u00a77  \u6301\u3063\u3066\u3057\u3083\u304c\u307f\u53f3\u30af\u30ea\u30c3\u30af\n\u00a77\u30fb\u30b9\u30ad\u30eb\u306b\u306fCT(\u30af\u30fc\u30eb\u30bf\u30a4\u30e0)\u3042\u308a\n\u00a77\u30fbCT\u306f\u7d4c\u9a13\u5024\u30d0\u30fc\u306b\u8868\u793a\n\u00a77\u30fbCT\u4e2d\u306f\u30b9\u30ad\u30eb\u4f7f\u7528\u4e0d\u53ef", "\u00a7c\u00a7l=== \u30d0\u30fc\u30b9\u30c8 ===\n\u00a77\u30fb\u30b9\u30ed\u30c3\u30c89\u306e\u30cf\u30fc\u30c8\u30aa\u30d6\u30b6\u30b7\u30fc\n\u00a77\u30fb\u53f3\u30af\u30ea\u30c3\u30af\u3067\u767a\u52d5\n\u00a77\u30fb\u5468\u56f2\u5927\u7206\u767a+\u5439\u304d\u98db\u3070\u3057+\u5f31\u4f53\u5316\n\u00a77\u30fb1\u30e9\u30a6\u30f3\u30c9\u306b1\u56de\u306e\u307f\u4f7f\u7528\u53ef\u80fd\n\u00a77\u30fb\u4f7f\u7528\u5f8c\u30a2\u30a4\u30c6\u30e0\u306f\u6d88\u6ec5\n\u00a77\u30fb\u3053\u3053\u305e\u3068\u3044\u3046\u5834\u9762\u3067\u4f7f\u3048\uff01", "\u00a76\u00a7l=== \u7279\u6b8a\u6761\u4ef6 ===\n\u00a77\u30fb\u4e2d\u592e\u30b3\u30f3\u30af\u30ea\u30fc\u30c8\u5236\u5727(5x5)\n\u00a77  \u767d\u2192\u81ea\u8272\u306b\u5857\u308a\u66ff\u3048\u308d\n\u00a77  25\u679a\u5168\u3066\u81ea\u8272\u306715\u79d2\u30db\u30fc\u30eb\u30c9\n\u00a77\u30fb\u30ac\u30fc\u30c9\u30d6\u30ec\u30a4\u30af\n\u00a77  1\u79d2\u9593\u30b9\u30cb\u30fc\u30af\u3092\u6e9c\u3081\u3066\u653b\u6483\n\u00a77\u30fb\u98a8\u7a74\u30de\u30fc\u30af\n\u00a77  \u30cb\u30eb\u30ae\u30ea\u30bf\u30fc\u30eb\u306e\u7279\u6b8a\u653b\u6483"};
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        if (paneMeta != null) {
            paneMeta.setDisplayName(" ");
            pane.setItemMeta(paneMeta);
        }
        for (int i = 0; i < 54; ++i) {
            inv.setItem(i, pane.clone());
        }
        if (page >= 0 && page < pages.length) {
            String[] lines = pages[page].split("\n");
            int paperSlot = 20;
            int maxLinesPerPaper = 8;
            for (int i = 0; i < lines.length && paperSlot <= 24; i += maxLinesPerPaper) {
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta pm = paper.getItemMeta();
                if (pm != null) {
                    pm.setDisplayName(lines[i]);
                    ArrayList<String> lore = new ArrayList<String>();
                    for (int j = i + 1; j < Math.min(i + maxLinesPerPaper, lines.length); ++j) {
                        lore.add(lines[j]);
                    }
                    pm.setLore(lore);
                    paper.setItemMeta(pm);
                }
                inv.setItem(paperSlot++, paper);
            }
        }
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pm = prev.getItemMeta();
            if (pm != null) {
                pm.setDisplayName("\u00a7e\u25c0 \u524d\u306e\u30da\u30fc\u30b8");
                pm.getPersistentDataContainer().set(this.tutorialPrevKey, PersistentDataType.INTEGER, page);
                prev.setItemMeta(pm);
            }
            inv.setItem(45, prev);
        }
        if (page < pages.length - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nm = next.getItemMeta();
            if (nm != null) {
                nm.setDisplayName("\u00a7e\u6b21\u306e\u30da\u30fc\u30b8 \u25b6");
                nm.getPersistentDataContainer().set(this.tutorialNextKey, PersistentDataType.INTEGER, page);
                next.setItemMeta(nm);
            }
            inv.setItem(53, next);
        }
        if ((cm = (close = new ItemStack(Material.BARRIER)).getItemMeta()) != null) {
            cm.setDisplayName("\u00a7c\u9589\u3058\u308b");
            close.setItemMeta(cm);
        }
        inv.setItem(49, close);
        p.openInventory(inv);
    }

    public void openBgmList(Player p) {
        ItemStack offBtn;
        ItemMeta offMeta;
        Inventory inv = Bukkit.createInventory(null, (int)27, (String)"\u00a7d\u00a7lBGM\u9078\u629e");
        NbsPlayer cur = this.plugin.getGameManager().getSelectedBgm();
        ItemStack info = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("\u00a7d\u00a7l\u73fe\u5728\u306eBGM");
            List<String> infoLore = new ArrayList<String>();
            infoLore.add(cur != null ? "\u00a7a" + cur.getName() : "\u00a77\u306a\u3057");
            infoMeta.setLore(infoLore);
            info.setItemMeta(infoMeta);
        }
        inv.setItem(13, info);
        List<NbsPlayer> songs = this.plugin.getSongs();
        int songSlot = 11;
        for (NbsPlayer song : songs) {
            if (songSlot > 15) break;
            ItemStack songItem = new ItemStack(Material.MUSIC_DISC_CAT);
            ItemMeta songMeta = songItem.getItemMeta();
            if (songMeta != null) {
                songMeta.setDisplayName("\u00a7e" + song.getName());
                ArrayList<String> songLore = new ArrayList<String>();
                songLore.add("\u00a77\u30af\u30ea\u30c3\u30af\u3067\u9078\u629e");
                songMeta.setLore(songLore);
                songMeta.getPersistentDataContainer().set(this.bgmSongKey, PersistentDataType.STRING, song.getName());
                songItem.setItemMeta(songMeta);
            }
            inv.setItem(songSlot++, songItem);
        }
        if ((offMeta = (offBtn = new ItemStack(Material.BARRIER)).getItemMeta()) != null) {
            offMeta.setDisplayName("\u00a7cBGM OFF");
            offBtn.setItemMeta(offMeta);
        }
        inv.setItem(22, offBtn);
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("\u00a77\u00a7l\u2190 \u623b\u308b");
            back.setItemMeta(backMeta);
        }
        inv.setItem(26, back);
        p.openInventory(inv);
    }

    private ItemStack makeKitIcon(KitType kit) {
        ItemStack item = new ItemStack(KitEditorGUI.iconMaterial(kit));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(kit.getDisplayName());
            ArrayList<String> lore = new ArrayList<String>();
            lore.add("\u00a77" + kit.getDescription());
            lore.add("");
            lore.add("\u00a7e\u25b6 \u30af\u30ea\u30c3\u30af\u3067\u30a2\u30a4\u30c6\u30e0\u4e00\u89a7");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(this.kitKey, PersistentDataType.STRING, kit.name());
            item.setItemMeta(meta);
        }
        return item;
    }
}

