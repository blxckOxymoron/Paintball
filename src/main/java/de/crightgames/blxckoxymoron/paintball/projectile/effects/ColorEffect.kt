package de.crightgames.blxckoxymoron.paintball.projectile.effects

import de.crightgames.blxckoxymoron.paintball.inc.ColorReplace
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileHitBlockEvent

class ColorEffect : ProjectileEffect() {
    override fun onBlockHit(e: ProjectileHitBlockEvent): Boolean {
        val shooter = e.projectile.shooter ?: return false
        ColorReplace.replaceRadius(e.location, shooter, radius = e.data)
        return true
    }
}