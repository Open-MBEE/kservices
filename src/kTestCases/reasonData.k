class ReasonData extends DurativeEvent {
    req startTime = 0
    req endTime = Timepoint.getHorizonDuration()

    var EncounterPhase : TimeVaryingMap = TimeVaryingMap("","../baeModels/europa/data/resources/EncounterPhase.csv", null, String.class)
    var flyby : TimeVaryingMap = !EncounterPhase.contains("Playback")
    var h : TimeVaryingMap = TimeVaryingMap("", "../baeModels/europa/data/resources/SpacecraftEuropaAltitude.csv", null, Double.class)
    var altIndex : TimeVaryingMap = TimeVaryingMap("altIndex", null, 0, Integer.class, TimeVaryingMap.LINEAR)
}

class Encounter extends DurativeEvent {
--    var h : TimeVaryingMap
--    var altIndex : TimeVaryingMap
}


var r : ReasonData
req startTime = 0
req endTime = Timepoint.getHorizonDuration()

-- req r.elaborates(null, Encounter.class, r.flyby, "h", r.h, "altIndex", r.altIndex)
