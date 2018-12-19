.data 
	
text: 	 .space 128
	
prompt: .asciiz "Enter some text "
space: 	.byte 32
nl:     .byte 10
words: 	.asciiz " words "
chars: 	.asciiz " characters\n"
goodbye:.asciiz "Goodbye!"

.text
loop:	la $a0 prompt
	la $a1 text
	li $a2 128
	li $v0 54
	syscall
	
	bltz $a1 end
	la $t4 text
	lb $t1 space
	lb $t5 nl
	li $t2 1	#words
	li $t3 0	#characters
	
	jal sumloop
 
	la $a0 text
	li $v0 4
	syscall

	la $a0 ($t2) 
	li $v0 1
	syscall
	
	la $a0 words
	li $v0 4
	syscall
	
	la $a0 ($t3)
	li $v0 1
	syscall
	
	la $a0 chars
	li $v0 4
	syscall

	j loop
	
sumloop:
	lb $t0 ($t4)
	beq $t0 $zero sumend
	beq $t0	$t5 sumend
	bne $t0 $t1 skip
	add $t2 $t2 1
skip:	add $t3 $t3 1
	addi $t4 $t4 1
	
	j sumloop
sumend: jr $ra	
	
end:	
	li $v0 59
	la $a0 goodbye
	syscall
	
	li $v0,10
	syscall
	
