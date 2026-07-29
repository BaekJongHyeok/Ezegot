package com.jonghyeok.ezegot.ui.screen.station

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.ui.theme.DividerColor
import com.jonghyeok.ezegot.ui.theme.SkyBlue400
import com.jonghyeok.ezegot.ui.theme.SurfaceWhite
import com.jonghyeok.ezegot.ui.theme.TextSecondary
import com.jonghyeok.ezegot.viewModel.StationViewModel

/** 역 상세 화면의 알람 / 전화 / 공유 액션 바. 상단 바 아래에 겹쳐 배치된다. */
@Composable
internal fun StationActionBar(
    isFavorite: Boolean,
    isNotification: Boolean,
    viewModel: StationViewModel,
    stationInfo: BasicStationInfo?,
    stationLocation: StationInfoResponse?,
    context: Context,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 6.dp,
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 알람
            ActionItem(
                icon = {
                    Icon(
                        imageVector = if (isNotification) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                        contentDescription = null,   // 아래 label "알람"이 읽히므로 중복을 피한다
                        tint = if (isNotification) SkyBlue400 else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = "알람",
                onClick = { viewModel.toggleNotification() }
            )
            VerticalDivider(modifier = Modifier.height(32.dp), thickness = 1.dp, color = DividerColor)
            // 전화
            ActionItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,   // 아래 label "전화"가 읽힌다
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = "전화",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:15447788")
                    }
                    context.startActivity(intent)
                }
            )
            VerticalDivider(modifier = Modifier.height(32.dp), thickness = 1.dp, color = DividerColor)
            // 공유
            ActionItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,   // 아래 label "공유"가 읽힌다
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = "공유",
                onClick = {
                    val stationName = stationInfo?.stationName ?: ""
                    val lineNumber = stationInfo?.lineNumber ?: ""

                    val textBuilder = StringBuilder().apply {
                        appendLine("[Ezegot - 지하철 정보 도우미]")
                        appendLine("🚇 $lineNumber ${stationName}역")

                        stationLocation?.let { loc ->
                            appendLine()
                            val address = loc.address ?: "주소 정보 없음"
                            appendLine("📍 위치: $address")
                            appendLine("🗺️ 지도에서 확인하기: https://www.google.com/maps/search/?api=1&query=${loc.latitude},${loc.longitude}")
                        }
                    }
                    val text = textBuilder.toString().trim()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(intent, "공유하기"))
                }
            )
        }
    }
}

@Composable
private fun ActionItem(icon: @Composable () -> Unit, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
