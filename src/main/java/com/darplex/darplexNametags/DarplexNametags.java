package com.darplex.darplexNametags;

import com.darplex.darplexNametags.commands.VanishCmd;
import com.darplex.darplexNametags.integrations.ComponentIntegration;
import com.darplex.darplexNametags.integrations.DefaultComponentIntegration;
import com.darplex.darplexNametags.integrations.DefaultVanishIntegration;
import com.darplex.darplexNametags.integrations.VanishIntegration;
import com.darplex.darplexNametags.listeners.JoinListener;
import com.darplex.darplexNametags.listeners.MountListener;
import com.darplex.darplexNametags.nametags.NametagManager;
import com.darplex.darplexNametags.passengers.VirtualPassengerManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import lombok.Setter;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.EntityLibAPI;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class DarplexNametags extends JavaPlugin {

    @NotNull @Getter PacketEventsAPI<?> packetEventsAPI;
    @NotNull @Getter EntityLibAPI<?> entityLibAPI;
    @NotNull @Getter @Setter ComponentIntegration componentIntegration;
    @NotNull @Getter VirtualPassengerManager virtualPassengerManager;
    @NotNull @Getter @Setter VanishIntegration vanishIntegration;
    @NotNull @Getter NametagManager nametagManager;

    private void registerMountListener() {
        getPacketEventsAPI().getEventManager().registerListener(
                new MountListener(this),
                PacketListenerPriority.NORMAL
        );
    }

    private void registerJoinListener() {
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
    }

    private void registerListeners() {
        // register mounting!
        registerMountListener();
        registerJoinListener();
    }

    private void registerCmds(ReloadableRegistrarEvent<Commands> commands) {
        commands.registrar().register(new VanishCmd(this).createCommand());
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, this::registerCmds);
    }


    @Override
    public void onEnable() {
        // TODO: check that both packetevents and entitylib are installed and are "softdepends"
        packetEventsAPI = PacketEvents.getAPI();
        registerListeners();
        registerCommands();
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
        vanishIntegration = new DefaultVanishIntegration(this);
        nametagManager = new NametagManager(this);
    }

    @Override
    public void onDisable() {
        getNametagManager().shutdown();
    }

}
