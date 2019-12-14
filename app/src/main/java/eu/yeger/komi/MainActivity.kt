package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.setContent
import androidx.ui.layout.Align
import androidx.ui.layout.Column
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme

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
        Align(alignment = Alignment.TopCenter) {
            Column {
                Text(text = "Komi", style = (+MaterialTheme.typography()).h1)
                Button(
                    text = "Play",
                    onClick = { activity.startActivity(GameActivity::class) }
                )
            }
        }
    }
}
