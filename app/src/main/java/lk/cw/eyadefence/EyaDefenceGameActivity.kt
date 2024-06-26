package lk.cw.eyadefence

import android.app.Activity
import android.graphics.Point
import android.os.Bundle

class EyaDefenceGameActivity : Activity() {

    private var eyaDefenceGameView: EyaDefenceGameView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val display = windowManager.defaultDisplay

        val size = Point()
        display.getSize(size)

        // Initialize gameView and set it as the view
        eyaDefenceGameView = EyaDefenceGameView(this, size)
        setContentView(eyaDefenceGameView)
    }

    // Executes when the player starts the game
    override fun onResume() {
        super.onResume()

        eyaDefenceGameView?.resume()
    }

    // Executes when the player quits the game
    override fun onPause() {
        super.onPause()

        eyaDefenceGameView?.pause()
    }
}
