package pl.game.core

import jdk.incubator.vector.FloatVector
import jdk.incubator.vector.VectorOperators
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class PhysicsEngineSimpleV1(
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
                    var forceVect = FloatVector.zero(FloatVector.SPECIES_PREFERRED)
                    group2.particles.forEach { particle2 ->
                        val rVector = particle2.posVect.sub(particle1.posVect)
                        val r = sqrt(rVector.pow(2f).reduceLanes(VectorOperators.ADD))
                        if (r > 0 && r < rMax) {
                            val force = force(r / rMax, attraction)
                            forceVect = forceVect.add(rVector.div(r * force))
                        }
                    }
                    particle1.accVect = particle1.accVect.mul(friction).add(forceVect.mul(rMax * deltaTime))
                }
            }
        }
    }

    fun updatePositions(deltaTime: Float) {
        particleGroupsManager.particleGroups().forEach { group ->
            group.particles.forEach { particle ->
                particle.posVect =
                    particle.posVect.add(particle.accVect.mul(deltaTime)).toArray().apply {
                        this[0].mod(screen.width.toFloat())
                        this[1].mod(screen.height.toFloat())
                    }.let {
                        FloatVector.fromArray(FloatVector.SPECIES_PREFERRED, it, 0)
                    }
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