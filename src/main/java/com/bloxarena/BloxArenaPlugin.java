/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.PluginCommand
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package com.bloxarena;

import com.bloxarena.bot.BotManager;
import com.bloxarena.command.BloxArenaCommand;
import com.bloxarena.comp.CompManager;
import com.bloxarena.game.GameManager;
import com.bloxarena.game.GameState;
import com.bloxarena.kit.KitEditorGUI;
import com.bloxarena.kit.KitInfoGUI;
import com.bloxarena.listener.GameListeners;
import com.bloxarena.listener.WandListener;
import com.bloxarena.lobby.LobbyManager;
import com.bloxarena.map.MapManager;
import com.bloxarena.scoreboard.ScoreboardManager;
import com.bloxarena.skill.SkillManager;
import com.bloxarena.stats.StatsManager;
import com.bloxarena.test.TestFieldManager;
import com.bloxarena.title.BATitleManager;
import com.bloxarena.tutorial.TutorialManager;
import com.bloxarena.ultimate.UltimateManager;
import com.bloxarena.util.SelectionTool;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BloxArenaPlugin
extends JavaPlugin {
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
    private UltimateManager ultimateManager;
    private TestFieldManager testFieldManager;
    private TutorialManager tutorialManager;
    private CompManager compManager;
    private BATitleManager titleManager;
    private final Set<UUID> oobImmunePlayers = new HashSet<UUID>();
    private final Set<String> labFeatures = new HashSet<String>();

    public boolean isLabEnabled(String feature) {
        return this.labFeatures.contains(feature);
    }

    public boolean toggleLab(String feature) {
        if (this.labFeatures.contains(feature)) {
            this.labFeatures.remove(feature);
            return false;
        }
        this.labFeatures.add(feature);
        return true;
    }

    public Set<String> getLabFeatures() {
        return this.labFeatures;
    }

    public void onEnable() {
        this.saveDefaultConfig();
        this.selectionTool = new SelectionTool();
        this.kitEditorGUI = new KitEditorGUI(this);
        this.kitInfoGUI = new KitInfoGUI(this);
        this.botManager = new BotManager(this);
        this.statsManager = new StatsManager(this);
        this.titleManager = new BATitleManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.gameManager = new GameManager(this);
        this.skillManager = new SkillManager(this);
        this.ultimateManager = new UltimateManager(this);
        this.testFieldManager = new TestFieldManager(this);
        this.testFieldManager.reload();
        this.tutorialManager = new TutorialManager(this);
        this.compManager = new CompManager(this);
        this.mapManager = new MapManager(this);
        this.lobbyManager = new LobbyManager(this);
        this.gameListeners = new GameListeners(this);
        this.getServer().getPluginManager().registerEvents((Listener)this.gameListeners, (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new WandListener(this.selectionTool), (Plugin)this);
        PluginCommand cmd = this.getCommand("bloxarena");
        BloxArenaCommand handler = new BloxArenaCommand(this);
        if (cmd != null) {
            cmd.setExecutor((CommandExecutor)handler);
            cmd.setTabCompleter((TabCompleter)handler);
        }
        this.getLogger().info("BAII WoNG v" + this.getDescription().getVersion() + " \u6709\u52b9\u5316\u5b8c\u4e86");
    }

    public void onDisable() {
        if (this.gameManager != null && this.gameManager.getState() != GameState.WAITING) {
            this.gameManager.returnAllToLobby();
        }
        if (this.testFieldManager != null) {
            this.testFieldManager.disable();
        }
        if (this.botManager != null) {
            this.botManager.clearAll();
        }
        if (this.titleManager != null) {
            this.titleManager.saveAll();
        }
        this.getLogger().info("BAII WoNG \u7121\u52b9\u5316");
    }

    public GameManager getGameManager() {
        return this.gameManager;
    }

    public LobbyManager getLobbyManager() {
        return this.lobbyManager;
    }

    public MapManager getMapManager() {
        return this.mapManager;
    }

    public GameListeners getGameListeners() {
        return this.gameListeners;
    }

    public SelectionTool getSelectionTool() {
        return this.selectionTool;
    }

    public KitEditorGUI getKitEditorGUI() {
        return this.kitEditorGUI;
    }

    public KitInfoGUI getKitInfoGUI() {
        return this.kitInfoGUI;
    }

    public BotManager getBotManager() {
        return this.botManager;
    }

    public StatsManager getStatsManager() {
        return this.statsManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    public SkillManager getSkillManager() {
        return this.skillManager;
    }

    public UltimateManager getUltimateManager() {
        return this.ultimateManager;
    }

    public TestFieldManager getTestFieldManager() {
        return this.testFieldManager;
    }

    public TutorialManager getTutorialManager() {
        return this.tutorialManager;
    }

    public CompManager getCompManager() {
        return this.compManager;
    }

    public BATitleManager getTitleManager() {
        return this.titleManager;
    }

    public Set<UUID> getOobImmunePlayers() {
        return this.oobImmunePlayers;
    }
}

