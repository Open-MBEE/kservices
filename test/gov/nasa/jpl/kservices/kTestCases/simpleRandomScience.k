import gov.nasa.jpl.ae.util.distributions.Distribution
import gov.nasa.jpl.ae.util.distributions.Exponential

req Timepoint.setEpoch("2027-365T00:00:00.000")
req Timepoint.setHorizon("2028-220T00:00:00.000")
-- req ParameterListenerImpl.setUsingArcConsistency(false)

var epoch : Time = 0

// scenario constants

var firstScience : Time = 2028-001T00:00:00.000
var periodBetweenScience : Duration = weeks(13.5)
var scienceDuration : Duration = hours(6.0)
var meanTimeBetweenFaults : Duration = years(0.1)

// main model

var maxSucceeded : Int = Timepoint.getHorizonDuration() / periodBetweenScience

var firstFault : Fault = Fault(prevFault::null)
var lastFault : Fault
class Fault extends DurativeEvent {
    var prevFault : Fault
    req duration = 1

    var lastTime : Distribution = if prevFault = null then epoch else prevFault.distStartTime
    var timeToNextFault : Exponential = Exponential(meanTimeBetweenFaults)
    var distStartTime : Distribution = lastTime + timeToNextFault
    req startTime = distStartTime.mean() as Time

    var failure : Distribution = ((distStartTime - firstScience) % periodBetweenScience) < scienceDuration
    var lastNumFail : Distribution[Int] = if prevFault = null then Normal(0,0.000000001) else prevFault.numFail
    var numFail : Distribution[Int] = lastNumFail + if failure then 1 else 0

    var pastHorizon : Bool = startTime > Timepoint.getHorizonDuration()  // try getHorizon()

    req if pastHorizon then lastFault = prevFault else true

    var nextFault : Fault = if pastHorizon then null else Fault(prevFault::this)
}

var numSucceeded : Distribution[Int] = maxSucceeded - if lastFault = null then 0 else lastFault.numFail

var p : Real = P( numSucceeded > 0.95 * maxSucceeded )
