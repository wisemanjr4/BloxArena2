package com.bloxarena.title;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class TitleData {

    public final UUID uuid;
    public final Map<String, Integer> counters = new HashMap<String, Integer>();
    public final Set<String> unlocked = new LinkedHashSet<String>();
    public String selected;

    public TitleData(UUID uuid) {
        this.uuid = uuid;
    }

    public int bump(String key, int amount) {
        int now = this.counters.getOrDefault(key, 0) + amount;
        this.counters.put(key, now);
        return now;
    }

    public int max(String key, int value) {
        int now = Math.max(this.counters.getOrDefault(key, 0), value);
        this.counters.put(key, now);
        return now;
    }

    public static TitleData load(File file, UUID uuid) {
        TitleData d = new TitleData(uuid);
        if (!file.exists()) {
            return d;
        }
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        d.selected = y.getString("selected");
        d.unlocked.addAll(y.getStringList("unlocked"));
        ConfigurationSection cs = y.getConfigurationSection("counters");
        if (cs != null) {
            for (String key : cs.getKeys(false)) {
                d.counters.put(key, cs.getInt(key));
            }
        }
        return d;
    }

    public void save(File file) {
        YamlConfiguration y = new YamlConfiguration();
        y.set("selected", this.selected);
        y.set("unlocked", new ArrayList<String>(this.unlocked));
        for (Map.Entry<String, Integer> e : this.counters.entrySet()) {
            y.set("counters." + e.getKey(), e.getValue());
        }
        try {
            y.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
