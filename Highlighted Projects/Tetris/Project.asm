#	___________     __          __        
#	\__    ___/____/  |________|__| ______
#	  |    | / __ \   __\_  __ \  |/  ___/
#	  |    |\  ___/|  |  |  | \/  |\___ \ 
#	  |____| \___  >__|  |__|  |__/____  >
#	             \/                    \/ 
#
#	Usage:
#		* Bitmap Display
#			-Pixel width/height: 16
#			-Display width: 256
#			-Display height: 512
#		* Keyboard and Display MMIO Simulator


.data
canvas:		.space 2048
width:		.word 64
height:		.word 128
file_board:	.asciiz	"Project/gameBoarder.txt"
file_pieces:	.asciiz	"Project/gamePieces.txt"
file_palette:	.asciiz	"Project/colorPalette.txt"
file_digits:	.asciiz "Project/gameDigits.txt"
palette:	.word 0x000000 0xFFFFFF 0xED1C24 0xFF7F27 0X00FF00 0X0080FF 0X8000FF 0XFFFF80 0X880015 #black, white, red, orange, green, blue, purple, yellow
board_width:	.word 40
board_height:	.word 80
board:		.space 2048
frame: 		.space 2048
frame_size:	.word 394
pieces: 	.space 3000
ghost_pieces: 	.space 3000
pieces_size:	.word 670
buffer:		.space 2048
digits_size:	.word 248
digits:		.space 2048
file_err:	.asciiz "Failed to read file: "
game_over:	.asciiz "Game Over"
key_input:	.word 0x41E


.text
	#main loop	
	jal 	clear_canvas		# clear canvas
	la 	$a0 file_pieces
	lw 	$a1 pieces_size
	la 	$a2 pieces
	li	$a3 0
	jal 	read_file		# read game pieces from file
	la 	$a0 file_pieces
	lw 	$a1 pieces_size
	la 	$a2 ghost_pieces
	li	$a3 0x555555		# set color override
	jal 	read_file		# read ghost pieces from file
	la	$a0 file_board
	lw	$a1 frame_size
	la	$a2 frame
	li	$a3 0
	jal 	read_file		# read game board from file
	la	$a0 file_digits
	lw	$a1 digits_size
	la	$a2 digits
	li	$a3 0
	jal 	read_file		# read score board digits from file
	jal	init_board		# initialize the board
	li	$s6 0			# $s6: score, set to 0
	li	$a1 7			
	li	$v0 42
	syscall				
	addiu	$sp $sp -4
	sw	$a0 ($sp)		# store next piece index to stack
	
while:	jal 	clear_canvas		# === Main Game loop ===
	jal	draw_frame		# draw game frame
	li	$a1 7			
	li	$v0 42
	syscall				# generate random integer
	lw	$t0 ($sp)		# load next piece index from stack
	addiu	$sp $sp 4
	move	$s2 $t0			# $s2: index, set to previously generated random number
	li 	$s0 4			# $s0: x
	li 	$s1 0			# $s1: y
	li 	$s3 0			# $s3: rotation
	li	$s4 0			# $s4: ghost piece y
	li 	$t0 10
	div 	$t6 $t0
	mflo	$t4
	sub	$s5 $t0 $t4 		# $s5: number of loop iterations	
	move	$t0 $a0	
	addiu	$sp $sp -4
	sw	$t0 ($sp)		# save next piece index to stack
	jal	draw_board		# draw game pieces
	move 	$a0 $s0
	move 	$a1 $s1
	move 	$a2 $s2
	move 	$a3 $s3
	jal	move_down
	move	$s4 $v0
	move 	$a0 $s0
	move 	$a1 $s4
	move 	$a2 $s2
	move 	$a3 $s3
	li	$v1 0
	jal 	draw_ghost_piece
	move 	$a0 $s0
	move 	$a1 $s1
	move 	$a2 $s2
	move 	$a3 $s3
	li	$v1 0
	jal 	draw_piece		# draw current piece
	li	$a0 11
	li	$a1 0
	lw	$a2 ($sp)		# load piece index from stack
	li	$a3 0
	li	$v1 1
	jal 	draw_piece		# draw next piece
	move	$a0 $s6
	jal	draw_score		# draw current score
	move 	$a0 $s0			# $s4: ghost piece y
	move 	$a1 $s1
	move 	$a2 $s2
	move 	$a3 $s3
	jal	check			# check to see if new piece is colliding with any pieces on the board, signifies the end of game
	beqz	$v0 gameover
	
	
