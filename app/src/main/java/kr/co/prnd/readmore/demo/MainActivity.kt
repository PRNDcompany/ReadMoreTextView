package kr.co.prnd.readmore.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kr.co.prnd.readmore.ReadMoreTextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val readMoreTextView: ReadMoreTextView = findViewById(R.id.readMoreTextView)
        readMoreTextView.changeListener = object : ReadMoreTextView.ChangeListener {
            override fun onStateChange(state: ReadMoreTextView.State) {
                Log.d("prnd", "state: $state")
            }
        }

    }
}