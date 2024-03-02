package pl.game.core

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
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
    var x: Float,
    var y: Float,
    var xAcc: Float = 0f,
    var yAcc: Float = 0f,
    val particleGroup: ParticleGroup,
) {
    companion object {
        fun fromRandom(particleGroup: ParticleGroup, screen: Screen) =
            Particle(
                x = Random.nextFloat() * screen.width,
                y = Random.nextFloat() * screen.height,
                particleGroup = particleGroup
            )
    }

    fun applyForce(force2D: Force2D, friction: Float, rMax: MutableState<Int>, deltaTime: Float) {
        xAcc = xAcc * friction + force2D.xForce * rMax.value * deltaTime
        yAcc = yAcc * friction + force2D.yForce * rMax.value * deltaTime
    }

    fun move(deltaTime: Float, screen: Screen) {
        x = (x + xAcc * deltaTime).mod(screen.width.toFloat())
        y = (y + yAcc * deltaTime).mod(screen.height.toFloat())
    }

    fun copy(x: Float? = null, y: Float? = null) = Particle(
        x = x ?: this.x,
        y = y ?: this.y,
        xAcc = xAcc,
        yAcc = yAcc,
        particleGroup = particleGroup
    )
}

class Force2D(
    var xForce: Float = 0f,
    var yForce: Float = 0f,
)

typealias Attraction = (particle1: Particle, particle2: Particle, attractionForce: Float, force2D: Force2D) -> Unit

