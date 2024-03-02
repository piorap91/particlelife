package pl.game.core

interface PhysicsEngine {
    fun update(deltaTime: Float)
    val particleGroupsManager: ParticleGroupsManager
}