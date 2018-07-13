ParameterListenerImpl.setArcConsistencyQuiet(false)
ParameterListenerImpl.setMaxLoopsWithNoProgress(10000)

class A extends DurativeEvent

var activity1 : A = A(startTime :: start1, endTime :: end1)
var start1 : Time
var end1 : Time

req start1 > 0
req start1 < 10
req end1 < start1 + seconds(5.0)
req end1 < start2

var activity2 : A = A(startTime :: start2, endTime :: end2)
var start2 : Time
var end2 : Time

req start2 > 0
req start2 < 10
req end2 < start2 + seconds(5.0)
req end2 < start3

var activity3 : A = A(startTime :: start3, endTime :: end3)
var start3 : Time
var end3 : Time

req start3 > 0
req start3 < 10
req end3 < start3 + seconds(5.0)
req end3 < start4

var activity4 : A = A(startTime :: start4, endTime :: end4)
var start4 : Time
var end4 : Time

req start4 > 0
req start4 < 10
req end4 < start4 + seconds(5.0)
req end4 < start5

var activity5 : A = A(startTime :: start5, endTime :: end5)
var start5 : Time
var end5 : Time

req start5 > 0
req start5 < 10
req end5 < start5 + seconds(5.0)