class AttractionRules(
    private val rMax: MutableState<Int>,
    private val beta: MutableState<Float>
) {
    val attract: Attraction = { particle1, particle2, attraction, force2D ->
        val rx = particle2.x - particle1.x
        val ry = particle2.y - particle1.y
        val r = hypot(rx, ry)
        if (r > 0 && r < rMax.value) {
            val force = force(r / rMax.value, attraction)
            force2D.xForce += (rx / r * force)
            force2D.yForce += (ry / r * force)
        }
    }


    private fun force(r: Float, a: Float): Float {
        return if (r < beta.value) {
            r / beta.value - 1f
        } else if (beta.value < r && r < 1f) {
            a * (1 - abs(2f * r - 1f - beta.value) / (1f - beta.value))
        } else {
            0f
        }
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

class ChunkManager(
    private val matrix: MutableList<MutableList<MutableList<Particle>>> = mutableListOf(),
    private val rMax: Int,
    private val chunkWidthSize: Int,
    private val chunkHeightSize: Int,
    private val screen: Screen,
) {
    companion object {
        fun forRmax(rMax: Int, screen: Screen): ChunkManager {
            val width = coordinateSize(screen.width, rMax)
            val height = coordinateSize(screen.height, rMax)
            return ChunkManager(
                MutableList(width) {
                    MutableList(height) {
                        mutableListOf()
                    }
                },
                rMax,
                width,
                height,
                screen
            )
        }

        private fun coordinateSize(position: Int, rMax: Int) = ceil(position / rMax.toFloat()).toInt()
        private fun coordinate(position: Float, rMax: Int) = floor(position / rMax.toFloat()).toInt()
    }

    fun neighbourParticlesFor(particle: Particle): List<Particle> {
        val chunkX = coordinate(particle.x, rMax)
        val chunkY = coordinate(particle.y, rMax)

        return chunkWithTranslation(chunkX - 1, chunkY + 1) +
                chunkWithTranslation(chunkX, chunkY + 1) +
                chunkWithTranslation(chunkX + 1, chunkY + 1) +
                chunkWithTranslation(chunkX - 1, chunkY) +
                chunkWithTranslation(chunkX, chunkY) +
                chunkWithTranslation(chunkX + 1, chunkY) +
                chunkWithTranslation(chunkX - 1, chunkY - 1) +
                chunkWithTranslation(chunkX, chunkY - 1) +
                chunkWithTranslation(chunkX + 1, chunkY - 1)

    }

    private fun chunkWithTranslation(chunkX: Int, chunkY: Int): List<Particle> {
        val particles = matrix[chunkX.mod(chunkWidthSize)][chunkY.mod(chunkHeightSize)]
        return (translation(chunkX, chunkY, particles))
    }

    private fun translation(
        chunkX: Int,
        chunkY: Int,
        particles: List<Particle>
    ) = when {
        chunkX > chunkWidthSize && chunkY > chunkHeightSize -> {
            particles.map {
                it.copy(
                    x = it.x + screen.width,
                    y = it.y + screen.height,
                )
            }
        }

        chunkX < 0 && chunkY > chunkHeightSize -> {
            particles.map {
                it.copy(
                    x = it.x - screen.width,
                    y = it.y + screen.height,
                )
            }
        }

        chunkX < 0 && chunkY < 0 -> {
            particles.map {
                it.copy(
                    x = it.x - screen.width,
                    y = it.y - screen.height,
                )
            }
        }

        chunkX > chunkHeightSize && chunkY < 0 -> {
            particles.map {
                it.copy(
                    x = it.x + screen.width,
                    y = it.y - screen.height,
                )
            }
        }

        chunkX > chunkHeightSize -> {
            particles.map {
                it.copy(
                    x = it.x + screen.width,
                )
            }
        }

        chunkX < 0 -> {
            particles.map {
                it.copy(
                    x = it.x - screen.width,
                )
            }
        }

        chunkY > chunkHeightSize -> {
            particles.map {
                it.copy(
                    y = it.y + screen.height,
                )
            }
        }

        chunkY < 0 -> {
            particles.map {
                it.copy(
                    y = it.y - screen.height,
                )
            }
        }

        else -> {
            particles
        }
    }

    fun reset() {
        matrix.forEach { rows ->
            rows.forEach { particles ->
                particles.clear()
            }
        }
    }

    fun add(particle: Particle) {
        val x = coordinate(particle.x, rMax)
        val y = coordinate(particle.y, rMax)
        try {
            matrix[x][y].add(particle)
        } catch (ex: RuntimeException) {
            matrix[x.mod(chunkWidthSize)][y.mod(chunkHeightSize)].add(particle)
        }

    }
}

class ChunkManagerCached(
    private val matrix: MutableList<MutableList<MutableList<Particle>>> = mutableListOf(),
    private val rMax: Int,
    private val chunkWidthSize: Int,
    private val chunkHeightSize: Int,
    private val screen: Screen,
) {
    companion object {
        fun forRmax(rMax: Int, screen: Screen): ChunkManagerCached {
            val width = coordinateSize(screen.width, rMax)
            val height = coordinateSize(screen.height, rMax)
            return ChunkManagerCached(
                MutableList(width) {
                    MutableList(height) {
                        mutableListOf()
                    }
                },
                rMax,
                width,
                height,
                screen
            )
        }

        private fun coordinateSize(position: Int, rMax: Int) = ceil(position / rMax.toFloat()).toInt()
        private fun coordinate(position: Float, rMax: Int) = floor(position / rMax.toFloat()).toInt()
    }

    private val neighbourCache = mutableMapOf<Pair<Int, Int>, List<Particle>>()
    fun neighbourParticlesFor(particle: Particle): List<Particle> {
        val chunkX = coordinate(particle.x, rMax)
        val chunkY = coordinate(particle.y, rMax)
        return neighbourCache.getOrPut(chunkX to chunkY) {
            chunkWithTranslation(chunkX - 1, chunkY + 1) +
                    chunkWithTranslation(chunkX, chunkY + 1) +
                    chunkWithTranslation(chunkX + 1, chunkY + 1) +
                    chunkWithTranslation(chunkX - 1, chunkY) +
                    chunkWithTranslation(chunkX, chunkY) +
                    chunkWithTranslation(chunkX + 1, chunkY) +
                    chunkWithTranslation(chunkX - 1, chunkY - 1) +
                    chunkWithTranslation(chunkX, chunkY - 1) +
                    chunkWithTranslation(chunkX + 1, chunkY - 1)
        }
    }

    private val chunkCache = mutableMapOf<Pair<Int, Int>, List<Particle>>()
    private fun chunkWithTranslation(chunkX: Int, chunkY: Int): List<Particle> {
        return chunkCache.getOrPut(chunkX to chunkY) {
            val particles = matrix[chunkX.mod(chunkWidthSize)][chunkY.mod(chunkHeightSize)]
            return (translation(chunkX, chunkY, particles))
        }
    }

    private fun translation(
        chunkX: Int,
        chunkY: Int,
        particles: List<Particle>
    ) = when {
        chunkX > chunkWidthSize && chunkY > chunkHeightSize -> {
            particles.map {
                it.copy(
                    x = it.x + screen.width,
                    y = it.y + screen.height,
                )
            }
        }

        chunkX < 0 && chunkY > chunkHeightSize -> {
            particles.map {
                it.copy(
                    x = it.x - screen.width,
                    y = it.y + screen.height,
                )
            }
        }

        chunkX < 0 && chunkY < 0 -> {
            particles.map {
                it.copy(
                    x = it.x - screen.width,
                    y = it.y - screen.height,
                )
            }
        }

        chunkX > chunkHeightSize && chunkY < 0 -> {
            particles.map {
                it.copy(
                    x = it.x + screen.width,
                    y = it.y - screen.height,
                )
            }
        }

        chunkX > chunkHeightSize -> {
            particles.map {
                it.copy(
                    x = it.x + screen.width,
                )
            }
        }

        chunkX < 0 -> {
            particles.map {
                it.copy(
                    x = it.x - screen.width,
                )
            }
        }

        chunkY > chunkHeightSize -> {
            particles.map {
                it.copy(
                    y = it.y + screen.height,
                )
            }
        }

        chunkY < 0 -> {
            particles.map {
                it.copy(
                    y = it.y - screen.height,
                )
            }
        }

        else -> {
            particles
        }
    }

    fun reset() {
        neighbourCache.clear()
        chunkCache.clear()
        matrix.forEach { rows ->
            rows.forEach { particles ->
                particles.clear()
            }
        }
    }

    fun add(particle: Particle) {
        val x = coordinate(particle.x, rMax)
        val y = coordinate(particle.y, rMax)
        try {
            matrix[x][y].add(particle)
        } catch (ex: RuntimeException) {
            matrix[x.mod(chunkWidthSize)][y.mod(chunkHeightSize)].add(particle)
        }

    }
}