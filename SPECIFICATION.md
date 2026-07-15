# BloxArena II — Warriors of NextGen 逆仕様書

> **バージョン:** 1.0.0  
> **対象:** Paper/Spigot 1.19.4  
> **依存:** Citizens (任意)  
> **言語:** Java 17 + Maven

---

## 目次

1. [概要](#1-概要)
2. [試合フロー（状態遷移）](#2-試合フロー状態遷移)
3. [ロビー・待機システム](#3-ロビー待機システム)
4. [チームシステム](#4-チームシステム)
5. [マップシステム](#5-マップシステム)
6. [キットシステム](#6-キットシステム)
7. [戦闘メカニクス](#7-戦闘メカニクス)
8. [オブジェクト制圧システム](#8-オブジェクト制圧システム)
9. [殲滅システム](#9-殲滅システム)
10. [ラウンド・マッチシステム](#10-ラウンドマッチシステム)
11. [BOTシステム](#11-botシステム)
12. [チャットシステム](#12-チャットシステム)
13. [観戦システム](#13-観戦システム)
14. [管理・設定システム](#14-管理設定システム)
15. [統計システム](#15-統計システム)
16. [スコアボードシステム](#16-スコアボードシステム)
17. [コマンド一覧](#17-コマンド一覧)
18. [ファイル構成](#18-ファイル構成)

---

## 1. 概要

**BloxArena II は赤青2チーム対戦型の5v5 PvPゲームプラグインである。**

- **ジャンル:** チーム対戦型 PvP アリーナ
- **プレイヤー数:** 2〜10人（奇数時はチーム人数 ±1 で振り分け）
- **試合の長さ:** 最大ラウンド数 3（BO3）、1ラウンドに時間制限なし
- **勝利条件:** ① 敵チームの殲滅 ② 中央コンクリートの制圧（25枚ホールド）
- **特徴:** 19種類の固有キット、オブジェクト制圧、ラウンド制、BOTトレーニング

---

## 2. 試合フロー（状態遷移）

```
WAITING ──(カウントダウン完了/強制開始)──▶ KIT_SELECT
                                                  │
                                      (全員選択 / タイムアウト)
                                                  │
                                                  ▼
                                              IN_GAME ──(ラウンド終了)──▶ ラウンド判定
                                                  ▲                        │
                                                  │              (3ポイント未達)
                                                  │                        │
                                                  └── 次ラウンド開始 ◀─────┘
                                                                  │
                                                       (3ポイント先取)
                                                                  │
                                                                  ▼
                                                               ENDING ──(5秒後)──▶ WAITING
```

### 2.1 各状態の詳細

| 状態 | 説明 |
|------|------|
| `WAITING` | ロビーで参加者待機中。コマンドによる強制開始も可能。 |
| `KIT_SELECT` | ホットバー選択GUIでキットを選択（デフォルト30秒タイムアウト）。 |
| `IN_GAME` | 試合中。殲滅/オブジェクトの両勝利条件が有効。ゲート開放→カウントダウン→戦闘。 |
| `ENDING` | マッチ終了後の勝利演出・リザルト表示（約12秒）。 |

---

## 3. ロビー・待機システム

### 3.1 待機エリア
- `config.yml` の `lobby.waiting_area` で min/max 座標を指定（ワンド選択で設定）
- プレイヤーがエリア内に入ると自動的に待機リストに追加
- エリアから出ると待機リストから削除

### 3.2 カウントダウン
| 待機人数 | 秒数 | 備考 |
|----------|------|------|
| 1人以下 | 開始せず | カウントダウンキャンセル |
| 2〜3人 | 120秒 | |
| 4人以上 | 30秒 | それ以上増えても短縮されない |

- 残り10秒: タイトル表示 + 効果音
- 残り3〜1秒: カウントダウン数字タイトル表示
- 0秒: 試合開始

### 3.3 連続試合モード
- `/ba continuous on` で有効化
- 試合終了15秒後に自動で次の試合が開始（待機エリアに2人以上いれば）
- ロビーに戻ったプレイヤーは自動的に次の試合に参加

### 3.4 ロビーOOB
- 待機中の移動許可エリアをワンドで設定可能
- エリア外に出たプレイヤーはロビースポーンにテレポートで戻される

### 3.5 ロビースポーン
- 試合終了後に全プレイヤーがテレポートされる座標
- 体力・空腹度・ポーション効果・炎・矢・歩行速度 すべてリセットされる

---

## 4. チームシステム

### 4.1 チーム振り分け
- 参加者リストをシャッフル後、均等2分割
- 奇数人数の場合、赤か青にランダムで +1人
- チーム割り当て結果はログとチャットで通知

### 4.2 チームカラー
| チーム | 表示名 | カラーコード | コンクリート |
|--------|--------|--------------|--------------|
| RED | 赤 | `§c` | `RED_CONCRETE` |
| BLUE | 青 | `§b` | `CYAN_CONCRETE` |

### 4.3 ネームタグ
- 味方のネームタグのみ表示（`NAME_TAG_VISIBILITY: FOR_OTHER_TEAMS`）
- 衝突判定は無効（`COLLISION_RULE: NEVER`）

---

## 5. マップシステム

### 5.1 マップ構成要素
| 要素 | 必須 | 説明 |
|------|------|------|
| `world` | 必須 | ワールド名 |
| `display_name` | 任意 | 表示名（未設定時はIDを使用） |
| `red_spawn_zone` | 必須 | 赤チームのスポーンエリア (min/max) |
| `blue_spawn_zone` | 必須 | 青チームのスポーンエリア (min/max) |
| `center` | 必須 | 中央コンクリートエリアの基準点 |
| `lobby` | 必須 | 試合後の戻り地点 |
| `gate.red` | 任意 | 赤チーム側ゲート領域 (min/max) |
| `gate.blue` | 任意 | 青チーム側ゲート領域 (min/max) |
| `gate.material` | 任意 | ゲートブロック素材 (デフォルト: BARRIER) |
| `oob` | 任意 | 試合中の移動許可エリア (min/max) |

### 5.2 isReady 条件
```java
worldName != null
  && redSpawnMin != null && redSpawnMax != null
  && blueSpawnMin != null && blueSpawnMax != null
  && center != null
  && lobby != null
```

### 5.3 マップ選択
- デフォルト: 準備完了マップからランダム選択
- `/ba setmap <id>` で次回マップを指定可能（1試合限りの優先指定）

### 5.4 ゲートシステム
- 試合開始時、赤・青の各スポーンゾーン前に任意素材の壁を生成
- カウントダウン終了（"FIGHT!"）時に全ゲートを空気に置換し開放
- 縦方向（壁）も横方向（床/天井）も選択可能

### 5.5 OOB（Out of Bounds）
- 試合中、設定されたOOBエリアの外に出ると即脱落（kill扱い）
- マップごととロビー用の2種類あり

### 5.6 スポーン位置
- スポーンゾーンの中央上部（`max.y + 0.1`）にTP

---

## 6. キットシステム

### 6.1 キット一覧

#### Duelist（決闘者）

| キット | 武器 | 防具 | 特殊アイテム | 説明 |
|--------|------|------|--------------|------|
| **BLADE** | Diamond Sword (Sharp II) + Shield | 鉄 | — | 正面1v1最強の王道ファイター |
| **BREAKER** | Diamond Axe (Sharp I) | 鉄 | Ender Pearl x2 | 盾割り＋エンパ奇襲 |
| **NINJA** | Iron Sword (Sharp II) | 鎖 | Ender Pearl x4, Speed II (永続) | 高速機動＋奇襲 |
| **BERSERKER** | Diamond Axe (Sharp V) + Wood Pickaxe | 革 | 採掘低下 III (永続) | 超攻撃型、防御皆無 |
| **SNIPER** | Crossbow (Piercing II, Quick Charge I) + Wooden Sword | 革 | Arrow x8 | 遠距離貫通、接近戦は死 |
| **COUNTER** | Iron Sword (Sharp II) + Shield | 鉄 | Strength Potion x2 | パリィ＋バフでタイミング型 |
| **PYRO** | Iron Sword (Fire Aspect I) + Bow (Flame I) | 鎖 | Arrow x16 | 炎で燃やし続ける中距離型 |
| **JESTER** | Stone Axe (KB I, Sharp I) | 革 | Ender Pearl x3, Speed Potion | 翻弄する高機動斧使い |

#### Initiator（先陣）

| キット | 武器 | 防具 | 特殊アイテム | 説明 |
|--------|------|------|--------------|------|
| **SCOUT** | Bow + Stone Sword + エフェクト矢 | 鎖 | Spectral Arrow x24, Arrow x8 | 遠距離マーキング偵察 |
| **FLASHER** | Iron Sword (Sharp II, KB I) | 鉄 | 盲目 Splash x6, Speed Potion | 盲目→突入で突破口 |
| **MARKSMAN** | Bow (Power II, Flame I) + Stone Sword | 革 | Arrow x6 | 1発1発が命のリソース管理型 |
| **SUNDANCE** | Crossbow (Quick Charge V) + Wooden Sword | 革 | Arrow x8 | 高速装填弾幕、近接は無力 |

#### Controller（制御）

| キット | 武器 | 防具 | 特殊アイテム | 説明 |
|--------|------|------|--------------|------|
| **ROCKETER** | Crossbow (Quick Charge I) + Iron Sword | 鉄 | 爆発 Firework x3, Arrow x8 | 爆発で集団を吹き飛ばす |
| **ALCHEMIST** | Stone Sword | 鉄 | Slowness Splash x2, Instant Damage Splash, Poison Splash, Weakness Splash | ポーション空間制圧 |
| **ENGINEER** | Iron Sword + Iron Pickaxe (Eff II) | 鎖 | Glowing Splash x3, Invis Splash | マーキング＋透明化 |

#### Sentinel（守護者）

| キット | 武器 | 防具 | 特殊アイテム | 説明 |
|--------|------|------|--------------|------|
| **TRAPPER** | Iron Sword (KB I, Sharp I) + Wood Pickaxe + Shield | 鉄 | Slowness Lingering x2, Poison II Lingering x2, Weakness Lingering, Blindness Lingering, Instant Damage Lingering, Ender Pearl | スロウ＋ハーミング即死コンボ |
| **GUARDIAN** | Stone Sword (KB IV) + Shield | 鉄 | Enchanted Golden Apple | ノックバック＋金リンゴで耐える |
| **MEDIC** | Bow + Iron Sword | 鉄 | Heal Arrow x8 | 回復矢で味方を遠距離支援 |
| **SUPPORTER** | Stone Sword | 鎖 | Speed Splash x2, Resistance Splash x2, Regeneration Splash, Strength Splash | バフポーションでチーム支援 |

### 6.2 共通アイテム
- **鉄のピッケル**（`CanDestroy: WHITE/RED/CYAN CONCRETE`）— TRAPPER/BERSERKER/ENGINEER は木のピッケル
- **パン x8**
- **チーム色コンクリート x32**（`CanPlaceOn: LIME_CONCRETE`、土台のみ設置可能）

### 6.3 キット選択GUI
- **方式:** ホットバーベース（統合版互換）
- **スロット配置:** 0=前ページ, 1〜7=キット, 8=次ページ
- **操作:** 右クリックで選択・ページ切り替え
- **重複禁止:** 同一チーム内で同じキットは選択不可
- **タイムアウト:** デフォルト30秒（`config.yml` で変更可）
- **自動選択:** タイムアウト時、未選択キットに BLADE がフォールバック
- **ウォッチドッグ:** 毎秒ホットバー再描画（破損防止）

### 6.4 キットエディタGUI（運営専用）
- 54スロットGUIでアイテムと防具を自由にカスタマイズ
- 保存先: `config.yml` の `kit_editor.<KIT名>.items.<スロット>` に `ItemStack` シリアライズ
- 防具スロット: 37=ヘルメット, 38=チェスト, 39=レギンス, 40=ブーツ, 41=オフハンド
- カスタム設定がある場合はデフォルトを上書き

---

## 7. 戦闘メカニクス

### 7.1 フレンドリーファイア防止
- 同チームのプレイヤー間ダメージはキャンセル
- BOTも同様にチーム判定あり

### 7.2 死亡処理
- 死亡時にドロップアイテム・経験値をクリア
- `deadPlayers` Set に登録し、1tick 後に SPECTATOR モードへ
- 死亡原因が特定できない場合（BOT・落下死）は `lastDamager` マップで追跡

### 7.3 キルストリーク

| 連続キル数 | タイトル表示 | 演出 |
|------------|--------------|------|
| 1 | `⚔ KILL!` (黄) | 経験値音, 幸福パーティクル |
| 2 | `DOUBLE KILL!!` (黄) | 専用サウンド + TOTEM パーティクル |
| 3 | `🔥 TRIPLE KILL!` (金) | 花火 + 全体アナウンス |
| 4 | `⚡ QUADRA KILL!` (赤) | 花火 + 全体アナウンス |
| 5 | `★ PENTA KILL ★` (暗赤) | 花火 + 全体アナウンス |
| 6+ | `💀 RAMPAGE 💀` (暗赤) | 花火 + 全体アナウンス |

- キルストリークは被弾（死亡）でリセット
- 花火の数: ストリーク数 - 2（最大4発）
- 花火の色: 3=黄, 4=橙, 5=赤, 6+=紫

### 7.4 死亡演出
- 死亡地点に雷エフェクト + 大爆発パーティクル
- CRIT + CRIT_MAGIC + SMOKE_LARGE パーティクル
- 攻撃と爆発の効果音

### 7.5 落下ダメージ免除
- ゲート開放直後に1回だけ落下ダメージが免除される（消費型）
- 高いところからスタートするマップ対策

### 7.6 投射物クリーンアップ
- ラウンド終了時に以下のエンティティを削除:
  - EnderPearl, Firework, ThrownPotion, Arrow, SpectralArrow

### 7.7 人数差バフ
- 生存人数が少ないチームに毎秒付与:
  - Speed I（人数差2以上なら Speed II）
  - Regeneration I
- 持続時間: 30秒（毎秒上書き更新）

### 7.8 ヒットエフェクト
- 攻撃命中時: CRIT パーティクル + HURT 効果音

---

## 8. オブジェクト制圧システム

### 8.1 中央コンクリートエリア
- 中央基準点を中心に 5x5 = 25ブロック
- **Y-1 レイヤー:** 緑コンクリート（土台）
- **Y レイヤー:** 白コンクリート（初期状態）
- プレイヤーは白・赤・シアンのコンクリートのみ破壊可能（設置は赤・シアンのみ）

### 8.2 3分ロック
- 試合開始から3分間（`OBJECTIVE_LOCK_MS = 120,000ms`）は操作不可
- ロック中に操作しようとするとチャット警告
- 試合開始時とバトル開始時（FIGHT!）にそれぞれアナウンス

### 8.3 制圧判定
- ブロック設置時に 5x5 全マスをチェック
- 同色25枚揃った時点で制圧状態に入る
- 1ブロックでも異なる色に破壊/置換されると制圧状態解除（ホールドタイマーリセット）

### 8.4 ホールドタイマー
- 制圧後、15秒のホールドタイマー開始
- 毎秒アクションバーに残り秒数表示
- 15秒完了でオブジェクト勝利
- 相手が1つでもブロックを破壊すると即座にリセット

### 8.5 進捗通知
- ブロック設置時、現在の占有率をチャット通知: `[BA] オブジェクトが奪取されています！ 赤:X/25 青:Y/25`

---

## 9. 殲滅システム

### 9.1 判定タイミング
- プレイヤー死亡の 1tick 後、BOT死亡時にも実行
- BOTを含めた生存者数で判定

### 9.2 勝利条件
- 一方のチームの生存者が0人 → 相手チームの殲滅勝利
- 両チーム同時に0人 → 引き分け（まれなケース）

### 9.3 BOTの生存カウント
- `BotManager.getAliveBotCount(team)` で有効なエンティティ数を計上
- 死亡済みのBOTはチームから削除されず、`isValid()/isDead()` で判定

---

## 10. ラウンド・マッチシステム

### 10.1 ラウンド制
- BO3: 3ポイント先取でマッチ勝利
- ラウンド間:
  - 体力・空腹度・状態異常をリセット（ロビーには戻さない）
  - 中央コンクリートを白にリセット
  - ゲート再設置
  - キット再選択（KIT_SELECTに戻る）
  - `deadPlayers` クリア、`spectators` クリア

### 10.2 ラウンド終了後
- ラウンド結果タイトル表示（4秒間）
- ラウンドスコア表示: `赤 X §7- §9Y §7青`
- 3ポイント先取でマッチ終了、そうでなければ次ラウンドへ

### 10.3 マッチ終了時
- 勝敗を StatsManager に記録
- マッチレポート表示:
  - 勝者チーム
  - MVP（最多キル、同数の場合ダメージで判定）
  - 最多ダメージ
  - 個人成績（Kill, DMG）
- 勝利演出（花火×16発、パーティクルシャワー、爆発フィナーレ、約12秒）
- 5秒後にロビーへ帰還
- 連続試合モード有効なら自動カウントダウン開始

---

## 11. BOTシステム

### 11.1 実装方式
| Citizens 有効 | Citizens なし |
|---------------|---------------|
| プレイヤーNPCとしてスポーン（リフレクション経由） | Zombieエンティティにフォールバック |
| ナビゲーターAIで追跡 | `setTarget()` + 近接補助ダメージ |
| 外見・当たり判定が実プレイヤーと同一 | ゾンビの外見・当たり判定 |

- Citizens API はコンパイル時依存なし（完全リフレクション）
- スポーン元コードは常に同一コードパス

### 11.2 AI
- 2秒間隔（40tick）でAIティック実行
- 最寄りの敵プレイヤー（試合参加者かつ異チーム）を探索
- Citizens: Navigator で指定座標に移動
- Zombie: `Mob.setTarget()` + 距離4m以内でダメージ2.0追加
- 近接補助: Citizens 3m以内で 1.5ダメージ

### 11.3 死亡処理
- `EntityDeathEvent` / Citizens `NPCDeathEvent` 経由で判定
- `lastDamager` でキル帰属を管理（`getKiller()` 非依存）
- 死亡時: GameManager に通知 → 殲滅判定

### 11.4 BOT管理コマンド
- `/ba bot add [n]` — BOTをn体予約（試合開始時にチームへ分配）
- `/ba bot clear` — 全BOTを削除
- `/ba bot list` — 現在のBOT数を表示

### 11.5 チーム分配
- チーム人数が少ない方に交互に割り当て
- 赤チーム用は `RED_WOOL` ヘルメット、青チーム用は `CYAN_WOOL` ヘルメット
- ヘルメットドロップ率は0%

---

## 12. チャットシステム

### 12.1 デフォルト: チームチャット
- IN_GAME 状態の参加者はチームメンバーにのみチャットが届く
- 接頭辞: `赤[赤] プレイヤー名: メッセージ`
- スペクテーターと自分自身にも表示される

### 12.2 全体チャット
- 行頭に `.` を入力すると全体チャットに切り替え
- 表示形式: `[全体] プレイヤー名: メッセージ`

### 12.3 スペクテーターチャット
- スペクテーター間 + 非参加者のみに届く独立チャット
- 生存者には見えない
- 接頭辞: `[spec] プレイヤー名: メッセージ`

---

## 13. 観戦システム

### 13.1 観戦入場
- `/ba spectate` で試合中に観戦モードに移行
- 待機エリアにいる状態で試合が始まった場合、非参加者は自動的に観戦モード
- 観戦入場時、アリーナ中心にTP

### 13.2 観戦チャット
- スペクテーター同士のみで会話可能（生存者には不可視）

### 13.3 試合後
- ロビーに戻る際に全プレイヤーが `fullyRestorePlayer` で通常状態に復帰

---

## 14. 管理・設定システム

### 14.1 設定ファイル: config.yml
```yaml
lobby:
  waiting_area:
    world: "world"
    min: { x, y, z }
    max: { x, y, z }
  spawn: { x, y, z }
  oob:  # 任意: ロビーOOB
    min: { x, y, z }
    max: { x, y, z }

maps:
  - id: "arena1"
    world: "bloxarena_world"
    display_name: "アリーナ1"
    red_spawn_zone:  { min, max }
    blue_spawn_zone: { min, max }
    center: { x, y, z }
    lobby:  { x, y, z }
    gate:   # 任意
      red:  { min, max }
      blue: { min, max }
      material: BARRIER
    oob:    # 任意
      min: { x, y, z }
      max: { x, y, z }

kit_select:
  timeout_seconds: 30

kit_editor:  # KitEditorGUI で自動生成
  <KIT名>:
    items:
      0: { ItemStack serialized }
    armor:
      0: { ItemStack serialized }  # 0=helmet, 1=chest, 2=legs, 3=boots, 4=offhand
```

### 14.2 設定ファイル: stats.yml
- プレイヤーごとの累計統計（自動生成）
- キー: UUID, 値: kills, deaths, wins, losses, damage, name

### 14.3 セットアップウィザード
- `/ba admin imigration` — 12ステップの初期セットアップ
  1. ロビースポーン
  2. 待機エリア
  3. マップ作成
  4. 赤スポーンゾーン
  5. 青スポーンゾーン
  6. 中央基準点
  7. マップロビー地点
  8. 赤ゲート領域 (任意)
  9. 青ゲート領域 (任意)
  10. ゲートブロック素材 (任意)
  11. マップOOBゾーン (任意)
  12. ロビーOOBゾーン (任意)

- `/ba admin addmap <id>` — 8ステップのマップ追加
  1. 赤スポーン 2. 青スポーン 3. センター 4. マップロビー
  5. 赤ゲート 6. 青ゲート 7. ゲートブロック 8. OOB

### 14.4 ワンドシステム
- `/ba wand` でガラスブロックワンドを取得
- 左クリック: Pos1 (min), 右クリック: Pos2 (max)
- getMin/getMax は座標の大小関係を正規化

### 14.5 再接続対応
- 切断後3分以内の再接続でキットを再支給
- 切断時刻は `disconnectedAt` マップで管理

---

## 15. 統計システム

### 15.1 プレイヤー統計 (PlayerStats)
| フィールド | 型 | 説明 |
|------------|------|------|
| kills | int | 累計キル数 |
| deaths | int | 累計死亡数 |
| wins | int | 累計勝利数 |
| losses | int | 累計敗北数 |
| damage | double | 累計与ダメージ |

- `getKD()`: `deaths == 0 ? kills : (double) kills / deaths`
- `getWinRate()`: `(double) wins / (wins + losses) * 100`（%）

### 15.2 マッチ統計 (MatchStats)
- 1試合限りの揮発性データ
- MVP: 最多キル → 同数の場合ダメージで判定
- 最多ダメージ: ダメージ量で判定

### 15.3 ランキング
- `/ba top kills/wins/kd/damage` で上位10名表示
- kills は降順ソート

---

## 16. スコアボードシステム

### 16.1 表示内容（毎秒更新）

| 行 | 内容 |
|----|------|
| 15 | (空行) |
| 14 | `ラウンド N` |
| 13 | `●●●○○ vs ●●●○○` (ラウンド勝利数表示) |
| 12 | (空行) |
| 11 | `赤: X人` |
| 10 | `青: Y人` |
| 9 | (空行) |
| 8 | `経過: MM:SS` |
| 7 | (空行) |
| 6 | `キット: [キット表示名]` |
| 5 | `チーム: [team color] [チーム名]` |

### 16.2 個別ボード方式
- プレイヤーごとに独立した Scoreboard を作成
- チームカラー設定（赤=RED, 青=AQUA）とネームタグ設定を含む
- キット名が共有されるバグを防止

---

## 17. コマンド一覧

### コマンドエイリアス
- メイン: `/bloxarena <sub>`  
- エイリアス: `/ba <sub>`

### 運営コマンド (`bloxarena.admin` 権限)
| コマンド | 説明 |
|----------|------|
| `/ba start` | 試合を強制開始 |
| `/ba stop` | 試合を強制終了 |
| `/ba wand` | ガラスワンドを受け取る |
| `/ba setwaitingarea` | ワンド選択範囲を待機エリアに設定 |
| `/ba setlobby` | ロビースポーンを現在地に設定 |
| `/ba addmap <id> [world]` | マップを新規追加 |
| `/ba setspawnzone <red\|blue> <mapId>` | ワンド選択範囲をスポーンゾーンに設定 |
| `/ba setcenter <mapId>` | 中央基準点を現在地に設定 |
| `/ba setmaplobby <mapId>` | マップロビー地点を現在地に設定 |
| `/ba setmap <mapId>` | 次試合のマップを指定（1試合限定） |
| `/ba setmapname <mapId> <表示名>` | マップの表示名を設定 |
| `/ba setgate <red\|blue> <mapId>` | ゲート領域をワンドで設定 |
| `/ba gatematl <mapId> <素材名>` | ゲートブロック素材を指定 |
| `/ba setoob <mapId\|lobby>` | OOBゾーンをワンドで設定 |
| `/ba kitedit` | キットエディタを開く |
| `/ba bot add [n]` | BOTを予約追加 |
| `/ba bot clear` | BOTを全削除 |
| `/ba bot list` | BOT数を表示 |
| `/ba continuous <on\|off>` | 連続試合モード切り替え |
| `/ba reload` | config.yml をリロード |
| `/ba admin imigration` | セットアップウィザード起動 |
| `/ba admin addmap <id>` | マップウィザード起動 |
| `/ba admin next` | ウィザードで次ステップへ |
| `/ba admin skip` | ウィザードで任意ステップをスキップ |
| `/ba admin cancel` | ウィザードをキャンセル |

### 一般コマンド (`bloxarena.play` 権限)
| コマンド | 説明 |
|----------|------|
| `/ba info [mapId]` | マップ情報・一覧表示 |
| `/ba status` | 現在の状態を表示 |
| `/ba spectate` | 試合を観戦 |
| `/ba kits` | キット一覧GUIを開く |
| `/ba stats [player]` | プレイヤー統計を表示 |
| `/ba top <kills\|wins\|kd\|damage>` | ランキング上位10名表示 |

---

## 18. ファイル構成

```
BloxArena2/
├── pom.xml                         # Maven ビルド設定
├── BloxArenaII.jar                 # ルートJAR（Maven出力コピー）
├── CHANGELOG.md                    # パッチノート
├── SPECIFICATION.md                # 本仕様書
├── src/main/
│   ├── resources/
│   │   ├── plugin.yml              # プラグイン定義
│   │   └── config.yml              # デフォルト設定
│   └── java/com/bloxarena/
│       ├── BloxArenaPlugin.java    # エントリポイント
│       ├── game/
│       │   ├── GameManager.java    # 試合進行 (1033行)
│       │   ├── GameState.java      # 状態列挙
│       │   ├── TeamColor.java      # チームカラー
│       │   └── WinCondition.java   # 勝利条件
│       ├── lobby/
│       │   └── LobbyManager.java   # ロビー管理 (323行)
│       ├── map/
│       │   ├── MapManager.java     # マップ管理 (184行)
│       │   ├── MapConfig.java      # マップ設定データ (113行)
│       │   └── ArenaGenerator.java # 自動生成 (廃止)
│       ├── kit/
│       │   ├── KitType.java        # 19キット定義
│       │   ├── KitBuilder.java     # キット装備構築 (647行)
│       │   ├── KitSelectGUI.java   # 選択UI (285行)
│       │   ├── KitEditorGUI.java   # 編集UI (358行)
│       │   └── KitInfoGUI.java     # 一覧UI (152行)
│       ├── bot/
│       │   └── BotManager.java     # BOT管理 (288行)
│       ├── command/
│       │   ├── BloxArenaCommand.java  # 全コマンド (488行)
│       │   ├── SetupWizard.java       # セットアップ (317行)
│       │   └── MapWizard.java         # マップ設定 (266行)
│       ├── listener/
│       │   ├── GameListeners.java     # メインリスナー (340行)
│       │   └── WandListener.java      # ワンド操作
│       ├── stats/
│       │   ├── StatsManager.java      # 統計管理
│       │   ├── PlayerStats.java       # プレイヤー統計
│       │   └── MatchStats.java        # マッチ統計
│       ├── scoreboard/
│       │   └── ScoreboardManager.java # スコアボード (188行)
│       └── util/
│           ├── SelectionTool.java     # ワンド選択
│           └── Effects.java           # 演出 (209行)
└── target/                         # Maven ビルド出力
    ├── BloxArenaII-1.0.0.jar
    ├── classes/...
    └── maven-*/...
```

---

> 本書は BloxArena II v1.0.0 の全ソースコードから逆生成された仕様書です。  
> 各機能の詳細な実装は対応するソースファイルを参照してください。
