package de.crightgames.blxckoxymoron.paintball.game.config

import org.bukkit.configuration.serialization.ConfigurationSerializable
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ConfigDuration : ConfigurationSerializable {

    val duration: Duration

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            "duration"  to duration.inWholeMilliseconds,
            "unit"      to DurationUnit.MILLISECONDS.name
        )
    }

    constructor(keys: Map<String, Any>) {
        val unit = try {
            enumValueOf(
                keys["unit"].toString()
            )
        } catch (_: IllegalArgumentException) {
            DurationUnit.MILLISECONDS
        }
        val dur = keys["duration"] as? Int ?: 0
        duration = dur.toDuration(unit)
    }

    constructor(dur: Duration) {
        duration = dur
    }
}