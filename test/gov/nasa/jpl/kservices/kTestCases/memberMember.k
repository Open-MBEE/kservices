class A {
  var x : Int = 3
}

class B {
  var a : A
}

var b : B


class C {
  var z : Int = b.a.x
}

var c : C
