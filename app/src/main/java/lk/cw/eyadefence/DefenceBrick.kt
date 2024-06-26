package lk.cw.eyadefence

import android.graphics.RectF


class DefenceBrick(row: Int, column: Int, shelterNumber: Int, screenX: Int, screenY: Int) {

    var isVisible = true

    private val width = screenX / 25
    private val height = screenY / 50

    private val brickPadding = 2

    // The number of shelters
    private val shelterPadding = screenX / 12f
    private val startHeight = screenY - screenY / 10f * 2f

    val position = RectF(column * width + brickPadding +
            shelterPadding * shelterNumber +
            shelterPadding + shelterPadding * shelterNumber,
            row * height + brickPadding + startHeight,
            column * width + width - brickPadding +
                    shelterPadding * shelterNumber +
                    shelterPadding + shelterPadding * shelterNumber,
            row * height + height - brickPadding + startHeight)
}