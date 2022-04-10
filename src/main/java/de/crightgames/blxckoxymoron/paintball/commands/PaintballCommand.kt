package de.crightgames.blxckoxymoron.paintball.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import de.crightgames.blxckoxymoron.paintball.util.BrigadierTabExecutor
import org.bukkit.command.CommandSender

class PaintballCommand : BrigadierTabExecutor("paintball"){
    init {
        dispatcher.register(
            literal<CommandSender?>(commandName)
            .then(
                TeamspawnCommand()
            ).then(
                MinPlayersCommand()
            ).then(
                AutostartCommand()
            ).then(
                ForceStartCommand()
            ).then(
                SwitchTeamCommand()
            ).then(
                SampleKillCommand()
            ).then(
                ArenaCommand()
            ).then(
                RestartCommand()
            )
        )
    }
}