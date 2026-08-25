/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Color
 *  org.bukkit.FireworkEffect
 *  org.bukkit.FireworkEffect$Type
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.Damageable
 *  org.bukkit.inventory.meta.FireworkMeta
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.PotionMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionData
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.potion.PotionType
 */
package com.bloxarena.kit;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.TeamColor;
import com.bloxarena.kit.KitType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class KitBuilder {
    public static final Material BURST_MATERIAL = Material.HEART_OF_THE_SEA;
    private static boolean supporterRotationB = false;

    public static List<ItemStack> getDefaultItems(KitType kit, TeamColor team) {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        ItemStack pick = new ItemStack(Material.WOODEN_PICKAXE);
        KitBuilder.addCanDestroy(pick);
        list.add(pick);
        list.add(new ItemStack(Material.BREAD, 8));
        ItemStack concrete = new ItemStack(team.getConcrete(), 32);
        KitBuilder.addCanPlaceOn(concrete);
        list.add(concrete);
        switch (kit) {
            case BLADE: {
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                list.add(sword);
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case BREAKER: {
                ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
                axe.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(axe);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case NINJA: {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                list.add(sword);
                list.add(new ItemStack(Material.ENDER_PEARL, 4));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case BERSERKER: {
                ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
                axe.addEnchantment(Enchantment.DAMAGE_ALL, 5);
                list.add(axe);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case SNIPER: {
                ItemStack xbow = new ItemStack(Material.CROSSBOW);
                xbow.addEnchantment(Enchantment.PIERCING, 2);
                xbow.addEnchantment(Enchantment.QUICK_CHARGE, 1);
                list.add(xbow);
                list.add(new ItemStack(Material.ARROW, 8));
                list.add(new ItemStack(Material.WOODEN_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case COUNTER: {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                list.add(sword);
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case PYRO: {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addEnchantment(Enchantment.FIRE_ASPECT, 1);
                list.add(sword);
                ItemStack bow = new ItemStack(Material.BOW);
                bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
                list.add(bow);
                list.add(new ItemStack(Material.ARROW, 16));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case LANCER: {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                list.add(sword);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case JESTER: {
                ItemStack axe = new ItemStack(Material.STONE_AXE);
                axe.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                axe.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(axe);
                list.add(new ItemStack(Material.ENDER_PEARL, 3));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case VAMPIRE: {
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                sword.addEnchantment(Enchantment.KNOCKBACK, 1);
                list.add(sword);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case BOMBER: {
                list.add(new ItemStack(Material.IRON_SWORD));
                ItemStack bp = new ItemStack(Material.STONE_PICKAXE);
                KitBuilder.addCanDestroy(bp);
                list.add(bp);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case COOK: {
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.COOKED_BEEF));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case WHIRLWIND: {
                ItemStack wws = new ItemStack(Material.IRON_SWORD);
                wws.addEnchantment(Enchantment.KNOCKBACK, 1);
                list.add(wws);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case NILGIRITAR: {
                ItemStack nx = new ItemStack(Material.CROSSBOW);
                nx.addEnchantment(Enchantment.QUICK_CHARGE, 2);
                list.add(nx);
                list.add(new ItemStack(Material.ARROW, 16));
                ItemStack nsword = new ItemStack(Material.IRON_SWORD);
                nsword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(nsword);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case MISTRAL: {
                ItemStack ms = new ItemStack(Material.IRON_SWORD);
                ms.addEnchantment(Enchantment.KNOCKBACK, 1);
                ms.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(ms);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case SUPERIOR_MISTRAL: {
                ItemStack sms = new ItemStack(Material.DIAMOND_SWORD);
                sms.addEnchantment(Enchantment.DAMAGE_ALL, 5);
                sms.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                list.add(sms);
                ItemStack sxbow = new ItemStack(Material.CROSSBOW);
                sxbow.addEnchantment(Enchantment.QUICK_CHARGE, 2);
                sxbow.addEnchantment(Enchantment.PIERCING, 2);
                list.add(sxbow);
                list.add(new ItemStack(Material.ARROW, 32));
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case RELEASER: {
                ItemStack rs = new ItemStack(Material.IRON_SWORD);
                rs.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(rs);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case SCOUT: {
                list.add(new ItemStack(Material.BOW));
                list.add(new ItemStack(Material.SPECTRAL_ARROW, 24));
                list.add(new ItemStack(Material.ARROW, 8));
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case FLASHER: {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                sword.addEnchantment(Enchantment.KNOCKBACK, 1);
                list.add(sword);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case ROCKETER: {
                ItemStack crossbow = new ItemStack(Material.CROSSBOW);
                crossbow.addEnchantment(Enchantment.QUICK_CHARGE, 1);
                list.add(crossbow);
                list.add(new ItemStack(Material.ARROW, 16));
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case ALCHEMIST: {
                list.add(KitBuilder.makeSplash(PotionType.SLOWNESS));
                list.add(KitBuilder.makeSplash(PotionType.INSTANT_DAMAGE));
                list.add(KitBuilder.makeSplash(PotionType.POISON));
                ItemStack wp = new ItemStack(Material.SPLASH_POTION);
                PotionMeta wm = (PotionMeta)wp.getItemMeta();
                wm.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 0), true);
                wm.setColor(Color.fromRGB((int)72, (int)72, (int)72));
                wm.setDisplayName("\u00a77\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u5f31\u4f53\u5316");
                wp.setItemMeta((ItemMeta)wm);
                list.add(wp);
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case ENGINEER: {
                ItemStack ip = new ItemStack(Material.IRON_PICKAXE);
                ip.addEnchantment(Enchantment.DIG_SPEED, 2);
                KitBuilder.addCanDestroy(ip);
                list.add(ip);
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case TRAPPER: {
                ItemStack tSword = new ItemStack(Material.IRON_SWORD);
                tSword.addEnchantment(Enchantment.KNOCKBACK, 1);
                tSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(tSword);
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.ENDER_PEARL));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case GUARDIAN: {
                ItemStack sword = new ItemStack(Material.STONE_SWORD);
                sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 4);
                list.add(sword);
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case MEDIC: {
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case SUPPORTER: {
                list.add(KitBuilder.makeSupportPotion(PotionEffectType.SPEED, 1, 600, Color.fromRGB((int)124, (int)175, (int)198), "\u00a7b\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u00a7f\u30b9\u30d4\u30fc\u30c9"));
                list.add(KitBuilder.makeSupportPotion(PotionEffectType.DAMAGE_RESISTANCE, 1, 400, Color.fromRGB((int)75, (int)75, (int)75), "\u00a77\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u00a7f\u8010\u6027"));
                list.add(KitBuilder.makeSupportPotion(PotionEffectType.REGENERATION, 1, 200, Color.fromRGB((int)255, (int)153, (int)204), "\u00a7d\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u00a7f\u518d\u751f"));
                list.add(KitBuilder.makeSupportPotion(PotionEffectType.INCREASE_DAMAGE, 0, 300, Color.fromRGB((int)147, (int)38, (int)8), "\u00a7c\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u00a7f\u529b"));
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case MARKSMAN: {
                ItemStack bow = new ItemStack(Material.BOW);
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, 2);
                list.add(bow);
                list.add(new ItemStack(Material.ARROW, 12));
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case SUNDANCE: {
                ItemStack sundx = new ItemStack(Material.CROSSBOW);
                list.add(sundx);
                list.add(new ItemStack(Material.ARROW, 16));
                list.add(new ItemStack(Material.WOODEN_SWORD));
                break;
            }
            case RESTRICTIONER: {
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case TRANSPORTER: {
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case KREUTZ: {
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case SWAPPER: {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(sword);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case STICKER: {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addEnchantment(Enchantment.KNOCKBACK, 1);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(sword);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case DECOY: {
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case PHANTOM: {
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case ANCHOR: {
                ItemStack sword = new ItemStack(Material.STONE_SWORD);
                sword.addEnchantment(Enchantment.KNOCKBACK, 2);
                list.add(sword);
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case GRANG: {
                list.add(new ItemStack(Material.WOODEN_SWORD));
                list.add(new ItemStack(Material.SHIELD));
                break;
            }
            case NECRO: {
                list.add(new ItemStack(Material.WOODEN_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case BULWARK: {
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case TIMEKEEPER: {
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case AEGIS: {
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case HEXER: {
                list.add(new ItemStack(Material.WOODEN_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case REFLECTOR: {
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
            case GLACIES: {
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
                break;
            }
        }
        while (list.size() < 36) {
            list.add(null);
        }
        return list;
    }

    public static void giveKit(Player player, KitType kit, TeamColor team) {
        KitBuilder.giveKit(player, kit, team, null);
    }

    public static void giveKit(Player player, KitType kit, TeamColor team, BloxArenaPlugin plugin) {
        player.getInventory().clear();
        KitBuilder.giveBaseItems(player, team);
        switch (kit) {
            case BLADE: {
                KitBuilder.giveBlade(player, plugin);
                break;
            }
            case BREAKER: {
                KitBuilder.giveBreaker(player, plugin);
                break;
            }
            case NINJA: {
                KitBuilder.giveNinja(player, plugin);
                break;
            }
            case BERSERKER: {
                KitBuilder.giveBerserker(player, plugin);
                break;
            }
            case SNIPER: {
                KitBuilder.giveSniper(player, plugin);
                break;
            }
            case COUNTER: {
                KitBuilder.giveCounter(player, plugin);
                break;
            }
            case PYRO: {
                KitBuilder.givePyro(player, plugin);
                break;
            }
            case LANCER: {
                KitBuilder.giveLancer(player, plugin);
                break;
            }
            case JESTER: {
                KitBuilder.giveJester(player, plugin);
                break;
            }
            case VAMPIRE: {
                KitBuilder.giveVampire(player, plugin);
                break;
            }
            case BOMBER: {
                KitBuilder.giveBomber(player, plugin);
                break;
            }
            case COOK: {
                KitBuilder.giveCook(player, plugin);
                break;
            }
            case SCOUT: {
                KitBuilder.giveScout(player, plugin);
                break;
            }
            case WHIRLWIND: {
                KitBuilder.giveWhirlwind(player, plugin);
                break;
            }
            case NILGIRITAR: {
                KitBuilder.giveNilgiritar(player, plugin);
                break;
            }
            case MISTRAL: {
                KitBuilder.giveMistral(player, plugin);
                break;
            }
            case SUPERIOR_MISTRAL: {
                KitBuilder.giveSuperiorMistral(player, plugin);
                break;
            }
            case DECOY: {
                KitBuilder.giveDecoy(player, plugin);
                break;
            }
            case FLASHER: {
                KitBuilder.giveFlasher(player, plugin);
                break;
            }
            case ROCKETER: {
                KitBuilder.giveRocketer(player, plugin);
                break;
            }
            case ALCHEMIST: {
                KitBuilder.giveAlchemist(player, plugin);
                break;
            }
            case ENGINEER: {
                KitBuilder.giveEngineer(player, plugin);
                break;
            }
            case TRAPPER: {
                KitBuilder.giveTrapper(player, plugin);
                break;
            }
            case GUARDIAN: {
                KitBuilder.giveGuardian(player, plugin);
                break;
            }
            case MEDIC: {
                KitBuilder.giveMedic(player, plugin);
                break;
            }
            case SUPPORTER: {
                KitBuilder.giveSupporter(player, plugin);
                break;
            }
            case RELEASER: {
                KitBuilder.giveReleaser(player, plugin);
                break;
            }
            case MARKSMAN: {
                KitBuilder.giveMarksman(player, plugin);
                break;
            }
            case SUNDANCE: {
                KitBuilder.giveSundance(player, plugin);
                break;
            }
            case RESTRICTIONER: {
                KitBuilder.giveRestrictioner(player, plugin);
                break;
            }
            case TRANSPORTER: {
                KitBuilder.giveTransporter(player, plugin);
                break;
            }
            case KREUTZ: {
                KitBuilder.giveKreutz(player, plugin);
                break;
            }
            case SWAPPER: {
                KitBuilder.giveSwapper(player, plugin);
                break;
            }
            case STICKER: {
                KitBuilder.giveSticker(player, plugin);
                break;
            }
            case PHANTOM: {
                KitBuilder.givePhantom(player, plugin);
                break;
            }
            case ANCHOR: {
                KitBuilder.giveAnchor(player, plugin);
                break;
            }
            case GRANG: {
                KitBuilder.giveGrang(player, plugin);
                break;
            }
            case NECRO: {
                KitBuilder.giveNecro(player, plugin);
                break;
            }
            case BULWARK: {
                KitBuilder.giveBulwark(player, plugin);
                break;
            }
            case TIMEKEEPER: {
                KitBuilder.giveTimekeeper(player, plugin);
                break;
            }
            case AEGIS: {
                KitBuilder.giveAegis(player, plugin);
                break;
            }
            case HEXER: {
                KitBuilder.giveHexer(player, plugin);
                break;
            }
            case REFLECTOR: {
                KitBuilder.giveReflector(player, plugin);
                break;
            }
            case GLACIES: {
                KitBuilder.giveGlacies(player, plugin);
                break;
            }
        }
    }

    private static void giveBaseItems(Player player, TeamColor team) {
        ItemStack pick = new ItemStack(Material.WOODEN_PICKAXE);
        KitBuilder.addCanDestroy(pick);
        player.getInventory().addItem(new ItemStack[]{pick});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.BREAD, 8)});
        ItemStack concrete = new ItemStack(team.getConcrete(), 32);
        KitBuilder.addCanPlaceOn(concrete);
        player.getInventory().addItem(new ItemStack[]{concrete});
    }

    private static void giveBlade(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        ItemMeta sm = sword.getItemMeta();
        if (sm != null) {
            sm.setLore(List.of("\u00a77\u00a7o\u53f3\u30af\u30ea\u30c3\u30af: \u8987\u65ac - \u5468\u56f2\u6253\u3061\u4e0a\u3052\uff0b\u5f31\u4f53"));
            sm.getPersistentDataContainer().set(new NamespacedKey((Plugin)plugin, "kit_skill"), PersistentDataType.STRING, "BLADE");
            sword.setItemMeta(sm);
        }
        player.getInventory().addItem(new ItemStack[]{sword});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.SHIELD)});
        KitBuilder.giveIronArmor(player);
    }

    private static void giveBreaker(Player player, BloxArenaPlugin plugin) {
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        ItemMeta am = axe.getItemMeta();
        if (am != null) {
            am.setLore(List.of("\u00a77\u00a7o\u53f3\u30af\u30ea\u30c3\u30af: \u70c8\u7a81 - \u9ad8\u901f\u7a81\u9032\uff0b\u63a5\u89e6\u30c0\u30e1\u30fc\u30b8"));
            am.getPersistentDataContainer().set(new NamespacedKey((Plugin)plugin, "kit_skill"), PersistentDataType.STRING, "BREAKER");
            axe.setItemMeta(am);
        }
        player.getInventory().addItem(new ItemStack[]{axe});
        KitBuilder.giveIronArmor(player);
    }

    private static void giveNinja(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        player.getInventory().addItem(new ItemStack[]{sword});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ENDER_PEARL, 4)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.NINJA, "\u00a72\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u96a0\u5f62")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveBerserker(Player player, BloxArenaPlugin plugin) {
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addEnchantment(Enchantment.DAMAGE_ALL, 5);
        ItemMeta bm = axe.getItemMeta();
        if (bm != null) {
            bm.setLore(List.of("\u00a77\u00a7o\u53f3\u30af\u30ea\u30c3\u30af: \u6012\u6d9b\u7206\u7815 - \u524d\u65b9\u9023\u7d9a\u7206\u767a"));
            bm.getPersistentDataContainer().set(new NamespacedKey((Plugin)plugin, "kit_skill"), PersistentDataType.STRING, "BERSERKER");
            axe.setItemMeta(bm);
        }
        player.getInventory().addItem(new ItemStack[]{axe});
        KitBuilder.giveLeatherArmor(player);
    }

    private static void giveSniper(Player player, BloxArenaPlugin plugin) {
        ItemStack xbow = new ItemStack(Material.CROSSBOW);
        xbow.addEnchantment(Enchantment.PIERCING, 2);
        xbow.addEnchantment(Enchantment.QUICK_CHARGE, 1);
        ItemMeta xm = xbow.getItemMeta();
        ArrayList<String> xl = new ArrayList<String>();
        xl.add("\u00a77\u3057\u3083\u304c\u307f+\u5730\u4e0a\u3067\u6575\u3092\u7167\u6e967\u79d2\u2192\u5373\u6b7b\u30de\u30fc\u30af");
        xm.setLore(xl);
        xbow.setItemMeta(xm);
        player.getInventory().addItem(new ItemStack[]{xbow});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ARROW, 8)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.WOODEN_SWORD)});
        KitBuilder.giveLeatherArmor(player);
    }

    private static void giveCounter(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        player.getInventory().addItem(new ItemStack[]{sword});
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemMeta sm = shield.getItemMeta();
        ArrayList<String> sl = new ArrayList<String>();
        sl.add("\u00a77\u3057\u3083\u304c\u307f\u53f3\u30af\u30ea\u21920.5\u79d2\u30d1\u30ea\u30a3");
        sm.setLore(sl);
        shield.setItemMeta(sm);
        player.getInventory().addItem(new ItemStack[]{shield});
        KitBuilder.giveIronArmor(player);
    }

    private static void givePyro(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.FIRE_ASPECT, 1);
        ItemMeta psm = sword.getItemMeta();
        if (psm != null) {
            psm.setLore(List.of("\u00a77\u00a7o\u53f3\u30af\u30ea\u30c3\u30af: \u696d\u708e - \u5468\u56f2\u708e\u4e0a/\u708e\u4e0a\u4e2d\u5373\u5927\u30c0\u30e1"));
            sword.setItemMeta(psm);
        }
        player.getInventory().addItem(new ItemStack[]{sword});
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
        ItemMeta pbm = bow.getItemMeta();
        if (pbm != null) {
            pbm.setLore(List.of("\u00a77\u00a7o\u708e\u306e\u5f13 - Flame I"));
            bow.setItemMeta(pbm);
        }
        player.getInventory().addItem(new ItemStack[]{bow});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ARROW, 16)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.PYRO, "\u00a76\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u696d\u708e")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveLancer(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.IRON_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.LANCER, "\u00a7b\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u523a\u7a81")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveJester(Player player, BloxArenaPlugin plugin) {
        ItemStack axe = new ItemStack(Material.STONE_AXE);
        axe.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        axe.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(new ItemStack[]{axe});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ENDER_PEARL, 3)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.JESTER, "\u00a7e\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u9053\u5316\u306e\u75be\u8d70")});
        }
        KitBuilder.giveLeatherArmor(player);
    }

    private static void giveVampire(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        ItemMeta sm = sword.getItemMeta();
        if (sm != null) {
            sm.getPersistentDataContainer().set(new NamespacedKey((Plugin)plugin, "vampire_skill"), PersistentDataType.STRING, "VAMPIRE");
            sm.setLore(List.of("\u00a77\u00a7o\u53f3\u30af\u30ea\u30c3\u30af: \u30c6\u30aa\u30b9\u30d1\u30fc\u30c0 \u00a77(\u30b9\u30cb\u30fc\u30af\u3067\u5438\u53ce\u5f3e)"));
            sword.setItemMeta(sm);
        }
        player.getInventory().addItem(new ItemStack[]{sword});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.VAMPIRE, "\u00a74\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30d6\u30e9\u30c3\u30c9\u30e2\u30fc\u30c9")});
        }
        KitBuilder.giveIronArmor(player);
        player.setMaxHealth(14.0);
        player.setHealth(14.0);
        if (plugin != null) {
            plugin.getSkillManager().initVampireDebuffs(player);
        }
    }

    private static void giveBomber(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.IRON_SWORD)});
        ItemStack pick = new ItemStack(Material.STONE_PICKAXE);
        KitBuilder.addCanDestroy(pick);
        player.getInventory().addItem(new ItemStack[]{pick});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.BOMBER, "\u00a7c\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u5730\u96f7\u8a2d\u7f6e")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveGlider(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(new ItemStack[]{sword});
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        Damageable dmg = (Damageable)elytra.getItemMeta();
        if (dmg != null) {
            dmg.setDamage(Math.max(0, elytra.getType().getMaxDurability() - 60));
            elytra.setItemMeta((ItemMeta)dmg);
        }
        player.getInventory().setChestplate(elytra);
        player.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
    }

    private static void giveGrang(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        player.getInventory().addItem(new ItemStack[]{sword});
        ItemStack gs = new ItemStack(Material.SHIELD);
        ItemMeta gsm = gs.getItemMeta();
        ArrayList<String> gsl = new ArrayList<String>();
        gsl.add("\u00a77\u3057\u3083\u304c\u307f\u7d9a\u3051\u308b\u2192\u30c1\u30e3\u30fc\u30b8\u2192\u7a81\u6483");
        gsm.setLore(gsl);
        gs.setItemMeta(gsm);
        player.getInventory().addItem(new ItemStack[]{gs});
        KitBuilder.giveLeatherArmor(player);
    }

    private static void giveScout(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.BOW)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.SPECTRAL_ARROW, 24)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ARROW, 8)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.STONE_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.SCOUT, "\u00a7a\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30ea\u30b3\u30f3\u30dc\u30eb\u30c8")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveFlasher(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        player.getInventory().addItem(new ItemStack[]{sword});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.FLASHER, "\u00a7b\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30d5\u30e9\u30c3\u30b7\u30e5\u30d0\u30f3")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveMarksman(Player player, BloxArenaPlugin plugin) {
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_DAMAGE, 2);
        player.getInventory().addItem(new ItemStack[]{bow});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ARROW, 12)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.STONE_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.MARKSMAN, "\u00a7c\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30d8\u30f4\u30a3\u30fc\u30dc\u30eb\u30c8")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveSundance(Player player, BloxArenaPlugin plugin) {
        ItemStack xbow = new ItemStack(Material.CROSSBOW);
        ItemMeta xm2 = xbow.getItemMeta();
        ArrayList<String> xl2 = new ArrayList<String>();
        xl2.add("\u00a77\u3057\u3083\u304c\u307f\u53f3\u30af\u30ea\u21926\u9023\u5c04\u30ea\u30dc\u30eb\u30d3\u30f3\u30b0");
        xm2.setLore(xl2);
        xbow.setItemMeta(xm2);
        player.getInventory().addItem(new ItemStack[]{xbow});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ARROW, 16)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.WOODEN_SWORD)});
        KitBuilder.giveLeatherArmor(player);
    }

    private static void giveSwapper(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(new ItemStack[]{sword});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.SWAPPER, "\u00a75\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u77ac\u9593\u4ea4\u5dee")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveSticker(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(new ItemStack[]{sword});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.STICKER, "\u00a73\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30b0\u30e9\u30c3\u30d7\u30eb")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveDecoy(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.IRON_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.DECOY, "\u00a78\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30c7\u30b3\u30a4\u5c55\u958b")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveRocketer(Player player, BloxArenaPlugin plugin) {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        crossbow.addEnchantment(Enchantment.QUICK_CHARGE, 1);
        crossbow.addEnchantment(Enchantment.PIERCING, 1);
        player.getInventory().addItem(new ItemStack[]{crossbow});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ARROW, 16)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.IRON_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.ROCKETER, "\u00a7e\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30e1\u30ac\u30ed\u30b1\u30c3\u30c8")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveAlchemist(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSplash(PotionType.SLOWNESS)});
        player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSplash(PotionType.INSTANT_DAMAGE)});
        player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSplash(PotionType.POISON)});
        ItemStack wp = new ItemStack(Material.SPLASH_POTION);
        PotionMeta wm = (PotionMeta)wp.getItemMeta();
        wm.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 0), true);
        wm.setColor(Color.fromRGB((int)72, (int)72, (int)72));
        wm.setDisplayName("\u00a77\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u5f31\u4f53\u5316");
        wp.setItemMeta((ItemMeta)wm);
        player.getInventory().addItem(new ItemStack[]{wp});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.IRON_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.ALCHEMIST, "\u00a7d\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u518d\u8abf\u5408")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveEngineer(Player player, BloxArenaPlugin plugin) {
        ItemStack ip = new ItemStack(Material.IRON_PICKAXE);
        ip.addEnchantment(Enchantment.DIG_SPEED, 2);
        KitBuilder.addCanDestroy(ip);
        player.getInventory().addItem(new ItemStack[]{ip});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.IRON_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.ENGINEER, "\u00a76\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30ec\u30fc\u30b6\u30fc\u30bf\u30ec\u30c3\u30c8")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveRestrictioner(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.STONE_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.RESTRICTIONER, "\u00a78\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30c7\u30c3\u30c9\u30ed\u30c3\u30af")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveTransporter(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        player.getInventory().addItem(new ItemStack[]{sword});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.TRANSPORTER, "\u00a73\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30ef\u30fc\u30d7\u30b2\u30fc\u30c8")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveMimic(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.STONE_SWORD)});
        ItemStack ip2 = new ItemStack(Material.IRON_PICKAXE);
        KitBuilder.addCanDestroy(ip2);
        player.getInventory().addItem(new ItemStack[]{ip2});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.valueOf("MIMIC"), "\u00a75\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30b9\u30ad\u30eb\u30b3\u30d4\u30fc")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveNecro(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        player.getInventory().addItem(new ItemStack[]{sword});
        KitBuilder.giveLeatherArmor(player);
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.NECRO, "\u00a78\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u5c4d\u4f53\u821e\u8e0f")});
        }
    }

    private static void giveCook(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        player.getInventory().addItem(new ItemStack[]{sword});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.COOK, "\u00a76\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u8abf\u7406")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveWhirlwind(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        player.getInventory().addItem(new ItemStack[]{sword});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.WHIRLWIND, "\u00a7f\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u6c17\u6d41\u7832+\u65cb\u98a8\u5f3e")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveReleaser(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(new ItemStack[]{sword});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.RELEASER, "\u00a7e\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u89e3\u653e(\u30ea\u30ea\u30fc\u30b9)")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveNilgiritar(Player player, BloxArenaPlugin plugin) {
        ItemStack xbow = new ItemStack(Material.CROSSBOW);
        xbow.addEnchantment(Enchantment.PIERCING, 1);
        xbow.addEnchantment(Enchantment.QUICK_CHARGE, 2);
        player.getInventory().addItem(new ItemStack[]{xbow});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ARROW, 16)});
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(new ItemStack[]{sword});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.NILGIRITAR, "\u00a7f\ud83c\udf00 \u30b9\u30ad\u30eb: \u98a8\u7a74\u611f\u77e5")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveMistral(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(new ItemStack[]{sword});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.MISTRAL, "\u00a7f\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u70c8\u98a8\u7832")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveSuperiorMistral(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 5);
        sword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
        player.getInventory().addItem(new ItemStack[]{sword});
        ItemStack xbow = new ItemStack(Material.CROSSBOW);
        xbow.addEnchantment(Enchantment.QUICK_CHARGE, 2);
        xbow.addEnchantment(Enchantment.PIERCING, 2);
        player.getInventory().addItem(new ItemStack[]{xbow});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ARROW, 32)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.SHIELD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.SUPERIOR_MISTRAL, "\u00a76\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u7a76\u6975\u70c8\u98a8\u7832")});
        }
        KitBuilder.giveDiamondArmor(player);
    }

    private static void giveKreutz(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.STONE_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.KREUTZ, "\u00a75\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u9b54\u6cd5\u30ab\u30fc\u30c9")});
        }
        KitBuilder.giveLeatherArmor(player);
    }

    private static void giveTrapper(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(new ItemStack[]{sword});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.SHIELD)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ENDER_PEARL)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.TRAPPER, "\u00a73\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30c7\u30b9\u30c8\u30e9\u30c3\u30d7")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveGuardian(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 4);
        player.getInventory().addItem(new ItemStack[]{sword});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.SHIELD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.GUARDIAN, "\u00a7f\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u9244\u58c1")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveMedic(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.IRON_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.MEDIC, "\u00a75\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30d5\u30a3\u30fc\u30eb\u30c9\u30b1\u30a2")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveSupporter(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSupportPotion(PotionEffectType.INCREASE_DAMAGE, 0, 300, Color.fromRGB((int)147, (int)38, (int)8), "\u00a7c\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u00a7f\u529b")});
        player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSupportPotion(PotionEffectType.REGENERATION, 1, 200, Color.fromRGB((int)255, (int)153, (int)204), "\u00a7d\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u00a7f\u518d\u751f")});
        player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSplash(PotionType.INSTANT_HEAL)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.STONE_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.SUPPORTER, "\u00a7a\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u518d\u8abf\u9054")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void givePhantom(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.IRON_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.PHANTOM, "\u00a77\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u970a\u4f53\u5316")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveAnchor(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 2);
        player.getInventory().addItem(new ItemStack[]{sword});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.ANCHOR, "\u00a79\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u78c1\u5834\u5c55\u958b")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveBulwark(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.IRON_SWORD)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.SHIELD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.BULWARK, "\u00a7f\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30a6\u30a9\u30fc\u30eb\u5c55\u958b")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveTimekeeper(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.STONE_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.TIMEKEEPER, "\u00a7b\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30ea\u30ef\u30a4\u30f3\u30c9+\u30af\u30ed\u30c3\u30af\u30b9\u30c8\u30c3\u30d7")});
        }
        KitBuilder.giveLeatherArmor(player);
    }

    private static void giveAegis(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.IRON_SWORD)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.SHIELD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.AEGIS, "\u00a7a\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30ac\u30fc\u30c7\u30a3\u30a2\u30f3\u30dc\u30f3\u30c9")});
        }
        KitBuilder.giveIronArmor(player);
    }

    private static void giveHexer(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.WOODEN_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.HEXER, "\u00a75\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u546a\u7e1b\u9818\u57df")});
        }
        KitBuilder.giveLeatherArmor(player);
    }

    private static void giveReflector(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.STONE_SWORD)});
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.SHIELD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.REFLECTOR, "\u00a7f\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30df\u30e9\u30fc\u30b9\u30bf\u30f3\u30b9")});
        }
        KitBuilder.giveChainArmor(player);
    }

    private static void giveGlacies(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.STONE_SWORD)});
        if (plugin != null) {
            player.getInventory().addItem(new ItemStack[]{KitBuilder.makeSkillItem(plugin, KitType.GLACIES, "\u00a7b\u00a7l\ud83c\udff7 \u30b9\u30ad\u30eb: \u30d5\u30ed\u30b9\u30c8\u30b9\u30c8\u30e9\u30a4\u30af")});
        }
        KitBuilder.giveLeatherArmor(player);
    }

    private static void giveIronArmor(Player p) {
        p.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
    }

    private static void giveChainArmor(Player p) {
        p.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        p.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        p.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
    }

    private static void giveDiamondArmor(Player p) {
        p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        p.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    }

    private static void giveLeatherArmor(Player p) {
        p.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        p.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        p.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
    }

    private static ItemStack makePotion(PotionType type, boolean splash, boolean upgraded) {
        Material mat = splash ? Material.SPLASH_POTION : Material.POTION;
        ItemStack item = new ItemStack(mat);
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setBasePotionData(new PotionData(type, false, upgraded));
        String suffix = upgraded ? "II" : "I";
        switch (type) {
            case STRENGTH: {
                meta.setColor(Color.fromRGB((int)147, (int)38, (int)8));
                meta.setDisplayName("\u00a7c\u529b " + suffix);
                break;
            }
            case SPEED: {
                meta.setColor(Color.fromRGB((int)124, (int)175, (int)198));
                meta.setDisplayName("\u00a7b\u30b9\u30d4\u30fc\u30c9 " + suffix);
                break;
            }
        }
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    private static ItemStack makeSplash(PotionType type) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return item;
        }
        try {
            meta.setBasePotionData(new PotionData(type, false, true));
        }
        catch (Exception e) {
            meta.setBasePotionData(new PotionData(type, false, false));
        }
        switch (type) {
            case SLOWNESS: {
                meta.setColor(Color.fromRGB((int)74, (int)90, (int)91));
                meta.setDisplayName("\u00a77\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u920d\u5316II");
                break;
            }
            case INSTANT_DAMAGE: {
                meta.setColor(Color.fromRGB((int)255, (int)0, (int)0));
                meta.setDisplayName("\u00a7c\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u5373\u6642\u30c0\u30e1\u30fc\u30b8");
                break;
            }
            case POISON: {
                meta.setColor(Color.fromRGB((int)78, (int)147, (int)49));
                meta.setDisplayName("\u00a72\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u6bd2");
                break;
            }
            case WEAKNESS: {
                meta.setColor(Color.fromRGB((int)72, (int)72, (int)72));
                meta.setDisplayName("\u00a77\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u5f31\u4f53\u5316");
                break;
            }
        }
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    private static ItemStack makeSplashCustom(PotionEffectType type, int amplifier, int duration) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    private static ItemStack makeSupportPotion(PotionEffectType type, int amplifier, int duration, Color color, String displayName) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        meta.setColor(color);
        meta.setDisplayName(displayName);
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    private static ItemStack makeBlindnessSplash() {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 120, 0), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 120, 1), true);
        meta.setColor(Color.fromRGB((int)10, (int)10, (int)10));
        meta.setDisplayName("\u00a78\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u00a7f\u76f2\u76ee\uff0b\u920d\u8db3");
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    private static ItemStack makeLingeringCustom(PotionEffectType type, int amplifier, int duration) {
        ItemStack item = new ItemStack(Material.LINGERING_POTION);
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        if (type == PotionEffectType.SLOW) {
            meta.setColor(Color.fromRGB((int)74, (int)90, (int)91));
            meta.setDisplayName("\u00a77\u6ede\u7559 \u920d\u5316");
        }
        if (type == PotionEffectType.POISON) {
            meta.setColor(Color.fromRGB((int)78, (int)147, (int)49));
            meta.setDisplayName("\u00a72\u6ede\u7559 \u6bd2");
        }
        if (type == PotionEffectType.WEAKNESS) {
            meta.setColor(Color.fromRGB((int)72, (int)72, (int)72));
            meta.setDisplayName("\u00a77\u6ede\u7559 \u5f31\u4f53\u5316");
        }
        if (type == PotionEffectType.HARM) {
            meta.setColor(Color.fromRGB((int)255, (int)0, (int)0));
            meta.setDisplayName("\u00a7c\u6ede\u7559 \u5373\u6642\u30c0\u30e1\u30fc\u30b8");
        }
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    private static ItemStack makeLingeringBlindness() {
        ItemStack item = new ItemStack(Material.LINGERING_POTION);
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0), true);
        meta.setColor(Color.fromRGB((int)10, (int)10, (int)10));
        meta.setDisplayName("\u00a78\u6ede\u7559 \u76f2\u76ee");
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    private static ItemStack makeFireworkRocket(int count) {
        return new ItemStack(Material.FIREWORK_ROCKET, count);
    }

    private static ItemStack makeExplosiveRocket(int count) {
        ItemStack item = new ItemStack(Material.FIREWORK_ROCKET, count);
        FireworkMeta meta = (FireworkMeta)item.getItemMeta();
        if (meta == null) {
            return item;
        }
        FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(new Color[]{Color.RED, Color.ORANGE}).withFade(Color.YELLOW).trail(true).flicker(false).build();
        meta.addEffect(effect);
        meta.addEffect(effect);
        meta.addEffect(effect);
        meta.setPower(1);
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    private static ItemStack makeHealArrow(int count) {
        ItemStack item = new ItemStack(Material.TIPPED_ARROW, count);
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 1, 2), true);
        meta.setColor(Color.fromRGB((int)248, (int)87, (int)166));
        meta.setDisplayName("\u00a7d\u56de\u5fa9\u306e\u77e2");
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    private static ItemStack makeGlowingSplash(int count) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION, count);
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.addCustomEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0), true);
        meta.setColor(Color.fromRGB((int)255, (int)255, (int)80));
        meta.setDisplayName("\u00a7e\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u767a\u5149");
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    private static ItemStack makeInvisSplash() {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0), true);
        meta.setColor(Color.fromRGB((int)200, (int)200, (int)200));
        meta.setDisplayName("\u00a7f\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u900f\u660e\u5316");
        item.setItemMeta((ItemMeta)meta);
        return item;
    }

    public static ItemStack makeBurstItem() {
        BloxArenaPlugin plugin = (BloxArenaPlugin)Bukkit.getPluginManager().getPlugin("BloxArenaII");
        ItemStack item = new ItemStack(BURST_MATERIAL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a7c\u00a7l\ud83d\udca5 \u30d0\u30fc\u30b9\u30c8 \u00a77(\u53f3\u30af\u30ea\u30c3\u30af)");
            meta.setLore(List.of("\u00a77\u5468\u56f2\u306e\u6575\u3092\u5439\u304d\u98db\u3070\u3057\uff0b\u30c0\u30e1\u30fc\u30b8", "\u00a77\u81ea\u8eab\u3068\u6575\u306b\u77ed\u6642\u9593\u5f31\u4f53\u5316\u4ed8\u4e0e", "\u00a7c\u00a7l1\u30e9\u30a6\u30f3\u30c91\u56de\u9650\u308a \u00a77\u4f7f\u7528\u5f8c\u6d88\u6ec5"));
            meta.getPersistentDataContainer().set(new NamespacedKey((Plugin)plugin, "burst_skill"), PersistentDataType.BYTE, (byte)1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isBurstItem(ItemStack item) {
        if (item == null || item.getType() != BURST_MATERIAL || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey((Plugin)((BloxArenaPlugin)Bukkit.getPluginManager().getPlugin("BloxArenaII")), "burst_skill");
        return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    private static ItemStack makeSkillItem(BloxArenaPlugin plugin, KitType kit, String displayName) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.getPersistentDataContainer().set(new NamespacedKey((Plugin)plugin, "kit_skill"), PersistentDataType.STRING, kit.name());
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void addCanDestroy(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.setCanDestroy(Set.of(Material.WHITE_CONCRETE, Material.RED_CONCRETE, Material.CYAN_CONCRETE));
        item.setItemMeta(meta);
    }

    private static void addCanPlaceOn(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.setCanPlaceOn(Set.of(Material.LIME_CONCRETE));
        item.setItemMeta(meta);
    }

    public static void refillAlchemistPotions(Player p) {
        for (int i = 0; i < p.getInventory().getSize(); ++i) {
            ItemStack item = p.getInventory().getItem(i);
            if (item == null || item.getType() != Material.SPLASH_POTION) continue;
            p.getInventory().setItem(i, null);
        }
        p.getInventory().addItem(new ItemStack[]{KitBuilder.makeSplash(PotionType.SLOWNESS)});
        p.getInventory().addItem(new ItemStack[]{KitBuilder.makeSplash(PotionType.INSTANT_DAMAGE)});
        p.getInventory().addItem(new ItemStack[]{KitBuilder.makeSplash(PotionType.POISON)});
        ItemStack wp = new ItemStack(Material.SPLASH_POTION);
        PotionMeta wm = (PotionMeta)wp.getItemMeta();
        wm.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 0), true);
        wm.setColor(Color.fromRGB((int)72, (int)72, (int)72));
        wm.setDisplayName("\u00a77\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u5f31\u4f53\u5316");
        wp.setItemMeta((ItemMeta)wm);
        p.getInventory().addItem(new ItemStack[]{wp});
    }

    public static void refillSupporterPotions(Player p) {
        for (int i = 0; i < p.getInventory().getSize(); ++i) {
            ItemStack item = p.getInventory().getItem(i);
            if (item == null || item.getType() != Material.SPLASH_POTION) continue;
            p.getInventory().setItem(i, null);
        }
        if (supporterRotationB) {
            p.sendMessage("\u00a72\u00a7l\u518d\u88dc\u7d66 [B] \u00a77\u30b9\u30d4\u30fc\u30c9+\u885d\u6483\u5438\u53ce");
            p.getInventory().addItem(new ItemStack[]{KitBuilder.makeSupportPotion(PotionEffectType.SPEED, 1, 600, Color.fromRGB((int)124, (int)175, (int)198), "\u00a7b\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u00a7f\u30b9\u30d4\u30fc\u30c9")});
            p.getInventory().addItem(new ItemStack[]{KitBuilder.makeSupportPotion(PotionEffectType.ABSORPTION, 3, 500, Color.fromRGB((int)75, (int)75, (int)75), "\u00a77\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u00a7f\u5438\u53ce")});
        } else {
            p.sendMessage("\u00a72\u00a7l\u518d\u88dc\u7d66 [A] \u00a77\u529b+\u518d\u751f+\u5373\u6642\u56de\u5fa9");
            p.getInventory().addItem(new ItemStack[]{KitBuilder.makeSupportPotion(PotionEffectType.INCREASE_DAMAGE, 0, 300, Color.fromRGB((int)147, (int)38, (int)8), "\u00a7c\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u00a7f\u529b")});
            p.getInventory().addItem(new ItemStack[]{KitBuilder.makeSupportPotion(PotionEffectType.REGENERATION, 0, 200, Color.fromRGB((int)255, (int)153, (int)204), "\u00a7d\u30b9\u30d7\u30e9\u30c3\u30b7\u30e5 \u00a7f\u518d\u751f")});
            p.getInventory().addItem(new ItemStack[]{KitBuilder.makeSplash(PotionType.INSTANT_HEAL)});
        }
        supporterRotationB = !supporterRotationB;
    }

    private static ItemStack makeKitGuide(KitType kit) {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a7e\u00a7l\ud83d\udcd6 " + kit.getName() + " \u89e3\u8aac");
            meta.setLore(List.of("\u00a77" + kit.getDescription(), "", "\u00a76\u30b9\u30ad\u30eb: \u00a7f" + KitBuilder.getSkillInfo(kit), "\u00a7a\u88c5\u5099: \u00a7f" + KitBuilder.getGearSummary(kit), "", "\u00a7c\ud83d\udca5 \u30d0\u30fc\u30b9\u30c8: \u00a7f\u5468\u56f2\u7206\u767a+\u6575\u5439\u98db+\u5f31\u4f53\u5316", "\u00a77  \u00a7o\u53f3\u30af\u30ea\u30c3\u30af / 1\u30e9\u30a6\u30f3\u30c91\u56de", "\u00a77  \u00a7o\u4f7f\u7528\u5f8c\u6d88\u6ec5", "", "\u00a77\u5de6\u30af\u30ea\u30c3\u30af\u3067\u8a73\u7d30\u3092\u898b\u308b"));
            book.setItemMeta(meta);
        }
        return book;
    }

    private static String getSkillInfo(KitType kit) {
        return switch (kit) {
            case BLADE -> "\u8987\u65ac - \u5468\u56f2\u6575\u6253\u4e0a\u3052+\u30c0\u30e1+\u5f31\u4f53";
            case BREAKER -> "\u70c8\u7a81 - \u524d\u65b9\u7a81\u9032+\u63a5\u89e6\u30c0\u30e1+\u5f31\u4f53+\u5439\u98db";
            case NINJA -> "\u96a0\u5f62 - 8\u79d2\u900f\u660e\u5316+Speed II";
            case BERSERKER -> "\u6012\u6d9b\u7206\u7815 - \u524d\u65b915m\u9023\u7d9a\u7206\u767a";
            case SNIPER -> "\u72d9\u6483\u773c - 15\u79d2\u7167\u6e96\u2192\u5373\u6b7b\u30de\u30fc\u30af";
            case COUNTER -> "\u92fc\u306e\u53cd\u5c04 - \u76fe\u69cb\u3048+\u30b9\u30cb\u30fc\u30af\u3067\u30d1\u30ea\u30a3";
            case PYRO -> "\u696d\u708e - \u5468\u56f2\u708e\u4e0a \u708e\u4e0a\u4e2d\u306f\u5927\u30c0\u30e1";
            case LANCER -> "\u523a\u7a81 - 4.5m\u30ea\u30fc\u30c1/7\u30c0\u30e1/\u76fe3\u8cab\u901a 3tick\u9045\u5ef6";
            case JESTER -> "\u9053\u5316\u306e\u75be\u8d70 - 8\u79d2Speed II";
            case VAMPIRE -> "\u30d6\u30e9\u30c3\u30c9\u30e2\u30fc\u30c9+\u30c6\u30aa\u30b9\u30d1\u30fc\u30c0 - \u5438\u8840\u5909\u8eab+\u30ec\u30fc\u30b6\u30fc";
            case BOMBER -> "\u5730\u96f7\u8a2d\u7f6e - \u4e0d\u53ef\u8996\u5730\u96f7 \u4efb\u610f\u8d77\u7206 \u7206\u767a10f";
            case COOK -> "\u8abf\u7406 - \u98df\u6750\u7372\u5f97+\u8abf\u7406\u3067\u30d0\u30d5/\u6599\u7406\u6295\u64f2\u3067\u30c7\u30d0\u30d5";
            case SCOUT -> "\u30ea\u30b3\u30f3\u30dc\u30eb\u30c8+\u30d1\u30eb\u30b9\u30dc\u30eb\u30c8 - \u7d22\u6575+\u7bc4\u56f2\u7d99\u7d9a\u30c0\u30e1";
            case WHIRLWIND -> "\u6c17\u6d41\u7832+\u65cb\u98a8\u5f3e - \u62bc\u51fa\u6c17\u6d41+\u8ffd\u5c3e\u6253\u4e0a\u7403";
            case NILGIRITAR -> "\u76fe\u8cab\u901a - \u30af\u30ed\u30b9\u30dc\u30a6\u547d\u4e2d\u5f8c\u8fd1\u63a5\u3067\u76fe\u8cab\u901a\u30c0\u30e1";
            case MISTRAL -> "\u70c8\u98a8\u7832 - \u72ed\u5c04\u7a0b\u30fb\u8d85\u5f37\u529b\u306a\u62bc\u51fa\u6c17\u6d41 5\u79d2";
            case SUPERIOR_MISTRAL -> "\u7a76\u6975\u70c8\u98a8\u7832 - 3x3x3\u98a8\u306e\u7a81\u6c17\u5f3e\u5e55 \u5927\u5e45\u62bc\u51fa\u5f0d\u5f01\u3057\u5f31\u4f53\uff0b\u920d\u8db3 5\u79d2";
            case FLASHER -> "\u30d5\u30e9\u30c3\u30b7\u30e5\u30d0\u30f3 - \u7740\u5f3e\u534a\u5f844m\u76f2\u76ee+\u920d\u8db3";
            case MARKSMAN -> "\u30d8\u30f4\u30a3\u30fc\u30dc\u30eb\u30c8 - \u88ab\u5f3e\u8005HP\u4e0a\u9650-10\u6c38\u7d9a";
            case SUNDANCE -> "\u30ea\u30dc\u30eb\u30d3\u30f3\u30b0 - 8\u767a\u9ad8\u901f\u81ea\u52d5\u88c5\u586b 18sCD";
            case ROCKETER -> "\u30e1\u30ac\u30ed\u30b1\u30c3\u30c8+\u30de\u30a4\u30af\u30ed - \u5927\u7206\u767a+\u5c0f\u30ed\u30b1\u9023\u5c04";
            case RELEASER -> "\u30d0\u30fc\u30b9\u30c8\u7279\u5316 - \u8d85\u7206\u767a(1\u56de)+\u5c0f\u7206\u767a(CT\u5236)";
            case ALCHEMIST -> "\u518d\u8abf\u5408 - \u5168\u30dd\u30fc\u30b7\u30e7\u30f3\u88dc\u5145 10sCD";
            case ENGINEER -> "\u30ec\u30fc\u30b6\u30fc\u30bf\u30ec\u30c3\u30c8 - 90\u79d2\u81ea\u52d5\u653b\u6483";
            case TRAPPER -> "\u30c7\u30b9\u30c8\u30e9\u30c3\u30d7 - \u4e0d\u53ef\u8996\u7f60x2 \u7206\u767a+\u76f2\u76ee+\u920d\u8db3+\u5f31\u4f53";
            case GUARDIAN -> "\u9244\u58c1 - 7\u79d2\u5b8c\u5168\u7121\u6575+\u6e1b\u901f 30sCD";
            case MEDIC -> "\u30d5\u30a3\u30fc\u30eb\u30c9\u30b1\u30a2 - \u534a\u5f848m\u5473\u65b9HP+5+\u518d\u751fIII";
            case SUPPORTER -> "\u518d\u8abf\u9054 - \u5168\u30d0\u30d5\u30dd\u30fc\u30b7\u30e7\u30f3\u88dc\u5145 10sCD";
            case RESTRICTIONER -> "\u30c7\u30c3\u30c9\u30ed\u30c3\u30af - 5m\u5c04\u7a0b \u76f8\u4e92\u62d8\u675f5\u79d2";
            case TRANSPORTER -> "\u30ef\u30fc\u30d7\u30b2\u30fc\u30c8 - 2\u70b9\u9593\u30dd\u30fc\u30bf\u30eb\u8a2d\u7f6e";
            case KREUTZ -> "\u9b54\u6cd5\u30ab\u30fc\u30c9 - \u53f3\u30c9\u30ed\u30fc/\u5de6\u8a60\u5531 15\u7a2e";
            case SWAPPER -> "\u77ac\u9593\u4ea4\u5dee - 10m\u5c04\u7a0b \u76f8\u624b\u3068\u4f4d\u7f6e\u5165\u66ff";
            case STICKER -> "\u30b0\u30e9\u30c3\u30d7\u30eb - \u7403\u6295\u5c04 \u547d\u4e2d\u3067\u5f15\u5bc4/\u63a5\u8fd1";
            case DECOY -> "\u30c7\u30b3\u30a4\u5c55\u958b - \u5206\u8eab\u751f\u6210+\u900f\u660e\u53163\u79d2";
            case PHANTOM -> "\u970a\u4f53\u5316 - 6\u79d2\u900f\u660e+\u7121\u6575 \u88ab\u5f3e\u3067\u89e3\u9664";
            case ANCHOR -> "\u78c1\u5834\u5c55\u958b - \u534a\u5f845m\u6575\u5927\u5e45\u6e1b\u901f 15\u79d2";
            case GRANG -> "\u6a5f\u52d5\u7a81\u6483 - \u76fe\u30b7\u30d5\u30c8\u3067\u30c1\u30e3\u30fc\u30b8(\u6700\u592710\u79d2) \u96e2\u3059\u3068\u7a81\u9032";
            case NECRO -> "\u5c4d\u4f53\u821e\u8e0f - \u30b9\u30b1\u30eb\u30c8\u30f33\u4f53\u53ec\u559a \u53f3\u30af\u30ea\u3067\u79fb\u52d5 \u30b7\u30d5\u30c8\u53f3\u30af\u30ea\u3067\u547c\u623b";
            case BULWARK -> "\u30a6\u30a9\u30fc\u30eb\u5c55\u958b - \u58c1\u3092\u5c55\u958b\u3057\u5473\u65b9\u3092\u5b88\u308b\u9632\u885b\u30b9\u30ad\u30eb";
            case TIMEKEEPER -> "\u30ea\u30ef\u30a4\u30f3\u30c9+\u30af\u30ed\u30c3\u30af\u30b9\u30c8\u30c3\u30d7 - \u6642\u3092\u623b\u3057\u5468\u56f2\u306e\u6642\u9593\u3092\u6b62\u3081\u308b";
            case AEGIS -> "\u30ac\u30fc\u30c7\u30a3\u30a2\u30f3\u30dc\u30f3\u30c9 - \u5473\u65b9\u3068\u7d42\u3092\u7d50\u3073\u30c0\u30e1\u30fc\u30b8\u3092\u80a9\u4ee3\u308f\u308a";
            case HEXER -> "\u546a\u7e1b\u9818\u57df - \u9818\u57df\u5185\u306e\u6575\u3092\u62d8\u675f\u3057\u5f31\u4f53\u5316";
            case REFLECTOR -> "\u30df\u30e9\u30fc\u30b9\u30bf\u30f3\u30b9 - \u653b\u6483\u3092\u53cd\u5c04\u3059\u308b\u30b9\u30bf\u30f3\u30b9";
            case GLACIES -> "\u30d5\u30ed\u30b9\u30c8\u30b9\u30c8\u30e9\u30a4\u30af - \u6575\u3092\u51cd\u7d50\u3055\u305b\u308b\u6c37\u6483";
            default -> throw new IncompatibleClassChangeError();
        };
    }

    private static String getGearSummary(KitType kit) {
        return switch (kit) {
            case BLADE, BREAKER, COUNTER, PYRO, BOMBER, COOK, NILGIRITAR, RELEASER, FLASHER, ROCKETER, ALCHEMIST, TRAPPER, GUARDIAN, MEDIC, SWAPPER, STICKER, ANCHOR -> "\u9244\u88c5\u5099";
            case NINJA, LANCER, WHIRLWIND, MISTRAL, SCOUT, ENGINEER, RESTRICTIONER, TRANSPORTER, DECOY, PHANTOM -> "\u9396\u88c5\u5099";
            case SUPERIOR_MISTRAL -> "\u30c0\u30a4\u30e4\u88c5\u5099\uff0b\u76fe";
            case BERSERKER, SNIPER, JESTER, MARKSMAN, SUNDANCE, KREUTZ, NECRO -> "\u76ae\u88c5\u5099";
            case GRANG -> "\u76ae\u88c5\u5099\uff0b\u76fe";
            case VAMPIRE -> "\u9244\u88c5\u5099(HP\u5909\u52d5)";
            case SUPPORTER -> "\u9244\u88c5\u5099";
            case BULWARK, AEGIS -> "\u9244\u88c5\u5099\uff0b\u76fe";
            case TIMEKEEPER, HEXER, GLACIES -> "\u76ae\u88c5\u5099";
            case REFLECTOR -> "\u9396\u88c5\u5099\uff0b\u76fe";
            default -> throw new IncompatibleClassChangeError();
        };
    }
}

