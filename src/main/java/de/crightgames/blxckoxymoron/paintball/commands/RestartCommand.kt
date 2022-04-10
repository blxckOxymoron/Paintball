package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.game.Countdown
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.game.Scores
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender

class RestartCommand : ArgumentBuilder<CommandSender, RestartCommand>() {
    override fun getThis(): RestartCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("restart").executes {
            Game.state = Game.GameState.WAITING
            Game.setupNewArenaWorld()
            Bukkit.getOnlinePlayers().forEach {
                it.gameMode = GameMode.SPECTATOR
                it.inventory.clear()
            }
            Paintball.gameConfig.teams.forEach { team ->
                team.reset()
            }

            Scores.createAndResetScores()
            Countdown.checkAndStart()
            Command.SINGLE_SUCCESS
        }
            .build()
    }
}