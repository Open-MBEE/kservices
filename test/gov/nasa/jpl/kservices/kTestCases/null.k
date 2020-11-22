import gov.nasa.jpl.ae.event.TimeVaryingMap
import gov.nasa.jpl.ae.util.distributions.EnumeratedDistribution
// verify handling of null values in variables

var epoch : Time = 0
var nullObj : Object = null
var nullStr : String = null
var nullTvm1 : TimeVaryingMap[Object] = null //TimeVaryingMap("nullTvm1",null,null,Object.class)
var nullTvm2 : TimeVaryingMap[Object]
nullTvm2.setValue(epoch, null)
var nullTvm3 : TimeVaryingMap[Object]
nullTvm3.setValue(epoch, null)
nullTvm3.setValue(seconds(1.0), null)
var nullDist : EnumeratedDistribution[Object] = EnumeratedDistribution(nullObj)

// TODO -- Do we want Sequences and Sets of null to be equal to null?

var nonNullStr : String = "iAmNotNull"
var nonNullTvm1 : TimeVaryingMap[String] = TimeVaryingMap("nonNullTvm1",null,"",String.class)
var nonNullTvm2 : TimeVaryingMap[String]
//nonNullTvm2.setValue(epoch, "iAmAlsoNotNull")
var nonNullTvm3 : TimeVaryingMap[String]
nonNullTvm3.setValue(epoch, null)
var oneSec : Time = seconds(1.0)
nonNullTvm3.setValue(oneSec, "")
var nonNullDist : EnumeratedDistribution[String] = EnumeratedDistribution("")

var arrayOfNulls : Seq[Object] = Seq{null, nullObj, nullStr, nullTvm1, nullTvm2, nullTvm3, nullDist}
var arrayOfNonNulls : Seq[Object] = Seq{"", "notNullStrValue", nonNullStr, nonNullTvm1, nonNullTvm2, nonNullTvm3, nonNullDist}

var numNulls : Int = arrayOfNulls.size()
var numNonNulls : Int = arrayOfNonNulls.size()
req numNulls = 7
req numNonNulls = 7

// TODO -- use forall

