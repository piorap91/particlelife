package pl.game.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.pow

class PhysicsEngineParallel(
    val screen: Screen,
    override val particleGroupsManager: ParticleGroupsManager,
    val attractionMatrix: AttractionMatrix,
    val changeables: Changeables,
    val attract: Attraction
): PhysicsEngine {

    override fun update(deltaTime: Float) {
        updateAccelerationsForEach(deltaTime)
        updatePositions(deltaTime)
    }

    fun updateAccelerationsForEach(deltaTime: Float) = runBlocking(Dispatchers.Default) {
        val friction = 0.5f.pow(deltaTime / changeables.frictionHalfTime.value)
        particleGroupsManager.particleGroups().forEach { group1 ->
            particleGroupsManager.particleGroups().forEach { group2 ->
                val attraction = attractionMatrix.force(group1.id, group2.id)
                group1.particles.map { particle1 ->
                    launch {
                        val force2D = Force2D()
                        group2.particles.forEach { particle2 ->
                            attract(particle1, particle2, attraction, force2D)
                        }
                        particle1.applyForce(force2D, friction, changeables.rMax, deltaTime)
                    }
                }.joinAll()
            }
        }
    }

    fun updatePositions(deltaTime: Float) {
        particleGroupsManager.particleGroups().forEach { group ->
            group.particles.forEach { particle ->
                particle.move(deltaTime, screen)
            }
        }
    }
}