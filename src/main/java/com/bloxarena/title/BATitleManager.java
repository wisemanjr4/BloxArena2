package com.bloxarena.title;

import com.bloxarena.BloxArenaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BATitleManager implements Listener {

    private final BloxArenaPlugin plugin;
    private final Map<String, BATitle> titles = new LinkedHashMap<String, BATitle>();
    private final Map<UUID, TitleData> data = new HashMap<UUID, TitleData>();
    private final TitleGUI gui;

    public BATitleManager(BloxArenaPlugin plugin) {
        this.plugin = plugin;
        this.loadTitles();
        this.gui = new TitleGUI(plugin, this);
        Bukkit.getPluginManager().registerEvents((Listener)this, (org.bukkit.plugin.Plugin)plugin);
    }

    public TitleGUI getGui() {
        return this.gui;
    }

    public Map<String, BATitle> getTitles() {
        return this.titles;
    }

    public BATitle get(String id) {
        return this.titles.get(id);
    }

    public void loadTitles() {
        this.titles.clear();
        File f = new File(this.plugin.getDataFolder(), "titles.yml");
        if (!f.exists()) {
            this.plugin.saveResource("titles.yml", false);
        }
        YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection root = y.getConfigurationSection("titles");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) {
                continue;
            }
            String display = s.getString("display");
            if (display == null) {
                this.plugin.getLogger().warning("称号 " + id + " に display がないためスキップ");
                continue;
            }
            BATitle t = new BATitle(id, display, s.getString("description", ""));
            t.hidden = s.getBoolean("hidden", false);
            ConfigurationSection condList = s.getConfigurationSection("conditions");
            if (condList != null) {
                for (String k : condList.getKeys(false)) {
                    ConfigurationSection cs = condList.getConfigurationSection(k);
                    if (cs == null) {
                        continue;
                    }
                    BATitle.Cond c = BATitle.Cond.load(cs);
                    if (c != null) {
                        t.conditions.add(c);
                    }
                }
            } else if (s.getConfigurationSection("condition") != null) {
                BATitle.Cond c = BATitle.Cond.load(s.getConfigurationSection("condition"));
                if (c != null) {
                    t.conditions.add(c);
                }
            } else if (!s.getString("type", "").isEmpty()) {
                BATitle.Cond c = BATitle.Cond.load(s);
                if (c != null) {
                    t.conditions.add(c);
                }
            }
            this.titles.put(id, t);
        }
        this.plugin.getLogger().info(this.titles.size() + " 個の称号を読み込み");
    }

    /* ---------- プレイヤーデータ ---------- */

    public TitleData data(UUID u) {
        return this.data.computeIfAbsent(u, k -> TitleData.load(this.dataFile(u), u));
    }

    public TitleData data(Player p) {
        return this.data(p.getUniqueId());
    }

    private File dataFile(UUID u) {
        return new File(this.plugin.getDataFolder(), "titledata" + File.separator + u + ".yml");
    }

    public void unload(Player p) {
        TitleData d = this.data.remove(p.getUniqueId());
        if (d != null) {
            d.save(this.dataFile(p.getUniqueId()));
        }
    }

    public void save(Player p) {
        if (this.data.containsKey(p.getUniqueId())) {
            this.data(p).save(this.dataFile(p.getUniqueId()));
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, TitleData> e : this.data.entrySet()) {
            e.getValue().save(this.dataFile(e.getKey()));
        }
    }

    /* ---------- 進捗と解放 ---------- */

    public void progress(Player p, BATitle.Type type, Predicate<BATitle.Cond> matcher, int amount) {
        TitleData d = this.data(p);
        for (BATitle t : this.titles.values()) {
            if (d.unlocked.contains(t.id)) {
                continue;
            }
            boolean touched = false;
            for (int i = 0; i < t.conditions.size(); ++i) {
                BATitle.Cond c = t.conditions.get(i);
                String k = BATitle.key(i, t.id);
                if (c.type != type || d.counters.getOrDefault(k, 0) >= c.count) {
                    continue;
                }
                if (!matcher.test(c)) {
                    continue;
                }
                d.bump(k, amount);
                touched = true;
            }
            if (touched && this.allSatisfied(t, d)) {
                this.unlock(p, t, d);
            }
        }
    }

    public void progressMax(Player p, BATitle.Type type, Predicate<BATitle.Cond> matcher, int value) {
        TitleData d = this.data(p);
        for (BATitle t : this.titles.values()) {
            if (d.unlocked.contains(t.id)) {
                continue;
            }
            boolean touched = false;
            for (int i = 0; i < t.conditions.size(); ++i) {
                BATitle.Cond c = t.conditions.get(i);
                String k = BATitle.key(i, t.id);
                if (c.type != type || d.counters.getOrDefault(k, 0) >= c.count) {
                    continue;
                }
                if (!matcher.test(c)) {
                    continue;
                }
                d.max(k, value);
                touched = true;
            }
            if (touched && this.allSatisfied(t, d)) {
                this.unlock(p, t, d);
            }
        }
    }

    private boolean allSatisfied(BATitle t, TitleData d) {
        if (t.conditions.isEmpty()) {
            return false;
        }
        for (int i = 0; i < t.conditions.size(); ++i) {
            if (d.counters.getOrDefault(BATitle.key(i, t.id), 0) < t.conditions.get(i).count) {
                return false;
            }
        }
        return true;
    }

    public void onKill(Player p) {
        this.progress(p, BATitle.Type.PLAYER_KILL, c -> true, 1);
    }

    public void onDeath(Player p) {
        this.progress(p, BATitle.Type.DEATHS, c -> true, 1);
    }

    public void onWin(Player p) {
        this.progress(p, BATitle.Type.WIN, c -> true, 1);
    }

    public void onDamage(Player p, double dmg) {
        if (dmg <= 0.0) {
            return;
        }
        this.progress(p, BATitle.Type.DAMAGE_DEAL, c -> true, (int)Math.round(dmg));
    }

    public void onKitUse(Player p, String kitName, int masteryLevel) {
        this.progress(p, BATitle.Type.KIT_USE, c -> c.targetMatches(kitName), 1);
        this.progressMax(p, BATitle.Type.KIT_MASTERY, c -> c.targetMatches(kitName), masteryLevel);
    }

    public void unlock(Player p, BATitle t) {
        TitleData d = this.data(p);
        if (d.unlocked.add(t.id)) {
            this.unlock(p, t, d);
        }
    }

    private void unlock(Player p, BATitle t, TitleData d) {
        if (!d.unlocked.add(t.id)) {
            return;
        }
        Bukkit.broadcastMessage("\u00a76[称号] \u00a7e" + p.getName() + " \u00a77が称号 \u00a7f" + t.display + " \u00a77を解放！");
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        p.showTitle(net.kyori.adventure.title.Title.title(
            net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize("\u00a76\u00a7l称号解放！"),
            net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize("\u00a7f" + t.display),
            net.kyori.adventure.title.Title.Times.times(
                java.time.Duration.ofMillis(200L), java.time.Duration.ofSeconds(3L), java.time.Duration.ofMillis(500L))));
        if (d.selected == null) {
            d.selected = t.id;
            this.refreshTabName(p);
        }
        d.save(this.dataFile(p.getUniqueId()));
    }

    /* ---------- 表示 ---------- */

    public String selectedDisplay(UUID u) {
        TitleData d = this.data(u);
        if (d.selected == null) {
            return null;
        }
        BATitle t = this.titles.get(d.selected);
        if (t == null || !d.unlocked.contains(d.selected)) {
            return null;
        }
        return t.display;
    }

    public void refreshTabName(Player p) {
        String t = this.selectedDisplay(p.getUniqueId());
        if (t != null) {
            p.setPlayerListName("\u00a77[" + t + "\u00a77] \u00a7f" + p.getName());
        } else {
            p.setPlayerListName(p.getName());
        }
    }

    public int progressOf(Player p, BATitle t, int i) {
        return Math.min(this.data(p).counters.getOrDefault(BATitle.key(i, t.id), 0), t.conditions.get(i).count);
    }

    /* ---------- イベント ---------- */

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        this.data(p);
        this.progress(p, BATitle.Type.FIRST_JOIN, c -> true, 1);
        this.refreshTabName(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        this.unload(e.getPlayer());
    }
}
