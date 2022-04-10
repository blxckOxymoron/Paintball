package de.crightgames.blxckoxymoron.paintball.game

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.boss.BarColor

enum class IncMaterial(
    private val wood: String,
    val color: String,
    val chatColor: Color,
    val barColor: BarColor,
    private val replacements: List<Pair<Regex, Material>> = listOf()
) {
    RED("CRIMSON", "RED", Color.RED, BarColor.RED, listOf(
        Regex("^(TALL_|LARGE_)?(GRASS|FERN|DEAD_BUSH)\$") to Material.CRIMSON_ROOTS,
        Regex("\\w*(LEAVES|WART_BLOCK)$") to Material.NETHER_WART_BLOCK,
        Regex("STRIPPED\\w*LOG$|^QUARTZ_PILLAR$") to Material.STRIPPED_CRIMSON_STEM,
        Regex("STRIPPED\\w*WOOD$|^QUARTZ_BLOCK$") to Material.STRIPPED_CRIMSON_HYPHAE,
        Regex("\\w*LOG$")           to Material.CRIMSON_STEM,
        Regex("\\w*WOOD$")          to Material.CRIMSON_HYPHAE,
        Regex("^GRASS_BLOCK$")      to Material.CRIMSON_NYLIUM,
        Regex("^SNOW$")             to Material.RED_CARPET,
        Regex("\\w*WALL_TORCH$")    to Material.REDSTONE_WALL_TORCH,
        Regex("\\w*TORCH$")         to Material.REDSTONE_TORCH,
        Regex("\\w*WALL")           to Material.RED_NETHER_BRICK_WALL,
    )),
    BLUE("WARPED", "CYAN", Color.TEAL, BarColor.BLUE, listOf(
        Regex("^(TALL_|LARGE_)?(GRASS|FERN|DEAD_BUSH)$") to Material.WARPED_ROOTS,
        Regex("\\w*(LEAVES|WART_BLOCK)$") to Material.WARPED_WART_BLOCK,
        Regex("STRIPPED\\w*LOG$|^QUARTZ_PILLAR\$") to Material.STRIPPED_WARPED_STEM,
        Regex("STRIPPED\\w*WOOD$|^QUARTZ_BLOCK\$") to Material.STRIPPED_WARPED_HYPHAE,
        Regex("\\w*LOG")            to Material.WARPED_STEM,
        Regex("\\w*WOOD")           to Material.WARPED_HYPHAE,
        Regex("^GRASS_BLOCK$")      to Material.WARPED_NYLIUM,
        Regex("^SNOW$")             to Material.CYAN_CARPET,
        Regex("\\w*WALL_TORCH$")    to Material.SOUL_WALL_TORCH,
        Regex("\\w*TORCH$")         to Material.SOUL_TORCH,
        Regex("\\w*WALL")           to Material.PRISMARINE_WALL,
    ));

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
