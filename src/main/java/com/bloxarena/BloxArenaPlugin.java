package com.bloxarena;

import com.bloxarena.command.BloxArenaCommand;
import com.bloxarena.game.GameManager;
import com.bloxarena.listener.GameListeners;
import com.bloxarena.listener.WandListener;
import com.bloxarena.lobby.LobbyManager;
import com.bloxarena.map.MapManager;
import com.bloxarena.kit.KitEditorGUI;
import com.bloxarena.kit.KitInfoGUI;
import com.bloxarena.bot.BotManager;
import com.bloxarena.stats.StatsManager;
import com.bloxarena.scoreboard.ScoreboardManager;
import com.bloxarena.skill.SkillManager;
import com.bloxarena.test.TestFieldManager;
import com.bloxarena.util.SelectionTool;
import org.bukkit.plugin.java.JavaPlugin;

public class BloxArenaPlugin extends JavaPlugin {

    private GameManager gameManager;
    private LobbyManager lobbyManager;
    private MapManager mapManager;
    private GameListeners gameListeners;
    private SelectionTool selectionTool;
    private KitEditorGUI kitEditorGUI;
    private KitInfoGUI kitInfoGUI;
    private StatsManager statsManager;
    private BotManager botManager;
    private ScoreboardManager scoreboardManager;
    private SkillManager skillManager;
    private TestFieldManager testFieldManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        selectionTool  = new SelectionTool();
        kitEditorGUI      = new KitEditorGUI(this);
        kitInfoGUI        = new KitInfoGUI(this);
        botManager        = new BotManager(this);
        statsManager      = new StatsManager(this);
        scoreboardManager = new ScoreboardManager(this);
        gameManager    = new GameManager(this);
        skillManager   = new SkillManager(this);
        testFieldManager = new TestFieldManager(this);
        testFieldManager.reload();
        mapManager     = new MapManager(this);
        lobbyManager   = new LobbyManager(this);
        gameListeners  = new GameListeners(this);

        getServer().getPluginManager().registerEvents(gameListeners, this);
        getServer().getPluginManager().registerEvents(new WandListener(selectionTool), this);

        var cmd = getCommand("bloxarena");
        var handler = new BloxArenaCommand(this);
        if (cmd != null) {
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        getLogger().info("BAII WoNG v" + getDescription().getVersion() + " 有効化完了");
    }

    @Override
    public void onDisable() {
        if (gameManager != null && gameManager.getState() != com.bloxarena.game.GameState.WAITING) {
            // 再起動時は即座にロビーへ戻す（タスク不使用）
            gameManager.returnAllToLobby();
        }
        getLogger().info("BAII WoNG 無効化");
    }

    public GameManager getGameManager()     { return gameManager; }
    public LobbyManager getLobbyManager()   { return lobbyManager; }
    public MapManager getMapManager()       { return mapManager; }
    public GameListeners getGameListeners() { return gameListeners; }
    public SelectionTool getSelectionTool() { return selectionTool; }
    public KitEditorGUI getKitEditorGUI()         { return kitEditorGUI; }
    public KitInfoGUI getKitInfoGUI()             { return kitInfoGUI; }
        public BotManager getBotManager()             { return botManager; }
    public StatsManager getStatsManager()         { return statsManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public TestFieldManager getTestFieldManager() { return testFieldManager; }
}
