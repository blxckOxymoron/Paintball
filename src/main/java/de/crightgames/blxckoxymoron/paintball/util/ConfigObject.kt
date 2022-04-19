package de.crightgames.blxckoxymoron.paintball.util

import de.crightgames.blxckoxymoron.paintball.Paintball
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.ConfigurationSerialization

abstract class ConfigObject<T : ConfigObject<T>> (private val key: String): ConfigurationSerializable {
    companion object {
        private var wasRegistered = mutableSetOf<String>()
    }

    private fun registerConfigClasses() {
        if (wasRegistered.contains(this::class.qualifiedName)) return

        ConfigurationSerialization.registerClass(this::class.java)
        additionalConfigClasses.forEach(ConfigurationSerialization::registerClass)

        this::class.qualifiedName?.let { wasRegistered.add(it) }
    }

    open val additionalConfigClasses: List<Class<out ConfigurationSerializable>> = emptyList()

    fun load(): T {
        registerConfigClasses()
        return (Paintball.INSTANCE.config.get(key) as? T ?: this as T)
    }

    fun save() {
        Paintball.INSTANCE.config.set(key, this)
        Paintball.INSTANCE.saveConfig()
    }
}
