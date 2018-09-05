class State
val s1 : State
val s2 : State

var stateMachine : TimeVaryingMap[State]

var epoch : Time = 0.0
var change1 : Time = seconds(5.0)
var change2 : Time = seconds(10.0)

stateMachine.setValue(epoch, s1)
// stateMachine.setValue(change1, s2)
// stateMachine.setValue(change2, s1)

// var inS1 : TimeVaryingMap[Bool] = (stateMachine = s1)
// var inS2 : TimeVaryingMap[Bool] = (stateMachine = s2)
// var tr_s1_s2 : TimeVaryingMap[Bool] = (inS1.differentiate() = -1.0) && (inS2.differentiate() = 1.0)

var tr_s1_s2 : TimeVaryingMap[Bool] = (stateMachine.shift(1) = s1 && stateMachine = s2)

req (time = change1) => tr_s1_s2

// class StateChanger {
//     // var num : Int
//     var newState : State
//     var changeTime : Time
//     stateMachine.setValue(changeTime, newState)
//     // var nextSC : StateChanger = if (num > 1) StateChanger(num :: num - 1) else null
// }
// var numChanges : Int = 1
// var sc : StateChanger // (num :: numChanges)

var changeTime : Time
var changeState : State = s2
stateMachine.setValue(changeTime, changeState)

req changeTime > seconds(4.9)
req changeTime < seconds(5.1)
