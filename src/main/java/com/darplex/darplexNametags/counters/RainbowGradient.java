package com.darplex.darplexNametags.counters;

import com.darplex.darplexNametags.DarplexNametags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;

@RequiredArgsConstructor
public class RainbowGradient implements Tickable {

    @RequiredArgsConstructor
    public enum Speed {
        FAST(2L),
        NORMAL(10L),
        SLOW(20L);

        @NotNull @Getter final Long period;
    }

    @NotNull @Getter DarplexNametags plugin;
    @NotNull @Getter @Setter Speed speed;

    private int tick = 1;
    private BukkitTask task = null;

    @Override
    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick += 1;
            }
        }.runTaskTimer(getPlugin(), 0L, getSpeed().getPeriod());
        // runs every second!
        // runs every 0.25 seconds!
    }

    @Override
    public void stop() {
        if (task != null) task.cancel();
    }

    // isCancelled if task is null!
    @Override
    public boolean isCancelled() { return task == null || task.isCancelled(); }

    public long getTick() {
        return tick;
    }
}
