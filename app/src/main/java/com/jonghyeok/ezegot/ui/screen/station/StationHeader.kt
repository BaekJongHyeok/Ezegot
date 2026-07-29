package com.jonghyeok.ezegot.ui.screen.station

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

/**
 * 역 상세 헤더. 배경이 그 역의 호선색이다.
 *
 * 예전 상단 영역은 220dp를 쓰면서 알람·전화·공유 액션바까지 얹혀 있었다.
 * 사용 빈도가 낮은 기능이 도착 정보보다 위에 있어서, 전화·공유는 ⋮ 메뉴로 내렸다.
 *
 * 글자색은 [onLine]으로 넘겨받는다. 노선색이 밝으면(수인분당선 등) 흰 글씨가
 * 읽히지 않아 호출부가 휘도로 골라준다.
 */
@Composable
internal fun StationHeader(
    stationName: String,
    lineNumber: String,
    lineColor: Color,
    onLine: Color,
    isAnyDirectionFavorite: Boolean,
    transferLines: List<String>,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCall: () -> Unit,
    onShare: () -> Unit,
    onTransferClick: (String) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(lineColor)
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
                    imageVector = if (isAnyDirectionFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isAnyDirectionFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
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
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                transferLines.forEach { name ->
                    HeaderChip(
                        text = "환승 $name",
                        onLine = onLine,
                        onClick = { onTransferClick(name) }
                    )
                }
            }
        }
    }
}

/** 헤더 위 반투명 칩. 배경이 노선색이라 흰 계열을 alpha로 얹는다 */
@Composable
private fun HeaderChip(text: String, onLine: Color, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(onLine.copy(alpha = 0.22f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = onLine,
            maxLines = 1
        )
    }
}

