package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType.bool
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.game.Countdown
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder.sendThemedMessage
import org.bukkit.command.CommandSender

class AutostartCommand : ArgumentBuilder<CommandSender, AutostartCommand>() {
    override fun getThis(): AutostartCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("autostart")
            .then(
                argument<CommandSender?, Boolean?>("enabled", bool()).executes { ctx ->

                    val newEnabled = ctx.getArgument("enabled", Boolean::class.java) ?: return@executes -1
                    Paintball.gameConfig.autostart = newEnabled
                    Paintball.gameConfig.save()

                    Countdown.checkAndStart()

                    ctx.source.sendThemedMessage(
                        "*Autostart* is now *${if (newEnabled) "enabled" else "disabled"}*."
                    )

                    return@executes Command.SINGLE_SUCCESS
                }
            )
            .build()
    }
}