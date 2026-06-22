package com.darplex.darplexNametags.counters;

import java.util.UUID;

public interface Tickable {
    void start();
    void stop();
    boolean isCancelled();
    long getTick();
}
