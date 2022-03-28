package de.crightgames.blxckoxymoron.paintball.util

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit

abstract class BrigadierTabExecutor(var commandName: String) : TabExecutor {

    val dispatcher = CommandDispatcher<CommandSender>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        try {
            var input = commandName
            if (args.isNotEmpty()) input += " " + args.joinToString(" ")
            dispatcher.execute(input, sender)
        } catch (e: CommandSyntaxException) {
            sender.sendMessage("" + ChatColor.RED + e.message)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        val parseResults = dispatcher.parse(commandName + " " + args.joinToString(" "), sender)
        return try {
            dispatcher.getCompletionSuggestions(parseResults)[20, TimeUnit.SECONDS].list.map { it.text }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun register(plugin: JavaPlugin) {
        val plCmd = plugin.getCommand(this.commandName) ?: return
        val usages = this.dispatcher.getAllUsage(this.dispatcher.root, null, false)
        plCmd.usage = usages.joinToString("\n")
        plCmd.setExecutor(this) // also sets Tab Executor
    }
}