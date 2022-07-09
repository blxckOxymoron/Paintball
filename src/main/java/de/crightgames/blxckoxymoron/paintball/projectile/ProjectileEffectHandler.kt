package de.crightgames.blxckoxymoron.paintball.projectile

class ProjectileEffectHandler(setup: ProjectileEffectHandler.() -> Unit): (ProjectileHitEvent) -> Boolean {
    var blockHit: (ProjectileHitBlockEvent) -> Boolean = { false }
    var entityHit: (ProjectileHitEntityEvent) -> Boolean = { false }
    var whenDestroyed: (ProjectileRemoveEvent) -> Unit = {}
    init {
        this.apply(setup)
    }

    override fun invoke(e: ProjectileHitEvent): Boolean {
        return when (e) {
            is ProjectileHitBlockEvent -> blockHit(e)
            is ProjectileHitEntityEvent -> entityHit(e)
            is ProjectileRemoveEvent -> {
                whenDestroyed(e)
                false
            }
            else -> false
        }
    }
}