package com.jonghyeok.ezegot.ui.screen

import com.jonghyeok.ezegot.util.forDisplay
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.dto.FavoriteStation
import com.jonghyeok.ezegot.dto.NearbyStation
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.dto.directionPairFor
import com.jonghyeok.ezegot.dto.matchesDirection
import com.jonghyeok.ezegot.ui.theme.EzegotWordmark
import com.jonghyeok.ezegot.ui.theme.getSubwayLineColor
import com.jonghyeok.ezegot.util.ArrivalEmphasis
import com.jonghyeok.ezegot.util.emphasis
import com.jonghyeok.ezegot.viewModel.MainViewModel
import java.time.LocalTime

/**
 * 홈.
 *
 * 즐겨찾기를 캐러셀 3개 + 리스트로 나눠 두었는데, 즐겨찾기가 역 단위가 되면서
 * 카드와 행이 담는 정보가 같아졌다. 그러자 카드가 더 주는 것은 큰 글자뿐인데
 * 가로 스와이프를 요구하고, 섹션 제목이 둘로 갈리고, 3+1 같은 분할이
 * 자의적으로 보였다. 리스트 하나로 합치고 도착 시간을 키웠다.
 */
@Composable
fun HomeScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onSearchClick: () -> Unit,
    onMapClick: () -> Unit,
    onStationClick: (String, String) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadRealtimeArrival()
        viewModel.updateCurrentLocation()
    }

    val favorites by viewModel.favoriteStationList.collectAsState()
    val arrivalMap by viewModel.realtimeArrivalInfo.collectAsState()
    val nearbyStations by viewModel.nearbyStationList.collectAsState()
    val nearestArrivals by viewModel.nearestArrivals.collectAsState()
    val lastUpdatedAt by viewModel.lastUpdatedAt.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeHeader(onSearchClick = onSearchClick, onSettingsClick = {})

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(6.dp))

            FavoriteSection(
                favorites = favorites,
                arrivalMap = arrivalMap,
                updatedAt = lastUpdatedAt,
                onRefresh = { viewModel.loadRealtimeArrival() },
                onStationClick = onStationClick
            )

            Spacer(Modifier.height(6.dp))

            NearbySection(
                stations = nearbyStations,
                nearestArrivals = nearestArrivals,
                onMapClick = onMapClick,
                onStationClick = onStationClick
            )

            // 마지막 카드가 하단바에 잘리지 않도록. Scaffold가 이미 바 높이만큼
            // 인셋을 주지만, 카드 테두리가 바로 맞닿아 잘려 보였다.
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── 헤더 ─────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(onSearchClick: () -> Unit, onSettingsClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .height(48.dp)
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 웨이트 예외는 Type.kt의 EzegotWordmark 한 곳에만 있다
            Text(
                text = "EZEGOT",
                style = EzegotWordmark,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "검색",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "설정",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    }
}

