package com.darplex.darplexNametags.nametags;

import com.darplex.darplexNametags.DarplexNametags;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import me.tofaa.entitylib.wrapper.WrapperPerPlayerEntity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class View {
    // View belongs to a NameTag owner.
    // Each player who views it has their entity.
    @NotNull @Getter DarplexNametags plugin;
    @NotNull @Getter UUID owner;
    @NotNull @Getter WrapperPerPlayerEntity entity;
    // might do this instead in the future (to save space complexity):
//    @NotNUll Map<UUID, UUID> viewerToEntityMap;
//    index with: EntityLib.getApi().getEntity()
    @NotNull @Getter @Setter Component defaultComponent;

    // SAFE TO USE EVEN IF "User"'s text display doesn't exist.
    public void modifyDisplay(@NotNull User user, Consumer<TextDisplayMeta> meta) {
        this.entity.modify(user, (entity) -> meta.accept(entity.getEntityMeta(TextDisplayMeta.class)));
    }

    // SAFE TO USE EVEN IF "User"'s text display doesn't exist.
    public void modifyEntity(@NotNull User user, Consumer<WrapperEntity> entity) {
        this.entity.modify(user, entity);
    }

    public static TextDisplayMeta getMeta(WrapperEntity wrapperEntity) {
        return wrapperEntity.getEntityMeta(TextDisplayMeta.class);
    }

    // todo: figure out if User can ever be null
    private User playerToUser(@NotNull Player player) {
        return getPlugin().getPacketEventsAPI().getPlayerManager().getUser(player);
    }

    public Optional<Player> userToPlayer(@NotNull User user) {
        return Optional.ofNullable(Bukkit.getPlayer(user.getUUID()));
    }

    // Caution: Runs on the main thread
    public Optional<User> resolveUser(UUID viewerUUID) {
        return Optional.ofNullable(Bukkit.getPlayer(viewerUUID))
                .map(this::playerToUser);
    }

    public Optional<Location> getOwnerHeadLoc() {
        return Optional.ofNullable(Bukkit.getPlayer(owner))
                .map(Player::getLocation)
                // todo: work on the locs..
                .map((loc) -> loc.add(0, 2.5, 0))
                .map(SpigotConversionUtil::fromBukkitLocation);
    }

    public Optional<Location> getLoc() {
        return Optional.ofNullable(Bukkit.getPlayer(owner))
                .map(Player::getLocation)
                // 1.8 is usually ideal!
                .map((loc) -> loc.add(0, 2.1, 0))
                .map(loc -> {
                    loc.setPitch(0);
                    loc.setYaw(-180);
                    return loc;
                })
                .map(SpigotConversionUtil::fromBukkitLocation);
    }

    public boolean hasViewer(UUID viewer) {
        return entity.getEntities().containsKey(viewer);
    }

//    private void editPassengers(UUID viewer, BiConsumer<WrapperEntity, WrapperEntity> playerEntityConsumer) {
//        // Consumer: First argument = playerEntity, Second argument = viewerEntity.
//        Optional<WrapperEntity> viewerEntityOpt = entityOf(viewer);
//        if (viewerEntityOpt.isEmpty()) {
//            return;
//        }
//        WrapperEntity viewerEntity = viewerEntityOpt.get();
//        Optional.ofNullable(Bukkit.getPlayer(owner))
//                .map(Player::getEntityId)
//                .flatMap((playerEntityId) -> Optional.ofNullable(getPlugin().getEntityLibAPI().getEntity(playerEntityId)))
//                .ifPresent((playerEntity) -> playerEntityConsumer.accept(playerEntity, viewerEntity));
//    }
//
//    private void removePassenger(UUID viewer) {
//        editPassengers(viewer, (playerEntity, viewerEntity) -> {
//            if (playerEntity.hasPassenger(viewerEntity)) {
//                playerEntity.removePassenger(viewerEntity);
//            }
//        });
//    }
//
//    private void addPassenger(UUID viewer) {
//        // Refreshes passengers (even if it exists)
//        editPassengers(viewer, (playerEntity, viewerEntity) -> {
//            if (playerEntity.hasPassenger(viewerEntity)) {
//                playerEntity.removePassenger(viewerEntity);
//            }
//            playerEntity.addPassenger(viewerEntity);
//        });
//    }


    // Gets all virtual entity riders.
    private Set<Integer> getAllEntityRiders() {
        return entity.getEntities().values().stream()
                .map(WrapperEntity::getEntityId)
                .collect(Collectors.toSet());
    }

    private Set<Integer> getVanillaPassengers() {
        return getPlugin().getVirtualPassengerManager()
                .getPassengerMap()
                .computeIfAbsent(owner, (ignored) -> new HashSet<>());
    }

    private Set<Integer> getPassengersSet() {
        Set<Integer> all = getAllEntityRiders();
        all.addAll(getVanillaPassengers());
        return all;
    }

    public int[] getPassengerArray() {
        return getPassengersSet().stream().mapToInt(i -> i).toArray();
    }

    // Making this public might be a bad idea!
    public PacketWrapper<WrapperPlayServerSetPassengers> getPassengersPacket() {
        Optional<User> ownerUserOpt = resolveUser(owner);
        if (ownerUserOpt.isEmpty()) {
            Bukkit.getLogger().log(Level.SEVERE, "View >> Cannot send setPassengers packet for a vehicle who is offline!");
            return null;
        }
        User ownerUser = ownerUserOpt.get();
        return new WrapperPlayServerSetPassengers(
                ownerUser.getEntityId(),
                getPassengerArray()
        );
    }

//    private void addVirtualPassenger(WrapperEntity viewerEntity) {
//        getPlugin().getVirtualPassengerManager().getPassengerMap()
//            .compute(owner, (ignored, entityList) -> {
//                if (entityList == null) {
//                    return Set.of(viewerEntity.getEntityId());
//                } else {
//                    entityList.add(viewerEntity.getEntityId());
//                    return entityList;
//                }
//            });
//    }
//
//    private void removeVirtualPassenger(WrapperEntity viewerEntity) {
//        getPlugin().getVirtualPassengerManager().getPassengerMap()
//                .computeIfPresent(owner, (ignored, entityList) -> {
//                    entityList.remove(viewerEntity.getEntityId());
//                    return entityList;
//                });
//    }
//
//    public void addPassengerClean(UUID viewer) {
//        entityOf(viewer).ifPresent(this::addVirtualPassenger);
//    }
//
//    public void removePassengerClean(UUID viewer) {
//        entityOf(viewer).ifPresent(this::removeVirtualPassenger);
//    }
//
    private void logSpawnNametagFailed(String name) {
        // todo: make logging more verbose
        getPlugin().getLogger().log(Level.SEVERE, "View >> Spawn location of NameTag viewer: " +
                name + " not found. (Spawning failed)");
    }

    public void create(UUID viewer) {
        if (!ownerIsOnline() || hasViewer(viewer)) {
            return;
        }
        Optional<User> viewerUserOpt = resolveUser(viewer);
        if (viewerUserOpt.isEmpty()) {
            return;
        }
        User viewerUser = viewerUserOpt.get();
        // todo: display should be updated automatically!
//        modifyDisplay(viewerUser, (display) -> {
//            display.setText(defaultComponent);
//        });
        modifyEntity(viewerUser, (viewerEntity) -> {
            getOwnerHeadLoc().ifPresentOrElse(viewerEntity::spawn,
                    () -> logSpawnNametagFailed(viewerUser.getName()));
        });
        // Sends the packets (confusing af)
        entity.addViewer(viewerUser);
        // todo: check that add passenger works!
        // TODO: double check that the code works without `addPassengerClean`!
//        addPassengerClean(viewer);
        // mount packet!
        viewerUser.sendPacketSilently(getPassengersPacket());
    }

    public Component view(UUID viewer) {
        return getPlugin().getComponentIntegration().getCustomNametag(owner, viewer);
    }

    // Whoever is viewing the owner
    public Optional<WrapperEntity> entityOf(UUID viewer) { return Optional.ofNullable(entity.getEntities().get(viewer)); }

    @RequiredArgsConstructor
    public static class NewView {
        @NotNull @Getter Component text;
        @NotNull @Getter Location loc;
        @NotNull @Getter Boolean isVisible;
    }

    public Optional<Location> getLocation(UUID viewer) {
        return entityOf(viewer).map(WrapperEntity::getLocation);
    }

    public boolean ownerIsOnline() {
        return Optional.ofNullable(Bukkit.getPlayer(owner))
                .map(Player::isOnline)
                .orElse(false);
    }

    private void removeExistingEntity(UUID viewer) {
        // Perhaps offline, but entity is still present! (no sending packets)
        entityOf(viewer)
                .ifPresent((e) -> e.removeViewerSilently(viewer));
        entity.getEntities().remove(viewer);
    }

    // handles invisible case.
    // & ensures that they don't show up as passenger!
    private void makeEntityInvisible(@NotNull User viewerUser) {
        // Sends destroy packets
        entity.removeViewer(viewerUser);
        // Ensures it has cleanly been removed.
        // todo: check that UUIDs match cleanly between spigot and packetevents
        entity.getEntities().remove(viewerUser.getUUID());
    }

    public void editText(@NotNull User viewerUser, Component text) {
        // adds a new line afterwards for aesthetic!
        modifyDisplay(viewerUser, (displayMeta) -> {
            displayMeta.setText(text.appendNewline());
        });
    }

    private void editText(@NotNull User viewerUser, NewView newView) {
        modifyDisplay(viewerUser, (displayMeta) -> {
            displayMeta.setText(newView.getText());
        });
    }

    private void refreshEntityAndSpawn(@NotNull User viewerUser, NewView newView) {
        // this is a temporary kind of refresh (just destroying, no teleporting):
        // This does not change the entityId (nice)!
        entity.removeViewer(viewerUser);
        // todo: figure out how to refresh easily!
        modifyEntity(viewerUser, (e) -> {
            // despawn just in case it has already been spawned!
            e.despawn();
            e.spawn(newView.getLoc());
        });
        // may not be necessary!
        entity.addViewer(viewerUser);
        // notice...
        viewerUser.sendPacketSilently(getPassengersPacket());
    }

    public void update(UUID viewer, NewView newView) {
        // todo: remove owner check if not necessary
        if (!ownerIsOnline()) {
            return;
        }
        if (!hasViewer(viewer)) {
            create(viewer);
            // double check that update has location updated!
            update(viewer, newView); // recursive call!
            return;
        }
        // The cases below are just for existing views (entity state has already been stored)
        Optional<User> viewerUserOpt = resolveUser(viewer);
        // Case 1: offline
        if (viewerUserOpt.isEmpty()) {
            // todo: ensure the code works without the removePassenger!
//            removePassengerClean(viewer);
            removeExistingEntity(viewer);
            return;
        }
        User viewerUser = viewerUserOpt.get();
        // If visible, normal stuff happens!
        // handles invisible case. (USUALLY IF OFFLINE)
        if (!newView.getIsVisible()) {
            // destroys its cache completely.
            makeEntityInvisible(viewerUser);
            return;
        }
        // todo: perhaps just remake
        editText(viewerUser, newView);
        // todo: notice addPassenger is RIGHT before viewer is added!
        refreshEntityAndSpawn(viewerUser, newView);
//        addPassengerClean(viewer);
        // resend mount packet!
    }

    public void remove(UUID viewer) {
        if (!hasViewer(viewer)) {
            return;
        }
        Optional<User> viewerUserOpt = resolveUser(viewer);
        // offline case
        if (viewerUserOpt.isEmpty()) {
            removeExistingEntity(viewer);
        } else {
            // viewer == online

            // don't remove passenger is owner is offline
            // todo: double check that the code works without remove passengers

//            if (ownerIsOnline()) {
//                removePassengerClean(viewer);
//            }

            // despawn before clearing cache!
            modifyEntity(viewerUserOpt.get(), WrapperEntity::despawn);
            makeEntityInvisible(viewerUserOpt.get());
        }
    }

    public void shutdown() {
        for (UUID uuid : getEntity().getEntities().keySet()) {
            remove(uuid);
        }
    }

}
