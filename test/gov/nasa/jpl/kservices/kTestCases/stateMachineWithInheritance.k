class State
val s1 : State
val s2 : State

class ExtendState extends State
val es1 : ExtendState
val es2 : ExtendState

var stateMachine : TimeVaryingMap[ExtendState]

var epoch : Time = 0.0
var change1 : Time = seconds(5.0)
var change2 : Time = seconds(10.0)

stateMachine.setValue(epoch, es1)
stateMachine.setValue(change1, es2)
stateMachine.setValue(change2, es1)

var es1Faked : State = es1
var inS1Faked : TimeVaryingMap[Bool] = (stateMachine = es1Faked)
