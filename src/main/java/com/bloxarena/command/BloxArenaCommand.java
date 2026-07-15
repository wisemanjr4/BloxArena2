package com.bloxarena.command;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameMode;
import com.bloxarena.game.GameState;
import com.bloxarena.map.MapConfig;
import com.bloxarena.util.SelectionTool;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class BloxArenaCommand implements CommandExecutor, TabCompleter {

    private final BloxArenaPlugin plugin;
    private final SetupWizard wizard;
    private final MapWizard mapWizard;

    public BloxArenaCommand(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.wizard = new SetupWizard(plugin);
        this.mapWizard = new MapWizard(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) { sendHelp(sender); return true; }
        String sub = args[0].toLowerCase();
        switch (sub) {

            // ─── 試合制御 ───
            case "start" -> {
                if (!isAdmin(sender)) return true;
                if (plugin.getGameManager().getState() != GameState.WAITING) {
                    sender.sendMessage("§c試合はすでに進行中です。"); return true;
                }
                List<UUID> participants = new ArrayList<>(plugin.getLobbyManager().getWaitingPlayers());
                if (participants.isEmpty()) {
                    sender.sendMessage("§c待機エリアにプレイヤーがいません。"); return true;
                }
                GameMode mode = GameMode.random();
                MapConfig map = plugin.getMapManager().selectMap(mode);
                if (map == null) {
                    sender.sendMessage("§c使用可能なマップがありません。§7(/ba addmap, /ba setspawnzone 等で設定してください)");
                    return true;
                }
                plugin.getGameManager().startGame(map, mode, participants);
                sender.sendMessage("§a試合を強制開始しました。");
            }

            case "stop" -> {
                if (!isAdmin(sender)) return true;
                plugin.getGameManager().forceStop();
                sender.sendMessage("§a試合を強制終了しました。");
            }

            // ─── ワンド ───
            case "wand" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                p.getInventory().addItem(plugin.getSelectionTool().createWand());
                p.sendMessage("§aガラスブロックのワンドを受け取りました。");
                p.sendMessage("§7§l左クリック §r§7= Pos1 (min)  §7§l右クリック §r§7= Pos2 (max)");
            }

            // ─── 待機エリア設定 ───
            case "setwaitingarea" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                SelectionTool tool = plugin.getSelectionTool();
                if (!tool.hasSelection(p)) {
                    sender.sendMessage("§cまずワンドで2点を選択してください。 (/bloxarena wand)"); return true;
                }
                plugin.getLobbyManager().setWaitingAreaMin(tool.getMin(p));
                plugin.getLobbyManager().setWaitingAreaMax(tool.getMax(p));
                sender.sendMessage("§a待機エリアを設定しました。");
                sender.sendMessage("§7min: " + fmt(tool.getMin(p)) + "  max: " + fmt(tool.getMax(p)));
            }

            // ─── ロビースポーン ───
            case "setlobby" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                plugin.getLobbyManager().setLobbySpawn(p.getLocation());
                sender.sendMessage("§aロビースポーンを現在地に設定しました。");
            }

            // ─── マップ追加 ───
            case "addmap" -> {
                if (!isAdmin(sender)) return true;
                // /ba addmap <id> [worldName]
                if (args.length < 2) { sender.sendMessage("§7使用法: /bloxarena addmap <mapId> [worldName]"); return true; }
                String mapId = args[1];
                String worldName = args.length >= 3 ? args[2]
                    : (sender instanceof Player p ? p.getWorld().getName() : "world");
                if (!plugin.getMapManager().addMap(mapId, worldName)) {
                    sender.sendMessage("§cマップ '" + mapId + "' はすでに存在します。"); return true;
                }
                sender.sendMessage("§aマップ §e" + mapId + " §a(ワールド: §e" + worldName + "§a) を追加しました。");
                sender.sendMessage("§7次: /ba setspawnzone red " + mapId + " / /ba setspawnzone blue " + mapId);
                sender.sendMessage("§7    /ba setcenter " + mapId + " / /ba setmaplobby " + mapId);
            }

            // ─── スポーンゾーン設定 ───
            case "setspawnzone" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                if (args.length < 3) { sender.sendMessage("§7使用法: /bloxarena setspawnzone <red|blue> <mapId>"); return true; }
                String teamStr = args[1].toLowerCase();
                MapConfig cfg = plugin.getMapManager().getById(args[2]);
                if (cfg == null) { sender.sendMessage("§cマップ '" + args[2] + "' が見つかりません。"); return true; }
                SelectionTool tool = plugin.getSelectionTool();
                if (!tool.hasSelection(p)) {
                    sender.sendMessage("§cまずワンドで2点を選択してください。 (/bloxarena wand)"); return true;
                }
                if ("red".equals(teamStr)) {
                    cfg.setRedSpawnMin(tool.getMin(p)); cfg.setRedSpawnMax(tool.getMax(p));
                } else if ("blue".equals(teamStr)) {
                    cfg.setBlueSpawnMin(tool.getMin(p)); cfg.setBlueSpawnMax(tool.getMax(p));
                } else {
                    sender.sendMessage("§c red か blue を指定してください。"); return true;
                }
                plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("§a" + teamStr + "スポーンゾーンを設定・保存しました。");
                sender.sendMessage("§7min: " + fmt(tool.getMin(p)) + "  max: " + fmt(tool.getMax(p)));
            }

            // ─── 中央コンクリート基準点 ───
            case "setcenter" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                if (args.length < 2) { sender.sendMessage("§7使用法: /bloxarena setcenter <mapId>"); return true; }
                MapConfig cfg = plugin.getMapManager().getById(args[1]);
                if (cfg == null) { sender.sendMessage("§cマップが見つかりません。"); return true; }
                cfg.setCenter(p.getLocation());
                plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("§a中央基準点を設定・保存しました: " + fmt(p.getLocation()));
            }

            // ─── マップロビー設定 ───
            case "setmaplobby" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                if (args.length < 2) { sender.sendMessage("§7使用法: /bloxarena setmaplobby <mapId>"); return true; }
                MapConfig cfg = plugin.getMapManager().getById(args[1]);
                if (cfg == null) { sender.sendMessage("§cマップが見つかりません。"); return true; }
                cfg.setLobby(p.getLocation());
                plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("§aマップロビー地点を設定・保存しました: " + fmt(p.getLocation()));
            }

            // ─── マップ情報 ───
            case "info" -> {
                if (args.length < 2) {
                    // 全マップ一覧
                    sender.sendMessage("§6§l=== マップ一覧 (" + plugin.getMapManager().getMaps().size() + ") ===");
                    for (MapConfig mc : plugin.getMapManager().getMaps()) {
                        String ready = mc.isReady() ? "§a✔" : "§c✘";
                        String modes = modeFlags(mc);
                        String centerStr = mc.getCenter() != null
                            ? " §7(" + mc.getCenter().getBlockX() + ", " + mc.getCenter().getBlockY() + ", " + mc.getCenter().getBlockZ() + ")"
                            : "";
                        sender.sendMessage(ready + " §e" + mc.getId() + " §7[" + mc.getWorldName() + "]" + centerStr + " " + modes);
                    }
                    sender.sendMessage("§7凡例: §fBA=アリーナ TDM=デスマッチ §cB=爆破解体 §eD=占領 §9CTF=旗取り");
                    return true;
                }
                MapConfig mc = plugin.getMapManager().getById(args[1]);
                if (mc == null) { sender.sendMessage("§cマップ '" + args[1] + "' が見つかりません。"); return true; }
                sender.sendMessage("§6§l=== マップ: " + mc.getId() + " ===");
                sender.sendMessage("§7ワールド: §f" + mc.getWorldName());
                sender.sendMessage("§7準備完了: " + (mc.isReady() ? "§a✔" : "§c✘ (未設定: " + "スポーン等" + ")"));
                sender.sendMessage("§7RedSpawn: §f" + fmtNull(mc.getRedSpawnMin()) + " ~ " + fmtNull(mc.getRedSpawnMax()));
                sender.sendMessage("§7BlueSpawn: §f" + fmtNull(mc.getBlueSpawnMin()) + " ~ " + fmtNull(mc.getBlueSpawnMax()));
                sender.sendMessage("§7Center: §f" + fmtNull(mc.getCenter()));
                sender.sendMessage("§7Lobby: §f" + fmtNull(mc.getLobby()));
                sender.sendMessage("§7Gate: R" + fmtNull(mc.getRedGateMin()) + " / B" + fmtNull(mc.getBlueGateMin()));
                sender.sendMessage("§7OOB: " + fmtNull(mc.getOobMin()) + " ~ " + fmtNull(mc.getOobMax()));
                // CTF
                sender.sendMessage("§9CTF旗:");
                sender.sendMessage("  §c赤旗: §f" + fmtNull(mc.getRedFlagLocation()) + "  §7帰還: §f" + fmtNull(mc.getRedReturnLocation()));
                sender.sendMessage("  §9青旗: §f" + fmtNull(mc.getBlueFlagLocation()) + "  §7帰還: §f" + fmtNull(mc.getBlueReturnLocation()));
                // Bomb
                sender.sendMessage("§c爆破:");
                sender.sendMessage("  §7設置: §f" + fmtNull(mc.getBombSite()) + "  §7解除: §f" + fmtNull(mc.getDefusePoint()));
                sender.sendMessage("§7対応モード: " + modeFlags(mc));
            }

            // ─── 次試合マップ指定 ───
            case "setmap" -> {
                if (!isAdmin(sender)) return true;
                if (args.length < 2) { sender.sendMessage("§7使用法: /bloxarena setmap <mapId>"); return true; }
                if (plugin.getMapManager().getById(args[1]) == null) {
                    sender.sendMessage("§cマップ '" + args[1] + "' が見つかりません。"); return true;
                }
                plugin.getMapManager().setNextMap(args[1]);
                sender.sendMessage("§a次の試合マップを §e" + args[1] + " §aに設定しました（1試合限定）。");
            }

            // ─── マップ表示名設定 ───
            case "setmapname" -> {
                if (!isAdmin(sender)) return true;
                if (args.length < 3) { sender.sendMessage("§7使用法: /ba setmapname <mapId> <表示名>"); return true; }
                MapConfig cfg = plugin.getMapManager().getById(args[1]);
                if (cfg == null) { sender.sendMessage("§cマップ '" + args[1] + "' が見つかりません。"); return true; }
                String name = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                cfg.setDisplayName(name);
                plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("§aマップ §e" + args[1] + " §aの表示名を §e" + name + " §aに設定しました。");
            }

            // ─── キット一覧（非OP対応） ───
            case "kits" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                plugin.getKitInfoGUI().openList(p);
            }

            // ─── 観戦 ───
            case "spectate" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                if (plugin.getGameManager().getState() != GameState.IN_GAME) {
                    sender.sendMessage("§c現在試合は進行中ではありません。"); return true;
                }
                plugin.getGameManager().addSpectator(p);
                sender.sendMessage("§a観戦モードへ移行しました。");
            }

            // ─── リロード ───
            case "reload" -> {
                if (!isAdmin(sender)) return true;
                plugin.reloadConfig();
                plugin.getLobbyManager().reload();
                plugin.getMapManager().reload();
                sender.sendMessage("§aconfig.yml をリロードしました。");
            }

            // ─── ステータス確認 ───
            case "status" -> {
                sender.sendMessage("§6§l=== BAII WoNG Status ===");
                sender.sendMessage("§7状態: §f" + plugin.getGameManager().getState());
                sender.sendMessage("§7待機人数: §f" + plugin.getLobbyManager().getWaitingPlayers().size());
                sender.sendMessage("§7マップ数: §f" + plugin.getMapManager().getMaps().size()
                    + " §7(準備完了: §a"
                    + plugin.getMapManager().getMaps().stream().filter(MapConfig::isReady).count() + "§7)");
                sender.sendMessage("§7赤チーム: §f" + plugin.getGameManager().getRedTeam().size() + "人");
                sender.sendMessage("§7青チーム: §f" + plugin.getGameManager().getBlueTeam().size() + "人");
            }



            // ─── キットエディタ ───
            case "kitedit" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                plugin.getKitEditorGUI().openList(p);
            }


            // ─── プレイヤー統計 ───
            case "stats" -> {
                org.bukkit.OfflinePlayer target;
                if (args.length >= 2) {
                    target = org.bukkit.Bukkit.getOfflinePlayerIfCached(args[1]);
                    if (target == null) { sender.sendMessage("§cプレイヤーが見つかりません: " + args[1]); return true; }
                } else if (sender instanceof Player p) {
                    target = p;
                } else { sender.sendMessage("§c使用法: /ba stats <name>"); return true; }

                var sm = plugin.getStatsManager();
                var s  = sm.getStats(target.getUniqueId());
                sender.sendMessage("§6§l=== " + target.getName() + " の統計 ===");
                sender.sendMessage("§7Kill: §f" + s.kills + "  Death: §f" + s.deaths
                    + "  §7K/D: §f" + String.format("%.2f", s.getKD()));
                sender.sendMessage("§7勝利: §f" + s.wins + "  敗北: §f" + s.losses
                    + "  §7勝率: §f" + String.format("%.1f", s.getWinRate()) + "%");
                sender.sendMessage("§7総ダメージ: §f" + String.format("%.1f", s.damage));
            }

            // ─── ランキング ───
            case "top" -> {
                String field = args.length >= 2 ? args[1].toLowerCase() : "kills";
                if (!java.util.List.of("kills","wins","kd","damage").contains(field)) {
                    sender.sendMessage("§7使用法: /ba top [kills|wins|kd|damage]"); return true;
                }
                var sm  = plugin.getStatsManager();
                var top = sm.getTop(field, 10);
                sender.sendMessage("§6§l=== Top 10: " + field + " ===");
                int rank = 1;
                for (var entry : top) {
                    var s = entry.getValue();
                    String name = sm.getName(entry.getKey());
                    String val = switch (field) {
                        case "wins"   -> s.wins + " wins";
                        case "kd"     -> String.format("%.2f K/D", s.getKD());
                        case "damage" -> String.format("%.0f dmg", s.damage);
                        default       -> s.kills + " kills";
                    };
                    sender.sendMessage("§7#" + rank++ + " §e" + name + " §f" + val);
                }
            }

            // ─── 連続試合モード ───
            case "continuous" -> {
                if (!isAdmin(sender)) return true;
                if (args.length < 2) {
                    boolean cur = plugin.getLobbyManager().isContinuousMode();
                    sender.sendMessage("§7連続試合モード: " + (cur ? "§aON" : "§cOFF"));
                    sender.sendMessage("§7切り替え: §f/ba continuous <on|off>");
                    return true;
                }
                boolean enable = "on".equalsIgnoreCase(args[1]);
                plugin.getLobbyManager().setContinuousMode(enable);
                sender.sendMessage("§a連続試合モードを " + (enable ? "§aON" : "§cOFF") + " §aにしました。");
            }

            // ─── セットアップウィザード ───
            case "admin" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                if (args.length < 2) {
                    sender.sendMessage("§7使用法: /ba admin <imigration|addmap|next|cancel|skip>");
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "imigration" -> wizard.start(p);
                    case "addmap" -> {
                        if (args.length < 3) { sender.sendMessage("§7使用法: /ba admin addmap <mapId>"); }
                        else mapWizard.start(p, args[2].toLowerCase());
                    }
                    case "next" -> {
                        if (mapWizard.isActive(p)) mapWizard.next(p);
                        else wizard.next(p);
                    }
                    case "skip" -> {
                        if (mapWizard.isActive(p)) mapWizard.skip(p);
                        else wizard.skip(p);
                    }
                    case "cancel" -> {
                        if (mapWizard.isActive(p)) mapWizard.cancel(p);
                        else wizard.cancel(p);
                    }
                    default -> sender.sendMessage("§7使用法: /ba admin <imigration|addmap|next|cancel|skip>");
                }
            }

            // ─── ゲート設定 ───
            case "setgate" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                if (args.length < 3) { sender.sendMessage("§7使用法: /ba setgate <red|blue> <mapId>"); return true; }
                String side = args[1].toLowerCase();
                MapConfig cfg = plugin.getMapManager().getById(args[2]);
                if (cfg == null) { sender.sendMessage("§cマップ '" + args[2] + "' が見つかりません。"); return true; }
                SelectionTool tool = plugin.getSelectionTool();
                if (!tool.hasSelection(p)) { sender.sendMessage("§cワンドで2点を選択してください。"); return true; }
                if ("red".equals(side)) {
                    cfg.setRedGateMin(tool.getMin(p)); cfg.setRedGateMax(tool.getMax(p));
                } else if ("blue".equals(side)) {
                    cfg.setBlueGateMin(tool.getMin(p)); cfg.setBlueGateMax(tool.getMax(p));
                } else { sender.sendMessage("§c red か blue を指定してください。"); return true; }
                plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("§a" + side + "チームのゲート領域を設定しました。");
            }

            // ─── ゲート素材 ───
            case "gatematl" -> {
                if (!isAdmin(sender)) return true;
                if (args.length < 3) { sender.sendMessage("§7使用法: /ba gatematl <mapId> <素材名>"); return true; }
                MapConfig cfg = plugin.getMapManager().getById(args[1]);
                if (cfg == null) { sender.sendMessage("§cマップ '" + args[1] + "' が見つかりません。"); return true; }
                org.bukkit.Material mat;
                try { mat = org.bukkit.Material.valueOf(args[2].toUpperCase()); }
                catch (IllegalArgumentException e) { sender.sendMessage("§c不明な素材: " + args[2]); return true; }
                cfg.setGateMaterial(mat);
                plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("§aゲートブロックを §e" + mat.name() + " §aに設定しました。");
            }

            // ─── OOBゾーン設定 ───
            case "setoob" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                if (args.length < 2) { sender.sendMessage("§7使用法: /ba setoob <mapId|lobby>"); return true; }
                SelectionTool tool = plugin.getSelectionTool();
                if (!tool.hasSelection(p)) { sender.sendMessage("§cワンドで2点を選択してください。"); return true; }
                if ("lobby".equalsIgnoreCase(args[1])) {
                    plugin.getLobbyManager().setLobbyOob(tool.getMin(p), tool.getMax(p));
                    sender.sendMessage("§aロビーOOBゾーンを設定しました。");
                } else {
                    MapConfig cfg = plugin.getMapManager().getById(args[1]);
                    if (cfg == null) { sender.sendMessage("§cマップ '" + args[1] + "' が見つかりません。"); return true; }
                    cfg.setOobMin(tool.getMin(p)); cfg.setOobMax(tool.getMax(p));
                    plugin.getMapManager().saveMap(cfg);
                    sender.sendMessage("§aマップ §e" + args[1] + " §aのOOBゾーンを設定しました。");
                }
            }

            // ─── CTF旗地点設定 ───
            case "setredflag", "setblueflag", "setredreturn", "setbluereturn" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                if (args.length < 2) {
                    sender.sendMessage("§7使用法: /ba " + sub + " <mapId>");
                    sender.sendMessage("§7  setredflag    → 相手陣地の赤旗初期位置を現在地に設定");
                    sender.sendMessage("§7  setblueflag   → 相手陣地の青旗初期位置を現在地に設定");
                    sender.sendMessage("§7  setredreturn  → 赤チームの旗持ち帰り地点を現在地に設定");
                    sender.sendMessage("§7  setbluereturn → 青チームの旗持ち帰り地点を現在地に設定");
                    return true;
                }
                MapConfig fc = plugin.getMapManager().getById(args[1].toLowerCase());
                if (fc == null) { sender.sendMessage("§cマップ '" + args[1] + "' が見つかりません。"); return true; }
                Location loc = p.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                switch (sub) {
                    case "setredflag" -> { fc.setRedFlagLocation(loc); sender.sendMessage("§a✔ 赤旗地点を設定しました（相手陣地に設置してください）。"); }
                    case "setblueflag" -> { fc.setBlueFlagLocation(loc); sender.sendMessage("§a✔ 青旗地点を設定しました（相手陣地に設置してください）。"); }
                    case "setredreturn" -> { fc.setRedReturnLocation(loc); sender.sendMessage("§a✔ 赤持ち帰り地点を設定しました。"); }
                    case "setbluereturn" -> { fc.setBlueReturnLocation(loc); sender.sendMessage("§a✔ 青持ち帰り地点を設定しました。"); }
                }
                plugin.getMapManager().saveMap(fc);
            }
            // ─── 爆破地点設定 ───
            case "setbombplant", "setbombdefuse" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                if (args.length < 2) {
                    sender.sendMessage("§7使用法: /ba " + sub + " <mapId>");
                    sender.sendMessage("§7  setbombplant → 爆弾設置地点を現在地に設定");
                    sender.sendMessage("§7  setbombdefuse→ 爆弾解除地点を現在地に設定");
                    return true;
                }
                MapConfig mc2 = plugin.getMapManager().getById(args[1].toLowerCase());
                if (mc2 == null) { sender.sendMessage("§cマップ '" + args[1] + "' が見つかりません。"); return true; }
                Location bl = p.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                switch (sub) {
                    case "setbombplant" -> { mc2.setBombSite(bl); sender.sendMessage("§a✔ 爆弾設置地点を設定しました。"); }
                    case "setbombdefuse" -> { mc2.setDefusePoint(bl); sender.sendMessage("§a✔ 爆弾解除地点を設定しました。"); }
                }
                plugin.getMapManager().saveMap(mc2);
            }

            // ─── BOT管理 ───
            case "bot" -> {
                if (!isAdmin(sender)) return true;
                if (args.length < 2) { sender.sendMessage("§7使用法: /ba bot <add [n]|clear|list>"); return true; }
                switch (args[1].toLowerCase()) {
                    case "add" -> {
                        int n = args.length >= 3 ? parseInt(args[2], 1) : 1;
                        plugin.getBotManager().addPendingBots(n);
                        sender.sendMessage("§aBOTを §e" + n + "体 §a追加予約しました（試合開始時にスポーン）。");
                    }
                    case "clear" -> {
                        plugin.getBotManager().clearAll();
                        sender.sendMessage("§aBOTをすべて削除しました。");
                    }
                    case "list" -> {
                        int cnt = plugin.getBotManager().getTotalBotCount();
                        sender.sendMessage("§7現在のBOT数: §f" + cnt + "体");
                    }
                    default -> sender.sendMessage("§7使用法: /ba bot <add [n]|clear|list>");
                }
            }

            case "convert" -> {
                if (!isAdmin(sender)) return true;
                org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
                int merged = 0, added = 0;

                // 旧BloxArenaのconfigを読み込み
                java.io.File oldFile = new java.io.File(plugin.getDataFolder().getParentFile(), "BloxArena/config.yml");
                if (oldFile.exists()) {
                    org.bukkit.configuration.file.YamlConfiguration oldCfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(oldFile);
                    // lobby セクション引き継ぎ
                    if (oldCfg.isConfigurationSection("lobby") && !cfg.isConfigurationSection("lobby")) {
                        cfg.set("lobby", oldCfg.get("lobby"));
                        merged++;
                    }
                    // maps セクション引き継ぎ
                    if (oldCfg.isConfigurationSection("maps") && !cfg.isConfigurationSection("maps")) {
                        cfg.set("maps", oldCfg.get("maps"));
                        merged++;
                    }
                    // 待機エリア/ロビー座標引き継ぎ
                    if (oldCfg.contains("lobby.waiting_area.world") && !cfg.contains("lobby.waiting_area.world")) {
                        cfg.set("lobby.waiting_area", oldCfg.get("lobby.waiting_area"));
                        merged++;
                    }
                    if (oldCfg.contains("lobby.spawn.x") && !cfg.contains("lobby.spawn.x")) {
                        cfg.set("lobby.spawn", oldCfg.get("lobby.spawn"));
                        merged++;
                    }
                    if (oldCfg.contains("lobby.oob.min.x")) {
                        cfg.set("lobby.oob", oldCfg.get("lobby.oob"));
                        merged++;
                    }
                    // マップごとのOOBも引き継ぎ
                    if (oldCfg.isConfigurationSection("maps")) {
                        for (String mapId : oldCfg.getConfigurationSection("maps").getKeys(false)) {
                            if (oldCfg.contains("maps." + mapId + ".oob.min.x")) {
                                cfg.set("maps." + mapId + ".oob", oldCfg.get("maps." + mapId + ".oob"));
                            }
                        }
                    }
                    sender.sendMessage("§7旧BloxArenaのconfigから §e" + merged + "§7セクションを引き継ぎました。");
                }

                // 新規セクション追加
                if (!cfg.isConfigurationSection("game_modes")) {
                    cfg.set("game_modes.enabled", java.util.List.of("BATTLE_ARENA","TEAM_DEATHMATCH","BOMB_MISSION","DOMINATION","CAPTURE_THE_FLAG"));
                    added++;
                }
                if (!cfg.isConfigurationSection("team_deathmatch")) {
                    cfg.set("team_deathmatch.time_limit_seconds", 300);
                    cfg.set("team_deathmatch.target_kills", 30);
                    added++;
                }
                if (!cfg.isConfigurationSection("bomb_mission")) {
                    cfg.set("bomb_mission.time_limit_seconds", 180);
                    cfg.set("bomb_mission.plant_time_seconds", 5);
                    cfg.set("bomb_mission.defuse_time_seconds", 7);
                    cfg.set("bomb_mission.bomb_fuse_seconds", 45);
                    added++;
                }
                if (!cfg.isConfigurationSection("domination")) {
                    cfg.set("domination.time_limit_seconds", 120);
                    cfg.set("domination.target_points", 100);
                    cfg.set("domination.points_per_second", 2);
                    added++;
                }
                if (!cfg.isConfigurationSection("capture_the_flag")) {
                    cfg.set("capture_the_flag.time_limit_seconds", 300);
                    cfg.set("capture_the_flag.captures_to_win", 3);
                    added++;
                }
                plugin.saveConfig();
                sender.sendMessage("§aコンバート完了！§e" + merged + "§a引継 + §e" + added + "§a追加");
                sender.sendMessage("§7/ba reload で再読込してください。");
            }

            case "upgrade" -> {
                if (!isAdmin(sender)) return true;
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                if (args.length < 2) { sender.sendMessage("§7使用法: /ba upgrade <mapId>"); return true; }
                String mapId = args[1];
                MapConfig mc = plugin.getMapManager().getById(mapId);
                if (mc == null) { sender.sendMessage("§cマップ '" + mapId + "' が見つかりません。"); return true; }
                mapWizard.startUpgrade(p, mapId);
                sender.sendMessage("§aマップ §e" + mapId + " §aのアップグレードウィザードを開始しました。");
            }

            case "test" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("§cプレイヤーのみ使用可能です。"); return true; }
                if (args.length >= 2 && "setup".equalsIgnoreCase(args[1])) {
                    if (!isAdmin(sender)) return true;
                    if (args.length >= 3 && args[2].matches("\\d+")) {
                        int count = Integer.parseInt(args[2]);
                        plugin.getConfig().set("test_field.dummy_count", count);
                        plugin.saveConfig();
                        sender.sendMessage("§aダミー数を §e" + count + "§a に設定しました。");
                    }
                    plugin.getConfig().set("test_field.spawn.world", p.getWorld().getName());
                    plugin.getConfig().set("test_field.spawn.x", p.getLocation().getX());
                    plugin.getConfig().set("test_field.spawn.y", p.getLocation().getY());
                    plugin.getConfig().set("test_field.spawn.z", p.getLocation().getZ());
                    plugin.saveConfig();
                    plugin.getTestFieldManager().reload();
                    sender.sendMessage("§aテスト場スポーンを現在地に設定しました！");
                    sender.sendMessage("§7ダミー数: §f" + plugin.getConfig().getInt("test_field.dummy_count", 3) + "体");
                    sender.sendMessage("§7§f/ba test setarea §7で範囲設定（ワンド選択後）");
                    sender.sendMessage("§7§f/ba test §7でテスト場に入れます");
                    return true;
                }
                if (args.length >= 2 && "setarea".equalsIgnoreCase(args[1])) {
                    if (!isAdmin(sender)) return true;
                    SelectionTool tool = plugin.getSelectionTool();
                    if (!tool.hasSelection(p)) {
                        sender.sendMessage("§cまず/ba wandで2点を選択してください。"); return true;
                    }
                    plugin.getTestFieldManager().setArea(tool.getMin(p), tool.getMax(p));
                    sender.sendMessage("§aテスト場の範囲を設定しました！");
                    return true;
                }
                if (!plugin.getTestFieldManager().isActive()) {
                    sender.sendMessage("§cテスト場が設定されていません。 §f/ba test setup [ダミー数] §cで設定してください。"); return true;
                }
                if (args.length >= 2 && "leave".equalsIgnoreCase(args[1])) {
                    plugin.getTestFieldManager().leave(p);
                } else {
                    plugin.getTestFieldManager().enter(p);
                }
            }

            default -> sendHelp(sender);
        }
        return true;
    }



    private String modeFlags(MapConfig mc) {
        return (mc.isReadyFor(GameMode.BATTLE_ARENA)      ? "§fBA " : "§8BA ")
            + (mc.isReadyFor(GameMode.TEAM_DEATHMATCH)    ? "§fTDM " : "§8TDM ")
            + (mc.isReadyFor(GameMode.BOMB_MISSION)       ? "§cB " : "§8B ")
            + (mc.isReadyFor(GameMode.DOMINATION)         ? "§eD " : "§8D ")
            + (mc.isReadyFor(GameMode.CAPTURE_THE_FLAG)   ? "§9CTF" : "§8CTF");
    }

    private String fmtNull(org.bukkit.Location l) {
        if (l == null) return "§c未設定";
        return String.format("§f(%.1f, %.1f, %.1f)", l.getX(), l.getY(), l.getZ());
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private boolean isAdmin(CommandSender s) {
        if (s.hasPermission("bloxarena.admin")) return true;
        s.sendMessage("§cこのコマンドには bloxarena.admin 権限が必要です。");
        return false;
    }

    private String fmt(org.bukkit.Location l) {
        if (l == null) return "null";
        return String.format("(%.1f, %.1f, %.1f)", l.getX(), l.getY(), l.getZ());
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage("§6§l=== BAII WoNG コマンド ===");
        s.sendMessage("§e/ba wand §7- ワンドを受け取る（2点選択）");
        s.sendMessage("§e/ba setwaitingarea §7- ワンド選択範囲を待機エリアに設定");
        s.sendMessage("§e/ba setlobby §7- ロビースポーンを現在地に設定");
        s.sendMessage("§e/ba addmap <id> [world] §7- 新しいマップを追加");
        s.sendMessage("§e/ba info [mapId] §7- マップ情報・一覧表示");
        s.sendMessage("§e/ba setspawnzone <red|blue> <mapId> §7- ワンド選択範囲をスポーンゾーンに設定");
        s.sendMessage("§e/ba setcenter <mapId> §7- 中央コンクリート基準点を現在地に設定");
        s.sendMessage("§e/ba setmaplobby <mapId> §7- マップのロビー地点を現在地に設定");
        s.sendMessage("§e/ba setmap <mapId> §7- 次の試合マップを指定");
        s.sendMessage("§e/ba start §7- 試合を強制開始");
        s.sendMessage("§e/ba stop §7- 試合を強制終了");
        s.sendMessage("§e/ba spectate §7- 進行中試合を観戦");
        s.sendMessage("§e/ba reload §7- config.yml をリロード");
        s.sendMessage("§e/ba status §7- 現在の状態を表示");
        s.sendMessage("§e/ba admin imigration §7- セットアップウィザード起動（ロビー+マップ1個を対話設定）");
        s.sendMessage("§e/ba kitedit §7- キットエディタを開く（運営限定GUI）");
        s.sendMessage("§e/ba setgate <red|blue> <mapId> §7- ゲート領域をワンドで設定（縦横対応）");
        s.sendMessage("§e/ba gatematl <mapId> <素材> §7- ゲートブロック素材を指定（例: BARRIER）");
        s.sendMessage("§e/ba setoob <mapId|lobby> §7- エリア外判定ゾーンをワンドで設定");
        s.sendMessage("§e/ba bot add [n] §7- テスト用BOTを追加（試合開始前）");
        s.sendMessage("§e/ba bot clear §7- BOTをすべて削除");
        s.sendMessage("§e/ba stats [player] §7- 統計を表示");
        s.sendMessage("§e/ba top [kills|wins|kd|damage] §7- ランキング表示");
        s.sendMessage("§e/ba continuous <on|off> §7- 連続試合モード切り替え");
        s.sendMessage("§e/ba setmapname <mapId> <名前> §7- マップの表示名を設定");
        s.sendMessage("§e/ba upgrade <mapId> §7- 既存マップを新モード対応にアップグレード");
        s.sendMessage("§e/ba convert §7- 旧configを新形式に自動変換");
        s.sendMessage("§e/ba setredflag <mapId> §7- CTF赤旗初期位置を現在地に設定（相手陣地側）");
        s.sendMessage("§e/ba setblueflag <mapId> §7- CTF青旗初期位置を現在地に設定（相手陣地側）");
        s.sendMessage("§e/ba setredreturn <mapId> §7- CTF赤持ち帰り地点を現在地に設定");
        s.sendMessage("§e/ba setbluereturn <mapId> §7- CTF青持ち帰り地点を現在地に設定");
        s.sendMessage("§e/ba setbombplant <mapId> §7- 爆破設置地点を現在地に設定");
        s.sendMessage("§e/ba setbombdefuse <mapId> §7- 爆破解除地点を現在地に設定");
        s.sendMessage("§e/ba test [leave] §7- テスト場に入る/退出");
        s.sendMessage("§e/ba kits §7- キット一覧を表示（誰でも使用可能）");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start","stop","wand","setwaitingarea","setlobby",
                "addmap","info","setspawnzone","setcenter","setmaplobby","setmap","setmapname","kitedit",
                "kits","bot","stats","top","continuous","setgate","gatematl","setoob","spectate","reload","status","admin","upgrade","convert","test");
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "setspawnzone" -> Arrays.asList("red", "blue");
                case "admin"      -> Arrays.asList("imigration", "addmap", "next", "cancel", "skip");
                case "top"        -> Arrays.asList("kills", "wins", "kd", "damage");
                case "bot"        -> Arrays.asList("add", "clear", "list");
                case "continuous" -> Arrays.asList("on", "off");
                case "setgate"    -> Arrays.asList("red", "blue");
                case "setoob" -> {
                    var ids = plugin.getMapManager().getMaps().stream()
                        .map(MapConfig::getId)
                        .collect(java.util.stream.Collectors.toList());
                    ids.add("lobby");
                    yield ids;
                }
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }
}
