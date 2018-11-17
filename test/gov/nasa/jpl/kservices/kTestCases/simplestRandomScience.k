import gov.nasa.jpl.ae.util.distributions.*

req Timepoint.setEpoch("2027-365T00:00:00.000")
req Timepoint.setHorizon("2028-220T00:00:00.000")
-- req ParameterListenerImpl.setUsingArcConsistency(false)

var epoch : Time = 0
var zerod : EnumeratedDistribution[Double] = EnumeratedDistribution(0.0)
var zeroi : EnumeratedDistribution[Long] = EnumeratedDistribution(epoch)

// scenario constants

var firstScience : Time = 2028-001T00:00:00.000
var periodBetweenScience : Duration = days(13.5)
var scienceDuration : Duration = hours(6.0)
var meanTimeBetweenFaults : Duration = days(100.0)

// main model

//var maxSucceeded : Int = Timepoint.getHorizonDuration() / periodBetweenScience
//
//var f1 : Fault = Fault(lastTime::zeroi)
//var f2 : Fault = Fault(lastTime::f1.distStartTime)
//var f3 : Fault = Fault(lastTime::f2.distStartTime)
//
//class Fault extends DurativeEvent {
//    req duration = 1
//
//    var lastTime : Distribution
//    var timeToNextFault : Exponential = Exponential(meanTimeBetweenFaults, Exponential(2*meanTimeBetweenFaults))
//    var distStartTime : FunctionOfDistributions = lastTime + timeToNextFault
//    req distStartTime.setMaxSamples(10)
//    req startTime = distStartTime.mean() as Time
//
//    var failure : Distribution = (((distStartTime - firstScience) % periodBetweenScience) < scienceDuration)
//    var numFail : Distribution[Int] = if failure then 1 else 0
//}
//
//var totalFail : Distribution[Int] = f1.numFail + f2.numFail + f3.numFail
//var numSucceeded : Distribution[Int] = maxSucceeded - totalFail


var timeToNextFault : Exponential = Exponential(meanTimeBetweenFaults, Exponential(2*meanTimeBetweenFaults))
//var distStartTime : FunctionOfDistributions = lastTime + timeToNextFault
//req distStartTime.setMaxSamples(10)

req failure.setMaxSamples(10000)
//var failure : FunctionOfDistributions = ((timeToNextFault % periodBetweenScience) < scienceDuration)
var failure : FunctionOfDistributions
req failure = ((timeToNextFault + 0.0) < scienceDuration)

//var f2 : FunctionOfDistributions = ((timeToNextFault % periodBetweenScience) < scienceDuration)

//var goal : FunctionOfDistributions = numSucceeded > 0.95 * maxSucceeded
//req goal.setMaxSamples(100000)
var p : Real = P( failure )
