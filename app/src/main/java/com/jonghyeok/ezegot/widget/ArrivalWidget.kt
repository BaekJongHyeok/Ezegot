package com.jonghyeok.ezegot.widget

import com.jonghyeok.ezegot.dto.matchesDirection
import com.jonghyeok.ezegot.dto.directionPairFor
import com.jonghyeok.ezegot.util.forDisplay
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.view.MainActivity
import com.jonghyeok.ezegot.ui.screen.toLineIconLabel
import com.jonghyeok.ezegot.ui.theme.onSubwayLineColor
import com.jonghyeok.ezegot.util.ArrivalEmphasis
import com.jonghyeok.ezegot.util.arrivalEmphasisOf
import com.jonghyeok.ezegot.ui.theme.getSubwayLineColor

data class WidgetStationEntry(
    val stationName: String,
    val lineNumber: String,
    val upDest: String?,
    val upArrival: String?,
    val dnDest: String?,
    val dnArrival: String?
)

class ArrivalWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entries = loadWidgetEntries(context)
        provideContent {
            if (entries.isEmpty()) EmptyWidgetContent()
            else WidgetRootContent(entries)
        }
    }

    companion object {
        const val PREFS_NAME = "ezegot_widget_prefs_v4"
        private const val KEY_COUNT = "wc"
        const val MAX_FAVORITES = 3
        const val LOADING = "__loading__"

        private fun k(i: Int, s: String) = "w${i}_$s"

        // ─────────────────────────────────────────────────────────
        // SharedPrefs 읽기
        // ─────────────────────────────────────────────────────────
        fun loadWidgetEntries(context: Context): List<WidgetStationEntry> {
            val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return (0 until p.getInt(KEY_COUNT, 0)).mapNotNull { i ->
                val name = p.getString(k(i, "n"), null) ?: return@mapNotNull null
                WidgetStationEntry(
                    stationName = name,
                    lineNumber  = p.getString(k(i, "l"), "") ?: "",
                    upDest      = p.getString(k(i, "ud"), null),
                    upArrival   = p.getString(k(i, "ua"), null),
                    dnDest      = p.getString(k(i, "dd"), null),
                    dnArrival   = p.getString(k(i, "da"), null)
                )
            }
        }

        // ─────────────────────────────────────────────────────────
        // Phase 1: 즐겨찾기 변경 즉시 역 이름만 반영
        // 완전히 thread-safe: suspend 없음, 언제든 호출 가능
        // ─────────────────────────────────────────────────────────
        fun writeSnapshot(context: Context, stations: List<Pair<String, String>>) {
            val taken = stations.take(MAX_FAVORITES)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                clear()
                putInt(KEY_COUNT, taken.size)
                taken.forEachIndexed { i, (name, line) ->
                    putString(k(i, "n"), name)
                    putString(k(i, "l"), line)
                    putString(k(i, "ua"), LOADING)
                    putString(k(i, "da"), LOADING)
                }
                commit()  // 즉시 디스크 반영
            }
            // 표준 AppWidget broadcast로 위젯 갱신 (가장 신뢰성 높은 방법)
            sendBroadcastUpdate(context)
        }

        // ─────────────────────────────────────────────────────────
        // Phase 2: Worker가 API 결과로 도착 정보 완성
        // ─────────────────────────────────────────────────────────
        suspend fun updateWidgets(context: Context, arrivals: List<FavoriteArrivalInfo>) {
            val entries = arrivals.take(MAX_FAVORITES).map { fav ->
                val lineId = SubwayLine.getLineId(fav.lineNumber)

                // 거르기와 정렬은 앱 화면과 같은 규칙을 쓴다(ArrivalOrdering).
                // 정렬하지 않으면 API가 급행 계통을 끼워 넣어 가장 빠른 열차가 오지 않는다.
                val (upDirection, dnDirection) = directionPairFor(fav.lineNumber)
                val upFirst = fav.arrivals
                    .forDisplay(lineId) { it.matchesDirection(upDirection) }.firstOrNull()
                val dnFirst = fav.arrivals
                    .forDisplay(lineId) { it.matchesDirection(dnDirection) }.firstOrNull()

                // 방향은 화면이 lineNumber로 다시 뽑으므로 여기서는 행선지만 넘긴다.
                // 2호선도 내선/외선 라벨을 따로 넣지 않는다.
                WidgetStationEntry(
                    stationName = fav.stationName,
                    lineNumber  = fav.lineNumber,
                    upDest      = upFirst?.bstatnNm,
                    upArrival   = upFirst?.getFormattedMessage(),
                    dnDest      = dnFirst?.bstatnNm,
                    dnArrival   = dnFirst?.getFormattedMessage()
                )
            }

            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putInt(KEY_COUNT, entries.size)
                entries.forEachIndexed { i, e ->
                    putString(k(i, "n"),  e.stationName)
                    putString(k(i, "l"),  e.lineNumber)
                    putString(k(i, "ud"), e.upDest)
                    putString(k(i, "ua"), e.upArrival)
                    putString(k(i, "dd"), e.dnDest)
                    putString(k(i, "da"), e.dnArrival)
                }
                commit()
            }
            sendBroadcastUpdate(context)
        }

        // ─────────────────────────────────────────────────────────
        // 표준 AppWidget 브로드캐스트 – thread-safe, 가장 신뢰성 높음
        // Glance의 GlanceAppWidgetReceiver.onUpdate()를 통해
        // provideGlance()를 다시 실행하게 만든다.
        // ─────────────────────────────────────────────────────────
        fun sendBroadcastUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ArrivalWidgetReceiver::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    setComponent(component)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}

