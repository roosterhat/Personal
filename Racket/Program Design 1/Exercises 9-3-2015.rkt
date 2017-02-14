;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname |Exercises 9-3-2015|) (read-case-sensitive #t) (teachpacks ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp")) #f)))
(define eye (overlay (star 20 "solid" "black") (circle 20 "solid" "white")))
(define smile(overlay/offset(square 10 "solid" "black") 35 5 (overlay/offset (rectangle 50 10 "solid" "black") 30 -10 (square 10 "solid" "balck"))))
(define nose (square 10 "solid" "black"))
(define test (overlay/offset (circle 20 "solid" "orange") 30 0 (rectangle 60 20 "solid" "green")))
(define backdrop (empty-scene 500 300 "white"))

;spaceship: creates a spaceship using overlay/offset and 2 shapes
(define spaceship (overlay  (rectangle 80 10 "solid" "black") (circle 20 "solid" "blue")))

;banner: creates a banner using overlay with a shape and text
(define banner (overlay (text "ERIK" 36 "white") (rectangle 100 40 "solid" "red")))

;shadowTriangle: creates a triangle with its shadow using above with 2 shapes 
(define shadowTriangle (above (triangle 40 "solid" "blue") (flip-vertical(triangle 40 "solid" "grey"))))

;smiley: combines 2 eys, a smile, and a nose inside a circle
(define smiley (overlay/offset eye 20 20 (overlay/offset eye -20 20 (overlay/offset smile 0 -30 (overlay/offset nose 0 -10(circle 60 "solid" "yellow"))))))

;double-mirror-image: takes a img ---> imggmi
;INVENTORY
;img is an object
;(scale 2 img) doubles the size of the image
;(flip-horizontal img) flips the image 
(define (double-mirror-image img) (beside (scale 2 img) (flip-horizontal (scale 2 img))))

;imageAtPer: takes an img and percentage and places that image at the height which is a certain percentage of the total height
;INVENTORY
;img is an object
;p is a int which represents a percentage
;place-image places an image
;(/ p 100) converts an int to a double which is a percentage
;image-width and image-height allows the size of the scene to change and make the method dynamic 
(define (imageAtPer img p) (place-image img (/(image-width backdrop) 2) (- (image-height backdrop) (* (image-height backdrop) (/ p 100))) backdrop))