;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname binarysearch) (read-case-sensitive #t) (teachpacks ((lib "draw.rkt" "teachpack" "htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "draw.rkt" "teachpack" "htdp")) #f)))
(define-struct person (id name))
(define (binarySearch list element condition)(local[
                      (define (searchList i h l)(cond[(or (< i 0) (> i (sub1(length list)))(and (= (condition list i element) 0)(not (equal? (list-ref list i)element))))-1]
                                                   [(> (condition list i element) 0)(searchList (floor(- i (/ (- i l) 2)))i l)]
                                                   [(< (condition list i element) 0)(searchList (floor(+ i (/ (- h i) 2)))h i)]
                                                   [(and (= (condition list i element)0)(equal? (list-ref list i)element))i]))
                      ](searchList (floor(/(sub1(length list))2)) (sub1(length list))0)))

(define (c list index search)(- (list-ref list index) search))

(define (p list index search)(cond[(>(list-ref list index)(person-id search))1]
                                  [(<(list-ref list index)(person-id search))-1]
                                  [(=(list-ref list index)(person-id search))0]))
