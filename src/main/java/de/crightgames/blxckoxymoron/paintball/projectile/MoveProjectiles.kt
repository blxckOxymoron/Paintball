package de.crightgames.blxckoxymoron.paintball.projectile

import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class MoveProjectiles : Runnable {

    companion object {
        var loop: BukkitTask? = null
        var updateSpeed = 2L
        val projectileRadius = 0.1

        fun runLoop(p: Plugin, speed: Long = 20L) {
            loop.takeIf { it?.isCancelled == false }?.cancel()

            loop = Bukkit.getScheduler().runTaskTimer(
                p,
                MoveProjectiles(),
                0L,
                speed
            )
            updateSpeed = speed
        }
    }

    override fun run() {
        GameProjectile.projectilesInWorld.forEach { pj ->
            val changeDir = pj.motion.clone().multiply(updateSpeed.toFloat())

            val hit = pj.location.world?.rayTrace(
                pj.location,
                changeDir,
                changeDir.length(),
                FluidCollisionMode.NEVER,
                true,
                projectileRadius
            ) { it.uniqueId != pj.shooter?.uniqueId }

            val hitEvent: ProjectileHitEvent? =
                if (hit?.hitBlock != null && hit.hitBlockFace != null) ProjectileHitBlockEvent(
                    hit.hitPosition.toLocation(hit.hitBlock!!.world),
                    pj,
                    hit.hitBlockFace!!,
                    hit.hitBlock!!,
                ) else if (hit?.hitEntity != null) ProjectileHitEntityEvent(
                    hit.hitPosition.toLocation(hit.hitEntity!!.world),
                    pj,
                    hit.hitEntity!!,
                ) else null

            var shouldRemove = false
            if (hitEvent != null) {
                shouldRemove = pj.type.effects.map {
                    hitEvent.data = it.second
                    it.first.whenHit(hitEvent)
                }.any { it }
            }

            if (hit != null && (shouldRemove || pj.isOverLifetime)) {
                pj.type.particle.create(pj.location.clone(), hit.hitPosition.subtract(pj.location.clone().toVector()))
                pj.removeFromWorld()
                pj.shouldBeRemoved = true
                return@forEach
            }

            pj.type.particle.create(pj.location.clone(), changeDir)

            pj.location.add(changeDir)

            // gravity in blocks per second | 20 Ticks
            val nextDirection = pj.motion.add(
                Vector(0.0, 0.5 * pj.type.gravity * (updateSpeed / 20.0), 0.0) // 0.5 because it's 1/2 * g * t^2
            )
            pj.location.direction = nextDirection

            // we might be able to set entity velocity with location direction once and leave out this update
            pj.entity?.velocity = nextDirection
            pj.entity?.teleport(pj.location)
        }
        GameProjectile.projectilesInWorld.removeIf { it.shouldBeRemoved }
    }
}