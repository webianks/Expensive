package com.webianks.expensive

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val background: ImageView = findViewById(R.id.background)

        /*Glide.with(this)
            .load("https://source.unsplash.com/user/erondu/1300x600")
            .into(background)
*/

    }
}
