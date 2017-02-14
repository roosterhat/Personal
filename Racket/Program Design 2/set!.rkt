;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname set!) (read-case-sensitive #t) (teachpacks ((lib "draw.rkt" "teachpack" "htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "draw.rkt" "teachpack" "htdp")) #f)))
(define-struct entry (name number))

(define addressbook (list (make-entry "erik" 1234567890)
                          (make-entry "yo" 098764321)))

(define (addContact n nm) (set! addressbook (cons (make-entry n nm) addressbook)))

(define (swap a b)(local ((define x a)
                    (begin (set! a b)
                           (set! b x))
                    (void))))