.data
height:			.word 	0
weight:			.word 	0
bmi:			.float 	0.0
name: 			.space 	128
under_bmi:		.float 	18.5
normal_bmi: 		.float 	25.0
over_bmi:		.float 	30
prompt_name: 		.asciiz "Enter your name: "
prompt_height: 		.asciiz "Enter your height in inches: "
prompt_weight: 		.asciiz "Enter your weight in pounds: "
show_bmi:		.asciiz ", your bmi is: "
show_underweight: 	.asciiz "\nThis is considered underweight.\n" 
show_normalweight:	.asciiz "\nThis is a normal weight.\n"
show_overweight: 	.asciiz "\nThis is considered overweight.\n"
show_obese:		.asciiz	"\nThis is considered obese.\n"

.text

	la $a0, prompt_name	#display 'Enter your name: '
	li $v0, 4
	syscall
	
	la $a0,name		#get user input and store it in 'name'
	li $a1,128
	li $v0, 8
	syscall
	
	li $s0,0 
remove:				#removes newline character
	lb $a3,name($s0)    	#load character at index
    	bne $a3,10,skip 
    	sb $0, name($s0)    	#add the terminating character in its place
skip:   addi $s0,$s0,1  
  	bnez $a3,remove
	
	la $a0, prompt_height	#display 'Enter your height: '
	li $v0, 4
	syscall
	
	li $v0, 5		#read user input
	syscall
	sw $v0,height		#store value in 'height'
	
	la $a0, prompt_weight	#display 'Enter your weight: '
	li $v0, 4
	syscall
	
	li $v0, 5		#read user input
	syscall
	sw $v0,weight		#store value in 'weight'
	
	lw $t0, weight		
	lw $t1, height		
	mul $t0, $t0, 703	#weight *= 703
	mul $t1, $t1, $t1	#height *= height
	
	mtc1 $t0, $f0		#move weight to coprossesor 1
  	cvt.s.w $f0, $f0	#convert word to float
  	mtc1 $t1, $f1		#move height to coprocessor 1
  	cvt.s.w $f1, $f1	#convert word to float
  	
	div.s $f2, $f0, $f1	#bmi = weight/height
	s.s $f2, bmi
	
	la $a0, name		#display 'name'
	li $v0, 4
	syscall
	
	la $a0, show_bmi	#display 'show_bmi'
	li $v0, 4
	syscall
	
	l.s $f12, bmi		#display 'bmi'
	li $v0, 2
	syscall
	
	li $v0, 4
	l.s $f0, under_bmi		#load under weight bmi
	c.le.s $f0, $f2			#compare floats
	bc1t normal			#branch if coprocessor flag is true
	la $a0, show_underweight	#otherwise display message
	syscall
	j exit				
normal:	l.s $f0, normal_bmi		#load under weight bmi
	c.le.s $f0, $f2			#compare floats
	bc1t over			#branch if coprocessor flag is true
	la $a0, show_normalweight	#otherwise display message
	syscall
	j exit
over:   l.s $f0, over_bmi		#load under weight bmi
	c.le.s $f0, $f2			#compare floats
	bc1t obese			#branch if coprocessor flag is true
	la $a0, show_overweight		#otherwise display message
	syscall
	j exit	
obese:	la $a0, show_obese		#display message
	syscall
	j exit
	
exit:	
	li $v0,10
	syscall