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
import com.bloxarena.song.NbsPlayer;
import com.bloxarena.stats.StatsManager;
import com.bloxarena.test.TestFieldManager;
import com.bloxarena.tutorial.TutorialManager;
import com.bloxarena.util.SelectionTool;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private TestFieldManager testFieldManager;
    private TutorialManager tutorialManager;
    private final Set<UUID> oobImmunePlayers = new HashSet<UUID>();
    private final List<NbsPlayer> songs = new ArrayList<NbsPlayer>();
    private NbsPlayer currentBgm;

    public void onEnable() {
        File[] nbsFiles;
        this.saveDefaultConfig();
        this.selectionTool = new SelectionTool();
        this.kitEditorGUI = new KitEditorGUI(this);
        this.kitInfoGUI = new KitInfoGUI(this);
        this.botManager = new BotManager(this);
        this.statsManager = new StatsManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.gameManager = new GameManager(this);
        this.skillManager = new SkillManager(this);
        this.testFieldManager = new TestFieldManager(this);
        this.testFieldManager.reload();
        this.tutorialManager = new TutorialManager(this);
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
        File songsDir = new File(this.getDataFolder(), "songs");
        if (!songsDir.exists()) {
            songsDir.mkdirs();
        }
        if ((nbsFiles = songsDir.listFiles((d, n) -> n.toLowerCase().endsWith(".nbs"))) != null) {
            for (File f : nbsFiles) {
                try {
                    this.songs.add(new NbsPlayer(f.getName().replace(".nbs", ""), f, (Plugin)this));
                    this.getLogger().info("BGM loaded: " + f.getName());
                }
                catch (Exception e) {
                    this.getLogger().warning("Failed to load NBS: " + f.getName() + " - " + e.getMessage());
                }
            }
        }
    }

    public void onDisable() {
        if (this.gameManager != null && this.gameManager.getState() != GameState.WAITING) {
            this.gameManager.returnAllToLobby();
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

    public TestFieldManager getTestFieldManager() {
        return this.testFieldManager;
    }

    public TutorialManager getTutorialManager() {
        return this.tutorialManager;
    }

    public Set<UUID> getOobImmunePlayers() {
        return this.oobImmunePlayers;
    }

    public List<NbsPlayer> getSongs() {
        return this.songs;
    }

    public NbsPlayer getCurrentBgm() {
        return this.currentBgm;
    }

    public void setCurrentBgm(NbsPlayer bgm) {
        this.currentBgm = bgm;
    }
}

