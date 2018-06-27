import gov.nasa.jpl.ae.event.TimeVaryingMap.TimeValue

//ParameterListenerImpl.setMaxLoopsWithNoProgress(300)

class A {
    var x : Long = 0
    var y : Real = 1.1
    var w : TimeVaryingMap[Real]
    var z : TimeValue = w.TimeValue(x, y)
}
a:A
