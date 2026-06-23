package com.darplex.darplexNametags.commands;

import com.darplex.darplexNametags.DarplexNametags;
import com.darplex.darplexNametags.component.DarplexComponent;
import com.darplex.darplexNametags.counters.RainbowGradient;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class RainbowSpeedCmd extends BaseCommand {
    @NotNull @Getter final DarplexNametags plugin;

    public RainbowSpeedCmd(@NotNull DarplexNametags plugin) {
        super("rainbow_speed");
        this.plugin = plugin;
    }

    private boolean hasRainbow(UUID uuid) {
        return getPlugin().getCounterManager().get(uuid, RainbowGradient.class)
                .isPresent();
    }


    private void applySpeedChange(RainbowGradient gradient, RainbowGradient.Speed speed) {
        if (gradient.getSpeed() == speed) {
            return;
        }
        if (!gradient.isCancelled()) {
            gradient.stop();
        }
        gradient.setSpeed(speed);
        gradient.start();
    }

    private void setRainbowSpeed(UUID uuid, RainbowGradient.Speed speed) {
        getPlugin().getCounterManager().get(uuid, RainbowGradient.class)
                .ifPresent((gradient) -> applySpeedChange(gradient, speed));
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal(commandName)
                .requires(this::hasPermissionCtx)
                .then(Commands.literal("fast")
                        .executes((ctx) -> run(ctx, RainbowGradient.Speed.FAST)))
                .then(Commands.literal("normal")
                        .executes((ctx) -> run(ctx, RainbowGradient.Speed.NORMAL)))
                .then(Commands.literal("slow")
                        .executes((ctx) -> run(ctx, RainbowGradient.Speed.SLOW)))
                .build();
    }

    @Override
    public boolean hasPermissionCtx(CommandSourceStack sender) {
        // TODO: Double check this is on main thread
        Optional<Player> player = super.getPlayer(sender);
        return player.isPresent() &&
                player.get().isOp();
//              hasRainbow(player.get().getUniqueId());
    }

    public int run(CommandContext<CommandSourceStack> ctx, RainbowGradient.Speed speed) {
        // check not needed!
        Player player = super.getPlayer(ctx.getSource()).get();
        if (!hasRainbow(player.getUniqueId())) {
            player.sendMessage("Darplex >> No rainbow component found for user!");
            return Command.SINGLE_SUCCESS;
        }
        setRainbowSpeed(player.getUniqueId(), speed);
        var comp = DarplexComponent
                .text("Darplex >> Rainbow speed is now: " + speed.name())
                .rainbow(getPlugin(), player.getUniqueId());
        player.sendMessage(comp.resolve());
        return Command.SINGLE_SUCCESS;
    }
}
