package lk.cw.eyadefence

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class GameOverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        // Retrieve final score from intent
        val finalScore = intent.getIntExtra("finalScore", 0)

        // Display final score
        val finalScoreTextView = findViewById<TextView>(R.id.final_score_text_view)
        finalScoreTextView.text = "Final Score: $finalScore"

        val button = findViewById<Button>(R.id.button_Try_Again)

        button.setOnClickListener {
            val intent = Intent(this, IntroScreenActivity::class.java)
            startActivity(intent)
        }

    }
}
