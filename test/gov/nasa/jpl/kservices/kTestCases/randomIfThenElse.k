import gov.nasa.jpl.ae.util.distributions.Distribution
import gov.nasa.jpl.ae.util.distributions.Normal

//req Timepoint.setEpoch("2027-365T00:00:00.000")
//req Timepoint.setHorizon("2028-220T00:00:00.000")
// -- req ParameterListenerImpl.setUsingArcConsistency(false)

//var epoch : Time = 0

// scenario constants

var x : Normal = Normal(0.0, 1.0)

var y : Distribution = if x >= 0 then 1 else 0

var yz : Distribution[Bool] = y > 0.5


var z : Real = P(y > 0.5)

var zz : Distribution = if x < 0 then 1 else 0

//var one : Distribution = y + zz

//var pOne : Real = P(one = 1.0)  // This isn't working -- no sampling, maybe thinks any particular value must have probability 0, but not in this case.  The probability should be 1.0.



