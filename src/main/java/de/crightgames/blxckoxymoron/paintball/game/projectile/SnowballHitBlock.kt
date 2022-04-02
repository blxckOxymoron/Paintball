package de.crightgames.blxckoxymoron.paintball.game.projectile

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.game.Game
import de.crightgames.blxckoxymoron.paintball.game.Scores
import de.crightgames.blxckoxymoron.paintball.game.Scores.plusAssign
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import de.crightgames.blxckoxymoron.paintball.util.VectorUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent
import kotlin.random.Random

class SnowballHitBlock : Listener {

    companion object {
        val showWithGlass: (Block) -> Unit = {
            it.type = when (it.type) {
                Material.LIME_STAINED_GLASS     -> Material.YELLOW_STAINED_GLASS
                Material.YELLOW_STAINED_GLASS   -> Material.ORANGE_STAINED_GLASS
                Material.ORANGE_STAINED_GLASS   -> Material.RED_STAINED_GLASS
                Material.RED_STAINED_GLASS      -> Material.MAGENTA_STAINED_GLASS
                Material.MAGENTA_STAINED_GLASS  -> Material.MAGENTA_STAINED_GLASS
                else -> Material.LIME_STAINED_GLASS
            }
        }

        private val noReplace = listOf(Material.AIR, Material.WATER, Material.LAVA, Material.VOID_AIR, Material.CAVE_AIR)
        private val noCopyData = listOf("lit")

        fun copyBlockDataJava(from: BlockData, to: Block) {

            try {
                val prevDataMap = from::class.java.methods.map { method ->
                    val name = method.name

                    return@map name.replace(Regex("^(get|is)"), "") to
                        if ((name.startsWith("get") || name.startsWith("is")) &&
                            method.parameterCount == 0
                        ) method.invoke(from)
                        else null
                }.filter { it.second != null }

                Bukkit.broadcastMessage(from.material.name)

                Bukkit.broadcastMessage(prevDataMap.joinToString("\n") {
                    it.first + " > " + if (it.second != null) it.second!!::class.simpleName else "null"
                })

                val newData = to.blockData

                newData::class.java.methods.forEach { method ->
                    val name = method.name
                    if (!name.startsWith("set")) return@forEach
                    val newDataVal = prevDataMap.find {
                        it.first == name.replace(Regex("^set"), "")
                    }?.second

                    if (newDataVal == null) Bukkit.broadcastMessage("no value for: $name")

                    if (
                        newDataVal != null &&
                        method.parameterCount == 1 &&
                        method.parameters.first().type.name == newDataVal::class.java.name
                    ) {
                        Bukkit.broadcastMessage("invoked: $name")
                        method.invoke(newData, newDataVal)
                    }
                }

                to.setBlockData(newData, false)

            } catch (e: IllegalArgumentException) {
                Bukkit.broadcastMessage(e.message ?: "ERROR")
            }

        }

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

            /*
            try
                // more complex: also checks if the key exists on the second block
                val toDataKeys = to.blockData.getAsString(false)
                    .replace(Regex("^[\\w:]*\\["), "")
                    .replace(Regex("]$"), "")
                    .split(",")
                    .mapNotNull { it.split("=").firstOrNull() }


                if (toDataKeys.isEmpty()) return
                Bukkit.broadcastMessage(toDataKeys.joinToString())

                val fromBlockString = from.asString
                val fromBlockStringData = Regex("(${toDataKeys.joinToString("|")})=[^,\\s\\]]*")
                    .findAll(fromBlockString).joinToString(",") { it.value }
                if (fromBlockStringData.isEmpty()) return
                Bukkit.broadcastMessage(fromBlockStringData)

                val toBlockData = Bukkit.createBlockData(
                    fromBlockString.replace(Regex("\\[.*]"), "") + "[" + fromBlockStringData + "]"
                )
                to.setBlockData(toBlockData, false)
                return
            } catch (_: IllegalArgumentException) {}
             */

        }

