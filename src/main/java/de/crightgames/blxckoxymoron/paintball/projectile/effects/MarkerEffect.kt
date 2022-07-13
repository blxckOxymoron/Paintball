package de.crightgames.blxckoxymoron.paintball.projectile.effects

import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileRemoveEvent
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Snowball
import org.bukkit.util.Vector

class MarkerEffect : ProjectileEffect() {
    override fun onDestroyed(e: ProjectileRemoveEvent) {
        val mark = e.location.world?.spawnEntity(e.location, EntityType.SNOWBALL) as Snowball
        mark.setGravity(false)
        mark.velocity = Vector(0, 0, 0)

        Bukkit.getScoreboardManager()?.mainScoreboard?.getTeam("gun-markers")?.addEntry(
            mark.uniqueId.toString()
        )
        mark.isGlowing = true
    }
}