# BloxArena II "Warriors of NextGen"

5v5 PvPアリーナ Minecraft Paper プラグイン。34種類のキットから1つを選び、6つのゲームモードで戦う。

- **プラットフォーム**: Paper 1.19.4
- **Java**: 17+（ビルドはJDK 21）
- **依存**: Citizens (soft-dep)

## ゲームモード

| モード | 概要 |
|--------|------|
| **バトルアリーナ** | 殲滅＋中央コンクリート制圧の2勝利条件。3ラウンド先取 |
| **チームデスマッチ** | 制限時間内キル数勝負。リスポーン有 |
| **爆破ミッション** | CS風。爆弾設置/解除。ラウンド制 |
| **占領戦** | 拠点の奪い合い。ポイント先取 |
| **キャプチャーザフラッグ** | 敵陣の旗を奪い自陣へ。3回先取 |
| **FFA** | チームなし個人戦。制限時間内最多キル |

## 主なシステム

- **バースト** — 全キット共通の超必殺技（1ラウンド1回）
- **コンボ** — 連続ヒットで演出が豪華に。コンボ中断でCOUNTER
- **ガードブレイク** — 1秒しゃがみ→攻撃で盾を3秒無効化
- **サドンデス** — 試合時間6分超過で全員超火力（CTFは10分時間制）
- **BGM** — `.nbs` ファイルを `plugins/BloxArenaII/songs/` に配置して試合中に再生

## キット（34種）

- **Duelist**: BLADE / BREAKER / NINJA / BERSERKER / PYRO / JESTER / VAMPIRE / BOMBER / COOK / GRANG / LANCER
- **Initiator**: SCOUT / FLASHER / WHIRLWIND / MISTRAL / NILGIRITAR / SWAPPER / STICKER / DECOY
- **Controller**: MARKSMAN / SUNDANCE / ROCKETER / ALCHEMIST / ENGINEER / RESTRICTIONER / TRANSPORTER / KREUTZ / NECRO
- **Sentinel**: TRAPPER / GUARDIAN / MEDIC / SUPPORTER / PHANTOM / ANCHOR / RELEASER

※詳細な仕様は [GAMEDESIGN.md](GAMEDESIGN.md) を参照

## ビルド

```bash
mvn package -DskipTests
```

生成物: `target/BloxArenaII-3.2.0.jar`

## セットアップ

1. `plugins/BloxArenaII/` に jar を配置
2. サーバーを起動して `/ba admin imigration` でマップ設定
3. `/ba start` でゲーム開始

## コマンド

| コマンド | 機能 |
|----------|------|
| `/ba start` / `/ba stop` | ゲーム開始/停止 |
| `/ba join` | 参加 |
| `/ba kits` | キット一覧 |
| `/ba stats` | 個人統計 |
| `/ba top kits` | サーバー統計 |
| `/ba test` | テスト場 |
| `/ba tutorial` | チュートリアル |
| `/ba bgm` | BGM選択 |
| `/ba version` | バージョン確認 |
| `/ba ffa addspawn` | FFAスポーン設定 |

## ライセンス

All Rights Reserved.
