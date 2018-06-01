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

  
  req Angles: a.value + b.value + c.value = 180
}

class  Equilateral extends Triangle {
  req a.eq(b)
  req b.eq(c) 
}

t:Triangle
e:Equilateral