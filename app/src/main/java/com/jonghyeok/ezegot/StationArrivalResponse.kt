package com.jonghyeok.ezegot

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "realtimeStationArrival", strict = false)
data class StationArrivalResponse(
    @field:Element(name = "RESULT", required = false)
    var result: Result = Result(), // Default constructor added

    @field:ElementList(name = "row", inline = true, required = false)
    var arrivals: MutableList<Arrival> = mutableListOf() // Mutable list for row elements
)

@Root(name = "RESULT", strict = false)
data class Result(
    @field:Element(name = "message", required = false)
    var message: String = "", // Default constructor added

    @field:Element(name = "status", required = false)
    var status: String = "" // Default constructor added
)

@Root(name = "row", strict = false)
data class Arrival(
    @field:Element(name = "trainLineNm", required = false)
    var trainLineName: String = "", // Default constructor added

    @field:Element(name = "btrainNo", required = false)
    var trainNumber: String = "", // Default constructor added

    @field:Element(name = "arvlMsg2", required = false)
    var arrivalMessage: String = "" // Default constructor added
)