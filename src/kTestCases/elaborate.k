class A1 extends DurativeEvent {
     req duration = 3
}

class A2 extends DurativeEvent {
     req startTime = 0
     req duration = 5

     var child : A1 = A1(startTime::startTime+1)
}

var a2: A2
