package de.crightgames.blxckoxymoron.paintball.projectile.effects

import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileRemoveEvent
import org.bukkit.Color
import org.bukkit.Particle

class DustEffect : ProjectileEffect() {

    override fun onDestroyed(e: ProjectileRemoveEvent) {
        e.location.world?.spawnParticle(
            Particle.REDSTONE,
            e.location,
            10,
            0.3,
            0.3,
            0.3,
            5.0,
            Particle.DustOptions(Color.fromRGB(e.data), 1.8F)
        )
    }
}