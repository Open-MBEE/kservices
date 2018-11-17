import gov.nasa.jpl.ae.util.distributions.*
import gov.nasa.jpl.ae.solver.ClassDomain

req Timepoint.setEpoch("2027-365T00:00:00.000")
req Timepoint.setHorizon("2028-220T00:00:00.000")
ParameterListenerImpl.setQuitEarlyWhenInconsistent(false)
-- req ParameterListenerImpl.setUsingArcConsistency(false)

var epoch : Time = 0

// scenario constants

var firstScience : Time = 2028-001T00:00:00.000
var periodBetweenScience : Duration = weeks(13.5)
var scienceDuration : Duration = hours(6.0)
var meanTimeBetweenFaults : Duration = years(0.1)

var zerod : EnumeratedDistribution[Double] = EnumeratedDistribution(0.0)
var zeroi : EnumeratedDistribution[Long] = EnumeratedDistribution(epoch)

// main model

var maxSucceeded : Int = Timepoint.getHorizonDuration() / periodBetweenScience

var nullFault : Fault = null
var firstFault : Fault = Fault(prevFault::nullFault)
var lastFault : Fault
class Fault extends DurativeEvent {
    var prevFault : Fault
    req duration = 1

    var lastTime : Distribution = if prevFault = null then zeroi else prevFault.distStartTime
    var timeToNextFault : Exponential = Exponential(meanTimeBetweenFaults, Exponential(meanTimeBetweenFaults/2))
    var distStartTime : Distribution = lastTime + timeToNextFault
    req startTime = distStartTime.mean() as Time

    var failure : Distribution = ((distStartTime - firstScience) % periodBetweenScience) < scienceDuration
    var lastNumFail : Distribution[Int] = if prevFault = null then zeroi else prevFault.numFail
    var numFail : Distribution[Int] = lastNumFail + if failure then 1 else 0

    var pastHorizon : Bool = startTime > Timepoint.getHorizonDuration()  // try getHorizon()

    req if pastHorizon then lastFault = prevFault else true

    var nextFault : Fault = if pastHorizon then nullFault else Fault(prevFault::this)
//    var setLastFault : MakeEq =  if pastHorizon then MakeEq(v1::lastFault, v2::prevFault) else null

//    var o1 : Object = (setLastFault.getDomain() as ClassDomain).setNullInDomain(true)
//    var o2 : Object = (prevFault.getDomain() as ClassDomain).setNullInDomain(true)
//    var o3 : Object = (nextFault.getDomain() as ClassDomain).setNullInDomain(true)
}

var numSucceeded : Distribution[Int] = EnumeratedDistribution(maxSucceeded) - if lastFault = null then zeroi else lastFault.numFail

//var goal : FunctionOfDistributions = numSucceeded > 0.95 * maxSucceeded
//
//req goal.setMaxSamples(100000)
//
//var p : Real = P( numSucceeded > 0.95 * maxSucceeded )

var goal : FunctionOfDistributions = numSucceeded > 0.95 * maxSucceeded
req goal.setMaxSamples(100000)
var p : Real = P( goal )


//class MakeEq extends DurativeEvent {
//    req startTime = epoch
//    req duration = 1
//
//    var v1 : Fault
//    var v2 : Fault
//
//    req v1 = v2
//}
