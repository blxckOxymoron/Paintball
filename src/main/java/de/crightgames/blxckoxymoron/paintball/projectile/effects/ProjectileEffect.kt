package de.crightgames.blxckoxymoron.paintball.projectile.effects

import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileHitBlockEvent
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileHitEntityEvent
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileHitEvent
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileRemoveEvent

abstract class ProjectileEffect {

    open fun onBlockHit(e: ProjectileHitBlockEvent): Boolean {
        return false
    }

    open fun onEntityHit(e: ProjectileHitEntityEvent): Boolean {
        return false
    }

    open fun onDestroyed(e: ProjectileRemoveEvent) {}

    fun onEvent(e: ProjectileHitEvent): Boolean {
        return when (e) {
            is ProjectileHitBlockEvent -> onBlockHit(e)
            is ProjectileHitEntityEvent -> onEntityHit(e)
            is ProjectileRemoveEvent -> {
                onDestroyed(e)
                false
            }
            else -> false
        }
    }
}