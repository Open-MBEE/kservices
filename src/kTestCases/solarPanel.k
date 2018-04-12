Timepoint.setEpoch("2024-354T00:00:00.000")

-- For the "Global" class.
req startTime = 2024-354T00:00:00.000
var zero : Long = 0

class clipper {

  -- var batterySOC : TimeVaryingMap[ Double ]
  -- req batterySOC >= 0.0 && batterySOC <= 1.0
  -- batterySOC.setValue(zero, 0.0)

  var solarPanelOrientation : TimeVaryingMap[ Real ] =  TimeVaryingMap("x", null, 0.0, Double.class, TimeVaryingMap.LINEAR)
  -- solarPanelOrientation.setValue(zero, 0.0)
  req solarPanelOrientation >= -180.0 && solarPanelOrientation <= 180.0

  class turnPanel1 extends DurativeEvent {

    var curr : Real = spacecraft.solarPanelOrientation.getValue(startTime)

    var done : Bool = (curr <= -180.0)

    var change : Real = if done then 0.0 else -90.0
    var new_dur : Duration = if done then 0 else seconds(45.0)

    --spacecraft.solarPanelOrientation.add(change, startTime, endTime)
    spacecraft.solarPanelOrientation.add(change, endTime)
    req duration = new_dur

    var nextTurn : turnPanel1 = if solarPanelOrientation.getValue(endTime) <= -180.0 then null else turnPanel1(startTime::endTime, duration::new_dur)
  }

  --  var makeTurns : Set[ turnPanel1 ]

}


var spacecraft : clipper = clipper(solarPanelOrientation::TimeVaryingMap.zero)

class JOC extends DurativeEvent {
  var clip : clipper
  -- var doTurn : turnPanel1 = clip.turnPanel1(startTime::startTime)
  clip.turnPanel1(startTime::startTime, duration::seconds(45.0))
  req time >= startTime && time <= endTime => clip.solarPanelOrientation = -180.0
}

var st : Time = 2024-354T11:05:01.616
var joc : JOC = JOC(clip::spacecraft, startTime::st)