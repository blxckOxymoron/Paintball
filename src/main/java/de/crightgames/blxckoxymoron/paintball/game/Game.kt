package de.crightgames.blxckoxymoron.paintball.game

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.Paintball.Companion.inWholeTicks
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigTeam
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballHitPlayer.Companion.fizzleOut
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Objective
import kotlin.math.ceil
import kotlin.time.Duration

object Game {

    val snowballItem = ItemStack(Material.SNOWBALL)
    init { snowballItem.addUnsafeEnchantment(Enchantment.CHANNELING, 1) }
    private val snowballStack = snowballItem.clone()
    init { snowballStack.amount = 16 }

    private var time = Duration.ZERO
    private var gameLoopTask: BukkitTask? = null

    private var started = false
    fun start() {
        if (started) return
        started = true

        Scores.createAndResetScores()

        val allPlayers = Bukkit.getOnlinePlayers().toMutableList()
        allPlayers.forEach { it.gameMode = GameMode.ADVENTURE }

        val teamCount = Paintball.gameConfig.teams.size
        allPlayers.shuffled().forEachIndexed { i, p ->
            Paintball.gameConfig.teams[i % teamCount].players.add(p)
        }

        Paintball.gameConfig.teams.forEach { team ->
            val spawnLocation = team.spawnPos ?: return@forEach run {
                Bukkit.broadcastMessage(ThemeBuilder.themed(
                ":RED:Can't teleport players of team ${team.displayName}!::\n" +
                    "Please set a spawnpoint and start again"
                ))
            }
            team.players.forEach {
                it.teleport(spawnLocation.clone().add(0.5, 0.0, 0.5))
                it.inventory.heldItemSlot = 0
                it.inventory.setItemInMainHand(snowballStack)
            }

        }

        Bukkit.broadcastMessage(ThemeBuilder.themed(
            "Teams:\n" +
            Paintball.gameConfig.teams.joinToString("\n") { team ->
                "${team.displayName}:\n" + team.players.joinToString("\n") { pl ->
                    "• " + pl.name
                }
            }, 1
        ))

        gameLoopTask = Bukkit.getScheduler().runTaskTimer(
            Paintball.INSTANCE,
            gameLoop,
            Paintball.gameConfig.durations["gameLoop"]!!.inWholeTicks,
            Paintball.gameConfig.durations["gameLoop"]!!.inWholeTicks
        )
    }

    fun respawnPlayer(player: Player, resolvedTeam: ConfigTeam?) {
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

            val ttNextShot = getTimeToNextShot(pl)
            if (ttNextShot > 0) actionBarMessage.append(ThemeBuilder.themed(
                " Cooldown: *${ceil(ttNextShot.toDouble() / 1000).toInt()}*"
            ))

            pl.spigot().sendMessage(
                ChatMessageType.ACTION_BAR, TextComponent(actionBarMessage.toString())
            )
        }

        if (time >= Paintball.gameConfig.durations["game"]!!) {
            // Game ended
            gameLoopTask?.cancel()

            Paintball.gameConfig.teams.forEach{ team ->
                team.players.forEach { player ->
                    if (player.gameMode == GameMode.SPECTATOR) respawnPlayer(player, team)
                }
            }

            Bukkit.getWorlds().first().getEntitiesByClass(Snowball::class.java).forEach {
                if (!it.item.isSimilar(snowballItem)) return@forEach
                it.fizzleOut()
            }

            val winnerTeam = enumValues<IncMaterial>().maxByOrNull {
                Scores.coloredObj?.getScore(it.name)?.score ?: 0
            } ?: return@Runnable Bukkit.getLogger().warning("No teams")

            val playerScoreColored = Scores.coloredIndividualObj
                ?: return@Runnable Bukkit.getLogger().warning("No scoreboard for player colored count")

            val playerScoreKills = Scores.killsObj
                ?: return@Runnable Bukkit.getLogger().warning("No scoreboard for player kills")


            Bukkit.broadcastMessage(ThemeBuilder.themed(
                "\n" +
                    "Am meisten *eingefärbt*:\n" +
                    topPlayers(playerScoreColored) + "\n\n" +
                    "Am meisten *Kills*:\n" +
                    topPlayers(playerScoreKills)
            ))

            Bukkit.getOnlinePlayers().forEach {
                it.sendTitle(winnerTeam.displayName, "hat gewonnen!", 2, 800, 5)
                it.sendMessage(ThemeBuilder.themed(
                    "Deine persönlichen Statistiken:\n" +
                        playerStatistics(it)
                ))
            }

        }
    }

    private fun playerStatistics(p: Player): String {

        val statistics = mapOf(
            "Kills" to (Scores.killsObj?.getScore(p.name)?.score ?: -1),
            "Tode" to (Scores.deathsObj?.getScore(p.name)?.score ?: -1),
            "Schüsse" to (Scores.shotsObj?.getScore(p.name)?.score ?: -1),
            "Fläche" to (Scores.coloredIndividualObj?.getScore(p.name)?.score ?: -1),
        )

        return statistics.entries.joinToString("\n") {
            "*${it.key}*: `${it.value}`"
        }

    }

    private fun topPlayers(obj: Objective): String {

        return Bukkit.getOnlinePlayers().asSequence().map {
            it.name to obj.getScore(it.name).score
        }.filter { it.second != 0 }.sortedByDescending { it.second }.filterIndexed { index, _ -> index < 5 }
            .joinToString("\n") {
                "*${it.first}*: *${it.second}*"
             }
    }

    private fun getTimeToNextShot(p: Player): Int {
        val timeToNextShot = Paintball.lastKill[p.uniqueId] ?: return -1
        val timeLong =
            (timeToNextShot + Paintball.gameConfig.durations["kill"]!!.inWholeMilliseconds) - System.currentTimeMillis()
        return timeLong.toInt()
    }

    private fun formatTimer(c: Duration): String {
        c.toComponents { m, s, _ ->
            return "$m:${if (s.toString().length == 1) "0$s" else s}"
        }
    }
}