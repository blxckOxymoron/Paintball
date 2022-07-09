package de.crightgames.blxckoxymoron.paintball.projectile

import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// do we need deceleration?
class GameProjectile(
    val type: ProjectileType,
    val location: Location,
    direction: Vector = location.direction,
) {
    companion object {
        val projectilesInWorld = mutableListOf<GameProjectile>() // direction inside Location

        private val MAX_LIFETIME = 30.seconds

        fun deleteAllProjectiles() {
            projectilesInWorld.forEach { it.removeFromWorld() }
            projectilesInWorld.clear()
        }
    }

    val entity = type.entity?.let { location.world?.spawnEntity(location, it) }
    val motion = direction.clone().normalize().multiply(type.speed)
    init {
        entity?.setGravity(false)
        projectilesInWorld.add(this)
    }

    private val createdAt = System.currentTimeMillis()
    val timeLived
        get() = (System.currentTimeMillis() - createdAt).milliseconds
    val isOverLifetime
        get() = timeLived > MAX_LIFETIME

    var shouldBeRemoved = false

    fun removeFromWorld() {
        this.entity?.remove()
    }
}