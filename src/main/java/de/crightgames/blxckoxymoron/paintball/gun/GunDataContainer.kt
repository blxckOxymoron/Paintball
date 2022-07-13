package de.crightgames.blxckoxymoron.paintball.gun

import de.crightgames.blxckoxymoron.paintball.Paintball
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileEffect
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileParticle
import de.crightgames.blxckoxymoron.paintball.projectile.ProjectileType
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.*

object GunDataContainer : PersistentDataType<PersistentDataContainer, Gun> {
    val KEY = NamespacedKey(Paintball.INSTANCE, "gun")

    val ItemStack.isGun: Boolean
        get() = this.itemMeta?.persistentDataContainer?.has(KEY, GunDataContainer) ?: false


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

        // MAGAZINE
        val magazineContainer = context.newPersistentDataContainer()
        magazineContainer.set(key("size"), INTEGER, gun.magazine.size)
        magazineContainer.set(key("speed"), LONG, gun.magazine.reloadSpeed)
        magazineContainer.set(key("content"), INTEGER, gun.magazine.content)

        // GUN
        val gunContainer = context.newPersistentDataContainer()
        gunContainer.set(key("projectile"), TAG_CONTAINER, projectileContainer)
        gunContainer.set(key("magazine"), TAG_CONTAINER, magazineContainer)
        gunContainer.set(key("bullets"), INTEGER, gun.bullets)
        gunContainer.set(key("spray"), DOUBLE, gun.spray)
        gunContainer.set(key("rate"), LONG, gun.rateOfFire)
        gunContainer.set(key("sound"), STRING, gun.sound.name)
        gunContainer.set(key("pitch"), FLOAT, gun.pitch)

        return gunContainer
    }

    /**
     * using a lot of `!!` operators. Might be bad when arbitrary values are passed
     */
    override fun fromPrimitive(container: PersistentDataContainer, context: PersistentDataAdapterContext): Gun {

        // PROJECTILE
        val projectileContainer = container.get(key("projectile"), TAG_CONTAINER)!!
        val effects = projectileContainer.get(
            key("effects"), TAG_CONTAINER_ARRAY
        )!!.mapNotNull {
            val effect = getEnum<ProjectileEffect>(it, key("name")) ?: return@mapNotNull null

            val level = it.get(key("lvl"), INTEGER)!!
            return@mapNotNull effect to level
        }

        val particle = getEnum(projectileContainer, key("particle")) ?: ProjectileParticle.NONE
        val entity = getEnum<EntityType>(projectileContainer, key("entity"))

        val projectile = ProjectileType(
            projectileContainer.get(key("speed"), DOUBLE)!!,
            projectileContainer.get(key("gravity"), DOUBLE)!!,
            effects,
            particle,
            entity
        )

        // MAGAZINE
        val magazineContainer = container.get(key("magazine"), TAG_CONTAINER)!!
        val magazine = Magazine(
            magazineContainer.get(key("size"), INTEGER)!!,
            magazineContainer.get(key("speed"), LONG)!!,
            magazineContainer.get(key("content"), INTEGER)!!
        )

        return Gun(
            projectile,
            magazine,
            container.get(key("rate"), LONG)!!,
            container.get(key("spray"), DOUBLE)!!,
            container.get(key("bullets"), INTEGER)!!,
            getEnum<Sound>(container, key("sound"))!!,
            container.get(key("pitch"), FLOAT)!!,
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