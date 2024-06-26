package lk.cw.eyadefence

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.view.SurfaceView
import android.util.Log
import android.view.MotionEvent

class EyaDefenceGameView(context: Context,
                         private val size: Point)
    : SurfaceView(context),
        Runnable {
    
    private val gameThread = Thread(this)
    
    private var playing = false

    // Game is paused at the start
    private var paused = true

    private var canvas: Canvas = Canvas()
    private val paint: Paint = Paint()

    private var playerShip: PlayerShip = PlayerShip(context, size.x, size.y)

    private val mosquitoes = ArrayList<Mosquito>()
    private var numMosquitoes = 0

    private val bricks = ArrayList<DefenceBrick>()
    private var numBricks: Int = 0

    private var playerBullet = Bullet(size.y, 1200f, 40f)

    private val mosquitoesBullets = ArrayList<Bullet>()
    private var nextBullet = 0
    private val maxMosquitoBullets = 10

    private var score = 0
    private var finalScore = score

    private var waves = 1
    private val finalWaves = 3

    private var lives = 3

    // To remember the high score
    private val prefs = context.getSharedPreferences("Eya Defence", Context.MODE_PRIVATE)

    private var highScore =  prefs.getInt("highScore", 0)

    // The delay between flaps
    private var flapInterval: Long = 250

    // Which flap direction should draw next
    private var upOrDown: Boolean = false
    // When did we last play a menacing sound
    private var lastFlapTime = System.currentTimeMillis()

    private var backgroundImage: Bitmap? = null

    private fun prepareLevel() {
        // Build an army of mosquitoes
        loadBackgroundImage()
        Mosquito.numberOfMosquitoes = 0
        numMosquitoes = 0
        for (column in 0..5) {
            for (row in 0..2) {
                mosquitoes.add(Mosquito(context,
                        row,
                        column,
                        size.x,
                        size.y))

                numMosquitoes++
            }
        }

        // Build the pillow shelters
        numBricks = 0
        for (shelterNumber in 0..4) {
            for (column in 0..2) {
                for (row in 0..1) {
                    bricks.add(DefenceBrick(row,
                            column,
                            shelterNumber,
                            size.x,
                            size.y))

                    numBricks++
                }
            }
        }

        for (i in 0 until maxMosquitoBullets) {
            mosquitoesBullets.add(Bullet(size.y))
        }
    }

    override fun run() {
        var fps: Long = 0

        while (playing) {

            val startFrameTime = System.currentTimeMillis()

            // Update the frame
            if (!paused) {
                update(fps)
            }

            draw()

            val timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame
            }

            // Change flaps based on the flap interval
            if (!paused && ((startFrameTime - lastFlapTime) > flapInterval))
                flapDrawer()

        }
    }

    private fun flapDrawer() {
        // Reset the last flap time
        lastFlapTime = System.currentTimeMillis()
        // Alter value of upOrDown
        upOrDown = !upOrDown

    }

    private fun update(fps: Long) {
        playerShip.update(fps)

        var bumped = false

        var lost = false

        for (mosquito in mosquitoes) {

            if (mosquito.isVisible) {
                mosquito.update(fps, size.x)

                if (mosquito.takeAim(playerShip.position.left,
                                playerShip.width,
                                waves)) {

                    if (mosquitoesBullets[nextBullet].shoot(mosquito.position.left
                                    + mosquito.width / 2,
                                    mosquito.position.top, playerBullet.down)) {

                        nextBullet++

                        if (nextBullet == maxMosquitoBullets) {
                            nextBullet = 0
                        }
                    }
                }

                if (mosquito.position.left > size.x - mosquito.width
                        || mosquito.position.left < 0) {

                    bumped = true

                }
            }
        }

        // Update the players playerBullet
        if (playerBullet.isActive) {
            playerBullet.update(fps)
        }

        // Update all the mosquitoes bullets if active

        for (bullet in mosquitoesBullets) {
            if (bullet.isActive) {
                bullet.update(fps)
            }
        }

        // Did an mosquito bump into the edge of the screen
        if (bumped) {

            // Move all the mosquitoes down and change direction
            for (mosquito in mosquitoes) {
                mosquito.dropDownAndReverse(waves)
                // Have the mosquitoes landed
                if (mosquito.position.bottom >= size.y && mosquito.isVisible) {
                    lost = true
                }
            }
        }

        // Has the player's playerBullet hit the top of the screen
        if (playerBullet.position.bottom < 0) {
            playerBullet.isActive =false
        }

        // Has an mosquitoes playerBullet hit the bottom of the screen
        for (bullet in mosquitoesBullets) {
            if (bullet.position.top > size.y) {
                bullet.isActive = false
            }
        }

        // Has the player's playerBullet hit an mosquito
        if (playerBullet.isActive) {
            for (mosquito in mosquitoes) {
                if (mosquito.isVisible) {
                    if (RectF.intersects(playerBullet.position, mosquito.position)) {
                        mosquito.isVisible = false

                        playerBullet.isActive = false
                        Mosquito.numberOfMosquitoes --
                        score += 10
                        if(score > highScore){
                            highScore = score
                        }

                        // Has the player cleared the level
                        if (Mosquito.numberOfMosquitoes == 0) {
                            finalScore = score
                            paused = true
                            lives ++
                            mosquitoes.clear()
                            bricks.clear()
                            mosquitoesBullets.clear()
                            prepareLevel()
                            waves ++
                            if (waves > finalWaves) {
                                gameWin()
                            }
                            break
                        }

                        break
                    }
                }
            }
        }

        for (bullet in mosquitoesBullets) {
            if (bullet.isActive) {
                for (brick in bricks) {
                    if (brick.isVisible) {
                        if (RectF.intersects(bullet.position, brick.position)) {
                            // A collision has occurred
                            bullet.isActive = false
                            brick.isVisible = false
                        }
                    }
                }
            }

        }

        if (playerBullet.isActive) {
            for (brick in bricks) {
                if (brick.isVisible) {
                    if (RectF.intersects(playerBullet.position, brick.position)) {
                        // A collision has occurred
                        playerBullet.isActive = false
                        brick.isVisible = false
                    }
                }
            }
        }

        // Has an mosquito playerBullet hit the player
        for (bullet in mosquitoesBullets) {
            if (bullet.isActive) {
                if (RectF.intersects(playerShip.position, bullet.position)) {
                    bullet.isActive = false
                    lives --

                    // Is it game over?
                    if (lives == 0) {
                        lost = true
                        break
                    }
                }
            }
        }

        if (lost) {
            finalScore = score
            paused = true
            lives = 3
            score = 0
            waves = 1
            mosquitoes.clear()
            bricks.clear()
            mosquitoesBullets.clear()
            gameOver()
        }
    }

    private fun loadBackgroundImage() {
        val res = resources
        backgroundImage = BitmapFactory.decodeResource(res, R.drawable.background_image)
    }

    private fun draw() {
        if (holder.surface.isValid) {
            canvas = holder.lockCanvas()

            // Draw the background color
            canvas.drawColor(Color.argb(255, 178, 216, 255))

            val centerX = (canvas.width - backgroundImage!!.width) / 2f
            val centerY = (canvas.height - backgroundImage!!.height) / 2f

            canvas.drawBitmap(backgroundImage!!, centerX, centerY, null)

            paint.color = Color.argb(255, 124, 0, 0)

            // Draw all the game objects here
            canvas.drawBitmap(playerShip.bitmap, playerShip.position.left,
                    playerShip.position.top
                    , paint)

            // Draw the mosquitoes
            for (mosquito in mosquitoes) {
                if (mosquito.isVisible) {
                    if (upOrDown) {
                        canvas.drawBitmap(Mosquito.bitmap1,
                                mosquito.position.left,
                                mosquito.position.top,
                                paint)
                    } else {
                        canvas.drawBitmap(Mosquito.bitmap2,
                                mosquito.position.left,
                                mosquito.position.top,
                                paint)
                    }
                }
            }

            // Draw the bricks if visible
            for (brick in bricks) {
                if (brick.isVisible) {
                    canvas.drawRect(brick.position, paint)
                }
            }

            // Draw the players playerBullet if active
            if (playerBullet.isActive) {
                canvas.drawRect(playerBullet.position, paint)
            }

            // Draw the mosquitoes bullets
            for (bullet in mosquitoesBullets) {
                if (bullet.isActive) {
                    canvas.drawRect(bullet.position, paint)
                }
            }

            // Draw the score and remaining lives
            // Change the brush color
            paint.color = Color.argb(255, 0, 0, 0)
            paint.textSize = 40f
            canvas.drawText("දැනට ලකුණු: $score   ♥: $lives", 20f, 75f, paint)
            canvas.drawText("මදුරු ප්\u200Dරහාර: $waves/3  වැඩිම ලකුණු: $highScore", 20f, 160f, paint)

            // Draw everything to the screen
            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun pause() {
        playing = false
        try {
            gameThread.join()
        } catch (e: InterruptedException) {
            Log.e("Error:", "joining thread")
        }

        val oldHighScore = prefs.getInt("highScore", 0)

        if(highScore > oldHighScore) {
            val editor = prefs.edit()

            editor.putInt(
                    "highScore", highScore)

            editor.apply()
        }
    }


    fun resume() {
        playing = true
        prepareLevel()
        gameThread.start()
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        val motionArea = size.y - (size.y / 8)
        when (motionEvent.action and MotionEvent.ACTION_MASK) {

        // Player has touched the screen
        // Or moved their finger while touching screen
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE-> {
                paused = false

                if (motionEvent.y > motionArea) {
                    if (motionEvent.x > size.x / 2) {
                        playerShip.moving = PlayerShip.right
                    } else {
                        playerShip.moving = PlayerShip.left
                    }

                }

                if (motionEvent.y < motionArea) {
                    // Shots fired
                    if (playerBullet.shoot(
                                    playerShip.position.left + playerShip.width / 2f,
                                    playerShip.position.top,
                                    playerBullet.up)) {
                    }
                }
            }

        // Player has removed finger from screen
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP -> {
                if (motionEvent.y > motionArea) {
                    playerShip.moving = PlayerShip.stopped
                }
            }

        }
        return true
    }

    private fun gameOver() {
        val gameOverIntent = Intent(context, GameOverActivity::class.java)
        gameOverIntent.putExtra("finalScore", finalScore)
        Log.d("GameActivity", "Final Score: $finalScore")

        context.startActivity(gameOverIntent)
        (context as Activity).finish()
    }

    private fun gameWin() {
        val gameOverIntent = Intent(context, GameWinActivity::class.java)
        gameOverIntent.putExtra("finalScore", finalScore)
        Log.d("GameActivity", "Final Score: $finalScore")

        context.startActivity(gameOverIntent)
        (context as Activity).finish()
    }

}
