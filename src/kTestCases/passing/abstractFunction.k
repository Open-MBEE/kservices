class X {
    var i: Int = 3
--    var p: String
--    req p < "z"
    fun f(s: String): String
}

class Y extends X {
    fun f(s: String): String {
        "hello " + s
    }
}
var x: X
var y: Y
-- req y.p = "hello"
var z : String = y.f("world")

-- req x.p = y.p
