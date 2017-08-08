class ReasonData extends DurativeEvent {
    req startTime = 0
    req endTime = Timepoint.getHorizonDuration()

    var EncounterPhase : TimeVaryingMap = TimeVaryingMap("","/Users/ayelaman/git/baeModels/europa/data/resources/EncounterPhase.csv", null, String.class)
    var flyby : TimeVaryingMap = !EncounterPhase.contains("Playback")
    var h : TimeVaryingMap = TimeVaryingMap("", "/Users/ayelaman/git/baeModels/europa/data/resources/SpacecraftEuropaAltitude.csv", null, Double.class)
    var altIndex : TimeVaryingMap = TimeVaryingMap("altIndex", null, 0, Integer.class, TimeVaryingMap.LINEAR)
}


class Encounter extends DurativeEvent {

}

var r : ReasonData

req r.elaborates(null, Encounter.class, r.flyby, r.h, r.altIndex)
