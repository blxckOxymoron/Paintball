package de.crightgames.blxckoxymoron.paintball.projectile

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

// the boolean is weather to remove the projectile
enum class ProjectileEffect(
    val whenHit: (e: ProjectileHitEvent) -> Boolean
) {
    HIT_BLOCK(
        { true },
        { false },
    ),
    HIT_ENTITY(
        { false },
        { true }
    ),
    COLOR(
        { TODO("color on block hit") },
        { false }
    ),
    FIREWORK(
        { e ->
            val firework = e.location.world?.spawnEntity(e.location, EntityType.FIREWORK) as Firework
            val meta = firework.fireworkMeta
            meta.addEffect(
                FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL)
                    .withFlicker()
                    .withColor(Color.fromRGB(e.data))
                    .build()
            )
            firework.fireworkMeta = meta
            firework.detonate()
            false
        }
    ),
    DAMAGE(
        { false },
        { TODO("Damage the Player") }
    ),
    SMOKE(
        { TODO("create smoke effect") }
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

    constructor(
        blockHit: (ProjectileHitBlockEvent) -> Boolean,
        entityHit: (ProjectileHitEntityEvent) -> Boolean,
    ): this ({ e ->
        when (e) {
            is ProjectileHitBlockEvent -> blockHit(e)
            is ProjectileHitEntityEvent -> entityHit(e)
            else -> false
        }
    })
}