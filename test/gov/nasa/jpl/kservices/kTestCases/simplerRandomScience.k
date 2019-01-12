import gov.nasa.jpl.ae.util.distributions.Distribution
import gov.nasa.jpl.ae.util.distributions.Exponential
import gov.nasa.jpl.ae.util.distributions.EnumeratedDistribution

req Timepoint.setEpoch("2027-365T00:00:00.000")
req Timepoint.setHorizon("2028-220T00:00:00.000")
-- req ParameterListenerImpl.setUsingArcConsistency(false)

var epoch : Time = 0
var h : Duration = days(220.0)
var zerod : EnumeratedDistribution[Double] = EnumeratedDistribution(0.0)
var zeroi : EnumeratedDistribution[Long] = EnumeratedDistribution(epoch)

// scenario constants

var firstScience : Time = 0
var periodBetweenScience : Duration = days(13.5)
var scienceDuration : Duration = hours(6.0)
var meanTimeBetweenFaults : Duration = days(100.0)

// main model

var maxSucceeded : Int = Timepoint.getHorizonDuration() / periodBetweenScience

var ubias : UniformReal = UniformReal(0.0,h)
var nobias : Distribution = null
var f1 : Fault = Fault(lastTime::zeroi)//, bias::nobias)
var f2 : Fault = Fault(lastTime::f1.distStartTime)//, bias::null)
var f3 : Fault = Fault(lastTime::f2.distStartTime)//, bias::null)
var f4 : Fault = Fault(lastTime::f3.distStartTime)//, bias::null)
var f5 : Fault = Fault(lastTime::f4.distStartTime)//, bias::null)
var f6 : Fault = Fault(lastTime::f5.distStartTime)//, bias::null)

class Fault extends DurativeEvent {
    req startTime = 0
    req duration = 1

    var lastTime : Distribution //= if prevFault = null then zeroi else prevFault.distStartTime
    //var bias : Distribution = null
    var timeToNextFault : Exponential = Exponential(meanTimeBetweenFaults)
    var distStartTime : FunctionOfDistributions = lastTime + timeToNextFault

    var failure : Distribution = (((distStartTime - firstScience) % periodBetweenScience) < scienceDuration)
    var numFail : Distribution[Int] = if failure then 1 else 0
}

var totalFail : Distribution[Int] = f1.numFail + f2.numFail + f3.numFail + f4.numFail + f5.numFail + f6.numFail
var numSucceeded : Distribution[Int] = maxSucceeded - totalFail

//var failureMean : Real = f1.failure.mean()
//var totalFailMean : Real = totalFail.mean()
//var numSucceededMean : Real = numSucceeded.mean()

var goal : FunctionOfDistributions = totalFail < 1 //numSucceeded > 0.95 * maxSucceeded
req goal.setMaxSamples(12121)
var p : Real = P( goal, true )
