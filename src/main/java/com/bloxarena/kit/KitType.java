package com.bloxarena.kit;

public enum KitType {
    // === Duelist ===
    BLADE     ("ブレード",    "§6⚔ ブレード §7(Duelist)",     "正面の1v1で負けない王道ファイター", KitRole.DUELIST),
    BREAKER   ("ブレイカー",  "§c🪓 ブレイカー §7(Duelist)",  "斧スキルで突進し敵陣を崩す機動型", KitRole.DUELIST),
    NINJA     ("ニンジャ",    "§2🥷 ニンジャ §7(Duelist)",    "透明化スキルで敵の背後を取る奇襲型", KitRole.DUELIST),
    BERSERKER ("バーサーカー","§4⚡ バーサーカー §7(Duelist)", "連続爆発スキルで制圧する超攻撃型", KitRole.DUELIST),
    SNIPER    ("スナイパー",  "§7🎯 スナイパー §7(Duelist)",  "狙撃眼で敵をマークし一撃必殺", KitRole.DUELIST),
    COUNTER   ("カウンター",  "§9🗡 カウンター §7(Duelist)",  "パリィで相手を無力化するタイミング型", KitRole.DUELIST),
    PYRO      ("パイロ",      "§6🔥 パイロ §7(Duelist)",      "炎の剣と弓＋業炎スキルで焼き尽くす", KitRole.DUELIST),
    JESTER    ("ジェスター",  "§e🃏 ジェスター §7(Duelist)",  "疾走スキルとエンパで翻弄する高機動斧使い", KitRole.DUELIST),
    VAMPIRE   ("ヴァンパイア","§4🩸 ヴァンパイア §7(Duelist)","吸血ゲージを溜めて段階強化する変身形", KitRole.DUELIST),
    BOMBER    ("ボマー",      "§c💣 ボマー §7(Duelist)",      "地雷を設置し任意起爆するトラップ型", KitRole.DUELIST),
    COOK      ("料理人",      "§6🍳 料理人 §7(Duelist)",      "ランダム食材から料理を作りバフ/デバフを撒く", KitRole.DUELIST),
    // GLIDER retired (keep code for future use)
    // GLIDER    ("グライダー",  "§f🕊 グライダー §7(Duelist)",  "エリトラ＋上昇気流で空から奇襲する空中型", KitRole.DUELIST),

    // === Initiator ===
    SCOUT     ("スカウト",    "§a🏹 スカウト §7(Initiator)",  "リコンボルトで敵位置を可視化する偵察役", KitRole.INITIATOR),
    FLASHER   ("フラッシャー","§b💥 フラッシャー §7(Initiator)","フラッシュバン球で敵を盲目化し突破口を作る", KitRole.INITIATOR),
    MARKSMAN  ("マークスマン","§c🏹 マークスマン §7(Initiator)","ヘヴィーボルトで敵のHP上限を削る", KitRole.INITIATOR),
    SUNDANCE  ("サンダンス",  "§b⚡ サンダンス §7(Initiator)","リボルビングクロスボウで弾幕を張る", KitRole.INITIATOR),
    SWAPPER   ("スワッパー",  "§5🔄 スワッパー §7(Initiator)","相手との位置を即座に入れ替える撹乱型", KitRole.INITIATOR),
    STICKER   ("ステッカー",  "§3🎣 ステッカー §7(Initiator)","グラップルで敵を引き寄せる捕獲型", KitRole.INITIATOR),
    DECOY     ("デコイ",      "§8👥 デコイ §7(Initiator)",    "自身の分身を生成し透明化する欺瞞型", KitRole.INITIATOR),
    WHIRLWIND ("ワールウィンド","§f🌪 ワールウィンド §7(Initiator)","気流で敵を押し出し＋追尾球で打ち上げる風使い", KitRole.INITIATOR),

    // === Controller ===
    ROCKETER  ("ロケッター",  "§e🚀 ロケッター §7(Controller)","メガロケットで大爆発を起こすエリア制圧役", KitRole.CONTROLLER),
    ALCHEMIST ("アルケミスト","§d🧪 アルケミスト §7(Controller)","再調合でポーションを補充し空間ごと支配", KitRole.CONTROLLER),
    ENGINEER  ("エンジニア",  "§6🔧 エンジニア §7(Controller)","レーザータレットを設置する制圧型", KitRole.CONTROLLER),
    RESTRICTIONER ("リストリクショナー","§8⛓ リストリクショナー §7(Controller)","自身ごと対象を拘束するロック型", KitRole.CONTROLLER),
    TRANSPORTER("トランスポーター","§3🌀 トランスポーター §7(Controller)","双方向ポータルを設置する移動型", KitRole.CONTROLLER),
    MIMIC     ("ミミック",    "§5🔄 ミミック §7(Controller)",  "敵のスキルをコピーする模倣型", KitRole.CONTROLLER),

    // === Sentinel ===
    TRAPPER   ("トラッパー",  "§3🪤 トラッパー §7(Sentinel)", "不可視の罠で敵を行動不能にする防衛型", KitRole.SENTINEL),
    GUARDIAN  ("ガーディアン","§f🛡 ガーディアン §7(Sentinel)","鉄壁スキルで一定時間無敵化する盾役", KitRole.SENTINEL),
    MEDIC     ("メディック",  "§5❤ メディック §7(Sentinel)",  "フィールドケアで味方を範囲回復する支援型", KitRole.SENTINEL),
    SUPPORTER ("サポーター",  "§a💊 サポーター §7(Sentinel)", "再調達でバフポーションを補充し戦場を支える", KitRole.SENTINEL),
    PHANTOM   ("ファントム",  "§7👻 ファントム §7(Sentinel)", "霊体化で透明＋無敵になる回避型", KitRole.SENTINEL),
    ANCHOR    ("アンカー",    "§9⚓ アンカー §7(Sentinel)",   "磁場で周囲の敵を減速させる制圧型", KitRole.SENTINEL),
    RELEASER  ("リリーサー",  "§e💢 リリーサー §7(Sentinel)",   "バースト系スキル特化。大爆発＋小爆発を駆使", KitRole.SENTINEL),
    GRANG     ("グラング",     "§7🛡 グラング §7(Duelist)",     "盾チャージで突撃する機動要塞", KitRole.DUELIST),
    NECRO     ("ネクロ",       "§8💀 ネクロ §7(Controller)",   "スケルトン3部隊を召喚・指揮する屍体舞踏者", KitRole.CONTROLLER);

    private final String name;
    private final String displayName;
    private final String description;
    private final KitRole role;

    KitType(String name, String displayName, String description, KitRole role) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.role = role;
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public KitRole getRole() { return role; }
}
