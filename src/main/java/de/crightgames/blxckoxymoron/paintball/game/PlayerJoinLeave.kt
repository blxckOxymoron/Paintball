package de.crightgames.blxckoxymoron.paintball.game

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerJoinLeave : Listener{

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        if (!e.player.isOp) e.player.gameMode = GameMode.SPECTATOR

        e.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(ThemeBuilder.themed(
            "Willkommen zu *Paintball*, *${e.player.name}*!"
        )))

        val onlinePlayerCount = Bukkit.getOnlinePlayers().size

        e.joinMessage = ThemeBuilder.themed(
            ":GREEN:»:: *${e.player.name}* `($onlinePlayerCount/${Paintball.gameConfig.minimumPlayers})`"
        )

        if (onlinePlayerCount == Paintball.gameConfig.minimumPlayers) {
            Countdown.start()
        }
    }

    @EventHandler
    fun onPlayerLeave(e: PlayerQuitEvent) {
        val onlinePlayerCount = Bukkit.getOnlinePlayers().size - 1
        e.quitMessage = ThemeBuilder.themed(
            ":RED:«:: *${e.player.name}* `($onlinePlayerCount/${Paintball.gameConfig.minimumPlayers})`"
        )
    }
}