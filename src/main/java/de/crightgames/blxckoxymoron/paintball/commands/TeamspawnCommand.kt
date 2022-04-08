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
import de.crightgames.blxckoxymoron.paintball.game.Game
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
                    argument<CommandSender?, Int?>("team", TeamNameArgument()).executes { ctx ->
                        val player = ctx.source as? Player ?: return@executes -1
                        val team = ctx.getArgument("team", Int::class.java) ?: return@executes -1

                        val dir = player.location.direction
                        val teamSpawnPos = player.location.block.location
                        teamSpawnPos.direction = dir

                        Paintball.gameConfig.teamSpawns[team] = teamSpawnPos

                        player.sendMessage(ThemeBuilder.themed(
                            "*Successfully* updated the spawnposition."
                        ))

                        return@executes Command.SINGLE_SUCCESS
                    }
                )
            )
            .then(
                literal<CommandSender?>("list").executes { ctx ->

                    ctx.source.sendMessage(ThemeBuilder.themed("Team spawn positions:"))
                    ctx.source.sendMessage(Game.teamNames.mapIndexed { i, teamName ->
                        val teamSpawn = Paintball.gameConfig.teamSpawns.getOrNull(i)

                        return@mapIndexed ThemeBuilder.themed("`----` *$teamName* `----`\n") +
                            if (teamSpawn != null) ThemeBuilder.serializableThemed(teamSpawn)
                            else ThemeBuilder.themed("null")

                    }.joinToString("\n"))

                    return@executes Command.SINGLE_SUCCESS
                }
            )
            .build()
    }

    class TeamNameArgument : ArgumentType<Int> {

        override fun parse(reader: StringReader): Int {
            val name = reader.readString()
            return Game.teamNames.indexOf(name).takeUnless { it == -1 }
                ?: throw CommandSyntaxException(NoSuchTeamNameException()
                ) { "$name is not a team. Use ${Game.teamNames.joinToString(" | ")}!" }
        }

        override fun <S : Any?> listSuggestions(
            context: CommandContext<S>,
            builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> {

            Game.teamNames.filter { it.startsWith(builder.remaining, true) }.forEach(builder::suggest)
            return builder.buildFuture()
        }

        class NoSuchTeamNameException : CommandExceptionType
    }

}