while_p:				# ===Player Control Loop===
	li	$s7 0			# $s7: update flag, signifies if canvas should update
  	lbu     $t0, 0xffff0000		# read ready bit for key input
	beqz	$t0 out			# if not ready skip key check
	lbu     $t0 0xffff0004		# if read load key
	sb	$0 0xffff0004		# clear key
up:	li	$t1 0x77		
	bne	$t0 $t1 left		# up 'w'
	move	$a0 $s0
	move	$a1 $s1
	move 	$a2 $s2
	addi	$a3 $s3 1		# increment rotation to check next state
	li 	$t0 4		
	div	$a3 $t0			# rotation = rotation % 4
	mfhi	$a3
	jal 	check			# check to see if rotation is valid 
	beqz	$v0 out			# if not 'out'
	addi	$s3 $s3 1		# increment rotation 
	move	$s0 $v1			# set x value to return value from 'check'
	li 	$t0 4		
	div	$s3 $t0			# rotation = rotation % 4
	mfhi	$s3
	move	$a0 $s0
	move	$a1 $s1
	move 	$a2 $s2
	move	$a3 $s3
	jal	move_down
	move	$s4 $v0
	li	$s7 1			# set update flag
left:	li	$t1 0x61		
	bne	$t0 $t1 down		# left 'a'
	addi	$a0 $s0 -1		# decrement x value to check next state
	move	$a1 $s1
	move 	$a2 $s2
	move	$a3 $s3
	jal 	check			# check if move is valid
	beqz	$v0 out			# if not 'out'
	move	$s0 $v1			# set x value to return value from 'check'
	move	$a0 $s0
	move	$a1 $s1
	move 	$a2 $s2
	move	$a3 $s3
	jal	move_down
	move	$s4 $v0
	li	$s7 1			# set update flag
down:	li	$t1 0x73
	bne	$t0 $t1 right		# down 's' 
	li 	$s5 1
right:	li	$t1 0x64		
	bne	$t0 $t1 space		# right 'd'
	addi	$a0 $s0 1		# increment x value to check next state
	move	$a1 $s1
	move 	$a2 $s2
	move	$a3 $s3
	jal 	check			# check if move is valid
	beqz	$v0 out			# if not 'out'
	move	$s0 $v1			# set x value to return value from 'check'
	move	$a0 $s0
	move	$a1 $s1
	move 	$a2 $s2
	move	$a3 $s3
	jal	move_down
	move	$s4 $v0
	li	$s7 1			# set update flag
space:	li	$t1 0x20		
	bne	$t0 $t1 out
	move	$a0 $s0		
	move	$a1 $s1
	move 	$a2 $s2
	move	$a3 $s3
	jal 	move_down
	addi	$s1 $v0 -1
	li 	$s5 1
out:	li 	$v0, 32			
	li 	$a0, 30
	syscall				# wait for loop time ($s5)
	addi	$s5 $s5 -1		# decrement loop iterations
	bgt 	$s5 $0 update		# if loop iteration is greater than zero go to 'update'
	move 	$a0 $s0			# if not move piece down
	addi	$a1 $s1 1		# increment y value to check next state
	move	$a2 $s2
	move 	$a3 $s3
	jal 	check			# check if move is valid
	beqz	$v0 done		# if not piece is done go to 'done'
	addi 	$s1 $s1 1		# else increment y value
	li 	$t0 10
	div 	$t6 $t0
	mflo	$t4
	sub	$s5 $t0 $t4		# reset loop iterations 
	li	$s7 1			# set update flag
update:	beqz	$s7 while_p		# if update flag is 0 skip draw, go to top of player control loop
	jal	draw_board		# else draw board pieces
	move 	$a0 $s0
	move 	$a1 $s4
	move 	$a2 $s2
	move 	$a3 $s3
	li	$v1 0
	jal	draw_ghost_piece
	move 	$a0 $s0
	move 	$a1 $s1
	move 	$a2 $s2
	move 	$a3 $s3
	li	$v1 0
	jal 	draw_piece		# draw current piece		
	j 	while_p			# go to top of player control loop
