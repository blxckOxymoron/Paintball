package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.game.Countdown
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.command.CommandSender

class ReloadConfigCommand : ArgumentBuilder<CommandSender, ReloadConfigCommand>() {
    override fun getThis(): ReloadConfigCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("reloadConfig").executes { ctx ->
            if (Game.state == Game.GameState.RUNNING) {
                ctx.source.sendMessage(ThemeBuilder.themed(
                    ":RED:Can't reload the config while the game is running::"
                ))
                return@executes Command.SINGLE_SUCCESS
            }
            Paintball.INSTANCE.reloadConfig()
            Countdown.checkAndStart()
            ctx.source.sendMessage(ThemeBuilder.themed(
                "Config reloaded"
            ))
            Command.SINGLE_SUCCESS
        }.build()
    }
}