package de.crightgames.blxckoxymoron.paintball.game.projectile

import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// do we need deceleration?
class GameProjectile(
    val type: ProjectileType,
    val location: Location,
    direction: Vector = location.direction.clone().normalize(),
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

    init {
        location.direction = direction.multiply(type.speed)
        entity?.setGravity(false)
        projectilesInWorld.add(this)
    }

    private val createdAt = System.currentTimeMillis()
    val isOverLifetime
        get() = (System.currentTimeMillis() - createdAt).milliseconds > MAX_LIFETIME

    var shouldBeRemoved = false

    fun removeFromWorld() {
        this.entity?.remove()
    }
}