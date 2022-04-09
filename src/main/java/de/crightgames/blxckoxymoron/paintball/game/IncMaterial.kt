package de.crightgames.blxckoxymoron.paintball.game

import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material

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
