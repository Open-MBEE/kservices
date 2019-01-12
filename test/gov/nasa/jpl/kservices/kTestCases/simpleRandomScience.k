import gov.nasa.jpl.ae.util.distributions.*  // Based on apache commons distributions.

// The time window of the scenario.
req Timepoint.setEpoch("2027-365T00:00:00.000")
req Timepoint.setHorizon("2028-220T00:00:00.000")

ParameterListenerImpl.setQuitEarlyWhenInconsistent(false) // gets around a bug

var epoch : Time = 0

// scenario constants

// The time that the first science activity occurs.
var firstScience : Time = 2028-001T00:00:00.000
// The time period between science activity start times.
var periodBetweenScience : Duration = days(13.5)
// The duration of science activities.
var scienceDuration : Duration = hours(6.0)
// The average time between fault occurrences.
var meanTimeBetweenFaults : Duration = days(100.0)

// a distribution for the integer constant 0, used for type compatibility
var zeroi : EnumeratedDistribution[Long] = EnumeratedDistribution(epoch)

// main model

// The number of science opportunities, the max that can succeed
var maxSucceeded : Int = Timepoint.getHorizonDuration() / periodBetweenScience

// The beginning and end of the series of faults.
var firstFault : Fault = Fault(prevFault::null)
var lastFault : Fault

// Alternative biases on the arrival times of faults.
var bias2x : Distribution = Exponential(meanTimeBetweenFaults*2)
var biasHalf : Distribution = Exponential(meanTimeBetweenFaults/2)
var noBias : Distribution = null
var biasUniform : Distribution = UniformReal(0, Timepoint.getHorizonDuration())

/**
  * A fault that generates subsequent faults.  The time of the fault, distStartTime,
  * is a random variable with an Exponential distribution, offset from the time
  * of the previous fault.
  *
  * The extension of DurativeEvent means that this is an activity with time variables.
  */
class Fault extends DurativeEvent {
    var prevFault : Fault // passed in as a parameter

    // The startTime is used to determine when to stop generating faults and is
    // not random.  (distStartTime is the actual time of the fault.)  Since the
    // startTimes are spaced apart by half of the meanTimeBetweenFaults, then the
    // number of faults generated is double the average for the scenario timeframe.
    req startTime =
      if prevFault = null then 0
                          else prevFault.startTime + meanTimeBetweenFaults/2
    req duration = 1  // unused -- just grounding

    // The time of the previous fault
    var lastTime : Distribution = if prevFault = null then zeroi else prevFault.distStartTime
    // The bias for importance sampling just on the first fault.
    var biasUniformOrNull : Distribution = if prevFault = null then biasUniform else null
    var bias : Distribution = biasUniformOrNull
    // The time between the last fault and this one.
    var timeToNextFault : Exponential = Exponential(meanTimeBetweenFaults, bias)
    // The time of the fault
    var distStartTime : Distribution = lastTime + timeToNextFault

    // The fault fails if it falls within the 6 hour scienceDuration window that
    // occurs every 13.5 days (periodBetweenScience).
    var failure : Distribution =
                ((distStartTime - firstScience) % periodBetweenScience) < scienceDuration

    // Track the number of failures for the sequence of faults.
    var lastNumFail : Distribution[Int] = if prevFault = null then zeroi else prevFault.numFail
    var numFail : Distribution[Int] = lastNumFail + if failure then 1 else 0

    // The maximum number of faults is fixed based on the schedule horizon and
    // the spacing of startTimes.
    var pastHorizon : Bool = startTime > Timepoint.getHorizonDuration()

    // Constrain the lastFault variable to equal this fault if it is the last.
    req if pastHorizon then lastFault = prevFault else true

    // Generate the next fault unless past the horizon
    var nextFault : Fault = if pastHorizon then null else Fault(prevFault::this)
}

// The number of science activities that succeed
var numSucceeded : Distribution[Int] =
    maxSucceeded -  // constant as Distribution for type compatibility.
    if lastFault = null then zeroi else lastFault.numFail

// The science requirement is that 95% of the activities succeed.
var goal : FunctionOfDistributions = numSucceeded > 0.95 * maxSucceeded
// Specify the number of samples to take to estimate the goal.
req goal.setMaxSamples(1000000)

// Get the probability that the goal is achieved.
var p : Real = P( goal, true )

// Get the average number of science activities to succeed.
var expectedNumsucceeded : Real = numSucceeded.mean()

