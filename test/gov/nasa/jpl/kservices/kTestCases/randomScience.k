req Timepoint.setEpoch("2024-365T00:00:00.000")
req Timepoint.setHorizon("2028-220T00:00:00.000")
-- req ParameterListenerImpl.setUsingArcConsistency(false)

var epoch : Time = 0

var scienceMode : TimeVaryingMap[Bool]
scienceMode.setValue(epoch, false)

var numCompleted : TimeVaryingMap[Int];
numCompleted.setValue(epoch, 0)

var lastScience : Science
var periodBetweenScience : Duration = weeks(13.5)

class Science extends DurativeEvent {
    var lastNumSucceeded : Int
    req duration = hours(6.0)
    req nextStart = startTime + periodBetweenScience

    scienceMode.setValue(startTime, true)
    scienceMode.setValue(endTime, false)

    var succeeded : Bool = scienceMode.getValue(endTime - 1)

    var numSucceeded : Int = lastNumSuccess + if numSuccess then 1 else 0

    var pastHorizon : Bool = nextStart > Timepoint.getHorizonDuration()  // try getHorizon()

    var nextScience : if pastHorizon then null else Science(startTime::nextStart, lastNumSucceeded::numSucceeded)

    numCompleted.setValue(endTime, numSucceeded); // try add(endTime, 1)

    req if pastHorizon then lastScience = this else true
}

var firstScience : Science = Science(2028-001T00:00:00.000)

var meanTimeBetweenFaults : Duration = years(0.25)

class Fault extends DurativeEvent {
    req duration = 1;
    var lastTime : Time
    var timeToNextFault : Exponential = Exponential(meanTimeBetweenFaults)
    req startTime = lastTime + timeToNextFault
    scienceMode.setValue(startTime, false)

    var nextFault : Fault = Fault(lastTime::startTime)
}

var firstFault : Fault = Fault(lastTime::epoch)

var numSucceeded : Int = lastScience.numSuccess
var maxSucceeded : Int = getHorizonDuration() / periodBetweenScience

var p : Real = P(numSucceeded > 0.95 * maxSucceeded )