// ─── 색상 팔레트 ─────────────────────────────────────────────────
private val BgWidget        = Color(0xFF0C1624)
private val DividerColor    = Color(0xFF1C2E40)
private val TextStationName = Color(0xFFF0F5FF)
private val TextDest        = Color(0xFF8EB4D4)
/**
 * 도착 강조 3단계. 앱의 ArrivalEmphasis 판정을 그대로 쓰고 색만 다크 배경용으로 갈아 끼운다.
 * 예전에는 "정보 있음/없음" 두 색뿐이라 3분 남은 열차와 30분 남은 열차가 같아 보였다.
 *
 * 모두 위젯 배경(#0C1624) 대비 4.5:1 이상이다.
 * 특히 예전 ArrivalNone(#2E4560)은 1.85:1로 사실상 읽을 수 없었다.
 */
private val ArrivalUrgent   = Color(0xFFFF8A8A)   // 8.01:1
private val ArrivalNormal   = Color(0xFFF0F5FF)   // 16.62:1
private val ArrivalDistant  = Color(0xFF8EB4D4)   // 8.33:1
private val ArrivalInactive = Color(0xFF7089A5)   // 5.03:1

/** 어두운 노선색 원이 배경에 묻히지 않도록 두르는 링. 1호선(#0052A4)은 배경 대비 2.37:1뿐이다 */
private val IconRing        = Color(0xFF8EB4D4)
private val BrandAccent     = Color(0xFF4DD9F5)
private val BrandSub        = Color(0xFF4A7291)

/**
 * 위젯을 누르면 앱이 열린다.
 *
 * 탭 대상은 위젯 전체 하나다. 역별 행에 따로 붙이면 "강남 행을 누르면 강남으로
 * 간다"는 기대가 생기는데, 지금은 딥링크가 없어 어느 행을 눌러도 홈으로 간다.
 * 기대를 만들어 놓고 어긋나는 것보다 위젯 전체가 하나의 대상인 편이 정직하다.
 * 딥링크를 붙일 때 그때 행 단위로 쪼개는 것이 순서다.
 */
private fun openApp() = actionStartActivity<MainActivity>()

@Composable
private fun WidgetRootContent(entries: List<WidgetStationEntry>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BgWidget)
            .clickable(openApp())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Vertical.CenterVertically,
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
            Text(
                text = "EZEGOT",
                style = TextStyle(
                    color = ColorProvider(BrandAccent),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.width(5.dp))
            Text(
                text = "실시간 도착",
                style = TextStyle(color = ColorProvider(BrandSub), fontSize = 10.sp)
            )
        }

        entries.forEachIndexed { idx, entry ->
            StationCard(entry)
            if (idx < entries.lastIndex) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DividerColor)
                ) {}
            }
        }
    }
}

