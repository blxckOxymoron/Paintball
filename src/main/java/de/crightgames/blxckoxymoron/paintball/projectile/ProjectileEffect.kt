package de.crightgames.blxckoxymoron.paintball.projectile

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Snowball
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

// the boolean is weather to remove the projectile
enum class ProjectileEffect(
    val handler: (e: ProjectileHitEvent) -> Boolean
) {
    HIT_BLOCK(
        ProjectileEffectHandler {
            blockHit = { true }
        }
    ),
    HIT_ENTITY(
        ProjectileEffectHandler {
            entityHit = { true }
        }
    ),
    COLOR(
        ProjectileEffectHandler {
            blockHit = {
                TODO("color")
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
    SMOKE(
        ProjectileEffectHandler {
            whenDestroyed = {
                TODO("create smoke")
            }
        }
    ),
    MARKER(
      ProjectileEffectHandler {
          whenDestroyed = { e ->
              val item = e.location.world?.spawnEntity(e.location, EntityType.SNOWBALL) as Snowball
              item.setGravity(false)

              item.velocity = Vector(0, 0, 0)
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