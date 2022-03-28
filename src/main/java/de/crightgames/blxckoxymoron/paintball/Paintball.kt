package de.crightgames.blxckoxymoron.paintball

import de.crightgames.blxckoxymoron.paintball.commands.PaintballCommand
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.game.PlayerJoinLeave
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballDrop
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballHitBlock
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballHitPlayer
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballUse
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class Paintball : JavaPlugin() {

    companion object {
        var autostart = false
        var minimumPlayers = 8
        var teamSpawns = mutableListOf<Location?>()
        var teams = mutableListOf<MutableList<Player>>()

        init {
            teamSpawns = Game.teamNames.map { null }.toMutableList()
            teams = Game.teamNames.map { mutableListOf<Player>() }.toMutableList()
        }

        lateinit var INSTANCE: Paintball

        val lastShot = mutableMapOf<UUID, Long>()
        val lastKill = mutableMapOf<UUID, Long>()

        const val COLOR_RADIUS = 3 // blocks

        // Cooldowns
        val REFILL_SPEED = 0.75.seconds // ticks
        val RESPAWN_COOLDOWN = 10.seconds // ticks
        val SHOT_COOLDOWN = 100.milliseconds // ms
        val KILL_COOLDOWN = 15000.milliseconds // ms
        val GAME_DURATION = 2.minutes //s

        inline val Duration.inWholeTicks: Long
            get()  = floor(toDouble(DurationUnit.MILLISECONDS) / 50).toLong()

    }

    override fun onEnable() {
        // Plugin startup logic
        INSTANCE = this

        // commands
        PaintballCommand().register(this)

        // listeners
        val pm = Bukkit.getPluginManager()
        pm.registerEvents(PlayerJoinLeave(), this)

        pm.registerEvents(SnowballUse(), this)
        pm.registerEvents(SnowballHitBlock(), this)
        pm.registerEvents(SnowballHitPlayer(), this)
        pm.registerEvents(SnowballDrop(), this)


        // config
        teamSpawns = config.getList("teamspawns")?.filterIsInstance<Location>()?.toMutableList() ?: teamSpawns
        minimumPlayers = config.getInt("minimumplayers", minimumPlayers)
        autostart = config.getBoolean("autostart", autostart)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        config.set("teamspawns", teamSpawns)
        config.set("minimumplayers", minimumPlayers)
        config.set("autostart", autostart)
        saveConfig()
    }
}