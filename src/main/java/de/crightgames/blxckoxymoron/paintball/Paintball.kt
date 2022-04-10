package de.crightgames.blxckoxymoron.paintball

import de.crightgames.blxckoxymoron.paintball.commands.PaintballCommand
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.game.NoPlayerDamage
import de.crightgames.blxckoxymoron.paintball.game.PlayerJoinLeave
import de.crightgames.blxckoxymoron.paintball.game.Scores
import de.crightgames.blxckoxymoron.paintball.game.config.GameConfig
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballDrop
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballHitBlock
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballHitPlayer
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballUse
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.DurationUnit

class Paintball : JavaPlugin() {

    companion object {
        lateinit var INSTANCE: Paintball

        lateinit var gameConfig: GameConfig

        val lastShot = mutableMapOf<UUID, Long>()
        val lastKill = mutableMapOf<UUID, Long>()

        inline val Duration.inWholeTicks: Long
            get()  = floor(toDouble(DurationUnit.MILLISECONDS) / 50).toLong()

    }

    override fun onEnable() {
        // Plugin startup logic
        INSTANCE = this

        // config
        GameConfig.registerConfigClasses()
        gameConfig = config.get("game") as? GameConfig ?: GameConfig()

        // commands
        PaintballCommand().register(this)

        // listeners
        val pm = Bukkit.getPluginManager()
        pm.registerEvents(PlayerJoinLeave(), this)
        pm.registerEvents(NoPlayerDamage(), this)

        pm.registerEvents(SnowballUse(), this)
        pm.registerEvents(SnowballHitBlock(), this)
        pm.registerEvents(SnowballHitPlayer(), this)
        pm.registerEvents(SnowballDrop(), this)

        Game.setupNewArenaWorld(true)
        Scores.createAndResetScores()
    }

    override fun onDisable() {
        // Plugin shutdown logic

        // config
        config.set("game", gameConfig)
        saveConfig()
    }
}