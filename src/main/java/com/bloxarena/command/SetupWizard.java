package com.bloxarena.command;

import com.bloxarena.BloxArenaPlugin;
import com.bloxarena.map.MapConfig;
import com.bloxarena.util.SelectionTool;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * /ba admin imigration で起動するマップ初期設定ウィザード。
 *
 * 12ステップで新規マップを設定します。
 *   [必須] ①ロビースポーン ②待機エリア ③マップ名 ④赤スポーン ⑤青スポーン ⑥センター ⑦マップロビー
 *   [任意] ⑧赤ゲート ⑨青ゲート ⑩ゲート素材 ⑪マップOOB ⑫ロビーOOB
 *
 * 操作:
 *   /ba admin next   → 現在のステップを確定して次へ
 *   /ba admin skip   → 任意ステップをスキップ（必須は飛ばせません）
 *   /ba admin cancel → 中断
 *
 * ポイント:
 *   ・スポーンゾーンとOOBゾーンは「ワンド」で範囲選択が必要です(/ba wand で取得)
 *   ・それ以外の地点は現在地に立って /ba admin next で設定します
 *   ・設定後も /ba setredflag など個別コマンドで変更可能です
 */
public class SetupWizard {

    private static final String BASE_MAP_ID = "arena1";

    private final BloxArenaPlugin plugin;

    private static class State {
        int    step           = 0;
        String mapId          = null;
        boolean redGateSet    = false;
        boolean blueGateSet   = false;
        // ゲートブロック素材 (テキスト入力代わりに pending として保持)
        String  pendingMaterial = null;
    }

    private final Map<UUID, State> sessions = new HashMap<>();

