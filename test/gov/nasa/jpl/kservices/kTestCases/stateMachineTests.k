var x : TimeVaryingMap[Integer]

class StateMachine extends DurativeEvent {
    var currentMode : TimeVaryingMap[Mode]
    
    var transition_1_2 : TimeVaryingMap[boolean] = (currentMode = Mode1 && x > 10)
    req elab_1_2 : elaborates(null, Tr_1_2.class, transition_1_2, "currentMode", currentMode)
}

class Mode
val Mode1 : Mode
val Mode2 : Mode

class Tr_1_2 extends DurativeEvent {
    var currentMode : TimeVaryingMap[Mode]
    var nextMoment : Time = startTime + 1
    currentMode.setValue(nextMoment, Mode2)
}

var epoch : Time = 0
var fiveSeconds : Time = epoch + seconds(5)
var tenSeconds : Time = epoch + seconds(10)
var fifteenSeconds : Time = epoch + seconds(15)
var twentySeconds : Time = epoch + seconds(20)
var twentyFiveSeconds : Time = epoch + seconds(25)
var thirtySeconds : Time = epoch + seconds(30)

var SM1 : StateMachine
req SM1.startTime = epoch
req SM1.endTime = thirtySeconds
SM1.currentMode.setValue(epoch, Mode1)

x.setValue(epoch, 0)
x.setValue(fiveSeconds, 20)
x.setValue(tenSeconds, 0)
