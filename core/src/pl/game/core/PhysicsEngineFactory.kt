package pl.game.core

object PhysicsEngineFactory {

    fun getSimpleV1(screen: Screen, changeables: Changeables): PhysicsEngineSimpleV1 {

        val (particleGroupsManager, attractionMatrix) = generateParticles(screen, changeables)

        return PhysicsEngineSimpleV1(screen, particleGroupsManager, attractionMatrix, changeables)
    }

    private fun generateParticles(
        screen: Screen,
        changeables: Changeables
    ): Pair<ParticleGroupsManager, AttractionMatrix> {
        val particleGroupsManager = ParticleGroupsManager().apply {
            addGroup(500, screen)
            addGroup(500, screen)
            addGroup(500, screen)
            addGroup(500, screen)

        }
        val attractionMatrix =
            AttractionMatrix.fromParticleGroups(particleGroupsManager, changeables.forceMultiplier).apply {
                setForce(0, 0, 1f)
                setForce(1, 0, 1.6f)
                setForce(0, 1, -0.5f)
                setForce(2, 1, -0.5f)
                setForce(3, 1, -0.25f)
                setForce(2, 0, 0.5f)
                setForce(2, 2, 1f)
                setForce(1, 3, 0.5f)
                setForce(3, 2, 1f)
            }
        return particleGroupsManager to attractionMatrix
    }
}