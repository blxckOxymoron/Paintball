package de.crightgames.blxckoxymoron.paintball.game.config

import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.ChatColor
import org.bukkit.configuration.serialization.ConfigurationSerializable

class ThemeConfig(cfg: Map<String, Any> = mutableMapOf()) : ConfigurationSerializable {

    val default: ChatColor
    val highlight: ChatColor
    val secondary: ChatColor

    init {
        default = try {
            enumValueOf((cfg["default"] as? String) ?: "RESET")
        } catch (_: IllegalArgumentException) { ChatColor.RESET }
        highlight = try {
            enumValueOf((cfg["highlight"] as? String) ?: "AQUA")
        } catch (_: IllegalArgumentException) { ChatColor.AQUA }
        secondary = try {
            enumValueOf((cfg["secondary"] as? String) ?: "GRAY")
        } catch (_: IllegalArgumentException) { ChatColor.GRAY }
        ThemeBuilder.loadConfig(this)
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            "default" to default.name,
            "highlight" to highlight.name,
            "secondary" to secondary.name,
        )
    }
}