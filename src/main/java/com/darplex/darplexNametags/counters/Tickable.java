package com.darplex.darplexNametags.counters;

import java.util.UUID;

public interface Tickable {
    UUID getUUID();
    void start();
    void stop();
    boolean isCancelled();
    long getTick();
}
