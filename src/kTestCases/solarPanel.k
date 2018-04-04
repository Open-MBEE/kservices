Timepoint.setEpoch("2020-01-01T00:00:00.000")

-- For the "Global" class.
req startTime = 2020-01-01T00:00:00.000
var zero : Long = 0

class clipper {

  -- var batterySOC : TimeVaryingMap[ Double ]
  -- req batterySOC >= 0.0 && batterySOC <= 1.0
  -- batterySOC.setValue(zero, 0.0)

  var solarPanelOrientation : TimeVaryingMap[ Double ]
  solarPanelOrientation.setValue(zero, 0.0)
  req solarPanelOrientation >= -180.0 && solarPanelOrientation <= 180.0

  class turnPanel1 extends DurativeEvent {

    var curr : Real = spacecraft.solarPanelOrientation.getValue(startTime)

    var done : Bool = (curr <= -180.0)

    val change : Real = if done then 0.0 else -90.0
    new_dur : Duration = if done then 0 else seconds(45.0)

    --spacecraft.solarPanelOrientation.add(change, startTime, endTime)
    spacecraft.solarPanelOrientation.add(0.0, startTime)
    spacecraft.solarPanelOrientation.add(change, endTime)
    req duration = new_dur

    var nextTurn : turnPanel1 = if solarPanelOrientation.getValue(endTime) <= -180.0 then null else turnPanel1(startTime::endTime)
  }

  --  var makeTurns : Set[ turnPanel1 ]

}


var spacecraft : clipper = clipper(solarPanelOrientation::TimeVaryingMap.zero)

class JOC extends DurativeEvent {
  var clip : clipper
  var doTurn : turnPanel1  = clip.turnPanel1(startTime::startTime)
  req time >= startTime && time <= endTime => clip.solarPanelOrientation = -180.0
}

var st : Time = 2024-354T11:05:01.616
var joc : JOC = JOC(clip::spacecraft, startTime::st)