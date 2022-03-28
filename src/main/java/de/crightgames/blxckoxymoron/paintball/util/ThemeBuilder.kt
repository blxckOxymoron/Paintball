package de.crightgames.blxckoxymoron.paintball.util

import net.md_5.bungee.api.ChatColor
import org.bukkit.configuration.serialization.ConfigurationSerializable

object ThemeBuilder {

        val DEFAULT = ChatColor.RESET
        val HIGHLIGHT = ChatColor.RED
        val SECONDARY = ChatColor.GRAY

        /**
         * @param text *highlighted* `secondary` default :RED:this appears red::
         *
         */
        fun themed(text: String): String {
            var highlight = false
            var secondary = false
            return DEFAULT.toString() + text
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
                .replace(Regex("(?<!\\\\)\\\\(?!\\\\+)"), "")
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