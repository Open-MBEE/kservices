var y : Int
var x : Seq[Int] = Seq{1,2,y}
req x[2] = -1

var z : Seq[Seq[Int]] = Seq{x, Seq{7,6}}

var a : Int = x[1]
var b : Seq[Int] = z[1]

-- var c : Int = b[1]
-- Double array access does not work as of 2017-09-13.
-- var c : Int = z[1][1]

===
fun z1( i: Int ) : Seq[Int] {
    z[i]
}
===

fun z2( i2: Int ) : Int {
    b[i2]
}

var cc : Int = z2(1)

-- var o : Object =
