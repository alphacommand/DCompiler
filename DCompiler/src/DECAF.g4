grammar DECAF;

fragment LETTER: ( 'a'..'z' | 'A'..'Z') ;
fragment DIGIT: '0'..'9' ;

NUM: DIGIT(DIGIT)* ;
ID : LETTER (LETTER | DIGIT)* ;
COMMENTS: '//' ~('\r' | '\n' )*  -> channel(HIDDEN);
WS: [ \t\r\n\f]+  -> channel(HIDDEN);

CHAR: '\'' (LETTER|DIGIT|' '| '!' | '"' | '#' | '$' | '%' | '&' | '(' | ')' | '*' | '+' 
| ',' | '-' | '.' | '/' | ':' | ';' | '<' | '=' | '>' | '?' | '@' | '[' | '\\' | ']' | '^' | '_' | '`'| '{' | '|' | '}' | '~' 
'\\t'| '\\n' | '\"' | '\'' | '\n') '\'';

program
	: 'class' ID '{' (declaration)* '}'		
	; 

declaration
	:	structDeclaration					#declStruct
	|	varDeclaration						#declVar	//check
	|	methodDeclaration					#declMet	//check
	;

varDeclaration
	: 	varType ID ';'						#declSimple //check
	| 	varType ID '[' NUM ']' ';'			#declArray //check
	;


structDeclaration
	:	'struct' ID '{' (varDeclaration)* '}'	
	;

varType													//check
	: 	'int'							#varTypeInt
	|	'char'							#VarTypeChar
	|	'boolean'						#VarTypeBool
	| 	'void'							#VarTypeVoid
	| 	'struct' ID						#structID
	;

methodDeclaration
	:	methodType ID '(' (parameter (',' parameter)*)? ')' block //check
	;
	
methodType													//check
	:	'int'							#methodTypeInt
	|	'char'							#methodTypeChar
	|	'boolean'						#methodTypeBool
	|	'void'							#methodTypeVoid
	;

parameter								//check
	: 	parameterType ID				
	;
	
parameterType							//check
	:	'int'							#parameterTypeInt
	|	'char'							#parameterTypeChar
	|	'boolean'						#parameterTypeBool
	;
	
block
	:	'{' (varDeclaration)* (statement)* '}'	//check
	;

castingType								//check
	: 'int'								#castingTypeInt
	| 'char'							#castingTypeChar
	;
	
statement
	:	'if' '(' expression ')' block ('else' block)?	#ifStatement //check
	|	'while' '(' expression ')' block				#whileStatement //check
	|	'return' (expression)? ';'						#returnStatement //check
	|	methodCall ';'									#methodCallStatement //check
	|	location '='  expression ';'					#assignStatement	//check
	|   'print' '(' expression')' ';'					#printStatement 
	;
	
location
	:	(ID | ID '[' expression ']') ('.' location)? //check
	;
	
	
methodCall
	:	ID '(' (expression (',' expression )*)* ')' //check
	;
	

	
literal
	:	int_literal										//check
	|	char_literal									//check
	|	bool_literal									//check
	;
	
int_literal
	:	NUM						//check
	;

char_literal
	:	CHAR					//check
	;
	
bool_literal
	:	'true'					//check				
	|	'false'					//check				
	;
	

arith_mayor
	: '*'												#arithProduct
	| '/'												#arithDivision
	| '%'												#arithModule
	;
	
arith_menor
	: '+'												#arithPlus
	| '-'												#arithMinus
	;
	
rel_op
	:	'<'												#relL
	|	'>'												#rekB
	| 	'<='											#relLE
	|	'>='											#relBE
	;
	
eq_op
	:	'=='											#eqE
	|	'!='											#eqNE	
	;
	
cond_op1
	:	'&&'
	;
	
cond_op2
	:	'||'
	;	

expression							//check
	: expression cond_op2 expr1		#expression1
	| expr1							#expression2
	;
	
expr1								//check
	: expr1 cond_op1 expr2		#expr11
	|expr2						#expr12
	;
	
expr2								//check
	: expr2 eq_op expr3			#expr21
	| expr3						#expr22
	;

expr3								//check
	:expr3 rel_op expr4			#expr31
	| expr4						#expr32
	;

expr4								//check
	: expr4 arith_menor expr5	#expr41
	| expr5						#expr42
	;

expr5								//check
	: expr5 arith_mayor uniFactor 	#expr51
	| uniFactor						#expr52
	;

uniFactor							//check
	: '(' castingType ')' factor	#uniFactorCast
	| '-' factor					#uniFactorNeg
	| '!' factor					#uniFactorCom
	| factor						#uniFactorF
	;
	

factor 							//check
	: location					#factorLocation
	| methodCall				#factorMethodCall //todavia no
	| literal					#factorLiteral
	| '(' expression ')'		#factorExpression
	| 'input' '(' ')' 			#factorInput
	;