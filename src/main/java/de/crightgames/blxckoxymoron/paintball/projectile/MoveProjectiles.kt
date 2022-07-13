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
        GameProjectile.projectilesInWorld.removeIf { pj ->
            val changeDir = pj.motion.clone().multiply(updateSpeed.toFloat())

            val entityRay = pj.location.world?.rayTraceEntities(
                pj.location,
                changeDir,
                changeDir.length(),
                projectileRadius
            ) { it.uniqueId != pj.shooter?.uniqueId }

            val blockRay = pj.location.world?.rayTraceBlocks(
                pj.location,
                changeDir,
                changeDir.length(),
                FluidCollisionMode.NEVER,
                true
            )

            val hitEntityEvent = entityRay?.let {
                ProjectileHitEntityEvent(
                    it.hitPosition.toLocation(it.hitEntity!!.world),
                    pj,
                    it.hitEntity!!,
                )
            }

            val hitBlockEvent = blockRay?.let {
                ProjectileHitBlockEvent(
                    it.hitPosition.toLocation(it.hitBlock!!.world),
                    pj,
                    it.hitBlockFace!!,
                    it.hitBlock!!
                )
            }

            val events = listOfNotNull(hitEntityEvent, hitBlockEvent)
                .sortedBy { it.location.distance(pj.location) }

            var activatedEffects = listOf<AllProjectileEffects>()

            val hitEvent = events.firstOrNull { event ->
                activatedEffects = pj.type.effects.filter {
                    event.data = it.second
                    it.first.handler.onEvent(event)
                }.map { it.first }

                return@firstOrNull activatedEffects.any()
            }

            if (hitEvent != null || pj.isOverLifetime) {
                hitEvent?.let { event ->
                    event.data = 0
                    val endEvent = ProjectileRemoveEvent(
                        event.location,
                        pj,
                        activatedEffects,
                        event
                    )
                    pj.type.effects.forEach {
                        endEvent.data = it.second
                        it.first.handler.onEvent(endEvent)
                    }
                    pj.type.particle.create(pj.location.clone(), event.location.clone().subtract(pj.location.clone()).toVector())
                }
                pj.removeFromWorld()
                return@removeIf true
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
            return@removeIf false
        }
    }
}