package de.crightgames.blxckoxymoron.paintball.game

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent

class NoHunger : Listener {

    @EventHandler
    fun noHunger(e: FoodLevelChangeEvent) {
        e.foodLevel = 20
    }
}