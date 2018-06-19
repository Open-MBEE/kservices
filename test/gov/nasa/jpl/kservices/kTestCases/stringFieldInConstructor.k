class X {
    var s : String = "Hello World!"
}

class Y extends X {
    var s2 : String
}

var x:X

class C {
    var y: Y = Y(s2 :: x.s + " again")

    var z: TimeVaryingMap[Real] = TimeVaryingMap(x.s + " again", null, 1.0, Double.class)
}

var c : C
