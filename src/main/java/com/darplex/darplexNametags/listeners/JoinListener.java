package com.darplex.darplexNametags.listeners;

import com.darplex.darplexNametags.DarplexNametags;
import com.darplex.darplexNametags.counters.refreshTickers.NametagRefreshTicker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;

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

    // Delays after 0.5 second, runs every second!
    private NametagRefreshTicker getRefreshTicker(UUID uuid) {
        return new NametagRefreshTicker(plugin, () -> refreshSelfView(uuid), 10L, 20L);
    }

    private void appendTagRefresh(UUID uuid) {
        getPlugin().getCounterManager()
                .add(uuid, getRefreshTicker(uuid));
    }

    private void log(String msg) {
        getPlugin().getLogger().log(Level.INFO, "JoinListener >> " + msg);
    }

    private void removeTagRefresh(UUID uuid) {
        getPlugin().getCounterManager()
                .cancel(uuid, NametagRefreshTicker.class);
    }

    // todo: use when possible! (for everything else basically)
    private void cancelAllCounters(UUID uuid) {
        getPlugin().getCounterManager().cancelCounters(uuid);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        createNametagIfNotExists(uuid);
        // refresh after quarter second! (fully joined in)
        appendTagRefresh(uuid);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeave(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        removeTagRefresh(uuid);
        cancelAllCounters(uuid);
        removeNametag(event.getPlayer().getUniqueId());
    }

}
