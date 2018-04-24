req Timepoint.setEpoch("2020-001T00:00:00.000")
req Timepoint.setHorizonDuration(minutes(1.0))
var x : TimeVaryingMap[Real] = TimeVaryingMap("x", "../resources/realData.csv", 0.0, Double.class, TimeVaryingMap.LINEAR)
