package com.darplex.darplexNametags.integrations;

import com.darplex.darplexNametags.DarplexNametags;
import com.darplex.darplexNametags.component.DarplexComponent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Should be thread safe!
@RequiredArgsConstructor
public class DefaultComponentIntegration implements ComponentIntegration {

    @NotNull @Getter DarplexNametags plugin;
    @Getter MiniMessage mm = MiniMessage.miniMessage();

    private Player getPlayer(@NotNull UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    // Single source of truth for Components!!! (Tab & Nametag sync)
    // UUID 1 = Owner, UUID 2 = Viewer
    Map<Map.Entry<UUID, UUID>, DarplexComponent> overrides = new ConcurrentHashMap<>();

    // Default component on the main thread
    @Override
    public Component getDefaultNametag(UUID uuid) {
        return getPlayer(uuid).name();
    }

    // nice and rainbow!
    public DarplexComponent getDefaultNametagDP(UUID uuid) {
        return DarplexComponent
                .from(mm, getDefaultNametag(uuid))
                .rainbow(getPlugin(), uuid);
    }

    @Override
    public Component getCustomNametag(UUID owner, UUID viewer) {
        // TODO: customize!
        // (owner, viewer) -> Component
        return overrides.getOrDefault(Map.entry(owner, viewer), getDefaultNametagDP(owner))
                .resolve();
    }

    @Override
    public void setCustomNametag(UUID owner, UUID viewer, Component newComponent) {
        overrides.put(Map.entry(owner, viewer), DarplexComponent.from(mm, newComponent));
    }
}
