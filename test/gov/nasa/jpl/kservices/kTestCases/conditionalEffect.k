import gov.nasa.jpl.ae.event.TimeVaryingMap

var b : Bool = true

var x : TimeVaryingMap[Int]
var y : TimeVaryingMap[Int]

var t : Time = 0

var v1 : Int = 3

if ( b ) then x.setValue(t, v1) else y.setValue(t, v1)

var v2 : Int = 5

if ( !b ) then x.setValue(t, v2) else y.setValue(t, v2)
