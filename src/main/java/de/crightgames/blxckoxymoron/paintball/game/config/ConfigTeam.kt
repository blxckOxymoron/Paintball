package de.crightgames.blxckoxymoron.paintball.game.config

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.game.IncMaterial
import org.bukkit.Bukkit
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.boss.BarStyle
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import java.util.*

class ConfigTeam (val material: IncMaterial, val displayName: String, var spawnPos: Location?) : ConfigurationSerializable {

    companion object {

        private val playerTeamCache = mutableMapOf<UUID, ConfigTeam>()
        private var freshCache = true

        private fun resetCache() {
            playerTeamCache.clear()
            freshCache = true
        }

        val Player.team: ConfigTeam?
            get() {
                val teamFromCache = playerTeamCache[this.uniqueId]
                if (teamFromCache != null) return teamFromCache

                val teamSearched = Paintball.gameConfig.teams.find { it.players.contains(this) }
                if (teamSearched != null) {
                    freshCache = false
                    playerTeamCache[this.uniqueId] = teamSearched
                    return teamSearched
                }

                return null
            }

        private val fireworkBase
            get() = FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withFlicker()
                .withTrail()

        fun Firework.teamEffect(team: ConfigTeam) {
            val meta = this.fireworkMeta
            meta.addEffect(fireworkBase.withColor(team.material.chatColor).build())
            meta.power = 1
            this.fireworkMeta = meta
        }
    }

    val players = mutableListOf<Player>()
    val bossBar = Bukkit.createBossBar("Punkte von $displayName", material.barColor, BarStyle.SOLID)

    val name
        get() = material.name

    val spawnPosInGame
        get() = spawnPos?.let { it.world = Game.arenaWorld; it }

    fun addPlayer(p: Player) {
        players.add(p)
        bossBar.addPlayer(p)
    }

    fun addSpectator(p: Player) {
        bossBar.addPlayer(p)
    }

    fun removePlayer(p: Player) {
        players.remove(p)
        bossBar.removePlayer(p)
    }

    fun reset() {
        players.clear()
        bossBar.removeAll()
        if (!freshCache) resetCache()
    }

    override fun serialize(): MutableMap<String, Any?> {
        spawnPos?.world = null
        return mutableMapOf(
            "material" to material.name,
            "displayName" to displayName,
            "spawn" to spawnPos,
        )
    }

    constructor(cfg: Map<String, Any?>) : this (
        (cfg["material"] as? String)?.let { try {
            enumValueOf<IncMaterial>(it)
        } catch (_: IllegalArgumentException) { null } } ?: IncMaterial.BLUE,
        cfg["displayName"] as? String ?: "team",
        cfg["spawn"] as? Location,
    )
}