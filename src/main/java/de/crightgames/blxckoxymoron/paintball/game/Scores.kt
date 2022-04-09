package de.crightgames.blxckoxymoron.paintball.game

import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.Bukkit
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Score
import org.bukkit.scoreboard.Scoreboard

object Scores {

    var killsObj: Objective? = null
    var deathsObj: Objective? = null
    var shotsObj: Objective? = null
    var coloredObj: Objective? = null
    var coloredIndividualObj: Objective? = null

    fun createAndResetScores() {
        val sm = Bukkit.getScoreboardManager() ?: return
        val board = sm.mainScoreboard
        killsObj = getOrCreateObjective(board, "pb-kills", "" + ThemeBuilder.HIGHLIGHT + "Kills")
        deathsObj = getOrCreateObjective(board, "pb-deaths")
        shotsObj = getOrCreateObjective(board, "pb-shots")
        coloredObj = getOrCreateObjective(board, "pb-color")
        coloredIndividualObj = getOrCreateObjective(board, "pb-color-player", "" + ThemeBuilder.HIGHLIGHT + "Top Spieler")

        killsObj?.displaySlot = DisplaySlot.BELOW_NAME
        coloredIndividualObj?.displaySlot = DisplaySlot.SIDEBAR
    }

    private fun getOrCreateObjective(board: Scoreboard, name: String, displayName: String = name): Objective? {
        board.getObjective(name)?.unregister()
        return try {
            board.registerNewObjective(name, "dummy", displayName)
        } catch (_: IllegalArgumentException) {
            Bukkit.getLogger().severe("Can't create scoreboard $name ($displayName)!")
            null
        }
    }

    operator fun Score.plusAssign(count: Int) {
        this.score = this.score + count
    }
}