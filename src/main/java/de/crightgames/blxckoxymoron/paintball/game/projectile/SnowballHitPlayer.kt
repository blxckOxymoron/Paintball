package de.crightgames.blxckoxymoron.paintball.game.projectile

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.Paintball.Companion.inWholeTicks
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.game.Scores
import de.crightgames.blxckoxymoron.paintball.game.Scores.plusAssign
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.entity.Sheep
import org.bukkit.entity.ThrowableProjectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent

class SnowballHitPlayer : Listener {

    @EventHandler
    fun onSnowballHit(e: ProjectileHitEvent) {

        val (shooter, team) = Game.checkProjectileEvent(e) ?: return

        val entity = e.hitEntity ?: return

        if (entity is Sheep) {
            entity.color = try {
                enumValueOf<DyeColor>(team.material.color)
            } catch (_: IllegalArgumentException) { return }
        }
        val hitPlayer = entity as? Player

        val isOnCooldown = (
            System.currentTimeMillis() - (Paintball.lastKill[shooter.uniqueId]?: -Paintball.gameConfig.durations["kill"]!!.inWholeMilliseconds)
            < Paintball.gameConfig.durations["kill"]!!.inWholeMilliseconds
        )

        if (hitPlayer == null || team.players.contains(hitPlayer) || isOnCooldown) {
            e.isCancelled = true
            return
        }


        Bukkit.broadcastMessage(ThemeBuilder.themed(
            "*${hitPlayer.name}* wurde von *${shooter.name}* abgeschossen!"
        ))
        hitPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(ThemeBuilder.themed(
            "In *${Paintball.gameConfig.durations["respawn"]!!.inWholeSeconds}*s bist du wieder im Spiel."
        )))
        shooter.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(ThemeBuilder.themed(
            "Warte *${Paintball.gameConfig.durations["kill"]!!.inWholeSeconds}*s, bis du wieder schießen kannst."
        )))

        hitPlayer.playSound(hitPlayer.location, Sound.ENTITY_TURTLE_EGG_BREAK, SoundCategory.MASTER, 100F, .8F)
        shooter.playSound(shooter.location, Sound.ENTITY_TURTLE_EGG_HATCH, 100F, 1F)

        // Scores
        Scores.killsObj?.getScore(shooter.name)?.plusAssign(1)
        Scores.deathsObj?.getScore(hitPlayer.name)?.plusAssign(1)

        hitPlayer.gameMode = GameMode.SPECTATOR
        Bukkit.getScheduler().runTaskLater(
            Paintball.INSTANCE,
            Runnable { Game.respawnPlayer(hitPlayer) },
            Paintball.gameConfig.durations["respawn"]!!.inWholeTicks
        )

        val now = System.currentTimeMillis()
        Paintball.lastKill[shooter.uniqueId] = now
        Paintball.lastDeath[hitPlayer.uniqueId] = now

    }

    companion object {
        fun ThrowableProjectile.fizzleOut() {
            this.world.spawnParticle(Particle.FIREWORKS_SPARK, this.location, 2, 0.1, 0.1, 0.1, 0.0)
            this.remove()
        }
    }

}
