;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname 1_19_16) (read-case-sensitive #t) (teachpacks ((lib "draw.rkt" "teachpack" "htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "draw.rkt" "teachpack" "htdp")) #f)))
;(define (fractal sides depth) (local [(define (drawShape points list)(cond[(= (length list )1)(drawLine (first list)(first points))]
;                                                                   [else (and (drawLine (first list)(second list))(drawShape points (rest list)))]))
;                                      (define (drawLine p1 p2)(draw-solid-line (posn-x p1)(posn-y p1)(posn-x p2)(posn-y p2)))
;                                      (define (fractalize d)(cond[(= d depth)0]
;                                                                 [else (drawShape
;                                      ]))

;; sierpinski : posn posn posn  ->  true
;; to draw a Sierpinski triangle down at a, b, and c,
;; assuming it is large enough
(define (sierpinski a b c)
  (cond
    [(too-small? a b c) true]
    [else 
      (local ((define a-b (mid-point a b))
	      (define b-c (mid-point b c))
	      (define c-a (mid-point a c)))
	(and
	  (draw-triangle a b c)	    
	  (sierpinski a a-b c-a)
	  (sierpinski b a-b b-c)
	  (sierpinski c c-a b-c)))]))

;; mid-point : posn posn  ->  posn
;; to compute the mid-point between a-posn and b-posn
(define (mid-point a-posn b-posn)
  (make-posn
    (mid (posn-x a-posn) (posn-x b-posn))
    (mid (posn-y a-posn) (posn-y b-posn))))

;; mid : number number  ->  number
;; to compute the average of x and y
(define (mid x y)
  (/ (+ x y) 2))

(define (draw-triangle p1 p2 p3)(and (draw-solid-line p1 p2)
                                     (draw-solid-line p2 p3)
                                     (draw-solid-line p3 p1)))
(define (too-small? p1 p2 p3)(< (sqrt (+(sqr(- (posn-y p2) (posn-y p1)))
                                       (sqr(- (posn-x p2) (posn-x p1)))))1))
(start 400 400)
(define A (make-posn 200 0))
(define B (make-posn 27 300))
(define C (make-posn 373 300))
  




(define (tabulateDiv n)
  (local [(define (computeDiv d)(cond[(>= d n)(list n)]
                                     [(= (remainder n d)0)(cons d (computeDiv (add1 d)))]
                                     [else (computeDiv (add1 d))]))]
    (computeDiv 1)))


(define (createMatrix n l)(cond[(empty? l)empty]
                               [else (local [(define (getNumbers a list)(cond[(= a 0)empty]
                                                                        [else (cons (first list)(getNumbers (sub1 a) (rest list)))]))
                                             ;(define (getNumbers list)(build-list n (lambda (i) (list-ref list i))))
                                             (define (getRest a list)(cond[(= a 0)list]
                                                                          [else (getRest (sub1 a)(rest list))]))] 
                                       (cons(getNumbers n l)(createMatrix n (getRest n l))))]))
                            