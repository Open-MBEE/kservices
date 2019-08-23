import java.util.TreeMap

ParameterListenerImpl.setArcConsistencyQuiet(false) // DEBUG
Parameter.setPrintOnSetValue(true) // DEBUG
Parameter.setPrintOnRestrictDomain(true) // DEBUG

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
    // val FIRSTCHAR : Seq[Char] = Seq{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'}
    // val OTHERCHAR : Seq[Char] = Seq{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'}

    var name : String
    // req name.length() <= 30
    // req name.charAt(0) isin FIRSTCHAR
    // req forall i : Int :-
    //     1 <= i && i < name.length() => name.charAt(i) isin OTHERCHAR

    req text = name
}

class ListExp extends S_Exp {
    // LPM: [A list] is composed of these elements in the following order:
    // a left parenthesis, an S-expression, a dot, an Sexpression, and a right parenthesis.

    var leftExp  : S_Exp
    var rightExp : S_Exp

    var simpleText : String = "(" + leftExp.text + "." + rightExp.text + ")"
    var fancyText : String = "(" + leftExp.text + (
        if rightExp.text = "NIL" then ")" else
            " " + (
                (if rightExp is ListExp then rightExp.fancyText.substring(1) else rightExp.text) + ")"
            )
    )
    req (text = simpleText) || (text = fancyText)
}

// LPM: An S-expression is either an atomic symbol or [a list expression]
// req forall s : S_Exp :-
//     s is Atom || s is ListExp

===========================================
TODO: Define a Lisp interpreter by defining
cons, car, cdr, etc.
===========================================

// symbol table
symbols: TreeMap[S_Exp, ListExp]

fun evalquote( sExp: S_Exp ) : S_Exp {
    // start out evaluating the expression with an empty symbol table
    eval(sExp, symbols)
}

============================================================
LPM p. 13:
eval[e;a] = [atom[e] -> cdr[associate[e;a]];
             atom[car[e]] -> [
                eq[car[e]; QUOTE] -> cadr[el];
                eq[car[e]; COND] -> evcon[cdr[el]; a];
                T -> apply[car[el]; evlis[cdr[el]; a]; a]];
             T -> apply[car[el]; evlis[cdr[el]; a]; a]] 
============================================================
fun eval( sExp: S_Exp, symbols: TreeMap[S_Exp, ListExp] ) : S_Exp {
    //return (
        if sExp is Atom then symbols.get(sExp).rightExp else
        if (sExp as ListExp).leftExp is Atom then (
            if (sExp as ListExp).leftExp.name = "QUOTE" then ((sExp as ListExp).rightExp as ListExp).leftExp else
            if (sExp as ListExp).leftExp.name = "COND" then evcon((sExp as ListExp).rightExp, symbols) else
                apply((sExp as ListExp).leftExp, evlis((sExp as ListExp).rightExp, symbols))
        ) else
            apply((sExp as ListExp).leftExp, evlis((sExp as ListExp).rightExp, symbols))
    //)
}

============================================================
LPM p. 13
apply [fn;x; a] =
    [atom[fn] ->
        [eq[fn; CAR] -> caar[x];
         eq[fn; CDR] -> cdar[x];
         eq[fn; CONS] -> cons[car[x]; cadr[x]];
         eq[fn; ATOM] -> atom[car[x]];
         eq[fn; EQ] - eq[car[x]; cadr[x]];
         T -> apply[eval[fn;a];x;a]];
     eq[car[fn]; LAMBDA] -> eval[caddr[fn]; pairlis[cadr[fn];x;a]];
     eq[car[fn]; LABEL] -> apply[caddr[fn]; x; cons[cons[cadr[fn]; caddr[fn]]; a]]]
============================================================
fun apply( fn: S_Exp, sExp: ListExp, symbols: TreeMap[S_Exp, ListExp] ) : S_Exp {
        if fn is Atom then (
            if (fn as Atom).name = "CAR" then (sExp.leftExp as ListExp).leftExp else
            if (fn as Atom).name = "CDR" then (sExp.leftExp as ListExp).rightExp else
            if (fn as Atom).name = "CONS" then cons(sExp.leftExp, (sExp.rightExp as ListExp).leftExp) else
            if (fn as Atom).name = "ATOM" then sExp.leftExp is Atom else
            if (fn as Atom).name = "EQ" then eq(sExp.leftExp, (sExp.rightExp as ListExp).leftExp) else
                apply(eval(fn, symbols), sExp, symbols)
        ) else
        if (fn as ListExp).leftExp is Atom && (fn as ListExp).leftExp.name = "LAMBDA" then
            eval((((fn as ListExp).rightExp as ListExp).rightExp as ListExp).leftExp, pairlis(((fn as ListExp).rightExp as ListExp).leftExp, sExp, symbols)) else
        if (fn as ListExp).leftExp is Atom && (fn as ListExp).leftExp.name = "LABEL" then
            apply((((fn as ListExp).rightExp as ListExp).rightExp as ListExp).leftExp, sExp, cons(cons(((fn as ListExp).rightExp as ListExp).leftExp, (((fn as ListExp).rightExp as ListExp).rightExp as ListExp).leftExp), symbols)) else
        Atom(name::"NIL") // error condition
}

