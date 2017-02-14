#lang class/2

(define-class shape% (fields x y)
)

(define-class quadrilateral% (super shape%)(fields width)
)

(define-class square% (super quadrilateral%)(fields)
  (define (area)(sqr (this . width)))
  (define (perimeter)(* 4(this . width)))
)

(define-class rectangle% (super quadrilateral%)(fields height)
  (define (area)(* (this . width)(this . height)))
  (define (perimeter)(+(* 2(this . width))(* 2(this . height))))
)

(define-class rhombus% (super rectangle%)(fields)
)

(define-class triangle% (super shape%)(fields side1)

)

(define-class equilateral% (super triangle%)(fields)
  (define (area)(local [(define p(/(this . perimeter)2))]
                  (sqrt (*(*(* p(- p (this . side1)))(- p (this . side1)))(- p (this . side1))))))
  (define (perimeter)(* 3 (this . side1)))
)

(define-class isosceles% (super triangle%)(fields side2)
  (define (area)(local [(define p(/(this . perimeter)2))]
                  (sqrt (*(*(* p(- p (this . side1)))(- p (this . side2)))(- p (this . side2))))))
  (define (perimeter)(+ (+ (this . side1) (this . side2))(this . side2)))
  )

(define-class scalene% (super triangle%)(fields side2 side3)
  (define (area)(local [(define p(/(this . perimeter)2))]
                  (sqrt (*(*(* p(- p (this . side1)))(- p (this . side2)))(- p (this . side3))))))
  (define (perimeter)(+ (+ (this . side1) (this . side2))(this . side3)))
  )


(define-class circle% (super shape%)(fields radius)
  (define (area)(* pi (sqr (this . radius))))
  (define (perimeter)(*(* 2 pi)(this . radius)))
  (define (diameter)(* 2 (this . radius)))
  (define (sectorArea angle)(*(*(/ angle 360)(sqr (this . radius)))pi))
  )

(define square (new square% 10 0 0))
(define rect (new rectangle% 10 20 0 0))
(define rhombus (new rhombus% 10 20 0 0))
(define Etri (new equilateral% 10 0 0))
(define Itri (new isosceles% 20 10 0 0))
(define Stri (new scalene%  40 20 30 0 0))
(define circle (new circle% 10 0 0))


(check-expect (square . perimeter) 40)
(check-expect (square . area)100)
(check-expect (rect . perimeter) 60)
(check-expect (rect . area)200)
(check-expect (rhombus . perimeter) 60)
(check-expect (rhombus . area)200)
(check-expect (Etri . perimeter) 30)
(check-within (Etri . area) 43.3013 0.1)
(check-expect (Itri . perimeter) 50)
(check-within (Itri . area) 96.8246 0.1)
(check-expect (Stri . perimeter) 90)
(check-within (Stri . area)290.4738 0.1)
(check-within (circle . perimeter) 62.83 0.1)
(check-within (circle . area) 314.16 0.1)
