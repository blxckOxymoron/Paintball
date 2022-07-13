package de.crightgames.blxckoxymoron.paintball.projectile.effects

import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileRemoveEvent
import org.bukkit.Particle

class FlashEffect : ProjectileEffect() {
    override fun onDestroyed(e: ProjectileRemoveEvent) {
        e.location.world?.spawnParticle(Particle.FLASH, e.location, 1, 0.0, 0.0, 0.0)
    }
}