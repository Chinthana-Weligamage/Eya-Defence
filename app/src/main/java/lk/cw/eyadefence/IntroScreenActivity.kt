package lk.cw.eyadefence

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class IntroScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_screen)

        val button = findViewById<Button>(R.id.button_Start_Game)

        button.setOnClickListener {
            val intent = Intent(this, EyaDefenceGameActivity::class.java)
            startActivity(intent)
        }

    }
}