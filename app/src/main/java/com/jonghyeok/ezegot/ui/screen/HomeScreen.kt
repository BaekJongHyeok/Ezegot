package com.jonghyeok.ezegot.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.jonghyeok.ezegot.dto.matchesDirection
import com.jonghyeok.ezegot.ui.theme.getSubwayLineColor
import com.jonghyeok.ezegot.ui.theme.onSubwayLineColor
import com.jonghyeok.ezegot.util.ArrivalEmphasis
import com.jonghyeok.ezegot.util.emphasis
import com.jonghyeok.ezegot.viewModel.MainViewModel

/** 캐러셀에 올릴 최대 개수. 넘치면 아래 리스트로 이어 붙인다 */
private const val CAROUSEL_LIMIT = 3

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

    val carouselItems = favorites.take(CAROUSEL_LIMIT)
    val overflowItems = favorites.drop(CAROUSEL_LIMIT)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeHeader(onSearchClick = onSearchClick, onSettingsClick = {})

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            if (carouselItems.isNotEmpty()) {
                FavoriteCarousel(
                    favorites = carouselItems,
                    arrivalMap = arrivalMap,
                    onStationClick = onStationClick
                )
            }

            Spacer(Modifier.height(6.dp))

            NearbySection(
                stations = nearbyStations,
                nearestArrivals = nearestArrivals,
                onMapClick = onMapClick,
                onStationClick = onStationClick
            )

            // 캐러셀에 다 넣으면 스와이프가 늘어난다. 4번째부터는 리스트로 잇는다
            if (overflowItems.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                OverflowFavoriteSection(
                    favorites = overflowItems,
                    arrivalMap = arrivalMap,
                    onStationClick = onStationClick
                )
            }

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
            Text(
                text = "EZEGOT",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 15.sp,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
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

// ── 즐겨찾기 캐러셀 ──────────────────────────────────────────────
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun FavoriteCarousel(
    favorites: List<FavoriteStation>,
    arrivalMap: Map<String, List<RealtimeArrival>>,
    onStationClick: (String, String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { favorites.size })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 14.dp)
    ) {
        // 아래 "다른 즐겨찾기" 리스트와 같은 성격임을 알리는 라벨.
        // 없으면 캐러셀과 리스트가 왜 나뉘는지 알 수 없다.
        Text(
            text = "즐겨찾기",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 12.dp, bottom = 10.dp)
        )

        HorizontalPager(
            state = pagerState,
            // 옆 카드가 살짝 보이도록 남긴다
            contentPadding = PaddingValues(horizontal = 12.dp),
            pageSpacing = 10.dp,
            pageSize = PageSize.Fixed(178.dp)
        ) { page ->
            val favorite = favorites[page]
            FavoriteCarouselCard(
                favorite = favorite,
                arrivals = arrivalMap.arrivalsFor(favorite),
                onClick = { onStationClick(favorite.stationName, favorite.lineNumber) }
            )
        }

        // 카드가 하나뿐이면 인디케이터가 정보를 주지 않는다
        if (favorites.size > 1) {
            Spacer(Modifier.height(10.dp))
            PagerIndicator(
                count = favorites.size,
                current = pagerState.currentPage,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun FavoriteCarouselCard(
    favorite: FavoriteStation,
    arrivals: List<RealtimeArrival>,
    onClick: () -> Unit
) {
    val lineColor = getSubwayLineColor(favorite.lineNumber)
    val onLine = onSubwayLineColor(lineColor)

    Column(
        modifier = Modifier
            .width(178.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        // 카드 상단 – 호선색 배경
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(lineColor)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = favorite.stationName,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = onLine,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(6.dp))
            TranslucentChip(text = favorite.lineNumber.removePrefix("0"), onLine = onLine)
            Spacer(Modifier.width(4.dp))
            // 같은 역이라도 방향마다 카드가 따로 있으므로, 어느 쪽인지 카드에서 바로 보여야 한다
            TranslucentChip(text = favorite.direction, onLine = onLine)
        }

        // 카드 본문 – 그 방향의 다음 열차를 최대 3대까지
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            val shown = arrivals.take(3)
            if (shown.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "도착 정보 없음",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            } else {
                shown.forEachIndexed { index, arrival ->
                    if (index > 0) {
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    // 한 카드가 한 방향이라 행선지가 대체로 같다.
                    // 매 행에 "성수행"을 반복하면 시선만 잡아먹으므로 첫 행에만 둔다.
                    // 다만 종착이 달라지는 열차(지선 종착 등)는 그대로 보여준다.
                    val previous = shown.getOrNull(index - 1)
                    CarouselArrivalRow(
                        arrival = arrival,
                        isFirst = index == 0,
                        showDestination = index == 0 || arrival.bstatnNm != previous?.bstatnNm
                    )
                }
            }
        }
    }
}

/** 호선색 헤더 위에 얹는 반투명 칩. 배경이 노선마다 달라 글자색은 넘겨받는다 */
@Composable
private fun TranslucentChip(text: String, onLine: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(onLine.copy(alpha = 0.22f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = onLine,
            maxLines = 1
        )
    }
}

@Composable
private fun CarouselArrivalRow(
    arrival: RealtimeArrival,
    isFirst: Boolean,
    showDestination: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = if (isFirst) 9.dp else 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (showDestination) "${arrival.bstatnNm}행" else "",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(6.dp))
        ArrivalTime(arrival = arrival, fontSize = if (isFirst) 26.sp else 15.sp)
    }
}

