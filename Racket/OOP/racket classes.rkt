#lang class/0

(define-class 3D-posn% (fields x y z)
  (define (yes) 0)
  (define (distance-to-origin)(sqrt (+(sqr (send this x))
                                      (sqr (send this y))
                                      (sqr (send this z)))))
  
  (define (distance-to pt)(sqrt (+(sqr(-(send pt x)(send this x)))
                                  (sqr(-(send pt y)(send this y)))
                                  (sqr(-(send pt z)(send this z)))))))

(define p1 (new 3D-posn% 10 20 30))

(define p1-x (send p1 x))
(define p1-y (send p1 y))
(define p1-z (send p1 z))

(check-expect p1-x 10)
(check-expect p1-y 20)
(check-expect p1-z 30)
(check-within (send p1 distance-to-origin) (sqrt 1400) 0.01)
(check-within (send p1 distance-to (new 3D-posn% 0 0 0)) (sqrt 1400) 0.01)


