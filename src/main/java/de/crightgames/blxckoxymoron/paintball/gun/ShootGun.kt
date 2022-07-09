package de.crightgames.blxckoxymoron.paintball.gun

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.projectile.GameProjectile
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder.sendThemedMessage
import de.crightgames.blxckoxymoron.paintball.util.VectorUtils.vectorWithSpray
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.*

class ShootGun : Listener {

    companion object {
        val nextShot: MutableMap<UUID, Long> = mutableMapOf() // timestamp for when a player can shoot again
    }

    @EventHandler
    fun shoot(e: PlayerInteractEvent) {
        if (e.hand != EquipmentSlot.HAND) return

        val gun = kotlin.runCatching {
            e.player.inventory
                .itemInMainHand.itemMeta?.persistentDataContainer
                ?.get(NamespacedKey(Paintball.INSTANCE, "gun"), GunDataContainer)
                ?: return
        }.getOrElse {
            return e.player.sendThemedMessage(
                ":RED:Illegal property in tag container:\n" +
                    "`${it.message}`"
            )
        }

        val cooldown = nextShot[e.player.uniqueId]
        val now = System.currentTimeMillis()
        if (cooldown != null && cooldown > now) return // player is on cooldown

        // SHOOT!
        // this should just be replaced with a check to not shoot oneself
        val spawnLocation = e.player.eyeLocation.clone().add(
            e.player.location.direction.normalize().multiply(0.9)
                .add(e.player.velocity.clone().multiply(1.4))
        )
        for (i in 1..gun.bullets) {
            GameProjectile(gun.projectile, spawnLocation.clone(), vectorWithSpray(
                spawnLocation.direction,
                gun.spray
            ))
        }

        nextShot[e.player.uniqueId] = now + gun.rateOfFire
    }
}