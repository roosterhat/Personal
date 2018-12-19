.data
file:		.asciiz "data.txt"
corrupted:	.asciiz "File data is corrupted"
uncorrupted:	.asciiz "File data is good"
buffer:		.space 46
file_size:	.word 46


.text

	li	$v0, 13			# 13=open file
	la	$a0,file 		# $a0 = name of file to read
	add	$a1, $0, $0		# $a1=flags=O_RDONLY=0
	add	$a2, $0, $0		# $a2=mode=0
	syscall				# Open FIle, $v0<-fd
	add	$s3, $v0, $0		# store fd in $s3

	li	$v0, 14			# 14=read from  file
	add	$a0, $s3, $0		# $s0 contains fd
	la	$a1, buffer		# buffer to hold int
	lw	$a2, file_size		# Read entire file into buffer
	syscall
	
	li	$v0, 16			# 16=close file
	add	$a0, $s3, $0		# $s0 contains fd
	syscall				# close file
	
	li	$v0 9			# allocate buffer space in heap
	lw	$a0 file_size
	syscall
	move	$s0 $v0			# $s0: pointer for new buffer
	
	li	$s1 0			# copy data from old buffer to new buffer + parity 
loop:	lb	$s2 buffer($s1)		# $s2: current byte
	move	$a0 $s2
	jal	get_par			# get byte parity
	move	$t2 $v0
	sll	$s2 $s2 1		# clear MSB
	srl	$s2 $s2 1
	beqz	$t2 out			# if odd set MSB to 1 else do nothing
	ori	$s2 $s2 0x80		# or byte to set MSB to 1
out:	add	$t2 $s0 $s1		# get next buffer address
	sb	$s2 ($t2)		# copy byte
	addi	$s1 $s1 1
	lw	$t1 file_size
	blt	$s1 $t1 loop
	
	li	$s1 0			# check parity for corruption (BREAK POINT)
loop1:	add	$t0 $s0 $s1		# get next buffer address
	lb	$a0 ($t0)		# $a0: current byte
	jal 	check_par		# check parity of byte
	bnez	$v0 skip		# if false goto 'failed'
	j	failed
skip:	addi 	$s1 $s1 1		
	lw	$t0 file_size
	blt	$s1 $t0 loop1
	j	succ			# otherwise goto 'succ'
	
check_par:				# confirms parity of byte
	move 	$t4 $a0			# copy input
	andi  	$a0 $a0 0x0000007F	# remove parity bit from byte
	move	$t5 $ra			# save jump address
	jal	get_par			# get parity of clipped byte 
	andi	$t0 $t4 0x80		# get MSB of input byte
	srl	$t0 $t0 7		# shift MSB to LSB
	beq	$t0 $v0 eq		# compare to parity
	li	$v0 0			# 0: false
	jr 	$t5
eq:	li	$v0 1			# 1: true
	jr	$t5
			
get_par:
	li 	$t0 0			# $t0: increment
	li	$t1 0			# $t1: 1 count
par_l:	andi	$t2 $a0 0x80		# get the MSB
	srl	$t2 $t2 7		# shift MSB to LSB
	add	$t1 $t1 $t2		# add to '1 count'
	sll	$a0 $a0 1		# shift input to the left 1 bit
	andi 	$a0 $a0 0x000000FF	# crop input to one byte
	li 	$t2 8		
	addi	$t0 $t0 1
	blt 	$t0 $t2 par_l
	li	$t0 2			# preform '1 count' % 2
	div	$t1 $t0
	mfhi	$v0			# return divide remainder
	jr	$ra	

failed:
	li 	$v0 4
	la	$a0 corrupted
	syscall
	j terminate

succ:
	li	$v0 4
	la	$a0 uncorrupted
	syscall
	j terminate
	
terminate: 	
   	li $v0, 10
        syscall
	
	
	
	
