package de.crightgames.blxckoxymoron.paintball.util

import de.crightgames.blxckoxymoron.paintball.config.ThemeConfig
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.serialization.ConfigurationSerializable
import kotlin.math.ceil
import kotlin.math.floor

object ThemeBuilder {

    var DEFAULT = ChatColor.RESET
    var HIGHLIGHT = ChatColor.AQUA
    var SECONDARY = ChatColor.GRAY

    fun loadConfig(cfg: ThemeConfig) {
        DEFAULT = cfg.default.asBungee()
        HIGHLIGHT = cfg.highlight.asBungee()
        SECONDARY = cfg.secondary.asBungee()
    }

    /**
     * @param text *highlighted* `secondary` default :RED:this appears red::
     */
    fun CommandSender.sendThemedMessage(text: String, padding: Double) = this.sendMessage(themed(text, padding))
    fun CommandSender.sendThemedMessage(text: String, padding: Int = 0) = this.sendMessage(themed(text, padding.toDouble()))

    /**
     * @param text *highlighted* `secondary` default :RED:this appears red::
     */
    fun themed(text: String, padding: Int = 0): String = themed(text, padding.toDouble())

    /**
     * @param text *highlighted* `secondary` default :RED:this appears red::
     */
    fun themed(text: String, padding: Double): String {
        var highlight = false
        var secondary = false
        return "\n".repeat(ceil(padding).toInt()) +
            DEFAULT.toString() + text
            .replace(Regex("(?<!\\\\)\\*")) {
                highlight = !highlight
                if (highlight) HIGHLIGHT.toString() else DEFAULT.toString()
            }
            .replace(Regex("(?<!\\\\)`")) {
                secondary = !secondary
                if (secondary) SECONDARY.toString() else DEFAULT.toString()
            }
            .replace(Regex("(?<!\\\\):[A-Z_]+:")) {
                try {
                    enumValueOf<org.bukkit.ChatColor>(it.value.replace(":", "")).toString()
                } catch (e: IllegalArgumentException) {
                    "?¿"
                }
            }
            .replace(Regex("(?<!\\\\)::"), DEFAULT.toString())
            .replace(Regex("(?<!\\\\)\\\\(?!\\\\+)"), "") +
            "\n".repeat(floor(padding).toInt())
    }

    fun serializableThemed(obj: ConfigurationSerializable): String {
        return obj.serialize().map { entry ->
            themed(
                "*${entry.key}*: " +
                    when (entry.value) {
                        is ConfigurationSerializable -> "`" + (entry.value as ConfigurationSerializable).serialize().toString() + "`"
                        is List<*> -> "`[\n" +
                            (entry.value as List<*>).filterIsInstance(ConfigurationSerializable::class.java)
                                .joinToString(",\n") { it.serialize().toString() } +
                            "]`"
                        else -> entry.value.toString()
                    }
            )
        }.joinToString("\n")
    }
}