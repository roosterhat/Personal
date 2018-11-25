.data
canvas:		.space 2048
width:		.word 64
height:		.word 128
file_board:	.asciiz	"Project/gameBoarder.txt"
file_pieces:	.asciiz	"Project/gamePieces.txt"
file_palette:	.asciiz	"Project/colorPalette.txt"
file_digits:	.asciiz "Project/gameDigits.txt"
palette:	.word 0x00000000 0xFFFFFFFF 0xED1C24 0xFF7F27 0X00FF00 0X0080FF 0X8000FF 0XFFFF80 0X880015 #black, white, red, orange, green, blue, purple, yellow
board_width:	.word 40
board_height:	.word 80
board:		.space 2048
frame: 		.space 2048
frame_size:	.word 306
pieces: 	.space 2048
pieces_size:	.word 670
buffer:		.space 2048
digits_size:	.word 248
digits:		.space 2048
file_err:	.asciiz "Failed to read file: "
key_input:	.word 0x41E


.text
	#main loop
	jal 	clear_canvas
	la 	$a0 file_pieces
	lw 	$a1 pieces_size
	la 	$a2 pieces
	jal 	read_file
	la	$a0 file_board
	lw	$a1 frame_size
	la	$a2 frame
	jal 	read_file
	la	$a0 file_digits
	lw	$a1 digits_size
	la	$a2 digits
	jal 	read_file
	jal	init_board
	li	$t6 0
	
while:	jal 	clear_canvas
	jal	draw_frame
	li	$a1 7
	li	$v0 42
	syscall	
	li 	$s0 4	# x 
	li 	$s1 0	# y	
	move	$s2 $a0	# index
	li 	$s3 0	# rotation
	li	$s5 10
	
	jal	draw_board
	move 	$a0 $s0
	move 	$a1 $s1
	move 	$a2 $s2
	move 	$a3 $s3
	jal 	draw_piece
	move	$a0 $s6
	jal	draw_score
	
while_p:li	$v1 0
	li	$s4 30
  	lbu     $t0, 0xffff0000
	beqz	$t0 up
	lbu     $t0 0xffff0004
	sb	$0 0xffff0004
up:	li	$t1 0x77	
	bne	$t0 $t1 left
	move	$a0 $s0
	move	$a1 $s1
	move 	$a2 $s2
	move	$a3 $s3
	addi	$a3 $a3 1
	jal 	check_collision
	beqz	$v0 left
	addi	$s3 $s3 1
	li 	$t0 4
	div	$s3 $t0
	mfhi	$s3
	li	$v1 1
left:	li	$t1 0x61
	bne	$t0 $t1 down
	bgt	$s0 $0 subt
	move	$a0 $s0
	move	$a1 $s1
	move 	$a2 $s2
	move	$a3 $s3
	jal 	check_left
	beqz	$v0 down
subt:	move	$a0 $s0
	addi	$a0 $a0 -1
	move	$a1 $s1
	move 	$a2 $s2
	move	$a3 $s3
	jal 	check_collision
	beqz	$v0 down
	addi 	$s0 $s0 -1
	li	$v1 1
down:	li	$t1 0x73
	bne	$t0 $t1 right
	li	$t1 2
	div	$s4 $t1
	mflo	$s4
right:	li	$t1 0x64
	bne	$t0 $t1 out
	move	$a0 $s0
	move	$a1 $s1
	move 	$a2 $s2
	move	$a3 $s3
	jal 	check_right
	beqz	$v0 out
	move	$a0 $s0
	addi	$a0 $a0 1
	move	$a1 $s1
	move 	$a2 $s2
	move	$a3 $s3
	jal 	check_collision
	beqz	$v0 out
	addi	$s0 $s0 1
	li	$v1 1	
out:	li 	$v0, 32
	move 	$a0, $s4
	syscall
	addi	$s5 $s5 -1
	bgt 	$s5 $0 update
	move 	$a0 $s0
	move 	$a1 $s1
	addi	$a1 $a1 1
	move	$a2 $s2
	move 	$a3 $s3
	jal 	check_collision
	beqz	$v0 done
	addi 	$s1 $s1 1
	li	$s5 10
	li	$v1 1
