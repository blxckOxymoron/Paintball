package de.crightgames.blxckoxymoron.paintball.projectile

import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.entity.Player
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
            ) { it is Player }

            val hitPos = hit?.hitPosition

            val shouldRemoveBlock = hitPos != null && hit.hitBlock?.let { block ->
                pj.type.effects.map { it.first.blockHit(it.second, hitPos.toLocation(block.world), block) }.any { it }
            } ?: false

            val shouldRemoveEntity = hitPos != null && hit.hitEntity?.let { player ->
                pj.type.effects.map { it.first.playerHit(it.second, hitPos.toLocation(player.world), player as Player) }.any {it}
            } ?: false

            if (hit != null && (shouldRemoveBlock || shouldRemoveEntity || pj.isOverLifetime)) {
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