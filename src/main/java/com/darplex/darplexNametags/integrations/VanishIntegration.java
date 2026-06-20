package com.darplex.darplexNametags.integrations;

import java.util.UUID;

public interface VanishIntegration {
    // Single source of truth for `Vanish` information!
    // Whether you can see a nametag (or tab) or not!
    // TODO: in the future, distinguish seeing "Tab" from seeing their "Nametag"
    boolean canSee(UUID owner, UUID viewer);
    void setCanSee(UUID owner, UUID viewer, boolean visible);
}
