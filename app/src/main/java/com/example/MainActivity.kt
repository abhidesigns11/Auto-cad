package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.CadMainScreen
import com.example.ui.theme.CadCanvasBg
import com.example.ui.theme.SteelDraftTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SteelDraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CadCanvasBg
                ) {
                    CadMainScreen()
                }
            }
        }
    }
}
