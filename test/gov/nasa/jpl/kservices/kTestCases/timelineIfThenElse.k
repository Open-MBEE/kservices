setHorizonDuration(30)

var epoch : Long = 0

var x : TimeVaryingMap[Real]
x.setValue(epoch, 1.5)
var ten : Long = 10
x.setValue(ten, 4.4)

var y : TimeVaryingMap[Real] = if x > 2.0 then 1.0 else 0.0

var b : Bool
var z : TimeVaryingMap[Real] = if b then y + 1.0 else null
