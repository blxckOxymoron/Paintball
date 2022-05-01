package de.crightgames.blxckoxymoron.paintball.game.config

import de.crightgames.blxckoxymoron.paintball.game.IncMaterial
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class GameConfig(cfg: MutableMap<String, Any> = mutableMapOf()) : ConfigurationSerializable {

    /**
     * game, gameLoop, refill, respawn, shot, kill, timer, restart, regen
     */
    var durations = mapOf<String, Duration>()

    var autostart = DefaultConfig.autostart
    var minimumPlayers = DefaultConfig.minimumPlayers
    var teams = DefaultConfig.teams
    var colorRadius = DefaultConfig.colorRadius
    var arenaWorldName = DefaultConfig.arenaWorldName
    var lastArenaName = DefaultConfig.lastArenaName
    var easterMode = DefaultConfig.easterMode
    var playerHealth = DefaultConfig.playerHealth
    var spawnProtection = DefaultConfig.spawnProtection

    var noReplace = DefaultConfig.noReplace

    init {
        val cfgDurations = cfg["durations"] as? Map<*, *> ?: mapOf<String, ConfigDuration>()
        val mappedDurs =
            DefaultConfig.durations +
            cfgDurations.mapNotNull { entry ->
                val cfgDurKey = entry.key as? String ?: return@mapNotNull null
                val cfgDurVal = entry.value as? ConfigDuration ?: return@mapNotNull null

                return@mapNotNull cfgDurKey to cfgDurVal.duration
            }.toMap()

        durations = mappedDurs.toMutableMap()

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
        (cfg["playerHealth"] as? Int)?.let { playerHealth = it }
        (cfg["spawnProtection"] as? Int)?.let { spawnProtection = it }
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
            "playerHealth" to playerHealth,
            "spawnProtection" to spawnProtection,
        )
    }

    private object DefaultConfig {
        const val minimumPlayers = 4
        const val autostart = true
        const val easterMode = false
        const val colorRadius = 3
        const val arenaWorldName = "arena"
        const val lastArenaName = ""
        const val playerHealth = 5
        const val spawnProtection = 5
        val noReplace = mutableListOf(
            Material.REDSTONE_LAMP, Material.GLOWSTONE, Material.BARRIER, Material.BEACON, Material.BEDROCK
        )
        val teams = mutableListOf(
            ConfigTeam(IncMaterial.BLUE, "" + ChatColor.DARK_AQUA + "Blau" + ThemeBuilder.DEFAULT, null),
            ConfigTeam(IncMaterial.RED, "" + ChatColor.DARK_RED + "Rot" + ThemeBuilder.DEFAULT, null)
        )
        //* this also sets the allowed keys
        val durations = mutableMapOf(
            "game"      to 2.minutes,
            "gameLoop"  to 200.milliseconds,
            "refill"    to 500.milliseconds,
            "respawn"   to 10.seconds,
            "shot"      to 100.milliseconds,
            "timer"     to 10.seconds,
            "restart"   to 30.seconds,
            "regen"     to 5.seconds,
        )
    }
}