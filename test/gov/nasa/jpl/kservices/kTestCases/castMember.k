ParameterListenerImpl.setArcConsistencyQuiet(false) // DEBUG
Parameter.setPrintOnSetValue(true) // DEBUG
Parameter.setPrintOnRestrictDomain(true) // DEBUG


class X {
}
class Y extends X {
    var s: String
}

x: X
y: Y
req y.s = "hello"
req x = y

var r: String = (x as Y).s.substring(1)