============================================================
LPM p. 13
evcon[c; a] =
    [eval[caar[c]; a] -> eval[cadar[c]; a];
     T -> evcon[cdr[c]; a]] 
============================================================
fun evcon(c: ListExp, a: ListExp) : S_Exp {
    if eval((c.leftExp as ListExp).leftExp, a)
    then eval(((c.leftExp as ListExp).rightExp as ListExp).leftExp, a)
    else evcon(c.rightExp, a)
}

============================================================
LPM p. 13
evlis[m;a] =
    [null[m] -> NIL;
     T -> cons[eval[car[m]; a]; evlis[cdr[m]; a]]] 
============================================================
fun evlis(m: S_Exp, a: ListExp) : S_Exp {
    if m is Atom && m.name = "NIL" then "NIL" else
       cons(eval((m as ListExp).leftExp, a), evlis((m as ListExp).rightExp, a))
}

============================================================
LPM p. 23
The predicate - eq has the following behavior.
1. If its arguments are different, the value of 3 is NIL.
2. If its arguments are both the same atomic symbol, its value is *T*.
3. If its arguments are both the same, but are not atomic, then the value is *T* or
NIL depending upon whether the arguments are identical in their representation in core
memory.
4. The value of - eq is always *T* or NIL. It is never undefined even if its arguments
are bad. 
============================================================
fun eq(x: S_Exp, y: S_Exp) : Bool {
    if (x is Atom && y is Atom && x.name = y.name) then Atom(name::"*T*") else x = y
}

fun cons(l: S_Exp, r: S_Exp) : S_Exp {
    ListExp(leftExp::l, rightExp::r)
}

============================================================
LPM p. 12
pairlis [x; y; a] = 
    [null[x] -> a;
     T -> cons[cons[car[x]; car[y]]; pairlis[cdr[x]; cdr [y]; a]]]
============================================================
fun pairlis(x: S_Exp, y: ListExp, a: S_Exp) : S_Exp{
        if x is Atom && x.name = "NIL" then a else
            cons(cons(x.leftExp, y.leftExp), pairlis(x.rightExp, y.rightExp, a))
}

============================================================
LPM p. 12
assoc[x; a] = 
    [equal[caar[a]; x] -> car[a];
     T -> assoc[x; cdr[a]]]
============================================================
fun associate(x: S_Exp, a: ListExp) : S_Exp {
    if (a.leftExp as ListExp).leftExp = x then a.leftExp else associate(x, a.rightExp)
}

=================================
Running the implicit Lisp parser.
=================================

// simpleText format, using the . separator to explicitly denote each cons cell

var myList : ListExp
// req myList.text = "(A.((B.C).D)"
req myList.text = "(A.B)"

// fancyText format, using the condensed list format and implicit NIL list terminator

var myFancyList : ListExp
req myFancyList.text = "(A B C D)"


==============
Executing Lisp
==============

var myProgram : ListExp
req myProgram.text =
    "(EQ (CAR (CDR (CDR (QUOTE (A B C D))))) (QUOTE C))"
var myProgramOutput : S_Exp = evalquote(myProgram)


============


var fibFunction : String = "(defun fibonacci (n) " + theRest + " )"
var theRest : String

fun fibProgram( n : Int ) : String {
    fibFunction + " (fibonacci " + n + ")"
}
fun fibList( n : Int ) : Seq[ Int ] {
    makeSeq(evalquote(fibProgram(n)))
}


req forall x : Int :- fibList (x).size() = x
req forall y : Int :- y > 2 => fibList (y) = fibList(y-2) .size() = x

req fibList(5) = Seq{ 1, 1, 2, 3, 5 }
req fibList(5) = Seq{ 1, 1, 2, 3, 5 }

============