update:	beqz	$v1 while_p
	jal	draw_board
	move 	$a0 $s0
	move 	$a1 $s1
	move 	$a2 $s2
	move 	$a3 $s3
	jal 	draw_piece
	j 	while_p
done:	move 	$a0 $s0
	move 	$a1 $s1
	move	$a2 $s2
	move 	$a3 $s3
	jal	add_piece
	jal	update_score
	beqz	$v0 cont
	add	$s6 $s6 $v0
cont:	j	while
	j 	terminate
	
clear_canvas:
	li $t0 0
	li $t1 2048
	li $t2 0xC3C3C3
loop0:	sw $t2 canvas($t0)
	addi $t0 $t0 4
	bne $t0 $t1 loop0
	jr $ra

init_board:
	li	$t0 20
	li	$t1 40
	mul	$t0 $t0 $t1
	li	$t1 0
loop1:	add	$t2 $t1 $t0
	li	$t3 0xFFFFFFFF
	sw	$t3 board($t2)
	addi	$t1 $t1 4
	li	$t3 40
	blt	$t1 $t3 loop1
	jr	$ra

read_file:	#$a0: file path (address), $a1: read size (word), $a2: destination array (address)
	move 	$s0 $a0			# path
	move 	$s1 $a1			# size
	move	$s2 $a2			# destinations
	move 	$s7 $ra
	
	li	$v0, 13			# 13=open file
	move	$a0, $s0		# $a2 = name of file to read
	add	$a1, $0, $0		# $a1=flags=O_RDONLY=0
	add	$a2, $0, $0		# $a2=mode=0
	syscall				# Open FIle, $v0<-fd
	add	$s3, $v0, $0		# store fd in $s3
	move	$a0 $s0
	blt 	$s3 $0 file_read_err

	# Read from file, storing in buffer
	li 	$s4 0			# offset
	li	$v0, 14			# 14=read from  file
	add	$a0, $s3, $0		# $s0 contains fd
	la	$a1, buffer		# buffer to hold int
	move	$a2, $s1		# Read entire file into buffer
	syscall
loop:	lb 	$a0 buffer($s4)
	addi 	$a0 $a0 -48
	blt 	$a0 $0 skip0
	jal	get_palette_color
	sw 	$v0, ($s2)
	addi 	$s2 $s2 4
skip0:	addi 	$s4, $s4, 1
	ble 	$s4,$s1,loop
	
	# Close File
	li	$v0, 16			# 16=close file
	add	$a0, $s3, $0		# $s0 contains fd
	syscall				# close file
	jr 	$s7
	
get_palette_color:	#$a0: index (word)	return	$v0: hex color value (word)
	li	$t0 4
	mul	$t0 $t0	$a0
	lw	$v0, palette($t0)
	jr 	$ra
	
draw_piece:	#$a0: x (word), $a1: y (word), $a2: piece index (word), $a3: piece rotation (word)
	move	 $s7 $ra
	addi	 $a0 $a0 1
	addi 	 $a1 $a1 1
	li	 $t2 256	# 64 * 4		
	mul	 $t0 $a2 $t2
	li	 $t2 64		# 16 * 4
	mul	 $t1 $a3 $t2
	add	 $t0 $t0 $t1	# $t0: piece offset = (index * 64 * 4) + (rotation * 16 * 4)
	la	 $t1 pieces	
	add	 $t0 $t0 $t1	# $t0: piece address
	li	 $a2 4
	li	 $a3 4
	move	 $v0 $t0
	li	 $v1 0
	jal 	 draw
	jr	 $s7

draw_frame:
	li	$a0 0
	li	$a1 0
	li  	$a2 12
	li	$a3 22
	la	$v0 frame
	li	$v1 0
	move	$s7 $ra
	jal 	draw
	jr	$s7
	
draw_board:
	li	$a0 1
	li	$a1 1
	li	$a2 10
	li	$a3 20
	la	$v0 board
	li	$v1 1
	move	$s7 $ra
	jal	draw
	jr 	$s7
	
draw_score:	# $a0: score
	move	$s7 $ra
	move	$t8 $a0
	li	$a0 0
	li	$a1 26
	li	$a2 3
	li	$a3 5
	li	$v1 0
	li	$t9 1000
