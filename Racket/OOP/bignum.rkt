#lang class/0

(define-class zero%(fields)
  (define (zero?)true)
  (define (add x)x)
  (define (succ) (new nonZero% (new nmtlob% 1 (new mtlob%))))
  (define (pred) (error "zero does not have a pred")))
(define-class nonZero%(fields n)
  (define (zero?)false)
  (define (succ)(new nonZero% (send (send this n) addOne))
  (define (pred)(local [(define res (send (send this n)subOne))]
                  (cond[(send res empty?)(new zero%)]
                       [else (new nonZero% res)])))
  (define (add x)(cond[(send x zero?)this]
                      [else (send(send this add (send x pred))succ)]))))

(define test (new nonZero% 5))

(define BASE 10)

(define-class mtlob%(fields)
  (define (empty?)true)
  (define (first)(error "does not have a first"))
  (define (rest)(error "does not have a rest"))
  (define (addOne)(new nmtlob% 1 (new mtlob%)))
  (define (subOne) (error "cannot go below zero")))

(define-class nmtlob%(fields f r)
  (define (emtpy?)false)
  (define (first)(send this f))
  (define (rest)(send this r))
  (define (addOne)(cond[(< (send this f)(sub1 BASE))(new nmtlob% (add1 (send this f)(send this r)))]
                       [else (new nmtlob% 0 (send (send this r)addOne))]))
  (define (subOne)(cond[(and (= 1 (send this f))(send (send this r)empty?))(new mtlob%)]
                       [(> (send this f)0)(new nmtlob% (sub1 (send this f))(send this r))]
                       [else (new nmtlob% (sub1 BASE)(send (send this r) subOne))])))
  