/*
var nullEqCheck0_0 : Bool = arrayOfNulls[0] = arrayOfNulls[0]
var nullEqCheck0_1 : Bool = arrayOfNulls[0] = arrayOfNulls[1]
var nullEqCheck0_2 : Bool = arrayOfNulls[0] = arrayOfNulls[2]
var nullEqCheck0_3 : Bool = arrayOfNulls[0] = arrayOfNulls[3]
var nullEqCheck0_4 : Bool = arrayOfNulls[0] = arrayOfNulls[4]
var nullEqCheck0_5 : Bool = arrayOfNulls[0] = arrayOfNulls[5]
var nullEqCheck0_6 : Bool = arrayOfNulls[0] = arrayOfNulls[6]

var nullEqCheck1_0 : Bool = arrayOfNulls[1] = arrayOfNulls[0]
var nullEqCheck1_1 : Bool = arrayOfNulls[1] = arrayOfNulls[1]
var nullEqCheck1_2 : Bool = arrayOfNulls[1] = arrayOfNulls[2]
var nullEqCheck1_3 : Bool = arrayOfNulls[1] = arrayOfNulls[3]
var nullEqCheck1_4 : Bool = arrayOfNulls[1] = arrayOfNulls[4]
var nullEqCheck1_5 : Bool = arrayOfNulls[1] = arrayOfNulls[5]
var nullEqCheck1_6 : Bool = arrayOfNulls[1] = arrayOfNulls[6]

var nullEqCheck2_0 : Bool = arrayOfNulls[2] = arrayOfNulls[0]
var nullEqCheck2_1 : Bool = arrayOfNulls[2] = arrayOfNulls[1]
var nullEqCheck2_2 : Bool = arrayOfNulls[2] = arrayOfNulls[2]
var nullEqCheck2_3 : Bool = arrayOfNulls[2] = arrayOfNulls[3]
var nullEqCheck2_4 : Bool = arrayOfNulls[2] = arrayOfNulls[4]
var nullEqCheck2_5 : Bool = arrayOfNulls[2] = arrayOfNulls[5]
var nullEqCheck2_6 : Bool = arrayOfNulls[2] = arrayOfNulls[6]

var nullEqCheck3_0 : Bool = arrayOfNulls[3] = arrayOfNulls[0]
var nullEqCheck3_1 : Bool = arrayOfNulls[3] = arrayOfNulls[1]
var nullEqCheck3_2 : Bool = arrayOfNulls[3] = arrayOfNulls[2]
var nullEqCheck3_3 : Bool = arrayOfNulls[3] = arrayOfNulls[3]
var nullEqCheck3_4 : Bool = arrayOfNulls[3] = arrayOfNulls[4]
var nullEqCheck3_5 : Bool = arrayOfNulls[3] = arrayOfNulls[5]
var nullEqCheck3_6 : Bool = arrayOfNulls[3] = arrayOfNulls[6]

var nullEqCheck4_0 : Bool = arrayOfNulls[4] = arrayOfNulls[0]
var nullEqCheck4_1 : Bool = arrayOfNulls[4] = arrayOfNulls[1]
var nullEqCheck4_2 : Bool = arrayOfNulls[4] = arrayOfNulls[2]
var nullEqCheck4_3 : Bool = arrayOfNulls[4] = arrayOfNulls[3]
var nullEqCheck4_4 : Bool = arrayOfNulls[4] = arrayOfNulls[4]
var nullEqCheck4_5 : Bool = arrayOfNulls[4] = arrayOfNulls[5]
var nullEqCheck4_6 : Bool = arrayOfNulls[4] = arrayOfNulls[6]

var nullEqCheck5_0 : Bool = arrayOfNulls[5] = arrayOfNulls[0]
var nullEqCheck5_1 : Bool = arrayOfNulls[5] = arrayOfNulls[1]
var nullEqCheck5_2 : Bool = arrayOfNulls[5] = arrayOfNulls[2]
var nullEqCheck5_3 : Bool = arrayOfNulls[5] = arrayOfNulls[3]
var nullEqCheck5_4 : Bool = arrayOfNulls[5] = arrayOfNulls[4]
var nullEqCheck5_5 : Bool = arrayOfNulls[5] = arrayOfNulls[5]
var nullEqCheck5_6 : Bool = arrayOfNulls[5] = arrayOfNulls[6]

var nullEqCheck6_0 : Bool = arrayOfNulls[6] = arrayOfNulls[0]
var nullEqCheck6_1 : Bool = arrayOfNulls[6] = arrayOfNulls[1]
var nullEqCheck6_2 : Bool = arrayOfNulls[6] = arrayOfNulls[2]
var nullEqCheck6_3 : Bool = arrayOfNulls[6] = arrayOfNulls[3]
var nullEqCheck6_4 : Bool = arrayOfNulls[6] = arrayOfNulls[4]
var nullEqCheck6_5 : Bool = arrayOfNulls[6] = arrayOfNulls[5]
var nullEqCheck6_6 : Bool = arrayOfNulls[6] = arrayOfNulls[6]
*/

-- var nullEqCheck_0__0 : Bool = null = null
-- var nullEqCheck_0__1 : Bool = null = nullObj
-- var nullEqCheck_0__2 : Bool = null = nullStr
-- var nullEqCheck_0__3 : Bool = null = nullTvm1
-- var nullEqCheck_0__4 : Bool = null = nullTvm2
-- var nullEqCheck_0__5 : Bool = null = nullTvm3
-- var nullEqCheck_0__6 : Bool = null = nullDist

-- var nullEqCheck_1__0 : Bool = nullObj = null
-- var nullEqCheck_1__1 : Bool = nullObj = nullObj
var nullEqCheck_1__2 : Bool = nullObj = nullStr
var nullEqCheck_1__3 : Bool = nullObj = nullTvm1
var nullEqCheck_1__4 : Bool = nullObj = nullTvm2
var nullEqCheck_1__5 : Bool = nullObj = nullTvm3
-- var nullEqCheck_1__6 : Bool = nullObj = nullDist

// var nullEqCheck_0__0 : Bool = null     = null
// var nullEqCheck_1__0 : Bool = nullObj  = null
var nullEqCheck_2__0 : Bool = nullStr  = null
-- var nullEqCheck_3__0 : Bool = nullTvm1 = null
-- var nullEqCheck_4__0 : Bool = nullTvm2 = null
-- var nullEqCheck_5__0 : Bool = nullTvm3 = null
-- var nullEqCheck_6__0 : Bool = nullDist = null

