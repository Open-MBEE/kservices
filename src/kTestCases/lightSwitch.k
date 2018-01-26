-- generated from lightSwitch.scenario


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

--    fun isOn(b : Long, e : Long): IS_ON {
--        IS_ON(startTime :: b, endTime :: e)
--    }

}

class BradTest {
    
    var actbegin: Time
    var actend: Time
    req actbegin = 2028-05-01T13:14:15.000
    req actbegin + PT4H <= actend
    req actbegin + PT4H >= actend
    var system: Light = Light()  -- TODO -- these constructors should not be necessary and should be generated automatically.
    
    var goal00001: IS_ON = system.IS_ON(startTime :: actbegin, endTime :: actend)
    -- var goal00001: IS_ON = system.isOn(actbegin, actend)
}

var b: BradTest = BradTest()  -- TODO -- these constructors should not be necessary and should be generated automatically.
