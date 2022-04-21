package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SampleKillCommand : ArgumentBuilder<CommandSender, SampleKillCommand>() {
    override fun getThis(): SampleKillCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("samplekill")
            .requires { it is Player }.executes { ctx ->
            val player = ctx.source as? Player ?: return@executes -1

            return@executes Command.SINGLE_SUCCESS
        }.build()
    }
}