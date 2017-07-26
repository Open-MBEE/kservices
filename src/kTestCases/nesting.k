package nesting

class LevelOne {
  var x1 : Int
  
  var nl1 : LevelTwo
  
  class LevelTwo {
    var x2 : Int
    
    var nl2 : LevelThree
    
    class LevelThree {
      var x3 : Int
      
      var y : Bool
    }
    
    class LevelThreeB {
      var back : LevelTwo
    }
  }
}