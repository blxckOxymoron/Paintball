package de.crightgames.blxckoxymoron.paintball.game.config

import de.crightgames.blxckoxymoron.paintball.game.Game
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.ConfigurationSerialization
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class GameConfig() : ConfigurationSerializable {

    companion object {
        fun registerConfigClasses() {
            ConfigurationSerialization.registerClass(ConfigDuration::class.java)
            ConfigurationSerialization.registerClass(GameConfig::class.java)
        }
    }

    /**
     * game
     * gameLoop
     * refill
     * respawn
     * shot
     * kill
     * timer
     */
    var durations = mutableMapOf(
        "game"      to 2.minutes,
        "gameLoop"  to 500.milliseconds,
        "refill"    to 750.milliseconds,
        "respawn"   to 10.seconds,
        "shot"      to 100.milliseconds,
        "kill"      to 8.seconds,
        "timer"     to 10.seconds,
    )

    var autostart = false
    var minimumPlayers = 8
    var teamSpawns = Game.teamNames.map { null }.toMutableList<Location?>()

    constructor(cfg: MutableMap<String, Any>) : this() {
        val cfgDurations = cfg["durations"] as? Map<*, *>
        cfgDurations?.forEach { entry ->
            val cfgDurKey = entry.key as? String ?: return@forEach
            val cfgDurVal = entry.value as? ConfigDuration ?: return@forEach
            if (!durations.containsKey(cfgDurKey)) return@forEach

            durations[cfgDurKey] = cfgDurVal.duration
        }

        val cfgTeamSpawns = cfg["teamSpawns"] as? List<*>
        cfgTeamSpawns?.filterIsInstance<Location>()?.toMutableList()?.forEachIndexed { index, sp ->
            if (index < teamSpawns.size) teamSpawns[index] = sp
        }

        (cfg["autostart"] as? Boolean)?.let { autostart = it }
        (cfg["minimumPlayers"] as? Int)?.let { minimumPlayers = it }

    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            "durations" to durations.map { it.key to ConfigDuration(it.value) }.toMap(),
            "autostart" to autostart,
            "minimumPlayers" to minimumPlayers,
            "teamSpawns" to teamSpawns
        )
    }

}