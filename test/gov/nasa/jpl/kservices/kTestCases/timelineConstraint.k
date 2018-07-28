// Meant to test K's ability to map a requirement over a timeline

var start : Time = seconds(1.0)
var end   : Time = seconds(5.0)

var x : TimeVaryingMap = time * 2
// x.setValue(start, start * 2)
// x.setValue(end  , end   * 2)

req time >= start && time <= end => x > 0

var r1 : TimeVaryingMap[boolean] = time >= start && time <= end => x < 0
var r2 : TimeVaryingMap[boolean] = time >= start
