package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.ruangai.di.DefaultAppContainer
import com.example.ruangai.di.RuangAiViewModelFactory
import com.example.ruangai.ui.RuangAiApp
import com.example.ruangai.ui.viewmodel.RuangAiViewModel
import com.example.ui.theme.RuangAiTheme
import com.example.ui.theme.WarmLinen

class MainActivity : ComponentActivity() {

    private val appContainer by lazy { DefaultAppContainer(applicationContext) }
    private val viewModel: RuangAiViewModel by lazy {
        ViewModelProvider(this, RuangAiViewModelFactory(appContainer))[RuangAiViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent?.data?.let { uri ->
            viewModel.handleDeepLink(uri)
        }

        setContent {
            RuangAiTheme {
                Surface(
                    color = WarmLinen,
                    modifier = Modifier.fillMaxSize()
                ) {
                    RuangAiApp(container = appContainer, viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            viewModel.handleDeepLink(uri)
        }
    }
}
