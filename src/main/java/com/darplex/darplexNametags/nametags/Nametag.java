package com.darplex.darplexNametags.nametags;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

import com.darplex.darplexNametags.DarplexNametags;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.Getter;
import me.tofaa.entitylib.meta.display.AbstractDisplayMeta;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import me.tofaa.entitylib.wrapper.WrapperPerPlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;


import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Nametag
 */
public class Nametag {
    @NotNull @Getter final DarplexNametags plugin;
    @NotNull final UUID uuid;
    @NotNull final Team noNametagTeam;
    @NotNull @Getter View view; // todo: maybe not make this getter (too risky)

    // Managable container for `TextDisplay` views
    Nametag(@NotNull DarplexNametags plugin, @NotNull UUID uuid, @NotNull Team noNametagTeam) {
        this.uuid = uuid;
        this.plugin = plugin;
        this.noNametagTeam = noNametagTeam;
        this.view = new View(this.plugin,
                this.uuid,
                newWrapperEntity(this.plugin, this.uuid),
                this.plugin.getComponentIntegration().getDefaultNametag(this.uuid));
        create();
    }

    private static WrapperEntity defaultEntitySupplier(DarplexNametags plugin, UUID ownerUUID) {
        WrapperEntity wrapperEntity = new WrapperEntity(
                SpigotReflectionUtil.generateEntityId(),
                UUID.randomUUID(),
                EntityTypes.TEXT_DISPLAY
        );
        TextDisplayMeta textMeta = wrapperEntity.getEntityMeta(TextDisplayMeta.class);
        textMeta.setText(plugin.getComponentIntegration().getDefaultNametag(ownerUUID));
        textMeta.setBillboardConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        textMeta.setShadow(true);
        textMeta.setSeeThrough(false);
        textMeta.setBackgroundColor(0); // todo: make sure this is right
        textMeta.setViewRange(60f);
        // todo: figure out interpolation duration
//        d.setInterpolationDuration(3);
        return wrapperEntity;
    }

    private static WrapperPerPlayerEntity newWrapperEntity(DarplexNametags plugin, UUID ownerUUID) {
        return new WrapperPerPlayerEntity((ignored) -> defaultEntitySupplier(plugin, ownerUUID));
    }

