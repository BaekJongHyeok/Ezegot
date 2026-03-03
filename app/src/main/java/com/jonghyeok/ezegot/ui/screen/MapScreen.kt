package com.jonghyeok.ezegot.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.jonghyeok.ezegot.ui.theme.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun MapScreen(
    onBack: () -> Unit,
    onSearchClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        hasPermission = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    currentLocation = latLng
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "위치 권한이 필요합니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }

        // 오버레이 – 상단 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Navy900.copy(alpha = 0.9f), Navy900.copy(alpha = 0f))))
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", tint = TextOnDark)
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onSearchClick?.invoke() },
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWhite,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "지하철 역 이름 검색",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextHint
                        )
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
