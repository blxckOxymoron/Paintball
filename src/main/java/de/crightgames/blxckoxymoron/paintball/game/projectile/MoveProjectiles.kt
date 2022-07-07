package de.crightgames.blxckoxymoron.paintball.game.projectile

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

        fun startLoop(p: Plugin, updateSpeed: Long = 20L) {
            if (loop?.isCancelled == false) return

            loop = Bukkit.getScheduler().runTaskTimer(
                p,
                MoveProjectiles(),
                0L,
                updateSpeed
            )
            this.updateSpeed = updateSpeed
        }
    }

    override fun run() {
        GameProjectile.projectilesInWorld.forEach { pj ->
            val changeDir = pj.location.direction.clone().multiply(updateSpeed.toFloat())

            val hit = pj.location.world?.rayTrace(
                pj.location,
                changeDir,
                changeDir.length(),
                FluidCollisionMode.NEVER,
                true,
                projectileRadius
            ) { it is Player }

            val shouldRemoveBlock = hit?.hitBlock?.let { block ->
                pj.type.effects.map { it.first.blockHit(it.second, block) }.any { it }
            } ?: false

            val shouldRemoveEntity = hit?.hitEntity?.let { player ->
                pj.type.effects.map { it.first.playerHit(it.second, player as Player) }.any {it}
            } ?: false

            if (shouldRemoveBlock || shouldRemoveEntity || pj.isOverLifetime) {
                pj.removeFromWorld()
                pj.shouldBeRemoved = true
                return@forEach
            }

            pj.type.particle.create(pj.location.clone(), changeDir)
            pj.location.add(changeDir)

            // gravity in blocks per second | 20 Ticks
            val nextDirection = pj.location.direction
            nextDirection.add(
                Vector(0.0, pj.type.gravity * (updateSpeed / 20.0), 0.0)
            )
            pj.location.direction = nextDirection

            // we might be able to set entity velocity with location direction once and leave out this update
            pj.entity?.velocity = nextDirection
            pj.entity?.teleport(pj.location)
        }
        GameProjectile.projectilesInWorld.removeIf { it.shouldBeRemoved }
    }
}