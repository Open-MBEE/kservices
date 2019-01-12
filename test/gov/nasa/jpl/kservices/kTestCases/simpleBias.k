import gov.nasa.jpl.ae.util.distributions.*

req Timepoint.setEpoch("2027-365T00:00:00.000")
req Timepoint.setHorizon("2028-220T00:00:00.000")

var periodBetweenScience : Real = 200
var meanTimeBetweenFaults : Real = 100.0

var x : Exponential = Exponential(meanTimeBetweenFaults, UniformReal(0, meanTimeBetweenFaults+periodBetweenScience))
//var x : Exponential = Exponential(meanTimeBetweenFaults)
var goal : FunctionOfDistributions = x + 0.0 < periodBetweenScience
req goal.setMaxSamples(1000000)
var px : Real = P( goal, false )
