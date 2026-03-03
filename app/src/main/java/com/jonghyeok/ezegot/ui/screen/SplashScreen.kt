package com.jonghyeok.ezegot.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jonghyeok.ezegot.ui.theme.Navy700
import com.jonghyeok.ezegot.ui.theme.Navy900
import com.jonghyeok.ezegot.ui.theme.SkyBlue400
import com.jonghyeok.ezegot.ui.theme.TextOnDark
import com.jonghyeok.ezegot.viewModel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onFinished: () -> Unit
) {
    val isLoading by viewModel.loadingState.collectAsState()
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            delay(600)
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Navy900, Navy700))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            // 앱 아이콘 대체 텍스트 로고
            Text(
                text = "EZEGOT",
                style = MaterialTheme.typography.displayMedium,
                color = TextOnDark
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "실시간 지하철 정보",
                style = MaterialTheme.typography.titleMedium,
                color = SkyBlue400
            )
        }
    }
}
