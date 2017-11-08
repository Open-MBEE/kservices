import gov.nasa.jpl.ae.event.TimeVaryingMap

class A {
    var gkhx : Real
    req myName : gkhx > 0.0
    req gkhx < java.lang.Math.sqrt(100.0)
    var t : TimeVaryingMap[String] = TimeVaryingMap[String]("str", null, "Fred", String.class)
}
var a : A
