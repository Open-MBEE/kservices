var x : Real
var y : Real

req x + y >= 4.0
req 2.0*x + 4.0*y >= 7.0
req 2.0*x + 4.0*y <= 14.0

req x >= 1.0
req x <= 6.0
req y >= 2.0
req y <= 5.0

var z : Real = maximize("x")
