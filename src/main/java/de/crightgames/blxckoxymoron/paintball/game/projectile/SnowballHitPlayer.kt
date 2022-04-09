package de.crightgames.blxckoxymoron.paintball.game.projectile

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.Paintball.Companion.inWholeTicks
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.game.Scores
import de.crightgames.blxckoxymoron.paintball.game.Scores.plusAssign
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.entity.Sheep
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent

class SnowballHitPlayer : Listener {

    @EventHandler
    fun onSnowballHit(e: ProjectileHitEvent) {
        val entity = e.hitEntity ?: return
        val shooter = e.entity.shooter as? Player ?: return

        val team = Paintball.gameConfig.teams.find { it.players.contains(shooter) } ?: return

        if (entity is Sheep) {
            entity.color = try {
                enumValueOf<DyeColor>(team.material.color)
            } catch (_: IllegalArgumentException) { return }
        }
        val hitPlayer = entity as? Player

        if (hitPlayer == null || team.players.contains(hitPlayer)) {
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

        // Scores
        Scores.killsObj?.getScore(shooter.name)?.plusAssign(1)
        Scores.deathsObj?.getScore(hitPlayer.name)?.plusAssign(1)

        hitPlayer.gameMode = GameMode.SPECTATOR
        Bukkit.getScheduler().runTaskLater(
            Paintball.INSTANCE,
            Runnable { Game.respawnPlayer(hitPlayer) },
            Paintball.gameConfig.durations["respawn"]!!.inWholeTicks
        )

        Paintball.lastKill[shooter.uniqueId] = System.currentTimeMillis()

        shooter.world.getEntitiesByClass(Snowball::class.java).forEach {
            if (!it.item.isSimilar(Game.snowballItem)) return@forEach
            val sbShooter = it.shooter as? Player ?: return@forEach
            if (sbShooter.uniqueId == shooter.uniqueId) it.fizzleOut()
        }

    }

    companion object {
        fun Snowball.fizzleOut() {
            this.world.spawnParticle(Particle.FIREWORKS_SPARK, this.location, 2, 0.1, 0.1, 0.1, 0.0)
            this.remove()
        }
    }

}
