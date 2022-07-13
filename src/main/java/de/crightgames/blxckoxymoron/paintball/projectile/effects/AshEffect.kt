package de.crightgames.blxckoxymoron.paintball.projectile.effects

import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileHitBlockEvent
import org.bukkit.Particle

class AshEffect : ProjectileEffect() {

    override fun onBlockHit(e: ProjectileHitBlockEvent): Boolean {
        e.location.world?.spawnParticle(Particle.ASH, e.location, 5, 0.1, 0.1, 0.1, 0.0)
        e.location.world?.spawnParticle(Particle.WHITE_ASH, e.location, 5, 0.1, 0.1, 0.1, 0.0)

        return false
    }
}