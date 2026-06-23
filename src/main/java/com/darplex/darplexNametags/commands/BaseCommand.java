package com.darplex.darplexNametags.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class BaseCommand implements Cmd {

    @NotNull @Getter String commandName;

    public boolean isPlayer(CommandSourceStack sender) {
        if (!(sender.getExecutor() instanceof Player)) {
            return false;
        }
        return true;
    }

    public Optional<Player> getPlayer(CommandSourceStack sender) {
        if (!isPlayer(sender)) return Optional.<Player>empty();
        return Optional.ofNullable((Player) sender.getExecutor());
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal(commandName)
                .requires(this::hasPermissionCtx)
                .executes(this::run)
                .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> ctx) {
        return Command.SINGLE_SUCCESS;
    }

}
