class Evt {
  t1 : Int
  t2 : Int 

  req t1 >= 0
  req t2 > t1 
  req t2 < 10

  fun before (e : Evt) : Bool {
    t2 < e.t1
  }

  fun meets (e : Evt) : Bool {
    t2 = e.t1
  }  

  fun contains (e : Evt) : Bool {
    t1 <= e.t1 && e.t2 <= t2
  }
}

class Schedule {
  a : Evt
  b : Evt
  c : Evt

req a.meets(b)


}

s:Schedule