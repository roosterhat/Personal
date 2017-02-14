;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname |sort in place|) (read-case-sensitive #t) (teachpacks ((lib "draw.rkt" "teachpack" "htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "draw.rkt" "teachpack" "htdp")) #f)))
(define (sortInPlace v)
  (local [(define (sortAux i)(cond[(zero? i)(void)]
                                  [else (begin (sortAux (sub1 i))
                                               (insert (sub1 i)))]))
          (define (insert i)(cond [(zero? i)(void)]
                                  [else (cond[(<(vector-ref v i)(vector-ref v (sub1 i)))
                                              (begin (swap i(sub1 i))
                                                     (insert (sub1 i)))]
                                             [else void])]))
          (define (swap a b)
            (local [(define temp (vector-ref v a))]
              (begin (vector-set! v a (vector-ref b))
                     (vector-set! v b temp))))]
    (sortAux (vector-length v))))