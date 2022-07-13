package de.crightgames.blxckoxymoron.paintball.projectile

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity

/**
 * location
 * player
 * block
 * particle
 * data
 */
interface ProjectileHitEvent {
    val location: Location
    val projectile: GameProjectile
    var data: Int
}

data class ProjectileRemoveEvent(
    override val location: Location,
    override val projectile: GameProjectile,
    val causedBy: List<AllProjectileEffects>,
    val hitEvent: ProjectileHitEvent,
    override var data: Int = 0,
) : ProjectileHitEvent

data class ProjectileHitEntityEvent(
    override val location: Location,
    override val projectile: GameProjectile,
    val hitEntity: Entity,
    override var data: Int = 0
) : ProjectileHitEvent

data class ProjectileHitBlockEvent(
    override val location: Location,
    override val projectile: GameProjectile,
    val hitBlockFace: BlockFace,
    val hitBlock: Block,
    override var data: Int = 0
) : ProjectileHitEvent