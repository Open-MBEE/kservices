class A {
	fun abs (x:Int):Int {
		if (x < 0) then
			-x
		else
			x
	}
		
	value : Int
	req value < 2
	req value = abs(value)
}

a:A