grammar Expression;

statement
    : effect
    // just some ideas to allow more powerful 'scripts'
    //| variableDefintion
    //| functionDefinition
    ;

effect
    : expression '->'
    // the rest of the statement is the effect, to be evaluated separately depending on the context:
    //  *  ComplexRelation: 'equal', 'subset', 'superset', 'inconsistent'
    //  *  validate: Error message
    ;

// may be implemented in the future if reasonable, currently unused
variableDefinition
    : Identifier ':=' expression
    EOF
    ;
// may be implemented in the future if reasonable, currently unused
functionDefinition
    : Identifier '(' ( Identifier (',' Identifier)* )? ')' ':=' expression
    EOF
    ;

expression: conditional;

conditional
    : logical
    | logical '?' logical ':' logical
    ;

logical
    : binary
    | binary '&&' binary
    | binary '||' binary
    ;

binary
    : unary
    | binary ('*'|'/'|'%') binary
    | binary ('+'|'-') binary
    | binary ('<<'|'>>'|'>>>') binary
    | binary ('>'|'>='|'<'|'<='|'instanceof') binary
    | binary ('=='|'!=') binary
    | binary '&' binary
    | binary '^' binary
    | binary '|' binary
    ;

unary
    : methodCall
    | ('~'|'+'|'-') unary
    ;

methodCall
    :   factor
    |   methodCall '.' Identifier '(' (conditional (',' conditional)* )? ')'
    ;

factor
    : '(' conditional ')'
    | literal
    | Identifier
    ;

literal
    : StringLiteral
    | 'true'
    | 'false'
    | IntLiteral
    | HexLiteral
    | OctalLiteral
    | FloatLiteral
    | CharLiteral
    ;

Identifier
    :   JAVA_FIRST JAVA_CONT*
    ;

StringLiteral
    :   '\'' CHAR* '\''
    ;
CharLiteral
    : '\'' CHAR '\'' ('c'|'C')
    ;
IntLiteral
    :   SIGN? ('0' | '1'..'9' DIGIT*)
    ;
HexLiteral
    :   SIGN? '0x' HEX_DIGIT+
    ;
OctalLiteral
    :   SIGN? '0' '0'..'7'+
    ;

FloatLiteral
    :   SIGN?
        (   ('0'..'9')+ '.' ('0'..'9')+ FLOAT_EXPONENT? FLOAT_TYPE_SUFFIX?
        |   '.' ('0'..'9')+ FLOAT_EXPONENT? FLOAT_TYPE_SUFFIX?
        |   ('0'..'9')+ FLOAT_EXPONENT FLOAT_TYPE_SUFFIX?
        |   ('0'..'9')+ FLOAT_TYPE_SUFFIX
        )
    ;

Whitespace
    :   WS+
    -> skip
    ;
Comment
    :   '/*' .*? '*/'
    -> skip
    ;
LineComment
    :   '//' ~('\n'|'\r')*
    -> skip
    ;

/*fragment CHAR
    :   ( '\\' . | ~('\''|'\\') )
    ;*/
fragment CHAR
    :   ('\\' ('\''|'\\'))
    |   ~('\\'|'\'')
    ;

fragment DIGIT
    :   '0'..'9'
    ;
fragment OCT_DIGIT
    :   '0'..'7'
    ;
fragment HEX_DIGIT
    :   '0'..'9' | 'a'..'f' | 'A'..'F'
    ;
fragment SIGN
    :   ('-'|'+')// WS*
    ;
fragment FLOAT_EXPONENT
    :   ('e'|'E') ('+'|'-')? ('0'..'9')+
    ;
fragment FLOAT_TYPE_SUFFIX
    :   ('f'|'F'|'d'|'D')
    ;

fragment
JAVA_FIRST
    :   [a-zA-Z$_] // these are the "java letters" below 0xFF
    |   // covers all characters above 0xFF which are not a surrogate
        ~[\u0000-\u00FF\uD800-\uDBFF]
        {Character.isJavaIdentifierStart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;
fragment
JAVA_CONT
    :   [a-zA-Z0-9$_] // these are the "java letters or digits" below 0xFF
    |   // covers all characters above 0xFF which are not a surrogate
        ~[\u0000-\u00FF\uD800-\uDBFF]
        {Character.isJavaIdentifierPart(_input.LA(-1))}?
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

fragment WS
    :   ( '\t' | ' ' | '\r' | '\n'| '\u000C' )
    ;
