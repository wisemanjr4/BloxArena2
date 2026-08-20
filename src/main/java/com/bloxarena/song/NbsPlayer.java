/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.bloxarena.song;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class NbsPlayer {
    private final String name;
    private final List<int[]> notes = new ArrayList<int[]>();
    private int tempo = 1000;
    private int songLength = 0;
    private BukkitRunnable currentTask;
    private final Plugin plugin;

    public NbsPlayer(String name, File file, Plugin plugin) {
        this.name = name;
        this.plugin = plugin;
        try {
            this.parse(file);
        }
        catch (Exception e) {
            Bukkit.getLogger().warning("[BloxArenaII] Failed to load NBS: " + name);
            this.notes.clear();
            this.tempo = 1000;
            this.songLength = 0;
        }
    }

    private void parse(File file) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        in.readShort();
        int version = in.readByte() & 0xFF;
        in.readByte();
        this.songLength = in.readShort() & 0xFFFF;
        int layers = in.readShort() & 0xFFFF;
        this.skipString(in);
        this.skipString(in);
        this.skipString(in);
        this.skipString(in);
        this.tempo = Math.max(100, in.readShort() & 0xFFFF);
        in.skipBytes(23);
        this.skipString(in);
        in.readByte();
        in.readByte();
        in.readShort();
        if (version >= 1) {
            int customCount = in.readShort() & 0xFFFF;
            for (int i = 0; i < customCount; ++i) {
                this.skipString(in);
                this.skipString(in);
                in.readByte();
                in.readByte();
            }
        }
        for (int i = 0; i < layers; ++i) {
            this.skipString(in);
            in.readByte();
            in.readByte();
            in.readByte();
        }
        short prevTick = -1;
        try {
            short tick;
            while ((tick = in.readShort()) > prevTick) {
                prevTick = tick;
                short instrument = in.readShort();
                short key = in.readShort();
                this.notes.add(new int[]{tick, instrument & 0xFFFF, key & 0xFFFF});
            }
        }
        catch (EOFException e) {
            // empty catch block
        }
        in.close();
        Collections.sort(this.notes, Comparator.comparingInt(a -> a[0]));
    }

    private void skipString(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len > 0 && len < 100000) {
            in.skipBytes(len);
        }
    }

    public String getName() {
        return this.name;
    }

    public void play(final Collection<Player> listeners) {
        this.stop();
        if (this.notes.isEmpty()) {
            return;
        }
        int rawPerTick = (int)(2000L / (long)this.tempo);
        if (rawPerTick < 1) {
            rawPerTick = 1;
        }
        final int perTick = rawPerTick;
        this.currentTask = new BukkitRunnable(){
            int serverTick = 0;
            int noteIdx = 0;
            int prevSongTick = -1;

            public void run() {
                int songTick = this.serverTick / perTick;
                ++this.serverTick;
                if (songTick == this.prevSongTick) {
                    return;
                }
                this.prevSongTick = songTick;
                while (this.noteIdx < NbsPlayer.this.notes.size() && NbsPlayer.this.notes.get(this.noteIdx)[0] <= songTick) {
                    int[] n = NbsPlayer.this.notes.get(this.noteIdx++);
                    Sound snd = NbsPlayer.this.getInstrument(n[1]);
                    float pitch = NbsPlayer.this.getPitch(n[2]);
                    for (Player p : listeners) {
                        if (!p.isOnline()) continue;
                        p.playSound(p.getLocation(), snd, 0.8f, pitch);
                    }
                }
                if (songTick > NbsPlayer.this.songLength + 10) {
                    this.cancel();
                    NbsPlayer.this.currentTask = null;
                }
            }
        };
        this.currentTask.runTaskTimer(this.plugin, 0L, 1L);
    }

    public void stop() {
        if (this.currentTask != null) {
            this.currentTask.cancel();
            this.currentTask = null;
        }
    }

    private Sound getInstrument(int id) {
        return switch (id) {
            case 0 -> Sound.BLOCK_NOTE_BLOCK_HARP;
            case 1 -> Sound.BLOCK_NOTE_BLOCK_BASS;
            case 2 -> Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case 3 -> Sound.BLOCK_NOTE_BLOCK_SNARE;
            case 4 -> Sound.BLOCK_NOTE_BLOCK_HAT;
            case 5 -> Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case 6 -> Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case 7 -> Sound.BLOCK_NOTE_BLOCK_BELL;
            case 8 -> Sound.BLOCK_NOTE_BLOCK_CHIME;
            case 9 -> Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case 10 -> Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
            case 11 -> Sound.BLOCK_NOTE_BLOCK_COW_BELL;
            case 12 -> Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case 13 -> Sound.BLOCK_NOTE_BLOCK_BIT;
            case 14 -> Sound.BLOCK_NOTE_BLOCK_BANJO;
            case 15 -> Sound.BLOCK_NOTE_BLOCK_PLING;
            default -> Sound.BLOCK_NOTE_BLOCK_HARP;
        };
    }

    private float getPitch(int key) {
        int k;
        for (k = key; k < 33; k += 12) {
        }
        while (k > 57) {
            k -= 12;
        }
        return (float)Math.pow(2.0, (double)(k - 45) / 12.0);
    }
}

