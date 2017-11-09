class A1 extends DurativeEvent {
    var name: String
    var resource: TimeVaryingMap[Int]
    req duration = 3

    req resource.add(1, startTime - 5)
}

class A2 extends DurativeEvent {
    req startTime = 0
    req duration = 5

    var stateVariable: TimeVaryingMap[Int]

    var anElaboratedEvent : A1 = A1(startTime :: startTime+1, name :: "an A1", resource :: stateVariable)
    var anotherElaboratedEvent : A1 = A1(startTime :: startTime-5, name :: "another A1", resource :: stateVariable)

    req stateVariable[0] = 0
}

var a2: A2
