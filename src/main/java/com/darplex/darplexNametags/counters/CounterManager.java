package com.darplex.darplexNametags.counters;

import com.darplex.darplexNametags.DarplexNametags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class CounterManager {
    // `TickCounters` cannot be shared across users! (all individualized)
    // 1) (userUUID, RainbowTicker.class) -> RainbowTicker instance!
    // 2) (userUUID) -> (RainbowTicker.class, ...)
    @NotNull @Getter DarplexNametags plugin;
    @Getter Map<Map.Entry<UUID, Class<?>>, Tickable> tickables = new ConcurrentHashMap<>();
    @Getter Map<UUID, Set<Class<?>>> userClasses = new ConcurrentHashMap<>();

    public <T extends Tickable> void add(UUID user, T tickable) {
//        getPlugin().getLogger().info("CounterManager: Checking: " + user + " clazz: " + tickable.getClass());
        Map.Entry<UUID, Class<?>> entry = Map.entry(user, tickable.getClass());
        if (tickables.containsKey(entry)) { // prevents duplicates!
            getPlugin().getLogger().info("CounterManager: Error! Duplicate: " + user + " clazz: " + tickable.getClass());
            getPlugin().getLogger().info("New len: " + userClasses.get(user).size());
            return;
        }
        userClasses.computeIfAbsent(user, (ignored) -> new HashSet<>())
                .add(tickable.getClass());
        tickables.put(entry, tickable);
        tickable.start();
    }

    public <T extends Tickable> void cancel(UUID user, Class<T> clazz) {
        get(user, clazz).ifPresent(this::cancelCounter);
    }

    private <T extends Tickable> void cancelCounter(T counter) {
        if (!counter.isCancelled()) {
            counter.stop();
        }
    }

    // todo: check casting!
    public <T extends Tickable> Optional<T> get(UUID user, Class<T> clazz) {
        return Optional.ofNullable((T) tickables.get(Map.entry(user, clazz)));
    }

    public void cancelCounters(UUID user) {
        if (!userClasses.containsKey(user)) {
            return;
        }
        for (var counterClazz : userClasses.get(user)) {
            Tickable counter = tickables.remove(Map.entry(user, counterClazz));
            if (counter != null) {
//                getPlugin().getLogger().info("CounterManager: Cancelled task for: " + user + " class: " + counterClazz);
                cancelCounter(counter);
            }
        }
        userClasses.remove(user);
    }

    public void shutdown() {
        for (var user : userClasses.keySet()) {
            cancelCounters(user);
        }
        userClasses.clear();
    }
}
