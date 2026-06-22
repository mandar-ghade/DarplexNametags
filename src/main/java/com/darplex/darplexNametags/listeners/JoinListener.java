package com.darplex.darplexNametags.listeners;

import com.darplex.darplexNametags.DarplexNametags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
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

    // Allows you to see everyone else's nametag!
    private void refreshSelfView(UUID uuid) {
        // what you see
        getPlugin().getNametagManager().refreshNametagAndUpdateView(uuid);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        createNametagIfNotExists(uuid);
        // refresh after 1 seconds! (fully joined in)
        Bukkit.getScheduler().runTaskLater(getPlugin(),
                () -> refreshSelfView(uuid),
                10L
        );
//        refreshSelfView(uuid);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeave(PlayerQuitEvent event) { removeNametag(event.getPlayer().getUniqueId()); }

}
