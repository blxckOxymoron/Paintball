package de.crightgames.blxckoxymoron.paintball.projectile

import de.crightgames.blxckoxymoron.paintball.inc.ColorReplace
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Particle
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Sheep
import org.bukkit.entity.Snowball
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

// the boolean is weather to remove the projectile
/**
 * x flash particle
 * x poof particle
 * x white ash
 * o squid ink
 * o smoke
 */
enum class ProjectileEffect(
    val handler: (e: ProjectileHitEvent) -> Boolean
) {
    SIMPLE_HIT_BLOCK(
        ProjectileEffectHandler {
            blockHit = { true }
        }
    ),
    SIMPLE_HIT_ENTITY(
        ProjectileEffectHandler {
            entityHit = { true }
        }
    ),
    ASH(
        ProjectileEffectHandler {
            blockHit = { e ->
                e.location.world?.spawnParticle(Particle.ASH, e.location, 5, 0.1, 0.1, 0.1, 0.0)
                e.location.world?.spawnParticle(Particle.WHITE_ASH, e.location, 5, 0.1, 0.1, 0.1, 0.0)
                false
            }
        }
    ),
    DUST(
        ProjectileEffectHandler {
            whenDestroyed = { e ->
                e.location.world?.spawnParticle(
                    Particle.REDSTONE,
                    e.location,
                    10,
                    0.3,
                    0.3,
                    0.3,
                    5.0,
                    Particle.DustOptions(Color.fromRGB(e.data), 1.8F)
                )
            }
        }
    ),
    FLASH(
        ProjectileEffectHandler {
            whenDestroyed = { e ->
                e.location.world?.spawnParticle(Particle.FLASH, e.location, 1, 0.0, 0.0, 0.0)
            }
        }
    ),
    COLOR(
        ProjectileEffectHandler {
            blockHit = hit@{ e ->
                val shooter = e.projectile.shooter ?: return@hit false
                ColorReplace.replaceRadius(e.location, shooter, radius = e.data)
                return@hit true
            }
        }
    ),
    COLOR_SHEEP(
        ProjectileEffectHandler {
            entityHit = hit@{ e ->
                if (e.hitEntity !is Sheep) return@hit false

                val color = org.bukkit.DyeColor.getByColor(Color.fromRGB(e.data))
                if (color == null) {
                    Bukkit.broadcastMessage(ThemeBuilder.themed(
                        "No color found for hex *${e.data.toString(16)}* use `DyeColor.CYAN.color.asRGB()`"
                    ))
                } else {
                    e.hitEntity.color = color
                }

                return@hit false
            }
        }
    ),
    FIREWORK(
        ProjectileEffectHandler {
            whenDestroyed = { e ->
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
            }
        }
    ),
    DAMAGE(
        ProjectileEffectHandler {
            entityHit = {
                TODO("damage the player")
            }
        }
    ),
    MARKER(
        ProjectileEffectHandler {
            whenDestroyed = { e ->
                val mark = e.location.world?.spawnEntity(e.location, EntityType.SNOWBALL) as Snowball
                mark.setGravity(false)
                mark.velocity = Vector(0, 0, 0)

                Bukkit.getScoreboardManager()?.mainScoreboard?.getTeam("gun-markers")?.addEntry(
                    mark.uniqueId.toString()
                )
                mark.isGlowing = true
            }
        }
    ),
    EFFECT_BLINDNESS(
        ProjectileEffectHandler {
            blockHit = { event ->
                effectCloudEffect(
                    PotionEffect(PotionEffectType.SPEED, 1, 2, false, false),
                    event
                )
            }
        }
    ),
    EFFECT_SPEED(
        ProjectileEffectHandler {
            blockHit = { event ->
                effectCloudEffect(
                    PotionEffect(PotionEffectType.SPEED, 1, 2, false, false),
                    event
                )
            }
        }
    );
}