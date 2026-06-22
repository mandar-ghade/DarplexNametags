package com.darplex.darplexNametags.counters;

import com.darplex.darplexNametags.DarplexNametags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

@RequiredArgsConstructor
public abstract class RefreshTicker implements Tickable {
    @NotNull @Getter DarplexNametags plugin;
    @NotNull @Getter RefreshTask refreshTask;
    @NotNull @Getter Long delay;
    @NotNull @Getter Long period;
    private int tick = 1;
    private BukkitTask task = null;

    public interface RefreshTask {
        void run();
    }

    @Override
    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                refreshTask.run();
                tick += 1;
            }
        }.runTaskTimer(getPlugin(), getDelay(), getPeriod());
    }

    @Override
    public void stop() {
        getPlugin().getLogger().log(Level.INFO, "RainbowRefreahTicker >> Refresh ticker stopped!");
        if (task != null) task.cancel();
    }

    @Override
    public boolean isCancelled() {
        return task == null || task.isCancelled();
    }

    @Override
    public long getTick() {
        return tick;
    }
}
