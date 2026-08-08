package com.bloxarena.song;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.*;
import java.util.*;

public class NbsPlayer {

    private final String name;
    private final List<int[]> notes = new ArrayList<>();
    private int tempo = 1000;
    private int songLength = 0;
    private BukkitRunnable currentTask;
    private final Plugin plugin;

    public NbsPlayer(String name, File file, Plugin plugin) {
        this.name = name;
        this.plugin = plugin;
        try {
            parse(file);
        } catch (Exception e) {
            org.bukkit.Bukkit.getLogger().warning("[BloxArenaII] Failed to load NBS: " + name);
            notes.clear();
            tempo = 1000;
            songLength = 0;
        }
    }

    private void parse(File file) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        in.readShort();
        int version = in.readByte() & 0xFF;
        in.readByte();
        songLength = in.readShort() & 0xFFFF;
        int layers = in.readShort() & 0xFFFF;
        skipString(in);
        skipString(in);
        skipString(in);
        skipString(in);
        tempo = Math.max(100, in.readShort() & 0xFFFF);
        in.skipBytes(23);
        skipString(in);
        in.readByte();
        in.readByte();
        in.readShort();
        if (version >= 1) {
            int customCount = in.readShort() & 0xFFFF;
            for (int i = 0; i < customCount; i++) {
                skipString(in);
                skipString(in);
                in.readByte();
                in.readByte();
            }
        }
        for (int i = 0; i < layers; i++) {
            skipString(in);
            in.readByte();
            in.readByte();
            in.readByte();
        }
        int prevTick = -1;
        while (true) {
            try {
                short tick = in.readShort();
                if (tick <= prevTick) break;
                prevTick = tick;
                short instrument = in.readShort();
                short key = in.readShort();
                notes.add(new int[]{tick, instrument & 0xFFFF, key & 0xFFFF});
            } catch (EOFException e) {
                break;
            }
        }
        in.close();
        Collections.sort(notes, Comparator.comparingInt(a -> a[0]));
    }

    private void skipString(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len > 0 && len < 100000) in.skipBytes(len);
    }

    public String getName() { return name; }

    public void play(Collection<Player> listeners) {
        stop();
        if (notes.isEmpty()) return;
        int rawPerTick = (int)((long)2000 / tempo);
        if (rawPerTick < 1) rawPerTick = 1;
        final int perTick = rawPerTick;
        currentTask = new BukkitRunnable() {
            int serverTick = 0;
            int noteIdx = 0;
            int prevSongTick = -1;
            @Override public void run() {
                int songTick = serverTick / perTick;
                serverTick++;
                if (songTick == prevSongTick) return;
                prevSongTick = songTick;
                while (noteIdx < notes.size() && notes.get(noteIdx)[0] <= songTick) {
                    int[] n = notes.get(noteIdx++);
                    Sound snd = getInstrument(n[1]);
                    float pitch = getPitch(n[2]);
                    for (Player p : listeners) {
                        if (p.isOnline()) p.playSound(p.getLocation(), snd, 0.8f, pitch);
                    }
                }
                if (songTick > songLength + 10) {
                    cancel();
                    currentTask = null;
                }
            }
        };
        currentTask.runTaskTimer(plugin, 0L, 1L);
    }

    public void stop() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
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
        int k = key;
        while (k < 33) k += 12;
        while (k > 57) k -= 12;
        return (float) Math.pow(2.0, (k - 45) / 12.0);
    }
}
