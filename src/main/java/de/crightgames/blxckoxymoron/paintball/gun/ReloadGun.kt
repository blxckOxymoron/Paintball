package de.crightgames.blxckoxymoron.paintball.gun

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.Paintball.Companion.inWholeTicks
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

class ReloadGun : Listener {

    companion object {
        val currentlyReloading = mutableMapOf<UUID, BukkitTask>()

        fun cancelReload(u: UUID) {
            currentlyReloading[u]?.cancel()
            currentlyReloading.remove(u)
        }
    }

    @EventHandler
    fun reloadWhenDropped(e: PlayerDropItemEvent) {
        val itemMeta = e.itemDrop.itemStack.itemMeta ?: return
        val gun = kotlin.runCatching {
            itemMeta.persistentDataContainer.get(GunDataContainer.KEY, GunDataContainer)
        }.getOrNull() ?: return

        e.isCancelled = true

        if (currentlyReloading.containsKey(e.player.uniqueId)) return // is reloading

        gun.magazine.content = gun.magazine.size
        itemMeta.persistentDataContainer.set(GunDataContainer.KEY, GunDataContainer, gun)

        e.player.playSound(e.player.location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 100F, .7F)

        currentlyReloading[e.player.uniqueId] = Bukkit.getScheduler().runTaskLater(Paintball.INSTANCE, Runnable {
            e.player.inventory.itemInMainHand.itemMeta = itemMeta
            currentlyReloading.remove(e.player.uniqueId)
            e.player.playSound(e.player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 50F, 0.9F)
        }, gun.magazine.reloadSpeed.milliseconds.inWholeTicks)
    }

    @EventHandler
    fun cancelReloadWhenSwitchingHandItem(e: PlayerItemHeldEvent) {
        if (!currentlyReloading.containsKey(e.player.uniqueId)) return
        cancelReload(e.player.uniqueId)
        e.player.playSound(e.player.location, Sound.BLOCK_SCAFFOLDING_PLACE, 30F, 1F)
    }
}