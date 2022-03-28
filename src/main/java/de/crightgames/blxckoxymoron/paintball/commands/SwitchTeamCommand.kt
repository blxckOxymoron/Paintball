package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
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
                var teamIndex = Paintball.teams.indexOfFirst { it.contains(player) }.takeUnless { it == -1 }

                if (teamIndex == null) {
                    player.sendMessage(ThemeBuilder.themed(
                        ":RED:You aren't in a team or the game hasn't started yet.::"
                    ))
                    return@executes -1
                }

                Paintball.teams[teamIndex].remove(player)

                teamIndex = (teamIndex + 1) % Paintball.teams.size
                Paintball.teams[teamIndex].add(player)
                val teamName = Game.teamNames[teamIndex]

                player.sendMessage(ThemeBuilder.themed(
                    "Successfully switched to team `$teamName`."
                ))

                return@executes Command.SINGLE_SUCCESS
            }
            .build()
    }
}