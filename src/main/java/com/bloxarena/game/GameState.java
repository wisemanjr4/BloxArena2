package com.bloxarena.game;

public enum GameState {
    WAITING,      // 待機エリアで参加者を待っている
    KIT_SELECT,   // キット選択フェーズ
    IN_GAME,      // 試合中
    ENDING        // 勝利演出・リザルト表示
}
