var epoch : Time = 0

var x : LinearTimeline

x.setValue(epoch, 0.0)

var t : Time = 1000
x.setValue(t,1.0)

var t2 : Time = t / 2

var y : Real = x.getValue(t2)
