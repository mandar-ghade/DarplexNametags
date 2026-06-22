package com.darplex.darplexNametags.counters.refreshTickers;

import com.darplex.darplexNametags.DarplexNametags;
import com.darplex.darplexNametags.counters.RefreshTicker;
import org.jetbrains.annotations.NotNull;

// Literally just a copy of Refresh Ticker but named!
public class NametagRefreshTicker extends RefreshTicker {
    public NametagRefreshTicker(@NotNull DarplexNametags plugin, @NotNull RefreshTask refreshTask, @NotNull Long delay, @NotNull Long period) {
        super(plugin, refreshTask, delay, period);
    }
}
