package com.darplex.darplexNametags.integrations;

import com.darplex.darplexNametags.DarplexNametags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class DefaultVanishIntegration implements VanishIntegration {

    @NotNull @Getter DarplexNametags plugin;

    // UUID 1: Owner (viewer), UUID 2 : target
    // Owner looks at target
    Map<Map.Entry<UUID, UUID>, Boolean> overrides = new ConcurrentHashMap<>();
    @Getter Set<UUID> vanishOverrides = new HashSet<>();

    @Override
    public boolean canSee(UUID owner, UUID target) {
        return overrides.getOrDefault(Map.entry(owner, target), true);
    }

    @Override
    public void setCanSee(UUID owner, UUID viewer, boolean visible) {
        overrides.put(Map.entry(owner, viewer), visible);
    }

    @Override
    public void vanishPlayer(Player player) {
        Bukkit.getOnlinePlayers().stream()
                .filter((target) -> player.getUniqueId() != target.getUniqueId()
                        && !canSee(target.getUniqueId(), player.getUniqueId())
                ).forEach((target) -> target.hidePlayer(getPlugin(), player));
    }

    @Override
    public void unvanishPlayer(Player player) {
        Bukkit.getOnlinePlayers().stream()
                .filter((target) -> player.getUniqueId() != target.getUniqueId()
                        && canSee(target.getUniqueId(), player.getUniqueId())
                ).forEach((target) -> target.showPlayer(getPlugin(), player));
    }

    @Override
    public boolean overridesVanish(UUID viewer) {
        return vanishOverrides.contains(viewer);
    }

    @Override
    public void addVanishOverride(UUID viewer) {
        vanishOverrides.add(viewer);
    }

    @Override
    public void removeVanishOverride(UUID viewer) {
        vanishOverrides.remove(viewer);
    }


}
