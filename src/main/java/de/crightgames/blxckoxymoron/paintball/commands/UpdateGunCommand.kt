package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.tree.CommandNode
import de.crightgames.blxckoxymoron.paintball.gun.GunDataContainer
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder.sendThemedMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UpdateGunCommand : ArgumentBuilder<CommandSender, UpdateGunCommand>() {
    override fun getThis(): UpdateGunCommand {
        return this
    }

    override fun build(): CommandNode<CommandSender> {
        return literal<CommandSender>("updategun")
            .requires { it is Player }
            .executes { ctx ->
                val player = ctx.source as? Player ?: return@executes -1

                val gun = kotlin.runCatching {
                    player.inventory.itemInMainHand.itemMeta
                        ?.persistentDataContainer?.get(GunDataContainer.KEY, GunDataContainer)
                        ?: throw NullPointerException("The item's meta is null")
                }.getOrElse {
                    player.sendThemedMessage(
                        ":RED:You aren't holding a gun"
                    )
                    return@executes Command.SINGLE_SUCCESS
                }

                val material = player.inventory.itemInMainHand.type
                player.inventory.setItemInMainHand(gun.createItem(material))
                player.sendThemedMessage(
                    "Lore *successfully* updated."
                )

                return@executes Command.SINGLE_SUCCESS
            }
            .build()
    }
}