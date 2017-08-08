class Shape {
  sides : Int
}

class Angle {
  value : Int

  req value >= 0
  req value <= 360
}

class TAngle extends Angle {
  req value < 180
  
  
  fun eq(other : TAngle) : Bool {
    other.value = value
  }
  
}

class Triangle extends Shape {

  a : TAngle
  b : TAngle
  c : TAngle

  
  req sides = 3
}


class Acute extends Triangle {
    req a.value < 90
    req b.value < 90
    req c.value < 90
    
}

acute : Acute
   