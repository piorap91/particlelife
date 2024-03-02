package pl.game.core

object PhysicsEngineFactory {

    fun getSimplePC(screen: Screen, changeables: Changeables): PhysicsEngineParallelChunking {

        val (particleGroupsManager, attractionMatrix) = generateParticles(screen, changeables)

        val attractionRules = AttractionRules(changeables.rMax, changeables.beta)
        //todo check
        val chunkManager = ChunkManager.forRmax(changeables.rMax.value, screen)
        return PhysicsEngineParallelChunking(screen, particleGroupsManager, attractionMatrix, chunkManager, changeables, attractionRules.attract)
    }

    fun getSimplePCC(screen: Screen, changeables: Changeables): PhysicsEngineParallelChunkingCached {

        val (particleGroupsManager, attractionMatrix) = generateParticles(screen, changeables)

        val attractionRules = AttractionRules(changeables.rMax, changeables.beta)
        //todo check
        val chunkManager = ChunkManagerCached.forRmax(changeables.rMax.value, screen)
        return PhysicsEngineParallelChunkingCached(screen, particleGroupsManager, attractionMatrix, chunkManager, changeables, attractionRules.attract)
    }

    fun getSimpleV1(screen: Screen, changeables: Changeables): PhysicsEngineSimpleV1 {

        val (particleGroupsManager, attractionMatrix) = generateParticles(screen, changeables)

        return PhysicsEngineSimpleV1(screen, particleGroupsManager, attractionMatrix, changeables)
    }

    fun getSimpleV2(screen: Screen, changeables: Changeables): PhysicsEngineSimpleV2 {

        val (particleGroupsManager, attractionMatrix) = generateParticles(screen, changeables)

        return PhysicsEngineSimpleV2(screen, particleGroupsManager, attractionMatrix, changeables)
    }

    fun getSimpleV3(screen: Screen, changeables: Changeables): PhysicsEngineSimpleV3 {

        val (particleGroupsManager, attractionMatrix) = generateParticles(screen, changeables)

        val attractionRules = AttractionRules(changeables.rMax, changeables.beta)
        return PhysicsEngineSimpleV3(screen, particleGroupsManager, attractionMatrix, changeables, attractionRules.attract)
    }

    fun getParallel(screen: Screen, changeables: Changeables): PhysicsEngineParallel {

        val (particleGroupsManager, attractionMatrix) = generateParticles(screen, changeables)

        val attractionRules = AttractionRules(changeables.rMax, changeables.beta)
        return PhysicsEngineParallel(screen, particleGroupsManager, attractionMatrix, changeables, attractionRules.attract)
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