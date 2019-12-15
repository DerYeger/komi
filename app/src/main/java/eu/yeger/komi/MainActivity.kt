package eu.yeger.komi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.layout.Column
import androidx.ui.layout.ExpandedHeight
import androidx.ui.layout.HeightSpacer
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.text.TextStyle

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
        Column(modifier = ExpandedHeight) {
            CenteredRow {
                Text(
                    text = "Komi",
                    style = (+MaterialTheme.typography()).h1.merge(TextStyle(color = secondaryColor))
                )
            }
            HeightSpacer(height = 16.dp)
            CenteredRow {
                Button(
                    text = "Play",
                    onClick = { activity.startActivity(GameActivity::class) }
                )
            }
        }
    }
}
