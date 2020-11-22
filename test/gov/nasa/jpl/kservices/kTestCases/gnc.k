import kTestCases.GNC
//import gov.nasa.jpl.kservices.GNC.TARGET
//myorg.GNC.TARGET
//myorg.GNC.slew

//var tx : String = "TARGET_X"
//var ty : String = "TARGET_Y"
//var tz : String = "TARGET_Z"

//var tx : Int = TARGET.X.ordinal()
//var ty : Int = TARGET.y.ordinal()
//var tz : Int = TARGET.z.ordinal()

var tx : kTestCases.GNC.TARGET = kTestCases.GNC.TARGET.X
var ty : kTestCases.GNC.TARGET = kTestCases.GNC.TARGET.Y
var tz : kTestCases.GNC.TARGET = kTestCases.GNC.TARGET.Z

//var slewSeq : Seq[TARGET]
//req slewSeq.size() = 3
//
//req TARGET.X isin slewSeq
//req TARGET.Y isin slewSeq
//req TARGET.Z isin slewSeq

var t1 : kTestCases.GNC.TARGET
var t2 : kTestCases.GNC.TARGET
var t3 : kTestCases.GNC.TARGET

//var t1 : Int
//var t2 : Int
//var t3 : Int

fun reqT( ta : Int, tb : Int ) : Bool {
  (ta = tx || ta = ty || ta = tz) &&
  ta != tb
}

req reqT(t1, t2)
req reqT(t2, t3)
req reqT(t3, t1)

//req t1 != t2
//req t2 != t3
//req t1 != t3

var slewDur : Int = GNC.slew(t1,t2) + GNC.slew(t2,t3)

//var slewDur : Int =
//  slew(slewSeq[0],slewSeq[1]) +
//  slew(slewSeq[1],slewSeq[2])

req slewDur < 120 // seconds

var minTime : Int = minimize(slewDur)
