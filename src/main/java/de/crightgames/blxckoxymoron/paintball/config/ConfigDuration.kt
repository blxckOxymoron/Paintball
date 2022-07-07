package de.crightgames.blxckoxymoron.paintball.config

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@SerializableAs("Duration")
class ConfigDuration : ConfigurationSerializable {

    val duration: Duration

    override fun serialize(): MutableMap<String, Any> {

        duration.toComponents { _, minutes, seconds, nanoseconds ->
            return if (nanoseconds != 0) mutableMapOf(
                "duration"  to duration.inWholeMilliseconds,
                "unit"      to DurationUnit.MILLISECONDS.name
            ) else if (seconds != 0) mutableMapOf(
                "duration"  to duration.inWholeSeconds,
                "unit"      to DurationUnit.SECONDS.name
            ) else if (minutes != 0) mutableMapOf(
                "duration"  to duration.inWholeMinutes,
                "unit"      to DurationUnit.MINUTES.name
            ) else mutableMapOf(
                "duration"  to duration.inWholeHours,
                "unit"      to DurationUnit.HOURS.name
            )
        }
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