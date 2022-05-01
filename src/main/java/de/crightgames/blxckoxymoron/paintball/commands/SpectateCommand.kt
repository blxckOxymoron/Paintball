package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.game.Countdown
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder.sendThemedMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpectateCommand : ArgumentBuilder<CommandSender, SpectateCommand>() {
    override fun getThis(): SpectateCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("spectate")
            .requires { it is Player }
            .executes { ctx ->
                val player = ctx.source as Player

                val wasSpectator = Game.permanentSpectators.remove(player)
                if (!wasSpectator) Game.permanentSpectators.add(player)

                player.sendThemedMessage(
                    "You are *${if (wasSpectator) "no longer" else "now"}* permanently a spectator."
                )

                Bukkit.broadcastMessage(
                    Game.getPlayerJoinMessage(player)
                )

                Countdown.checkAndStart()

                Command.SINGLE_SUCCESS
            }.build()
    }
}