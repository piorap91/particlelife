package pl.game.core

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow

class PhysicsEngineSimpleV2(
    val screen: Screen,
    override val particleGroupsManager: ParticleGroupsManager,
    val attractionMatrix: AttractionMatrix,
    val changeables: Changeables,
) : PhysicsEngine {

    override fun update(deltaTime: Float) {
        updateAccelerationsForEach(deltaTime)
        updatePositions(deltaTime)
    }

    fun updateAccelerationsForEach(deltaTime: Float) {
        val friction = 0.5f.pow(deltaTime / changeables.frictionHalfTime.value)
        val rMax = changeables.rMax.value
        particleGroupsManager.particleGroups().forEach { group1 ->
            particleGroupsManager.particleGroups().forEach { group2 ->
                val attraction = attractionMatrix.force(group1.id, group2.id)
                group1.particles.map { particle1 ->
                    val force2D = Force2D()
                    group2.particles.forEach { particle2 ->
                        val rx = particle2.x - particle1.x
                        val ry = particle2.y - particle1.y
                        val r = hypot(rx, ry)
                        if (r > 0 && r < rMax) {
                            val force = force(r / rMax, attraction)
                            force2D.xForce += (rx / r * force)
                            force2D.yForce += (ry / r * force)
                        }
                    }
                    particle1.xAcc = particle1.xAcc * friction + force2D.xForce * rMax * deltaTime
                    particle1.yAcc =  particle1.yAcc * friction + force2D.yForce * rMax * deltaTime
                }
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

    private fun force(r: Float, a: Float): Float {
        val beta = changeables.beta.value
        return if (r < beta) {
            r / beta - 1f
        } else if (beta < r && r < 1f) {
            a * (1 - abs(2f * r - 1f - beta) / (1f - beta))
        } else {
            0f
        }
    }
}