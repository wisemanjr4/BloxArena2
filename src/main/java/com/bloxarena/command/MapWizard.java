package com.bloxarena.command;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.game.GameMode;
import com.bloxarena.map.MapConfig;
import com.bloxarena.util.SelectionTool;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * /ba admin addmap <mapId> で起動（新規マップ作成）
 * /ba upgrade <mapId> で起動（既存マップの新モード対応追加）
 *
 * 新規: 8ステップ（必須4＋任意4）
 *   [必須] ①赤スポーン ②青スポーン ③センター ④マップロビー
 *   [任意] ⑤赤ゲート ⑥青ゲート ⑦ゲート素材 ⑧マップOOB
 *
 * アップグレード: 不足している新モード設定のみ案内
 *   爆破: 設置地点・解除地点
 *   CTF:  赤旗・青旗・赤帰還・青帰還
 *   占領: 拠点位置（複数追加可 / skipで終了）
 *
 * 操作: /ba admin next = 確定 /ba admin skip = スキップ /ba admin cancel = 中断
 */
public class MapWizard {

    private static final int LAST_STEP = 7;
    private static final int OPTIONAL_FROM = 4;

    private final BloxArenaPlugin plugin;

    private static class State {
        String mapId;
        int    step         = 0;
        boolean redGateSet  = false;
        boolean blueGateSet = false;
        boolean isUpgrade   = false;

        State(String mapId) { this.mapId = mapId; }
    }

    private final Map<UUID, State> sessions = new HashMap<>();

