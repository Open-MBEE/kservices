import gov.nasa.jpl.ae.event.ParameterListenerImpl

ParameterListenerImpl.setUsingArcConsistency(true)
ParameterListenerImpl.setMaxLoopsWithNoProgress(1200)

var x : Real
var y : Real
var z : Real = 5.0
//var sq : Real  // sqrt(5.0) = 2.23606797749978969640
//
//req sq > 2.0 && sq < 3.0
//req x > 2.0 && sq < 3.0
//req y > 2.0 && sq < 3.0
//
///*
//-- req sq * sq = z
//req y = z - sq * sq
//req x = y / 2.0
//req sq = x + z / sq
//*/
//
//var e : Real
///*
//req e >= 0.0
//req e = z - sq * sq
//req x = sq + e
//req y = sq - e
//
//req sq = (x + y) / 2
//
//req x * y = z
//*/
//
//req e = z - sq * sq
//
//req x = if e > 0.0 then sq else x - abs((sq-x)/10.0)
//req y = if e < 0.0 then sq else y + abs((y-sq)/10.0)
////req x = if e > 0 then (sq + x) / 2.0  else x
////req y = if e < 0 then (sq + y) / 2.0  else y
//
//req sq = (x + y) / 2.0
//
//req y > x
//req y >= sq
//req x <= sq
req w > 0.0
req w < 10.0
var w : Real = (z+w)/2