package de.crightgames.blxckoxymoron.paintball.inc

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.config.ConfigTeam
import de.crightgames.blxckoxymoron.paintball.config.ConfigTeam.Companion.team
import de.crightgames.blxckoxymoron.paintball.game.Scores
import de.crightgames.blxckoxymoron.paintball.game.Scores.plusAssign
import de.crightgames.blxckoxymoron.paintball.util.VectorUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import kotlin.math.round
import kotlin.random.Random

object ColorReplace {

    private val noCopyData = listOf("lit")

    private fun copyBlockDataSting(from: BlockData, to: Block) {
        val fromBlockString = from.asString
            .replace(Regex("^[\\w:]*"), "")
            .replace(Regex("(,?${noCopyData.joinToString("|")})=[^,\\s\\]]*"), "")
            .replace(Regex("(?<=\\[),"), "")
            .replace(Regex("\\[]"), "")

        if (fromBlockString.isEmpty()) return

        try {
            // try a simple conversion
            val toBlockData = Bukkit.createBlockData("minecraft:" + to.type.name.lowercase() + fromBlockString)
            to.setBlockData(toBlockData, false)
        } catch (_: IllegalArgumentException) { }

    }

    private fun replaceWithColor(color: IncMaterial): (Block) -> Pair<Boolean, IncMaterial?> {
        // returns true if block was replaced
        return replacer@{ block: Block ->
            if (
                Paintball.gameConfig.noReplace.contains(block.type) ||
                block.state is Container ||
                block.isLiquid ||
                block.isEmpty
            ) return@replacer false to null
            val prevData = block.blockData.clone()

            // to copy block data:
            // search for every get method in previous data
            // if a set method exists on the new data, use it
            // apply the new data
            val (replacement, prevTeam) = color.findReplacement(block.type)
            val sameBlock = replacement == prevData.material
            block.setType(replacement, false)


            copyBlockDataSting(prevData, block)

            return@replacer !sameBlock to prevTeam
        }
    }

    fun replaceRadius(location: Location, player: Player, team: ConfigTeam? = player.team, multiplier: Double = 1.0) {
        if (team == null) return

        val radius = round(Paintball.gameConfig.colorRadius * multiplier).toInt()

        val blocksAround = VectorUtils.vectorsInRadius(radius)
            .filter { Random.Default.nextDouble() < ((radius.toDouble() * .6) / it.length()) }
            .map { location.clone().add(it).block }

        val replacedColors = blocksAround.map(replaceWithColor(team.material)).filter { it.first }.map { it.second }

        val individualColoredScore = Scores.coloredIndividualObj?.getScore(player.name)
        if (individualColoredScore != null)
            individualColoredScore.score = individualColoredScore.score + replacedColors.size

        replacedColors.filterNotNull().forEach { inc ->
            Scores.coloredObj?.getScore(inc.name)?.plusAssign(-1)
        }
        Scores.coloredObj?.getScore(team.name)?.plusAssign(replacedColors.size)

    }
}