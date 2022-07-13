package de.crightgames.blxckoxymoron.paintball.projectile

import de.crightgames.blxckoxymoron.paintball.projectile.effects.*

// the boolean is weather to remove the projectile
/**
 * x flash particle
 * x poof particle
 * x white ash
 * o squid ink
 * o smoke
 */
enum class AllProjectileEffects(
    val handler: ProjectileEffect
) {
    SIMPLE_HIT_BLOCK(
        SimpleHitBlockEffect()
    ),
    SIMPLE_HIT_ENTITY(
        SimpleHitEntityEffect()
    ),
    ASH(
        AshEffect()
    ),
    DUST(
        DustEffect()
    ),
    FLASH(
        FlashEffect()
    ),
    COLOR(
        ColorEffect()
    ),
    COLOR_SHEEP(
        ColorSheepEffect()
    ),
    FIREWORK(
        FireworkEffect()
    ),
    DAMAGE(
        DamageEffect()
    ),
    MARKER(
        MarkerEffect()
    ),
}