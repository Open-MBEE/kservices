ParameterListenerImpl.setArcConsistencyQuiet(false)

// var z : Bool = Parameter.setAllowPickValue(true)

// class A {
//   var x: String
//   // var prefix: String = "|||"
//   // var suffix: String = "___"
//   // var foo : String = prefix + x + suffix
//   var foo : String = "|||" + x
// }

// var bar : String = a.prefix + a.x + a.suffix

// var a : A = A(x :: "RRRR")
// var rrrr : String = "TTTT"
// var aa : A = A(x :: rrrr)
// var aaa : A
// req aaa.x = "SSSS"
// 
// 
// var pref: String = "@@@"
// class B {
//   var x: String
//   var suffix: String = "^^^"
//   var foofoo : String = pref + x + suffix
// }
// 
// var b : B = B( x :: "BBB" )
// var bb : B
// req bb.x = "CCC"

// var s : String = "foo "
var t : String // = "bar"
var u : String

var x : String // = "got"
//req u = "foo " + t + " monkey " + x + " hungry"
//req u = "foo bar monkey got hungry"
req u = t + " monkey " + x
req u = "bar monkey hungry"

//req u = "foo " + t + " monkey"
//req u = "foo bar monkey"
