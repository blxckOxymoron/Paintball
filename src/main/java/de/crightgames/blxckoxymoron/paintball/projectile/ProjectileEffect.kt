package de.crightgames.blxckoxymoron.paintball.projectile

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

// the boolean is weather to remove the projectile
enum class ProjectileEffect(
    val blockHit: (Int, Block) -> Boolean,
    val playerHit: (Int, Player) -> Boolean,
) {
    HIT(
        { _, _ -> true},
        { _, _ -> false}
    ),
    COLOR(
        { strength, bl ->
            TODO()
        },
        {_, _ -> false}
    ),
    DAMAGE(
        {_, _ -> false},
        { strength, loc ->
            TODO()
        }
    ),
    DUST(
        { strength, loc ->
            TODO()
        }
    ),
    EFFECT_BLINDNESS(
        effectCloudEffect(
            PotionEffect(PotionEffectType.BLINDNESS, 1, 2, false, false)
        )
    ),
    EFFECT_SPEED(
        effectCloudEffect(
            PotionEffect(PotionEffectType.SPEED, 1, 2, false, false)
        )
    );

    constructor(anyHit: (Int, Location) -> Boolean): this({s, b -> anyHit(s, b.location)}, { s, p -> anyHit(s, p.location)})

    companion object {

    }
}