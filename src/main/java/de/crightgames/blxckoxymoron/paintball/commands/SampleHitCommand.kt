package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigTeam
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigTeam.Companion.team
import de.crightgames.blxckoxymoron.paintball.util.PlayerHitHandler
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SampleHitCommand : ArgumentBuilder<CommandSender, SampleHitCommand>() {
    companion object {
        fun playerAndTeam(ctx: CommandContext<CommandSender>): Triple<Player?, ConfigTeam?, Int> {
            val player = ctx.source as? Player ?: return Triple(null, null, -1)
            val playerTeam = player.team

            if (playerTeam == null) {
                player.sendMessage(ThemeBuilder.themed(
                    ":RED:You aren't in a team.::"
                ))
                return Triple(player, null, Command.SINGLE_SUCCESS)
            }
            return Triple(player, playerTeam, Command.SINGLE_SUCCESS)
        }

        fun debugEnemy(team: ConfigTeam): ConfigTeam {
            return Paintball.gameConfig.teams[0]
                .takeIf { it.name != team.name }
                ?: Paintball.gameConfig.teams[1]
        }
    }

    override fun getThis(): SampleHitCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("samplehit")
            .requires { it is Player }
            .executes { ctx ->
                val (player, team, err) = playerAndTeam(ctx)

                if (player == null || team == null) return@executes err

                val died = PlayerHitHandler(player, team, debugEnemy(team)).wasHit()
                player.sendMessage(ThemeBuilder.themed(
                    "Hit!" + if (died) " You would've died now!" else ""
                ))

                return@executes Command.SINGLE_SUCCESS
            }.then(
                argument<CommandSender?, Int?>("hitcount", integer()).executes { ctx ->
                    val (player, team, err) = playerAndTeam(ctx)

                    if (player == null || team == null) return@executes err

                    val count = ctx.getArgument("hitcount", Int::class.java) ?: return@executes -1

                    PlayerHitHandler(player, team, debugEnemy(team)).updateDamage(count)

                    return@executes Command.SINGLE_SUCCESS
                }
            ).build()
    }
}