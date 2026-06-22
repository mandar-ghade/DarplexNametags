package com.darplex.darplexNametags.integrations;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface VanishIntegration {
    // Single source of truth for `Vanish` information!
    // Whether you can see a nametag (or tab) or not!
    // TODO: in the future, distinguish seeing "Tab" from seeing their "Nametag"
    // TODO: refactor: Arg 1: viewer, Arg 2: audience
    // Owner == looker, Viewer == target (owner tries to look at viewer)
    boolean canSee(UUID owner, UUID viewer);
    void setCanSee(UUID owner, UUID viewer, boolean visible);
    void vanishPlayer(Player player);
    void unvanishPlayer(Player player);
    // can see all vanished users!
    boolean overridesVanish(UUID viewer);
    // add vanish override!
    void addVanishOverride(UUID viewer);
    // remove vanish override!
    void removeVanishOverride(UUID viewer);
}
