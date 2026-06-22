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

    private UUID uuid = UUID.randomUUID();
    private int tick = 1;
    private BukkitRunnable task;

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick += 1;
                if ((tick % 10) == 0L) {
                    tick = 1;
                }
            }
        };
        // every second!
        task.runTaskTimer(getPlugin(), 0L, 20L);
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
