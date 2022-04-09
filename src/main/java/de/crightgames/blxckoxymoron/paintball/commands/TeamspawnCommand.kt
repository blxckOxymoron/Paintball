package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandExceptionType
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigTeam
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

class TeamspawnCommand : ArgumentBuilder<CommandSender, TeamspawnCommand>() {

    override fun getThis(): TeamspawnCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("teamspawn")
            .requires { it is Player }
            .then(
                literal<CommandSender?>("set").then(
                    argument<CommandSender?, ConfigTeam?>("team", TeamNameArgument()).executes { ctx ->
                        val player = ctx.source as? Player ?: return@executes -1
                        val team = ctx.getArgument("team", ConfigTeam::class.java) ?: return@executes -1

                        val dir = player.location.direction
                        val teamSpawnPos = player.location.block.location
                        teamSpawnPos.direction = dir

                        team.spawnPos = teamSpawnPos

                        player.sendMessage(ThemeBuilder.themed(
                            "*Successfully* updated the spawnposition for team ${team.displayName}."
                        ))

                        return@executes Command.SINGLE_SUCCESS
                    }
                )
            )
            .then(
                literal<CommandSender?>("list").executes { ctx ->

                    ctx.source.sendMessage(ThemeBuilder.themed("Team spawn positions:"))
                    ctx.source.sendMessage(Paintball.gameConfig.teams.map { team ->

                        return@map ThemeBuilder.themed("`----` ${team.displayName} `----`\n") +
                            if (team.spawnPos != null) ThemeBuilder.serializableThemed(team.spawnPos!!)
                            else ThemeBuilder.themed("null")

                    }.joinToString("\n"))

                    return@executes Command.SINGLE_SUCCESS
                }
            )
            .build()
    }

    class TeamNameArgument : ArgumentType<ConfigTeam> {

        override fun parse(reader: StringReader): ConfigTeam {
            val index = reader.readString().replace(Regex(".*-"), "").toIntOrNull()

            val team = index?.let{ try {Paintball.gameConfig.teams[it]} catch (_: IndexOutOfBoundsException) {null} }
                ?: throw CommandSyntaxException(NoSuchTeamNameException()) {
                    "Invalid team name"
                }
            return team
        }

        override fun <S : Any?> listSuggestions(
            context: CommandContext<S>,
            builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> {

            Paintball.gameConfig.teams
                .mapIndexed { i, tm -> "${tm.displayName.replace(Regex("§[0-fklmnor]"), "")}-$i"}
                .filter { it.startsWith(builder.remaining, true) }
                .forEach(builder::suggest)

            return builder.buildFuture()
        }

        class NoSuchTeamNameException : CommandExceptionType
    }

}