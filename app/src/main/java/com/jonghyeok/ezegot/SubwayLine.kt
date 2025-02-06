package com.jonghyeok.ezegot

data class SubwayLine(val id: String, val name: String) {
    companion object {
        private val lines = mapOf(
            "1001" to "1호선",
            "1002" to "2호선",
            "1003" to "3호선",
            "1004" to "4호선",
            "1005" to "5호선",
            "1006" to "6호선",
            "1007" to "7호선",
            "1008" to "8호선",
            "1009" to "9호선",
            "1061" to "중앙선",
            "1063" to "경의중앙선",
            "1065" to "공항철도",
            "1067" to "경춘선",
            "1075" to "수인분당선",
            "1077" to "신분당선",
            "1092" to "우이신설선",
            "1093" to "서해선",
            "1081" to "경강선",
            "1032" to "GTX-A"
        )

        fun getLineName(id: String): String? {
            return lines[id]
        }

        fun getLineId(name: String): String? {
            val formattedName = name.removePrefix("0") // "01호선" -> "1호선"
            return lines.entries.find { it.value == formattedName }?.key
        }

        fun getLineImage(lineNumber: String): Int {
            return when (lineNumber) {
                "01호선" -> R.drawable.ic_line1
                "02호선" -> R.drawable.ic_line2
                "03호선" -> R.drawable.ic_line3
                "04호선" -> R.drawable.ic_line4
                "05호선" -> R.drawable.ic_line5
                "06호선" -> R.drawable.ic_line6
                "07호선" -> R.drawable.ic_line7
                "08호선" -> R.drawable.ic_line8
                "09호선" -> R.drawable.ic_line9
                "중앙선" -> R.drawable.ic_line0
                "경의중앙선" -> R.drawable.ic_line11
                "공항철도" -> R.drawable.ic_line12
                "경춘선" -> R.drawable.ic_line13
                "수인분당선" -> R.drawable.ic_line14
                "신분당선" -> R.drawable.ic_line15
                "우이신설선" -> R.drawable.ic_line16
                "서해선" -> R.drawable.ic_line17
                "경강선" -> R.drawable.ic_line18
                "GTX-A" -> R.drawable.ic_line19
                else -> R.drawable.ic_line0 // 기본 이미지
            }
        }
    }
}