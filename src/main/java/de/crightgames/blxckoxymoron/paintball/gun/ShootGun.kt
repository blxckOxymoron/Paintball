package de.crightgames.blxckoxymoron.paintball.gun

import de.crightgames.blxckoxymoron.paintball.projectile.GameProjectile
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder.sendThemedMessage
import de.crightgames.blxckoxymoron.paintball.util.VectorUtils.vectorWithSpray
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.*

class ShootGun : Listener {

    companion object {
        val nextShot: MutableMap<UUID, Long> = mutableMapOf() // timestamp for when a player can shoot again
    }

    @EventHandler
    fun shoot(e: PlayerInteractEvent) {
        if (
            e.hand != EquipmentSlot.HAND ||
            e.action == Action.PHYSICAL ||
                !(e.player.inventory.type != InventoryType.CRAFTING ||
                e.player.inventory.type == InventoryType.CREATIVE) ||
            ReloadGun.currentlyReloading.containsKey(e.player.uniqueId)
        ) return

        val mainHandMeta = e.player.inventory
            .itemInMainHand.itemMeta ?: return

        val gun = kotlin.runCatching {
            mainHandMeta.persistentDataContainer.get(GunDataContainer.KEY, GunDataContainer) ?: return
        }.getOrElse {
            return e.player.sendThemedMessage(
                ":RED:Illegal property in tag container:\n" +
                    "`${it.message ?: "Unknown error"}`"
            )
        }

        e.setUseInteractedBlock(Event.Result.DENY)
        e.setUseItemInHand(Event.Result.DENY)

        if (gun.magazine.content <= 0) return

        val cooldown = nextShot[e.player.uniqueId]
        val now = System.currentTimeMillis()
        if (cooldown != null && cooldown > now) return // player is on cooldown

        // SHOOT!
        gun.magazine.content--
        mainHandMeta.persistentDataContainer.set(GunDataContainer.KEY, GunDataContainer, gun)
        e.player.inventory.itemInMainHand.itemMeta = mainHandMeta

        val spawnLocation = e.player.eyeLocation.clone()
        spawnLocation.world?.playSound(spawnLocation, gun.sound, 100F, gun.pitch) // TODO revise Sound category

        for (i in 1..gun.bullets) {
            GameProjectile(gun.projectile, spawnLocation.clone(), e.player, vectorWithSpray(
                spawnLocation.direction,
                gun.spray
            ))
        }

        nextShot[e.player.uniqueId] = now + gun.rateOfFire
    }
}