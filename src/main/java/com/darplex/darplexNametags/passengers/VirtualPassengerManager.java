package com.darplex.darplexNametags.passengers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class VirtualPassengerManager {
    @Getter Map<UUID, Set<Integer>> passengerMap = new ConcurrentHashMap<>();

    public void replaceAll(UUID uuid, Set<Integer> ints) {
        if (passengerMap.containsKey(uuid)) {
            passengerMap.replace(uuid, ints);
        }
    }

}