@Composable
private fun PagerIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val active = index == current
            Box(
                modifier = Modifier
                    .width(if (active) 14.dp else 4.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline
                    )
            )
        }
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
                    arrival = if (index == 0) {
                        nearestArrivals.firstOrNull {
                            it.subwayId == SubwayLine.getLineId(station.lineNumber)
                        }
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
    val lineColor = getSubwayLineColor(station.lineNumber)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LineSquareBadge(lineName = station.lineNumber, lineColor = lineColor)
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

// ── 캐러셀에서 넘친 즐겨찾기 ─────────────────────────────────────
@Composable
private fun OverflowFavoriteSection(
    favorites: List<FavoriteStation>,
    arrivalMap: Map<String, List<RealtimeArrival>>,
    onStationClick: (String, String) -> Unit
) {
    // "더보기"는 왜 나뉘었는지 설명하지 못한다. 위 캐러셀과 같은 즐겨찾기이고
    // 앞선 것만 위로 뽑았다는 뜻이 드러나게 쓴다.
    SectionCard(title = "다른 즐겨찾기") {
        favorites.forEachIndexed { index, favorite ->
            if (index > 0) {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
            OverflowFavoriteRow(
                favorite = favorite,
                arrivals = arrivalMap.arrivalsFor(favorite),
                onClick = { onStationClick(favorite.stationName, favorite.lineNumber) }
            )
        }
    }
}

@Composable
private fun OverflowFavoriteRow(
    favorite: FavoriteStation,
    arrivals: List<RealtimeArrival>,
    onClick: () -> Unit
) {
    val lineColor = getSubwayLineColor(favorite.lineNumber)
    val first = arrivals.firstOrNull()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LineSquareBadge(lineName = favorite.lineNumber, lineColor = lineColor)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = favorite.stationName,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = favorite.direction,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (first != null) {
            Column(horizontalAlignment = Alignment.End) {
                ArrivalTime(arrival = first, fontSize = 15.sp)
                Text(
                    text = "${first.bstatnNm}행",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        } else {
            Text(
                text = "정보 없음",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
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

/** 이 즐겨찾기(역 + 방향)에 해당하는 도착만 남긴다 */
private fun Map<String, List<RealtimeArrival>>.arrivalsFor(favorite: FavoriteStation): List<RealtimeArrival> {
    val lineId = SubwayLine.getLineId(favorite.lineNumber)
    // 종착역으로 중복을 지우면 안 된다. 2호선 내선은 다음 두 대가 모두 "성수행"이라
    // 열차가 2대 와도 1대만 남아 카드가 비어 보였다.
    return (this[favorite.stationName] ?: emptyList())
        .filter {
            it.subwayId == lineId &&
                it.updnLine.matchesDirection(favorite.direction) &&
                it.getFormattedMessage() != "출발"
        }
}
