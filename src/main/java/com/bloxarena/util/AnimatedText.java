package com.bloxarena.util;

import java.util.Collection;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class AnimatedText {
    private static final String[] WAVE_COLORS = {"\u00a7f", "\u00a7b", "\u00a7d", "\u00a75", "\u00a7c", "\u00a7e", "\u00a7a", "\u00a7e"};

    private AnimatedText() {
    }

    private static String plain(String text) {
        return text.replaceAll("\u00a7.", "");
    }

    public static String colorWave(String text, int frame) {
        String body = plain(text);
        StringBuilder sb = new StringBuilder(body.length() * 2);
        int base = Math.floorMod(frame, WAVE_COLORS.length);
        for (int i = 0; i < body.length(); ++i) {
            sb.append(WAVE_COLORS[(base + i) % WAVE_COLORS.length]).append(body.charAt(i));
        }
        return sb.toString();
    }

    public static void typewriter(Plugin plugin, Collection<Player> players, String title, String sub, int ticksPerChar) {
        final String body = plain(sub);
        new BukkitRunnable() {
            int step = 0;

            public void run() {
                if (this.step > body.length()) {
                    this.cancel();
                    return;
                }
                for (Player p : players) {
                    if (p == null || !p.isOnline()) continue;
                    if (this.step == body.length()) {
                        p.sendTitle(title, sub, 5, 30, 10);
                    } else {
                        p.sendTitle(title, "\u00a7f" + body.substring(0, this.step), 0, ticksPerChar + 4, 0);
                    }
                }
                ++this.step;
            }
        }.runTaskTimer(plugin, 0L, ticksPerChar);
    }

    public static void wave(Plugin plugin, Collection<Player> players, String title, String sub, int frames) {
        new BukkitRunnable() {
            int f = 0;

            public void run() {
                if (this.f >= frames) {
                    this.cancel();
                    return;
                }
                boolean last = this.f == frames - 1;
                for (Player p : players) {
                    if (p == null || !p.isOnline()) continue;
                    p.sendTitle(last ? title : AnimatedText.colorWave(title, this.f), sub, last ? 3 : 0, last ? 30 : 4, last ? 10 : 0);
                }
                ++this.f;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public static void pop(Plugin plugin, Collection<Player> players, String title, String sub, int frames) {
        new BukkitRunnable() {
            int f = 0;

            public void run() {
                if (this.f >= frames) {
                    this.cancel();
                    return;
                }
                boolean last = this.f == frames - 1;
                String frame = (this.f % 2 == 0 ? "\u00a7e\u00a7l" : "\u00a76\u00a7l") + AnimatedText.plain(title);
                for (Player p : players) {
                    if (p == null || !p.isOnline()) continue;
                    p.sendTitle(last ? title : frame, sub, last ? 3 : 0, last ? 25 : 4, last ? 8 : 0);
                }
                ++this.f;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public static void glitch(Plugin plugin, Collection<Player> players, String title, String sub, int frames) {
        final String body = plain(title);
        final Random rnd = new Random();
        new BukkitRunnable() {
            int f = 0;

            public void run() {
                if (this.f >= frames) {
                    this.cancel();
                    return;
                }
                boolean last = this.f == frames - 1;
                StringBuilder sb = new StringBuilder("\u00a7c\u00a7l");
                for (int i = 0; i < body.length(); ++i) {
                    char c = body.charAt(i);
                    if (c == ' ') {
                        sb.append(' ');
                    } else if (rnd.nextInt(3) == 0) {
                        sb.append("\u00a7k").append(c).append("\u00a7r\u00a7c\u00a7l");
                    } else {
                        sb.append(c);
                    }
                }
                for (Player p : players) {
                    if (p == null || !p.isOnline()) continue;
                    p.sendTitle(last ? title : sb.toString(), sub, last ? 3 : 0, last ? 30 : 3, last ? 10 : 0);
                }
                ++this.f;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public static void scroll(Plugin plugin, Collection<Player> players, String text, int ticks) {
        final String track = "      " + plain(text) + "      ";
        final int width = 34;
        new BukkitRunnable() {
            int f = 0;

            public void run() {
                if (this.f >= ticks) {
                    this.cancel();
                    return;
                }
                int span = Math.max(1, track.length() - width + 1);
                int start = this.f % span;
                String window = track.substring(start, Math.min(track.length(), start + width));
                String frame = WAVE_COLORS[(this.f / 3) % WAVE_COLORS.length] + "\u00a7l" + window;
                for (Player p : players) {
                    if (p == null || !p.isOnline()) continue;
                    p.sendActionBar(Component.text(frame));
                }
                ++this.f;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public static void rainbowBar(Plugin plugin, Collection<Player> players, String text, int ticks) {
        new BukkitRunnable() {
            int f = 0;

            public void run() {
                if (this.f >= ticks) {
                    this.cancel();
                    return;
                }
                String frame = AnimatedText.colorWave(text, this.f);
                for (Player p : players) {
                    if (p == null || !p.isOnline()) continue;
                    p.sendActionBar(Component.text(frame));
                }
                ++this.f;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
