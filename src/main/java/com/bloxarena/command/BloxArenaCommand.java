/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package com.bloxarena.command;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.command.MapWizard;
import com.bloxarena.command.SetupWizard;
import com.bloxarena.game.GameMode;
import com.bloxarena.game.GameState;
import com.bloxarena.map.MapConfig;
import com.bloxarena.song.NbsPlayer;
import com.bloxarena.stats.PlayerStats;
import com.bloxarena.stats.StatsManager;
import com.bloxarena.util.SelectionTool;
import java.io.File;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BloxArenaCommand
implements CommandExecutor,
TabCompleter {
    private final BloxArenaPlugin plugin;
    private final SetupWizard wizard;
    private final MapWizard mapWizard;

    public BloxArenaCommand(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.wizard = new SetupWizard(plugin);
        this.mapWizard = new MapWizard(plugin);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String sub;
        if (args.length == 0) {
            this.sendHelp(sender);
            return true;
        }
        block42 : switch (sub = args[0].toLowerCase()) {
            case "start": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (this.plugin.getGameManager().getState() != GameState.WAITING) {
                    sender.sendMessage("\u00a7c\u8a66\u5408\u306f\u3059\u3067\u306b\u9032\u884c\u4e2d\u3067\u3059\u3002");
                    return true;
                }
                ArrayList<UUID> participants = new ArrayList<UUID>(this.plugin.getLobbyManager().getWaitingPlayers());
                if (participants.isEmpty()) {
                    sender.sendMessage("\u00a7c\u5f85\u6a5f\u30a8\u30ea\u30a2\u306b\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u3044\u307e\u305b\u3093\u3002");
                    return true;
                }
                GameMode mode = GameMode.random(participants.size());
                MapConfig map = this.plugin.getMapManager().selectMap(mode);
                if (map == null) {
                    sender.sendMessage("\u00a7c\u4f7f\u7528\u53ef\u80fd\u306a\u30de\u30c3\u30d7\u304c\u3042\u308a\u307e\u305b\u3093\u3002\u00a77(/ba addmap, /ba setspawnzone \u7b49\u3067\u8a2d\u5b9a\u3057\u3066\u304f\u3060\u3055\u3044)");
                    return true;
                }
                this.plugin.getGameManager().startGame(map, mode, participants);
                sender.sendMessage("\u00a7a\u8a66\u5408\u3092\u5f37\u5236\u958b\u59cb\u3057\u307e\u3057\u305f\u3002");
                break;
            }
            case "stop": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                this.plugin.getGameManager().forceStop();
                sender.sendMessage("\u00a7a\u8a66\u5408\u3092\u5f37\u5236\u7d42\u4e86\u3057\u307e\u3057\u305f\u3002");
                break;
            }
            case "wand": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                p.getInventory().addItem(new ItemStack[]{this.plugin.getSelectionTool().createWand()});
                p.sendMessage("\u00a7a\u30ac\u30e9\u30b9\u30d6\u30ed\u30c3\u30af\u306e\u30ef\u30f3\u30c9\u3092\u53d7\u3051\u53d6\u308a\u307e\u3057\u305f\u3002");
                p.sendMessage("\u00a77\u00a7l\u5de6\u30af\u30ea\u30c3\u30af \u00a7r\u00a77= Pos1 (min)  \u00a77\u00a7l\u53f3\u30af\u30ea\u30c3\u30af \u00a7r\u00a77= Pos2 (max)");
                break;
            }
            case "setwaitingarea": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                SelectionTool tool = this.plugin.getSelectionTool();
                if (!tool.hasSelection(p)) {
                    sender.sendMessage("\u00a7c\u307e\u305a\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002 (/bloxarena wand)");
                    return true;
                }
                this.plugin.getLobbyManager().setWaitingAreaMin(tool.getMin(p));
                this.plugin.getLobbyManager().setWaitingAreaMax(tool.getMax(p));
                sender.sendMessage("\u00a7a\u5f85\u6a5f\u30a8\u30ea\u30a2\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                sender.sendMessage("\u00a77min: " + this.fmt(tool.getMin(p)) + "  max: " + this.fmt(tool.getMax(p)));
                break;
            }
            case "setlobby": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                this.plugin.getLobbyManager().setLobbySpawn(p.getLocation());
                sender.sendMessage("\u00a7a\u30ed\u30d3\u30fc\u30b9\u30dd\u30fc\u30f3\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                break;
            }
            case "addmap": {
                String worldName;
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /bloxarena addmap <mapId> [worldName]");
                    return true;
                }
                String mapId = args[1];
                if (args.length >= 3) {
                    worldName = args[2];
                } else if (sender instanceof Player) {
                    Player p = (Player)sender;
                    worldName = p.getWorld().getName();
                } else {
                    worldName = "world";
                }
                if (!this.plugin.getMapManager().addMap(mapId, worldName)) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + mapId + "' \u306f\u3059\u3067\u306b\u5b58\u5728\u3057\u307e\u3059\u3002");
                    return true;
                }
                sender.sendMessage("\u00a7a\u30de\u30c3\u30d7 \u00a7e" + mapId + " \u00a7a(\u30ef\u30fc\u30eb\u30c9: \u00a7e" + worldName + "\u00a7a) \u3092\u8ffd\u52a0\u3057\u307e\u3057\u305f\u3002");
                sender.sendMessage("\u00a77\u6b21: /ba setspawnzone red " + mapId + " / /ba setspawnzone blue " + mapId);
                sender.sendMessage("\u00a77    /ba setcenter " + mapId + " / /ba setmaplobby " + mapId);
                break;
            }
            case "setspawnzone": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length < 3) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /bloxarena setspawnzone <red|blue> <mapId>");
                    return true;
                }
                String teamStr = args[1].toLowerCase();
                MapConfig cfg = this.plugin.getMapManager().getById(args[2]);
                if (cfg == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + args[2] + "' \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                SelectionTool tool = this.plugin.getSelectionTool();
                if (!tool.hasSelection(p)) {
                    sender.sendMessage("\u00a7c\u307e\u305a\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002 (/bloxarena wand)");
                    return true;
                }
                if ("red".equals(teamStr)) {
                    cfg.setRedSpawnMin(tool.getMin(p));
                    cfg.setRedSpawnMax(tool.getMax(p));
                } else if ("blue".equals(teamStr)) {
                    cfg.setBlueSpawnMin(tool.getMin(p));
                    cfg.setBlueSpawnMax(tool.getMax(p));
                } else {
                    sender.sendMessage("\u00a7c red \u304b blue \u3092\u6307\u5b9a\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return true;
                }
                this.plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("\u00a7a" + teamStr + "\u30b9\u30dd\u30fc\u30f3\u30be\u30fc\u30f3\u3092\u8a2d\u5b9a\u30fb\u4fdd\u5b58\u3057\u307e\u3057\u305f\u3002");
                sender.sendMessage("\u00a77min: " + this.fmt(tool.getMin(p)) + "  max: " + this.fmt(tool.getMax(p)));
                break;
            }
            case "setcenter": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length < 2) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /bloxarena setcenter <mapId>");
                    return true;
                }
                MapConfig cfg = this.plugin.getMapManager().getById(args[1]);
                if (cfg == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                cfg.setCenter(p.getLocation());
                this.plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("\u00a7a\u4e2d\u592e\u57fa\u6e96\u70b9\u3092\u8a2d\u5b9a\u30fb\u4fdd\u5b58\u3057\u307e\u3057\u305f: " + this.fmt(p.getLocation()));
                break;
            }
            case "setmaplobby": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length < 2) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /bloxarena setmaplobby <mapId>");
                    return true;
                }
                MapConfig cfg = this.plugin.getMapManager().getById(args[1]);
                if (cfg == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                cfg.setLobby(p.getLocation());
                this.plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("\u00a7a\u30de\u30c3\u30d7\u30ed\u30d3\u30fc\u5730\u70b9\u3092\u8a2d\u5b9a\u30fb\u4fdd\u5b58\u3057\u307e\u3057\u305f: " + this.fmt(p.getLocation()));
                break;
            }
            case "info": {
                if (args.length < 2) {
                    sender.sendMessage("\u00a76\u00a7l=== \u30de\u30c3\u30d7\u4e00\u89a7 (" + this.plugin.getMapManager().getMaps().size() + ") ===");
                    for (MapConfig mc : this.plugin.getMapManager().getMaps()) {
                        String ready = mc.isReady() ? "\u00a7a\u2714" : "\u00a7c\u2718";
                        String modes = this.modeFlags(mc);
                        String centerStr = mc.getCenter() != null ? " \u00a77(" + mc.getCenter().getBlockX() + ", " + mc.getCenter().getBlockY() + ", " + mc.getCenter().getBlockZ() + ")" : "";
                        sender.sendMessage(ready + " \u00a7e" + mc.getId() + " \u00a77[" + mc.getWorldName() + "]" + centerStr + " " + modes);
                    }
                    sender.sendMessage("\u00a77\u51e1\u4f8b: \u00a7fBA=\u30a2\u30ea\u30fc\u30ca TDM=\u30c7\u30b9\u30de\u30c3\u30c1 \u00a7cB=\u7206\u7834\u89e3\u4f53 \u00a7eD=\u5360\u9818 \u00a79CTF=\u65d7\u53d6\u308a");
                    return true;
                }
                MapConfig mc = this.plugin.getMapManager().getById(args[1]);
                if (mc == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + args[1] + "' \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                sender.sendMessage("\u00a76\u00a7l=== \u30de\u30c3\u30d7: " + mc.getId() + " ===");
                sender.sendMessage("\u00a77\u30ef\u30fc\u30eb\u30c9: \u00a7f" + mc.getWorldName());
                sender.sendMessage("\u00a77\u6e96\u5099\u5b8c\u4e86: " + (mc.isReady() ? "\u00a7a\u2714" : "\u00a7c\u2718 (\u672a\u8a2d\u5b9a: \u30b9\u30dd\u30fc\u30f3\u7b49)"));
                sender.sendMessage("\u00a77RedSpawn: \u00a7f" + this.fmtNull(mc.getRedSpawnMin()) + " ~ " + this.fmtNull(mc.getRedSpawnMax()));
                sender.sendMessage("\u00a77BlueSpawn: \u00a7f" + this.fmtNull(mc.getBlueSpawnMin()) + " ~ " + this.fmtNull(mc.getBlueSpawnMax()));
                sender.sendMessage("\u00a77Center: \u00a7f" + this.fmtNull(mc.getCenter()));
                sender.sendMessage("\u00a77Lobby: \u00a7f" + this.fmtNull(mc.getLobby()));
                sender.sendMessage("\u00a77Gate: R" + this.fmtNull(mc.getRedGateMin()) + " / B" + this.fmtNull(mc.getBlueGateMin()));
                sender.sendMessage("\u00a77OOB: " + this.fmtNull(mc.getOobMin()) + " ~ " + this.fmtNull(mc.getOobMax()));
                sender.sendMessage("\u00a79CTF\u65d7:");
                sender.sendMessage("  \u00a7c\u8d64\u65d7: \u00a7f" + this.fmtNull(mc.getRedFlagLocation()) + "  \u00a77\u5e30\u9084: \u00a7f" + this.fmtNull(mc.getRedReturnLocation()));
                sender.sendMessage("  \u00a79\u9752\u65d7: \u00a7f" + this.fmtNull(mc.getBlueFlagLocation()) + "  \u00a77\u5e30\u9084: \u00a7f" + this.fmtNull(mc.getBlueReturnLocation()));
                sender.sendMessage("\u00a7c\u7206\u7834:");
                sender.sendMessage("  \u00a77\u8a2d\u7f6e: \u00a7f" + this.fmtNull(mc.getBombSite()) + "  \u00a77\u89e3\u9664: \u00a7f" + this.fmtNull(mc.getDefusePoint()));
                sender.sendMessage("\u00a77\u5bfe\u5fdc\u30e2\u30fc\u30c9: " + this.modeFlags(mc));
                break;
            }
            case "setmap": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /bloxarena setmap <mapId>");
                    return true;
                }
                if (this.plugin.getMapManager().getById(args[1]) == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + args[1] + "' \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                this.plugin.getMapManager().setNextMap(args[1]);
                sender.sendMessage("\u00a7a\u6b21\u306e\u8a66\u5408\u30de\u30c3\u30d7\u3092 \u00a7e" + args[1] + " \u00a7a\u306b\u8a2d\u5b9a\u3057\u307e\u3057\u305f\uff081\u8a66\u5408\u9650\u5b9a\uff09\u3002");
                break;
            }
            case "setmapname": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba setmapname <mapId> <\u8868\u793a\u540d>");
                    return true;
                }
                MapConfig cfg = this.plugin.getMapManager().getById(args[1]);
                if (cfg == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + args[1] + "' \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                String name = String.join((CharSequence)" ", Arrays.copyOfRange(args, 2, args.length));
                cfg.setDisplayName(name);
                this.plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("\u00a7a\u30de\u30c3\u30d7 \u00a7e" + args[1] + " \u00a7a\u306e\u8868\u793a\u540d\u3092 \u00a7e" + name + " \u00a7a\u306b\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                break;
            }
            case "kits": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                this.plugin.getKitInfoGUI().openList(p);
                break;
            }
            case "spectate": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (this.plugin.getGameManager().getState() != GameState.IN_GAME) {
                    sender.sendMessage("\u00a7c\u73fe\u5728\u8a66\u5408\u306f\u9032\u884c\u4e2d\u3067\u306f\u3042\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                this.plugin.getGameManager().addSpectator(p);
                sender.sendMessage("\u00a7a\u89b3\u6226\u30e2\u30fc\u30c9\u3078\u79fb\u884c\u3057\u307e\u3057\u305f\u3002");
                break;
            }
            case "reload": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                this.plugin.reloadConfig();
                this.plugin.getLobbyManager().reload();
                this.plugin.getMapManager().reload();
                sender.sendMessage("\u00a7aconfig.yml \u3092\u30ea\u30ed\u30fc\u30c9\u3057\u307e\u3057\u305f\u3002");
                break;
            }
            case "status": {
                sender.sendMessage("\u00a76\u00a7l=== BAII WoNG Status ===");
                sender.sendMessage("\u00a77\u72b6\u614b: \u00a7f" + String.valueOf((Object)this.plugin.getGameManager().getState()));
                sender.sendMessage("\u00a77\u5f85\u6a5f\u4eba\u6570: \u00a7f" + this.plugin.getLobbyManager().getWaitingPlayers().size());
                sender.sendMessage("\u00a77\u30de\u30c3\u30d7\u6570: \u00a7f" + this.plugin.getMapManager().getMaps().size() + " \u00a77(\u6e96\u5099\u5b8c\u4e86: \u00a7a" + this.plugin.getMapManager().getMaps().stream().filter(MapConfig::isReady).count() + "\u00a77)");
                sender.sendMessage("\u00a77\u8d64\u30c1\u30fc\u30e0: \u00a7f" + this.plugin.getGameManager().getRedTeam().size() + "\u4eba");
                sender.sendMessage("\u00a77\u9752\u30c1\u30fc\u30e0: \u00a7f" + this.plugin.getGameManager().getBlueTeam().size() + "\u4eba");
                break;
            }
            case "kitedit": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                this.plugin.getKitEditorGUI().openList(p);
                break;
            }
            case "stats": {
                OfflinePlayer target;
                if (args.length >= 2) {
                    target = Bukkit.getOfflinePlayerIfCached((String)args[1]);
                    if (target == null) {
                        sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093: " + args[1]);
                        return true;
                    }
                } else if (sender instanceof Player) {
                    Player p = (Player)sender;
                    target = p;
                } else {
                    sender.sendMessage("\u00a7c\u4f7f\u7528\u6cd5: /ba stats <name>");
                    return true;
                }
                StatsManager sm = this.plugin.getStatsManager();
                PlayerStats s = sm.getStats(target.getUniqueId());
                sender.sendMessage("\u00a76\u00a7l=== " + target.getName() + " \u306e\u7d71\u8a08 ===");
                sender.sendMessage("\u00a77Kill: \u00a7f" + s.kills + "  Death: \u00a7f" + s.deaths + "  \u00a77K/D: \u00a7f" + String.format("%.2f", s.getKD()));
                sender.sendMessage("\u00a77\u52dd\u5229: \u00a7f" + s.wins + "  \u6557\u5317: \u00a7f" + s.losses + "  \u00a77\u52dd\u7387: \u00a7f" + String.format("%.1f", s.getWinRate()) + "%");
                sender.sendMessage("\u00a77\u7dcf\u30c0\u30e1\u30fc\u30b8: \u00a7f" + String.format("%.1f", s.damage));
                if (s.kitCounts.isEmpty()) break;
                Map<String, Integer> mastery = sm.getKitMasteryLevels(target.getUniqueId());
                List<Map.Entry<String, Integer>> topKits = s.kitCounts.entrySet().stream().sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())).limit(5L).collect(Collectors.toList());
                List<String> parts = new ArrayList<String>();
                for (Map.Entry<String, Integer> e2 : topKits) {
                    int level = mastery.getOrDefault(e2.getKey(), 0);
                    parts.add("\u00a7e" + e2.getKey() + " \u00a77Lv." + level + " \u00a77(" + e2.getValue() + "\u56ce)");
                }
                String bestKit = null;
                int bestLevel = 0;
                for (Map.Entry<String, Integer> me : mastery.entrySet()) {
                    if (me.getValue() > bestLevel) {
                        bestLevel = me.getValue();
                        bestKit = me.getKey();
                    }
                }
                if (bestKit != null) {
                    sender.sendMessage("\u00a77\u6700\u9ad8\u30de\u30b9\u30bf\u30ea\u30fc: " + sm.getKitMasteryTitle(bestKit, bestLevel));
                }
                sender.sendMessage("\u00a77\u3088\u304f\u4f7f\u3046\u30ad\u30c3\u30c8: " + String.join("  ", parts));
                break;
            }
            case "mastery": {
                OfflinePlayer target;
                if (args.length >= 2) {
                    target = Bukkit.getOfflinePlayerIfCached((String)args[1]);
                    if (target == null) {
                        sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093: " + args[1]);
                        return true;
                    }
                } else if (sender instanceof Player) {
                    Player p = (Player)sender;
                    target = p;
                } else {
                    sender.sendMessage("\u00a7c\u4f7f\u7528\u6cd5: /ba mastery <name>");
                    return true;
                }
                StatsManager sm = this.plugin.getStatsManager();
                PlayerStats s = sm.getStats(target.getUniqueId());
                Map<String, Integer> levels = sm.getKitMasteryLevels(target.getUniqueId());
                sender.sendMessage("\u00a76\u00a7l=== " + target.getName() + " \u306e\u30de\u30b9\u30bf\u30ea\u30fc ===");
                sender.sendMessage("\u00a77\u7dcf\u30ad\u30c3\u30c8\u4f7f\u7528: \u00a7f" + levels.size() + "\u7a2e\u985e");
                String bestKit = null;
                int bestLevel = 0;
                for (Map.Entry<String, Integer> me : levels.entrySet()) {
                    if (me.getValue() > bestLevel) {
                        bestLevel = me.getValue();
                        bestKit = me.getKey();
                    }
                }
                if (bestKit != null) {
                    sender.sendMessage("\u00a77\u6700\u9ad8\u30ec\u30d9\u30eb\u30ad\u30c3\u30c8: " + sm.getKitMasteryTitle(bestKit, bestLevel));
                }
                List<Map.Entry<String, Integer>> sorted = levels.entrySet().stream().sorted((e1, e2) -> {
                    int c = Integer.compare(e2.getValue(), e1.getValue());
                    if (c != 0) {
                        return c;
                    }
                    return Integer.compare(s.kitCounts.getOrDefault(e2.getKey(), 0), s.kitCounts.getOrDefault(e1.getKey(), 0));
                }).limit(3L).collect(Collectors.toList());
                sender.sendMessage("\u00a76\u00a7l=== Top 3 \u30de\u30b9\u30bf\u30ea\u30fc\u30ad\u30c3\u30c8 ===");
                int rank = 1;
                for (Map.Entry<String, Integer> me : sorted) {
                    int count = s.kitCounts.getOrDefault(me.getKey(), 0);
                    sender.sendMessage("\u00a77#" + rank++ + " " + sm.getKitMasteryTitle(me.getKey(), me.getValue()) + " \u00a77(" + count + "\u56ce)");
                }
                break;
            }
            case "top": {
                String field;
                String string = field = args.length >= 2 ? args[1].toLowerCase() : "kills";
                if (!List.of("kills", "wins", "kd", "damage", "kits").contains(field)) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba top [kills|wins|kd|damage|kits]");
                    return true;
                }
                StatsManager sm = this.plugin.getStatsManager();
                if ("kits".equals(field)) {
                    List<Map.Entry<String, Integer>> kitTop = sm.getKitTop(15);
                    sender.sendMessage("\u00a76\u00a7l=== \u30ad\u30c3\u30c8\u4f7f\u7528\u7387 Top 15 ===");
                    int rank = 1;
                    for (Map.Entry<String, Integer> e2 : kitTop) {
                        sender.sendMessage("\u00a77#" + rank++ + " \u00a7e" + e2.getKey() + " \u00a7f" + String.valueOf(e2.getValue()) + "\u56de");
                    }
                    Map<String, List<Map.Entry<UUID, Integer>>> kitPlayers = sm.getKitUsageWithTopPlayers();
                    if (!kitPlayers.isEmpty()) {
                        sender.sendMessage("\u00a76\u00a7l=== \u30ad\u30c3\u30c8\u5225 Top 3 \u30d7\u30ec\u30a4\u30e4\u30fc ===");
                        int kitIdx = 1;
                        int maxShow = Math.min(10, kitPlayers.size());
                        for (Map.Entry<String, List<Map.Entry<UUID, Integer>>> kitEntry : kitPlayers.entrySet()) {
                            if (kitIdx > maxShow) break;
                            String kitName = kitEntry.getKey();
                            List<Map.Entry<UUID, Integer>> players = kitEntry.getValue();
                            int total = kitTop.stream().filter(e -> ((String)e.getKey()).equals(kitName)).findFirst().map(Map.Entry::getValue).orElse(0);
                            sender.sendMessage("\u00a77#" + kitIdx + " \u00a7e" + kitName + " \u00a77(\u00a7f" + total + "\u56de\u00a77)");
                            for (Map.Entry<UUID, Integer> pe : players) {
                                double pct = total > 0 ? (double)pe.getValue().intValue() / (double)total * 100.0 : 0.0;
                                String name = sm.getName(pe.getKey());
                                sender.sendMessage("    \u00a77- \u00a7f" + name + " \u00a77(\u00a7f" + String.valueOf(pe.getValue()) + "\u56de \u00a77" + String.format("%.0f%%\u00a77)", pct));
                            }
                            ++kitIdx;
                        }
                    }
                    return true;
                }
                List<Map.Entry<UUID, PlayerStats>> top = sm.getTop(field, 10);
                sender.sendMessage("\u00a76\u00a7l=== Top 10: " + field + " ===");
                int rank = 1;
                for (Map.Entry<UUID, PlayerStats> entry : top) {
                    PlayerStats s = entry.getValue();
                    String name = sm.getName(entry.getKey());
                    String val = switch (field) {
                        case "wins" -> s.wins + " wins";
                        case "kd" -> String.format("%.2f K/D", s.getKD());
                        case "damage" -> String.format("%.0f dmg", s.damage);
                        default -> s.kills + " kills";
                    };
                    sender.sendMessage("\u00a77#" + rank++ + " \u00a7e" + name + " \u00a7f" + val);
                }
                break;
            }
            case "continuous": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (args.length < 2) {
                    boolean cur = this.plugin.getLobbyManager().isContinuousMode();
                    sender.sendMessage("\u00a77\u9023\u7d9a\u8a66\u5408\u30e2\u30fc\u30c9: " + (cur ? "\u00a7aON" : "\u00a7cOFF"));
                    sender.sendMessage("\u00a77\u5207\u308a\u66ff\u3048: \u00a7f/ba continuous <on|off>");
                    return true;
                }
                boolean enable = "on".equalsIgnoreCase(args[1]);
                this.plugin.getLobbyManager().setContinuousMode(enable);
                sender.sendMessage("\u00a7a\u9023\u7d9a\u8a66\u5408\u30e2\u30fc\u30c9\u3092 " + (enable ? "\u00a7aON" : "\u00a7cOFF") + " \u00a7a\u306b\u3057\u307e\u3057\u305f\u3002");
                break;
            }
            case "admin": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length < 2) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba admin <imigration|addmap|next|cancel|skip>");
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "imigration": {
                        this.wizard.start(p);
                        break block42;
                    }
                    case "addmap": {
                        if (args.length < 3) {
                            sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba admin addmap <mapId>");
                            break block42;
                        }
                        this.mapWizard.start(p, args[2].toLowerCase());
                        break block42;
                    }
                    case "next": {
                        if (this.mapWizard.isActive(p)) {
                            this.mapWizard.next(p);
                            break block42;
                        }
                        this.wizard.next(p);
                        break block42;
                    }
                    case "skip": {
                        if (this.mapWizard.isActive(p)) {
                            this.mapWizard.skip(p);
                            break block42;
                        }
                        this.wizard.skip(p);
                        break block42;
                    }
                    case "cancel": {
                        if (this.mapWizard.isActive(p)) {
                            this.mapWizard.cancel(p);
                            break block42;
                        }
                        this.wizard.cancel(p);
                        break block42;
                    }
                }
                sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba admin <imigration|addmap|next|cancel|skip>");
                break;
            }
            case "setgate": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length < 3) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba setgate <red|blue> <mapId>");
                    return true;
                }
                String side = args[1].toLowerCase();
                MapConfig cfg = this.plugin.getMapManager().getById(args[2]);
                if (cfg == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + args[2] + "' \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                SelectionTool tool = this.plugin.getSelectionTool();
                if (!tool.hasSelection(p)) {
                    sender.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return true;
                }
                if ("red".equals(side)) {
                    cfg.setRedGateMin(tool.getMin(p));
                    cfg.setRedGateMax(tool.getMax(p));
                } else if ("blue".equals(side)) {
                    cfg.setBlueGateMin(tool.getMin(p));
                    cfg.setBlueGateMax(tool.getMax(p));
                } else {
                    sender.sendMessage("\u00a7c red \u304b blue \u3092\u6307\u5b9a\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return true;
                }
                this.plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("\u00a7a" + side + "\u30c1\u30fc\u30e0\u306e\u30b2\u30fc\u30c8\u9818\u57df\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                break;
            }
            case "gatematl": {
                Material mat;
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba gatematl <mapId> <\u7d20\u6750\u540d>");
                    return true;
                }
                MapConfig cfg = this.plugin.getMapManager().getById(args[1]);
                if (cfg == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + args[1] + "' \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                try {
                    mat = Material.valueOf((String)args[2].toUpperCase());
                }
                catch (IllegalArgumentException e2) {
                    sender.sendMessage("\u00a7c\u4e0d\u660e\u306a\u7d20\u6750: " + args[2]);
                    return true;
                }
                cfg.setGateMaterial(mat);
                this.plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("\u00a7a\u30b2\u30fc\u30c8\u30d6\u30ed\u30c3\u30af\u3092 \u00a7e" + mat.name() + " \u00a7a\u306b\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                break;
            }
            case "setoob": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length < 2) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba setoob <mapId|lobby>");
                    return true;
                }
                SelectionTool tool = this.plugin.getSelectionTool();
                if (!tool.hasSelection(p)) {
                    sender.sendMessage("\u00a7c\u30ef\u30f3\u30c9\u30672\u70b9\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return true;
                }
                if ("lobby".equalsIgnoreCase(args[1])) {
                    this.plugin.getLobbyManager().setLobbyOob(tool.getMin(p), tool.getMax(p));
                    sender.sendMessage("\u00a7a\u30ed\u30d3\u30fcOOB\u30be\u30fc\u30f3\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                    break;
                }
                MapConfig cfg = this.plugin.getMapManager().getById(args[1]);
                if (cfg == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + args[1] + "' \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                cfg.setOobMin(tool.getMin(p));
                cfg.setOobMax(tool.getMax(p));
                this.plugin.getMapManager().saveMap(cfg);
                sender.sendMessage("\u00a7a\u30de\u30c3\u30d7 \u00a7e" + args[1] + " \u00a7a\u306eOOB\u30be\u30fc\u30f3\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                break;
            }
            case "setredflag": 
            case "setblueflag": 
            case "setredreturn": 
            case "setbluereturn": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length < 2) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba " + sub + " <mapId>");
                    sender.sendMessage("\u00a77  setredflag    \u2192 \u76f8\u624b\u9663\u5730\u306e\u8d64\u65d7\u521d\u671f\u4f4d\u7f6e\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
                    sender.sendMessage("\u00a77  setblueflag   \u2192 \u76f8\u624b\u9663\u5730\u306e\u9752\u65d7\u521d\u671f\u4f4d\u7f6e\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
                    sender.sendMessage("\u00a77  setredreturn  \u2192 \u8d64\u30c1\u30fc\u30e0\u306e\u65d7\u6301\u3061\u5e30\u308a\u5730\u70b9\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
                    sender.sendMessage("\u00a77  setbluereturn \u2192 \u9752\u30c1\u30fc\u30e0\u306e\u65d7\u6301\u3061\u5e30\u308a\u5730\u70b9\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
                    return true;
                }
                MapConfig fc = this.plugin.getMapManager().getById(args[1].toLowerCase());
                if (fc == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + args[1] + "' \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                Location loc = p.getLocation().getBlock().getLocation().add(0.5, 0.0, 0.5);
                switch (sub) {
                    case "setredflag": {
                        fc.setRedFlagLocation(loc);
                        sender.sendMessage("\u00a7a\u2714 \u81ea\u9663\u306e\u8d64\u65d7\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\uff08\u6575\u304c\u596a\u3044\u306b\u6765\u307e\u3059\uff09\u3002");
                        break;
                    }
                    case "setblueflag": {
                        fc.setBlueFlagLocation(loc);
                        sender.sendMessage("\u00a7a\u2714 \u81ea\u9663\u306e\u9752\u65d7\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\uff08\u6575\u304c\u596a\u3044\u306b\u6765\u307e\u3059\uff09\u3002");
                        break;
                    }
                    case "setredreturn": {
                        fc.setRedReturnLocation(loc);
                        sender.sendMessage("\u00a7a\u2714 \u8d64\u6301\u3061\u5e30\u308a\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                        break;
                    }
                    case "setbluereturn": {
                        fc.setBlueReturnLocation(loc);
                        sender.sendMessage("\u00a7a\u2714 \u9752\u6301\u3061\u5e30\u308a\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                    }
                }
                this.plugin.getMapManager().saveMap(fc);
                break;
            }
            case "setbombplant": 
            case "setbombdefuse": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length < 2) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba " + sub + " <mapId>");
                    sender.sendMessage("\u00a77  setbombplant \u2192 \u7206\u5f3e\u8a2d\u7f6e\u5730\u70b9\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
                    sender.sendMessage("\u00a77  setbombdefuse\u2192 \u7206\u5f3e\u89e3\u9664\u5730\u70b9\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
                    return true;
                }
                MapConfig mc2 = this.plugin.getMapManager().getById(args[1].toLowerCase());
                if (mc2 == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + args[1] + "' \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                Location bl = p.getLocation().getBlock().getLocation().add(0.5, 0.0, 0.5);
                switch (sub) {
                    case "setbombplant": {
                        mc2.setBombSite(bl);
                        sender.sendMessage("\u00a7a\u2714 \u7206\u5f3e\u8a2d\u7f6e\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                        break;
                    }
                    case "setbombdefuse": {
                        mc2.setDefusePoint(bl);
                        sender.sendMessage("\u00a7a\u2714 \u7206\u5f3e\u89e3\u9664\u5730\u70b9\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                    }
                }
                this.plugin.getMapManager().saveMap(mc2);
                break;
            }
            case "bot": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba bot <add [n]|clear|list>");
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "add": {
                        int n = args.length >= 3 ? this.parseInt(args[2], 1) : 1;
                        this.plugin.getBotManager().addPendingBots(n);
                        sender.sendMessage("\u00a7aBOT\u3092 \u00a7e" + n + "\u4f53 \u00a7a\u8ffd\u52a0\u4e88\u7d04\u3057\u307e\u3057\u305f\uff08\u8a66\u5408\u958b\u59cb\u6642\u306b\u30b9\u30dd\u30fc\u30f3\uff09\u3002");
                        break block42;
                    }
                    case "clear": {
                        this.plugin.getBotManager().clearAll();
                        sender.sendMessage("\u00a7aBOT\u3092\u3059\u3079\u3066\u524a\u9664\u3057\u307e\u3057\u305f\u3002");
                        break block42;
                    }
                    case "list": {
                        int cnt = this.plugin.getBotManager().getTotalBotCount();
                        sender.sendMessage("\u00a77\u73fe\u5728\u306eBOT\u6570: \u00a7f" + cnt + "\u4f53");
                        break block42;
                    }
                }
                sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba bot <add [n]|clear|list>");
                break;
            }
            case "convert": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                FileConfiguration cfg = this.plugin.getConfig();
                int merged = 0;
                int added = 0;
                File oldFile = new File(this.plugin.getDataFolder().getParentFile(), "BloxArena/config.yml");
                if (oldFile.exists()) {
                    YamlConfiguration oldCfg = YamlConfiguration.loadConfiguration((File)oldFile);
                    if (oldCfg.isConfigurationSection("lobby") && !cfg.isConfigurationSection("lobby")) {
                        cfg.set("lobby", oldCfg.get("lobby"));
                        ++merged;
                    }
                    if (oldCfg.isConfigurationSection("maps") && !cfg.isConfigurationSection("maps")) {
                        cfg.set("maps", oldCfg.get("maps"));
                        ++merged;
                    }
                    if (oldCfg.contains("lobby.waiting_area.world") && !cfg.contains("lobby.waiting_area.world")) {
                        cfg.set("lobby.waiting_area", oldCfg.get("lobby.waiting_area"));
                        ++merged;
                    }
                    if (oldCfg.contains("lobby.spawn.x") && !cfg.contains("lobby.spawn.x")) {
                        cfg.set("lobby.spawn", oldCfg.get("lobby.spawn"));
                        ++merged;
                    }
                    if (oldCfg.contains("lobby.oob.min.x")) {
                        cfg.set("lobby.oob", oldCfg.get("lobby.oob"));
                        ++merged;
                    }
                    if (oldCfg.isConfigurationSection("maps")) {
                        for (String mapId : oldCfg.getConfigurationSection("maps").getKeys(false)) {
                            if (!oldCfg.contains("maps." + mapId + ".oob.min.x")) continue;
                            cfg.set("maps." + mapId + ".oob", oldCfg.get("maps." + mapId + ".oob"));
                        }
                    }
                    sender.sendMessage("\u00a77\u65e7BloxArena\u306econfig\u304b\u3089 \u00a7e" + merged + "\u00a77\u30bb\u30af\u30b7\u30e7\u30f3\u3092\u5f15\u304d\u7d99\u304e\u307e\u3057\u305f\u3002");
                }
                if (!cfg.isConfigurationSection("game_modes")) {
                    cfg.set("game_modes.enabled", List.of("BATTLE_ARENA", "TEAM_DEATHMATCH", "BOMB_MISSION", "DOMINATION", "CAPTURE_THE_FLAG", "FFA"));
                    ++added;
                }
                if (!cfg.isConfigurationSection("team_deathmatch")) {
                    cfg.set("team_deathmatch.time_limit_seconds", (Object)300);
                    cfg.set("team_deathmatch.target_kills", (Object)30);
                    ++added;
                }
                if (!cfg.isConfigurationSection("bomb_mission")) {
                    cfg.set("bomb_mission.time_limit_seconds", (Object)180);
                    cfg.set("bomb_mission.plant_time_seconds", (Object)5);
                    cfg.set("bomb_mission.defuse_time_seconds", (Object)7);
                    cfg.set("bomb_mission.bomb_fuse_seconds", (Object)45);
                    ++added;
                }
                if (!cfg.isConfigurationSection("domination")) {
                    cfg.set("domination.time_limit_seconds", (Object)120);
                    cfg.set("domination.target_points", (Object)100);
                    cfg.set("domination.points_per_second", (Object)2);
                    ++added;
                }
                if (!cfg.isConfigurationSection("capture_the_flag")) {
                    cfg.set("capture_the_flag.time_limit_seconds", (Object)300);
                    cfg.set("capture_the_flag.captures_to_win", (Object)3);
                    ++added;
                }
                this.plugin.saveConfig();
                sender.sendMessage("\u00a7a\u30b3\u30f3\u30d0\u30fc\u30c8\u5b8c\u4e86\uff01\u00a7e" + merged + "\u00a7a\u5f15\u7d99 + \u00a7e" + added + "\u00a7a\u8ffd\u52a0");
                sender.sendMessage("\u00a77/ba reload \u3067\u518d\u8aad\u8fbc\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                break;
            }
            case "upgrade": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length < 2) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba upgrade <mapId>");
                    return true;
                }
                String mapId = args[1];
                MapConfig mc = this.plugin.getMapManager().getById(mapId);
                if (mc == null) {
                    sender.sendMessage("\u00a7c\u30de\u30c3\u30d7 '" + mapId + "' \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
                    return true;
                }
                this.mapWizard.startUpgrade(p, mapId);
                sender.sendMessage("\u00a7a\u30de\u30c3\u30d7 \u00a7e" + mapId + " \u00a7a\u306e\u30a2\u30c3\u30d7\u30b0\u30ec\u30fc\u30c9\u30a6\u30a3\u30b6\u30fc\u30c9\u3092\u958b\u59cb\u3057\u307e\u3057\u305f\u3002");
                break;
            }
            case "test": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length >= 2 && "setup".equalsIgnoreCase(args[1])) {
                    if (!this.isAdmin(sender)) {
                        return true;
                    }
                    if (args.length >= 3 && args[2].matches("\\d+")) {
                        int count = Integer.parseInt(args[2]);
                        this.plugin.getConfig().set("test_field.dummy_count", (Object)count);
                        this.plugin.saveConfig();
                        sender.sendMessage("\u00a7a\u30c0\u30df\u30fc\u6570\u3092 \u00a7e" + count + "\u00a7a \u306b\u8a2d\u5b9a\u3057\u307e\u3057\u305f\u3002");
                    }
                    this.plugin.getConfig().set("test_field.spawn.world", (Object)p.getWorld().getName());
                    this.plugin.getConfig().set("test_field.spawn.x", (Object)p.getLocation().getX());
                    this.plugin.getConfig().set("test_field.spawn.y", (Object)p.getLocation().getY());
                    this.plugin.getConfig().set("test_field.spawn.z", (Object)p.getLocation().getZ());
                    this.plugin.saveConfig();
                    this.plugin.getTestFieldManager().reload();
                    sender.sendMessage("\u00a7a\u30c6\u30b9\u30c8\u5834\u30b9\u30dd\u30fc\u30f3\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a\u3057\u307e\u3057\u305f\uff01");
                    sender.sendMessage("\u00a77\u30c0\u30df\u30fc\u6570: \u00a7f" + this.plugin.getConfig().getInt("test_field.dummy_count", 3) + "\u4f53");
                    sender.sendMessage("\u00a77\u00a7f/ba test setarea \u00a77\u3067\u7bc4\u56f2\u8a2d\u5b9a\uff08\u30ef\u30f3\u30c9\u9078\u629e\u5f8c\uff09");
                    sender.sendMessage("\u00a77\u00a7f/ba test \u00a77\u3067\u30c6\u30b9\u30c8\u5834\u306b\u5165\u308c\u307e\u3059");
                    return true;
                }
                if (args.length >= 2 && "setarea".equalsIgnoreCase(args[1])) {
                    if (!this.isAdmin(sender)) {
                        return true;
                    }
                    SelectionTool tool = this.plugin.getSelectionTool();
                    if (!tool.hasSelection(p)) {
                        sender.sendMessage("\u00a7c\u307e\u305a/ba wand\u30672\u70b9\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                        return true;
                    }
                    this.plugin.getTestFieldManager().setArea(tool.getMin(p), tool.getMax(p));
                    sender.sendMessage("\u00a7a\u30c6\u30b9\u30c8\u5834\u306e\u7bc4\u56f2\u3092\u8a2d\u5b9a\u3057\u307e\u3057\u305f\uff01");
                    return true;
                }
                if (!this.plugin.getTestFieldManager().isActive()) {
                    sender.sendMessage("\u00a7c\u30c6\u30b9\u30c8\u5834\u304c\u8a2d\u5b9a\u3055\u308c\u3066\u3044\u307e\u305b\u3093\u3002 \u00a7f/ba test setup [\u30c0\u30df\u30fc\u6570] \u00a7c\u3067\u8a2d\u5b9a\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    return true;
                }
                if (args.length >= 2 && "leave".equalsIgnoreCase(args[1])) {
                    this.plugin.getTestFieldManager().leave(p);
                    break;
                }
                this.plugin.getTestFieldManager().enter(p);
                break;
            }
            case "debug": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                boolean on = this.plugin.getStatsManager().toggleDebug(p.getUniqueId());
                p.sendMessage("\u00a7a\u30c7\u30d0\u30c3\u30b0\u30e2\u30fc\u30c9: " + (on ? "\u00a7eON \u00a77(\u7d71\u8a08\u306b\u8a18\u9332\u3055\u308c\u307e\u305b\u3093)" : "\u00a7cOFF \u00a77(\u7d71\u8a08\u8a18\u9332\u518d\u958b)"));
                break;
            }
            case "tutorial": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length >= 2 && "setup".equalsIgnoreCase(args[1])) {
                    if (!this.isAdmin(sender)) {
                        return true;
                    }
                    this.plugin.getConfig().set("tutorial.spawn.world", (Object)p.getWorld().getName());
                    this.plugin.getConfig().set("tutorial.spawn.x", (Object)p.getLocation().getX());
                    this.plugin.getConfig().set("tutorial.spawn.y", (Object)p.getLocation().getY());
                    this.plugin.getConfig().set("tutorial.spawn.z", (Object)p.getLocation().getZ());
                    this.plugin.saveConfig();
                    sender.sendMessage("\u00a7a\u30c1\u30e5\u30fc\u30c8\u30ea\u30a2\u30eb\u30b9\u30dd\u30fc\u30f3\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a\u3057\u307e\u3057\u305f\uff01");
                    sender.sendMessage("\u00a77\u00a7f/ba test \u00a77\u3067\u30c6\u30b9\u30c8\u5834\u3078\u3082\u8a2d\u5b9a\u3057\u3066\u304f\u3060\u3055\u3044");
                    break;
                }
                if (args.length >= 2 && ("stop".equalsIgnoreCase(args[1]) || "leave".equalsIgnoreCase(args[1]))) {
                    this.plugin.getTutorialManager().stop(p);
                    break;
                }
                if (args.length >= 2 && "next".equalsIgnoreCase(args[1])) {
                    this.plugin.getTutorialManager().advance(p);
                    break;
                }
                this.plugin.getTutorialManager().start(p);
                break;
            }
            case "oob": {
                if (!this.isAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f");
                    return true;
                }
                Player p = (Player)sender;
                if (this.plugin.getOobImmunePlayers().contains(p.getUniqueId())) {
                    this.plugin.getOobImmunePlayers().remove(p.getUniqueId());
                    sender.sendMessage("\u00a7c\u5834\u5916\u5224\u5b9a: \u6709\u52b9\uff08\u901a\u5e38\u901a\u308a\u8131\u843d\u3057\u307e\u3059\uff09");
                    break;
                }
                this.plugin.getOobImmunePlayers().add(p.getUniqueId());
                sender.sendMessage("\u00a7a\u5834\u5916\u5224\u5b9a: \u7121\u52b9\uff08\u5834\u5916\u3067\u3082\u8131\u843d\u3057\u307e\u305b\u3093\uff09");
                break;
            }
            case "bgm": {
                if (args.length < 2 || "gui".equalsIgnoreCase(args[1])) {
                    if (sender instanceof Player) {
                        Player p = (Player)sender;
                        this.plugin.getKitInfoGUI().openBgmList(p);
                    } else {
                        sender.sendMessage("\u00a77\u30b3\u30f3\u30bd\u30fc\u30eb\u304b\u3089\u306f /ba bgm list \u3067\u4e00\u89a7\u8868\u793a\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    }
                    return true;
                }
                String action = args[1].toLowerCase();
                if ("list".equals(action)) {
                    List<NbsPlayer> list = this.plugin.getSongs();
                    if (list.isEmpty()) {
                        sender.sendMessage("\u00a77BGM\u304c\u767b\u9332\u3055\u308c\u3066\u3044\u307e\u305b\u3093\u3002songs\u30d5\u30a9\u30eb\u30c0\u306b.nbs\u30d5\u30a1\u30a4\u30eb\u3092\u914d\u7f6e\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
                    } else {
                        sender.sendMessage("\u00a76\u00a7l=== BGM\u4e00\u89a7 (" + list.size() + "\u66f2) ===");
                        for (int i = 0; i < list.size(); ++i) {
                            String sel = list.get(i) == this.plugin.getGameManager().getSelectedBgm() ? " \u00a7a\u25c0 \u9078\u629e\u4e2d" : "";
                            sender.sendMessage("\u00a7e  " + (i + 1) + ". \u00a7f" + list.get(i).getName() + sel);
                        }
                    }
                    sender.sendMessage("\u00a77\u9078\u629e: \u00a7f/ba bgm <\u66f2\u540d>  \u00a77\u89e3\u9664: \u00a7f/ba bgm off");
                    return true;
                }
                if ("off".equals(action)) {
                    NbsPlayer cur = this.plugin.getGameManager().getSelectedBgm();
                    if (cur != null) {
                        cur.stop();
                    }
                    this.plugin.getGameManager().setSelectedBgm(null);
                    sender.sendMessage("\u00a7aBGM\u3092\u7121\u52b9\u306b\u3057\u307e\u3057\u305f\u3002");
                    return true;
                }
                String searchName = String.join((CharSequence)" ", Arrays.copyOfRange(args, 1, args.length));
                for (NbsPlayer song : this.plugin.getSongs()) {
                    if (!song.getName().equalsIgnoreCase(searchName)) continue;
                    this.plugin.getGameManager().setSelectedBgm(song);
                    sender.sendMessage("\u00a7a\u6b21\u8a66\u5408\u306eBGM: \u00a7e" + song.getName() + " \u00a7a\u3092\u9078\u629e\u3057\u307e\u3057\u305f\u3002");
                    return true;
                }
                sender.sendMessage("\u00a7c\u66f2\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093: " + searchName + " \u00a77(/ba bgm list \u3067\u4e00\u89a7\u8868\u793a)");
                break;
            }
            case "vote": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("\u00a7c\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u307f\u4f7f\u7528\u53ef\u80fd\u3067\u3059\u3002");
                    return true;
                }
                Player p = (Player)sender;
                if (args.length < 2) {
                    sender.sendMessage("\u00a77\u4f7f\u7528\u6cd5: /ba vote <1|2|3>");
                    return true;
                }
                int choice = this.parseInt(args[1], -1);
                this.plugin.getLobbyManager().castVote(p, choice);
                break;
            }
            case "version": {
                sender.sendMessage("\u00a76\u00a7lBloxArena II \u00a7f\u00a7lWarriors of NextGen");
                sender.sendMessage("\u00a77Version: \u00a7f" + this.plugin.getDescription().getVersion());
                sender.sendMessage("\u00a77Author: \u00a7fBloxArenaII");
                sender.sendMessage("\u00a77API: \u00a7f1.19.4");
                break;
            }
            default: {
                this.sendHelp(sender);
            }
        }
        return true;
    }

    private String modeFlags(MapConfig mc) {
        return (mc.isReadyFor(GameMode.BATTLE_ARENA) ? "\u00a7fBA " : "\u00a78BA ") + (mc.isReadyFor(GameMode.TEAM_DEATHMATCH) ? "\u00a7fTDM " : "\u00a78TDM ") + (mc.isReadyFor(GameMode.BOMB_MISSION) ? "\u00a7cB " : "\u00a78B ") + (mc.isReadyFor(GameMode.DOMINATION) ? "\u00a7eD " : "\u00a78D ") + (mc.isReadyFor(GameMode.CAPTURE_THE_FLAG) ? "\u00a79CTF " : "\u00a78CTF ") + (mc.isReadyFor(GameMode.FFA) ? "\u00a7fFFA" : "\u00a78FFA");
    }

    private String fmtNull(Location l) {
        if (l == null) {
            return "\u00a7c\u672a\u8a2d\u5b9a";
        }
        return String.format("\u00a7f(%.1f, %.1f, %.1f)", l.getX(), l.getY(), l.getZ());
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            return def;
        }
    }

    private boolean isAdmin(CommandSender s) {
        if (s.hasPermission("bloxarena.admin")) {
            return true;
        }
        s.sendMessage("\u00a7c\u3053\u306e\u30b3\u30de\u30f3\u30c9\u306b\u306f bloxarena.admin \u6a29\u9650\u304c\u5fc5\u8981\u3067\u3059\u3002");
        return false;
    }

    private String fmt(Location l) {
        if (l == null) {
            return "null";
        }
        return String.format("(%.1f, %.1f, %.1f)", l.getX(), l.getY(), l.getZ());
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage("\u00a76\u00a7l=== BAII WoNG \u30b3\u30de\u30f3\u30c9 ===");
        s.sendMessage("\u00a7e/ba wand \u00a77- \u30ef\u30f3\u30c9\u3092\u53d7\u3051\u53d6\u308b\uff082\u70b9\u9078\u629e\uff09");
        s.sendMessage("\u00a7e/ba setwaitingarea \u00a77- \u30ef\u30f3\u30c9\u9078\u629e\u7bc4\u56f2\u3092\u5f85\u6a5f\u30a8\u30ea\u30a2\u306b\u8a2d\u5b9a");
        s.sendMessage("\u00a7e/ba setlobby \u00a77- \u30ed\u30d3\u30fc\u30b9\u30dd\u30fc\u30f3\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
        s.sendMessage("\u00a7e/ba addmap <id> [world] \u00a77- \u65b0\u3057\u3044\u30de\u30c3\u30d7\u3092\u8ffd\u52a0");
        s.sendMessage("\u00a7e/ba info [mapId] \u00a77- \u30de\u30c3\u30d7\u60c5\u5831\u30fb\u4e00\u89a7\u8868\u793a");
        s.sendMessage("\u00a7e/ba setspawnzone <red|blue> <mapId> \u00a77- \u30ef\u30f3\u30c9\u9078\u629e\u7bc4\u56f2\u3092\u30b9\u30dd\u30fc\u30f3\u30be\u30fc\u30f3\u306b\u8a2d\u5b9a");
        s.sendMessage("\u00a7e/ba setcenter <mapId> \u00a77- \u4e2d\u592e\u30b3\u30f3\u30af\u30ea\u30fc\u30c8\u57fa\u6e96\u70b9\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
        s.sendMessage("\u00a7e/ba setmaplobby <mapId> \u00a77- \u30de\u30c3\u30d7\u306e\u30ed\u30d3\u30fc\u5730\u70b9\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
        s.sendMessage("\u00a7e/ba setmap <mapId> \u00a77- \u6b21\u306e\u8a66\u5408\u30de\u30c3\u30d7\u3092\u6307\u5b9a");
        s.sendMessage("\u00a7e/ba start \u00a77- \u8a66\u5408\u3092\u5f37\u5236\u958b\u59cb");
        s.sendMessage("\u00a7e/ba stop \u00a77- \u8a66\u5408\u3092\u5f37\u5236\u7d42\u4e86");
        s.sendMessage("\u00a7e/ba spectate \u00a77- \u9032\u884c\u4e2d\u8a66\u5408\u3092\u89b3\u6226");
        s.sendMessage("\u00a7e/ba reload \u00a77- config.yml \u3092\u30ea\u30ed\u30fc\u30c9");
        s.sendMessage("\u00a7e/ba status \u00a77- \u73fe\u5728\u306e\u72b6\u614b\u3092\u8868\u793a");
        s.sendMessage("\u00a7e/ba admin imigration \u00a77- \u30bb\u30c3\u30c8\u30a2\u30c3\u30d7\u30a6\u30a3\u30b6\u30fc\u30c9\u8d77\u52d5\uff08\u30ed\u30d3\u30fc+\u30de\u30c3\u30d71\u500b\u3092\u5bfe\u8a71\u8a2d\u5b9a\uff09");
        s.sendMessage("\u00a7e/ba kitedit \u00a77- \u30ad\u30c3\u30c8\u30a8\u30c7\u30a3\u30bf\u3092\u958b\u304f\uff08\u904b\u55b6\u9650\u5b9aGUI\uff09");
        s.sendMessage("\u00a7e/ba setgate <red|blue> <mapId> \u00a77- \u30b2\u30fc\u30c8\u9818\u57df\u3092\u30ef\u30f3\u30c9\u3067\u8a2d\u5b9a\uff08\u7e26\u6a2a\u5bfe\u5fdc\uff09");
        s.sendMessage("\u00a7e/ba gatematl <mapId> <\u7d20\u6750> \u00a77- \u30b2\u30fc\u30c8\u30d6\u30ed\u30c3\u30af\u7d20\u6750\u3092\u6307\u5b9a\uff08\u4f8b: BARRIER\uff09");
        s.sendMessage("\u00a7e/ba setoob <mapId|lobby> \u00a77- \u30a8\u30ea\u30a2\u5916\u5224\u5b9a\u30be\u30fc\u30f3\u3092\u30ef\u30f3\u30c9\u3067\u8a2d\u5b9a");
        s.sendMessage("\u00a7e/ba bot add [n] \u00a77- \u30c6\u30b9\u30c8\u7528BOT\u3092\u8ffd\u52a0\uff08\u8a66\u5408\u958b\u59cb\u524d\uff09");
        s.sendMessage("\u00a7e/ba bot clear \u00a77- BOT\u3092\u3059\u3079\u3066\u524a\u9664");
        s.sendMessage("\u00a7e/ba stats [player] \u00a77- \u7d71\u8a08\u3092\u8868\u793a");
        s.sendMessage("\u00a7e/ba mastery [player] \u00a77- \u30de\u30b9\u30bf\u30ea\u30fc\u6982\u8981\u3092\u8868\u793a");
        s.sendMessage("\u00a7e/ba top [kills|wins|kd|damage|kits] \u00a77- \u30e9\u30f3\u30ad\u30f3\u30b0\u8868\u793a");
        s.sendMessage("\u00a7e/ba continuous <on|off> \u00a77- \u9023\u7d9a\u8a66\u5408\u30e2\u30fc\u30c9\u5207\u308a\u66ff\u3048");
        s.sendMessage("\u00a7e/ba setmapname <mapId> <\u540d\u524d> \u00a77- \u30de\u30c3\u30d7\u306e\u8868\u793a\u540d\u3092\u8a2d\u5b9a");
        s.sendMessage("\u00a7e/ba upgrade <mapId> \u00a77- \u65e2\u5b58\u30de\u30c3\u30d7\u3092\u65b0\u30e2\u30fc\u30c9\u5bfe\u5fdc\u306b\u30a2\u30c3\u30d7\u30b0\u30ec\u30fc\u30c9");
        s.sendMessage("\u00a7e/ba convert \u00a77- \u65e7config\u3092\u65b0\u5f62\u5f0f\u306b\u81ea\u52d5\u5909\u63db");
        s.sendMessage("\u00a7e/ba setredflag <mapId> \u00a77- CTF\u8d64\u65d7\u521d\u671f\u4f4d\u7f6e\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a\uff08\u81ea\u9663\u5074\uff09");
        s.sendMessage("\u00a7e/ba setblueflag <mapId> \u00a77- CTF\u9752\u65d7\u521d\u671f\u4f4d\u7f6e\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a\uff08\u81ea\u9663\u5074\uff09");
        s.sendMessage("\u00a7e/ba setredreturn <mapId> \u00a77- CTF\u8d64\u6301\u3061\u5e30\u308a\u5730\u70b9\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
        s.sendMessage("\u00a7e/ba setbluereturn <mapId> \u00a77- CTF\u9752\u6301\u3061\u5e30\u308a\u5730\u70b9\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
        s.sendMessage("\u00a7e/ba setbombplant <mapId> \u00a77- \u7206\u7834\u8a2d\u7f6e\u5730\u70b9\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
        s.sendMessage("\u00a7e/ba setbombdefuse <mapId> \u00a77- \u7206\u7834\u89e3\u9664\u5730\u70b9\u3092\u73fe\u5728\u5730\u306b\u8a2d\u5b9a");
        s.sendMessage("\u00a7e/ba test [leave] \u00a77- \u30c6\u30b9\u30c8\u5834\u306b\u5165\u308b/\u9000\u51fa");
        s.sendMessage("\u00a7e/ba kits \u00a77- \u30ad\u30c3\u30c8\u4e00\u89a7\u3092\u8868\u793a\uff08\u8ab0\u3067\u3082\u4f7f\u7528\u53ef\u80fd\uff09");
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "wand", "setwaitingarea", "setlobby", "addmap", "info", "setspawnzone", "setcenter", "setmaplobby", "setmap", "setmapname", "kitedit", "kits", "bot", "stats", "mastery", "top", "continuous", "setgate", "gatematl", "setoob", "spectate", "reload", "status", "admin", "upgrade", "convert", "test", "debug", "tutorial", "oob", "bgm", "vote", "version");
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "setspawnzone" -> Arrays.asList("red", "blue");
                case "admin" -> Arrays.asList("imigration", "addmap", "next", "cancel", "skip");
                case "top" -> Arrays.asList("kills", "wins", "kd", "damage", "kits");
                case "bot" -> Arrays.asList("add", "clear", "list");
                case "continuous" -> Arrays.asList("on", "off");
                case "vote" -> Arrays.asList("1", "2", "3");
                case "tutorial" -> Arrays.asList("setup", "stop", "leave", "next");
                case "oob" -> Collections.emptyList();
                case "setgate" -> Arrays.asList("red", "blue");
                case "bgm" -> {
                    ArrayList<String> names = new ArrayList<String>(Arrays.asList("list", "off", "gui"));
                    for (NbsPlayer song : this.plugin.getSongs()) {
                        names.add(song.getName());
                    }
                    yield names;
                }
                case "setoob" -> {
                    List ids = this.plugin.getMapManager().getMaps().stream().map(MapConfig::getId).collect(Collectors.toList());
                    ids.add("lobby");
                    yield ids;
                }
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }
}

