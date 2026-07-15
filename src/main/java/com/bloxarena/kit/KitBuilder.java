package com.bloxarena.kit;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.TeamColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.*;

import java.util.*;

public class KitBuilder {

    public static final Material BURST_MATERIAL = Material.HEART_OF_THE_SEA;

    public static List<ItemStack> getDefaultItems(KitType kit, TeamColor team) {
        List<ItemStack> list = new ArrayList<>();
        ItemStack pick = new ItemStack(Material.WOODEN_PICKAXE);
        addCanDestroy(pick);
        list.add(pick);
        list.add(new ItemStack(Material.BREAD, 8));
        ItemStack concrete = new ItemStack(team.getConcrete(), 32);
        addCanPlaceOn(concrete);
        list.add(concrete);

        switch (kit) {
            case BLADE -> {
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                list.add(sword);
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case BREAKER -> {
                ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
                axe.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(axe);
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case NINJA -> {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                list.add(sword);
                list.add(new ItemStack(Material.ENDER_PEARL, 4));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case BERSERKER -> {
                ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
                axe.addEnchantment(Enchantment.DAMAGE_ALL, 5);
                list.add(axe);
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case SNIPER -> {
                ItemStack xbow = new ItemStack(Material.CROSSBOW);
                xbow.addEnchantment(Enchantment.PIERCING, 2);
                xbow.addEnchantment(Enchantment.QUICK_CHARGE, 1);
                list.add(xbow);
                list.add(new ItemStack(Material.ARROW, 8));
                list.add(new ItemStack(Material.WOODEN_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case COUNTER -> {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                list.add(sword);
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case PYRO -> {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addEnchantment(Enchantment.FIRE_ASPECT, 1);
                list.add(sword);
                ItemStack bow = new ItemStack(Material.BOW);
                bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
                list.add(bow);
                list.add(new ItemStack(Material.ARROW, 16));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case JESTER -> {
                ItemStack axe = new ItemStack(Material.STONE_AXE);
                axe.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                axe.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(axe);
                list.add(new ItemStack(Material.ENDER_PEARL, 3));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case VAMPIRE -> {
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                sword.addEnchantment(Enchantment.KNOCKBACK, 1);
                list.add(sword);
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case BOMBER -> {
                list.add(new ItemStack(Material.IRON_SWORD));
                ItemStack bp = new ItemStack(Material.STONE_PICKAXE);
                addCanDestroy(bp);
                list.add(bp);
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case COOK -> {
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.COOKED_BEEF));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case WHIRLWIND -> {
                ItemStack wws = new ItemStack(Material.IRON_SWORD);
                wws.addEnchantment(Enchantment.KNOCKBACK, 1);
                list.add(wws);
                list.add(new ItemStack(Material.BOW));
                list.add(new ItemStack(Material.ARROW, 16));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case RELEASER -> {
                ItemStack rs = new ItemStack(Material.IRON_SWORD);
                rs.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(rs);
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case SCOUT -> {
                list.add(new ItemStack(Material.BOW));
                list.add(new ItemStack(Material.SPECTRAL_ARROW, 24));
                list.add(new ItemStack(Material.ARROW, 8));
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case FLASHER -> {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                sword.addEnchantment(Enchantment.KNOCKBACK, 1);
                list.add(sword);
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case ROCKETER -> {
                ItemStack crossbow = new ItemStack(Material.CROSSBOW);
                crossbow.addEnchantment(Enchantment.QUICK_CHARGE, 1);
                list.add(crossbow);
                list.add(new ItemStack(Material.ARROW, 16));
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case ALCHEMIST -> {
                list.add(makeSplash(PotionType.SLOWNESS));
                list.add(makeSplash(PotionType.INSTANT_DAMAGE));
                list.add(makeSplash(PotionType.POISON));
                list.add(makeSplash(PotionType.WEAKNESS));
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case ENGINEER -> {
                ItemStack ip = new ItemStack(Material.IRON_PICKAXE);
                ip.addEnchantment(Enchantment.DIG_SPEED, 2);
                addCanDestroy(ip);
                list.add(ip);
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case TRAPPER -> {
                ItemStack tSword = new ItemStack(Material.IRON_SWORD);
                tSword.addEnchantment(Enchantment.KNOCKBACK, 1);
                tSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(tSword);
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.ENDER_PEARL));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case GUARDIAN -> {
                ItemStack sword = new ItemStack(Material.STONE_SWORD);
                sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 4);
                list.add(sword);
                list.add(new ItemStack(Material.SHIELD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case MEDIC -> {
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case SUPPORTER -> {
                list.add(makeSupportPotion(PotionEffectType.SPEED,             1, 600, Color.fromRGB(124,175,198), "§bスプラッシュ §fスピード"));
                list.add(makeSupportPotion(PotionEffectType.DAMAGE_RESISTANCE, 0, 400, Color.fromRGB( 75, 75, 75), "§7スプラッシュ §f耐性"));
                list.add(makeSupportPotion(PotionEffectType.REGENERATION,      1, 200, Color.fromRGB(255,153,204), "§dスプラッシュ §f再生"));
                list.add(makeSupportPotion(PotionEffectType.INCREASE_DAMAGE,   0, 300, Color.fromRGB(147, 38,  8), "§cスプラッシュ §f力"));
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case MARKSMAN -> {
                ItemStack bow = new ItemStack(Material.BOW);
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, 2);
                bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
                list.add(bow);
                list.add(new ItemStack(Material.ARROW, 12));
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case SUNDANCE -> {
                list.add(new ItemStack(Material.CROSSBOW));
                list.add(new ItemStack(Material.ARROW, 16));
                list.add(new ItemStack(Material.WOODEN_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case RESTRICTIONER -> {
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case TRANSPORTER -> {
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case MIMIC -> {
                list.add(new ItemStack(Material.STONE_SWORD));
                ItemStack mp = new ItemStack(Material.IRON_PICKAXE);
                addCanDestroy(mp);
                list.add(mp);
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case SWAPPER -> {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                list.add(sword);
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case STICKER -> {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);
                sword.addEnchantment(Enchantment.KNOCKBACK, 1);
                list.add(sword);
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case DECOY -> {
                list.add(new ItemStack(Material.IRON_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case PHANTOM -> {
                list.add(new ItemStack(Material.STONE_SWORD));
                list.add(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case ANCHOR -> {
                ItemStack sword = new ItemStack(Material.STONE_SWORD);
                sword.addEnchantment(Enchantment.KNOCKBACK, 2);
                list.add(sword);
                list.add(new ItemStack(Material.NETHER_STAR));
            }
            case GRANG -> {
                list.add(new ItemStack(Material.WOODEN_SWORD));
                list.add(new ItemStack(Material.SHIELD));
            }
            case NECRO -> {
                list.add(new ItemStack(Material.WOODEN_SWORD));
                list.add(new ItemStack(Material.NETHER_STAR));
            }
        }
        while (list.size() < 36) list.add(null);
        return list;
    }

    public static void giveKit(Player player, KitType kit, TeamColor team) {
        giveKit(player, kit, team, null);
    }

    public static void giveKit(Player player, KitType kit, TeamColor team, BloxArenaPlugin plugin) {
        player.getInventory().clear();
        giveBaseItems(player, team);
        switch (kit) {
            case BLADE         -> giveBlade(player, plugin);
            case BREAKER       -> giveBreaker(player, plugin);
            case NINJA         -> giveNinja(player, plugin);
            case BERSERKER     -> giveBerserker(player, plugin);
            case SNIPER        -> giveSniper(player, plugin);
            case COUNTER       -> giveCounter(player, plugin);
            case PYRO          -> givePyro(player, plugin);
            case JESTER        -> giveJester(player, plugin);
            case VAMPIRE       -> giveVampire(player, plugin);
            case BOMBER        -> giveBomber(player, plugin);
            case COOK          -> giveCook(player, plugin);
            case SCOUT         -> giveScout(player, plugin);
            case WHIRLWIND     -> giveWhirlwind(player, plugin);
            case FLASHER       -> giveFlasher(player, plugin);
            case ROCKETER      -> giveRocketer(player, plugin);
            case ALCHEMIST     -> giveAlchemist(player, plugin);
            case ENGINEER      -> giveEngineer(player, plugin);
            case TRAPPER       -> giveTrapper(player, plugin);
            case GUARDIAN      -> giveGuardian(player, plugin);
            case MEDIC         -> giveMedic(player, plugin);
            case SUPPORTER     -> giveSupporter(player, plugin);
            case RELEASER      -> giveReleaser(player, plugin);
            case MARKSMAN      -> giveMarksman(player, plugin);
            case SUNDANCE      -> giveSundance(player, plugin);
            case RESTRICTIONER -> giveRestrictioner(player, plugin);
            case TRANSPORTER   -> giveTransporter(player, plugin);
            case MIMIC         -> giveMimic(player, plugin);
            case SWAPPER       -> giveSwapper(player, plugin);
            case STICKER       -> giveSticker(player, plugin);
            case DECOY         -> giveDecoy(player, plugin);
            case PHANTOM       -> givePhantom(player, plugin);
            case ANCHOR        -> giveAnchor(player, plugin);
            case GRANG         -> giveGrang(player, plugin);
            case NECRO         -> giveNecro(player, plugin);
        }
        // Burst is given by refreshBurst() at game start / respawn — not from kit
    }

    private static void giveBaseItems(Player player, TeamColor team) {
        ItemStack pick = new ItemStack(Material.WOODEN_PICKAXE);
        addCanDestroy(pick);
        player.getInventory().addItem(pick);
        player.getInventory().addItem(new ItemStack(Material.BREAD, 8));
        ItemStack concrete = new ItemStack(team.getConcrete(), 32);
        addCanPlaceOn(concrete);
        player.getInventory().addItem(concrete);
    }

    // ─── Duelist kits ───

    private static void giveBlade(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        ItemMeta sm = sword.getItemMeta();
        if (sm != null) {
            sm.setLore(java.util.List.of("§7§o右クリック: 覇斬 - 周囲打ち上げ＋弱体"));
            sm.getPersistentDataContainer().set(new NamespacedKey(plugin, "kit_skill"), PersistentDataType.STRING, "BLADE");
            sword.setItemMeta(sm);
        }
        player.getInventory().addItem(sword);
        player.getInventory().addItem(new ItemStack(Material.SHIELD));
        giveIronArmor(player);
    }
    private static void giveBreaker(Player player, BloxArenaPlugin plugin) {
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        ItemMeta am = axe.getItemMeta();
        if (am != null) {
            am.setLore(java.util.List.of("§7§o右クリック: 烈突 - 高速突進＋接触ダメージ"));
            am.getPersistentDataContainer().set(new NamespacedKey(plugin, "kit_skill"), PersistentDataType.STRING, "BREAKER");
            axe.setItemMeta(am);
        }
        player.getInventory().addItem(axe);
        giveIronArmor(player);
    }

    private static void giveNinja(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 4));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.NINJA, "§2§l🏷 スキル: 隠形"));
        }
        giveChainArmor(player);
    }

    private static void giveBerserker(Player player, BloxArenaPlugin plugin) {
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addEnchantment(Enchantment.DAMAGE_ALL, 5);
        ItemMeta bm = axe.getItemMeta();
        if (bm != null) {
            bm.setLore(java.util.List.of("§7§o右クリック: 怒涛爆砕 - 前方連続爆発"));
            bm.getPersistentDataContainer().set(new NamespacedKey(plugin, "kit_skill"), PersistentDataType.STRING, "BERSERKER");
            axe.setItemMeta(bm);
        }
        player.getInventory().addItem(axe);
        giveLeatherArmor(player);
    }

    private static void giveSniper(Player player, BloxArenaPlugin plugin) {
        ItemStack xbow = new ItemStack(Material.CROSSBOW);
        xbow.addEnchantment(Enchantment.PIERCING, 2);
        xbow.addEnchantment(Enchantment.QUICK_CHARGE, 1);
        player.getInventory().addItem(xbow);
        player.getInventory().addItem(new ItemStack(Material.ARROW, 8));
        player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
        giveLeatherArmor(player);
    }

    private static void giveCounter(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(new ItemStack(Material.SHIELD));
        giveIronArmor(player);
    }

    private static void givePyro(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.FIRE_ASPECT, 1);
        ItemMeta psm = sword.getItemMeta();
        if (psm != null) { psm.setLore(java.util.List.of("§7§o右クリック: 業炎 - 周囲炎上/炎上中即大ダメ")); sword.setItemMeta(psm); }
        player.getInventory().addItem(sword);
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
        ItemMeta pbm = bow.getItemMeta();
        if (pbm != null) { pbm.setLore(java.util.List.of("§7§o炎の弓 - Flame I")); bow.setItemMeta(pbm); }
        player.getInventory().addItem(bow);
        player.getInventory().addItem(new ItemStack(Material.ARROW, 16));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.PYRO, "§6§l🏷 スキル: 業炎"));
        }
        giveChainArmor(player);
    }

    private static void giveJester(Player player, BloxArenaPlugin plugin) {
        ItemStack axe = new ItemStack(Material.STONE_AXE);
        axe.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        axe.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(axe);
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 3));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.JESTER, "§e§l🏷 スキル: 道化の疾走"));
        }
        giveLeatherArmor(player);
    }

    private static void giveVampire(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        ItemMeta sm = sword.getItemMeta();
        if (sm != null) {
            sm.getPersistentDataContainer().set(new NamespacedKey(plugin, "vampire_skill"), PersistentDataType.STRING, "VAMPIRE");
            sm.setLore(java.util.List.of("§7§o右クリック: テオスパーダ §7(スニークで吸収弾)"));
            sword.setItemMeta(sm);
        }
        player.getInventory().addItem(sword);
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.VAMPIRE, "§4§l🏷 スキル: ブラッドモード"));
        }
        giveIronArmor(player);
        player.setMaxHealth(14.0);
        player.setHealth(14.0);
        if (plugin != null) plugin.getSkillManager().initVampireDebuffs(player);
    }

    private static void giveBomber(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        ItemStack pick = new ItemStack(Material.STONE_PICKAXE);
        addCanDestroy(pick);
        player.getInventory().addItem(pick);
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.BOMBER, "§c§l🏷 スキル: 地雷設置"));
        }
        giveChainArmor(player);
    }

    private static void giveGlider(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(sword);
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        Damageable dmg = (Damageable) elytra.getItemMeta();
        if (dmg != null) {
            dmg.setDamage(Math.max(0, elytra.getType().getMaxDurability() - 60));
            elytra.setItemMeta((ItemMeta) dmg);
        }
        player.getInventory().setChestplate(elytra);
        player.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
    }

    private static void giveGrang(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.WOODEN_SWORD);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(new ItemStack(Material.SHIELD));
        giveLeatherArmor(player);
    }

    // ─── Initiator kits ───

    private static void giveScout(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack(Material.BOW));
        player.getInventory().addItem(new ItemStack(Material.SPECTRAL_ARROW, 24));
        player.getInventory().addItem(new ItemStack(Material.ARROW, 8));
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.SCOUT, "§a§l🏷 スキル: リコンボルト"));
        }
        giveChainArmor(player);
    }

    private static void giveFlasher(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        player.getInventory().addItem(sword);
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.FLASHER, "§b§l🏷 スキル: フラッシュバン"));
        }
        giveIronArmor(player);
    }

    private static void giveMarksman(Player player, BloxArenaPlugin plugin) {
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_DAMAGE, 2);
        bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
        player.getInventory().addItem(bow);
        player.getInventory().addItem(new ItemStack(Material.ARROW, 12));
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.MARKSMAN, "§c§l🏷 スキル: ヘヴィーボルト"));
        }
        giveChainArmor(player);
    }

    private static void giveSundance(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack(Material.CROSSBOW));
        player.getInventory().addItem(new ItemStack(Material.ARROW, 16));
        player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.SUNDANCE, "§b§l🏷 スキル: リボルビング"));
        }
        giveLeatherArmor(player);
    }

    private static void giveSwapper(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(sword);
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.SWAPPER, "§5§l🏷 スキル: 瞬間交差"));
        }
        giveIronArmor(player);
    }

    private static void giveSticker(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        player.getInventory().addItem(sword);
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.STICKER, "§3§l🏷 スキル: グラップル"));
        }
        giveIronArmor(player);
    }

    private static void giveDecoy(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.DECOY, "§8§l🏷 スキル: デコイ展開"));
        }
        giveChainArmor(player);
    }

    // ─── Controller kits ───

    private static void giveRocketer(Player player, BloxArenaPlugin plugin) {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        player.getInventory().addItem(crossbow);
        player.getInventory().addItem(new ItemStack(Material.ARROW, 16));
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.ROCKETER, "§e§l🏷 スキル: メガロケット"));
        }
        giveIronArmor(player);
    }

    private static void giveAlchemist(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(makeSplash(PotionType.SLOWNESS));
        player.getInventory().addItem(makeSplash(PotionType.INSTANT_DAMAGE));
        player.getInventory().addItem(makeSplash(PotionType.POISON));
        player.getInventory().addItem(makeSplash(PotionType.WEAKNESS));
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.ALCHEMIST, "§d§l🏷 スキル: 再調合"));
        }
        giveIronArmor(player);
    }

    private static void giveEngineer(Player player, BloxArenaPlugin plugin) {
        ItemStack ip = new ItemStack(Material.IRON_PICKAXE);
        ip.addEnchantment(Enchantment.DIG_SPEED, 2);
        addCanDestroy(ip);
        player.getInventory().addItem(ip);
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.ENGINEER, "§6§l🏷 スキル: レーザータレット"));
        }
        giveChainArmor(player);
    }

    private static void giveRestrictioner(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.RESTRICTIONER, "§8§l🏷 スキル: デッドロック"));
        }
        giveChainArmor(player);
    }

    private static void giveTransporter(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.TRANSPORTER, "§3§l🏷 スキル: ワープゲート"));
        }
        giveChainArmor(player);
    }

    private static void giveMimic(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        ItemStack ip2 = new ItemStack(Material.IRON_PICKAXE);
        addCanDestroy(ip2);
        player.getInventory().addItem(ip2);
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.MIMIC, "§5§l🏷 スキル: スキルコピー"));
        }
        giveChainArmor(player);
    }

    private static void giveNecro(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.WOODEN_SWORD);
        player.getInventory().addItem(sword);
        giveLeatherArmor(player);
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.NECRO, "§8§l🏷 スキル: 屍体舞踏"));
        }
    }

    private static void giveCook(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        player.getInventory().addItem(sword);
        // Random food items (3-5 types)
        Material[] foods = {Material.COOKED_BEEF, Material.COOKED_CHICKEN, Material.COOKED_PORKCHOP,
                Material.COOKED_MUTTON, Material.COOKED_RABBIT, Material.COOKED_COD, Material.COOKED_SALMON,
                Material.BAKED_POTATO, Material.BREAD, Material.PUMPKIN_PIE, Material.GOLDEN_CARROT};
        java.util.Random rnd = new java.util.Random();
        int count = 3 + rnd.nextInt(3);
        for (int i = 0; i < count; i++) {
            player.getInventory().addItem(new ItemStack(foods[rnd.nextInt(foods.length)]));
        }
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.COOK, "§6§l🏷 スキル: 調理"));
        }
        giveIronArmor(player);
    }

    private static void giveWhirlwind(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(new ItemStack(Material.BOW));
        player.getInventory().addItem(new ItemStack(Material.ARROW, 16));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.WHIRLWIND, "§f§l🏷 スキル: 気流砲+旋風弾"));
        }
        giveChainArmor(player);
    }

    private static void giveReleaser(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(new ItemStack(Material.SHIELD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.RELEASER, "§e§l🏷 スキル: 解放(リリース)"));
        }
        giveIronArmor(player);
    }

    // ─── Sentinel kits ───

    private static void giveTrapper(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(new ItemStack(Material.SHIELD));
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.TRAPPER, "§3§l🏷 スキル: デストラップ"));
        }
        giveIronArmor(player);
    }

    private static void giveGuardian(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 4);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(new ItemStack(Material.SHIELD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.GUARDIAN, "§f§l🏷 スキル: 鉄壁"));
        }
        giveIronArmor(player);
    }

    private static void giveMedic(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.MEDIC, "§5§l🏷 スキル: フィールドケア"));
        }
        giveIronArmor(player);
    }

    private static void giveSupporter(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(makeSupportPotion(PotionEffectType.SPEED,             1, 600, Color.fromRGB(124,175,198), "§bスプラッシュ §fスピード"));
        player.getInventory().addItem(makeSupportPotion(PotionEffectType.DAMAGE_RESISTANCE, 0, 400, Color.fromRGB( 75, 75, 75), "§7スプラッシュ §f耐性"));
        player.getInventory().addItem(makeSupportPotion(PotionEffectType.REGENERATION,      1, 200, Color.fromRGB(255,153,204), "§dスプラッシュ §f再生"));
        player.getInventory().addItem(makeSupportPotion(PotionEffectType.INCREASE_DAMAGE,   0, 300, Color.fromRGB(147, 38,  8), "§cスプラッシュ §f力"));
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.SUPPORTER, "§a§l🏷 スキル: 再調達"));
        }
        giveIronArmor(player);
    }



    private static void givePhantom(Player player, BloxArenaPlugin plugin) {
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.PHANTOM, "§7§l🏷 スキル: 霊体化"));
        }
        giveChainArmor(player);
    }

    private static void giveAnchor(Player player, BloxArenaPlugin plugin) {
        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 2);
        player.getInventory().addItem(sword);
        if (plugin != null) {
            player.getInventory().addItem(makeSkillItem(plugin, KitType.ANCHOR, "§9§l🏷 スキル: 磁場展開"));
        }
        giveIronArmor(player);
    }

    // ─── Armor helpers ───

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

    private static void giveLeatherArmor(Player p) {
        p.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        p.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        p.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        p.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
    }

    // ─── Potion helpers ───

    @SuppressWarnings("deprecation")
    private static ItemStack makePotion(PotionType type, boolean splash, boolean upgraded) {
        Material mat = splash ? Material.SPLASH_POTION : Material.POTION;
        ItemStack item = new ItemStack(mat);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;
        meta.setBasePotionData(new PotionData(type, false, upgraded));
        String suffix = upgraded ? "II" : "I";
        switch (type) {
            case STRENGTH -> { meta.setColor(Color.fromRGB(147,38,8));   meta.setDisplayName("§c力 " + suffix); }
            case SPEED    -> { meta.setColor(Color.fromRGB(124,175,198)); meta.setDisplayName("§bスピード " + suffix); }
            default       -> {}
        }
        item.setItemMeta(meta);
        return item;
    }

    @SuppressWarnings("deprecation")
    private static ItemStack makeSplash(PotionType type) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;
        try { meta.setBasePotionData(new PotionData(type, false, true)); }
        catch (Exception e) { meta.setBasePotionData(new PotionData(type, false, false)); }
        switch (type) {
            case SLOWNESS       -> { meta.setColor(Color.fromRGB( 74, 90, 91)); meta.setDisplayName("§7スプラッシュ 鈍化II"); }
            case INSTANT_DAMAGE -> { meta.setColor(Color.fromRGB(255,  0,  0)); meta.setDisplayName("§cスプラッシュ 即時ダメージ"); }
            case POISON         -> { meta.setColor(Color.fromRGB( 78,147, 49)); meta.setDisplayName("§2スプラッシュ 毒"); }
            case WEAKNESS       -> { meta.setColor(Color.fromRGB( 72, 72, 72)); meta.setDisplayName("§7スプラッシュ 弱体化"); }
            default             -> {}
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeSplashCustom(PotionEffectType type, int amplifier, int duration) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;
        meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeSupportPotion(PotionEffectType type, int amplifier, int duration,
                                               Color color, String displayName) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;
        meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        meta.setColor(color);
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeBlindnessSplash() {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;
        meta.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 120, 0), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 120, 1), true);
        meta.setColor(Color.fromRGB(10, 10, 10));
        meta.setDisplayName("§8スプラッシュ §f盲目＋鈍足");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeLingeringCustom(PotionEffectType type, int amplifier, int duration) {
        ItemStack item = new ItemStack(Material.LINGERING_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;
        meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        if (type == PotionEffectType.SLOW)    { meta.setColor(Color.fromRGB( 74, 90, 91)); meta.setDisplayName("§7滞留 鈍化"); }
        if (type == PotionEffectType.POISON)  { meta.setColor(Color.fromRGB( 78,147, 49)); meta.setDisplayName("§2滞留 毒"); }
        if (type == PotionEffectType.WEAKNESS){ meta.setColor(Color.fromRGB( 72, 72, 72)); meta.setDisplayName("§7滞留 弱体化"); }
        if (type == PotionEffectType.HARM)    { meta.setColor(Color.fromRGB(255,  0,  0)); meta.setDisplayName("§c滞留 即時ダメージ"); }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeLingeringBlindness() {
        ItemStack item = new ItemStack(Material.LINGERING_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;
        meta.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0), true);
        meta.setColor(Color.fromRGB(10, 10, 10));
        meta.setDisplayName("§8滞留 盲目");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeFireworkRocket(int count) {
        return new ItemStack(Material.FIREWORK_ROCKET, count);
    }

    private static ItemStack makeExplosiveRocket(int count) {
        ItemStack item = new ItemStack(Material.FIREWORK_ROCKET, count);
        org.bukkit.inventory.meta.FireworkMeta meta =
            (org.bukkit.inventory.meta.FireworkMeta) item.getItemMeta();
        if (meta == null) return item;
        org.bukkit.FireworkEffect effect = org.bukkit.FireworkEffect.builder()
            .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
            .withColor(org.bukkit.Color.RED, org.bukkit.Color.ORANGE)
            .withFade(org.bukkit.Color.YELLOW)
            .trail(true)
            .flicker(false)
            .build();
        meta.addEffect(effect);
        meta.addEffect(effect);
        meta.addEffect(effect);
        meta.setPower(1);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeHealArrow(int count) {
        ItemStack item = new ItemStack(Material.TIPPED_ARROW, count);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;
        meta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 1, 2), true);
        meta.setColor(Color.fromRGB(248, 87, 166));
        meta.setDisplayName("§d回復の矢");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeGlowingSplash(int count) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION, count);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;
        meta.addCustomEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0), true);
        meta.setColor(Color.fromRGB(255, 255, 80));
        meta.setDisplayName("§eスプラッシュ 発光");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeInvisSplash() {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return item;
        meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0), true);
        meta.setColor(Color.fromRGB(200, 200, 200));
        meta.setDisplayName("§fスプラッシュ 透明化");
        item.setItemMeta(meta);
        return item;
    }

    // ─── Burst skill ───

    public static ItemStack makeBurstItem() {
        BloxArenaPlugin plugin = (BloxArenaPlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("BloxArenaII");
        ItemStack item = new ItemStack(BURST_MATERIAL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§l💥 バースト §7(右クリック)");
            meta.setLore(List.of("§7周囲の敵を吹き飛ばし＋ダメージ", "§7自身と敵に短時間弱体化付与", "§c§l1ラウンド1回限り §7使用後消滅"));
            meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "burst_skill"), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isBurstItem(ItemStack item) {
        if (item == null || item.getType() != BURST_MATERIAL || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(
            (BloxArenaPlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("BloxArenaII"),
            "burst_skill");
        return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    // ─── Skill item ───

    private static ItemStack makeSkillItem(BloxArenaPlugin plugin, KitType kit, String displayName) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "kit_skill"), PersistentDataType.STRING, kit.name());
            item.setItemMeta(meta);
        }
        return item;
    }

    // ─── CanDestroy / CanPlaceOn ───

    @SuppressWarnings("deprecation")
    private static void addCanDestroy(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setCanDestroy(Set.of(
            Material.WHITE_CONCRETE, Material.RED_CONCRETE, Material.CYAN_CONCRETE
        ));
        item.setItemMeta(meta);
    }

    @SuppressWarnings("deprecation")
    private static void addCanPlaceOn(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setCanPlaceOn(Set.of(Material.LIME_CONCRETE));
        item.setItemMeta(meta);
    }

    // ─── Refill helpers ───

    public static void refillAlchemistPotions(Player p) {
        ItemStack burst = p.getInventory().getItem(8);
        p.getInventory().setItem(8, null);
        p.getInventory().addItem(makeSplash(PotionType.SLOWNESS));
        p.getInventory().addItem(makeSplash(PotionType.INSTANT_DAMAGE));
        p.getInventory().addItem(makeSplash(PotionType.POISON));
        p.getInventory().addItem(makeSplash(PotionType.WEAKNESS));
        p.getInventory().setItem(8, burst);
    }

    public static void refillSupporterPotions(Player p) {
        ItemStack burst = p.getInventory().getItem(8);
        p.getInventory().setItem(8, null);
        p.getInventory().addItem(makeSupportPotion(PotionEffectType.SPEED,             1, 600, Color.fromRGB(124,175,198), "§bスプラッシュ §fスピード"));
        p.getInventory().addItem(makeSupportPotion(PotionEffectType.DAMAGE_RESISTANCE, 0, 400, Color.fromRGB( 75, 75, 75), "§7スプラッシュ §f耐性"));
        p.getInventory().addItem(makeSupportPotion(PotionEffectType.REGENERATION,      1, 200, Color.fromRGB(255,153,204), "§dスプラッシュ §f再生"));
        p.getInventory().addItem(makeSupportPotion(PotionEffectType.INCREASE_DAMAGE,   0, 300, Color.fromRGB(147, 38,  8), "§cスプラッシュ §f力"));
        p.getInventory().setItem(8, burst);
    }

    // ─── Kit guide book ───

    private static ItemStack makeKitGuide(KitType kit) {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§l📖 " + kit.getName() + " 解説");
            meta.setLore(java.util.List.of(
                "§7" + kit.getDescription(),
                "",
                "§6スキル: §f" + getSkillInfo(kit),
                "§a装備: §f" + getGearSummary(kit),
                "",
                "§c💥 バースト: §f周囲爆発+敵吹飛+弱体化",
                "§7  §o右クリック / 1ラウンド1回",
                "§7  §o使用後消滅",
                "",
                "§7左クリックで詳細を見る"
            ));
            book.setItemMeta(meta);
        }
        return book;
    }

    private static String getSkillInfo(KitType kit) {
        return switch (kit) {
            case BLADE -> "覇斬 - 周囲敵打上げ+ダメ+弱体";
            case BREAKER -> "烈突 - 前方突進+接触ダメ+弱体+吹飛";
            case NINJA -> "隠形 - 8秒透明化+Speed II";
            case BERSERKER -> "怒涛爆砕 - 前方15m連続爆発";
            case SNIPER -> "狙撃眼 - 5秒照準→即死マーク";
            case COUNTER -> "鋼の反射 - 盾構え+スニークでパリィ";
            case PYRO -> "業炎 - 周囲炎上 炎上中は大ダメ";
            case JESTER -> "道化の疾走 - 8秒Speed II";
            case VAMPIRE -> "ブラッドモード+テオスパーダ - 吸血変身+レーザー";
            case BOMBER -> "地雷設置 - 不可視地雷 任意起爆 爆発10f";
            case COOK -> "調理 - 食材獲得+調理でバフ/料理投擲でデバフ";
            case SCOUT -> "リコンボルト+パルスボルト - 索敵+範囲継続ダメ";
            case WHIRLWIND -> "気流砲+旋風弾 - 押出気流+追尾打上球";
            case FLASHER -> "フラッシュバン - 着弾半径4m盲目+鈍足";
            case MARKSMAN -> "ヘヴィーボルト - 被弾者HP上限-10永続";
            case SUNDANCE -> "リボルビング - 8発高速自動装填 18sCD";
            case ROCKETER -> "メガロケット+マイクロ - 大爆発+小ロケ連射";
            case RELEASER -> "バースト特化 - 超爆発(1回)+小爆発(CT制)";
            case ALCHEMIST -> "再調合 - 全ポーション補充 10sCD";
            case ENGINEER -> "レーザータレット - 90秒自動攻撃";
            case TRAPPER -> "デストラップ - 不可視罠x2 爆発+盲目+鈍足+弱体";
            case GUARDIAN -> "鉄壁 - 7秒完全無敵+減速 30sCD";
            case MEDIC -> "フィールドケア - 半径8m味方HP+5+再生III";
            case SUPPORTER -> "再調達 - 全バフポーション補充 10sCD";
            case RESTRICTIONER -> "デッドロック - 5m射程 相互拘束5秒";
            case TRANSPORTER -> "ワープゲート - 2点間ポータル設置";
            case MIMIC -> "スキルコピー - 20m射程 相手に5sCD付与";
            case SWAPPER -> "瞬間交差 - 10m射程 相手と位置入替";
            case STICKER -> "グラップル - 球投射 命中で引寄/接近";
            case DECOY -> "デコイ展開 - 分身生成+透明化3秒";
            case PHANTOM -> "霊体化 - 6秒透明+無敵 被弾で解除";
            case ANCHOR -> "磁場展開 - 半径5m敵大幅減速 15秒";
            case GRANG -> "機動突撃 - 盾シフトでチャージ(最大10秒) 離すと突進";
            case NECRO -> "屍体舞踏 - スケルトン3体召喚 右クリで移動 シフト右クリで呼戻";
        };
    }

    private static String getGearSummary(KitType kit) {
        return switch (kit) {
            case BLADE, BREAKER, COUNTER, PYRO, FLASHER, ROCKETER, ALCHEMIST, TRAPPER, GUARDIAN, MEDIC, BOMBER, SWAPPER, STICKER, ANCHOR, COOK, RELEASER -> "鉄装備";
            case NINJA, SCOUT, ENGINEER, PHANTOM, DECOY, TRANSPORTER, RESTRICTIONER, WHIRLWIND -> "鎖装備";
            case BERSERKER, SNIPER, MARKSMAN, JESTER, SUNDANCE, MIMIC, NECRO -> "皮装備";
            case GRANG -> "皮装備＋盾";
            case VAMPIRE -> "鉄装備(HP変動)";
            case SUPPORTER -> "鉄装備";
        };
    }
}
