;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname errors) (read-case-sensitive #t) (teachpacks ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp")) #f)))






(define (concat l1 l2) (append l1 l2))

(check-expect (concat '(1 2 3 4)'('a 'b 'c)) '(1 2 3 4 'a 'b 'c))
(check-expect (concat empty '(1 2 3 4)) '(1 2 3 4))

(define (getImg l n)
  (cond [(empty? l)(error 'getImg "List is too short: " l)]
        [(< (length l)n)(error 'getImg "Index out of bounds: "n)]
        [(and(cons? l)(= n 0))(first l)]
        [(and (cons? l)(> n 0))(getImg (rest l)(sub1 n))]))

