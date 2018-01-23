-- generated from lightSwitch.scenario

var horizonDur : Long = 10928118000
req Timepoint.setUnits("milliseconds")
req Timepoint.setEpoch("Mon Mar 10 03:00:00 PDT 2028")
req Timepoint.setHorizonDuration(horizonDur)


class Light {
    class PowerMode
    val OFF: PowerMode
    val ON: PowerMode
    
    var pmode: TimeVaryingMap[PowerMode]
    class IS_ON extends DurativeEvent {
        var pro: Long
        req time >= startTime && time < endTime => pmode = ON
        var goal00000: TO_ON = TO_ON(startTime :: pro, endTime :: startTime)
    }
    class TO_ON extends DurativeEvent {
        req startTime + seconds(20.0) <= endTime
        req startTime + seconds(30.0) >= endTime
        pmode.setValue(startTime, ON)
    }

--    fun isOn(b : Long, e : Long): IS_ON {
--        IS_ON(startTime :: b, endTime :: e)
--    }

}

class BradTest {
    
    var actbegin: Long
    var actend: Long
    req actbegin = 2028-05-01T13:14:15.000
    req actbegin + Timepoint.hours(4.0) <= actend
    req actbegin + Timepoint.hours(4.0) >= actend
    var system: Light = Light()  -- TODO -- these constructors should not be necessary and should be generated automatically.
    
    var goal00001: IS_ON = system.IS_ON(startTime :: actbegin, endTime :: actend)
    -- var goal00001: IS_ON = system.isOn(actbegin, actend)
}

var b: BradTest = BradTest()  -- TODO -- these constructors should not be necessary and should be generated automatically.
