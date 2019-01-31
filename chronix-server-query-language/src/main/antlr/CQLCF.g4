grammar CQLCF;


cqlcf: chronixTypedFunctions;
chronixTypedFunctions: chronixTypedFunction ((';')? chronixTypedFunction)*;
chronixTypedFunction: chronixType '{' chronixfunction ((';')? chronixfunction)* '}';

chronixfunction
        : name
        | name ':' parameter (',' parameter)*
        ;

name: LOWERCASE_CHAR;

parameter
        : STRING_LITERAL+;

chronixType: LOWERCASE_CHAR;

LOWERCASE_CHAR  : [a-z]+ ;
STRING_LITERAL: 'a'..'z' | 'A'..'Z' | '0'..'9' | ':' | '.' | ',' | '&' | '/' | '\\';
