package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder.sendThemedMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SwitchTeamCommand : ArgumentBuilder<CommandSender, SwitchTeamCommand>() {
    override fun getThis(): SwitchTeamCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("switchteam")
            .requires { it is Player }.executes { ctx ->
                val player = ctx.source as? Player ?: return@executes -1
                var teamIndex = Paintball.gameConfig.teams.indexOfFirst { it.players.contains(player) }.takeUnless { it == -1 }

                if (teamIndex == null) {
                    player.sendThemedMessage(
                        ":RED:You aren't in a team or the game hasn't started yet.::"
                    )
                    return@executes -1
                }

                val prevTeam = Paintball.gameConfig.teams[teamIndex]
                prevTeam.removePlayer(player)

                teamIndex = (teamIndex + 1) % Paintball.gameConfig.teams.size
                val nextTeam = Paintball.gameConfig.teams[teamIndex]
                nextTeam.addPlayer(player)

                player.sendThemedMessage(
                    "Successfully switched from `${prevTeam.displayName}` to team `${nextTeam.displayName}`."
                )

                return@executes Command.SINGLE_SUCCESS
            }
            .build()
    }
}