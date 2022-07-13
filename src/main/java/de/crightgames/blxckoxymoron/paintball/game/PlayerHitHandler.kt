package de.crightgames.blxckoxymoron.paintball.game

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.Paintball.Companion.inWholeTicks
import de.crightgames.blxckoxymoron.paintball.config.ConfigTeam
import de.crightgames.blxckoxymoron.paintball.game.Scores.plusAssign
import de.crightgames.blxckoxymoron.paintball.gun.ReloadGun
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.scheduler.BukkitTask
import java.util.*

class PlayerHitHandler(val hitPlayer: Player, val team: ConfigTeam, val enemy: Player, val enemyTeam: ConfigTeam) {

    companion object {
        private val damage = mutableMapOf<UUID, Int>()
        private val regenerate = mutableMapOf<UUID, BukkitTask>()

        fun resetDamage() {
            damage.clear()
        }

        fun getHealthPercent(player: Player): Float {
            return (damage[player.uniqueId]?: 0) / Paintball.gameConfig.playerHealth.toFloat()
        }
    }

    private fun regeneratePlayer() {
        if (regenerate[hitPlayer.uniqueId]?.isCancelled == false) return

        regenerate[hitPlayer.uniqueId] =  Bukkit.getScheduler().runTaskTimer(
            Paintball.INSTANCE,
            Runnable {
                val damage = (damage[hitPlayer.uniqueId] ?: 0) - 1

                if (damage >= 0) updateDamage(damage)

                if (damage == 0) {
                    regenerate[hitPlayer.uniqueId]?.cancel()
                }

            },
            Paintball.gameConfig.durations["regen"]!!.inWholeTicks,
            Paintball.gameConfig.durations["regen"]!!.inWholeTicks
        )
    }

    fun wasHit(strength: Int = 1): Boolean {
        val newDamage = (damage[hitPlayer.uniqueId] ?: 0) + strength

        if (newDamage >= Paintball.gameConfig.playerHealth) {
            updateDamage(0)
            regenerate[hitPlayer.uniqueId]?.cancel()
            wasKilled()
            return true
        }

        updateDamage(newDamage)
        return false
    }

    private fun wasKilled() {
        Bukkit.broadcastMessage(
            ThemeBuilder.themed( // hit handler
            "*${hitPlayer.name}* wurde von *${enemy.name}* abgeschossen!"
        ))

        Scores.killsObj?.getScore(enemy.name)?.plusAssign(1)
        Scores.deathsObj?.getScore(hitPlayer.name)?.plusAssign(1)

        hitPlayer.gameMode = GameMode.SPECTATOR
        Bukkit.getScheduler().runTaskLater(
            Paintball.INSTANCE,
            Runnable { Game.respawnPlayer(hitPlayer) },
            Paintball.gameConfig.durations["respawn"]!!.inWholeTicks
        )

        val now = System.currentTimeMillis()
        Paintball.lastDeath[hitPlayer.uniqueId] = now

        ReloadGun.cancelReload(hitPlayer.uniqueId)

    }

    fun updateDamage(dmg: Int) {
        damage[hitPlayer.uniqueId] = dmg
        if (dmg > 0) regeneratePlayer()
        setVisibleColorLevel(dmg.toFloat() / Paintball.gameConfig.playerHealth)
    }

    private val baseMeta =
        (ItemStack(Material.LEATHER_BOOTS).itemMeta as LeatherArmorMeta).also {
            it.isUnbreakable = true
            it.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS)
            it.addEnchant(Enchantment.BINDING_CURSE, 1, true)
        }

    private fun setVisibleColorLevel(damagePercent: Float) {

        hitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = Paintball.gameConfig.playerHealth * 2.0
        hitPlayer.health = ((1 - damagePercent) * Paintball.gameConfig.playerHealth * 2.0).coerceAtLeast(1.0)

        val ownMeta = baseMeta.clone()
        ownMeta.setColor(team.material.chatColor)
        val enemyMeta = baseMeta.clone()
        enemyMeta.setColor(enemyTeam.material.chatColor)

        hitPlayer.inventory.setItem(EquipmentSlot.FEET, ItemStack(Material.LEATHER_BOOTS)
            .also { it.itemMeta = if (damagePercent > 0) enemyMeta else ownMeta })

        hitPlayer.inventory.setItem(EquipmentSlot.LEGS, ItemStack(Material.LEATHER_LEGGINGS)
            .also { it.itemMeta = if (damagePercent > 0.25) enemyMeta else ownMeta })

        hitPlayer.inventory.setItem(EquipmentSlot.CHEST, ItemStack(Material.LEATHER_CHESTPLATE)
            .also { it.itemMeta = if (damagePercent > 0.5) enemyMeta else ownMeta })

        hitPlayer.inventory.setItem(EquipmentSlot.HEAD, ItemStack(Material.LEATHER_HELMET)
            .also { it.itemMeta = if (damagePercent > 0.75) enemyMeta else ownMeta })
    }

}