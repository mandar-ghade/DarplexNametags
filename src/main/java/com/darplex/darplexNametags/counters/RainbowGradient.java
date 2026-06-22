package com.darplex.darplexNametags.counters;

import com.darplex.darplexNametags.DarplexNametags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;

@RequiredArgsConstructor
public class RainbowGradient implements Tickable {

    @NotNull @Getter DarplexNametags plugin;

    private int tick = 1;
    private BukkitTask task = null;

    private boolean needsReset() {
        return (tick % 10) == 0L;
    }

    @Override
    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick += 1;
            }
        }.runTaskTimer(getPlugin(), 0L, 5L);
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
