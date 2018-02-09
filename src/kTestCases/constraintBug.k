====
class Light extends DurativeEvent {
        req startTime + PT20S <= endTime
        req startTime + PT30S >= endTime
}
====

class BradTest extends DurativeEvent {
    
    var actbegin: Time
    --var actend: Time

    req actbegin = 2028-05-01T13:14:15.000

    --req actbegin + PT4H <= actend
    --req actbegin + PT4H >= actend

    req startTime = actbegin
    req duration = minutes(1)

    -- var system: Light = Light(startTime :: actbegin, endTime :: actend)

}

var foo: Time
var b: BradTest = BradTest(startTime :: foo)

req startTime = 2028-05-01T00:00:00.000
req endTime = 2028-05-02T00:00:00.000