        fun replaceWithColor(color: IncMaterial): (Block) -> Pair<Boolean, IncMaterial?> {
            // returns true if block was replaced
            return replacer@{ block: Block ->
                if (noReplace.contains(block.type) || block.blockData is Container) return@replacer false to null
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
    }


    @EventHandler
    fun onSnowballHit(e: ProjectileHitEvent) {
        val block = e.hitBlock ?: return
        val player = e.entity.shooter as? Player ?: return
        val teamIndex = Paintball.teams.indexOfFirst { it.contains(player) }.takeUnless { it == -1 } ?: return
        val teamName = Game.teamNames[teamIndex]
        val color = enumValueOf<IncMaterial>(teamName)

        //DEBATABLE: only replace blocks with line of sight
        val blocksAround = VectorUtils.vectorsInRadius(Paintball.COLOR_RADIUS)
            .filter { Random.Default.nextDouble() < ((Paintball.COLOR_RADIUS.toDouble() * .6) / it.length()) }
            .map { block.location.clone().add(it).block }

        val replacedTeams = blocksAround.map(replaceWithColor(color)).filter { it.first }.map { it.second }

        val individualColoredScore = Scores.coloredIndividualObj?.getScore(player.name)
        if (individualColoredScore != null)
            individualColoredScore.score = individualColoredScore.score + replacedTeams.size

        replacedTeams.filterNotNull().forEach { inc ->
            Scores.coloredObj?.getScore(inc.name)?.plusAssign(-1)
        }
        Scores.coloredObj?.getScore(teamName)?.plusAssign(replacedTeams.size)

    }

    enum class IncMaterial(private val wood: String, val color: String, private val replacements: List<Pair<Regex, Material>> = listOf(), val displayName: String) {
        RED("CRIMSON", "RED", listOf(
            Pair(Regex("^(TALL_|LARGE_)?(GRASS|FERN|DEAD_BUSH)\$"), Material.CRIMSON_ROOTS),
            Pair(Regex("\\w*(LEAVES|WART_BLOCK)$"), Material.NETHER_WART_BLOCK),
            Pair(Regex("STRIPPED\\w*LOG$"), Material.STRIPPED_CRIMSON_STEM),
            Pair(Regex("\\w*LOG$"),         Material.CRIMSON_STEM),
            Pair(Regex("^GRASS_BLOCK$"),    Material.CRIMSON_NYLIUM),
            Pair(Regex("^SNOW$"),           Material.RED_CARPET),
            Pair(Regex("\\w*WALL_TORCH$"),  Material.REDSTONE_WALL_TORCH),
            Pair(Regex("\\w*TORCH$"),       Material.REDSTONE_TORCH),
        ), "" + ChatColor.DARK_RED + "Rot" + ThemeBuilder.DEFAULT),
        BLUE("WARPED", "CYAN", listOf(
            Pair(Regex("^(TALL_|LARGE_)?(GRASS|FERN|DEAD_BUSH)$"), Material.WARPED_ROOTS),
            Pair(Regex("\\w*(LEAVES|WART_BLOCK)$"), Material.WARPED_WART_BLOCK),
            Pair(Regex("STRIPPED\\w*LOG$"), Material.STRIPPED_WARPED_STEM),
            Pair(Regex("\\w*LOG"),          Material.WARPED_STEM),
            Pair(Regex("^GRASS_BLOCK$"),    Material.WARPED_NYLIUM),
            Pair(Regex("^SNOW$"),           Material.CYAN_CARPET),
            Pair(Regex("\\w*WALL_TORCH$"),  Material.SOUL_WALL_TORCH),
            Pair(Regex("\\w*TORCH$"),       Material.SOUL_TORCH),
        ), "" + ChatColor.DARK_AQUA + "Blau" + ThemeBuilder.DEFAULT);

        companion object {
            private const val MAX_SPLITS = 2
            private const val DEFAULT_MATERIAL = "CONCRETE"

        }

        fun findReplacement(oldMaterial: Material): Pair<Material, IncMaterial?> {

            /** possible blocks
             * - glass
             * - glass panes
             *
             * - concrete
             * - concrete powder
             * - wool
             *
             * - slabs
             * - stairs
             * - trapdoors
             * - doors
             * - signs
             * - buttons
             * - pressure plates
             *
             * - else

             * => take away first part of name of material (e. g. STONE_SLAB -> SLAB)
             * !! some colors have double names -> light_gray

             * try to use the wood type => CRIMSON_SLAB
             * try the color type => RED_WOOL
             * use the default =>
             */

            var currentName = oldMaterial.name

            val previousTeam = enumValues<IncMaterial>().find { inc ->
                currentName.startsWith(inc.color)
                    || currentName.startsWith(inc.wood)
                    || inc.replacements.any { it.second.name == currentName }
            }

            //TODO replace non-solid blocks (e.g. grass or flowers) with their warped ot crimson counterparts
            this.replacements.forEach { (pattern, replacement) ->
                if (oldMaterial == replacement) return oldMaterial to previousTeam
                if (pattern.matches(currentName)) return replacement to previousTeam
            }

            // also color "uncolored" blocks: glass_pane -> stained_glass_pane; terracotta -> red_terracotta
            currentName = "STAINED_$currentName"

            for (i in 0..MAX_SPLITS+2) {

                if (i == MAX_SPLITS+2 && !oldMaterial.isSolid) {
                    return oldMaterial to previousTeam
                }

                try {
                    return enumValueOf<Material>(wood + "_" + currentName) to previousTeam
                } catch (_: IllegalArgumentException) {}
                try {
                    return enumValueOf<Material>(color + "_" + currentName) to previousTeam
                } catch (_: IllegalArgumentException) {}

                currentName =
                    if (i == MAX_SPLITS + 1) DEFAULT_MATERIAL
                    else currentName.replace(Regex("^[A-Z]*_"), "")

            }

            Bukkit.getLogger().warning("illegal default material for coloring: $DEFAULT_MATERIAL")
            return Material.BEDROCK to previousTeam
        }
    }
}