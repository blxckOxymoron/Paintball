package de.crightgames.blxckoxymoron.paintball.game.projectile.listeners

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.Paintball.Companion.inWholeTicks
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.game.Scores
import de.crightgames.blxckoxymoron.paintball.game.Scores.plusAssign
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigTeam.Companion.team
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigTeam.Companion.teamEffect
import de.crightgames.blxckoxymoron.paintball.util.ColorReplace
import de.crightgames.blxckoxymoron.paintball.game.PlayerHitHandler
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.entity.Sheep
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
        val hitTeam = hitPlayer?.team

        if (hitTeam == null) {
            e.isCancelled = true
            return
        }

        val isNearSpawn =
            runCatching {
                hitPlayer.location.distance(hitTeam.spawnPosInGame ?: throw Error()) < Paintball.gameConfig.spawnProtection
            }.getOrDefault(false)

        if (team.players.contains(hitPlayer) || isNearSpawn) {
            e.isCancelled = true
            return
        }

        shooter.playSound(shooter.location, Sound.ENTITY_TURTLE_EGG_HATCH, 100F, 1F)
        hitPlayer.playSound(hitPlayer.location, Sound.ENTITY_TURTLE_EGG_BREAK, SoundCategory.MASTER, 100F, .8F)



        val wasKilled = PlayerHitHandler(hitPlayer, hitTeam, team).wasHit()
        if (!wasKilled) return

        Bukkit.broadcastMessage(ThemeBuilder.themed(
            "*${hitPlayer.name}* wurde von *${shooter.name}* abgeschossen!"
        ))

        val firework = hitPlayer.world.spawnEntity(hitPlayer.eyeLocation, EntityType.FIREWORK) as Firework
        firework.teamEffect(team)
        firework.detonate()

        ColorReplace.replaceRadius(hitPlayer.location, shooter, team, 2.0)

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
        Paintball.lastDeath[hitPlayer.uniqueId] = now

    }
}
