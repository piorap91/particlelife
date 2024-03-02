package pl.game.view

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.ScreenUtils
import pl.game.core.Changeables
import pl.game.core.PhysicsEngine
import pl.game.core.PhysicsEngineFactory
import pl.game.core.Screen
import pl.game.view.components.Commons
import pl.game.view.components.Commons.addNamedSlider
import kotlin.time.measureTime


class Simulation : ApplicationAdapter() {

    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var spriteBatch: SpriteBatch

    private lateinit var stage: Stage
    private lateinit var skin: Skin
    private lateinit var tableContainer: Container<Table>

    private lateinit var physicsEngine: PhysicsEngine
    private lateinit var screen: Screen
    private val changeables: Changeables = Changeables()


    override fun create() {
        shapeRenderer = ShapeRenderer()
        spriteBatch = SpriteBatch()

        stage = Stage()
        Gdx.input.inputProcessor = stage

        skin = Commons.initSkin()

        tableContainer = Commons.initUiContainer(stage, skin) { table ->
            table.debug = true

            table.addNamedSlider(skin, "DeltaTime", changeables.deltaT.value ) { value ->
                changeables.deltaT.value = value
            }
            table.addNamedSlider(skin, "frictionHalfTime", changeables.frictionHalfTime.value, 0f, 10f) { value ->
                changeables.frictionHalfTime.value = value
            }
            table.addNamedSlider(skin, "forceMultipier", changeables.forceMultiplier.value, 0f, 5f) { value ->
                changeables.forceMultiplier.value = value
            }
            table.addNamedSlider(skin, "beta", changeables.beta.value, 0f) { value ->
                changeables.beta.value = value
            }
            table.addNamedSlider(skin, "rMax", changeables.rMax.value.toFloat(), 1f, 1000f, 1f) { value ->
                changeables.rMax.value = value.toInt()
            }
//            table.addMatrix(skin, changeables.attractionMatrix, 1f,1f, 0.01f)
        }


//        val button = TextButton("Click me!", skin)
//        table.add(button)
//
//        button.addListener(object : ChangeListener() {
//            override fun changed(event: ChangeEvent, actor: Actor) {
//                println("Clicked! Is checked: " + button.isChecked)
//                button.setText("Good job!")
//                event.cancel()
//            }
//        })

        screen = Screen(Gdx.graphics.width / 2, Gdx.graphics.height / 2)
        physicsEngine = PhysicsEngineFactory.getSimplePCC(screen, changeables)
    }


    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        shapeRenderer(ShapeType.Filled) {
            drawParticles()
            drawParticlesAround()
        }

        stage.act(Gdx.graphics.deltaTime)
        stage.draw()

        spriteBatch {
            drawFPS()
        }

//        physicsEngine.update(Gdx.graphics.deltaTime)
        println(
            "TIME: " +
                    measureTime {
                        physicsEngine.update(changeables.deltaT.value * Gdx.graphics.deltaTime)
//                    physicsEngine.update(0.02f)
                    }
        )
    }

    private fun drawParticles() {
        val xShift = screen.width / 2f
        val yShift = screen.height / 2f
        val groups = physicsEngine.particleGroupsManager.particleGroups()
        groups.forEach { group ->
            val color = group.color
            shapeRenderer.setColor(color.red, color.green, color.blue, 1f)
            group.particles.forEach { particle ->
                shapeRenderer.circle(particle.x + xShift, particle.y + yShift, 2f)
            }
        }
    }

    private fun drawParticlesAround() {
        drawParticlesShifted(-1, 0)
        drawParticlesShifted(1, 0)
        drawParticlesShifted(0, -1)
        drawParticlesShifted(0, 1)
        drawParticlesShifted(1, 1)
        drawParticlesShifted(-1, 1)
        drawParticlesShifted(1, -1)
        drawParticlesShifted(-1, -1)
    }

    private fun drawParticlesShifted(xScreensShift: Int, yScreensShift: Int) {
        val xShift = screen.width / 2f + screen.width * xScreensShift
        val yShift = screen.height / 2f + screen.height * yScreensShift
        val groups = physicsEngine.particleGroupsManager.particleGroups()
        groups.forEach { group ->
            shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f)
            group.particles.forEach { particle ->
                shapeRenderer.circle(particle.x + xShift, particle.y + yShift, 2f)
            }
        }
    }

    private fun drawFPS() {
        val font = skin.getFont("default")
        font.setColor(0f, 1f, 0f, 1f)
        font.draw(spriteBatch, "FPS: ${Gdx.graphics.framesPerSecond}", 10f, 20f)
    }

    override fun dispose() {
        shapeRenderer.dispose()
        spriteBatch.dispose()

        stage.dispose()
        skin.dispose()
    }

//    override fun keyDown(keycode: Int) = false
//
//    override fun keyUp(keycode: Int) = false
//
//    override fun keyTyped(p0: Char) = p0.also { println("CHAR: $p0") }.takeIf { it == ' ' }?.let {
//        changeables.showUI.value = !changeables.showUI.value
//        true
//    } ?: false
//
//    override fun touchDown(p0: Int, p1: Int, p2: Int, p3: Int) = false
//
//    override fun touchUp(p0: Int, p1: Int, p2: Int, p3: Int) = false
//
//    override fun touchCancelled(p0: Int, p1: Int, p2: Int, p3: Int) = false
//
//    override fun touchDragged(p0: Int, p1: Int, p2: Int) = false
//
//    override fun mouseMoved(p0: Int, p1: Int) = false
//
//    override fun scrolled(p0: Float, p1: Float) = false


    private fun shapeRenderer(shapeType: ShapeType, function: () -> Unit) {
        shapeRenderer.begin(shapeType)
        function()
        shapeRenderer.end()
    }

    private fun spriteBatch(function: () -> Unit) {
        spriteBatch.begin()
        function()
        spriteBatch.end()
    }
}



