import gov.nasa.jpl.ae.util.distributions.Exponential

setHorizonDuration(hours(15.0))

var epoch : Long = 0

var burnStart : Time = timeStart + hours(2.0)
var burnTarget : Time = hours(5.0)
var burnEnd : Time

var timeStart : Time = 0
var timeEnd : Time = hours(10.0)

var state : TimeVaryingMap[String]

state.setValue(epoch, "OFF")

state.setValue(burnStart, "ON")

var on : TimeVaryingMap[Long] = if state = "ON" then 1 else 0
var totalBurnTime : TimeVaryingMap = on.integrate()

req burnEnd = totalBurnTime.getTimeWhenValue(burnTarget)

var success : Bool = burnEnd < timeEnd  // totalBurnTime.getValue(timeEnd) >= burnTarget


state.setValue(burnEnd, "OFF")


var fail : Fail = Fail(startTime :: burnStart)

class Fail extends DurativeEvent {

   var lambdaF : Real = 1.0/hours(100.0)
   var lambdaR : Real = 1.0/hours(100.0)
   var failTime : Exponential = startTime + Exponential(lambdaF)
   var beforeFailTime : Time = failTime - 1
   var o1 : Object
   //if forall t : Time :- t >= startTime && t < failTime => state.getValue(t) = "ON" then
   req if state.getValue(startTime) = "ON" && state.getValue(beforeFailTime) = "ON" then
          o1 = state.setValue(failTime, "FAIL")
       else true

   var recoveryTime : Exponential =  failTime + Exponential(lambdaR)
   var beforeRecoveryTime : Time = recoveryTime - 1
   var o2 : Object
   //if forall t : Time :- t >= failTime && t < recoveryTime => state.getValue(t) = "FAIL" then
   req if state.getValue(startTime) = "ON" && state.getValue(beforeRecoveryTime) = "FAIL" then
         o2 = state.setValue(recoveryTime, "ON")
       else true

   var failAgain : Fail =
      if state.getValue(startTime) = "ON" then
        Fail(startTime::recoveryTime)
      else null
}
