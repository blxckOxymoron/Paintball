package de.crightgames.blxckoxymoron.paintball.gun

import de.crightgames.blxckoxymoron.paintball.gun.GunDataContainer.isGun
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack

class KeepGunInInventory : Listener {

    @EventHandler
    fun dontDropFromInventory(e: InventoryClickEvent) {
        val item =
            when (e.action) {
                InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_SLOT -> e.currentItem
                InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ALL_CURSOR -> e.cursor
                else -> null
            }

        if (item?.isGun == true) {
            e.isCancelled = true
            return
        }
    }

    @EventHandler
    fun dontCloseInventoryWhenGunOnCursor(e: InventoryCloseEvent) {
        if (e.player.itemOnCursor.isGun) {
            e.player.inventory.addItem(e.player.itemOnCursor)
            e.player.setItemOnCursor(ItemStack(Material.AIR))
        }
    }

    @EventHandler
    fun dontPickUpItemWhenGunOnCursor(e: EntityPickupItemEvent) {
        val player = e.entity as? Player ?: return
        // TODO better check to allow additional items as long as the gun still fits:
        // add the item
        // add the item on cursor
        // ⇑ if any overflow occurs we cancel and remove the item again
        // else we remove the item on cursor from the inventory
        if (player.itemOnCursor.isGun) {
            e.item.pickupDelay = 10
            e.isCancelled = true
        }
    }
}