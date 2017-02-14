;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname |Bouncing ball|) (read-case-sensitive #t) (teachpacks ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp")) #f)))
(define width 800)
(define height 600)
(define ball (circle 10 "solid" "blue"))
(define ballX (/ width 2))
(define ballY (/ height 2))
(define xVelocity 5)
(define yVelocity 5)
(define (collisionY)(cond          
                      [(or(> (+ ballY 10) height)(< (- ballY 10) 0)) (* yVelocity -1)]
                      [else yVelocity]))
(define (collisionX)(cond
                      [(or(> (+ ballX 10) width)(< (- ballX 10) 0)) (* xVelocity -1)]
                      [else xVelocity]))
(define (moveBall x y) (place-image ball (+ ballX x) (+ ballY y) (empty-scene width height)))
(define (run t) (moveBall (collisionX) (collisionY)))
(animate run)
  