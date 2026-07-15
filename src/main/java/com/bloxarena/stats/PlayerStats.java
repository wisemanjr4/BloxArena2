package com.bloxarena.stats;

/** プレイヤー1人分の累計統計 */
public class PlayerStats {
    public int kills;
    public int deaths;
    public int wins;
    public int losses;
    public double damage;  // 与えたダメージ合計

    public double getKD()      { return deaths == 0 ? kills : (double) kills / deaths; }
    public int    getGames()   { return wins + losses; }
    public double getWinRate() { return getGames() == 0 ? 0 : (double) wins / getGames() * 100; }
}
