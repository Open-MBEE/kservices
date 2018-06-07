import gov.nasa.jpl.ae.event.TimeVaryingPlottableMap

setEpoch("2001-001T12:00:00")
setHorizon("2001-001T12:00:01")
var epoch : Long = 0
var e : Long = 100
var x : TimeVaryingMap[Real] = TimeVaryingMap("x", null, 0.0, Double.class, TimeVaryingMap.LINEAR)
x.setValue(e, 1.0)

var period : Long = 20
var y : TimeVaryingMap[Real] = x.sample(period, TimeVaryingMap.LINEAR)

var foo : TimeVaryingPlottableMap[Real] =
    TimeVaryingPlottableMap[Real]( "load", "Math", "sin", null, 
                                     period, Timepoint.getHorizonDuration(), true )
