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
MOV R5,#8
ADD R5,R5,#0
STR R4,[R11,R5]
MOV R4,#97
MOV R5,#8
ADD R5,R5,#4
STR R4,[R11,R5]
MOV R4,#8
ADD R4,R4,#0
LDR R4,[R11,R4]
LDR R0, =_formatoInt
MOV R1,R4
BL printf
MOV R4,#8
ADD R4,R4,#4
LDR R4,[R11,R4]
LDR R0, =_formatoChar
MOV R1,R4
BL printf
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
	.space 0