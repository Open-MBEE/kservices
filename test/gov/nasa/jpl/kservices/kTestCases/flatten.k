var x : Seq[String] = Seq{"h", "ell", "o"}
var y: Seq[Seq[String]] = Seq{x,Seq{" wor", "ld"}}
var z : Seq[String] = flatten(y, String.class)
var w : Seq[String] = flatten(y)
