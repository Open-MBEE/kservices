req Timepoint.setEpoch("2020-001T00:00:00.000")
req Timepoint.setHorizon("2020-001T10:00:00.000")

var epoch : Time = 0
var t1 : Time = epoch + PT1H
var t2 : Time = t1 + PT1H

var a: TimeVaryingMap[String]
var b: TimeVaryingMap[String]

a.setValue(epoch, "off")
a.setValue(t1, "on")
a.setValue(t2, "off")

req c1: a = "on" => b = "off"
req c2: a = "off" => b = "on"
//
//var changed : TimeVaryingMap[Bool]
//req changed = b != b.shift(-1)

