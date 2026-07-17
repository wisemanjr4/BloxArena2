package com.bloxarena.kit;

public enum KitType {
    // === Duelist ===
    BLADE     ("ブレード",    "§6⚔ ブレード §7(Duelist)",     "正面の1v1で負けない王道ファイター", KitRole.DUELIST,
               "君は「剣豪」だ。剣を振るい、敵陣を切り裂け。"),
    BREAKER   ("ブレイカー",  "§c🪓 ブレイカー §7(Duelist)",  "斧スキルで突進し敵陣を崩す機動型", KitRole.DUELIST,
               "君は「破壊者」だ。その突撃で全てを破壊しろ。"),
    NINJA     ("ニンジャ",    "§2🥷 ニンジャ §7(Duelist)",    "透明化スキルで敵の背後を取る奇襲型", KitRole.DUELIST,
               "君は「影法師」だ。敵の裏を取り、攪乱せよ。"),
    BERSERKER ("バーサーカー","§4⚡ バーサーカー §7(Duelist)", "連続爆発スキルで制圧する超攻撃型", KitRole.DUELIST,
               "君は「戦鬼」だ。怒りを解き放ち、敵を粉砕せよ。"),
    SNIPER    ("スナイパー",  "§7🎯 スナイパー §7(Duelist)",  "狙撃眼で敵をマークし一撃必殺", KitRole.DUELIST,
               "君は「神射手」だ。狙い澄まし、一撃で仕留めろ。"),
    COUNTER   ("カウンター",  "§9🗡 カウンター §7(Duelist)",  "パリィで相手を無力化するタイミング型", KitRole.DUELIST,
               "君は「断罪者」だ。敵の攻撃を見切り、その刃で裁け。"),
    PYRO      ("パイロ",      "§6🔥 パイロ §7(Duelist)",      "炎の剣と弓＋業炎スキルで焼き尽くす", KitRole.DUELIST,
               "君は「火葬者」だ。業火を纏い、敵を灰燼へ帰せ。"),
    JESTER    ("ジェスター",  "§e🃏 ジェスター §7(Duelist)",  "疾走スキルとエンパで翻弄する高機動斧使い", KitRole.DUELIST,
               "君は「欺瞞者」だ。敵を欺き、戦場を嘲笑え。"),
    VAMPIRE   ("ヴァンパイア","§4🩸 ヴァンパイア §7(Duelist)","吸血ゲージを溜めて段階強化する変身形", KitRole.DUELIST,
               "君は「吸血騎」だ。血の力を解き放ち、敵を絶望へ沈めよ。"),
    BOMBER    ("ボマー",      "§c💣 ボマー §7(Duelist)",      "地雷を設置し任意起爆するトラップ型", KitRole.DUELIST,
               "君は「爆轟」だ。爆炎を操り、敵を吹き飛ばせ。"),
    COOK      ("料理人",      "§6🍳 料理人 §7(Duelist)",      "ランダム食材から料理を作りバフ/デバフを撒く", KitRole.DUELIST,
               "君は「美食家」だ。至高の料理で、仲間を勝利へ導け。"),
    // GLIDER retired
    // GLIDER    ("グライダー",...),

    GRANG     ("グラング",     "§7🛡 グラング §7(Duelist)",     "盾チャージで突撃する機動要塞", KitRole.DUELIST,
               "君は「鋼騎」だ。鋼鉄の意志で、敵陣を突破せよ。"),

    // === Initiator ===
    SCOUT     ("スカウト",    "§a🏹 スカウト §7(Initiator)",  "リコンボルトで敵位置を可視化する偵察役", KitRole.INITIATOR,
               "君は「千里眼」だ。敵を暴き、味方へ勝機をもたらせ。"),
    FLASHER   ("フラッシャー","§b💥 フラッシャー §7(Initiator)","フラッシュバン球で敵を盲目化し突破口を作る", KitRole.INITIATOR,
               "君は「閃光」だ。光で敵の視界を奪え。"),
    MARKSMAN  ("マークスマン","§c🏹 マークスマン §7(Initiator)","ヘヴィーボルトで敵のHP上限を削る", KitRole.INITIATOR,
               "君は「魔弾の射手」だ。魔弾を放ち、敵の命を撃ち抜け。"),
    SUNDANCE  ("サンダンス",  "§b⚡ サンダンス §7(Initiator)","リボルビングクロスボウで弾幕を張る", KitRole.INITIATOR,
               "君は「荒野の決闘者」だ。一瞬の隙が、勝敗を決める。"),
    SWAPPER   ("スワッパー",  "§5🔄 スワッパー §7(Initiator)","相手との位置を即座に入れ替える撹乱型", KitRole.INITIATOR,
               "君は「転換者」だ。敵の位置を入れ替え、戦況を覆せ。"),
    STICKER   ("ステッカー",  "§3🎣 ステッカー §7(Initiator)","グラップルで敵を引き寄せる捕獲型", KitRole.INITIATOR,
               "君は「万有の手」だ。敵を引き寄せ、逃げ場を奪え。"),
    DECOY     ("デコイ",      "§8👥 デコイ §7(Initiator)",    "自身の分身を生成し透明化する欺瞞型", KitRole.INITIATOR,
               "君は「攪乱者」だ。敵を惑わせ、戦場を支配せよ。"),
    WHIRLWIND ("ワールウィンド","§f🌪 ワールウィンド §7(Initiator)","気流で敵を押し出し＋追尾球で打ち上げる風使い", KitRole.INITIATOR,
               "君は「旋風」だ。風を纏い、敵を空へ舞い上げろ。"),
    NILGIRITAR("ニルギリタール","§f🌀 ニルギリタール §7(Initiator)","クロスボウ＋近接で盾貫通ダメージを与える風穴使い", KitRole.INITIATOR,
               "君は「風穴を開ける者」だ。防御を貫き、敵を穿て。"),

