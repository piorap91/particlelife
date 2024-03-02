package unit

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.floats.shouldBeGreaterThan
import io.kotest.matchers.floats.shouldBeLessThan
import pl.game.core.AttractionRules
import pl.game.core.MutableState
import pl.game.core.Color
import pl.game.core.Force2D
import pl.game.core.Particle
import pl.game.core.ParticleGroup

class AttractionRulesTest: ShouldSpec ({
    should("Force should move particle up right when attraction positive and distance > beta") {
        //given
        val attractionRules = AttractionRules(MutableState(20), MutableState(0.3f))
        val particleGroup = ParticleGroup(1, Color(1f,1f,1f))
        val particle1 = Particle(0f,0f, particleGroup = particleGroup)
        val particle2 = Particle(10f,10f, particleGroup = particleGroup)
        val attractionForce = 1f

        val force2D = Force2D()
        //when
        attractionRules.attract(particle1, particle2, attractionForce, force2D)
        //then
        force2D.xForce shouldBeGreaterThan 0f
        force2D.yForce shouldBeGreaterThan  0f
    }

    should("Force should move particle down left when attraction positive and distance < beta") {
        //given
        val attractionRules = AttractionRules(MutableState(200), MutableState(0.3f))
        val particleGroup = ParticleGroup(1, Color(1f,1f,1f))
        val particle1 = Particle(0f,0f, particleGroup = particleGroup)
        val particle2 = Particle(10f,10f, particleGroup = particleGroup)
        val attractionForce = 1f

        val force2D = Force2D()
        //when
        attractionRules.attract(particle1, particle2, attractionForce, force2D)
        //then
        force2D.xForce shouldBeLessThan  0f
        force2D.yForce shouldBeLessThan  0f
    }

    should("Force should move particle down left when attraction negative and distance > beta") {
        //given
        val attractionRules = AttractionRules(MutableState(20), MutableState(0.3f))
        val particleGroup = ParticleGroup(1, Color(1f,1f,1f))
        val particle1 = Particle(0f,0f, particleGroup = particleGroup)
        val particle2 = Particle(10f,10f, particleGroup = particleGroup)
        val attractionForce = -1f

        val force2D = Force2D()
        //when
        attractionRules.attract(particle1, particle2, attractionForce, force2D)
        //then
        force2D.xForce shouldBeLessThan 0f
        force2D.yForce shouldBeLessThan  0f
    }

    should("Force should move particle down left when attraction negative and distance < beta") {
        //given
        val attractionRules = AttractionRules(MutableState(200), MutableState(0.3f))
        val particleGroup = ParticleGroup(1, Color(1f,1f,1f))
        val particle1 = Particle(0f,0f, particleGroup = particleGroup)
        val particle2 = Particle(10f,10f, particleGroup = particleGroup)
        val attractionForce = -1f

        val force2D = Force2D()
        //when
        attractionRules.attract(particle1, particle2, attractionForce, force2D)
        //then
        force2D.xForce shouldBeLessThan  0f
        force2D.yForce shouldBeLessThan  0f
    }
})