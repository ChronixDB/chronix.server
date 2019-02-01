grammar CQLCF;


cqlcf: chronixTypedFunctions;
chronixTypedFunctions: chronixTypedFunction ((';')? chronixTypedFunction)*;
chronixTypedFunction: chronixType '{' chronixfunction (';' chronixfunction)* '}';


chronixType: LOWERCASE_STRING;
chronixfunction
        : name
        | name ':' parameter (',' parameter)*
        ;
name: LOWERCASE_STRING;
parameter: STRING_AND_NUMBERS_UPPERCASE;


LOWERCASE_STRING  : [a-z]+ ;
STRING_AND_NUMBERS_UPPERCASE: [A-Z0-9.]+ ;