// ── 내 주변 역 ───────────────────────────────────────────────────
@Composable
private fun NearbySection(
    stations: List<NearbyStation>,
    nearestArrivals: List<RealtimeArrival>,
    onMapClick: () -> Unit,
    onStationClick: (String, String) -> Unit
) {
    // 반경을 좁혀도 도심에서는 후보가 많다. 화면 몫도 고려해 3개까지만.
    // 개수 표시도 이 목록을 따른다. 반경 안의 전체 수를 쓰면 화면에 3개가 보이는데
    // 5라고 적혀 어디에 둘이 더 있는지 찾게 된다.
    val shown = stations.take(3)

    SectionCard(
        title = "내 주변 역",
        count = shown.size.takeIf { it > 0 },
        actionLabel = "지도 ›",
        onAction = onMapClick
    ) {
        if (shown.isEmpty()) {
            EmptyRow("주변에 표시할 역이 없습니다")
        } else {
            shown.forEachIndexed { index, station ->
                if (index > 0) {
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
                NearbyStationRow(
                    station = station,
                    // 도착 정보는 가장 가까운 역에만 붙인다. 목록 전체에 붙이면
                    // 역 수만큼 실시간 API를 부르게 되고 일일 제한을 넘긴다.
                    // 방향은 가리지 않는다. 즐겨찾기가 아니라 "근처에 뭐가 있나"를
                    // 보는 자리라 어느 쪽이든 가장 빨리 오는 한 대면 된다.
                    arrival = if (index == 0) {
                        nearestArrivals
                            .forDisplay(SubwayLine.getLineId(station.lineNumber)) { true }
                            .firstOrNull()
                    } else null,
                    onClick = { onStationClick(station.stationName, station.lineNumber) }
                )
            }
        }
    }
}

@Composable
private fun NearbyStationRow(
    station: NearbyStation,
    arrival: RealtimeArrival?,
    onClick: () -> Unit
) {
    StationRow(
        lineNumber = station.lineNumber,
        stationName = station.stationName,
        onClick = onClick
    ) {
        // 도착 정보가 없는 역(2·3번째)은 거리만. 실시간 API를 최근접 한 곳에만
        // 부르기 때문이지, 그 역에 열차가 없다는 뜻이 아니다.
        DetailRow(
            label = station.walkingLabel(),
            arrival = arrival,
            emptyPlaceholder = null,
            trailingLabel = arrival?.let { "${it.bstatnNm}행" }
        )
    }
}

/** "250m · 도보 4분". 도보 속도는 분당 67m로 잡는다 */
private fun NearbyStation.walkingLabel(): String {
    val meters = (distance * 1000).toInt()
    val minutes = maxOf(1, Math.round(meters / 67.0).toInt())
    val distanceText = if (meters >= 1000) String.format("%.1fkm", meters / 1000.0) else "${meters}m"
    return "$distanceText · 도보 ${minutes}분"
}

// ── 즐겨찾기 ─────────────────────────────────────────────────────
@Composable
private fun FavoriteSection(
    favorites: List<FavoriteStation>,
    arrivalMap: Map<String, List<RealtimeArrival>>,
    updatedAt: LocalTime?,
    onRefresh: () -> Unit,
    onStationClick: (String, String) -> Unit
) {
    SectionCard(
        title = "즐겨찾기",
        count = favorites.size.takeIf { it > 0 },
        updatedAt = updatedAt,
        onRefresh = onRefresh
    ) {
        if (favorites.isEmpty()) {
            EmptyRow("역 상세에서 별을 눌러 추가하세요")
            return@SectionCard
        }
        favorites.forEachIndexed { index, favorite ->
            if (index > 0) {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
            FavoriteRow(
                favorite = favorite,
                arrivals = arrivalMap.directionalArrivals(favorite),
                onClick = { onStationClick(favorite.stationName, favorite.lineNumber) }
            )
        }
    }
}

@Composable
private fun FavoriteRow(
    favorite: FavoriteStation,
    arrivals: List<Pair<String, RealtimeArrival?>>,
    onClick: () -> Unit
) {
    StationRow(
        lineNumber = favorite.lineNumber,
        stationName = favorite.stationName,
        onClick = onClick
    ) {
        // 그 방향에 열차가 없어도 줄을 지우지 않는다.
        // 한 줄만 남으면 그 역에 방향이 하나뿐인 것처럼 보인다.
        arrivals.forEach { (direction, arrival) ->
            DetailRow(
                label = if (arrival != null) "$direction · ${arrival.bstatnNm}행" else direction,
                arrival = arrival
            )
        }
    }
}

// ── 공통 행 ──────────────────────────────────────────────────────
/**
 * 홈 리스트의 한 행.
 *
 * 예전에는 역명이 왼쪽 끝, 정보가 오른쪽 끝에 몰려 가운데가 비고 오른쪽만
 * 글자가 뭉쳤다. 역명을 첫 줄에 단독으로 두고 상세를 아래 줄로 내린다.
 * 시선이 좌우로 튀지 않고 위에서 아래로 흐른다.
 *
 * 왼쪽 세로 바가 노선색을 담당하므로 아이콘은 20dp로 줄여도 된다.
 */
@Composable
private fun StationRow(
    lineNumber: String,
    stationName: String,
    onClick: () -> Unit,
    details: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable { onClick() }
    ) {
        // 행 전체 높이를 채우는 노선색 바
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(getSubwayLineColor(lineNumber))
        )
        Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubwayLineIcon(lineName = lineNumber, size = 20.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stationName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(3.dp))
            details()
        }
    }
}

/**
 * 역명 아래 한 줄. 왼쪽 라벨, 오른쪽 도착 시간.
 *
 * [emptyPlaceholder]가 null이면 도착 정보가 없을 때 오른쪽을 비운다.
 * 즐겨찾기는 "정보 없음"을 띄워 그 방향이 있다는 것을 알리지만,
 * 주변 역은 애초에 호출하지 않은 것이라 빈칸이 맞다.
 */
