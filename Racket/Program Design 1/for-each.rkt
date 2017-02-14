;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname for-each) (read-case-sensitive #t) (teachpacks ((lib "image.rkt" "teachpack" "2htdp") (lib "universe.rkt" "teachpack" "2htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "image.rkt" "teachpack" "2htdp") (lib "universe.rkt" "teachpack" "2htdp")) #f)))
(define (count nmuber)(for-each (lambda (arg)(printf "Got ~a\n" arg))'()))
(define l1 (list (make-posn 1 2) (make-posn 5 8)))
(define l3 (list ))
(define l2 (append l1 (list (make-posn 12 2))))
(define l4 (append l3 (list (make-posn 12 2))))
(define (for start stop inc list func)(build-list (- stop start) (lambda (x) (func x start list))))
(define (p a b c)a)