    public SetupWizard(BloxArenaPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── 公開API ───

    public boolean isActive(Player p) { return sessions.containsKey(p.getUniqueId()); }

    public void start(Player p) {
        if (isActive(p)) {
            p.sendMessage("§cウィザードはすでに起動中です。 §f/ba admin next §c で次へ進めてください。");
            return;
        }
        sessions.put(p.getUniqueId(), new State());
        header(p);
        p.sendMessage("§6§lBAII WoNG マップ初期設定ウィザード");
        p.sendMessage("§7全12ステップ（必須7＋任意5）で新規マップを作成します。");
        p.sendMessage("§7  §f/ba admin next §7= 確定して次へ");
        p.sendMessage("§7  §f/ba admin skip §7= 任意ステップのみスキップ");
        p.sendMessage("§7  §f/ba admin cancel §7= 中断");
        p.sendMessage("§7  §7§oワンドが必要なステップでは /ba wand で取得してください");
        footer(p);
        showStep(p, sessions.get(p.getUniqueId()));
    }

    public void next(Player p) {
        State s = sessions.get(p.getUniqueId());
        if (s == null) { p.sendMessage("§c/ba admin imigration でウィザードを開始してください。"); return; }
        if (applyStep(p, s)) advance(p, s);
    }

    public void skip(Player p) {
        State s = sessions.get(p.getUniqueId());
        if (s == null) { p.sendMessage("§c/ba admin imigration でウィザードを開始してください。"); return; }
        if (s.step < 7) {
            p.sendMessage("§c必須ステップはスキップできません。 §f/ba admin next §c で進んでください。");
            return;
        }
        p.sendMessage("§7ステップをスキップしました。");
        advance(p, s);
    }

    public void cancel(Player p) {
        if (sessions.remove(p.getUniqueId()) != null)
            p.sendMessage("§cセットアップをキャンセルしました。 §f/ba admin imigration §c で再開できます。");
        else
            p.sendMessage("§c起動中のウィザードがありません。");
    }

    // ─── ナビゲーション ───

    private void advance(Player p, State s) {
        // ゲートを一切設定しなかった場合はゲートブロックステップ(9)をスキップ
        if (s.step == 8 && !s.redGateSet && !s.blueGateSet) {
            s.step = 9; // ゲートブロックをスキップ
        }
        s.step++;
        if (s.step > LAST_STEP) { finish(p, s); sessions.remove(p.getUniqueId()); }
        else showStep(p, s);
    }

    private static final int LAST_STEP = 11;

    // ─── ステップ表示 ───

    private void showStep(Player p, State s) {
        header(p);
        switch (s.step) {
            // ── 必須 ──
            case 0 -> {
                p.sendMessage("§6§l【1/12】§e 必須 §6- ロビースポーン地点");
                p.sendMessage("§7試合待機中にプレイヤーが集まる場所です。");
                p.sendMessage("§7ロビーの中心に移動して §f/ba admin next §7を実行してください。");
            }
            case 1 -> {
                p.sendMessage("§6§l【2/12】§e 必須 §6- 待機エリア（範囲選択）");
                p.sendMessage("§7試合開始までプレイヤーが自由に動けるエリアです。");
                p.sendMessage("§7  §7§l左クリック§r§7=始点　§7§l右クリック§r§7=終点");
                p.sendMessage("§7ワンドがない場合: §f/ba wand");
                p.sendMessage("§7範囲を選択したら §f/ba admin next");
            }
            case 2 -> {
                p.sendMessage("§6§l【3/12】§e 必須 §6- マップ作成");
                p.sendMessage("§7現在のワールド §e" + p.getWorld().getName() + " §7上に新規マップを作成します。");
                p.sendMessage("§7マップIDは自動採番されます（arena1, arena2...）。");
                p.sendMessage("§f/ba admin next §7で確定");
            }
            case 3 -> {
                p.sendMessage("§6§l【4/12】§e 必須 §6- §c赤チーム §6スポーンゾーン（範囲選択）");
                p.sendMessage("§7§c赤チーム§7のプレイヤーが試合開始時に出現するエリアです。");
                p.sendMessage("§7ワンドで範囲を囲んで §f/ba admin next");
            }
            case 4 -> {
                p.sendMessage("§6§l【5/12】§e 必須 §6- §9青チーム §6スポーンゾーン（範囲選択）");
                p.sendMessage("§7§9青チーム§7のプレイヤーが試合開始時に出現するエリアです。");
                p.sendMessage("§7ワンドで範囲を囲んで §f/ba admin next");
            }
            case 5 -> {
                p.sendMessage("§6§l【6/12】§e 必須 §6- 中央コンクリート基準点");
                p.sendMessage("§7バトルアリーナの中央（コンクリートを敷き詰める中心地点）です。");
                p.sendMessage("§7アリーナ中央に立って §f/ba admin next §7で現在地を設定");
            }
            case 6 -> {
                p.sendMessage("§6§l【7/12】§e 必須 §6- マップ観戦リスポーン地点");
                p.sendMessage("§7試合中に脱落したプレイヤーが観戦モードで飛ばされる場所です。");
                p.sendMessage("§7普段はアリーナ中央上空が適しています。");
                p.sendMessage("§7設定したい場所に立って §f/ba admin next");
            }
            // ── 任意 ──
            case 7 -> {
                p.sendMessage("§6§l【8/12】§7 任意 §6- §c赤チーム§6 スタートゲート（範囲選択）");
                p.sendMessage("§7試合開始時に封鎖する壁や床の領域です。");
                p.sendMessage("§7§c赤チーム§7側の封鎖したい範囲をワンドで囲んでください。");
                p.sendMessage("§7選択後: §f/ba admin next　　不要なら: §f/ba admin skip");
            }
            case 8 -> {
                p.sendMessage("§6§l【9/12】§7 任意 §6- §9青チーム§6 スタートゲート（範囲選択）");
                p.sendMessage("§7§9青チーム§7側の封鎖したい範囲をワンドで囲んでください。");
                p.sendMessage("§7選択後: §f/ba admin next　　不要なら: §f/ba admin skip");
            }
            case 9 -> {
                p.sendMessage("§6§l【10/12】§7 任意 §6- ゲート素材の確認");
                String currentMat = (s.mapId != null && plugin.getMapManager().getById(s.mapId) != null
                        ? plugin.getMapManager().getById(s.mapId).getGateMaterial().name()
                        : "BARRIER");
                p.sendMessage("§7スタートゲートのブロック素材（現在: §e" + currentMat + "§7）");
                p.sendMessage("§7変更したい場合: §f/ba gatematl " + (s.mapId != null ? s.mapId : "<mapId>") + " <素材名>");
                p.sendMessage("§7  §8例: §7§o/ba gatematl " + (s.mapId != null ? s.mapId : "arena1") + " IRON_BARS");
                p.sendMessage("§7このままでよければ §f/ba admin next　　不要なら: §f/ba admin skip");
            }
            case 10 -> {
                p.sendMessage("§6§l【11/12】§7 任意 §6- 場外判定ゾーン（範囲選択）");
                p.sendMessage("§7試合中にプレイヤーが移動できる範囲です。エリア外に出ると脱落します。");
                p.sendMessage("§7ワンドで範囲を囲んで §f/ba admin next　　不要なら: §f/ba admin skip");
            }
            case 11 -> {
                p.sendMessage("§6§l【12/12】§7 任意 §6- ロビー場外判定ゾーン（範囲選択）");
                p.sendMessage("§7待機中にプレイヤーが移動できる範囲です。エリア外に出るとロビーに戻されます。");
                p.sendMessage("§7ワンドで範囲を囲んで §f/ba admin next　　不要なら: §f/ba admin skip");
            }
        }
        if (s.step >= 7) p.sendMessage("§8スキップ: §f/ba admin skip");
        p.sendMessage("§8キャンセル: §f/ba admin cancel");
        footer(p);
    }

    // ─── ステップ実行 ───

    /** @return true=成功して次へ / false=条件未達、留まる */
    private boolean applyStep(Player p, State s) {
        SelectionTool tool = plugin.getSelectionTool();
        MapConfig mc;

        switch (s.step) {
            case 0: // ロビーSP
                plugin.getLobbyManager().setLobbySpawn(p.getLocation());
                p.sendMessage("§a✔ ロビースポーンを設定しました: " + fmtLoc(p.getLocation()));
                return true;

            case 1: // 待機エリア
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択してください。"); return false; }
                plugin.getLobbyManager().setWaitingAreaMin(tool.getMin(p));
                plugin.getLobbyManager().setWaitingAreaMax(tool.getMax(p));
                p.sendMessage("§a✔ 待機エリアを設定しました。");
                return true;

            case 2: // マップ作成
                String id = BASE_MAP_ID;
                int n = 1;
                while (plugin.getMapManager().getById(id) != null) id = BASE_MAP_ID + (n++);
                s.mapId = id;
                plugin.getMapManager().addMap(id, p.getWorld().getName());
                p.sendMessage("§a✔ マップ §e" + id + " §aを作成しました。");
                return true;

            case 3: // 赤スポーン
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択してください。"); return false; }
                mc = plugin.getMapManager().getById(s.mapId);
                mc.setRedSpawnMin(tool.getMin(p)); mc.setRedSpawnMax(tool.getMax(p));
                plugin.getMapManager().saveMap(mc);
                p.sendMessage("§a✔ §c赤チーム §aスポーンゾーンを設定しました。");
                return true;

            case 4: // 青スポーン
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択してください。"); return false; }
                mc = plugin.getMapManager().getById(s.mapId);
                mc.setBlueSpawnMin(tool.getMin(p)); mc.setBlueSpawnMax(tool.getMax(p));
                plugin.getMapManager().saveMap(mc);
                p.sendMessage("§a✔ §9青チーム §aスポーンゾーンを設定しました。");
                return true;

            case 5: // センター
                mc = plugin.getMapManager().getById(s.mapId);
                mc.setCenter(p.getLocation());
                plugin.getMapManager().saveMap(mc);
                p.sendMessage("§a✔ 中央基準点を設定しました: " + fmtLoc(p.getLocation()));
                return true;

            case 6: // マップロビー
                mc = plugin.getMapManager().getById(s.mapId);
                mc.setLobby(p.getLocation());
                plugin.getMapManager().saveMap(mc);
                p.sendMessage("§a✔ マップロビー地点を設定しました: " + fmtLoc(p.getLocation()));
                return true;

            case 7: // 赤ゲート (任意)
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択するか §f/ba admin skip §c でスキップしてください。"); return false; }
                mc = plugin.getMapManager().getById(s.mapId);
                mc.setRedGateMin(tool.getMin(p)); mc.setRedGateMax(tool.getMax(p));
                plugin.getMapManager().saveMap(mc);
                s.redGateSet = true;
                p.sendMessage("§a✔ §c赤チーム §aゲート領域を設定しました。");
                return true;

            case 8: // 青ゲート (任意)
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択するか §f/ba admin skip §c でスキップしてください。"); return false; }
                mc = plugin.getMapManager().getById(s.mapId);
                mc.setBlueGateMin(tool.getMin(p)); mc.setBlueGateMax(tool.getMax(p));
                plugin.getMapManager().saveMap(mc);
                s.blueGateSet = true;
                p.sendMessage("§a✔ §9青チーム §aゲート領域を設定しました。");
                return true;

            case 9: // ゲートブロック (任意)
                // このステップは next を押すだけで「現在の設定を確認して続行」
                mc = plugin.getMapManager().getById(s.mapId);
                String matName = mc != null ? mc.getGateMaterial().name() : "BARRIER";
                p.sendMessage("§a✔ ゲートブロック: §e" + matName + " §aで確定しました。");
                return true;

            case 10: // マップOOB (任意)
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択するか §f/ba admin skip §c でスキップしてください。"); return false; }
                mc = plugin.getMapManager().getById(s.mapId);
                mc.setOobMin(tool.getMin(p)); mc.setOobMax(tool.getMax(p));
                plugin.getMapManager().saveMap(mc);
                p.sendMessage("§a✔ マップOOBゾーンを設定しました。");
                return true;

            case 11: // ロビーOOB (任意)
                if (!tool.hasSelection(p)) { p.sendMessage("§cワンドで2点を選択するか §f/ba admin skip §c でスキップしてください。"); return false; }
                plugin.getLobbyManager().setLobbyOob(tool.getMin(p), tool.getMax(p));
                p.sendMessage("§a✔ ロビーOOBゾーンを設定しました。");
                return true;

            default: return true;
        }
    }

    // ─── 完了画面 ───

    private void finish(Player p, State s) {
        MapConfig mc = s.mapId != null ? plugin.getMapManager().getById(s.mapId) : null;
        header(p);
        p.sendMessage("§a§l✔ 初期設定が完了しました！");
        p.sendMessage("");
        p.sendMessage("§7ロビースポーン:     §a設定済");
        p.sendMessage("§7待機エリア:         §a設定済");
        if (mc != null) {
            p.sendMessage("§7マップ §e" + s.mapId + "§7:     " + (mc.isReady() ? "§a準備完了（全モード対応）" : "§e基本設定完了（一部モード要追加設定）"));
            p.sendMessage("§7ゲート(§c赤§7):        " + (s.redGateSet  ? "§a設定済" : "§7スキップ"));
            p.sendMessage("§7ゲート(§9青§7):        " + (s.blueGateSet ? "§a設定済" : "§7スキップ"));
            p.sendMessage("§7場外判定:           " + (mc.hasOob()   ? "§a設定済" : "§7スキップ"));
        }
        p.sendMessage("§7ロビー場外判定:     " + (plugin.getLobbyManager().hasLobbyOob() ? "§a設定済" : "§7スキップ"));
        p.sendMessage("");
        p.sendMessage("§6【次のステップ】");
        if (s.mapId != null) p.sendMessage("§7  マップ確認:      §f/ba info " + s.mapId);
        p.sendMessage("§7  CTF旗の設置:     §f/ba setredflag " + (s.mapId != null ? s.mapId : "<mapId>"));
        p.sendMessage("§7  爆破地点の設定:  §f/ba setbombplant " + (s.mapId != null ? s.mapId : "<mapId>"));
        p.sendMessage("§7  試合を始める:    §f/ba start");
        p.sendMessage("§7  マップを追加:    §f/ba admin imigration");
        p.sendMessage("§7  コマンド一覧:    §f/ba help");
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
