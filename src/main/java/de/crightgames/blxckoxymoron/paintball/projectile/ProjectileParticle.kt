package de.crightgames.blxckoxymoron.paintball.projectile

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

// do we need a setting for density?
enum class ProjectileParticle(val create: (loc: Location, dir: Vector) -> Unit) {
    NONE({ _, _ ->}),
    FIREWORKS(projParticleTrail(Particle.FIREWORKS_SPARK, extra = 0.0)),
    BLUE(dustParticleTrail(Particle.DustOptions(Color.TEAL, 0.8F))),
    RED(dustParticleTrail(Particle.DustOptions(Color.RED, 0.8F))),
    CRIT(projParticleTrail(Particle.CRIT, extra = 0.05));
}