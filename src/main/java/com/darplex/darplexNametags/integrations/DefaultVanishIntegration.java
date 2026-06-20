package com.darplex.darplexNametags.integrations;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultVanishIntegration implements VanishIntegration {

    Map<Map.Entry<UUID, UUID>, Boolean> overrides = new ConcurrentHashMap<>();

    @Override
    public boolean canSee(UUID owner, UUID viewer) {
        return overrides.getOrDefault(Map.entry(owner, viewer), true);
    }

    @Override
    public void setCanSee(UUID owner, UUID viewer, boolean visible) {
        overrides.put(Map.entry(owner, viewer), visible);
    }

}
