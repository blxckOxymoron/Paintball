package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.LongArgumentType.longArg
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.projectile.MoveProjectiles
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder.sendThemedMessage
import org.bukkit.command.CommandSender

class ProjectileResolutionCommand : ArgumentBuilder<CommandSender, ProjectileResolutionCommand>() {
    override fun getThis(): ProjectileResolutionCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("projectileresolution")
            .then(argument<CommandSender, Long>("value", longArg())
                .executes { ctx ->
                    val value = ctx.getArgument("value", Long::class.java)
                    MoveProjectiles.runLoop(Paintball.INSTANCE, value)

                    ctx.source.sendThemedMessage("Loop running every *$value* ticks.")

                    return@executes Command.SINGLE_SUCCESS
                })
            .build()
    }
}