@Composable
private fun DetailRow(
    label: String,
    arrival: RealtimeArrival?,
    emptyPlaceholder: String? = "정보 없음",
    trailingLabel: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 방향 줄이 둘이라 여백이 두 배로 쌓인다. 1dp만 줘도 행이 88dp를 넘겼다
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        // 시간 바로 앞자리는 "어느 열차인지"로 통일한다.
        // 즐겨찾기는 "상행 · 청량리행"이 왼쪽에 있지만, 주변 역은 왼쪽이 거리라
        // 성격이 다른 값이 한 구분자로 이어지지 않도록 행선지를 이쪽으로 옮겼다.
        if (trailingLabel != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = trailingLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        if (arrival != null) {
            ArrivalTime(arrival = arrival)
        } else if (emptyPlaceholder != null) {
            Text(
                text = emptyPlaceholder,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

// ── 공통 ─────────────────────────────────────────────────────────
/** 섹션 제목 + 흰 카드. 카드 안 내용은 호출부가 채운다 */
@Composable
private fun SectionCard(
    title: String,
    count: Int? = null,
    updatedAt: LocalTime? = null,
    onRefresh: (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    letterSpacing = 0.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            if (count != null) {
                Spacer(Modifier.width(5.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(Modifier.weight(1f))
            if (updatedAt != null) {
                // 요청 시각이 아니라 응답이 실제로 들어온 시각이다(MainViewModel 참고)
                Text(
                    text = "%02d:%02d 기준".format(updatedAt.hour, updatedAt.minute),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            if (onRefresh != null) {
                IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "새로고침",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (actionLabel != null && onAction != null) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onAction() }
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                )
            }
        }
        // 테두리는 클립 바깥에 둔다. 안쪽 Column이 자식을 모서리로 잘라내므로
        // 첫 행·마지막 행의 호선색 세로 바가 둥근 모서리를 뚫고 나오지 않는다.
        val shape = RoundedCornerShape(14.dp)
        Box(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, shape)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface),
                content = content
            )
        }
    }
}

@Composable
private fun EmptyRow(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

/**
 * 도착 시간.
 *
 * 수치("3분")와 상태("도착", "곧 도착", "진입")는 성격이 달라 표현도 나눈다.
 * 예전에는 둘 다 같은 크기의 빨간 굵은 글씨라, 한 행에 겹치면 붉은 덩어리로
 * 보이고 정작 숫자가 묻혔다. 상태는 칩으로 내려 빨강 면적을 줄인다.
 *
 * 숫자는 tabular figures로 고정폭을 줘 세로로 자릿수가 흔들리지 않게 한다.
 */
@Composable
private fun ArrivalTime(arrival: RealtimeArrival) {
    val message = arrival.getFormattedMessage()
    val minutes = Regex("(\\d+)분").find(message)?.groupValues?.get(1)

    if (minutes == null) {
        StatusChip(message)
        return
    }

    val color = when (arrival.emphasis()) {
        ArrivalEmphasis.URGENT -> MaterialTheme.colorScheme.error
        ArrivalEmphasis.NORMAL -> MaterialTheme.colorScheme.onSurface
        ArrivalEmphasis.DISTANT -> MaterialTheme.colorScheme.onSurfaceVariant
        ArrivalEmphasis.INACTIVE -> MaterialTheme.colorScheme.tertiary
    }

    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = minutes,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 18.sp,
                letterSpacing = (-0.6).sp,
                fontFeatureSettings = "tnum"
            ),
            color = color,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "분",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = color,
            modifier = Modifier.padding(bottom = 2.dp, start = 1.dp)
        )
    }
}

/** "도착"·"진입"·"곧 도착" 같은 상태 표현 */
@Composable
private fun StatusChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            maxLines = 1
        )
    }
}

/**
 * 이 역의 방향별 다음 열차 한 대씩.
 *
 * 즐겨찾기가 역 단위가 되면서 한 항목이 두 방향을 함께 보여준다.
 * 방향마다 가장 빠른 한 대만 남긴다 — 카드가 두 줄을 넘기면
 * 캐러셀 높이가 역마다 달라진다.
 *
 * 반환 순서는 [directionPairFor]가 주는 순서(상행→하행 / 내선→외선)를 따른다.
 * 도착 시각순으로 정렬하면 갱신할 때마다 두 줄이 자리를 바꿔 읽기 어렵다.
 */
private fun Map<String, List<RealtimeArrival>>.directionalArrivals(
    favorite: FavoriteStation
): List<Pair<String, RealtimeArrival?>> {
    val lineId = SubwayLine.getLineId(favorite.lineNumber)
    val all = this[favorite.stationName] ?: emptyList()

    val (first, second) = directionPairFor(favorite.lineNumber)
    // 열차가 없는 방향도 자리를 남긴다. 화면이 방향 라벨과 "정보 없음"을 그린다.
    return listOf(first, second).map { direction ->
        // 거르기와 정렬은 역 상세·위젯과 같은 규칙을 쓴다(ArrivalOrdering).
        // 정렬하지 않으면 API가 급행 계통을 끼워 넣어 가장 빠른 열차가 첫 줄에 오지 않는다.
        direction to all.forDisplay(lineId) { it.matchesDirection(direction) }.firstOrNull()
    }
}
