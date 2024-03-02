package perf

import org.openjdk.jmh.Main
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import pl.game.core.Changeables
import pl.game.core.PhysicsEngineFactory
import pl.game.core.PhysicsEngineSimpleV1
import pl.game.core.Screen
import java.util.concurrent.TimeUnit


@Fork(value = 1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 30, timeUnit = TimeUnit.SECONDS)
open class PhysicsPerfTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Main()
        }
    }

//    val parallelChunking: PhysicsEngineParallelChunking = PhysicsEngineFactory.getSimplePC(Screen(500,500), Changeables())
//    val parallelChunkingCached: PhysicsEngineParallelChunkingCached = PhysicsEngineFactory.getSimplePCC(Screen(500,500), Changeables())
    val simpleV1: PhysicsEngineSimpleV1 = PhysicsEngineFactory.getSimpleV1(Screen(500,500), Changeables())
//    val simpleV2: PhysicsEngineSimpleV2 = PhysicsEngineFactory.getSimpleV2(Screen(500,500), Changeables())
//    val simpleV3: PhysicsEngineSimpleV3 = PhysicsEngineFactory.getSimpleV3(Screen(500,500), Changeables())
//    val parallel: PhysicsEngineParallel = PhysicsEngineFactory.getParallel(Screen(500,500), Changeables())

    @Benchmark
    fun simpleV1(blackhole: Blackhole) {
        simpleV1.updateAccelerationsForEach(0.1f)
    }
//    @Benchmark
//    fun simpleV2(blackhole: Blackhole) {
//        simpleV2.updateAccelerationsForEach(0.1f)
//    }
//    @Benchmark
//    fun simpleV3(blackhole: Blackhole) {
//        simpleV3.updateAccelerationsForEach(0.1f)
//    }
//    @Benchmark
//    fun parallel(blackhole: Blackhole) {
//        parallel.updateAccelerationsForEach(0.1f)
//    }

//    @Benchmark
//    fun parallelChunking(blackhole: Blackhole) {
//        parallelChunking.updateAccelerationsChunking(0.1f)
//    }
//
//    @Benchmark
//    fun parallelChunkingCached(blackhole: Blackhole) {
//        parallelChunkingCached.updateAccelerationsChunking(0.1f)
//    }
}

