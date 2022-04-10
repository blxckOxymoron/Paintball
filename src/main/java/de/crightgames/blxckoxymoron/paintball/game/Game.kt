package de.crightgames.blxckoxymoron.paintball.game

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.Paintball.Companion.inWholeTicks
import de.crightgames.blxckoxymoron.paintball.game.config.ConfigTeam
import de.crightgames.blxckoxymoron.paintball.game.projectile.SnowballHitPlayer.Companion.fizzleOut
import de.crightgames.blxckoxymoron.paintball.util.EmptyWorldGen
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
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

    val snowballItem = ItemStack(Material.SNOWBALL)
    init {
        snowballItem.addUnsafeEnchantment(Enchantment.CHANNELING, 1)
        snowballItem.itemMeta = snowballItem.itemMeta.let {
            it?.setCustomModelData(112201)
            it
        }
    }
    private val snowballStack = snowballItem.clone()
    init { snowballStack.amount = 16 }
    private val winnerFirework = FireworkEffect.builder().withTrail()
        .with(FireworkEffect.Type.BALL_LARGE)
        .withFlicker()

    var maxPlayersInTeam = 1

    private var time = Duration.ZERO
    private var gameLoopTask: BukkitTask? = null

    var state = GameState.WAITING

    var arenaWorld: World? = null

    fun setupNewArenaWorld(deleteOldWorlds: Boolean = false) {
        // https://www.spigotmc.org/threads/world-copy-and-load.316248/

        thread {
            if (deleteOldWorlds) {
                val worldFolders =
                    Bukkit.getWorldContainer().listFiles { file -> Regex("arena-tmp-\\w+").matches(file.name) }
                        ?: return@thread
                Bukkit.getLogger().info("deleting ${worldFolders.size} old arena worlds")
                worldFolders.forEach {
                    Bukkit.unloadWorld(it.name, false)
                    it.deleteRecursively()
                }
            }

            val from = File(Bukkit.getWorldContainer().absolutePath, Paintball.gameConfig.arenaWorldName)
            if (!from.exists()) return@thread Bukkit.getLogger().warning(
                "Arena world doesn't exist. Create one with `/paintball arena create` and restart.s"
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

            val prevWorld = arenaWorld
            Bukkit.getScheduler().runTask(Paintball.INSTANCE, Runnable {
                arenaWorld = Bukkit.createWorld(WorldCreator(randomWorldName).generator(EmptyWorldGen()))
                arenaWorld?.isAutoSave = false
                Paintball.gameConfig.teams.forEach {
                    it.spawnPos?.world = arenaWorld
                }
                arenaWorld?.spawnLocation?.let { spawn ->
                    Bukkit.getOnlinePlayers().forEach {
                        it.teleport(spawn)
                    }
                }
                if (prevWorld == null) return@Runnable

                Bukkit.unloadWorld(prevWorld, false)
                prevWorld.worldFolder.deleteRecursively()
            })
        }
    }

    fun start() {
        if (state != GameState.WAITING) return
        state = GameState.RUNNING
        time = Duration.ZERO

        val allPlayers = Bukkit.getOnlinePlayers().toMutableList()
        allPlayers.forEach { it.gameMode = GameMode.ADVENTURE }

        maxPlayersInTeam = ceil(allPlayers.size.toDouble() / 2).toInt()

        val teamCount = Paintball.gameConfig.teams.size
        allPlayers.shuffled().forEachIndexed { i, p ->
            Paintball.gameConfig.teams[i % teamCount].addPlayer(p)
        }

        Paintball.gameConfig.teams.forEach { team ->
            val spawnLocation = team.spawnPos ?: return@forEach run {
                Bukkit.broadcastMessage(ThemeBuilder.themed(
                ":RED:Can't teleport players of team ::${team.displayName}\n" +
                    "Please set a spawnpoint and start again"
                ))
            }
            team.players.forEach { pl ->

                pl.sendMessage(ThemeBuilder.themed(
                    "*Paintball*: Benutze den Schneeball, um Blöcke einzufärben! " +
                        "Außerdem kannst du deine Gegner abschießen, " +
                        "wodurch du aber für ${Paintball.gameConfig.durations["kill"]?.inWholeSeconds ?: "ein paar"} " +
                        "Sekunden nicht schießen kannst. " +
                        "\nDas Team, das am Ende die größte Fläche eingefärbt hat, gewinnt!" +
                        "\nViel Erfolg!",
                    1
                ))

                pl.teleport(spawnLocation.clone().add(0.5, 0.0, 0.5))
                pl.inventory.heldItemSlot = 0
                pl.inventory.setItemInMainHand(snowballStack)

                pl.playSound(pl.location, Sound.BLOCK_NOTE_BLOCK_FLUTE, SoundCategory.MASTER, 100F, 1F)

                val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)

                val meta = chestplate.itemMeta as LeatherArmorMeta
                meta.setColor(team.material.chatColor)
                meta.isUnbreakable = true
                meta.addItemFlags(ItemFlag.HIDE_DYE)
                meta.addEnchant(Enchantment.BINDING_CURSE, 1, true)

                chestplate.itemMeta = meta

                pl.inventory.setItem(EquipmentSlot.CHEST, chestplate)
                pl.inventory.setItem(EquipmentSlot.LEGS, ItemStack(Material.LEATHER_LEGGINGS).let { it.itemMeta = meta; it })
                pl.inventory.setItem(EquipmentSlot.FEET, ItemStack(Material.LEATHER_BOOTS).let { it.itemMeta = meta; it })
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

            val ttNextShot = getTimeToNextShot(pl)
            if (ttNextShot > Duration.ZERO) actionBarMessage.append(ThemeBuilder.themed(
                " Kill-Cooldown: *${ttNextShot.inWholeSeconds + 1}*"
            ))

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

        Bukkit.getWorlds().first().getEntitiesByClass(Snowball::class.java).forEach {
            if (!it.item.isSimilar(snowballItem)) return@forEach
            it.fizzleOut()
        }

        val winnerTeamMaterial = enumValues<IncMaterial>().maxByOrNull {
            Scores.coloredObj?.getScore(it.name)?.score ?: 0
        } ?: return Bukkit.getLogger().warning("Something's off with the scoreboard")
        val winnerTeam = Paintball.gameConfig.teams.find { it.material == winnerTeamMaterial }
            ?: return Bukkit.getLogger().warning("Can't find team ${winnerTeamMaterial.name}")

        val playerScoreColored = Scores.coloredIndividualObj
            ?: return Bukkit.getLogger().warning("No scoreboard for player colored count")

        val playerScoreKills = Scores.killsObj
            ?: return Bukkit.getLogger().warning("No scoreboard for player kills")


        Bukkit.getOnlinePlayers().forEach {
            it.sendTitle(winnerTeam.displayName, "hat gewonnen!", 2, 1.minutes.inWholeTicks.toInt(), 5)
        }

        winnerTeam.players.forEach { pl ->
            val firework = pl.world.spawnEntity(pl.location, EntityType.FIREWORK) as Firework
            val meta = firework.fireworkMeta
            meta.addEffect(winnerFirework.withColor(winnerTeam.material.chatColor).build())
            meta.power = 1
            firework.fireworkMeta = meta
            pl.playSound(pl.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 100F, 1F)
        }

        Bukkit.broadcastMessage(ThemeBuilder.themed(
            Paintball.gameConfig.teams.joinToString("\n") {
                (if (it.material == winnerTeamMaterial) ":GOLD:★:: " else "   ") +
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
                it.sendMessage(ThemeBuilder.themed(
                    "Deine persönlichen Statistiken:\n" +
                        playerStatistics(it) +
                    if (it.isOp) "\nStarte eine neue Runde mit `/paintball restart`!" else "",
                    .5
                ))
            }
        }, 9.seconds.inWholeTicks)
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

        return Bukkit.getOnlinePlayers().asSequence()
            .map { it.name to obj.getScore(it.name).score }
            .filter { it.second != 0 }
            .sortedByDescending { it.second }
            .filterIndexed { index, _ -> index < 5 }
            .mapIndexed { i, pair -> "${i + 1}. *${pair.first}*: ${pair.second}" }
            .joinToString("\n")
    }

    private fun getTimeToNextShot(p: Player): Duration {
        val timeToNextShot = Paintball.lastKill[p.uniqueId] ?: return Duration.ZERO
        val timeLong =
            (timeToNextShot + Paintball.gameConfig.durations["kill"]!!.inWholeMilliseconds) - System.currentTimeMillis()
        return timeLong.milliseconds
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