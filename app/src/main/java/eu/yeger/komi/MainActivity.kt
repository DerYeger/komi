package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.ui.core.setContent
import androidx.ui.material.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainPage(this)
        }
    }
}

@Composable
fun MainPage(activity: AppCompatActivity) {
    ThemedPage {
        Button(
            text = "Play",
            onClick = { activity.startActivity(GameActivity::class) }
        )
    }
}