    public MapWizard(BloxArenaPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── 公開API ───

    public boolean isActive(Player p) { return sessions.containsKey(p.getUniqueId()); }

    /**
     * @param mapId 新規作成するマップID
     */
    public void start(Player p, String mapId) {
        if (isActive(p)) {
            p.sendMessage("§cマップウィザードはすでに起動中です。 §f/ba admin next §c で次へ進めてください。");
            return;
        }
        if (plugin.getMapManager().getById(mapId) != null) {
            p.sendMessage("§cマップ §e" + mapId + " §cはすでに存在します。別のIDを指定してください。");
            return;
        }
        plugin.getMapManager().addMap(mapId, p.getWorld().getName());
        State s = new State(mapId);
        sessions.put(p.getUniqueId(), s);

        header(p);
        p.sendMessage("§6§lマップ §e§l" + mapId + " §6§lを作成します");
        p.sendMessage("§7必須4＋任意4の全8ステップです。");
        p.sendMessage("§7  §f/ba admin next §7= 確定　§f/ba admin skip §7= 任意をスキップ　§f/ba admin cancel §7= 中断");
        p.sendMessage("§7  §7§oワンドが必要です → /ba wand");
        footer(p);
        showStep(p, s);
    }

    /**
     * 既存マップのアップグレードウィザード（新モード対応の不足設定を順番に案内）
     */
    public void startUpgrade(Player p, String mapId) {
        MapConfig mc = plugin.getMapManager().getById(mapId);
        if (mc == null) {
            p.sendMessage("§cマップ '" + mapId + "' が見つかりません。"); return;
        }
        if (mc.isReadyFor(GameMode.BOMB_MISSION)
                && mc.isReadyFor(GameMode.DOMINATION)
                && mc.isReadyFor(GameMode.CAPTURE_THE_FLAG)) {
            p.sendMessage("§aこのマップは既に全モードに対応しています。");
            return;
        }
        State s = new State(mapId);
        s.isUpgrade = true;
        sessions.put(p.getUniqueId(), s);
        header(p);
        p.sendMessage("§6§lマップ §e" + mapId + " §6§lのアップグレード");
        p.sendMessage("§7新モード（爆破・CTF・占領）に必要な設定を順に案内します。");
        p.sendMessage("§7  §f/ba admin next §7= 確定　§f/ba admin skip §7= スキップ　§f/ba admin cancel §7= 中断");
        footer(p);
        showUpgradeStep(p, s);
    }

    public void next(Player p) {
        State s = sessions.get(p.getUniqueId());
        if (s == null) { p.sendMessage("§c/ba admin addmap <id> でウィザードを開始してください。"); return; }
        if (applyStep(p, s)) advance(p, s);
    }

    public void skip(Player p) {
        State s = sessions.get(p.getUniqueId());
        if (s == null) { p.sendMessage("§cウィザードが起動していません。"); return; }
        if (s.isUpgrade) {
            p.sendMessage("§7ステップをスキップしました。");
            advance(p, s);
            return;
        }
        if (s.step < OPTIONAL_FROM) {
            p.sendMessage("§c必須ステップはスキップできません。 §f/ba admin next §c で進んでください。");
            return;
        }
        p.sendMessage("§7ステップをスキップしました。");
        advance(p, s);
    }

    public void cancel(Player p) {
        State s = sessions.remove(p.getUniqueId());
        if (s != null) {
            p.sendMessage("§cウィザードをキャンセルしました。");
        } else {
            p.sendMessage("§c起動中のウィザードがありません。");
        }
    }

    private void showUpgradeStep(Player p, State s) {
        MapConfig mc = plugin.getMapManager().getById(s.mapId);
        if (mc == null) { cancel(p); return; }
        
        if (!mc.isReadyFor(GameMode.BOMB_MISSION)) {
            if (mc.getBombSite() == null) {
                header(p); p.sendMessage("§6§l【爆破モード】§e 必須 §6- 爆弾設置地点");
                p.sendMessage("§7攻撃側が爆弾を設置する場所です。その地点に立ってください。");
                p.sendMessage("§f/ba admin next §7= 現在地を設定　§f/ba admin skip §7= 爆破モードを飛ばす");
                footer(p); return;
            }
            if (mc.getDefusePoint() == null) {
                header(p); p.sendMessage("§6§l【爆破モード】§e 必須 §6- 爆弾解除地点");
                p.sendMessage("§7守備側が爆弾を解除する場所です。その地点に立ってください。");
                p.sendMessage("§f/ba admin next §7= 現在地を設定");
                footer(p); return;
            }
        }
        if (!mc.isReadyFor(GameMode.CAPTURE_THE_FLAG)) {
            if (mc.getRedFlagLocation() == null) {
                header(p); p.sendMessage("§6§l【CTF】§e 必須 §6- §c赤旗 §6初期位置（相手陣地）");
                p.sendMessage("§7§c赤チーム§7が奪いに行く旗です。§9青陣地側§7に設置します。");
                p.sendMessage("§7設置したい場所に立って §f/ba admin next　§f/ba admin skip §7= CTFを飛ばす");
                footer(p); return;
            }
            if (mc.getBlueFlagLocation() == null) {
                header(p); p.sendMessage("§6§l【CTF】§e 必須 §6- §9青旗 §6初期位置（自陣）");
                p.sendMessage("§7自陣に置く旗です。§c赤チーム§7が奪いに来ます。");
                p.sendMessage("§7設置したい場所に立って §f/ba admin next");
                footer(p); return;
            }
            if (mc.getRedReturnLocation() == null) {
                header(p); p.sendMessage("§6§l【CTF】§e 必須 §6- §c赤 §6持ち帰り地点");
                p.sendMessage("§7§c赤チーム§7が相手の§9青旗§7を持ち帰るゴール地点です。");
                p.sendMessage("§f/ba admin next §7= 現在地を設定");
                footer(p); return;
            }
            if (mc.getBlueReturnLocation() == null) {
                header(p); p.sendMessage("§6§l【CTF】§e 必須 §6- §9青 §6持ち帰り地点");
                p.sendMessage("§7§9青チーム§7が相手の§c赤旗§7を持ち帰るゴール地点です。");
                p.sendMessage("§f/ba admin next §7= 現在地を設定");
                footer(p); return;
            }
        }
        if (!mc.isReadyFor(GameMode.DOMINATION)) {
            header(p); p.sendMessage("§6§l【占領戦】§7 任意 §6- 占領拠点の追加");
            p.sendMessage("§7占領拠点の中心に立ってください。半径5mで設定されます。");
            p.sendMessage("§f/ba admin next §7= 拠点を追加（何個でもOK）　§f/ba admin skip §7= 終了");
            footer(p); return;
        }
        finish(p, s);
        sessions.remove(p.getUniqueId());
    }

    // ─── ナビゲーション ───

    private void advance(Player p, State s) {
        if (s.isUpgrade) {
            showUpgradeStep(p, s);
            return;
        }
        // 両ゲートとも未設定ならゲートブロックステップ(6)をスキップ
        if (s.step == 5 && !s.redGateSet && !s.blueGateSet) {
            s.step = 6;
        }
        s.step++;
        if (s.step > LAST_STEP) { finish(p, s); sessions.remove(p.getUniqueId()); }
        else showStep(p, s);
    }

    // ─── ステップ表示 ───

    private void showStep(Player p, State s) {
        header(p);
        switch (s.step) {
            // ── 必須 ──
            case 0 -> {
                p.sendMessage("§6§l【1/8】§e 必須 §6- §c赤チーム §6スポーンゾーン（範囲選択）");
                p.sendMessage("§7§c赤チーム§7の出現エリアをワンドで囲んでください。");
                p.sendMessage("§7§l左クリック§r§7=始点　§7§l右クリック§r§7=終点　→ §f/ba admin next");
            }
            case 1 -> {
                p.sendMessage("§6§l【2/8】§e 必須 §6- §9青チーム §6スポーンゾーン（範囲選択）");
                p.sendMessage("§7§9青チーム§7の出現エリアをワンドで囲んでください。");
                p.sendMessage("§7選択後 → §f/ba admin next");
            }
            case 2 -> {
                p.sendMessage("§6§l【3/8】§e 必須 §6- 中央コンクリート基準点");
                p.sendMessage("§7バトルアリーナ中央（コンクリート中心）に立ってください。");
                p.sendMessage("§f/ba admin next §7で現在地を設定");
            }
            case 3 -> {
                p.sendMessage("§6§l【4/8】§e 必須 §6- 観戦リスポーン地点");
                p.sendMessage("§7脱落したプレイヤーが観戦モードで飛ばされる場所です。");
                p.sendMessage("§f/ba admin next §7で現在地を設定");
            }
            // ── 任意 ──
            case 4 -> {
                p.sendMessage("§6§l【5/8】§7 任意 §6- §c赤チーム§6 スタートゲート（範囲選択）");
                p.sendMessage("§7試合開始時に封鎖する§c赤§7側の領域をワンドで囲んでください。");
                p.sendMessage("§7選択後: §f/ba admin next　　不要なら: §f/ba admin skip");
            }
            case 5 -> {
                p.sendMessage("§6§l【6/8】§7 任意 §6- §9青チーム§6 スタートゲート（範囲選択）");
                p.sendMessage("§7§9青§7側の封鎖領域をワンドで囲んでください。");
                p.sendMessage("§7選択後: §f/ba admin next　　不要なら: §f/ba admin skip");
            }
            case 6 -> {
                MapConfig mc = plugin.getMapManager().getById(s.mapId);
                String mat = mc != null ? mc.getGateMaterial().name() : "BARRIER";
                p.sendMessage("§6§l【7/8】§7 任意 §6- ゲート素材の確認");
                p.sendMessage("§7スタートゲートのブロック素材（現在: §e" + mat + "§7）");
                p.sendMessage("§7変更: §f/ba gatematl " + s.mapId + " <素材名>");
                p.sendMessage("§7  §8例: §7§o/ba gatematl " + s.mapId + " IRON_BARS");
                p.sendMessage("§7確定: §f/ba admin next　　不要なら: §f/ba admin skip");
            }
            case 7 -> {
                p.sendMessage("§6§l【8/8】§7 任意 §6- 場外判定ゾーン（範囲選択）");
                p.sendMessage("§7試合中の移動許可範囲です。エリア外に出ると脱落します。");
                p.sendMessage("§7選択後: §f/ba admin next　　不要なら: §f/ba admin skip");
            }
        }
        if (s.step >= OPTIONAL_FROM) p.sendMessage("§8スキップ: §f/ba admin skip");
        p.sendMessage("§8キャンセル: §f/ba admin cancel");
        footer(p);
    }

    // ─── ステップ実行 ───

    private boolean applyStep(Player p, State s) {
        SelectionTool tool = plugin.getSelectionTool();
        MapConfig mc = plugin.getMapManager().getById(s.mapId);
        if (mc == null) return true;

        if (s.isUpgrade) {
            if (!mc.isReadyFor(GameMode.BOMB_MISSION)) {
                if (mc.getBombSite() == null) { mc.setBombSite(p.getLocation()); plugin.getMapManager().saveMap(mc); p.sendMessage("§a✔ 爆弾設置地点を設定しました"); return true; }
                if (mc.getDefusePoint() == null) { mc.setDefusePoint(p.getLocation()); plugin.getMapManager().saveMap(mc); p.sendMessage("§a✔ 爆弾解除地点を設定しました"); return true; }
            }
            if (!mc.isReadyFor(GameMode.CAPTURE_THE_FLAG)) {
                if (mc.getRedFlagLocation() == null) { mc.setRedFlagLocation(p.getLocation()); plugin.getMapManager().saveMap(mc); p.sendMessage("§a✔ §c赤旗§a地点を設定しました（§c自陣§aに置いてください）"); return true; }
                if (mc.getBlueFlagLocation() == null) { mc.setBlueFlagLocation(p.getLocation()); plugin.getMapManager().saveMap(mc); p.sendMessage("§a✔ §9青旗§a地点を設定しました（§9自陣§aに置いてください）"); return true; }
                if (mc.getRedReturnLocation() == null) { mc.setRedReturnLocation(p.getLocation()); plugin.getMapManager().saveMap(mc); p.sendMessage("§a✔ §c赤§a持ち帰り地点を設定しました"); return true; }
                if (mc.getBlueReturnLocation() == null) { mc.setBlueReturnLocation(p.getLocation()); plugin.getMapManager().saveMap(mc); p.sendMessage("§a✔ §9青§a持ち帰り地点を設定しました"); return true; }
            }
            if (!mc.isReadyFor(GameMode.DOMINATION)) {
                mc.addDomPoint(new MapConfig.DomPoint(p.getLocation(), 5.0));
                plugin.getMapManager().saveMap(mc);
                p.sendMessage("§a✔ 占領拠点を追加しました（半径5m）。続けて追加できます。");
            }
            return true;
        }

        switch (s.step) {
            case 0: // 赤スポーン
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択してください。"); return false; }
                mc.setRedSpawnMin(tool.getMin(p)); mc.setRedSpawnMax(tool.getMax(p));
                plugin.getMapManager().saveMap(mc);
                p.sendMessage("§a✔ §c赤チーム §aスポーンゾーンを設定しました。");
                return true;

            case 1: // 青スポーン
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択してください。"); return false; }
                mc.setBlueSpawnMin(tool.getMin(p)); mc.setBlueSpawnMax(tool.getMax(p));
                plugin.getMapManager().saveMap(mc);
                p.sendMessage("§a✔ §9青チーム §aスポーンゾーンを設定しました。");
                return true;

            case 2: // センター
                mc.setCenter(p.getLocation());
                plugin.getMapManager().saveMap(mc);
                p.sendMessage("§a✔ 中央基準点を設定しました: " + fmtLoc(p.getLocation()));
                return true;

            case 3: // マップロビー
                mc.setLobby(p.getLocation());
                plugin.getMapManager().saveMap(mc);
                p.sendMessage("§a✔ マップロビー地点を設定しました: " + fmtLoc(p.getLocation()));
                return true;

            case 4: // 赤ゲート (任意)
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択するか §f/ba admin skip §c でスキップしてください。"); return false; }
                mc.setRedGateMin(tool.getMin(p)); mc.setRedGateMax(tool.getMax(p));
                plugin.getMapManager().saveMap(mc);
                s.redGateSet = true;
                p.sendMessage("§a✔ §c赤チーム §aゲート領域を設定しました。");
                return true;

            case 5: // 青ゲート (任意)
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択するか §f/ba admin skip §c でスキップしてください。"); return false; }
                mc.setBlueGateMin(tool.getMin(p)); mc.setBlueGateMax(tool.getMax(p));
                plugin.getMapManager().saveMap(mc);
                s.blueGateSet = true;
                p.sendMessage("§a✔ §9青チーム §aゲート領域を設定しました。");
                return true;

            case 6: // ゲートブロック (任意)
                String matName = mc != null ? mc.getGateMaterial().name() : "BARRIER";
                p.sendMessage("§a✔ ゲートブロック: §e" + matName + " §aで確定しました。");
                return true;

            case 7: // マップOOB (任意)
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択するか §f/ba admin skip §c でスキップしてください。"); return false; }
                mc.setOobMin(tool.getMin(p)); mc.setOobMax(tool.getMax(p));
                plugin.getMapManager().saveMap(mc);
                p.sendMessage("§a✔ マップOOBゾーンを設定しました。");
                return true;

            default: return true;
        }
    }

    // ─── 完了画面 ───

    private void finish(Player p, State s) {
        MapConfig mc = plugin.getMapManager().getById(s.mapId);
        header(p);
        p.sendMessage("§a§l✔ マップ §e§l" + s.mapId + " §a§lの設定が完了しました");
        p.sendMessage("");
        if (mc != null) {
            p.sendMessage("§7状態:        " + (mc.isReady() ? "§a全モード対応完了" : "§e基本設定完了（不足モードあり）"));
            p.sendMessage("§7ゲート(§c赤§7):   " + (s.redGateSet  ? "§a設定済" : "§7なし"));
            p.sendMessage("§7ゲート(§9青§7):   " + (s.blueGateSet ? "§a設定済" : "§7なし"));
            p.sendMessage("§7場外判定:    " + (mc.hasOob()   ? "§a設定済" : "§7なし"));
        }
        p.sendMessage("");
        p.sendMessage("§6【次のステップ】");
        p.sendMessage("§7  マップ確認:     §f/ba info " + s.mapId);
        p.sendMessage("§7  不足を追加:     §f/ba upgrade " + s.mapId);
        p.sendMessage("§7  CTF旗の設定:   §f/ba setredflag " + s.mapId);
        p.sendMessage("§7  爆破地点の設定: §f/ba setbombplant " + s.mapId);
        p.sendMessage("§7  別のマップ追加: §f/ba admin addmap <id>");
        footer(p);
    }

    // ─── UI ヘルパー ───

    private void header(Player p) { p.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"); }
    private void footer(Player p) { p.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"); }

    private String fmtLoc(Location l) {
        return String.format("§f(%d,%d,%d)§7[§f%s§7]",
            l.getBlockX(), l.getBlockY(), l.getBlockZ(),
            l.getWorld() != null ? l.getWorld().getName() : "?");
    }
}
