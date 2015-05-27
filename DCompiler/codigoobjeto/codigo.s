.globl main
main:
stmfd sp!, {lr}
LDR R11, =_dataGlobal
push {lr}
BL main0
B _salir
main0:
push {lr}
LDR R1,=input
LDR R0,=_scanformat
BL scanf
LDR R1,=input
LDR R4,[R1,#0]
MOV R5,#8
STR R4,[R11,R5]
LDR R1,=input
LDR R0,=_scanformat
BL scanf
LDR R1,=input
LDR R4,[R1,#0]
MOV R5,#12
STR R4,[R11,R5]
MOV R4,#8
LDR R4,[R11,R4]
MOV R5,#12
LDR R5,[R11,R5]
pendiente
MOV R5,#0
STR R4,[R11,R5]
MOV R4,#8
LDR R4,[R11,R4]
MOV R5,#12
LDR R5,[R11,R5]
pendiente
MOV R5,#4
STR R4,[R11,R5]
MOV R4,#100
LDR R0, =_formatoChar
MOV R1,R4
BL printf
MOV R4,#0
LDR R4,[R11,R4]
LDR R0, =_formatoInt
MOV R1,R4
BL printf
MOV R4,#109
LDR R0, =_formatoChar
MOV R1,R4
BL printf
MOV R4,#4
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
	.space 8