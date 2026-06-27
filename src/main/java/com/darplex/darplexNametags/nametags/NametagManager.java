package com.darplex.darplexNametags.nametags;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.darplex.darplexNametags.DarplexNametags;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * NametagManager
 */
public class NametagManager {

    final DarplexNametags plugin;
    final Team noNametagTeam;
    final ConcurrentHashMap<UUID, Nametag> nametagMap;

    public NametagManager(final DarplexNametags plugin) {
        this.plugin = plugin;
        Scoreboard sb = plugin.getServer().getScoreboardManager().getMainScoreboard();
        this.noNametagTeam = sb.getTeam("no_nametag") != null
                ? sb.getTeam("no_nametag")
                : sb.registerNewTeam("no_nametag");
        this.noNametagTeam.setNameTagVisibility(NameTagVisibility.NEVER);
        this.nametagMap = new ConcurrentHashMap<>();
    }

    public boolean nametagExists(UUID uuid) {
        return nametagMap.containsKey(uuid);
    }

    public Optional<Nametag> get(UUID uuid) {
        return Optional.ofNullable(nametagMap.get(uuid));
    }

    public int countSize() {
        return nametagMap.size();
    }

    public boolean createIfNotExists(UUID uuid) {
        if (nametagExists(uuid)) {
            return false;
        }
        nametagMap.put(uuid, new Nametag(plugin, uuid, noNametagTeam));
        return true;
    }

    // Makes sure "offline user" doesn't have an entity for anyone else's nametag
    // todo: playtest
    private void removeOfflineViewer(UUID uuid) {
        for (var nt : nametagMap.values() ) {
            nt.getView().removeSilent(uuid);
        }
        // before:
//        nametagMap.values().stream()
//                .map(Nametag::getView)
//                .filter(view -> view.hasViewer(uuid))
//                .forEach((v) -> v.removeExistingEntity(uuid));
    }

    public boolean delete(UUID uuid) {
        Nametag nametag = nametagMap.remove(uuid);
        if (nametag == null) {
            return false;
        } else {
            // so you can't see anyone else (important stuff)
            removeOfflineViewer(uuid);
            // no one can see you
            nametag.shutdown();
        }
        return true;
    }

    public boolean refreshNametag(UUID uuid) {
        // Call this if you only want to change
        // how other users see you
        Nametag nametag = nametagMap.get(uuid);
        if (nametag == null) {
            return false;
        }
        return nametag.refresh();
    }

    //
    // Refreshes what user with "UUID" "sees".
    //
    // O(n) optimization of `refreshAllDisplays`
    // n = # online players
    //
    //
    // refreshes "self" view of everyone else's tags
    // (for ex. when you use setRank you may not be allowed to see certain people)
    //
    // Affects what "you" see
    public boolean refreshNametagAndUpdateView(UUID uuid) {
        Nametag receiver = nametagMap.get(uuid);
        if (receiver == null) {
            return false;
        }
        receiver.refresh();
        boolean anyFailure = Bukkit.getOnlinePlayers()
                .stream()
                .map((sender) -> this.nametagMap.get(sender.getUniqueId()))
                .map(receiver::refreshSelfView)
                .anyMatch((success) -> !success);
        return !anyFailure;
    }

    // O(n^2), where n = # online players
    //
    // Use `refreshNametagAndUpdateView` instead
    @Deprecated
    public void refreshAllNametags() {
        // Call this if you want to also change "what you see"
        // (SetRank)
        plugin.getServer().getOnlinePlayers().stream()
                .forEach((player) -> Optional
                        .ofNullable(nametagMap.get(player.getUniqueId()))
                        .ifPresent((nametag) -> nametag.refresh()));
    }

    public void shutdown() {
        nametagMap.values().forEach(Nametag::shutdown);
    }
}
