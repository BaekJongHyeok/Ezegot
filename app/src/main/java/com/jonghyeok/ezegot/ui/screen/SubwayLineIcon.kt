package com.jonghyeok.ezegot.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.ui.theme.getSubwayLineColor
import com.jonghyeok.ezegot.ui.theme.onSubwayLineColor

/**
 * 호선 아이콘. 홈·역 상세·검색·알림이 함께 쓴다.
 *
 * 서울 지하철 표기 관행대로 원형에 노선색을 채운다. 예전에는 둥근 사각형에
 * 노선색을 12%로 연하게 깔고 진한 글씨를 얹었는데, 실제 노선 표기와 달라
 * 무엇을 뜻하는지 한눈에 들어오지 않았다.
 *
 * 다만 관행인 **흰 숫자는 쓰지 않는다.** 9개 숫자 노선 중 8개가 흰 글씨로는
 * 4.5:1에 미치지 못한다(2호선 3.56, 9호선 2.15). 실물 표지판은 조명과 크기,
 * 인쇄 매체를 전제로 한 것이라 26dp 화면 아이콘에는 그대로 적용되지 않는다.
 * 채움색은 공식 원색을 그대로 두고 글자색만 휘도로 고른다.
 *
 * 이름 노선은 원에 이름을 다 넣을 수 없어 1~2자로 줄인다. [toLineIconLabel] 참고.
 */
@Composable
fun SubwayLineIcon(
    lineName: String,
    modifier: Modifier = Modifier,
    size: Dp = LINE_ICON_SIZE,
    // 노선색 배경 위에 얹을 때는 테두리를 둘러 아이콘이 묻히지 않게 한다
    borderColor: Color? = null
) {
    val fill = getSubwayLineColor(lineName)
    val label = lineName.toLineIconLabel()
    // 숫자는 한 글자라 크게, 이름은 두 글자가 들어가야 해서 작게
    val fontSize = if (label.length <= 1) 13.sp else 10.sp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(fill)
            .then(
                if (borderColor != null) Modifier.border(1.5.dp, borderColor, CircleShape)
                else Modifier
            )
            // 원 안의 "2"는 스크린리더에 그대로 읽히면 뜻이 통하지 않는다
            .clearAndSetSemantics { contentDescription = lineName },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = fontSize,
                letterSpacing = 0.sp
            ),
            color = onSubwayLineColor(fill),
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

/**
 * 원 안에 넣을 1~2자.
 *
 * 숫자 노선은 숫자만("02호선" → "2"). 이름 노선은 원의 지름이 26dp라
 * 두 자가 한계다.
 *
 * 공항철도를 "A"로 줄이면 GTX-A와 겹친다. 둘 다 A로 시작하는 유일한 충돌이라
 * 공항철도는 "공항", GTX-A만 "A"로 둔다.
 */
fun String.toLineIconLabel(): String {
    val digits = filter { it.isDigit() }.trimStart('0')
    if (digits.isNotEmpty()) return digits
    if (startsWith("GTX")) return substringAfter("-").ifEmpty { "G" }
    if (startsWith("공항")) return "공항"
    // 기계적으로 두 자를 자르면 "신분"이 되어 뜻이 이상해진다. 신으로 시작하는
    // 노선이 이것 하나뿐이라 한 자로 줄여도 겹치지 않는다.
    if (startsWith("신분당")) return "신"
    return removeSuffix("선").take(2)
}

/** 목록 좌측 아이콘 기본 크기 */
val LINE_ICON_SIZE = 26.dp
