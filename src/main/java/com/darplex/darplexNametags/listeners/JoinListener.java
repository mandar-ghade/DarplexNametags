package com.darplex.darplexNametags.listeners;

import com.darplex.darplexNametags.DarplexNametags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public class JoinListener implements Listener {

    @NotNull @Getter DarplexNametags plugin;

    private void createNametagIfNotExists(UUID uuid) {
        getPlugin().getNametagManager().createIfNotExists(uuid);
    }

    private void removeNametag(UUID uuid) {
        getPlugin().getNametagManager().delete(uuid);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        createNametagIfNotExists(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeave(PlayerQuitEvent event) { removeNametag(event.getPlayer().getUniqueId()); }

}
