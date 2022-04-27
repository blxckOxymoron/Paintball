package de.crightgames.blxckoxymoron.paintball.game

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.Paintball.Companion.inWholeTicks
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigTeam
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigTeam.Companion.team
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigTeam.Companion.teamEffect
import de.crightgames.blxckoxymoron.paintball.util.EmptyWorldGen
import de.crightgames.blxckoxymoron.paintball.util.PlayerHitHandler
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder.sendThemedMessage
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.entity.ThrowableProjectile
import org.bukkit.event.entity.EntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Objective
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object Game {

    fun checkProjectileEvent(e: EntityEvent): Pair<Player, ConfigTeam>? {
        val ent = e.entity as? ThrowableProjectile ?: return null

        val item = (e.entity as? ThrowableProjectile)?.item
        if (item == null || !item.isSimilar(projectileItem)) return null

        val shooter = ent.shooter as? Player ?: return null

        return shooter to (shooter.team ?: return null)
    }

    val projectileItem = ItemStack(if (Paintball.gameConfig.easterMode) Material.EGG else Material.SNOWBALL )
    init {
        projectileItem.addUnsafeEnchantment(Enchantment.CHANNELING, 1)
        projectileItem.itemMeta = projectileItem.itemMeta.let {
            it?.setCustomModelData(112201)
            it
        }
    }
    private val snowballStack = projectileItem.clone()
    init { snowballStack.amount = 16 }

    var currentBiggestTeamSize = 1

    private var time = Duration.ZERO
    private var gameLoopTask: BukkitTask? = null

    val spectators = mutableListOf<UUID>()

    var state = GameState.WAITING

    var arenaWorld: World? = null

    fun setupNewArenaWorld() {
        // https://www.spigotmc.org/threads/world-copy-and-load.316248/

        thread {
            if (Paintball.gameConfig.lastArenaName.isNotBlank()) {
                val worldFolder = File(Bukkit.getWorldContainer(), Paintball.gameConfig.lastArenaName)
                if (worldFolder.exists() && worldFolder.isDirectory) {
                    Bukkit.getLogger().info("deleting old arena world")
                    worldFolder.deleteRecursively()
                }
            }

            val from = File(Bukkit.getWorldContainer().absolutePath, Paintball.gameConfig.arenaWorldName)
            if (!from.exists()) return@thread Bukkit.getLogger().warning(
                "Arena world doesn't exist. Create one with `/paintball arena create` and restart."
            )

            val randomWorldName = "arena-tmp-" + UUID.randomUUID().mostSignificantBits.absoluteValue.toString(16)
            val to = File(Bukkit.getWorldContainer().absolutePath, randomWorldName)
            if (to.exists()) to.delete()

            val success = try {
                from.copyRecursively(to) { a, x ->
                    Bukkit.getLogger().warning("${a.name} - ${x.message}")
                    OnErrorAction.SKIP
                }
            } catch (e: IOException) {
                Bukkit.getLogger().warning("we got an IOException while copying - ignoring it")
                true
            }
            if (!success) return@thread Bukkit.getLogger().warning("Couldn't copy the world.")

            File(to.absolutePath, "uid.dat").delete()

            Bukkit.getScheduler().runTask(Paintball.INSTANCE, Runnable {
                arenaWorld = Bukkit.createWorld(WorldCreator(randomWorldName).generator(EmptyWorldGen()))
                arenaWorld?.isAutoSave = false

                arenaWorld?.spawnLocation?.let { spawn ->
                    Bukkit.getOnlinePlayers().forEach {
                        it.teleport(spawn)
                    }
                }

                if (Paintball.gameConfig.lastArenaName.isNotBlank())
                    Bukkit.unloadWorld(Paintball.gameConfig.lastArenaName, false)

                Paintball.gameConfig.lastArenaName = randomWorldName
                Paintball.gameConfig.save()
            })
        }
    }

    fun restart() {
        Bukkit.broadcastMessage(ThemeBuilder.themed(
            "Die Arena wurde für eine neue Runde zurückgesetzt."
        ))
        state = GameState.WAITING
        setupNewArenaWorld()
        Bukkit.getOnlinePlayers().forEach {
            it.gameMode = GameMode.SPECTATOR
            it.inventory.clear()
        }
        Paintball.gameConfig.teams.forEach { team ->
            team.reset()
        }

        PlayerHitHandler.resetDamage()
        Scores.createAndResetScores()
        Countdown.checkAndStart()
    }

    fun start() {
        if (state != GameState.WAITING) return
        state = GameState.RUNNING
        time = Duration.ZERO

        val allPlayers = Bukkit.getOnlinePlayers().toMutableList()
        allPlayers.forEach { it.gameMode = GameMode.ADVENTURE }

        currentBiggestTeamSize = ceil(allPlayers.size.toDouble() / 2).toInt()

        val teamCount = Paintball.gameConfig.teams.size
        allPlayers.shuffled().forEachIndexed { i, p ->
            Paintball.gameConfig.teams[i % teamCount].addPlayer(p)
        }

        Paintball.gameConfig.teams.forEach { team ->
            val spawnLocation = team.spawnPosInGame ?: return@forEach run {
                Bukkit.broadcastMessage(ThemeBuilder.themed(
                ":RED:Can't teleport players of team ::${team.displayName}\n" +
                    "Please set a spawnpoint and start again"
                ))
            }
            team.players.forEach { pl ->

                pl.sendThemedMessage(
                    "*Paintball*: Benutze ${if (Paintball.gameConfig.easterMode) "das Osterei" else "den Schneeball"}, um Blöcke einzufärben und " +
                        "Gegner abzuschießen!" +
                        "\nDas Team, das *am Ende die größte Fläche* eingefärbt hat, gewinnt!" +
                        "\nViel Erfolg!",
                    1
                )

                pl.teleport(spawnLocation.clone().add(0.5, 0.0, 0.5))
                pl.inventory.heldItemSlot = 0
                pl.inventory.setItemInMainHand(snowballStack)

                pl.playSound(pl.location, Sound.BLOCK_NOTE_BLOCK_FLUTE, SoundCategory.MASTER, 100F, 1F)

                PlayerHitHandler(pl, team, team).updateDamage(0)
            }

        }

        gameLoopTask = Bukkit.getScheduler().runTaskTimer(
            Paintball.INSTANCE,
            gameLoop,
            Paintball.gameConfig.durations["gameLoop"]!!.inWholeTicks,
            Paintball.gameConfig.durations["gameLoop"]!!.inWholeTicks
        )
    }

    fun respawnPlayer(player: Player, resolvedTeam: ConfigTeam? = null) {
        val team = resolvedTeam ?: Paintball.gameConfig.teams.find { it.players.contains(player) } ?: return
        player.gameMode = GameMode.ADVENTURE

        player.teleport(team.spawnPos ?: return)
    }

    private val gameLoop = Runnable {
        time += Paintball.gameConfig.durations["gameLoop"]!!

        /**
         * display:
         * - timer
         * - kill cooldown
         * - stats
         * - boss bar (how much color)
         */

        val timerMessage = ThemeBuilder.themed(
            "Timer: *${formatTimer(Paintball.gameConfig.durations["game"]!! - time)}*"
        )

        Bukkit.getOnlinePlayers().forEach { pl ->
            val actionBarMessage = StringBuilder()

            actionBarMessage.append(timerMessage)

            val ttRespawn = getTimeToRespawn(pl)
            if (ttRespawn > Duration.ZERO) actionBarMessage.append(ThemeBuilder.themed(
                " Respawn: *${ttRespawn.inWholeSeconds + 1}*"
            ))

            pl.spigot().sendMessage(
                ChatMessageType.ACTION_BAR, TextComponent(actionBarMessage.toString())
            )
        }
        val teamColored = Paintball.gameConfig.teams.map {
            Scores.coloredObj?.getScore(it.name)?.score ?: 0
        }
        val totalColored = teamColored.sum().coerceAtLeast(1)

        Paintball.gameConfig.teams.forEachIndexed { i, team ->
            team.bossBar.progress =
                (teamColored[i].toDouble() / totalColored)
                    .coerceAtLeast(0.0)
                    .coerceAtMost(1.0)
        }

        if (time >= Paintball.gameConfig.durations["game"]!!) end()
    }

    private fun end() {
        state = GameState.ENDED
        gameLoopTask?.cancel()

        Paintball.gameConfig.teams.forEach{ team ->
            team.players.forEach { player ->
                if (player.gameMode == GameMode.SPECTATOR) respawnPlayer(player, team)
            }
        }

        Bukkit.getWorlds().first().getEntitiesByClass(ThrowableProjectile::class.java).forEach {
            if (!it.item.isSimilar(projectileItem)) return@forEach
            it.world.spawnParticle(Particle.FIREWORKS_SPARK, it.location, 2, 0.1, 0.1, 0.1, 0.0)
            it.remove()
        }

        val winnerTeams = Paintball.gameConfig.teams
            .groupBy { Scores.coloredObj?.getScore(it.name)?.score ?: 0 }
            .maxByOrNull { it.key }?.value ?: return Bukkit.getLogger().warning("Something's off with the scoreboard")

        val playerScoreColored = Scores.coloredIndividualObj
            ?: return Bukkit.getLogger().warning("No scoreboard for player colored count")

        val playerScoreKills = Scores.killsObj
            ?: return Bukkit.getLogger().warning("No scoreboard for player kills")


        Bukkit.getOnlinePlayers().forEach { pl ->
            pl.sendTitle(
                winnerTeams.joinToString(" & ") { it.displayName },
                "hat gewonnen!",
                2,
                1.minutes.inWholeTicks.toInt(),
                5
            )
        }

        winnerTeams.forEach { team ->
            team.players.forEach { pl ->
                val firework = pl.world.spawnEntity(pl.location, EntityType.FIREWORK) as Firework
                firework.teamEffect(team)
                pl.playSound(pl.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 100F, 1F)
            }
        }

        Bukkit.broadcastMessage(ThemeBuilder.themed(
            Paintball.gameConfig.teams.joinToString("\n") {
                (if (winnerTeams.contains(it)) ":GOLD:★:: " else "   ") +
                    "*${it.displayName}*: ${Scores.coloredObj?.getScore(it.name)?.score ?: "?"}"

            },
            .5
        ))

        Bukkit.getScheduler().runTaskLater(Paintball.INSTANCE, Runnable {
            Bukkit.broadcastMessage(ThemeBuilder.themed(
                "Am meisten eingefärbt:\n" +
                    topPlayers(playerScoreColored),
                .5
            ))
        }, 3.seconds.inWholeTicks)

        Bukkit.getScheduler().runTaskLater(Paintball.INSTANCE, Runnable {
            Bukkit.broadcastMessage(ThemeBuilder.themed(
                "Am meisten Kills:\n" +
                    topPlayers(playerScoreKills),
                .5
            ))
        }, 6.seconds.inWholeTicks)

        Bukkit.getScheduler().runTaskLater(Paintball.INSTANCE, Runnable {
            Bukkit.getOnlinePlayers().forEach {
                it.sendThemedMessage(
                    "Deine persönlichen Statistiken:\n" +
                        playerStatistics(it),
                    .5
                )
            }
        }, 9.seconds.inWholeTicks)

        Bukkit.getScheduler().runTaskLater(Paintball.INSTANCE, Runnable {
            val restartDur = Paintball.gameConfig.durations["restart"]!!
            if (restartDur >= Duration.ZERO) {
                Bukkit.broadcastMessage(ThemeBuilder.themed(
                    "Eine neue Runde startet in *${restartDur.inWholeSeconds}*s",
                    .5
                ))
                Bukkit.getScheduler().runTaskLater(Paintball.INSTANCE, Runnable {
                    restart()
                }, restartDur.inWholeTicks)
            }
        }, 10.seconds.inWholeTicks)
    }

    private fun playerStatistics(p: Player): String {

        val statistics = mapOf(
            "Kills" to (Scores.killsObj?.getScore(p.name)?.score ?: -1),
            "Tode" to (Scores.deathsObj?.getScore(p.name)?.score ?: -1),
            "Schüsse" to (Scores.shotsObj?.getScore(p.name)?.score ?: -1),
            "Fläche" to (Scores.coloredIndividualObj?.getScore(p.name)?.score ?: -1),
        )

        return statistics.entries.joinToString("\n") {
            "*${it.key}*: ${it.value}"
        }

    }

    private fun topPlayers(obj: Objective): String {
        return Bukkit.getOnlinePlayers()
            .groupBy { obj.getScore(it.name).score }.entries
            .asSequence()
            .filter { it.key > 0 }
            .sortedByDescending { it.key }
            .filterIndexed { index, _ -> index < 5 }
            .mapIndexed { i, en -> en.value.joinToString("\n") { "${i + 1}. *${it.name}*: ${en.key}" } }
            .joinToString("\n")
    }

    private fun getTimeToRespawn(p: Player): Duration {
        val timeToRespawn = Paintball.lastDeath[p.uniqueId] ?: return Duration.ZERO
        val timeLong =
            (timeToRespawn + Paintball.gameConfig.durations["respawn"]!!.inWholeMilliseconds) - System.currentTimeMillis()
        return timeLong.milliseconds
    }

    private fun formatTimer(c: Duration): String {
        c.toComponents { m, s, _ ->
            return "$m:${if (s.toString().length == 1) "0$s" else s}"
        }
    }

    enum class GameState {
        WAITING, RUNNING, ENDED;
    }
}