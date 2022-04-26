package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import org.bukkit.command.CommandSender

class ResetGameConfigCommand : ArgumentBuilder<CommandSender, ResetGameConfigCommand>() {
    override fun getThis(): ResetGameConfigCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("resetgameconfig").executes {
            Paintball.INSTANCE.resetConfig(false)

            return@executes Command.SINGLE_SUCCESS
        }.build()

    }
}