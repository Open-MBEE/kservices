
var t1 : Time
var t2 : Time
var t3 : Time
var t4 : Time
var t5 : Time
var t6 : Time
var t7 : Time

req t1 >= 0
req t2 >= 0
req t3 >= 0
req t4 >= 0
req t5 >= 0
req t6 >= 0
req t7 >= 0

req t1 <= 100
req t2 <= 100
req t3 <= 100
req t4 <= 100
req t5 <= 100
req t6 <= 100
req t7 <= 100

req t1 + 5 <= t2
req t1 + 20 >= t2
req t2 + 5 <= t3
req t2 + 20 >= t3
req t3 + 5 <= t4
req t3 + 20 >= t4
req t4 + 5 <= t5
req t4 + 20 >= t5
req t5 + 5 <= t6
req t5 + 20 >= t6
req t6 + 5 <= t7
req t6 + 20 >= t7
