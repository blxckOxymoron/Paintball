package de.crightgames.blxckoxymoron.paintball.projectile

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

// do we need a setting for density?
enum class ProjectileParticle(val create: (loc: Location, dir: Vector) -> Unit) {
    NONE({ _, _ ->}),
    BARRIER(projParticle(Particle.BARRIER)),
    FIREWORKS(projParticleTrail(Particle.FIREWORKS_SPARK, extra = 0.0)),
    CRIT(projParticleTrail(Particle.CRIT, extra = 0.05));
}