loop4:	div	$t8 $t9
	mflo	$t0
	mfhi	$t8
	li	$t1 60
	mul	$t0 $t0 $t1
	la	$v0 digits($t0)
	jal 	draw
	li	$t0 10
	div	$t9 $t0
	mflo	$t9
	addi	$a0 $a0 4
	li	$t0 16
	bne	$a0 $t0 loop4
	jr 	$s7

	
draw:	# $a0: x, $a1: y, $a2: width, $a3: height, $v0: address, $v1: draw black
	li	 $t1 0		# y
for_y:	li	 $t2 0		# x
for_x:	li	 $t3 4
	mul	 $t3 $t3 $a2 
	mul	 $t3 $t3 $t1
	li	 $t4 4
	mul	 $t4 $t4 $t2 
	add	 $t3 $t3 $t4
	add	 $t3 $t3 $v0		# $t3: address offset
	lw	 $t6 ($t3)		# $t6: pixel color value
	bnez	 $v1 black
	beqz 	 $t6 skip1
black:	lw	 $t5 width
	add	 $t4 $t1 $a1		# y + ypos
	mul	 $t4 $t4 $t5		# (y + ypos) * width
	li	 $t7 4	
	add	 $t5 $t2 $a0		# x + xpos
	mul	 $t5 $t5 $t7		# (x + xpos) * 4
	add	 $t4 $t4 $t5		# $t4: canvas offset = (ypos + y) * width + (xpos + x) * 4
	sw	 $t6 canvas($t4)
skip1:	addi	 $t2 $t2 1
	blt	 $t2 $a2 for_x
	addi	 $t1 $t1 1
	blt 	 $t1 $a3 for_y
	jr	 $ra

check_collision: # $a0: x, $a1: y, $a2: index, $a3: rotation
	li	 $t2 256	# 64 * 4		
	mul	 $t0 $a2 $t2
	li	 $t2 64		# 16 * 4
	mul	 $t1 $a3 $t2
	add	 $t0 $t0 $t1	# $t0: piece offset = (index * 64 * 4) + (rotation * 16 * 4)
	la	 $t1 pieces	
	add	 $t0 $t0 $t1	# $t0: piece address
	li	 $t1 0		# y
for_y2:	li	 $t2 0		# x
for_x2:	li	 $t3 16
	mul	 $t3 $t3 $t1
	li	 $t4 4
	mul	 $t4 $t4 $t2 
	add	 $t3 $t3 $t4
	add	 $t3 $t3 $t0		# $t3: piece address offset
	lw	 $t6 ($t3)		# $t6: piece's pixel color value
	beqz 	 $t6 skip2
	lw	 $t5 board_width
	add	 $t4 $t1 $a1		# y + ypos
	mul	 $t4 $t4 $t5		# (y + ypos) * width
	li	 $t7 4	
	add	 $t5 $t2 $a0		# x + xpos
	mul	 $t5 $t5 $t7		# (x + xpos) * 4
	add	 $t4 $t4 $t5		# $t4: board offset = (ypos + y) * width + (xpos + x) * 4
	lw	 $t8 board($t4)
	beqz	 $t8 skip2
	li	 $v0 0
	jr	 $ra
skip2:	addi	 $t2 $t2 1
	li	 $t5 4
	blt	 $t2 $t5 for_x2
	addi	 $t1 $t1 1
	blt 	 $t1 $t5 for_y2
	li	 $v0 1
	jr	 $ra

check_right: # $a0: x, $a1: y, $a2: index, $a3: rotation
	li	$t3 9
	sub	$t4 $t3 $a0
	li	$t0 4
	blt	$t4 $t0 skip3
	li	$v0 1
	jr	$ra
skip3:	li	$t2 256	# 64 * 4		
	mul	$t0 $a2 $t2
	li	$t2 64		# 16 * 4
	mul	$t1 $a3 $t2
	add	$t0 $t0 $t1	# $t0: piece offset = (index * 64 * 4) + (rotation * 16 * 4)
	la	$t1 pieces	
	add	$t0 $t0 $t1	# $t0: piece address
	li 	$t1 0