    // Gets what "you" are supposed to see!
    private PacketWrapper<WrapperPlayServerPlayerInfoUpdate> getUpdateTabPacket(Player tabbedPlayer, User tabbedUser, UUID viewerUUID) {
        Component text = view.view(viewerUUID);
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                tabbedUser.getProfile(),
                true,
                tabbedPlayer.getPing(),
                SpigotConversionUtil.fromBukkitGameMode(tabbedPlayer.getGameMode()),
                text,
                null,
                0
        );
        return new WrapperPlayServerPlayerInfoUpdate(
                EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME),
                List.of(playerInfo)
        );
    }

    private void sendTabUpdatePacket(Player viewer, User viewerUser, Player tabbedPlayer, User tabbedUser) {
        var tabPacket = getUpdateTabPacket(tabbedPlayer, tabbedUser, viewer.getUniqueId());
        viewerUser.sendPacket(tabPacket);
    }

    private <T extends Player> void toggleInTabListForViewers(Stream<T> viewers, User tabbedUser, Player tabbedPlayer) {
        // TODO: Error management!!!!!!!!
        viewers.map((viewerPlayer) -> Map.entry(viewerPlayer, view.resolveUser(viewerPlayer.getUniqueId()).orElseThrow()))
                .forEach((e) -> sendTabUpdatePacket(e.getKey(), e.getValue(), tabbedPlayer, tabbedUser));
    }

    public boolean canSee(UUID viewer) {
        return getPlugin().getVanishIntegration().canSee(uuid, viewer);
    }

    public boolean canSeeMe(UUID viewer) {
        return getPlugin().getVanishIntegration().canSee(viewer, uuid);
    }

    public <T> void logIfAbsent(Optional<T> opt, String msg) {
        if (opt.isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "View >> " + msg);
        }
    }

    public void makeTagInvisibleFor(UUID viewer) {
        var location = view.getOwnerHeadLoc();
        logIfAbsent(location, "Could not make tag invisible for viewer!");
        // Makes your own tag invisible for viewer
        view.update(viewer, new View.NewView(
                getPlugin().getComponentIntegration().getCustomNametag(uuid, viewer),
                // todo: double check it never throws!
                location.orElseThrow(),
                false
        ));
    }

    public void makeTagVisibleFor(UUID viewer) {
        var location = view.getOwnerHeadLoc();
        logIfAbsent(location, "Could not make tag visible for viewer!");
        // Makes your own tag Visible for viewer
        view.update(viewer, new View.NewView(
                getPlugin().getComponentIntegration().getCustomNametag(uuid, viewer),
                // todo: double check it never throws!
                location.orElseThrow(),
                true
        ));
    }

    private Map.Entry<Player, User> getFromUUID(UUID userUUID) {
        Player player = Bukkit.getPlayer(userUUID);
        User user = player == null ? null : view.resolveUser(userUUID).get();
        return Map.entry(player, user);
    }

    private void sendTabUpdatePacket(UUID viewerUUID) {
        var tabbed = getFromUUID(uuid);
        var viewer = getFromUUID(viewerUUID);
        sendTabUpdatePacket(viewer.getKey(), viewer.getValue(), tabbed.getKey(), tabbed.getValue());
    }

    public void updateNametagFor(UUID viewer) {
        // yay!! no refreshing required! (live updates!)
        var owner = view.resolveUser(uuid);
        var viewerUserOpt = view.resolveUser(viewer);
        if (owner.isEmpty() || viewerUserOpt.isEmpty()) {
            Bukkit.getLogger().info("Nametag >> trying to send nametag packet of someone who may be offline!");
            // if nametag owner doesn't exist, we can't update nametag!
            return;
        }
        User viewerUser = viewerUserOpt.get();
        view.editText(viewerUser,
                getPlugin().getComponentIntegration().getCustomNametag(uuid, viewer));
        viewerUser.sendPacketSilently(view.getPassengersPacket());
        sendTabUpdatePacket(viewer);
    }

    public void updateText() {
        Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .filter(this::canSeeMe)
                .forEach(this::updateNametagFor);
    }

    private void create() {
        Player player = Bukkit.getPlayer(uuid);
        User user = player == null ? null : view.resolveUser(player.getUniqueId()).orElse(null);
        if (player == null || !player.isOnline() || user == null) {
            return;
        }
//        Bukkit.getServer().getLogger().warning("Tag creation in `Nametag.java` has ran!");
        // THIS makes a view for everyone who can see you! O(n)
        List<Player> viewers = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            UUID viewerUUID = onlinePlayer.getUniqueId();
            view.create(viewerUUID);
            if (!canSeeMe(viewerUUID)) {
                makeTagInvisibleFor(viewerUUID);
            } else { // only those who can SEE ME gets my tab packet!
                viewers.add(onlinePlayer);
            }
        }
        // invisible vanilla tag!
        if (!noNametagTeam.hasEntry(player.getName())) {
            noNametagTeam.addEntry(player.getName());
        }
        // todo: toggle in tab list.
        toggleInTabListForViewers(viewers.stream(), user, player);
    }

    // Logout or server shutdown
    public void shutdown() {
        view.shutdown();
        view.resolveUser(uuid)
                .ifPresent((user) -> noNametagTeam.removeEntry(user.getName()));
    }

    // First, run `refresh`, then for every other player on the server,
    // run this command (sender = other player)
    //
    // Usually, sender will be the player whose Nametag was previously updated,
    // and we want everyone else on the server to see this update.
    //
    // Self = You
    // Sender = Other player(s)
    //
    // (Every other player == sender)
    //
    // (SetRank O(n) optimization)
    //
    // Haves someone else send you their display
    // Changes what "you" see
    //
    // This is the reverse of `refresh` because the packets send in the opposite
    // direction.
    //
    // You == viewer
    // They == tagOwner
    //
    //
    // Changes what "you" see
    public boolean refreshSelfView(Nametag senderNametag) {
        // #1 refresh self display component (do `refresh` on sender)

        // STOP you from seeing vanished players (!canSee(vanish))
            // SENDER.deleteView(YOU)
        // Allow you to see unvanished players!
            // SENDER.addViewer(YOU)


        if (senderNametag == null) {
            plugin.getLogger().log(Level.SEVERE, "View >> senderNametag is null!");
            return false;
        }

        Player viewer = Bukkit.getPlayer(uuid);
        User viewerUser = viewer == null ? null : view.resolveUser(uuid).orElse(null);

        Player senderPlayer = Bukkit.getPlayer(senderNametag.uuid);
        User senderUser = senderPlayer == null ? null : view.resolveUser(senderPlayer.getUniqueId()).orElse(null);

        if (viewer == null || !viewer.isOnline() || viewerUser == null) {
            return false;
        } if (senderPlayer == null || !senderPlayer.isOnline() || senderUser == null) {
            return false;
        }

        // both methods dynamically updates text too!
        // Refreshes display & sends back to you!

        if (canSee(senderNametag.uuid)) {
//            plugin.getLogger().log(Level.INFO, "View >> " + viewer.getName() + " has *visible tag for owner: " + senderPlayer.getName());
            senderNametag.makeTagVisibleFor(uuid);

            Set<Player> you = Set.of(viewer);
            // Sends tab list of other player back to you!
            senderNametag.toggleInTabListForViewers(you.stream(), senderUser, senderPlayer);

        } else {
            // Can see them but I'm not allowed to now!
            // (Make invisible)
            senderNametag.makeTagInvisibleFor(uuid);
        }

        return true;
    }

    // Disguise, Vanish, Player Death
    // Only use `refresh` if you don't want
    // `tabbedPlayer`'s view of other people to change.
    //
    //
    // Changes what "everyone else" sees
    public boolean refresh() {
        Player playerWithNametag = Bukkit.getPlayer(uuid);
        if (playerWithNametag == null) {
            return false;
        }
        this.view.shutdown();
        create();
        return true;
    }

}
