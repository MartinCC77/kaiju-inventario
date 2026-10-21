package cl.duoc.kaiju

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.kaiju.ui.screen.ProductoScreen
import cl.duoc.kaiju.ui.theme.KaijuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaijuTheme {
                ProductoScreen()
            }
        }
    }
}