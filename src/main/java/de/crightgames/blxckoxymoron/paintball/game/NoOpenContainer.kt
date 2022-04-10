package de.crightgames.blxckoxymoron.paintball.game

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent

class NoOpenContainer : Listener {

    @EventHandler
    fun noOpenContainer(e: InventoryOpenEvent) {
        e.isCancelled = true
    }
}