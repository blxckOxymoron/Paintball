package de.crightgames.blxckoxymoron.paintball.game

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.Paintball.Companion.inWholeTicks
import de.crightgames.blxckoxymoron.paintball.config.ConfigTeam
import org.bukkit.Bukkit
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

class PlayerHitHandler(val player: Player, val team: ConfigTeam, private val enemy: ConfigTeam) {
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
        if (regenerate[player.uniqueId]?.isCancelled == false) return

        regenerate[player.uniqueId] =  Bukkit.getScheduler().runTaskTimer(
            Paintball.INSTANCE,
            Runnable {
                val damage = (damage[player.uniqueId] ?: 0) - 1

                if (damage >= 0) updateDamage(damage)

                if (damage == 0) {
                    regenerate[player.uniqueId]?.cancel()
                }

            },
            Paintball.gameConfig.durations["regen"]!!.inWholeTicks,
            Paintball.gameConfig.durations["regen"]!!.inWholeTicks
        )
    }

    fun wasHit(): Boolean {
        val newDamage = (damage[player.uniqueId] ?: 0) + 1

        if (newDamage >= Paintball.gameConfig.playerHealth) {
            updateDamage(0)
            regenerate[player.uniqueId]?.cancel()
            return true
        }

        updateDamage(newDamage)
        return false
    }

    fun updateDamage(dmg: Int) {
        damage[player.uniqueId] = dmg
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

        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = Paintball.gameConfig.playerHealth * 2.0
        player.health = ((1 - damagePercent) * Paintball.gameConfig.playerHealth * 2.0).coerceAtLeast(1.0)

        val ownMeta = baseMeta.clone()
        ownMeta.setColor(team.material.chatColor)
        val enemyMeta = baseMeta.clone()
        enemyMeta.setColor(enemy.material.chatColor)

        player.inventory.setItem(EquipmentSlot.FEET, ItemStack(Material.LEATHER_BOOTS)
            .also { it.itemMeta = if (damagePercent > 0) enemyMeta else ownMeta })

        player.inventory.setItem(EquipmentSlot.LEGS, ItemStack(Material.LEATHER_LEGGINGS)
            .also { it.itemMeta = if (damagePercent > 0.25) enemyMeta else ownMeta })

        player.inventory.setItem(EquipmentSlot.CHEST, ItemStack(Material.LEATHER_CHESTPLATE)
            .also { it.itemMeta = if (damagePercent > 0.5) enemyMeta else ownMeta })

        player.inventory.setItem(EquipmentSlot.HEAD, ItemStack(Material.LEATHER_HELMET)
            .also { it.itemMeta = if (damagePercent > 0.75) enemyMeta else ownMeta })
    }

}