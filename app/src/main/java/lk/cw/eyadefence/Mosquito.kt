package lk.cw.eyadefence

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import java.util.*
import android.graphics.BitmapFactory

class Mosquito(context: Context, row: Int, column: Int, screenX: Int, screenY: Int) {
    var width = screenX / 10f
    private var height = screenY / 20f
    private val padding = screenX / 60

    var position = RectF(
            column * (width + padding),
            100 + row * (width + padding / 4),
            column * (width + padding) + width,
            100 + row * (width + padding / 4) + height
    )

    private var speed = 40f

    private val left = 1
    private val right = 2

    private var shipMoving = right

    var isVisible = true

    companion object {
        lateinit var bitmap1: Bitmap
        lateinit var bitmap2: Bitmap

        var numberOfMosquitoes = 0
    }

    init {
        bitmap1 = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.invader1)

        bitmap2 = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.invader2)

        bitmap1 = Bitmap.createScaledBitmap(
                bitmap1,
                (width.toInt()),
                (height.toInt()),
                false)

        bitmap2 = Bitmap.createScaledBitmap(
                bitmap2,
                (width.toInt()),
                (height.toInt()),
                false)

        numberOfMosquitoes ++
    }

    fun update(fps: Long, screenX: Int) {
        // Ensure mosquito stays within screen boundaries
        if (shipMoving == left && position.left > 0) {
            position.left -= speed / fps
        } else if (shipMoving == right && position.right < screenX) {
            position.left += speed / fps
        }

        position.right = position.left + width
    }

    fun dropDownAndReverse(waveNumber: Int) {
        shipMoving = if (shipMoving == left) {
            right
        } else {
            left
        }


        position.top += height
        position.bottom += height

        speed *=  (1.1f + (waveNumber.toFloat() / 10))
    }

    fun takeAim(playerShipX: Float,
                playerShipLength: Float,
                waves: Int)
            : Boolean {

        val generator = Random()
        var randomNumber: Int

        // If near the player consider taking a shot
        if (playerShipX + playerShipLength > position.left &&
                playerShipX + playerShipLength < position.left + width ||
                playerShipX > position.left && playerShipX < position.left + width) {

            randomNumber = generator.nextInt((100 * numberOfMosquitoes) / waves)
            if (randomNumber == 0) {
                return true
            }

        }

        // If firing randomly (not near the player)
        randomNumber = generator.nextInt(150 * numberOfMosquitoes)
        return randomNumber == 0

    }
}
