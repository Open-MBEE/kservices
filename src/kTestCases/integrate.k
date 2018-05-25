Timepoint.setEpoch("2024-353T12:00:00")
Timepoint.setHorizonDuration(days(2.0))

var epoch : Time = 0

var t1 : Time = epoch + hours(1.0)
var t2 : Time = epoch + hours(12.0)
var t3 : Time = epoch + hours(36.0)

var battery : Battery

battery.power.setValue(epoch, 0)
battery.power.setValue(t1, 10)
battery.power.setValue(t2, 30)
battery.power.setValue(t3, 15)

class Battery {
    var power : TimeVaryingMap[Real]
    var energy : TimeVaryingMap[Real] = power.integrate()
}
