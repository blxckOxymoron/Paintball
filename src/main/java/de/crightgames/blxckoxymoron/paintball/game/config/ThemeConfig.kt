package de.crightgames.blxckoxymoron.paintball.game.config

import org.bukkit.ChatColor
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.ConfigurationSerialization

class ThemeConfig(cfg: Map<String, Any> = mutableMapOf()) : ConfigurationSerializable {

    companion object {
        fun registerConfigClasses() {
            ConfigurationSerialization.registerClass(ThemeConfig::class.java)
        }
    }

    val default: ChatColor
    val highlight: ChatColor
    val secondary: ChatColor

    init {
        default = try {
            enumValueOf((cfg["default"] as? String) ?: "RESET")
        } catch (_: IllegalArgumentException) { ChatColor.RESET }
        highlight = try {
            enumValueOf((cfg["highlight"] as? String) ?: "AQUA")
        } catch (_: IllegalArgumentException) { ChatColor.RESET }
        secondary = try {
            enumValueOf((cfg["highlight"] as? String) ?: "GRAY")
        } catch (_: IllegalArgumentException) { ChatColor.RESET }
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            "default" to default.name,
            "highlight" to highlight.name,
            "secondary" to secondary.name,
        )
    }
}