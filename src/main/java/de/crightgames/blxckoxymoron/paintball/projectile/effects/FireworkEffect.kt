package de.crightgames.blxckoxymoron.paintball.projectile.effects

import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileRemoveEvent
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework

class FireworkEffect : ProjectileEffect() {
    override fun onDestroyed(e: ProjectileRemoveEvent) {
        val firework = e.location.world?.spawnEntity(e.location, EntityType.FIREWORK) as Firework
        val meta = firework.fireworkMeta
        meta.addEffect(
            FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL)
                .withFlicker()
                .withColor(Color.fromRGB(e.data))
                .build()
        )
        firework.fireworkMeta = meta
        firework.detonate()
    }
}