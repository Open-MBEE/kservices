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
    var fancyText : String
    req (text = simpleText) || (text = fancyText)
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
    req fancyText = text
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
}

fun car( sExp: S_Exp ) : S_Exp {
    if sExp is ListExp then (sExp as ListExp).leftExp else null
}
fun cdr( sExp: S_Exp ) : S_Exp {
    if sExp is ListExp then (sExp as ListExp).rightExp else null
}
fun caar( sExp: S_Exp ) : S_Exp { car( car( sExp ) ) }
fun cdar( sExp: S_Exp ) : S_Exp { cdr( car( sExp ) ) }
fun cadr( sExp: S_Exp ) : S_Exp { car( cdr( sExp ) ) }
fun caddr( sExp: S_Exp ) : S_Exp { cadr( cdr( sExp ) ) }
fun cadar( sExp: S_Exp ) : S_Exp { cadr( car( sExp ) ) }


// LPM: An S-expression is either an atomic symbol or [a list expression]
// req forall s : S_Exp :-
//     s is Atom || s is ListExp

===========================================
TODO: Define a Lisp interpreter by defining
cons, car, cdr, etc.
===========================================


fun evalquote( sExp: S_Exp ) : S_Exp {
    // start out evaluating the expression with an empty symbol table
    if sExp is Atom then evalAtom(sExp as Atom, ListExp())
    else if sExp is ListExp then evalList(sExp as ListExp, a)
    else null
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
fun evalAtom( sExp: Atom, a: ListExp ) : S_Exp {
    cdr(a.get(sExp))
}
fun evalList( sExp: ListExp, a: ListExp ) : S_Exp {
    if car(sExp) is Atom && sExp.rightExp is ListExp then (
        if (car(sExp) as Atom).name = "QUOTE" then cadr( sExp ) else
        if (car(sExp) as Atom).name = "COND" then evcon(cdr(sExp), a) else
            apply(car(sExp), evlis(cdr(sExp), a))
    ) else
        apply(car(sExp), evlis(cdr(sExp), a))
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
fun apply( fn: S_Exp, sExp: ListExp, a: ListExp ) : S_Exp {
        if fn is Atom then
            applyAtom(fn as Atom, sExp, a)
        else if fn is ListExp then
            applyList(fn as ListExp, sExp, a)
        else Atom(name::"NIL") // error condition
}
fun applyAtom( fn: Atom, sExp: ListExp, a: ListExp ) : S_Exp {
            if fn.name = "CAR" then caar(sExp)
            else if fn.name = "CDR" then cdar(sExp)
            else if fn.name = "CONS" then cons(car(sExp), cadr(sExp))
            else if fn.name = "ATOM" then car(sExp)
            else if fn.name = "EQ" then eq(car(sExp), cadr(sExp))
            else apply(evalAtom(fn, a), sExp, a)
}
fun applyList( fn: ListExp, sExp: ListExp, a: ListExp ) : S_Exp {
    if car(fn) = "LAMBDA" then eval(caddr(fn), pairlis(cadr(fn), a))
    else if car(fn) = "LABEL" then apply(caddr(fn), sExp, cons(cons(cadr(fn), caddr(fn))), a)
    else Atom(name::"NIL") // error condition
}


============================================================
LPM p. 13
evcon[c; a] =
    [eval[caar[c]; a] -> eval[cadar[c]; a];
     T -> evcon[cdr[c]; a]] 
============================================================
fun evcon(c: ListExp, a: ListExp) : S_Exp {
    if eval(caar(c), a) != null then eval(cadar(c), a)
    else evcon(cdr(c), a)
}

============================================================
LPM p. 13
evlis[m;a] =
    [null[m] -> NIL;
     T -> cons[eval[car[m]; a]; evlis[cdr[m]; a]]] 
============================================================
fun evlis(m: S_Exp, a: ListExp) : S_Exp {
    if m = null || (m is Atom && (m as Atom).name = "NIL") then Atom(name::"NIL")
    else if m is ListExp then cons(eval(car(m), a), evlis(cdr(m), a))
}

============================================================
LPM p. 23
The predicate - eq has the following behavior.
1. If its arguments are different, the value of 3 is NIL.
2. If its arguments are both the same atomic symbol, its value is *T*.
3. If its arguments are both the same, but are not atomic, then the value is *T* or
NIL depending upon whether the arguments are identical in their representation in core
memory.
4. The value of - eq is always T or NIL. It is never undefined even if its arguments
are bad. 
============================================================
fun eq(x: S_Exp, y: S_Exp) : Bool {
    if (x is Atom && y is Atom && (x as Atom).name = (y as Atom).name) then true else x = y
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
fun pairlis(x: S_Exp, y: ListExp, a: S_Exp) : S_Exp {
        if x is Atom && (x as Atom).name = "NIL" then a
        else if x is ListExp then
            cons(cons(car(x), car(y)), pairlis(cdr(x), cdr(y), a))
        else Atom(name::"NIL")  // error condition
}

============================================================
LPM p. 12
assoc[x; a] = 
    [equal[caar[a]; x] -> car[a];
     T -> assoc[x; cdr[a]]]
============================================================
fun associate(x: S_Exp, a: ListExp) : S_Exp {
    if caar(a) = x then car(a) else associate(x, cdr(a))
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
