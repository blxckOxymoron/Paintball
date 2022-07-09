package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.projectile.GameProjectile
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileEffect
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileParticle
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileType
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class TestProjectileCommand : ArgumentBuilder<CommandSender, TestProjectileCommand>() {

    companion object {
        val projectileType = ProjectileType(
            0.2,
            ProjectileType.GRAVITY * 0.1,
            listOf(ProjectileEffect.SIMPLE_HIT_BLOCK to 1),
            ProjectileParticle.FIREWORKS,
            EntityType.SNOWBALL,
        )
    }

    override fun getThis(): TestProjectileCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("testprojectile")
            .requires { it is Player }.executes { ctx ->

                val player = ctx.source as? Player ?: return@executes -1

                GameProjectile(
                    projectileType,
                    player.eyeLocation,
                )

                return@executes Command.SINGLE_SUCCESS
            }.build()
    }
}