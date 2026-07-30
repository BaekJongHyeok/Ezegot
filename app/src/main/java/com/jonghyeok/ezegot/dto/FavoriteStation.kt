package com.jonghyeok.ezegot.dto

/**
 * 즐겨찾기 한 건. 역이 단위다.
 *
 * 방향은 저장하지 않는다. 한동안 방향까지 넣었더니 같은 역의 반대 방향이
 * 별도 항목이 되어 목록에 역 이름이 두 번씩 나왔다.
 * 화면에서 한 항목 안에 두 방향을 나란히 보여주면 될 일이다.
 *
 * 화면에 보이는 "교대 방면" 같은 라벨도 저장하지 않는다.
 * 열차마다 달라져서 저장하면 낡는다. 도착 정보에서 런타임에 만든다.
 */
data class FavoriteStation(
    val stationName: String,
    val lineNumber: String
)

/**
 * 노선이 쓰는 방향 표기 쌍.
 *
 * 2호선은 순환선이라 상하행 대신 내선/외선이 온다.
 * 저장 단위는 아니지만, 한 역의 도착 정보를 방향별로 가르는 데는 여전히 필요하다.
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
