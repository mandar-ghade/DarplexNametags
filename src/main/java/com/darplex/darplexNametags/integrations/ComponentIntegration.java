package com.darplex.darplexNametags.integrations;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public interface ComponentIntegration {
    Component getDefaultNametag(UUID uuid);
    Component getCustomNametag(UUID owner, UUID viewer);
    void setCustomNametag(UUID owner, UUID viewer, Component newComponent);
}
