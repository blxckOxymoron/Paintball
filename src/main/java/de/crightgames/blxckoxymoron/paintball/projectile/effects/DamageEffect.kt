package de.crightgames.blxckoxymoron.paintball.projectile.effects

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.config.ConfigTeam.Companion.team
import de.crightgames.blxckoxymoron.paintball.config.ConfigTeam.Companion.teamEffect
import de.crightgames.blxckoxymoron.paintball.game.PlayerHitHandler
import de.crightgames.blxckoxymoron.paintball.inc.ColorReplace
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileHitEntityEvent
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player

class DamageEffect : ProjectileEffect() {
    override fun onEntityHit(e: ProjectileHitEntityEvent): Boolean {
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
        ) return false

        val isNearSpawn = e.location.distance(spawn) < Paintball.gameConfig.spawnProtection

        if (isNearSpawn) return true

        // team protection (can't hit through mates rn)
        val isTeammate = team.players.contains(e.hitEntity)
        if (isTeammate) return true

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

        return true
    }
}