package com.example.jive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectButton.setOnClickListener {
            SpotifyService.connect(this) {
                val intent = Intent(this, PlayerActivity::class.java)
                startActivity(intent)
            }
        }
    }
}