// var nullEqCheck_0__1 : Bool = null     = nullObj
// var nullEqCheck_1__1 : Bool = nullObj  = nullObj
var nullEqCheck_2__1 : Bool = nullStr  = nullObj
var nullEqCheck_3__1 : Bool = nullTvm1 = nullObj
var nullEqCheck_4__1 : Bool = nullTvm2 = nullObj
var nullEqCheck_5__1 : Bool = nullTvm3 = nullObj
-- var nullEqCheck_6__1 : Bool = nullDist = nullObj

/*

var nullNotEqCheck0_0_0 : Bool = arrayOfNulls[0] = arrayOfNonNulls[0]
var nullNotEqCheck0_1_0 : Bool = arrayOfNulls[0] = arrayOfNonNulls[1]
var nullNotEqCheck0_2_0 : Bool = arrayOfNulls[0] = arrayOfNonNulls[2]
var nullNotEqCheck0_3_0 : Bool = arrayOfNulls[0] = arrayOfNonNulls[3]
var nullNotEqCheck0_4_0 : Bool = arrayOfNulls[0] = arrayOfNonNulls[4]
var nullNotEqCheck0_5_0 : Bool = arrayOfNulls[0] = arrayOfNonNulls[5]
var nullNotEqCheck0_6_0 : Bool = arrayOfNulls[0] = arrayOfNonNulls[6]

var nullNotEqCheck1_0_0 : Bool = arrayOfNulls[1] = arrayOfNonNulls[0]
var nullNotEqCheck1_1_0 : Bool = arrayOfNulls[1] = arrayOfNonNulls[1]
var nullNotEqCheck1_2_0 : Bool = arrayOfNulls[1] = arrayOfNonNulls[2]
var nullNotEqCheck1_3_0 : Bool = arrayOfNulls[1] = arrayOfNonNulls[3]
var nullNotEqCheck1_4_0 : Bool = arrayOfNulls[1] = arrayOfNonNulls[4]
var nullNotEqCheck1_5_0 : Bool = arrayOfNulls[1] = arrayOfNonNulls[5]
var nullNotEqCheck1_6_0 : Bool = arrayOfNulls[1] = arrayOfNonNulls[6]

var nullNotEqCheck2_0_0 : Bool = arrayOfNulls[2] = arrayOfNonNulls[0]
var nullNotEqCheck2_1_0 : Bool = arrayOfNulls[2] = arrayOfNonNulls[1]
var nullNotEqCheck2_2_0 : Bool = arrayOfNulls[2] = arrayOfNonNulls[2]
var nullNotEqCheck2_3_0 : Bool = arrayOfNulls[2] = arrayOfNonNulls[3]
var nullNotEqCheck2_4_0 : Bool = arrayOfNulls[2] = arrayOfNonNulls[4]
var nullNotEqCheck2_5_0 : Bool = arrayOfNulls[2] = arrayOfNonNulls[5]
var nullNotEqCheck2_6_0 : Bool = arrayOfNulls[2] = arrayOfNonNulls[6]

var nullNotEqCheck3_0_0 : Bool = arrayOfNulls[3] = arrayOfNonNulls[0]
var nullNotEqCheck3_1_0 : Bool = arrayOfNulls[3] = arrayOfNonNulls[1]
var nullNotEqCheck3_2_0 : Bool = arrayOfNulls[3] = arrayOfNonNulls[2]
var nullNotEqCheck3_3_0 : Bool = arrayOfNulls[3] = arrayOfNonNulls[3]
var nullNotEqCheck3_4_0 : Bool = arrayOfNulls[3] = arrayOfNonNulls[4]
var nullNotEqCheck3_5_0 : Bool = arrayOfNulls[3] = arrayOfNonNulls[5]
var nullNotEqCheck3_6_0 : Bool = arrayOfNulls[3] = arrayOfNonNulls[6]

var nullNotEqCheck4_0_0 : Bool = arrayOfNulls[4] = arrayOfNonNulls[0]
var nullNotEqCheck4_1_0 : Bool = arrayOfNulls[4] = arrayOfNonNulls[1]
var nullNotEqCheck4_2_0 : Bool = arrayOfNulls[4] = arrayOfNonNulls[2]
var nullNotEqCheck4_3_0 : Bool = arrayOfNulls[4] = arrayOfNonNulls[3]
var nullNotEqCheck4_4_0 : Bool = arrayOfNulls[4] = arrayOfNonNulls[4]
var nullNotEqCheck4_5_0 : Bool = arrayOfNulls[4] = arrayOfNonNulls[5]
var nullNotEqCheck4_6_0 : Bool = arrayOfNulls[4] = arrayOfNonNulls[6]

var nullNotEqCheck5_0_0 : Bool = arrayOfNulls[5] = arrayOfNonNulls[0]
var nullNotEqCheck5_1_0 : Bool = arrayOfNulls[5] = arrayOfNonNulls[1]
var nullNotEqCheck5_2_0 : Bool = arrayOfNulls[5] = arrayOfNonNulls[2]
var nullNotEqCheck5_3_0 : Bool = arrayOfNulls[5] = arrayOfNonNulls[3]
var nullNotEqCheck5_4_0 : Bool = arrayOfNulls[5] = arrayOfNonNulls[4]
var nullNotEqCheck5_5_0 : Bool = arrayOfNulls[5] = arrayOfNonNulls[5]
var nullNotEqCheck5_6_0 : Bool = arrayOfNulls[5] = arrayOfNonNulls[6]

var nullNotEqCheck6_0_0 : Bool = arrayOfNulls[6] = arrayOfNonNulls[0]
var nullNotEqCheck6_1_0 : Bool = arrayOfNulls[6] = arrayOfNonNulls[1]
var nullNotEqCheck6_2_0 : Bool = arrayOfNulls[6] = arrayOfNonNulls[2]
var nullNotEqCheck6_3_0 : Bool = arrayOfNulls[6] = arrayOfNonNulls[3]
var nullNotEqCheck6_4_0 : Bool = arrayOfNulls[6] = arrayOfNonNulls[4]
var nullNotEqCheck6_5_0 : Bool = arrayOfNulls[6] = arrayOfNonNulls[5]
var nullNotEqCheck6_6_0 : Bool = arrayOfNulls[6] = arrayOfNonNulls[6]

*/

