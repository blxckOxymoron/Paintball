package de.crightgames.blxckoxymoron.paintball.projectile

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.EntityType


class ProjectileType (
    val speed: Double,
    val gravity: Double,
    val effects: List<Pair<ProjectileEffect, Int>>, // Effect and strength
    val particle: ProjectileParticle,
    val entity: EntityType? = null,
) : ConfigurationSerializable {

    companion object {
        val GRAVITY = -9.8065 // blocks per 20 Ticks

        fun parseEffects(cfg: Any?): List<Pair<ProjectileEffect, Int>> {
            return (cfg as? List<*>)?.filterIsInstance(String::class.java)?.mapNotNull {

                val (typeStr, strengthStr) = it.split(":")

                val type = kotlin.runCatching {
                    enumValueOf<ProjectileEffect>(typeStr.uppercase())
                }.getOrNull()
                val strength = strengthStr.toIntOrNull()

                if (type == null || strength == null) return@mapNotNull null
                return@mapNotNull type to strength
            } ?: emptyList()
        }

        fun serializeEffects(effects: List<Pair<ProjectileEffect, Int>>): List<String> {
            return effects.map { (eff, st) ->
                return@map eff.name + ":" + st
            }
        }
    }

    constructor(cfg: Map<String, Any>): this(
        (cfg["speed"] as? Double) ?: 1.0,
        (cfg["gravity"] as? Double) ?: GRAVITY,
        parseEffects(cfg["effects"]),

        (cfg["particle"] as? String)?.let {
            kotlin.runCatching {
                enumValueOf<ProjectileParticle>(it)
            }.getOrNull()
        } ?: ProjectileParticle.NONE,

        (cfg["entity"] as? String)?.let {
            kotlin.runCatching {
                enumValueOf<EntityType>(it)
            }.getOrNull()
        }
    )

    override fun serialize(): MutableMap<String, Any?> {
        return mutableMapOf(
            "speed" to speed,
            "gravity" to gravity,
            "effects" to serializeEffects(effects),
            "particle" to particle.name,
            "entity" to entity?.name
        )
    }

}