done:	move 	$a0 $s0			# ===End of Player Control Loop===
	move 	$a1 $s1
	move	$a2 $s2
	move 	$a3 $s3
	li	$v1 0
	jal	add_piece		# add current piece to board
	jal	update_score		# check to see if line(s) completed 
	beqz	$v0 while		# if not go to top of Game Loop
	add	$s6 $s6 $v0		# if line(s) completed add return value to score
	j	while

	
clear_canvas:				# clears the canvas and set the background to grey 
	li $t0 0
	li $t1 2048
	li $t2 0xC3C3C3			# gray
loop0:	sw $t2 canvas($t0)
	addi $t0 $t0 4
	bne $t0 $t1 loop0
	jr $ra

init_board:				# adds a bottom to the board so that pieces collide and stop at the bottom
	li	$t0 20
	li	$t1 40
	mul	$t0 $t0 $t1		# go to the bottom of the board
	li	$t1 0
loop1:	add	$t2 $t1 $t0
	li	$t3 0xFFFFFF		# white
	sw	$t3 board($t2)
	addi	$t1 $t1 4
	li	$t3 40
	blt	$t1 $t3 loop1
	jr	$ra

read_file:	#$a0: file path (address), $a1: read size (word), $a2: destination array (address), $a3 color override
	addiu	$sp $sp -4
	sw	$ra ($sp)		# store jump address to stack
	move 	$t0 $a0			# path
	move 	$t1 $a1			# size
	move	$t2 $a2			# destinations	
	move	$t5 $a3
	
	li	$v0 13	
	move	$a0 $t0		
	add	$a1 $0, $0		
	add	$a2 $0, $0		
	syscall				# Open FIle
	add	$t3 $v0 $0		# store file descriptor
	move	$a0 $s0			# move file name to parameter incase error opening file
	blt 	$t3 $0 file_read_err

	li	$v0 14			
	add	$a0 $t3 $0		
	la	$a1 buffer		
	move	$a2 $t1			# Read entire file into buffer
	syscall
	
	li	$t4 0
loop:	lb 	$a0 buffer($t4)		# load 
	addi 	$a0 $a0 -48		# subtract 48 to get integer value	
	blt	$a0 $0 skip0
	bnez	$t5 color_override
	jal	get_palette_color	# otherwise convert integer to color word value		
	j	load_color	
color_override:
	li	$v0 0
	beqz 	$a0 load_color		# skip if black
	move	$v0 $t5
load_color:
	sw 	$v0 ($t2)		# store in memory address 
	addi 	$t2 $t2 4		# increment memory address
skip0:	addi 	$t4 $t4 1		# increment buffer offset
	blt 	$t4 $t1 loop		# break if offset is greater than or equal to file size
	
	li	$v0 16			
	add	$a0 $t3 $0		
	syscall				# close file
	lw	$ra ($sp)
	addiu	$sp $sp 4		# load jump address from stack
	jr	$ra
	
get_palette_color:	#$a0: index (word)	return	$v0: hex color value (word)
	li	$t0 4
	mul	$t0 $t0	$a0
	lw	$v0 palette($t0)	# gets the word value for the corresponding integer
	jr 	$ra
	
draw_ghost_piece:	# $a0: x (word), $a1: y (word), $a2: piece index (word), $a3: piece rotation (word)
	addiu	$sp $sp -4
	sw 	$ra ($sp)		
	la	$v0 ghost_pieces	
	jal 	draw_piece_base
	lw	$ra ($sp)
	addiu	$sp $sp 4		
	jr	$ra
	
draw_piece:	# $a0: x (word), $a1: y (word), $a2: piece index (word), $a3: piece rotation (word)
	addiu	$sp $sp -4
	sw 	$ra ($sp)		
	la	$v0 pieces	
	jal 	draw_piece_base
	lw	$ra ($sp)
	addiu	$sp $sp 4		
	jr	$ra

draw_piece_base:	#$a0: x (word), $a1: y (word), $a2: piece index (word), $a3: piece rotation (word), $v0 address
	addiu	 $sp $sp -4
	sw 	 $ra ($sp)		# save jump address
	addi	 $a0 $a0 1		# increment x value to represent position on canvas rather than board
	addi 	 $a1 $a1 1		# increment y value to represent position on canvas rather than board
	li	 $t2 256		# 64 * 4		
	mul	 $t0 $a2 $t2
	li	 $t2 64			# 16 * 4
	mul	 $t1 $a3 $t2
	add	 $t0 $t0 $t1		# $t0: piece offset = (index * 64 * 4) + (rotation * 16 * 4)
	add	 $v0 $t0 $v0		# piece address
	li	 $a2 4			# width
	li	 $a3 4			# height
	jal 	 draw
	lw	 $ra ($sp)
	addiu	 $sp $sp 4		# load jump address
	jr	 $ra

