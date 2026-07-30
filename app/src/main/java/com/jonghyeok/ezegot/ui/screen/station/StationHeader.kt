package com.jonghyeok.ezegot.ui.screen.station

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.ui.screen.SubwayLineIcon
import com.jonghyeok.ezegot.ui.theme.SubwayHeaderColors

/**
 * 역 상세 헤더. 배경이 그 역의 호선색이다.
 *
 * 예전 상단 영역은 220dp를 쓰면서 알람·전화·공유 액션바까지 얹혀 있었다.
 * 사용 빈도가 낮은 기능이 도착 정보보다 위에 있어서, 전화·공유는 ⋮ 메뉴로 내렸다.
 *
 * 배경과 글자색은 [colors]로 넘겨받는다. 헤더 안 가장 작은 글자가 11sp라
 * 4.5:1이 필요한데, 노선 원색 위 흰 글씨는 절반이 미달한다. 호출부가
 * 배경을 어둡게 보정하거나(색상·채도 유지) 밝은 노선은 검은 글자로 바꿔 넘긴다.
 * `subwayLineHeaderColors()` 참고.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StationHeader(
    stationName: String,
    lineNumber: String,
    colors: SubwayHeaderColors,
    isFavorite: Boolean,
    transferLines: List<String>,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCall: () -> Unit,
    onShare: () -> Unit,
    onTransferClick: (String) -> Unit
) {
    val onLine = colors.content
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(bottom = 14.dp)
    ) {
        // 상단 행 – 뒤로 / 별 / 더보기
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = onLine)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                    tint = onLine
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "더보기", tint = onLine)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("전화") },
                        leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                        onClick = { menuOpen = false; onCall() }
                    )
                    DropdownMenuItem(
                        text = { Text("공유") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = { menuOpen = false; onShare() }
                    )
                }
            }
        }

        // 역명 + 호선명
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stationName,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                color = onLine,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = lineNumber.removePrefix("0"),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = onLine.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }

        if (transferLines.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            // 환승이 3개인 역(왕십리 등)이 있어 한 줄에 담기지 않는다.
            // "환승" 라벨을 첫 항목으로 같은 흐름에 넣어 줄바꿈이 자연스럽게 되게 한다.
            FlowRow(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 아이콘이 26dp라 라벨을 세로 가운데에 맞춘다
                Box(
                    modifier = Modifier.height(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "환승",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = colors.content,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
                transferLines.forEach { name ->
                    SubwayLineIcon(
                        lineName = name,
                        // 헤더 배경이 노선색이라 아이콘이 묻힌다. 실측한 13개 환승 조합 중
                        // 11개가 3:1 미만이었다(왕십리 2호선 헤더 위 5호선은 1.10:1).
                        // 테두리를 예외가 아니라 기본으로 두르고, 색은 헤더 글자색을 쓴다.
                        // 그 색은 배경 대비 4.5:1이 보장되므로 어느 노선 위에서도 보인다.
                        borderColor = colors.content,
                        modifier = Modifier.clickable { onTransferClick(name) }
                    )
                }
            }
        }
    }
}

