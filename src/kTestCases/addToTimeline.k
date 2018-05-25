import gov.nasa.jpl.ae.event.TimeVaryingMap

var zero : Long = 0

var x : TimeVaryingMap[Int]
x.setValue(zero, 1)

var t1 : Time = startTime + seconds(2.0)

x.add(2, t1)


-- ====
-- Timepoint.setEpoch("2020-01-01T00:00:00.000")
-- -- For the "Global" class.
-- req startTime = 2020-01-01T00:00:00.000
-- var zero : Long = 0
-- var x : TimeVaryingMap[Int] -- = TimeVaryingMap("x", null, 1, Integer.class, TimeVaryingMap.LINEAR)
-- x.setValue(zero, 1)
--
-- var t1 : Time = startTime + seconds(2.0)
-- -- var t2 : Time = t1 + seconds(2.0)
-- x.add(2, t1)
-- -- x.add(3, t2)
--
-- -- class Adder extends DurativeEvent {
--   --   x.add(0.2, startTime)
--   --   req duration = seconds(1.0)
--   -- }
-- -- var a1: Adder = Adder(startTime::t1)
-- -- var a2: Adder = Adder(startTime::t2)
-- ====

/*
hello this is a c block comment
*/

var a : Long = 7 //this is a line comment

--
//
/* */
==== ====