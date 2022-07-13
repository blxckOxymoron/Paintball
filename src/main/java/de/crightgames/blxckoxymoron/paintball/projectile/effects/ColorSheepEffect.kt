package de.crightgames.blxckoxymoron.paintball.projectile.effects

import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileHitEntityEvent
import de.crightgames.blxckoxymoron.paintball.util.ThemeBuilder
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Sheep

class ColorSheepEffect : ProjectileEffect() {
    override fun onEntityHit(e: ProjectileHitEntityEvent): Boolean {
        if (e.hitEntity !is Sheep) return false

        val color = org.bukkit.DyeColor.getByColor(Color.fromRGB(e.data))
        if (color == null) {
            Bukkit.broadcastMessage(
                ThemeBuilder.themed(
                "No color found for hex *${e.data.toString(16)}* use `DyeColor.CYAN.color.asRGB()`"
            ))
        } else {
            e.hitEntity.color = color
        }

        return false
    }
}