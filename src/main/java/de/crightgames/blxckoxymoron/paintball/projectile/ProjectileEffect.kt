package de.crightgames.blxckoxymoron.paintball.projectile

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.config.ConfigTeam.Companion.team
import de.crightgames.blxckoxymoron.paintball.config.ConfigTeam.Companion.teamEffect
import de.crightgames.blxckoxymoron.paintball.game.PlayerHitHandler
import de.crightgames.blxckoxymoron.paintball.inc.ColorReplace
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.*
import org.bukkit.entity.*
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
            entityHit = hit@{ e ->
                // spawn protection
                val shooter = e.projectile.shooter
                val team = shooter?.team
                val hitPlayer = e.hitEntity as? Player
                val hitPlayerTeam = hitPlayer?.team
                val spawn = team?.spawnPosInGame

                if (
                    shooter == null ||
                    team == null ||
                    hitPlayer == null ||
                    hitPlayerTeam == null ||
                    spawn == null
                ) return@hit false

                val isNearSpawn = e.location.distance(spawn) < Paintball.gameConfig.spawnProtection

                if (isNearSpawn) return@hit true

                // team protection (can't hit through mates rn)
                val isTeammate = team.players.contains(e.hitEntity)
                if (isTeammate) return@hit true

                // sound
                shooter.playSound(shooter.location, Sound.ENTITY_TURTLE_EGG_HATCH, 100F, 1F)
                e.hitEntity.world.playSound(e.location, Sound.ENTITY_TURTLE_EGG_BREAK, 100F, .8F)

                // hit handler
                val died = PlayerHitHandler(hitPlayer, hitPlayerTeam, shooter, team).wasHit(e.data)
                if (died) {
                    // explosion
                    val firework = hitPlayer.world.spawnEntity(hitPlayer.eyeLocation, EntityType.FIREWORK) as Firework // damage
                    firework.teamEffect(team)
                    firework.detonate()

                    // color
                    ColorReplace.replaceRadius(hitPlayer.location, shooter, team, 2.0)
                }

                return@hit true
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