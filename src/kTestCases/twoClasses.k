class A {

  prop1: C

  req prop1.prop2=this

}

class B {

  prop2: A

  req prop2.prop1=this

}

a:A
