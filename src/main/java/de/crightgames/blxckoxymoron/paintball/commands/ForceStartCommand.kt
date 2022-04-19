package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.game.Game
import org.bukkit.command.CommandSender

class ForceStartCommand : ArgumentBuilder<CommandSender, ForceStartCommand>() {
    override fun getThis(): ForceStartCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("forcestart").executes {

            Game.start()

            return@executes Command.SINGLE_SUCCESS
        }.build()
    }
}