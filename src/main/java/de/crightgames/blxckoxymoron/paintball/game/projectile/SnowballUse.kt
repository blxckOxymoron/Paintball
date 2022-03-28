package de.crightgames.blxckoxymoron.paintball.game.projectile

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.Paintball.Companion.inWholeTicks
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.game.Scores
import de.crightgames.blxckoxymoron.paintball.game.Scores.plusAssign
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.scheduler.BukkitTask
import java.util.*

class SnowballUse : Listener {

    companion object {
        fun refillSnowballsFor(player: Player, cancel: () -> Unit): Runnable {
            return Runnable {
                if (!player.inventory.containsAtLeast(Game.snowballItem, 16)) {
                    player.inventory.addItem(Game.snowballItem)
                } else {
                    cancel()
                }
            }
        }
        val playersWithRefill = mutableListOf<UUID>()
    }

    @EventHandler
    fun onSnowballSpawn(e: ProjectileLaunchEvent) {

        val snowball = e.entity as? Snowball ?: return
        if (!snowball.item.isSimilar(Game.snowballItem)) return

        val player = snowball.shooter as? Player ?: return

        if (!player.inventory.containsAtLeast(Game.snowballItem, 2)) {
            e.isCancelled = true
            return
        }

        val now = System.currentTimeMillis()

        val isOnCooldown = (
            now - (Paintball.lastShot[player.uniqueId]?: -Paintball.SHOT_COOLDOWN.inWholeMilliseconds) < Paintball.SHOT_COOLDOWN.inWholeMilliseconds ||
            now - (Paintball.lastKill[player.uniqueId]?: -Paintball.KILL_COOLDOWN.inWholeMilliseconds) < Paintball.KILL_COOLDOWN.inWholeMilliseconds
        )

        if (isOnCooldown) {
            e.isCancelled = true
            return
        }

        Scores.shotsObj?.getScore(player.name)?.plusAssign(1)

        if (!playersWithRefill.contains(player.uniqueId)) {
            playersWithRefill.add(player.uniqueId)
            var task: BukkitTask? = null

            task = Bukkit.getScheduler().runTaskTimer(
                Paintball.INSTANCE,
                refillSnowballsFor(player) {
                    playersWithRefill.remove(player.uniqueId)
                    task?.cancel()
                },
                Paintball.REFILL_SPEED.inWholeTicks,
                Paintball.REFILL_SPEED.inWholeTicks
            )
        }

        Paintball.lastShot[player.uniqueId] = now
    }
}