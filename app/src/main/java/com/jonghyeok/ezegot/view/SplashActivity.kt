package com.jonghyeok.ezegot.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.modelFactory.SplashViewModelFactory
import com.jonghyeok.ezegot.repository.SplashRepository
import com.jonghyeok.ezegot.ui.theme.App_Background_Color
import com.jonghyeok.ezegot.ui.theme.Egegot_mkTheme
import com.jonghyeok.ezegot.viewModel.SplashViewModel

class SplashActivity : ComponentActivity() {
    private val viewModel: SplashViewModel by viewModels {
        SplashViewModelFactory(SplashRepository(SharedPreferenceManager(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Egegot_mkTheme {
                SplashScreen(viewModel)
            }
        }

        viewModel.fetchStations()
        viewModel.fetchStationsLocation()
    }
}

@Composable
fun SplashScreen(viewModel: SplashViewModel) {
    val context = LocalContext.current
    val isLoading = viewModel.loadingState.collectAsState().value
    val errorState = viewModel.errorState.collectAsState().value

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            val intent = if (errorState == null) {
                Intent(context, MainActivity::class.java)
            } else {
                Intent(context, MainActivity::class.java)
            }
            context.startActivity(intent)
            (context as SplashActivity).finish()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = App_Background_Color
    ) { }
}
