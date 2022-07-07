package de.crightgames.blxckoxymoron.paintball.game.listeners

import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.inc.ColorReplace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent

class SnowballHitBlock : Listener {

    @EventHandler
    fun onSnowballHit(e: ProjectileHitEvent) {

        val (player, team) = Game.checkProjectileEvent(e) ?: return

        val block = e.hitBlock ?: return

        ColorReplace.replaceRadius(block.location.clone().add(0.5, 0.0, 0.5), player, team)
    }
}