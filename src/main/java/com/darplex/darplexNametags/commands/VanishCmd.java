package com.darplex.darplexNametags.commands;

import com.darplex.darplexNametags.DarplexNametags;
import com.darplex.darplexNametags.nametags.Nametag;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class VanishCmd {
    // todo: work on vanish visibility nanagement!
    @NotNull @Getter DarplexNametags plugin;
    @Getter Set<UUID> vanishedUsers = new HashSet<>();

    public boolean hasPermissionCtx(CommandSourceStack sender) {
        // TODO: Double check this is on main thread
        if (!(sender.getExecutor() instanceof Player playerSender)) {
            return false;
        }
        return playerSender.isOp();
    }

    public LiteralCommandNode<io.papermc.paper.command.brigadier.CommandSourceStack> createCommand() {
        return Commands.literal("vanish")
                .requires(this::hasPermissionCtx)
                .executes(this::runVanishOrUnvanish)
                .build();
    }

    private void setOwnerInvisible(UUID owner, UUID viewer) {
        getPlugin().getVanishIntegration().setCanSee(owner, viewer, false);
    }

    private void setOwnerVisible(UUID owner, UUID viewer) {
        getPlugin().getVanishIntegration().setCanSee(owner, viewer, true);
    }

    public boolean isVanished(UUID owner) {
        return vanishedUsers.contains(owner);
    }

    private boolean bypassesVanish(UUID viewer) {
        return getPlugin().getVanishIntegration().overridesVanish(viewer);
    }

    // Sets "canSee" relation between (owner, viewer)
    private void vanishOrUnvanish(UUID ownerUUID, UUID viewerUUID) {
        // if previously visible, make owner invisible to viewer if viewer doesn't have
        // the right to see thme
        if (!isVanished(ownerUUID) && !bypassesVanish(viewerUUID)) {
            setOwnerInvisible(ownerUUID, viewerUUID);
        } else {
            // Any time viewer bypasses vanish, owner will be visible regardless!
            setOwnerVisible(ownerUUID, viewerUUID);
        }
    }

    private void vanish(Player owner) {
        getPlugin().getVanishIntegration().vanishPlayer(owner);
    }

    private void unvanish(Player owner) {
        getPlugin().getVanishIntegration().unvanishPlayer(owner);
    }

    // Conditionals will be handled internally (in VanishIntegration)
    private void togglePlayerVisibility(Player owner) {
        UUID ownerUUID = owner.getUniqueId();
        if (isVanished(ownerUUID)) {
            unvanish(owner);
        } else {
            vanish(owner);
        }
    }

    public void changeVanishStatus(UUID ownerUUID) {
        if (isVanished(ownerUUID)) {
            vanishedUsers.remove(ownerUUID);
        } else {
            vanishedUsers.add(ownerUUID);
        }
    }

    public int runVanishOrUnvanish(CommandContext<CommandSourceStack> ctx) {
        Player owner = (Player) ctx.getSource().getExecutor();
        UUID ownerUUID = owner.getUniqueId();
        Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId)
                .forEach((viewerUUID) -> {
                    // dont vanish same player!
                    // TODO: make this check customizable!
                    if (viewerUUID == ownerUUID) {
                        return;
                    }
                    vanishOrUnvanish(ownerUUID, viewerUUID);
                });
        // hide / show owner
        togglePlayerVisibility(owner);
        // internal change in vanish status.
        changeVanishStatus(ownerUUID);
        // refreshes what other people see.
        getPlugin().getNametagManager().refreshNametag(ownerUUID);
        return Command.SINGLE_SUCCESS;
    }
}
