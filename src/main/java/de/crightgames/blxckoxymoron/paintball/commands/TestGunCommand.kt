package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.gun.Gun
import de.crightgames.blxckoxymoron.paintball.gun.Magazine
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileEffect
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileParticle
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileType
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder.sendThemedMessage
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TestGunCommand : ArgumentBuilder<CommandSender, TestGunCommand>() {
    companion object {
        private val testProjectile = ProjectileType(
            8.0,
            ProjectileType.GRAVITY,
            listOf(
                ProjectileEffect.COLOR to Paintball.gameConfig.colorRadius,
                ProjectileEffect.COLOR_SHEEP to DyeColor.CYAN.color.asRGB(),
                ProjectileEffect.DUST to 0x5BC0BE,
            ),
            ProjectileParticle.CRIT,
        )
        private val testMagazine = Magazine(
            16,
            2000,
            16
        )
        val testGun = Gun(
            testProjectile,
            testMagazine,
            300,
            0.05,
            1,
            Sound.ENTITY_TURTLE_EGG_CRACK,
            1.5F
        )
    }

    override fun getThis(): TestGunCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("testgun")
            .requires { it is Player }
            .executes { ctx ->
                val player = ctx.source as? Player ?: return@executes -1

                player.inventory.setItemInMainHand(testGun.createItem(Material.SPECTRAL_ARROW))
                player.sendThemedMessage("There you go ;)")

                return@executes Command.SINGLE_SUCCESS
            }
            .build()
    }

}