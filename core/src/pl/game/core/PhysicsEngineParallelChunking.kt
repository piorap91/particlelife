package pl.game.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.random.Random

class PhysicsEngineParallelChunking(
    val screen: Screen,
    override val particleGroupsManager: ParticleGroupsManager,
    val attractionMatrix: AttractionMatrix,
    val chunkManager: ChunkManager,
    val changeables: Changeables,
    val attract: Attraction
): PhysicsEngine {
    override fun update(deltaTime: Float) {
        updateAccelerationsChunking(deltaTime)
        updatePositions(deltaTime)
    }

    fun updateAccelerationsChunking(deltaTime: Float) = runBlocking(Dispatchers.Default) {
        val friction = 0.5f.pow(deltaTime / changeables.frictionHalfTime.value)
        particleGroupsManager.particleGroups().forEach { group1 ->
            group1.particles.map { particle1 ->
                launch {
                    val force2D = Force2D()
                    chunkManager.neighbourParticlesFor(particle1).forEach { particle2 ->
                        val attraction = attractionMatrix.force(group1.id, particle2.particleGroup.id)
                        attract(particle1, particle2, attraction, force2D)
                    }
                    particle1.applyForce(force2D, friction, changeables.rMax, deltaTime)
                }
            }.joinAll()
        }
    }


    fun updatePositions(deltaTime: Float) {
        chunkManager.reset()
        particleGroupsManager.particleGroups().forEach { group ->
            group.particles.forEach { particle ->
                particle.move(deltaTime, screen).also {
                    chunkManager.add(particle)
                }
            }
        }
    }
}