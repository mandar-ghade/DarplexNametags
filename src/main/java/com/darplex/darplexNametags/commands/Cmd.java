package com.darplex.darplexNametags.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface Cmd {
    boolean hasPermissionCtx(CommandSourceStack sender);
    public LiteralCommandNode<CommandSourceStack> createCommand();
    public int run(CommandContext<CommandSourceStack> ctx);
}
