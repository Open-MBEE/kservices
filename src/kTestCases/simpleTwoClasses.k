class A {
	fun sq(x:Int):Int {
		x*x
	}
	value:Int
	b:B
	req value = sq(b.y)
}

class B {
	y:Int
	req y > 8
	req y < 10
}

a:A