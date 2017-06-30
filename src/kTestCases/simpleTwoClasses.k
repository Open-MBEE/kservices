class A {
	fun sq(x:Int):Int {
		x*x
	}
	value:Int
	b:B
	req x = sq(b.y)
}

class B {
	y:Int = 10
}

a:A