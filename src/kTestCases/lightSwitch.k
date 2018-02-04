-- generated from lightSwitch.scenario and then hand edited


class Light {
    class PowerMode
    val OFF: PowerMode
    val ON: PowerMode
    
    var pmode: TimeVaryingMap[PowerMode]
    class IS_ON extends DurativeEvent {
        var pro: Time
        req time >= startTime && time < endTime => pmode = ON
        var goal00000: TO_ON = TO_ON(startTime :: pro, endTime :: startTime)
    }
    class TO_ON extends DurativeEvent {
        req startTime + PT20S <= endTime
        req startTime + PT30S >= endTime
        req pmode.setValue(startTime, ON)
    }

}

class BradTest {
    
    var actbegin: Time
    var actend: Time
    req actbegin = 2028-05-01T13:14:15.000
    req actbegin + PT4H <= actend
    req actbegin + PT4H >= actend
    var system: Light
    
    var goal00001: IS_ON = system.IS_ON(startTime :: actbegin, endTime :: actend)

}

var b: BradTest

req startTime = 2028-05-01T00:00:00.000
req endTime = 2028-05-02T00:00:00.000