-- var nullNotEqCheck_0__0_0 : Bool = null = ""
-- var nullNotEqCheck_0__1_0 : Bool = null = "notNullStrValue"
-- var nullNotEqCheck_0__2_0 : Bool = null = nonNullStr
-- var nullNotEqCheck_0__3_0 : Bool = null = nonNullTvm1
var nullNotEqCheck_0__4_0 : Bool = null = nonNullTvm2
-- var nullNotEqCheck_0__5_0 : Bool = null = nonNullTvm3
-- var nullNotEqCheck_0__6_0 : Bool = null = nonNullDist
--
-- var nullNotEqCheck_1__0_0 : Bool = nullObj = ""
-- var nullNotEqCheck_1__1_0 : Bool = nullObj = "notNullStrValue"
-- var nullNotEqCheck_1__2_0 : Bool = nullObj = nonNullStr
-- var nullNotEqCheck_1__3_0 : Bool = nullObj = nonNullTvm1
-- var nullNotEqCheck_1__4_0 : Bool = nullObj = nonNullTvm2
-- var nullNotEqCheck_1__5_0 : Bool = nullObj = nonNullTvm3
-- var nullNotEqCheck_1__6_0 : Bool = nullObj = nonNullDist
--
-- // var nullNotEqCheck_0__0_0 : Bool = null     = null
-- // var nullNotEqCheck_1__0_0 : Bool = nullObj  = null
-- var nullNotEqCheck_2__0_0 : Bool = nullStr  = ""
-- var nullNotEqCheck_3__0_0 : Bool = nullTvm1 = ""
-- var nullNotEqCheck_4__0_0 : Bool = nullTvm2 = ""
-- var nullNotEqCheck_5__0_0 : Bool = nullTvm3 = ""
-- var nullNotEqCheck_6__0_0 : Bool = nullDist = ""
--
-- // var nullNotEqCheck_0__1_0 : Bool = null     = nonNullStr
-- // var nullNotEqCheck_1__1_0 : Bool = nullObj  = nonNullStr
-- var nullNotEqCheck_2__1_0 : Bool = nullStr  = nonNullStr
-- var nullNotEqCheck_3__1_0 : Bool = nullTvm1 = nonNullStr
-- var nullNotEqCheck_4__1_0 : Bool = nullTvm2 = nonNullStr
-- var nullNotEqCheck_5__1_0 : Bool = nullTvm3 = nonNullStr
-- var nullNotEqCheck_6__1_0 : Bool = nullDist = nonNullStr