@Composable
private fun StationCard(entry: WidgetStationEntry) {
    val (upDirection, dnDirection) = directionPairFor(entry.lineNumber)

    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 5.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 3.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            LineIcon(entry.lineNumber)
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = "${entry.stationName}역",
                style = TextStyle(
                    color = ColorProvider(TextStationName),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            ArrivalItem(upDirection, entry.upDest, entry.upArrival, GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(4.dp))
            Box(
                modifier = GlanceModifier
                    .width(1.dp)
                    .height(14.dp)
                    .background(DividerColor)
            ) {}
            Spacer(GlanceModifier.width(4.dp))
            ArrivalItem(dnDirection, entry.dnDest, entry.dnArrival, GlanceModifier.defaultWeight())
        }
    }
}

/**
 * 앱의 SubwayLineIcon을 Glance로 옮긴 것.
 *
 * Glance는 Canvas도 border도 쓸 수 없어 Box를 겹쳐 원과 링을 만든다.
 * 바깥 Box가 링, 안쪽 Box가 노선색 원이다.
 * 링이 필요한 이유는 어두운 노선색이 어두운 위젯 배경에 묻히기 때문이다
 * (1호선 #0052A4는 배경 대비 2.37:1).
 *
 * 라벨과 글자색 규칙은 앱과 같은 함수(toLineIconLabel, onSubwayLineColor)를 쓴다.
 * 예전에는 "2호선"을 "2선"으로 줄여 쓰고 있었는데, 원형 아이콘이 되면서
 * 앱과 동일하게 "2"만 남는다.
 */
@Composable
private fun LineIcon(lineName: String) {
    val fill = getSubwayLineColor(lineName)
    val label = lineName.toLineIconLabel()

    Box(
        modifier = GlanceModifier
            .size(WIDGET_ICON_SIZE + 2.dp)
            .background(IconRing)
            .cornerRadius((WIDGET_ICON_SIZE + 2.dp) / 2),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = GlanceModifier
                .size(WIDGET_ICON_SIZE)
                .background(fill)
                .cornerRadius(WIDGET_ICON_SIZE / 2),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = TextStyle(
                    color = ColorProvider(onSubwayLineColor(fill)),
                    fontSize = if (label.length <= 1) 11.sp else 8.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun ArrivalItem(
    direction: String,
    destination: String?,
    arrival: String?,
    modifier: GlanceModifier
) {
    val isLoading = arrival == ArrivalWidget.LOADING
    val displayTime = when {
        isLoading       -> "···"
        arrival != null -> arrival.replace(" 후", "")
        else            -> "-"
    }

    // 강조 판정은 앱과 같은 규칙(ArrivalEmphasis)을 쓰고 색만 다크 배경용으로 바꾼다
    val timeColor = when {
        isLoading || arrival == null -> ArrivalInactive
        else -> when (arrivalEmphasisOf(arrival)) {
            ArrivalEmphasis.URGENT -> ArrivalUrgent
            ArrivalEmphasis.NORMAL -> ArrivalNormal
            ArrivalEmphasis.DISTANT -> ArrivalDistant
            ArrivalEmphasis.INACTIVE -> ArrivalInactive
        }
    }

    // 즐겨찾기가 역 단위가 되어 한 역이 두 방향을 나란히 보여준다.
    // 행선지만 쓰면 2호선처럼 양방향이 모두 "성수행"인 노선에서 구분되지 않고,
    // 방향만 쓰면 어디로 가는지 알 수 없다. 폭이 좁아 "행"·"방면" 접미사는 뺀다.
    val label = if (destination.isNullOrBlank()) direction else "$direction · $destination"

    Row(modifier = modifier, verticalAlignment = Alignment.Vertical.CenterVertically) {
        Text(
            text = label,
            style = TextStyle(color = ColorProvider(TextDest), fontSize = 10.sp),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            text = displayTime,
            style = TextStyle(
                color = ColorProvider(timeColor),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

/** 위젯 아이콘 지름. 앱은 26dp지만 위젯 행 높이가 낮아 조금 줄인다 */
private val WIDGET_ICON_SIZE = 20.dp

@Composable
private fun EmptyWidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BgWidget)
            .padding(12.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "EZEGOT",
            style = TextStyle(
                color = ColorProvider(BrandAccent),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = "앱에서 즐겨찾기를 추가해주세요 ⭐",
            style = TextStyle(color = ColorProvider(BrandSub), fontSize = 11.sp)
        )
    }
}
