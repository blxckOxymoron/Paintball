package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.game.Game
import org.bukkit.command.CommandSender

class RestartCommand : ArgumentBuilder<CommandSender, RestartCommand>() {
    override fun getThis(): RestartCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("restart").executes {
            Game.restart()
            Command.SINGLE_SUCCESS
        }
            .build()
    }
}