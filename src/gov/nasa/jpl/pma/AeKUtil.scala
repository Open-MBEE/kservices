package gov.nasa.jpl.pma

import k.frontend._
import gov.nasa.jpl.pma.AeToK.AeOp
import gov.nasa.jpl.pma.AeToK.AeOp._

object AeKUtil {
  def aeToKUnaryOp( aeOp : AeOp ) : UnaryOp = {
    val op : UnaryOp = aeOp match {
            case Negative => NEG
            case Not => NOT
//            case Xor => null  //XOR not in K!
//            case Prev => PREV  // not in AE!
    }
    return op
  }
  def aeToKBinaryOp( aeOp : AeOp ) : BinaryOp = {
    val op : BinaryOp = aeOp match {
            case Add => ADD
            case Plus => ADD
            case Sum => ADD
            case And => AND
//            case Conditional =>
            case Divide => DIV
            case AeOp.EQ => k.frontend.EQ
            case Equals => k.frontend.EQ
            //case AeOp.Exists => k.frontend.Exists
            //case DoesThereExist =>
            //case ForAll =>
            //case ThereExists =>
            case AeOp.GT => k.frontend.GT
            case Greater => k.frontend.GT
            case AeOp.GTE => k.frontend.GTE
            case GreaterEquals => k.frontend.GTE
            case AeOp.LT => k.frontend.LT
            case Less => k.frontend.LT
            case AeOp.LTE => k.frontend.LTE
            case LessEquals => k.frontend.LTE
            case Minus => SUB
            case Sub => SUB
            case AeOp.NEQ => k.frontend.NEQ
            case NotEquals => k.frontend.NEQ
            case Or => OR
            case Times => MUL
            //case Xor => null  // Not in K!
        }
   return op
  }
  
  def makeFunApplExp( exp: Exp, arr : java.util.ArrayList[Argument] ) : FunApplExp = {
    var f = FunApplExp(exp, arrayListToList(arr))
    return f
  }
  def arrayListToList( arr : java.util.ArrayList[Argument] ) : List[Argument] = {
    var list : List[Argument] = List[Argument]()
    var iter : java.util.Iterator[Argument] = arr.iterator()
    while ( iter.hasNext() ) {
      iter.next() :: list
    }
    return list
  }
}