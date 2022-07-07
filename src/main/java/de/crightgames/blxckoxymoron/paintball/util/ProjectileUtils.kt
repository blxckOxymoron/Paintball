package de.crightgames.blxckoxymoron.paintball.util

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.AreaEffectCloud
import org.bukkit.entity.EntityType
import org.bukkit.potion.PotionEffect
import org.bukkit.util.Vector

private typealias SpawnerFunction = (Location, Vector) -> Unit
private typealias EffectFunction = (Int, Location) -> Boolean

object ProjectileUtils {
    fun effectCloudEffect(pot: PotionEffect): EffectFunction {
        return cloudCreator@{ strength, loc ->
            val aec = loc.world?.spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD) as? AreaEffectCloud
                ?: return@cloudCreator true

            aec.radius = strength.toFloat()
            aec.addCustomEffect(pot, true)

            return@cloudCreator true
        }
    }

    fun projParticle(
        p: Particle,
        count: Int = 1,
        x: Double = 0.0,
        y: Double = 0.0,
        z: Double = 0.0,
        extra: Double? = null
    ): SpawnerFunction {
        return { loc, _ ->
            loc.world?.spawnParticle(p, loc, count, x, y, z, extra)
        }
    }

    fun projParticleTrail(
        p: Particle,
        count: Int = 1,
        x: Double = 0.0,
        y: Double = 0.0,
        z: Double = 0.0,
        extra: Double? = null
    ): SpawnerFunction {
        return { loc, dir ->
            VectorUtils.particleAlongVector(
                loc,
                dir,
            ) {
                it.world?.spawnParticle(p, it, count, x, y, z, extra)
            }
        }
    }
}