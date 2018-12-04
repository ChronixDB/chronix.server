grammar CQL;


cql:	(chronixJoinParameter)? (chronixFunctionParameter)? ;

chronixJoinParameter: '&cj=' chronixJoinFields ;
chronixJoinFields: chronixJoinField ((',')? chronixJoinField)*;
chronixJoinField: STRING;

chronixFunctionParameter : '&cf=' chronixTypedFunctions;
chronixTypedFunctions: chronixTypedFunction ((';')? chronixTypedFunction)*;
chronixTypedFunction: chronixType '{' chronixfunction ((';')? chronixfunction)* '}';

chronixfunction
        : name
        | name ':' parameter (',' parameter)*
        ;

name: STRING;

parameter
        : STRING
        | INT STRING
        | INT
        ;
chronixType: STRING;


NEWLINE : [\r\n]+ ;
STRING  : [a-z]+ ;
INT     : [0-9]+ ;
