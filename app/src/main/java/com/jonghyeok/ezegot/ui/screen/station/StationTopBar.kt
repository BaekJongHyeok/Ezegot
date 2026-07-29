package com.jonghyeok.ezegot.ui.screen.station

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.ui.theme.getSubwayLineColor
import com.jonghyeok.ezegot.ui.theme.onSubwayLineColor

/**
 * 역 상세 상단 바.
 *
 * 뒤로가기 · 즐겨찾기 토글 · 역명 · 호선 뱃지 · 환승 노선 아이콘을 표시한다.
 * 환승 노선은 실시간 도착 응답의 `subwayList`에서 현재 호선을 뺀 값이다.
 */
@Composable
internal fun StationTopBar(
    stationName: String,
    lineNumber: String,
    arrivalInfo: List<RealtimeArrival>,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onStationClick: (String, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(bottom = 48.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "즐겨찾기",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stationName,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    val lineColor = getSubwayLineColor(lineNumber)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = lineColor
                    ) {
                        Text(
                            text = lineNumber.removePrefix("0"),
                            style = MaterialTheme.typography.labelLarge,
                            color = onSubwayLineColor(lineColor),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                // 환승 정보
                val currentLineId = SubwayLine.getLineId(lineNumber)
                val transferLines = remember(arrivalInfo) {
                    arrivalInfo.firstOrNull()?.subwayList?.split(",")?.filter { it.isNotBlank() && it != currentLineId } ?: emptyList<String>()
                }
                if (transferLines.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "환승 노선",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            transferLines.forEach { id ->
                                val lineImageRes = SubwayLine.getLineImageById(id)
                                val name = SubwayLine.getLineName(id) ?: id
                                Image(
                                    painter = painterResource(id = lineImageRes),
                                    contentDescription = name,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { onStationClick(stationName, name) }
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
