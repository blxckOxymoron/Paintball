package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.util.EmptyWorldGen
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder.sendThemedMessage
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.WorldCreator
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File
import java.util.concurrent.CompletableFuture

class ArenaCommand : ArgumentBuilder<CommandSender, ArenaCommand>() {
    override fun getThis(): ArenaCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("arena")
            .requires { it is Player } .then(
            literal<CommandSender?>("teleport").then(
                literal<CommandSender?>("base").executes { ctx ->
                    val player = ctx.source as? Player ?: return@executes -1

                    val world =
                        if (
                            File(Bukkit.getWorldContainer().absolutePath, Paintball.gameConfig.arenaWorldName).exists()
                        ) Bukkit.createWorld(WorldCreator(Paintball.gameConfig.arenaWorldName).generator(EmptyWorldGen()))
                        else null

                    if (world == null) {
                        player.sendThemedMessage(
                            ":RED:Can't find a world with name `${Paintball.gameConfig.arenaWorldName}`" +
                                "\n:RED:To change the world name use `/paintball arena world <name>`"
                        )
                        return@executes Command.SINGLE_SUCCESS
                    }
                    player.teleport(world.spawnLocation)

                    Command.SINGLE_SUCCESS
                }
            ).then(
                literal<CommandSender?>("game").executes { ctx ->
                    val player = ctx.source as? Player ?: return@executes -1

                    val world = Game.arenaWorld
                    if (world == null) {
                        player.sendThemedMessage(
                            ":RED:Can't get game world. This means, the base world doesn't exist. " +
                            "Create one with `/paintball arena create`:RED: and restart."
                        )
                        return@executes Command.SINGLE_SUCCESS
                    }
                    player.teleport(world.spawnLocation)

                    Command.SINGLE_SUCCESS
                }
            )
        ).then(
            literal<CommandSender?>("world").then(
                argument<CommandSender?, String?>("worldName", WorldNameArgument()).executes { ctx ->

                    val newWordName = ctx.getArgument("worldName", String::class.java)
                    Paintball.gameConfig.arenaWorldName = newWordName
                    Paintball.gameConfig.save()
                    ctx.source.sendThemedMessage(
                        "*Successfully* updated arena world name to '$newWordName'"
                    )

                    Command.SINGLE_SUCCESS
                }
            )
        ).then(
            literal<CommandSender?>("create").executes { ctx ->
                val world = Bukkit.createWorld(WorldCreator(Paintball.gameConfig.arenaWorldName).generator(EmptyWorldGen()))
                    ?: return@executes -1

                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
                world.setGameRule(GameRule.DO_FIRE_TICK, false)
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
                world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false)
                world.setGameRule(GameRule.NATURAL_REGENERATION, false)

                ctx.source.sendThemedMessage(
                    "*Successfully* created or loaded world '${Paintball.gameConfig.arenaWorldName}'" +
                        "\nYou can teleport to it with `/paintball arena teleportbase`"
                )

                Command.SINGLE_SUCCESS
            }
        ).build()
    }

    private class WorldNameArgument : ArgumentType<String> {
        override fun parse(reader: StringReader): String {
            return reader.readString()
        }

        override fun <S : Any?> listSuggestions(
            context: CommandContext<S>?,
            builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> {

            Bukkit.getWorlds()
                .map { it.name }
                .filter { it.startsWith(builder.remaining, true) }
                .forEach(builder::suggest)

            return builder.buildFuture()
        }

    }
}