draw_frame:
	li	$a0 0			# x position
	li	$a1 0			# y position
	li  	$a2 16			# width
	li	$a3 22			# height
	la	$v0 frame		# frame address
	li	$v1 0			# dont draw black
	addiu	$sp $sp -4
	sw	$ra ($sp)
	jal 	draw
	lw	$ra ($sp)
	addiu	$sp $sp 4
	jr	$ra
	
draw_board:
	li	$a0 1			# x position
	li	$a1 1			# y position
	li	$a2 10			# width
	li	$a3 20			# height
	la	$v0 board		# board address
	li	$v1 1			# draw black
	addiu	$sp $sp -4
	sw	$ra ($sp)
	jal 	draw
	lw	$ra ($sp)
	addiu	$sp $sp 4
	jr	$ra
	
draw_score:	# $a0: score
	addiu	$sp $sp -4
	sw	$ra ($sp)
	move	$t8 $a0			# save score
	li	$a0 0			# x position
	li	$a1 26			# y position
	li	$a2 3			# width
	li	$a3 5			# height
	li	$v1 0			# dont draw black
	li	$t9 1000		# initial digit
loop4:	div	$t8 $t9			# score / value
	mflo	$t0			# get digit value
	mfhi	$t8			# remainder
	li	$t1 60			
	mul	$t0 $t0 $t1		# size of each digit in memory * digit value
	la	$v0 digits($t0)		# digit address
	jal 	draw
	li	$t0 10			
	div	$t9 $t0			# divide value by 10 to get next digit
	mflo	$t9			
	addi	$a0 $a0 4		# increment x value 
	li	$t0 16
	bne	$a0 $t0 loop4		# compare x value to 16 ( loop 4 times )
	lw	$ra ($sp)
	addiu	$sp $sp 4
	jr	$ra
	
draw:	# $a0: xpos, $a1: ypos, $a2: width, $a3: height, $v0: address, $v1: draw black	( Copies the given memory location to a space in the canvas memory segment specified by the x,y coordinates )
	li	 $t1 0			# y 
for_y:	li	 $t2 0			# x 
for_x:	li	 $t3 4
	mul	 $t3 $t3 $a2 		# width * 4
	mul	 $t3 $t3 $t1		# width * 4 * y
	li	 $t4 4
	mul	 $t4 $t4 $t2 		# x * 4
	add	 $t3 $t3 $t4		# ( width * 4 * y ) + ( x * 4 )
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

move_down: # $a0: x, $a1: y, $a2: index, $a3: rotation    return $v0: new y coordinate
	addiu	$sp $sp -4
	sw	$ra ($sp)
loop7:	addi	$a1 $a1 1
	jal	check
	beqz	$v0 skip4
	j	loop7
skip4:  addi 	$v0 $a1 -1
	lw	$ra ($sp)
	addiu	$sp $sp 4
	jr	$ra

check: # $a0: x, $a1: y, $a2: index, $a3: rotation	return $v0: bool valid, $v1: new x coordinate ( Checks whether the piece is colliding with any pixels in the board, and attempts to move the piece into the bounds of the board )
	li	$v0 1
	li	$t2 256			# 64 * 4		
	mul	$t0 $a2 $t2
	li	$t2 64			# 16 * 4
	mul	$t1 $a3 $t2
	add	$t0 $t0 $t1		# $t0: piece offset = (index * 64 * 4) + (rotation * 16 * 4)
	la	$t1 pieces	
	add	$t0 $t0 $t1		# $t0: piece address
	li	$t1 0			# y
for_y2:	li	$t2 0			# x
for_x2:	li	$t3 16
	mul	$t3 $t3 $t1
	li	$t4 4
	mul	$t4 $t4 $t2 
	add	$t3 $t3 $t4
	add	$t3 $t3 $t0		# $t3: piece address offset
	lw	$t6 ($t3)		# $t6: piece's pixel color value
	beqz 	$t6 skip2
	lw	$t5 board_width
	add	$t4 $t1 $a1		# y + ypos
	mul	$t4 $t4 $t5		# (y + ypos) * width
	li	$t7 4	
	add	$t5 $t2 $a0		# x + xpos
	mul	$t5 $t5 $t7		# (x + xpos) * 4
	add	$t4 $t4 $t5		# $t4: board offset = (ypos + y) * width + (xpos + x) * 4
	lw	$t8 board($t4)		# get color at position in board 
	beqz	$t8 skip3		# if black skip
	li	$v0 0
