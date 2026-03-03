package com.jonghyeok.ezegot.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.jonghyeok.ezegot.navigation.NavGraph
import com.jonghyeok.ezegot.ui.theme.EzegotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EzegotTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
