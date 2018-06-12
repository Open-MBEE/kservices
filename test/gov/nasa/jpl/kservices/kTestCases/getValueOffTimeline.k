setHorizonDuration(100)

var epoch : Long = 0

var x : TimeVaryingMap[Real]
x.setValue(epoch, 1.5)
var ten : Long = 10
x.setValue(ten, 4.4)

var t : Time = 20
var y : Real = x.getValue(t)
// var Z : Real = x.getValue(20)  // THIS DOESN'T WORK!

