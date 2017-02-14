;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname pathFinding) (read-case-sensitive #t) (teachpacks ((lib "draw.rkt" "teachpack" "htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "draw.rkt" "teachpack" "htdp")) #f)))

(define graph 
  '((A (B E))
    (B (E F))
    (C (D))
    (D ())
    (E (C F))
    (F (D G))
    (G ())))

(define graph2
  '((A (B E))
    (B (E F))
    (C (B D))
    (D ())
    (E (C F))
    (F (D G))
    (G ())))
;findRoute (symbol symbol list)->list/boolean
;purpose: to determine if there exists a route between two points and if so find that route
;returns a list of points or false if no route exists
;(define (findRoute start end g)(cond[(eq? start end)(list start)]
;                              [else (local [(define possibleRoute (findAnyRoute (neighbors start g) end g))]
;                                    (cond [(boolean? possibleRoute) false]
;                                          [else (cons start possibleRoute )]))]))
;findAnyRoute (list symbol list)->list/boolean
;purpose: to test paths to see if they lead to the final destination
;returns list if path is found else false
;(define (findAnyRoute possible end g)(cond[(empty? possible)false]
;                                     [else(local[(define possibleRoute (findRoute (first possible) end g))]
;                                     (cond[(boolean? possibleRoute)(findAnyRoute (rest possible) end g)]
;                                          [else possibleRoute]))]))
;neighbors (symbole list)-> list
;purpose: returns any points that lead from the given point
;returns list 
;(define (neighbors start g)(cond[(empty? g)empty]
;                            [else(cond[(eq? (first (first g))start)(second (first g))]
;                                      [else (neighbors start (rest g))])]))
(define (find-route a b G)(local [(define (find-r a visited)
                                    (cond[(eq? a b)(list a)]
                                         [else (local [(define possible-route(find-any-route (neighbors a G)(cons a visited)))]
                                                 (cond [(boolean? possible-route) false]
                                                       [else (cons a possible-route)]))]))
                                  (define (find-any-route l visited)
                                    (cond[(empty? l)false]
                                         [(member (first l)visited)(find-any-route (rest l)visited)]
                                         [else (local [(define possible-route (find-r (first l)visited))]
                                                 (cond [(boolean? possible-route)(find-any-route (rest l)visited)]
                                                       [else possible-route]))]))
                                  (define (neighbors start g)(cond[(empty? g)empty]
                                                                  [else(cond[(eq? (first (first g))start)(second (first g))]
                                                                            [else (neighbors start (rest g))])]))]
                            (find-r a '())))