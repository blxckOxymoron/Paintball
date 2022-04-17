package de.crightgames.blxckoxymoron.paintball.game.config

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.game.IncMaterial
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.boss.BarStyle
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player

class ConfigTeam (val material: IncMaterial, val displayName: String, var spawnPos: Location?) : ConfigurationSerializable {

    companion object {
        val Player.team
            get() = Paintball.gameConfig.teams.find { it.players.contains(this) }
    }

    val players = mutableListOf<Player>()
    val bossBar = Bukkit.createBossBar("Punkte von $displayName", material.barColor, BarStyle.SOLID)

    val name
        get() = material.name

    fun addPlayer(p: Player) {
        players.add(p)
        bossBar.addPlayer(p)
    }

    fun removePlayer(p: Player) {
        players.remove(p)
        bossBar.removePlayer(p)
    }

    fun reset() {
        players.clear()
        bossBar.removeAll()
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
    ) {
        Bukkit.getLogger().info("spawn location for team $name is ${if (spawnPos == null) "null" else "set"}")
    }
}