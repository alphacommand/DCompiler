.globl main
main:
stmfd sp!, {lr}
LDR R11, =_dataGlobal
push {lr}
BL main0
B _salir
ackermann0:
pop {R4}
STR R4,[R11,#4]
pop {R4}
STR R4,[R11,#0]
push {lr}
MOV R4,#0
LDR R4,[R11,R4]
MOV R5,#0
CMP R4,R5
MOVEQ R4,#1
MOVNE R4,#0
CMP R4,#0
BEQ label1
MOV R4,#4
LDR R4,[R11,R4]
MOV R5,#1
ADD R4,R4,R5
pop {R5}
push {R4}
MOV pc,R5
label1:
MOV R4,#4
LDR R4,[R11,R4]
MOV R5,#0
CMP R4,R5
MOVEQ R4,#1
MOVNE R4,#0
CMP R4,#0
BEQ label2
LDR R4,[R11,#0]
push {R4}
LDR R4,[R11,#4]
push {R4}
LDR R4,[R11,#8]
push {R4}
MOV R4,#0
LDR R4,[R11,R4]
MOV R5,#1
SUB R4,R4,R5
push {R4}
MOV R4,#1
push {R4}
BL ackermann0
pop {R4}
pop {R5}
STR R5,[R11,#12]
pop {R5}
STR R5,[R11,#8]
pop {R5}
STR R5,[R11,#4]
pop {R5}
push {R4}
MOV pc,R5
label2:
LDR R4,[R11,#0]
push {R4}
LDR R4,[R11,#4]
push {R4}
LDR R4,[R11,#8]
push {R4}
MOV R4,#0
LDR R4,[R11,R4]
push {R4}
MOV R4,#4
LDR R4,[R11,R4]
MOV R5,#1
SUB R4,R4,R5
push {R4}
BL ackermann0
pop {R4}
pop {R5}
STR R5,[R11,#8]
pop {R5}
STR R5,[R11,#4]
pop {R5}
STR R5,[R11,#0]
MOV R5,#8
STR R4,[R11,R5]
LDR R4,[R11,#0]
push {R4}
LDR R4,[R11,#4]
push {R4}
LDR R4,[R11,#8]
push {R4}
MOV R4,#0
LDR R4,[R11,R4]
MOV R5,#1
SUB R4,R4,R5
push {R4}
MOV R4,#8
LDR R4,[R11,R4]
push {R4}
BL ackermann0
pop {R4}
pop {R5}
STR R5,[R11,#8]
pop {R5}
STR R5,[R11,#4]
pop {R5}
STR R5,[R11,#0]
pop {R5}
push {R4}
MOV pc,R5
main0:
push {lr}
MOV R4,#1
MOV R5,#4
STR R4,[R11,R5]
MOV R4,#4
MOV R5,#8
STR R4,[R11,R5]
LDR R4,[R11,#0]
push {R4}
LDR R4,[R11,#4]
push {R4}
LDR R4,[R11,#8]
push {R4}
MOV R4,#4
LDR R4,[R11,R4]
push {R4}
MOV R4,#8
LDR R4,[R11,R4]
push {R4}
BL ackermann0
pop {R4}
pop {R5}
STR R5,[R11,#8]
pop {R5}
STR R5,[R11,#4]
pop {R5}
STR R5,[R11,#0]
MOV R5,#0
STR R4,[R11,R5]
MOV R4,#0
LDR R4,[R11,R4]
LDR R0, =_formatoInt
MOV R1,R4
BL printf
pop {pc}
IndexOutOfBounds:
LDR R0, =_IOOB
BL puts
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
	.space 12