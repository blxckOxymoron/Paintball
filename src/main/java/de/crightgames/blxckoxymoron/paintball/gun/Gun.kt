package de.crightgames.blxckoxymoron.paintball.gun

import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileType

data class Gun(
    val projectile: ProjectileType,
    val rateOfFire: Long = 5,
    val spray: Double = 0.1,
    val bullets: Int = 1 // number of bullets shot each shot 1 for simple weapons, 5 for shotguns
)