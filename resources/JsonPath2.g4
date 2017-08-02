grammar JsonPath2;

/**
 * Defines the grammar for the variant of JsonPath used by the Path class.
 * 
 * Grammar based off of Steven Alexander's JsonPath grammar, found at
 * https://github.com/stevenalexander/antlr4-jsonpath-grammar/blob/master/JsonPath.g4
 */

/*
 * Parser Rules
 */

path     : element branches?
         ;


element  : tagelement
         | indexelement
         | attributefilterelement
         | doubleattributefilterelement
         | negationfilterelement
         | alternationelement
         ;


tagelement : ROOTTAG
           | REFTAG
           | LIBTAG
           | '.' tag
           ;

tag        : WILDCARD
           | IDENTIFIER
           ;


indexelement : '[' index ']' ;

index : WILDCARD
      | NATURALNUM
      ;


attributefilterelement : FILTERSTART path '=' attributevalue FILTEREND ;

attributevalue : STRING ;


doubleattributefilterelement : FILTERSTART path '=' path FILTEREND ;


negationfilterelement : FILTERSTART '!' path FILTEREND ;


alternationelement : '[' element (',' element)* ']' ;

branches : path
         | '[' path (',' path)* ']'
         ;

/*
 * Lexer Rules
 */

STRING      : '"' (~[\\"] | '\\' [\\"])* '"'; // STRING adapted from Bart Kiers, https://stackoverflow.com/questions/23799285/parsing-quoted-string-with-escape-chars
FILTERSTART : '?(' ;
FILTEREND   : ')'  ;
WILDCARD    : '*'  ;
ROOTTAG     : '$'  ;
REFTAG      : '^'  ;
LIBTAG      : 'LIB:' ;
IDENTIFIER  : [a-zA-Z_][a-zA-Z0-9_]* ;
NATURALNUM  : '0' | [1-9][0-9]* ;
WS : [ \t\n\r]+ -> skip ;