package com.jonghyeok.ezegot

import com.jonghyeok.ezegot.dto.RealtimeArrival
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "realtimeStationArrival", strict = false)
data class StationArrivalResponse(
    @field:Element(name = "RESULT", required = false)
    var result: Result = Result(), // Default constructor added

    @field:ElementList(name = "row", inline = true, required = false)
    var arrivals: MutableList<RealtimeArrival> = mutableListOf() // Mutable list for row elements
)

@Root(name = "RESULT", strict = false)
data class Result(
    @field:Element(name = "message", required = false)
    var message: String = "", // Default constructor added

    @field:Element(name = "status", required = false)
    var status: String = "" // Default constructor added
)