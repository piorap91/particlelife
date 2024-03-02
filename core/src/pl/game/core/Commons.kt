package pl.game.core

import jdk.incubator.vector.FloatVector
import kotlin.random.Random

data class Screen(
    val width: Int,
    val height: Int,
)

class MutableState<T>(
    var value: T
)

data class Changeables(
    val deltaT: MutableState<Float> = MutableState(0.2f),
    val frictionHalfTime: MutableState<Float> = MutableState(0.25f),
    val forceMultiplier: MutableState<Float> = MutableState(0.1f),
    val beta: MutableState<Float> = MutableState(0.3f),
    val rMax: MutableState<Int> = MutableState(100),
    val showUI: MutableState<Boolean> = MutableState(true)
)

class ParticleGroupsManager(
    private val particleGroups: MutableMap<Int, ParticleGroup> = mutableMapOf(),
    private val maxGroups: Int = 4,
) {
    private var values = particleGroups.values

    private var i: Int = 0
    fun addGroup(count: Int, screen: Screen) {
        synchronized(this) {
            if (i < maxGroups) {
                val groupId = i++
                particleGroups[groupId] = ParticleGroup.generate(groupId, count, screen, maxGroups)
                values = particleGroups.values
            }
        }
    }

    fun particleGroups(): Collection<ParticleGroup> = values

}

data class Color(
    val red: Float,
    val green: Float,
    val blue: Float
) {
    companion object {
        fun fromId(id: Int, maxId: Int): Color =
            fromHue(id.toFloat() / maxId, 1f, 1f)

        fun fromHue(hue: Float, saturation: Float, value: Float): Color {
            val hf = (hue - hue.toInt()) * 6.0f
            val ihf = hf.toInt()
            val f = hf - ihf
            val v = value
            val pv = value * (1.0f - saturation)
            val qv = value * (1.0f - saturation * f)
            val tv = value * (1.0f - saturation * (1.0f - f))

            return when (ihf) {
                0 -> Color(
                    red = v,
                    green = tv,
                    blue = pv,
                )

                1 -> Color(
                    red = qv,
                    green = v,
                    blue = pv,
                )

                2 -> Color(
                    red = pv,
                    green = v,
                    blue = tv,
                )

                3 -> Color(
                    red = pv,
                    green = qv,
                    blue = v,
                )

                4 -> Color(
                    red = tv,
                    green = pv,
                    blue = v,
                )

                5 -> Color(
                    red = v,
                    green = pv,
                    blue = qv,
                )

                else -> {
                    throw RuntimeException("WTF?")
                }
            }
        }
    }
}

data class ParticleGroup(
    val id: Int,
    val color: Color,
    val particles: MutableList<Particle> = mutableListOf()
) {
    companion object {
        fun generate(groupId: Int, count: Int, screen: Screen, maxGroups: Int): ParticleGroup {
            val particleGroup = ParticleGroup(groupId, Color.fromId(groupId, maxGroups))
            val particles = (0..<count).map {
                Particle.fromRandom(particleGroup, screen)
            }
            particleGroup.particles.addAll(particles)
            return particleGroup
        }
    }
}

class Particle(
    var posVect: FloatVector,
    var accVect: FloatVector,
    val particleGroup: ParticleGroup,
) {
    companion object {
        fun fromRandom(particleGroup: ParticleGroup, screen: Screen) =
            Particle(
                posVect = FloatVector.fromArray(FloatVector.SPECIES_PREFERRED, floatArrayOf(Random.nextFloat() * screen.width, Random.nextFloat() * screen.height), 0),
                accVect = FloatVector.zero(FloatVector.SPECIES_PREFERRED),
                particleGroup = particleGroup
            )
    }
}

data class AttractionMatrix(
    private val matrix: MutableList<MutableList<Float>> = mutableListOf(),
    private val forceMultiplier: MutableState<Float>
) {
    companion object {

        fun fromParticleGroups(
            particleGroupsManager: ParticleGroupsManager,
            forceMultiplier: MutableState<Float>
        ): AttractionMatrix {
            val matrixSize = particleGroupsManager.particleGroups().size
            return AttractionMatrix(
                MutableList(matrixSize) {
                    MutableList(matrixSize) { 0f }
                },
                forceMultiplier
            )
        }
    }

    fun force(groupId1: Int, groupId2: Int) =
        matrix[groupId1][groupId2] * forceMultiplier.value

    fun setForce(groupId1: Int, groupId2: Int, force: Float) {
        matrix[groupId1][groupId2] = force
    }
}