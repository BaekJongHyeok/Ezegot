package com.jonghyeok.ezegot.ui.screen

import com.jonghyeok.ezegot.util.forDisplay
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.jonghyeok.ezegot.util.ArrivalEmphasis
import com.jonghyeok.ezegot.util.emphasis
import com.jonghyeok.ezegot.viewModel.MainViewModel

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
                onStationClick = onStationClick
            )

            Spacer(Modifier.height(6.dp))

            NearbySection(
                stations = nearbyStations,
                nearestArrivals = nearestArrivals,
                onMapClick = onMapClick,
                onStationClick = onStationClick
            )

            Spacer(Modifier.height(16.dp))
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
    SectionCard(
        title = "내 주변 역",
        actionLabel = "지도 ›",
        onAction = onMapClick
    ) {
        if (stations.isEmpty()) {
            EmptyRow("주변에 표시할 역이 없습니다")
        } else {
            // 반경을 좁혀도 도심에서는 후보가 많다. 화면 몫도 고려해 3개까지만
            stations.take(3).forEachIndexed { index, station ->
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubwayLineIcon(lineName = station.lineNumber)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = station.stationName,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = station.walkingLabel(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (arrival != null) {
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                ArrivalTime(arrival = arrival, fontSize = 15.sp)
                Text(
                    text = "${arrival.bstatnNm}행",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
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
    onStationClick: (String, String) -> Unit
) {
    SectionCard(title = "즐겨찾기") {
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubwayLineIcon(lineName = favorite.lineNumber)
        Spacer(Modifier.width(10.dp))
        Text(
            text = favorite.stationName,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        run {
            // 역 단위라 한 행이 두 방향을 함께 보여준다.
            // 방향 라벨은 왼쪽, 시간은 오른쪽으로 맞춰 두 줄의 열이 흔들리지 않게 한다.
            Column(horizontalAlignment = Alignment.End) {
                arrivals.forEachIndexed { index, (direction, arrival) ->
                    if (index > 0) Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            // 그 방향에 열차가 없어도 줄을 지우지 않는다.
                            // 한 줄만 남으면 그 역에 방향이 하나뿐인 것처럼 보인다.
                            text = if (arrival != null) "$direction · ${arrival.bstatnNm}행" else direction,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(Modifier.width(8.dp))
                        if (arrival != null) {
                            ArrivalTime(arrival = arrival, fontSize = 18.sp)
                        } else {
                            Text(
                                text = "정보 없음",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 공통 ─────────────────────────────────────────────────────────
/** 섹션 제목 + 흰 카드. 카드 안 내용은 호출부가 채운다 */
@Composable
private fun SectionCard(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
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
            Spacer(Modifier.weight(1f))
            if (actionLabel != null && onAction != null) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onAction() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
            content = content
        )
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
 * 도착 시간. 숫자와 "분"을 분리해 단위를 작게 쓴다.
 * 숫자는 tabular figures로 고정폭을 줘 세로로 자릿수가 흔들리지 않게 한다.
 */
@Composable
private fun ArrivalTime(
    arrival: RealtimeArrival,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    val message = arrival.getFormattedMessage()
    val emphasis = arrival.emphasis()
    val color = when (emphasis) {
        ArrivalEmphasis.URGENT -> MaterialTheme.colorScheme.error
        ArrivalEmphasis.NORMAL -> MaterialTheme.colorScheme.onSurface
        ArrivalEmphasis.DISTANT -> MaterialTheme.colorScheme.onSurfaceVariant
        ArrivalEmphasis.INACTIVE -> MaterialTheme.colorScheme.tertiary
    }

    val minutes = Regex("(\\d+)분").find(message)?.groupValues?.get(1)

    Row(verticalAlignment = Alignment.Bottom) {
        if (minutes != null) {
            Text(
                text = minutes,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = fontSize,
                    letterSpacing = (-0.8).sp,
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
        } else {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = color,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
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
