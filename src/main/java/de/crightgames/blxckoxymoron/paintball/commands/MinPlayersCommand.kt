package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.game.Countdown
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder.sendThemedMessage
import org.bukkit.command.CommandSender

class MinPlayersCommand : ArgumentBuilder<CommandSender, MinPlayersCommand>() {
    override fun getThis(): MinPlayersCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("minplayers")
            .then(
                argument<CommandSender?, Int?>("playercount", integer(1)).executes { ctx ->

                    val newCount = ctx.getArgument("playercount", Int::class.java) ?: return@executes -1
                    Paintball.gameConfig.minimumPlayers = newCount
                    Paintball.gameConfig.save()

                    ctx.source.sendThemedMessage(
                        "*Updated* minimum player count to *$newCount*."
                    )

                    Countdown.checkAndStart()

                    return@executes Command.SINGLE_SUCCESS
                }
            )
            .build()
    }
}