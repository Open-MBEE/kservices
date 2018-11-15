import gov.nasa.jpl.ae.util.distributions.*

req Timepoint.setEpoch("2027-365T00:00:00.000")
req Timepoint.setHorizon("2028-220T00:00:00.000")
-- req ParameterListenerImpl.setUsingArcConsistency(false)



var periodBetweenScience : Real = 200
var meanTimeBetweenFaults : Real = 100.0

//var x : Exponential = Exponential(meanTimeBetweenFaults)
var x : Exponential = Exponential(meanTimeBetweenFaults, Exponential(2*meanTimeBetweenFaults))
//var x : Exponential = Exponential(meanTimeBetweenFaults, UniformReal(0.0, 100.0 * meanTimeBetweenFaults))

//var y : Exponential = Exponential(meanTimeBetweenFaults)

//var xgoal : Distribution = x < periodBetweenScience
var xgoal : FunctionOfDistributions = x + 0.0 < periodBetweenScience
//var xgoal : FunctionOfDistributions = x + x < periodBetweenScience + periodBetweenScience
req xgoal.setMaxSamples(3000000)
var px : Real = P( xgoal )

//var ygoal : Distribution = y < periodBetweenScience
//req ygoal.setMaxSamples(100000)
//var py : Real = P( ygoal )
