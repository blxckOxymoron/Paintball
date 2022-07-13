package de.crightgames.blxckoxymoron.paintball.projectile

import de.crightgames.blxckoxymoron.paintball.util.VectorUtils
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Particle.REDSTONE
import org.bukkit.util.Vector

private typealias SpawnerFunction = (Location, Vector) -> Unit

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

// TODO smooth trail (spline)
fun projParticleTrail(
    p: Particle,
    count: Int = 1,
    x: Double = 0.0,
    y: Double = 0.0,
    z: Double = 0.0,
    extra: Double? = null,
    space: Double = 0.6
): SpawnerFunction {
    return { loc, dir ->
        VectorUtils.particleAlongVector(
            loc,
            dir,
            space
        ) {
            if (extra != null) it.world?.spawnParticle(p, it, count, x, y, z, extra)
            else it.world?.spawnParticle(p, it, count, x, y, z)
        }
    }
}

fun dustParticleTrail(
    data: DustOptions
): SpawnerFunction {
    return { loc, dir ->
        VectorUtils.particleAlongVector(
            loc,
            dir,
            data.size * 1.3
        ) {
            it.world?.spawnParticle(REDSTONE, it, 1, 0.0, 0.0, 0.0, 5.0, data)
        }
    }
}
