import gov.nasa.jpl.ae.util.distributions.*
//import gov.nasa.jpl.ae.util.distributions.{Distribution, Exponential, Normal}

var three : Real = 3
var x : Exponential = Exponential(1.0)

var z : Real = 0.0
var n : Normal = Normal(1.0, 1.0)

var sumxn : Distribution = x + n


var p : Real = P(sumxn > 2.5)
