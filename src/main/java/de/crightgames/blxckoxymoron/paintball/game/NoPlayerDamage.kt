package de.crightgames.blxckoxymoron.paintball.game

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class NoPlayerDamage : Listener {

    @EventHandler
    fun handlePlayerDamage(e: EntityDamageEvent) {
        if (e.entity !is Player) return
        e.isCancelled = true
    }

}