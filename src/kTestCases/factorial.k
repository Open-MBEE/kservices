class A {
	x:Int = factorial(10)

	fun factorial(y:Int):Int {
		if y = 1 then 
			1 
		else 
			y*factorial(y-1) 
	}
	
}

a:A