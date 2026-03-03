package com.jonghyeok.ezegot.widget

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
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.jonghyeok.ezegot.SubwayLine
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
                val is2 = fav.lineNumber.contains("2호선")

                val upFirst = fav.arrivals.firstOrNull {
                    it.subwayId == lineId &&
                    (it.updnLine == "상행" || it.updnLine == "내선") &&
                    it.getFormattedMessage() != "출발"
                }
                val dnFirst = fav.arrivals.firstOrNull {
                    it.subwayId == lineId &&
                    (it.updnLine == "하행" || it.updnLine == "외선") &&
                    it.getFormattedMessage() != "출발"
                }

                WidgetStationEntry(
                    stationName = fav.stationName,
                    lineNumber  = fav.lineNumber,
                    upDest      = if (is2) "내선" else upFirst?.bstatnNm,
                    upArrival   = upFirst?.getFormattedMessage(),
                    dnDest      = if (is2) "외선" else dnFirst?.bstatnNm,
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
private val ArrivalActive   = Color(0xFF4DD9F5)
private val ArrivalNone     = Color(0xFF2E4560)
private val BrandAccent     = Color(0xFF4DD9F5)
private val BrandSub        = Color(0xFF4A7291)

@Composable
private fun WidgetRootContent(entries: List<WidgetStationEntry>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BgWidget)
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
    val lineColor = getSubwayLineColor(entry.lineNumber)
    val badgeText = entry.lineNumber.removePrefix("0").replace("호선", "선")
        .let { if (it.length > 5) it.take(5) else it }

    val is2 = entry.lineNumber.contains("2호선")
    val upDestLabel = entry.upDest ?: if (is2) "내선" else "상행"
    val dnDestLabel = entry.dnDest ?: if (is2) "외선" else "하행"

    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 5.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 3.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "${entry.stationName}역",
                style = TextStyle(
                    color = ColorProvider(TextStationName),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.width(5.dp))
            Text(
                text = badgeText,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier
                    .background(lineColor)
                    .cornerRadius(4.dp)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            ArrivalItem(upDestLabel, entry.upArrival, GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(4.dp))
            Box(
                modifier = GlanceModifier
                    .width(1.dp)
                    .height(14.dp)
                    .background(DividerColor)
            ) {}
            Spacer(GlanceModifier.width(4.dp))
            ArrivalItem(dnDestLabel, entry.dnArrival, GlanceModifier.defaultWeight())
        }
    }
}

@Composable
private fun ArrivalItem(dest: String, arrival: String?, modifier: GlanceModifier) {
    val isLoading = arrival == ArrivalWidget.LOADING
    val displayTime = when {
        isLoading       -> "···"
        arrival != null -> arrival.replace(" 후", "")
        else            -> "-"
    }
    val timeColor = if (!isLoading && arrival != null) ArrivalActive else ArrivalNone

    Row(modifier = modifier, verticalAlignment = Alignment.Vertical.CenterVertically) {
        Text(
            text = "${dest} 방면",
            style = TextStyle(color = ColorProvider(TextDest), fontSize = 10.sp),
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
