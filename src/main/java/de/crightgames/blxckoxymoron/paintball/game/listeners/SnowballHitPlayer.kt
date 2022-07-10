package de.crightgames.blxckoxymoron.paintball.game.listeners

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.config.ConfigTeam.Companion.team
import de.crightgames.blxckoxymoron.paintball.config.ConfigTeam.Companion.teamEffect
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.game.PlayerHitHandler
import de.crightgames.blxckoxymoron.paintball.inc.ColorReplace
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
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

        // COLOR_SHEEP Effect
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

        // INTO DAMAGE
        val isNearSpawn =
            runCatching {
                hitPlayer.location.distance(hitTeam.spawnPosInGame ?: throw Error()) < Paintball.gameConfig.spawnProtection
            }.getOrDefault(false)

        if (team.players.contains(hitPlayer) || isNearSpawn) {
            e.isCancelled = true
            return
        }

        // INTO EFFECTS

        shooter.playSound(shooter.location, Sound.ENTITY_TURTLE_EGG_HATCH, 100F, 1F) // ✅
        hitPlayer.playSound(hitPlayer.location, Sound.ENTITY_TURTLE_EGG_BREAK, SoundCategory.MASTER, 100F, .8F) // damage


        val wasKilled = PlayerHitHandler(hitPlayer, hitTeam, shooter, team).wasHit()
        if (!wasKilled) return

        Bukkit.broadcastMessage(ThemeBuilder.themed( // hit handler
            "*${hitPlayer.name}* wurde von *${shooter.name}* abgeschossen!"
        ))

        val firework = hitPlayer.world.spawnEntity(hitPlayer.eyeLocation, EntityType.FIREWORK) as Firework // damage
        firework.teamEffect(team)
        firework.detonate()

        ColorReplace.replaceRadius(hitPlayer.location, shooter, team, 2.0)

        // Scores


    }
}
