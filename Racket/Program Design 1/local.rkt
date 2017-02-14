;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname local) (read-case-sensitive #t) (teachpacks ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp")) #f)))
(local
  ((define (f x)(+ x 1))
   (define (incr-lon l)
     (cond[(empty? l)empty]
          [else(cons(f(first l)(incr-lon(rest l))))]))))

(define x 5)
(+ x (local ((defin x 100) (define z(+ xx)))z))