package de.crightgames.blxckoxymoron.paintball.game

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.Paintball.Companion.inWholeTicks
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object Countdown {
    private var TIMER_SPEED = 1.seconds

    private var currentTime = Duration.ZERO
    private var decreaseTask: BukkitTask? = null

    private val countingDown
        get() = decreaseTask?.isCancelled == false

    fun checkAndStart() {
        val onlinePlayerCount = Game.players.size
        if (onlinePlayerCount < Paintball.gameConfig.minimumPlayers || !Paintball.gameConfig.autostart) return cancelStart()

        if (Game.state == Game.GameState.WAITING && !countingDown)
            start()

    }

    private fun start() {
        currentTime = Paintball.gameConfig.durations["timer"]!!
        decreaseTask = Bukkit.getScheduler().runTaskTimer(Paintball.INSTANCE, decrease, 0, TIMER_SPEED.inWholeTicks)
    }

    private val decrease = Runnable {

        val allPlayers = Game.players

        val enoughPlayers = allPlayers.size >= Paintball.gameConfig.minimumPlayers
        if (!enoughPlayers) return@Runnable cancelStart()

        allPlayers.forEach { notifyPlayer(it) }

        currentTime -= TIMER_SPEED
        if (currentTime < Duration.ZERO) {
            startGame()
        }
    }

    private fun startGame() {
        decreaseTask?.cancel()
        Game.start()
    }

    private fun cancelStart() {
        if (countingDown) Bukkit.broadcastMessage(ThemeBuilder.themed(":RED:Start abgebrochen::"))
        decreaseTask?.cancel()
    }

    private fun notifyPlayer(player: Player) {
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, SoundCategory.MASTER, 100F, 0.5F)
        player.sendTitle(
            ThemeBuilder.themed("*${if (currentTime <= Duration.ZERO) "GO" else currentTime.inWholeSeconds}*"),
            ThemeBuilder.themed("`Paintball`"),
            0,
            21,
            2,
        )
    }
}