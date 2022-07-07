package de.crightgames.blxckoxymoron.paintball.game.projectile

import de.crightgames.blxckoxymoron.paintball.util.ProjectileUtils.projParticle
import de.crightgames.blxckoxymoron.paintball.util.ProjectileUtils.projParticleTrail
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

private typealias ParticleCreator = (loc: Location, dir: Vector) -> Unit

// do we need a setting for density?
enum class ProjectileParticle(val create: ParticleCreator) {
    NONE({ _, _ ->}),
    BARRIER(projParticle(Particle.BARRIER)),
    FIREWORKS(projParticleTrail(Particle.FIREWORKS_SPARK));
}