package pl.game

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import pl.game.view.Simulation

/**
 * To Show
 * How it works
 *  game loop / libgdx
 *  rendering
 *  forces, friction
 *  chunking vs no chunking
 * Java
 *  gc - deltaT vs const deltaT
 *  java 8 vs java 21 - loop perf
 * Other
 *  % vs mod
 *  perf tests
 *  window dragging
 */

fun main() {
    val config = Lwjgl3ApplicationConfiguration()
    config.setForegroundFPS(60)
    config.setTitle("particle-life")
    config.setWindowedMode(1000, 1000)
    Lwjgl3Application(Simulation(), config)
}