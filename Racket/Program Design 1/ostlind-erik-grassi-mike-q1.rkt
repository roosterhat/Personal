;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname ostlind-erik-grassi-mike-q1) (read-case-sensitive #t) (teachpacks ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp")) #f)))

(define backdrop (empty-scene 1000 800 "black"))

;flower: uses a pullled-regular-polygon to create the petals and a curve to create the stem 
(define flower(scale .5 (rotate -40(overlay/offset (pulled-regular-polygon 50 5 1.3 140 "solid" "orchid") 20 50 (add-curve (circle 0 "solid" "pink") 20 20 0 1/3 80 80 0 1/3 "darkgreen")))))

;randomStar: creates x number of stars in random locations within the backdrop
;TOOLBOX
;place-image
;random
(define (randomStar x) (cond
                        [(equal? x 0) backdrop]
                        [else (place-image (star 10 "solid" "white") (random (image-width backdrop)) (random (image-height backdrop)) (randomStar (- x 1)))]))

;randomFlower: creates x number of flowers in random locations within the ground
;TOOLBOX
;place-image
;random
(define (randomFlower x) (cond
                        [(equal? x 0) ground]
                        [else (place-image flower (random (image-width backdrop)) (+ 50 (random 100)) (randomFlower (- x 1)))]))
;ground: this is the ground in the image
(define ground (rectangle (image-width backdrop) 200 "solid" "green"))

;tree: created by using a rectangle and a circle
(define tree (overlay/offset (circle 50 "solid" "green") 0 70 (rectangle 25 150 "solid" "brown")))

(define c1(circle 90 "solid" "floralwhite"))
(define c2(circle 100 "solid" "beige"))
(define m1(overlay/offset c1 10 10 c2))
(define c3(circle 15 "solid" "linen"))
(define e1(ellipse 40 20 "solid" "linen"))
(define e2(rotate 37 e1))
(define m2(overlay/offset e2 50 65 m1))
(define m3(overlay/offset c3 40 -10 m2))
(define e3(ellipse 45 30 "solid" "linen"))
(define e4(rotate 45 e3))
(define m4(overlay/offset e4 -65 -50 m3))
(define c4(scale .75 c3))
(define m5(overlay/offset c4 -30 10 m4))
(define e5(scale 1.75 (rotate 125 e1)))
(define m6(overlay/offset e5 -65 50 m5))
(define c5(circle 6 "solid" "linen"))
(define m7(overlay/offset c5 20 -60 m6))

;moonScene: creates a new scene with random stars on the backdrop
(define moonScene (place-image m7 100 100 (randomStar 50)))
;groundscene: creates a new scene with a ground and random flowers on the ground
(define groundScene (place-image (randomFlower 10) 500 700 moonScene))
;treescene: creates a new scene with a tree in the scene
(define treeScene (place-image tree 800 505 groundScene))

;(overlay/offset m7 350 150 (overlay/offset (randomFlower 10) 0 -300 (overlay/offset tree -350 -105 (randomStar 50))))