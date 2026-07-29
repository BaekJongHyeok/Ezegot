package com.jonghyeok.ezegot.dto

/**
 * 방향까지 포함한 즐겨찾기 한 건.
 *
 * 대부분의 사용자는 한 방향만 필요하다. 역 단위로 담으면 쓰지 않는 방향이
 * 항상 함께 나오므로 방향을 저장 단위에 넣었다.
 *
 * [direction]은 실시간 API의 `updnLine` 값을 그대로 쓴다.
 * 화면에 보이는 "교대 방면" 같은 라벨은 열차마다 달라져서 저장하면 낡는다.
 * 라벨은 저장하지 않고 도착 정보에서 런타임에 만든다.
 */
data class FavoriteStation(
    val stationName: String,
    val lineNumber: String,
    val direction: String
)

/**
 * 노선이 쓰는 방향 표기 쌍.
 *
 * 2호선은 순환선이라 상하행 대신 내선/외선이 온다.
 */
fun directionPairFor(lineNumber: String): Pair<String, String> =
    if (lineNumber.contains("2호선")) INNER to OUTER else UP to DOWN

const val UP = "상행"
const val DOWN = "하행"
const val INNER = "내선"
const val OUTER = "외선"

/** 실시간 도착의 `updnLine`이 이 방향에 속하는지. 2호선 표기 차이를 흡수한다. */
fun String.matchesDirection(direction: String): Boolean = when (direction) {
    UP, INNER -> this == UP || this == INNER
    else -> this == DOWN || this == OUTER
}
