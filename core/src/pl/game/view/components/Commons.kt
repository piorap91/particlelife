package pl.game.view.components

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

object Commons {

    fun initSkin(): Skin =
        Skin().apply {
            val white = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
                setColor(Color.WHITE)
                fill()
            }
            add("white", Texture(white))

            val gray = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
                setColor(Color.LIGHT_GRAY)
                fill()
            }
            add("gray", Texture(gray))

            add("sliderBackS", Texture(white))

            val red = Pixmap(1, 1, Pixmap.Format.RGBA8888)
            red.setColor(Color.RED)
            red.fill()
            add("knobS", Texture(red))

            add("tableBackground", TextureRegion(Texture(gray)))

            val bitmapFont = BitmapFont()
            add("default", bitmapFont)

            val textButtonStyle = TextButton.TextButtonStyle().apply {
                up = newDrawable("white", Color.DARK_GRAY)
                down = newDrawable("white", Color.DARK_GRAY)
                checked = newDrawable("white", Color.BLUE)
                over = newDrawable("white", Color.LIGHT_GRAY)
                font = getFont("default")
            }
            add("default", textButtonStyle)

            val sliderStyle = Slider.SliderStyle().apply {
                background = newDrawable("sliderBackS")
                knob = newDrawable("knobS")
                background.minHeight = 20f
                knob.minHeight = 20f
                knob.minWidth = 10f
            }
            add("default-horizontal", sliderStyle)

            val labelStyle = LabelStyle().apply {
                font = getFont("default")
                fontColor = Color.BLACK
            }
            add("default", labelStyle)

            val textFieldStyle = TextFieldStyle().apply {
                font = getFont("default")
                fontColor = Color.BLACK
            }
            add("default", textFieldStyle)
        }

    fun initUiContainer(stage: Stage, skin: Skin, tableBuilder: (Table) -> Unit): Container<Table> =
        Container<Table>().apply {
            setSize(Gdx.graphics.width * 0.25f, Gdx.graphics.height.toFloat())
            top().left().fillY()

            val table = Table().apply {
                setFillParent(false)
                left()
                top()
                pad(10f)
                background = TextureRegionDrawable(skin.getRegion("tableBackground"))
            }
            actor = table
            stage.addActor(this)
            tableBuilder(table)
        }

    fun Table.addNamedSlider(
        skin: Skin,
        name: String,
        default: Float = 0f,
        min: Float = -1f,
        max: Float = 1f,
        step: Float = 0.01f,
        onChange: (Float) -> Unit
    ) {
        val label = Label("$name:", skin)
        label.debug = true

        val input = TextField("", skin)
        input.debug = true

        val slider = Slider(min, max, step, false, skin)
        slider.value = default
        slider.debug = true
        input.text = slider.value.toString()

        slider.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                onChange(slider.value)
                input.text = slider.value.toString()
            }
        })

        input.addListener(object : ChangeListener() {
            var value: String = input.text
            override fun handle(event: Event): Boolean {
                if (event is InputEvent && event.type == Type.touchDown) {
                    slider.value = 0f
                    input.text = ""
                    value = ""
                }
                if (event is InputEvent && event.type == Type.exit) {
                    try {
                        val floatValue = value.toFloat()
                        onChange(floatValue)
                        slider.value = floatValue
                    } catch (ex: NumberFormatException) {
                        slider.value = 0f
                        onChange(0f)
                    }
                }
                return super.handle(event)
            }

            override fun changed(event: ChangeEvent, actor: Actor) {
                value = input.text
            }
        })

        row()
        add(Table().apply {
            left()
            add(label).left()
            add(input)
            row()
            add(slider).center()
        })
    }
}