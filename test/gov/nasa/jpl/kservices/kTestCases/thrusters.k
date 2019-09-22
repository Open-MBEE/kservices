ParameterListenerImpl.setArcConsistencyQuiet(false) // DEBUG
Parameter.setPrintOnSetValue(true) // DEBUG
Parameter.setPrintOnRestrictDomain(true) // DEBUG
ParameterListenerImpl.setQuitEarlyWhenInconsistent(false)
import java.lang.Math

// objective - smallest time on 8 thrusters with no penalty
var min_t8 : Real = minimize("t8")
//req additionalDeltaV = 0.0
//req t_delay > 0.0
t8 + t6 + t4 <= 7.5

// constants: assume these are given some value
var nominalDeltaV : Real = 900.0 // the nominal delta-v goal
var tau_nominal : Real = 6.5 * 8.0  // nominal thruster-hrs to goal
var T_undervolt : Real = 0.30 // battery undervolt threshold
var R : Real = 0.018     // battery recharge \% per hr
var D : Real = 0.074     // battery discharge \% per hr
var T1: Real = 0.0       // delay bound 1 hrs
var T2: Real = 12.0      // delay bound 2 hrs
var T3: Real = 24.0      // delay bound 3 hrs
var T4: Real = 72.0      // delay bound 4 hrs
var m1: Real = 0.0       // delta-v penalty slope bound 1
var m2: Real = 255.3207979 // delta-v penalty slope bound 2
var m3: Real = 548.4686863 // delta-v penalty slope bound 3
var m4: Real = 1042.600881 // delta-v penalty slope bound 4

// declare non-constant variables
var t8 : Real            // time burning on all 8 thrusters
var t6 : Real            // time burning on 6 thrusters
var t4 : Real            // time burning on 4 thrusters
var t8_1 : Real          // time on 8 before undervolt
var t6_1 : Real          // time on 6 before undervolt
var t4_1 : Real          // time on 4 before undervolt
var t_delay : Real       // time delay to recover
var additionalDeltaV : Real  // delta-v added for delay
var tau_add : Real       // thruster-hrs added for delay
var C : Real             // \% delta-v achieved before undervolt
var m : Real             // delta-v penalty slope for delay

// keep values positive
req t8 >= 0.0
req t6 = 0.0
req t4 = 0.0

// model the constraints
req tau_add = additionalDeltaV * tau_nominal / nominalDeltaV
req 8.0*t8 + 6.0*t6 + 4.0*t4 = tau_nominal + tau_add
req C = (t8_1 + t6_1 + t4_1) / tau_nominal
req t8_1 = Math.min(t8, T_undervolt)
req t6_1 = Math.min(t6, T_undervolt - t8_1)
req t4_1 = Math.min(t4, T_undervolt - t8_1, t6_1)
req t_delay = R*(T_undervolt - (t8 + t6 + t4))
req additionalDeltaV = m(1 - C)
req additionalDeltaV > 0.0 && additionalDeltaV < D

// interpolate m in m1,..,m4 for t_delay in T1,..,T4
req m = if t_delay >= T1 && t_delay < T2
        then m1 + (t_delay - T1) * (m2 - m1) / (T2 - T1)
        else if t_delay >= T2 && t_delay < T3
        then m2 + (t_delay - T2) * (m3 - m2) / (T3 - T2)
        else m3 + (t_delay - T3) * (m4 - m3) / (T4 - T3)