loop2:	li	$t2 4
	mul	$t3 $t4 $t2
	li	$t2 16
	mul	$t2 $t2 $t1
	add 	$t2 $t2 $t3
	add	$t2 $t2 $t0
	lw	$t3 ($t2)
	beqz	$t3 skip4
	li	$v0 0
	jr	$ra
skip4:	addi	$t1 $t1 1
	li	$t2 4
	blt	$t1 $t2 loop2
	li	$v0 1
	jr	$ra	

check_left: # $a0: x, $a1: y, $a2: index, $a3: rotation
	sub	$t4 $0 $a0
	li	$t2 256	# 64 * 4		
	mul	$t0 $a2 $t2
	li	$t2 64		# 16 * 4
	mul	$t1 $a3 $t2
	add	$t0 $t0 $t1	# $t0: piece offset = (index * 64 * 4) + (rotation * 16 * 4)
	la	$t1 pieces	
	add	$t0 $t0 $t1	# $t0: piece address
	li 	$t1 0
loop3:	li	$t2 4
	mul	$t3 $t4 $t2
	li	$t2 16
	mul	$t2 $t2 $t1
	add 	$t2 $t2 $t3
	add	$t2 $t2 $t0
	lw	$t3 ($t2)
	beqz	$t3 skip5
	li	$v0 0
	jr	$ra
skip5:	addi	$t1 $t1 1
	li	$t2 4
	blt	$t1 $t2 loop3
	li	$v0 1
	jr	$ra

add_piece:	# $a0: x, $a1: y, $a2: index, $a3: rotation
	li	 $t2 256	# 64 * 4		
	mul	 $t0 $a2 $t2
	li	 $t2 64		# 16 * 4
	mul	 $t1 $a3 $t2
	add	 $t0 $t0 $t1	# $t0: piece offset = (index * 64 * 4) + (rotation * 16 * 4)
	la	 $t1 pieces	
	add	 $t0 $t0 $t1	# $t0: piece address
	li	 $t1 0		# y
for_y3:	li	 $t2 0		# x
for_x3:	li	 $t3 16
	mul	 $t3 $t3 $t1
	li	 $t4 4
	mul	 $t4 $t4 $t2 
	add	 $t3 $t3 $t4
	add	 $t3 $t3 $t0		# $t3: piece address offset
	lw	 $t6 ($t3)		# $t6: piece's pixel color value
	beqz 	 $t6 skip7
	lw	 $t5 board_width
	add	 $t4 $t1 $a1		# y + ypos
	mul	 $t4 $t4 $t5		# (y + ypos) * width
	li	 $t7 4	
	add	 $t5 $t2 $s0		# x + xpos
	mul	 $t5 $t5 $t7		# (x + xpos) * 4
	add	 $t4 $t4 $t5		# $t4: board offset = (ypos + y) * width + (xpos + x) * 4
	sw	 $t6 board($t4)		# add pieces pixel to board
skip7:	addi	 $t2 $t2 1
	li	 $t5 4
	blt	 $t2 $t5 for_x3
	addi	 $t1 $t1 1
	blt 	 $t1 $t5 for_y3
	li	 $v0 1
	jr	 $ra

update_score:
	li	$v0 0
	li	$t0 20
for_y4:	li	$t1 10
	addi 	$t0 $t0 -1
	bnez	$t0 for_x4
	jr 	$ra
for_x4:	addi 	$t1 $t1 -1
	li	$t2 40
	mul	$t2 $t2 $t0
	li	$t3 4
	mul	$t3 $t3 $t1
	add	$t2 $t2 $t3	
	lw	$t3 board($t2)
	beqz	$t3 for_y4
	bnez	$t1 for_x4
	addi	$v0 $v0 1
	la	$t3 board
	add	$t3 $t3 $t2 
loop5:	lw	$t4 ($t3)
	addi 	$t3 $t3 -4
	#beqz 	$t4 loop5
	addi  	$t5 $t3 44
	sw	$t4 ($t5)
	la	$t2 board
	bne	$t3 $t2 loop5
	addi 	$t0 $t0 1
	j	for_y4

	
file_read_err:
	move 	$t0 $a0
	li 	$v0 4
	la 	$a0 file_err
	syscall
	move	$a0 $t0
	syscall
	j terminate
    	
terminate: 	
   	li $v0, 10
        syscall
