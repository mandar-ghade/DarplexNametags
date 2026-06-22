package com.darplex.darplexNametags.counters;

import com.darplex.darplexNametags.DarplexNametags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public class RainbowGradient implements Tickable {

    @NotNull @Getter DarplexNametags plugin;

    private int tick = 1;
    private BukkitTask task;

    private boolean needsReset() {
        return (tick % 10) == 0L;
    }

    @Override
    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (needsReset()) {
                    tick = 1;
                } else {
                    tick += 1;
                }
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
        // runs every second!
    }

    @Override
    public void stop() {
        task.cancel();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }

    public long getTick() {
        return tick;
    }
}
