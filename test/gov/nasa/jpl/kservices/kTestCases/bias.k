import gov.nasa.jpl.ae.event.DistributionFunctionCall
import gov.nasa.jpl.ae.util.distributions.*

var x1 : Exponential = Exponential(1)
var x1b2 : Exponential = Exponential(1, Exponential(2))
var x1bu : Exponential = Exponential(1, UniformReal(0,4))

var analytic : Real = P(x1 < 3)
// Adding 0.0 to force sampling
var px1LT3 : Real = P(x1 + 0.0 < 3)
var px1b2LT3 : Real = P(x1b2 + 0.0 < 3)
var px1buLT3 : Real = P(x1bu + 0.0 < 3, false)