/*

var nullNotEqCheck0_0_1 : Bool = arrayOfNonNulls[0] = arrayOfNulls[0]
var nullNotEqCheck0_1_1 : Bool = arrayOfNonNulls[0] = arrayOfNulls[1]
var nullNotEqCheck0_2_1 : Bool = arrayOfNonNulls[0] = arrayOfNulls[2]
var nullNotEqCheck0_3_1 : Bool = arrayOfNonNulls[0] = arrayOfNulls[3]
var nullNotEqCheck0_4_1 : Bool = arrayOfNonNulls[0] = arrayOfNulls[4]
var nullNotEqCheck0_5_1 : Bool = arrayOfNonNulls[0] = arrayOfNulls[5]
var nullNotEqCheck0_6_1 : Bool = arrayOfNonNulls[0] = arrayOfNulls[6]

var nullNotEqCheck1_0_1 : Bool = arrayOfNonNulls[1] = arrayOfNulls[0]
var nullNotEqCheck1_1_1 : Bool = arrayOfNonNulls[1] = arrayOfNulls[1]
var nullNotEqCheck1_2_1 : Bool = arrayOfNonNulls[1] = arrayOfNulls[2]
var nullNotEqCheck1_3_1 : Bool = arrayOfNonNulls[1] = arrayOfNulls[3]
var nullNotEqCheck1_4_1 : Bool = arrayOfNonNulls[1] = arrayOfNulls[4]
var nullNotEqCheck1_5_1 : Bool = arrayOfNonNulls[1] = arrayOfNulls[5]
var nullNotEqCheck1_6_1 : Bool = arrayOfNonNulls[1] = arrayOfNulls[6]

var nullNotEqCheck2_0_1 : Bool = arrayOfNonNulls[2] = arrayOfNulls[0]
var nullNotEqCheck2_1_1 : Bool = arrayOfNonNulls[2] = arrayOfNulls[1]
var nullNotEqCheck2_2_1 : Bool = arrayOfNonNulls[2] = arrayOfNulls[2]
var nullNotEqCheck2_3_1 : Bool = arrayOfNonNulls[2] = arrayOfNulls[3]
var nullNotEqCheck2_4_1 : Bool = arrayOfNonNulls[2] = arrayOfNulls[4]
var nullNotEqCheck2_5_1 : Bool = arrayOfNonNulls[2] = arrayOfNulls[5]
var nullNotEqCheck2_6_1 : Bool = arrayOfNonNulls[2] = arrayOfNulls[6]

var nullNotEqCheck3_0_1 : Bool = arrayOfNonNulls[3] = arrayOfNulls[0]
var nullNotEqCheck3_1_1 : Bool = arrayOfNonNulls[3] = arrayOfNulls[1]
var nullNotEqCheck3_2_1 : Bool = arrayOfNonNulls[3] = arrayOfNulls[2]
var nullNotEqCheck3_3_1 : Bool = arrayOfNonNulls[3] = arrayOfNulls[3]
var nullNotEqCheck3_4_1 : Bool = arrayOfNonNulls[3] = arrayOfNulls[4]
var nullNotEqCheck3_5_1 : Bool = arrayOfNonNulls[3] = arrayOfNulls[5]
var nullNotEqCheck3_6_1 : Bool = arrayOfNonNulls[3] = arrayOfNulls[6]

var nullNotEqCheck4_0_1 : Bool = arrayOfNonNulls[4] = arrayOfNulls[0]
var nullNotEqCheck4_1_1 : Bool = arrayOfNonNulls[4] = arrayOfNulls[1]
var nullNotEqCheck4_2_1 : Bool = arrayOfNonNulls[4] = arrayOfNulls[2]
var nullNotEqCheck4_3_1 : Bool = arrayOfNonNulls[4] = arrayOfNulls[3]
var nullNotEqCheck4_4_1 : Bool = arrayOfNonNulls[4] = arrayOfNulls[4]
var nullNotEqCheck4_5_1 : Bool = arrayOfNonNulls[4] = arrayOfNulls[5]
var nullNotEqCheck4_6_1 : Bool = arrayOfNonNulls[4] = arrayOfNulls[6]

var nullNotEqCheck5_0_1 : Bool = arrayOfNonNulls[5] = arrayOfNulls[0]
var nullNotEqCheck5_1_1 : Bool = arrayOfNonNulls[5] = arrayOfNulls[1]
var nullNotEqCheck5_2_1 : Bool = arrayOfNonNulls[5] = arrayOfNulls[2]
var nullNotEqCheck5_3_1 : Bool = arrayOfNonNulls[5] = arrayOfNulls[3]
var nullNotEqCheck5_4_1 : Bool = arrayOfNonNulls[5] = arrayOfNulls[4]
var nullNotEqCheck5_5_1 : Bool = arrayOfNonNulls[5] = arrayOfNulls[5]
var nullNotEqCheck5_6_1 : Bool = arrayOfNonNulls[5] = arrayOfNulls[6]

var nullNotEqCheck6_0_1 : Bool = arrayOfNonNulls[6] = arrayOfNulls[0]
var nullNotEqCheck6_1_1 : Bool = arrayOfNonNulls[6] = arrayOfNulls[1]
var nullNotEqCheck6_2_1 : Bool = arrayOfNonNulls[6] = arrayOfNulls[2]
var nullNotEqCheck6_3_1 : Bool = arrayOfNonNulls[6] = arrayOfNulls[3]
var nullNotEqCheck6_4_1 : Bool = arrayOfNonNulls[6] = arrayOfNulls[4]
var nullNotEqCheck6_5_1 : Bool = arrayOfNonNulls[6] = arrayOfNulls[5]
var nullNotEqCheck6_6_1 : Bool = arrayOfNonNulls[6] = arrayOfNulls[6]
*/

