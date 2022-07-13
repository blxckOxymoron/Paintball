package de.crightgames.blxckoxymoron.paintball.projectile.effects

import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileHitBlockEvent

class SimpleHitBlockEffect : ProjectileEffect() {
    override fun onBlockHit(e: ProjectileHitBlockEvent): Boolean {
        return true
    }
}