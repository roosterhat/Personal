.data

name: 	 .space 128
results: .space 12
inputs:  .space 12
space: .asciiz " "
name_prompt:
	.asciiz "Enter your name: "

int_prompt:
	.asciiz "Enter an integer from 1-100: "
	
greeting:
	.asciiz "Hello "
	
answer:
	.asciiz "Your answers are: "
	
.text
	la $a0 name_prompt
	li $v0, 4
	syscall
	
	la $a0,name
	li,$a1,128
	li $v0, 8
	syscall
	
	li	$t0,0		# iterator index
	la	$t1,inputs	# array index
	li 	$t2,3		# length of array
input_loop:	
	beq	$t0,$t2,end_input_loop
	li 	$v0, 4
	la 	$a0 int_prompt
	syscall			# prompt user
	
	li	$v0,5
	syscall 
	sw	$v0,($t1)	# add value to array
	
	add 	$t1,$t1,4
	add	$t0,$t0,1
	j input_loop
end_input_loop:

	li 	$t0,0
	la	$t1,inputs
	la	$t2,results
	lw	$t3,($t1)	# A
	lw	$t4,4($t1)	# B
	lw	$t5,8($t1)	# C
	
	add	$t0,$t3,$t4 	# A+B+C
	add	$t0,$t0,$t5
	sw	$t0,($t2)
	
	li	$t0,0		# C+B-A
	add	$t0,$t5,$t4
	sub	$t0,$t0,$t3
	sw	$t0,4($t2)
	
	li	$t0,0		#(A+2)+(B-5)-(C-1)
	add	$t0,$t3,2
	add	$t0,$t0,$t4
	sub	$t0,$t0,5
	sub	$t0,$t0,$t5
	add	$t0,$t0,1
	sw	$t0,8($t2)

	li	$v0, 4
	la	$a0, greeting	# display greeting
	syscall
	
	la	$a0, name	# display name	
	syscall

	li	$v0, 4
	la 	$a0 answer
	syscall			#results display
	
	li	$t0,0		# iterator index
	la	$t1,results	# array index
	li 	$t2,3		# length of array
display_loop:	
	beq	$t0,$t2,end_display_loop
	li	$v0,1
	lw	$a0, ($t1)
	syscall 		# display value

	la 	$a0,space	# display space
	li	$v0,4
	syscall			# add space
	
	add 	$t1,$t1,4
	add	$t0,$t0,1
	j display_loop
end_display_loop:
		
end:	li	$v0,10
	syscall
	
#Enter your name: Erik
#Enter an integer from 1-100: 7
#Enter an integer from 1-100: 17
#Enter an integer from 1-100: 27
#Hello Erik
#Your answers are: 51 37 -5 

#Enter your name: Bob
#Enter an integer from 1-100: 1
#Enter an integer from 1-100: 2
#Enter an integer from 1-100: 3
#Hello Bob
#Your answers are: 6 4 -2

#Enter your name: Rick
#Enter an integer from 1-100: 23
#Enter an integer from 1-100: 41
#Enter an integer from 1-100: 77
#Hello Rick
#Your answers are: 141 95 -15
