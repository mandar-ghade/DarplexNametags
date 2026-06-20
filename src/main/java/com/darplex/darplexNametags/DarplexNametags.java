package com.darplex.darplexNametags;

import com.darplex.darplexNametags.integrations.ComponentIntegration;
import com.darplex.darplexNametags.integrations.DefaultComponentIntegration;
import com.darplex.darplexNametags.integrations.DefaultVanishIntegration;
import com.darplex.darplexNametags.integrations.VanishIntegration;
import com.darplex.darplexNametags.listeners.MountListener;
import com.darplex.darplexNametags.nametags.NametagManager;
import com.darplex.darplexNametags.passengers.VirtualPassengerManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import lombok.Getter;
import lombok.Setter;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.EntityLibAPI;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DarplexNametags extends JavaPlugin {

    @NotNull @Getter PacketEventsAPI<?> packetEventsAPI;
    @NotNull @Getter EntityLibAPI<?> entityLibAPI;
    @NotNull @Getter @Setter ComponentIntegration componentIntegration;
    @NotNull @Getter VirtualPassengerManager virtualPassengerManager;
    @NotNull @Getter @Setter VanishIntegration vanishIntegration;
    @NotNull @Getter NametagManager nametagManager;

    public void registerMountListener() {
        getPacketEventsAPI().getEventManager().registerListener(
                new MountListener(this),
                PacketListenerPriority.NORMAL
        );
    }

    @Override
    public void onEnable() {
        // TODO: check that both packetevents and entitylib are installed and are "softdepends"
        packetEventsAPI = PacketEvents.getAPI();
        // register mounting!
        registerMountListener();
        SpigotEntityLibPlatform platform = new SpigotEntityLibPlatform(this);
        APIConfig settings = new APIConfig(packetEventsAPI)
                .debugMode()
                .checkForUpdates()
                .tickTickables()
                .useBstats()
                .usePlatformLogger();
        EntityLib.init(platform, settings);
        entityLibAPI = EntityLib.getApi();
        componentIntegration = new DefaultComponentIntegration();
        virtualPassengerManager = new VirtualPassengerManager();
        vanishIntegration = new DefaultVanishIntegration();
        nametagManager = new NametagManager(this);
    }

    @Override
    public void onDisable() {
    }

}