-- var nullNotEqCheck_0__0_1 : Bool = ""                = null
-- var nullNotEqCheck_0__1_1 : Bool = "notNullStrValue" = null
-- var nullNotEqCheck_0__2_1 : Bool = nonNullStr        = null
-- var nullNotEqCheck_0__3_1 : Bool = nonNullTvm1       = null
var nullNotEqCheck_0__4_1 : Bool = nonNullTvm2       = null
-- var nullNotEqCheck_0__5_1 : Bool = nonNullTvm3       = null
-- var nullNotEqCheck_0__6_1 : Bool = nonNullDist       = null
--
-- var nullNotEqCheck_1__0_1 : Bool = ""                = nullObj
-- var nullNotEqCheck_1__1_1 : Bool = "notNullStrValue" = nullObj
-- var nullNotEqCheck_1__2_1 : Bool = nonNullStr        = nullObj
-- var nullNotEqCheck_1__3_1 : Bool = nonNullTvm1       = nullObj
-- var nullNotEqCheck_1__4_1 : Bool = nonNullTvm2       = nullObj
-- var nullNotEqCheck_1__5_1 : Bool = nonNullTvm3       = nullObj
-- var nullNotEqCheck_1__6_1 : Bool = nonNullDist       = nullObj
--
-- // var nullNotEqCheck_0__0_1 : Bool = null = null
-- // var nullNotEqCheck_1__0_1 : Bool = null = nullObj
-- var nullNotEqCheck_2__0_1 : Bool = "" = nullStr
-- var nullNotEqCheck_3__0_1 : Bool = "" = nullTvm1
-- var nullNotEqCheck_4__0_1 : Bool = "" = nullTvm2
-- var nullNotEqCheck_5__0_1 : Bool = "" = nullTvm3
-- var nullNotEqCheck_6__0_1 : Bool = "" = nullDist
--
-- // var nullNotEqCheck_0__1_1 : Bool = nonNullStr = null
-- // var nullNotEqCheck_1__1_1 : Bool = nonNullStr = nullObj
-- var nullNotEqCheck_2__1_1 : Bool = nonNullStr = nullStr
-- var nullNotEqCheck_3__1_1 : Bool = nonNullStr = nullTvm1
-- var nullNotEqCheck_4__1_1 : Bool = nonNullStr = nullTvm2
-- var nullNotEqCheck_5__1_1 : Bool = nonNullStr = nullTvm3
-- var nullNotEqCheck_6__1_1 : Bool = nonNullStr = nullDist
