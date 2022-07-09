package de.crightgames.blxckoxymoron.paintball.projectile

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

// the boolean is weather to remove the projectile
enum class ProjectileEffect(
    val blockHit: (Int, Location, Block) -> Boolean,
    val playerHit: (Int, Location, Player) -> Boolean,
) {
    HIT(
        { _, _ -> true},
    ),
    COLOR(
        { strength, loc, bl ->
            TODO()
        },
        {_, _, _ -> false}
    ),
    FIREWORK(
        { id, loc ->
            val firework = loc.world?.spawnEntity(loc, EntityType.FIREWORK) as Firework
            val meta = firework.fireworkMeta
            meta.addEffect(
                FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL)
                    .withFlicker()
                    .withColor(Color.fromRGB(id))
                    .build()
            )
            firework.fireworkMeta = meta
            firework.detonate()
            false
        }
    ),
    DAMAGE(
        {_, _, _ -> false},
        { strength, loc, player ->
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

    constructor(anyHit: (Int, Location) -> Boolean): this({s, l, _ -> anyHit(s, l)}, { s, l, _ -> anyHit(s, l)})
}