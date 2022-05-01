package de.crightgames.blxckoxymoron.paintball

import de.crightgames.blxckoxymoron.paintball.commands.PaintballCommand
import de.crightgames.blxckoxymoron.paintball.game.*
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigDuration
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigTeam
import de.crightgames.blxckoxymoron.paintball.game.config.GameConfig
import de.crightgames.blxckoxymoron.paintball.game.config.ThemeConfig
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballDrop
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballHitBlock
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballHitPlayer
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballUse
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.DurationUnit

class Paintball : JavaPlugin() {

    companion object {
        lateinit var INSTANCE: Paintball

        lateinit var gameConfig: GameConfig
        private lateinit var themeConfig: ThemeConfig

        val lastShot = mutableMapOf<UUID, Long>()
        val lastDeath = mutableMapOf<UUID, Long>()

        inline val Duration.inWholeTicks: Long
            get()  = floor(toDouble(DurationUnit.MILLISECONDS) / 50).toLong()

    }

    fun resetConfig(alsoTheme: Boolean = true) {
        gameConfig = ConfigKey.GAME.default(this.config)
        if (alsoTheme) themeConfig = ConfigKey.THEME.default(this.config)

        saveConfig()
    }

    override fun saveConfig() {
        ConfigKey.GAME.set(this.config, gameConfig)
        ConfigKey.THEME.set(this.config, themeConfig)
        super.saveConfig()
    }

    override fun reloadConfig() {
        super.reloadConfig()
        gameConfig = ConfigKey.GAME.get(this.config)
        themeConfig = ConfigKey.THEME.get(this.config)
    }

    override fun onEnable() {
        // Plugin startup logic
        INSTANCE = this

        ConfigurationSerialization.registerClass(ConfigDuration::class.java, "crg.Duration")
        ConfigurationSerialization.registerClass(ConfigTeam::class.java, "crg.Team")
        ConfigurationSerialization.registerClass(GameConfig::class.java, "crg.Game")
        ConfigurationSerialization.registerClass(ThemeConfig::class.java, "crg.Theme")

        // config
        reloadConfig()

        // commands
        PaintballCommand().register(this)

        // listeners
        val pm = Bukkit.getPluginManager()
        pm.registerEvents(PlayerJoinLeave(), this)

        pm.registerEvents(NoPlayerDamage(), this)
        pm.registerEvents(NoHunger(), this)
        pm.registerEvents(NoOpenContainer(), this)

        pm.registerEvents(SnowballUse(), this)
        pm.registerEvents(SnowballHitBlock(), this)
        pm.registerEvents(SnowballHitPlayer(), this)
        pm.registerEvents(SnowballDrop(), this)

        Game.setupNewArenaWorld()
        Scores.createAndResetScores()
    }

    override fun onDisable() {
    }

    private class ConfigKey <T : ConfigurationSerializable> (val path: String, val clazz: Class<T>, val create: () -> T) {
        companion object {
            val GAME = ConfigKey("game", GameConfig::class.java) { GameConfig() }
            val THEME = ConfigKey("theme", ThemeConfig::class.java) { ThemeConfig() }
        }

        fun get(cfg: Configuration): T {
            return cfg.getSerializable(path, clazz, create()) as T
        }
        fun set(cfg: Configuration, updated: T) {
            cfg.set(path, updated)
        }
        fun default(cfg: Configuration): T {
            val newInstance = create()
            cfg.set(path, newInstance)
            return newInstance
        }
    }
}