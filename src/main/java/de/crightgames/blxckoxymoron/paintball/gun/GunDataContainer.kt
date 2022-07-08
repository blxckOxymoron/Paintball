package de.crightgames.blxckoxymoron.paintball.gun

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileEffect
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileParticle
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileType
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.*

object GunDataContainer : PersistentDataType<PersistentDataContainer, Gun> {
    override fun getPrimitiveType(): Class<PersistentDataContainer> {
        return PersistentDataContainer::class.java
    }

    override fun getComplexType(): Class<Gun> {
        return Gun::class.java
    }

    override fun toPrimitive(gun: Gun, context: PersistentDataAdapterContext): PersistentDataContainer {

        // PROJECTILE
        val projectileContainer = context.newPersistentDataContainer()
        val pj = gun.projectile
        if (pj.entity != null)
            projectileContainer.set(key("entity"), STRING, pj.entity.name)
        projectileContainer.set(key("gravity"), DOUBLE, pj.gravity)
        projectileContainer.set(key("speed"), DOUBLE, pj.speed)
        projectileContainer.set(key("particle"), STRING, pj.particle.name)

        val effects = pj.effects.map {
            val container = context.newPersistentDataContainer()
            container.set(key("name"), STRING, it.first.name)
            container.set(key("lvl"), INTEGER, it.second)
            return@map container
        }
        projectileContainer.set(key("effects"), TAG_CONTAINER_ARRAY, effects.toTypedArray())

        // GUN
        val gunContainer = context.newPersistentDataContainer()
        gunContainer.set(key("projectile"), TAG_CONTAINER, projectileContainer)
        gunContainer.set(key("bullets"), INTEGER, gun.bullets)
        gunContainer.set(key("spray"), DOUBLE, gun.spray)
        gunContainer.set(key("rate"), LONG, gun.rateOfFire)

        return gunContainer
    }

    /**
     * using a lot of `!!` operators. Might be bad when arbitrary values are passed
     */
    override fun fromPrimitive(container: PersistentDataContainer, context: PersistentDataAdapterContext): Gun {
        val projectileContainer = container.get(key("projectile"), TAG_CONTAINER)!!
        val effects = projectileContainer.get(
            key("effects"), TAG_CONTAINER_ARRAY
        )!!.mapNotNull {
            val effect = getEnum<ProjectileEffect>(it, key("name")) ?: return@mapNotNull null

            val level = it.get(key("lvl"), INTEGER)!!
            return@mapNotNull effect to level
        }

        val particle = getEnum(projectileContainer, key("particle")) ?: ProjectileParticle.NONE
        val entity = getEnum<EntityType>(projectileContainer, key("particle"))

        val projectile = ProjectileType(
            projectileContainer.get(key("speed"), DOUBLE)!!,
            projectileContainer.get(key("gravity"), DOUBLE)!!,
            effects,
            particle,
            entity
        )

        return Gun(
            projectile,
            container.get(key("rate"), LONG)!!,
            container.get(key("spray"), DOUBLE)!!,
            container.get(key("bullets"), INTEGER)!!
        )
    }

    private fun key(name: String): NamespacedKey {
        return NamespacedKey(Paintball.INSTANCE, name)
    }

    private inline fun <reified E : Enum<E>> getEnum(
        container: PersistentDataContainer,
        key: NamespacedKey,
    ): E? {
        return kotlin.runCatching {
            enumValueOf<E>(
                container.get(key, STRING) ?: return null
            )
        }.getOrNull()
    }
}