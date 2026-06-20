package com.darplex.darplexNametags.listeners;

import com.darplex.darplexNametags.DarplexNametags;
import com.darplex.darplexNametags.nametags.Nametag;
import com.darplex.darplexNametags.nametags.View;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@RequiredArgsConstructor
public class MountListener implements PacketListener {
    @NotNull @Getter DarplexNametags plugin;

    private Set<Integer> getVanillaPassengers(int[] passengers) {
        Set<Integer> vanillaPassengers = new HashSet<>();
        for (int passenger : passengers) {
            vanillaPassengers.add(passenger);
        }
        return vanillaPassengers;
    }

    private void replaceOldPassengers(UUID uuid, int[] vanillaPassengers) {
        getPlugin().getVirtualPassengerManager()
                .replaceAll(uuid, getVanillaPassengers(vanillaPassengers));
    }

    private void editNametag(Nametag nametag, PacketSendEvent event, WrapperPlayServerSetPassengers packet) {
        View view = nametag.getView();
        packet.setPassengers(view.getPassengerArray());
        event.markForReEncode(true);
    }

    private void editIfNametagExists(PacketSendEvent event, WrapperPlayServerSetPassengers packet) {
        UUID uuid = event.getUser().getUUID();
        Optional<Nametag> nametagOpt = getPlugin().getNametagManager().get(uuid);
        nametagOpt.ifPresent((nametag) -> editNametag(nametag, event, packet));
    }

    // This assumes that NONE of the sent `WrapperPlayServerSetPassengers`
    // are Nametags (all vanilla)
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SET_PASSENGERS) {
            WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(event);
            UUID uuid = event.getUser().getUUID();
            // replaces vanilla passengers from before with the new ones.
            replaceOldPassengers(uuid, packet.getPassengers());
            // If nametag is registered, resend edited packet!
            editIfNametagExists(event, packet);
        }
    }

}
