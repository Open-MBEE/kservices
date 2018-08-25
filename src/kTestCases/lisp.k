============================================================================================
The following is an attempt to describe a LISP parser using the
LISP 1.5 programmer's manual, found here:
http://www.softwarepreservation.org/projects/LISP/book/LISP%201.5%20Programmers%20Manual.pdf
Comments prefixed with 'LPM:' are quotes from the Lisp Programmer's Manual
============================================================================================

=========================
Defining the Lisp parser:
=========================

class S_Exp {
    var text : String
}

class Atom extends S_Exp {
    // LPM: An atomic symbol is 
    // a string of no more than thirty numerals and capital letters;
    // the first character must be a letter.
    val FIRSTCHAR : Seq[Char] = Seq{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'}
    val OTHERCHAR : Seq[Char] = Seq{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'}

    var name : String
    // req name.length() <= 30
    req name.charAt(0) isin FIRSTCHAR
    // req forall i : Int :-
    //     1 <= i && i < name.length() => name.charAt(i) isin OTHERCHAR

    req text = name
}

class ListExp extends S_Exp {
    // LPM: [A list] is composed of these elements in the following order:
    // a left parenthesis, an S-expression, a dot, an Sexpression, and a right parenthesis.

    var leftExp  : S_Exp
    var rightExp : S_Exp

    req text = "(" + leftExp.text + "." + rightExp.text + ")"
}

// LPM: An S-expression is either an atomic symbol or [a list expression]
// forall s : S_Exp :-
//     s is Atom || s is ListExp

===========================================
TODO: Define a Lisp interpreter by defining
cons, car, cdr, etc.
===========================================


================================
Rnning the implicit Lisp parser.
================================

var myList : S_Exp
myList.text = "(A.((B.C).D)"