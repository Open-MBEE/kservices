
class A {
    var a : Bool
}

class AA extends A {
    var aa : Bool
}

//class AAA extends A {
//    var aaa : Bool
//}

fun f(a: A) : Bool {
    if a is AA then (a as AA).aa
//    else if a is AAA then (a as AAA).aaa
    else a.a
}

var x : A = A(a::true)
var xx : AA = AA(aa::true)
//var xxx : AAA(aaa::true)

req f(x) = f(xx)


