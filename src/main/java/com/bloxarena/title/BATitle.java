package com.bloxarena.title;

import com.bloxarena.kit.KitType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.configuration.ConfigurationSection;

public class BATitle {

    public enum Type {
        MANUAL, PLAYER_KILL, DEATHS, WIN, DAMAGE_DEAL, KIT_USE, KIT_MASTERY, FIRST_JOIN
    }

    public static class Cond {
        public Type type = Type.MANUAL;
        public int count = 1;
        public final List<String> targets = new ArrayList<String>();

        public boolean targetMatches(String kitName) {
            return this.targets.isEmpty() || this.targets.contains(kitName);
        }

        public String describe() {
            switch (this.type) {
                case PLAYER_KILL -> {
                    return "対人キル " + this.count + "回";
                }
                case DEATHS -> {
                    return "死亡 " + this.count + "回";
                }
                case WIN -> {
                    return "試合勝利 " + this.count + "回";
                }
                case DAMAGE_DEAL -> {
                    return "累計与ダメージ " + this.count;
                }
                case KIT_USE -> {
                    return "キット「" + this.kitNames() + "」で " + this.count + "回出場";
                }
                case KIT_MASTERY -> {
                    if (this.targets.isEmpty()) {
                        return "いずれかのキットで熟練度Lv." + this.count + "到達";
                    }
                    return "キット「" + this.kitNames() + "」の熟練度Lv." + this.count + "到達";
                }
                case FIRST_JOIN -> {
                    return "サーバーに初参加";
                }
                default -> {
                    return "運営付与";
                }
            }
        }

        private String kitNames() {
            if (this.targets.isEmpty()) {
                return "すべて";
            }
            List<String> names = new ArrayList<String>();
            for (String t : this.targets) {
                try {
                    names.add(KitType.valueOf(t.toUpperCase(Locale.ROOT)).getName());
                } catch (IllegalArgumentException ex) {
                    names.add(t);
                }
            }
            return String.join("・", names);
        }

        static Cond load(ConfigurationSection c) {
            Cond cond = new Cond();
            try {
                cond.type = Type.valueOf(c.getString("type", "MANUAL").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return null;
            }
            cond.count = Math.max(1, c.getInt("count", 1));
            cond.targets.addAll(c.getStringList("targets"));
            return cond;
        }
    }

    public final String id;
    public String display;
    public String description;
    public boolean hidden;
    public final List<Cond> conditions = new ArrayList<Cond>();

    public BATitle(String id, String display, String description) {
        this.id = id;
        this.display = display;
        this.description = description == null ? "" : description;
    }

    public boolean isManualOnly() {
        if (this.conditions.isEmpty()) {
            return true;
        }
        for (Cond c : this.conditions) {
            if (c.type != Type.MANUAL) {
                return false;
            }
        }
        return true;
    }

    public static String key(int index, String titleId) {
        return index + ":" + titleId;
    }
}