skip3:	li	$t3 5			
	add	$t5 $a0 $t2		# x position + x
	bgt	$t5 $t3 rhalf		# if greater than 5 right half of board else left half
	bgez	$t5 skip2		# if greater equal to 0 skip
	addi	$a0 $a0 1		# increment x position to move toward bounds
	j 	check
rhalf:	li	$t3 9			
	ble	$t5 $t3 skip2		# if less equal to 9 skip
	addi	$a0 $a0 -1		# decrement x position to move toward bounds
	j 	check
skip2:	addi	$t2 $t2 1
	li	$t5 4
	blt	$t2 $t5 for_x2	
	addi	$t1 $t1 1
	blt 	$t1 $t5 for_y2
	move	$v1 $a0
	jr	$ra

add_piece:	# $a0: x, $a1: y, $a2: index, $a3: rotation	( Adds the current piece to the board in the specified position and orientation )
	li	 $t2 256		# 64 * 4		
	mul	 $t0 $a2 $t2
	li	 $t2 64			# 16 * 4
	mul	 $t1 $a3 $t2
	add	 $t0 $t0 $t1		# $t0: piece offset = (index * 64 * 4) + (rotation * 16 * 4)
	la	 $t1 pieces	
	add	 $t0 $t0 $t1		# $t0: piece address
	li	 $t1 0			# y
for_y3:	li	 $t2 0			# x
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
	jr	 $ra

update_score:	# return $v0: number of lines cleared 	( Checks to see if any lines have been completed, if so it copies the lines above the completed row down 1 row )
	li	$v0 0			# return value initialized to 0
	li	$t0 20			# y, start at the bottom and work up
for_y4:	li	$t1 10			# x, start at the right and work left
	addi 	$t0 $t0 -1		# decrement y 
	bnez	$t0 for_x4
	move	$t7 $v0
	li 	$v0, 32			
	li 	$a0, 200
	syscall
	move	$v0 $t7
	jr 	$ra
for_x4:	addi 	$t1 $t1 -1		# decrement x
	li	$t2 40			
	mul	$t2 $t2 $t0		# y * width 
	li	$t3 4
	mul	$t3 $t3 $t1		# x * 4
	add	$t2 $t2 $t3		# ( y * width ) + ( x * 4 )
	lw	$t3 board($t2)		# load value from board
	beqz	$t3 for_y4		# if zero, line imcomplete, go to next line
	bnez	$t1 for_x4		# loop if x greater than 0 else line complete

	lw	$t5 width
	sub	$t6 $t0 $v0
	mul	$t6 $t6 $t5
	add 	$t6 $t6 $t5
loop6:	addi	$t6 $t6 4
	li	$t7 0xFFFFFF
	sw	$t7 canvas($t6)
	sub	$t7 $t0 $v0
	mul	$t7 $t7 $t5
	add	$t7 $t7 $t5
	li	$t8 40
	add	$t7 $t7 $t8
	blt	$t6 $t7 loop6
	addi	$v0 $v0 1		# increment lines complete
loop5:					
	addi 	$t2 $t2 -4		# move 1 word to the left 
	lw	$t3 board($t2)		# loop through board and move each pixel down 1 row
	addi  	$t4 $t2 40		# add length of row to move it 1 row down
	sw	$t3 board($t4)		# store value in new position			
	bnez	$t2 loop5		# branch if not at the start of board
	addi 	$t0 $t0 1		# increment y to start at new moved row
	j	for_y4

file_read_err:	# $a0: file name	( Prints error message 'Failed to read file: FILENAME' )
	move 	$t0 $a0			# save file name
	li 	$v0 4		
	la 	$a0 file_err		# load error message 
	syscall				# print
	move	$a0 $t0			# load file name
	syscall				# print
	j terminate
   
gameover:	# ( Prints message 'Game Over' )
	la	$a0 game_over		# load game over message
	li	$v0 4	
	syscall				# print
	j 	terminate 	
    	
terminate: 	# exit cleanly
   	li $v0, 10
        syscall
