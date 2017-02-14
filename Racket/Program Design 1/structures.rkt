;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname structures) (read-case-sensitive #t) (teachpacks ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp")) #f)))
(define p1 (make-posn 100 200))
(define p2 (make-posn 435 602))
(define (getDistance a b) (sqrt (+(sqr (-(posn-x b)(posn-x a))) (sqr (-(posn-y b)(posn-y a))))))
(define-struct student(name lastname gpa))
(define erik (make-student "erik" "ostlind" 4.0))
