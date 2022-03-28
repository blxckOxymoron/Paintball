package de.crightgames.blxckoxymoron.paintball.game.projectile

import de.crightgames.blxckoxymoron.paintball.game.Game
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class SnowballDrop : Listener {

    @EventHandler
    fun onSnowballDrop(e: PlayerDropItemEvent) {
        if (e.itemDrop.itemStack.isSimilar(Game.snowballItem)) e.isCancelled = true
    }
}