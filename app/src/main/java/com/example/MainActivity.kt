package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ruangai.ui.RuangAiApp
import com.example.ui.theme.RuangAiTheme
import com.example.ui.theme.WarmLinen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RuangAiTheme {
                Surface(
                    color = WarmLinen,
                    modifier = Modifier.fillMaxSize()
                ) {
                    RuangAiApp()
                }
            }
        }
    }
}
