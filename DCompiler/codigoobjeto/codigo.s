.globl main
main:
stmfd sp!, {lr}
LDR R11, =_dataGlobal
push {lr}
BL main0
B _salir
main0:
push {lr}
MOV R4,#97
MOV R5,#28
STR R4,[R11,R5]
MOV R4,#0
MOV R5,#24
STR R4,[R11,R5]
label1:
MOV R4,#24
LDR R4,[R11,R4]
MOV R5,#3
CMP R4,R5
MOVLT R4,#1
MOVGE R4,#0
CMP R4,#0
BEQ label2
MOV R4,#24
LDR R4,[R11,R4]
MOV R5,#28
LDR R5,[R11,R5]
ADD R4,R4,R5
MOV R5,#0
MOV R6,#24
LDR R6,[R11,R6]
MOV R0,#8
MUL R7,R6,R0
ADD R5,R7,R5
ADD R5,R5,#0
STR R4,[R11,R5]
MOV R4,#24
LDR R4,[R11,R4]
MOV R5,#28
LDR R5,[R11,R5]
ADD R4,R4,R5
MOV R5,#0
MOV R6,#24
LDR R6,[R11,R6]
MOV R0,#8
MUL R7,R6,R0
ADD R5,R7,R5
ADD R5,R5,#4
STR R4,[R11,R5]
MOV R4,#0
MOV R5,#24
LDR R5,[R11,R5]
MOV R0,#8
MUL R6,R5,R0
ADD R4,R6,R4
ADD R4,R4,#0
LDR R4,[R11,R4]
LDR R0, =_formatoInt
MOV R1,R4
BL printf
MOV R4,#0
MOV R5,#24
LDR R5,[R11,R5]
MOV R0,#8
MUL R6,R5,R0
ADD R4,R6,R4
ADD R4,R4,#4
LDR R4,[R11,R4]
LDR R0, =_formatoChar
MOV R1,R4
BL printf
MOV R4,#24
LDR R4,[R11,R4]
MOV R5,#1
ADD R4,R4,R5
MOV R5,#24
STR R4,[R11,R5]
B label1
label2:
pop {pc}
_salir:
mov r0, #0
mov r3, #0
ldmfd sp!, {lr}
BX lr

.section .data
.align 2
_IOOB:
	.asciz "El indice no esta dentro del rango del arreglo "
_formatoInt:
	.asciz "%d\n"
_formatoChar:
	.asciz "%c\n"
_scanformat:
	.asciz "%d"
input:
	.word 0
_dataGlobal:
	.space 32