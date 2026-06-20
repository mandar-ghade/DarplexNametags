package com.darplex.darplexNametags.integrations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Should be thread safe!
public class DefaultComponentIntegration implements ComponentIntegration {

    private Player getPlayer(@NotNull UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    // Single source of truth for Components!!! (Tab & Nametag sync)
    // UUID 1 = Owner, UUID 2 = Viewer
    Map<Map.Entry<UUID, UUID>, Component> overrides = new ConcurrentHashMap<>();

    // Default component on the main thread
    @Override
    public Component getDefaultNametag(UUID uuid) {
        return getPlayer(uuid).name().color(TextColor.color(NamedTextColor.WHITE.asHSV()));
    }

    @Override
    public Component getCustomNametag(UUID owner, UUID viewer) {
        // TODO: customize!
        return overrides.getOrDefault(Map.entry(owner, viewer), getDefaultNametag(owner));
        // (owner, viewer) -> Component
    }

    @Override
    public void setCustomNametag(UUID owner, UUID viewer, Component newComponent) {
        overrides.put(Map.entry(owner, viewer), newComponent);
    }
}
