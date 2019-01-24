// Java doesn't allow this.
// It is a compile-time error if a class has the same simple name as any of its enclosing classes or interfaces.
class A {
    class A {
        class A

        var a:A
    }

    var a:A
}

var a:A