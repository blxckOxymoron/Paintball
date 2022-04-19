package de.crightgames.blxckoxymoron.paintball.game.config

import de.crightgames.blxckoxymoron.paintball.game.IncMaterial
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.ConfigurationSerialization
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class GameConfig() : ConfigurationSerializable {

    companion object {
        fun registerConfigClasses() {
            ConfigurationSerialization.registerClass(ConfigDuration::class.java)
            ConfigurationSerialization.registerClass(ConfigTeam::class.java)
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
     * restart
     */
    var durations = DefaultConfig.durations
    var autostart = DefaultConfig.autostart
    var minimumPlayers = DefaultConfig.minimumPlayers
    var teams = DefaultConfig.teams
    var colorRadius = DefaultConfig.colorRadius
    var arenaWorldName = DefaultConfig.arenaWorldName
    var lastArenaName = DefaultConfig.lastArenaName
    var easterMode = DefaultConfig.easterMode

    var noReplace = DefaultConfig.noReplace

    constructor(cfg: MutableMap<String, Any>) : this() {
        val cfgDurations = cfg["durations"] as? Map<*, *>
        cfgDurations?.forEach { entry ->
            val cfgDurKey = entry.key as? String ?: return@forEach
            val cfgDurVal = entry.value as? ConfigDuration ?: return@forEach
            if (!durations.containsKey(cfgDurKey)) return@forEach

            durations[cfgDurKey] = cfgDurVal.duration
        }

        val cfgTeams = cfg["teams"] as? List<*>
        cfgTeams?.filterIsInstance<ConfigTeam>()?.let { teams = it.toMutableList() }

        val cfgNoReplace = cfg["noReplace"] as? List<*>
        val filteredNoReplace = cfgNoReplace?.filterIsInstance<String>()?.mapNotNull {
            try {
                enumValueOf<Material>(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }?.toMutableList()
        if (filteredNoReplace != null) noReplace = filteredNoReplace

        (cfg["autostart"] as? Boolean)?.let { autostart = it }
        (cfg["easterMode"] as? Boolean)?.let { easterMode = it }
        (cfg["minimumPlayers"] as? Int)?.let { minimumPlayers = it }
        (cfg["colorRadius"] as? Int)?.let { colorRadius = it }
        (cfg["arenaWorldName"] as? String)?.let { arenaWorldName = it }
        (cfg["lastArenaName"] as? String)?.let { lastArenaName = it }

    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            "durations" to durations.map { it.key to ConfigDuration(it.value) }.toMap(),
            "teams" to teams,
            "noReplace" to noReplace.map { it.name },
            "autostart" to autostart,
            "easterMode" to easterMode,
            "minimumPlayers" to minimumPlayers,
            "colorRadius" to colorRadius,
            "arenaWorldName" to arenaWorldName,
            "lastArenaName" to lastArenaName,
        )
    }


    private object DefaultConfig {
        const val minimumPlayers = 4
        const val autostart = true
        const val easterMode = false
        const val colorRadius = 3
        const val arenaWorldName = "arena"
        const val lastArenaName = ""
        val noReplace = mutableListOf(
            Material.REDSTONE_LAMP, Material.GLOWSTONE, Material.BARREL, Material.BEACON, Material.BEDROCK
        )
        val teams = mutableListOf(
            ConfigTeam(IncMaterial.BLUE, "" + ChatColor.DARK_AQUA + "Blau" + ThemeBuilder.DEFAULT, null),
            ConfigTeam(IncMaterial.RED, "" + ChatColor.DARK_RED + "Rot" + ThemeBuilder.DEFAULT, null)
        )
        val durations = mutableMapOf(
            "game"      to 2.minutes,
            "gameLoop"  to 500.milliseconds,
            "refill"    to 750.milliseconds,
            "respawn"   to 10.seconds,
            "shot"      to 100.milliseconds,
            "kill"      to 15.seconds,
            "timer"     to 10.seconds,
            "restart"   to 30.seconds,
        )
    }
}