    // === Controller ===
    ROCKETER  ("ロケッター",  "§e🚀 ロケッター §7(Controller)","メガロケットで大爆発を起こすエリア制圧役", KitRole.CONTROLLER,
               "君は「砲撃手」だ。爆轟を響かせ、敵陣を壊滅させよ。"),
    ALCHEMIST ("アルケミスト","§d🧪 アルケミスト §7(Controller)","再調合でポーションを補充し空間ごと支配", KitRole.CONTROLLER,
               "君は「錬金術師」だ。秘薬を操り、戦局を掌握せよ。"),
    ENGINEER  ("エンジニア",  "§6🔧 エンジニア §7(Controller)","レーザータレットを設置する制圧型", KitRole.CONTROLLER,
               "君は「機構師」だ。機械を従え、戦場そのものを支配せよ。"),
    RESTRICTIONER ("リストリクショナー","§8⛓ リストリクショナー §7(Controller)","自身ごと対象を拘束するロック型", KitRole.CONTROLLER,
               "君は「拘束者」だ。敵を縛り上げ、勝機を創り出せ。"),
    TRANSPORTER("トランスポーター","§3🌀 トランスポーター §7(Controller)","双方向ポータルを設置する移動型", KitRole.CONTROLLER,
               "君は「境界渡り」だ。空間を繋ぎ、新たな戦場を切り拓け。"),
    KREUTZ    ("クロイツ",    "§5🃏 クロイツ §7(Controller)",  "魔法カードを引き様々な効果を発動する魔術師", KitRole.CONTROLLER,
               "君は「魔法使い」だ。運命のカードを引き、戦場を掌握せよ。"),
    // MIMIC retired - keep code
    MIMIC     ("ミミック",    "§5🔄 ミミック §7(Controller)",  "敵のスキルをコピーする模倣型", KitRole.CONTROLLER,
               "君は「模倣者」だ。敵の力を奪い、その牙を敵へ返せ。"),
    NECRO     ("ネクロ",       "§8💀 ネクロ §7(Controller)",   "スケルトン3部隊を召喚・指揮する屍体舞踏者", KitRole.CONTROLLER,
               "君は「死体舞踏者」だ。死者を従え、死の舞踏会を始めよ。"),

    // === Sentinel ===
    TRAPPER   ("トラッパー",  "§3🪤 トラッパー §7(Sentinel)", "不可視の罠で敵を行動不能にする防衛型", KitRole.SENTINEL,
               "君は「罠師」だ。罠を巡らせ、敵を狩り尽くせ。"),
    GUARDIAN  ("ガーディアン","§f🛡 ガーディアン §7(Sentinel)","鉄壁スキルで一定時間無敵化する盾役", KitRole.SENTINEL,
               "君は「不沈艦」だ。いかなる猛攻にも沈まず、戦線を支え続けろ。"),
    MEDIC     ("メディック",  "§5❤ メディック §7(Sentinel)",  "フィールドケアで味方を範囲回復する支援型", KitRole.SENTINEL,
               "君は「生命線」だ。仲間を救い、戦線を繋ぎ止めよ。"),
    SUPPORTER ("サポーター",  "§a💊 サポーター §7(Sentinel)", "再調達でバフポーションを補充し戦場を支える", KitRole.SENTINEL,
               "君は「加護者」だ。仲間へ力を授け、勝利へ導け。"),
    PHANTOM   ("ファントム",  "§7👻 ファントム §7(Sentinel)", "霊体化で透明＋無敵になる回避型", KitRole.SENTINEL,
               "君は「幻影」だ。姿を晦まし、敵を翻弄せよ。"),
    ANCHOR    ("アンカー",    "§9⚓ アンカー §7(Sentinel)",   "磁場で周囲の敵を減速させる制圧型", KitRole.SENTINEL,
               "君は「停滞者」だ。領域を展開し、敵の進軍を封じよ。"),
    RELEASER  ("リリーサー",  "§e💢 リリーサー §7(Sentinel)",   "バースト系スキル特化。大爆発＋小爆発を駆使", KitRole.SENTINEL,
               "君は「解放者」だ。秘めたる力を解き放ち、全てを吹き飛ばせ。");

    private final String name;
    private final String displayName;
    private final String description;
    private final String lore;
    private final KitRole role;

    KitType(String name, String displayName, String description, KitRole role, String lore) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.role = role;
        this.lore = lore;
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getLore() { return lore; }
    public KitRole getRole() { return role; }
}
