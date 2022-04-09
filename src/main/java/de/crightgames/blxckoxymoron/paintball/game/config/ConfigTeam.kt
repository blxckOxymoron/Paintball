package de.crightgames.blxckoxymoron.paintball.game.config

import de.crightgames.blxckoxymoron.paintball.game.IncMaterial
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player

class ConfigTeam (val material: IncMaterial, val displayName: String, var spawnPos: Location?) : ConfigurationSerializable {

    val players = mutableListOf<Player>()

    override fun serialize(): MutableMap<String, Any?> {
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