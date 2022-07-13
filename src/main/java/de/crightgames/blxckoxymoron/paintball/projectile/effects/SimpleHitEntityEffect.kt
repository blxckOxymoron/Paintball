package de.crightgames.blxckoxymoron.paintball.projectile.effects

import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileHitEntityEvent

class SimpleHitEntityEffect : ProjectileEffect() {
    override fun onEntityHit(e: ProjectileHitEntityEvent): Boolean {
        return true
    }
}