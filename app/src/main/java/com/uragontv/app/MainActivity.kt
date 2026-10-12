package com.uragontv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.uragontv.app.ui.navigation.UragonTvMain
import com.uragontv.app.ui.theme.ObsidianBlack
import com.uragontv.app.ui.theme.UragonTvTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as UragonTvApp).repository

        setContent {
            UragonTvTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBlack
                ) {
                    UragonTvMain(repository = repository)
                }
            }
        }
    }
}
