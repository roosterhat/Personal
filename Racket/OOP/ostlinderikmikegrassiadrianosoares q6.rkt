#lang class/2
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;Interface;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; a binary tree is either empty% or a tree%
;;;; functions on a binary tree
;left: tree -> tree
;  purpose: returns the left side of the binary tree
;right: tree -> tree
;  purpose: returns the right side of the binary tree
;element: tree -> number
;  purpose: returns the top element of the binary tree
;getLowest: tree -> number
;  purpose: returns the lowest # value in the binary tree
;getHighest: tree -> number
;  purpose: returns the highest # value in the binary tree
;insert: tree # -> tree
;  purpose: adds the new # value into the current binary tree
;delete: tree # -> tree
;  purpose: remove the given # value from the given tree
;search: tree # -> tree
;  purpose: determine whether the given number is in the tree, then return the sub-tree that it resides in
;member?: tree # -> boolean
;  purpose: returns true if the given number is a member of the given tree, false if not
;empty?: tree -> boolean
;  purpose: determines whether the given tree is empty
;in-order: tree -> list
;  purpose: returns the elements of a tree in list form in ascending order
;pre-order: tree -> list
;  purpose: returns the elements of a tree in list form in the order: element, left tree (descending), right tree (ascending)
;post-order: tree -> list
;  purpose: returns the elements of a tree in list form in the order: left tree (ascending), right tree (descending), element


;an empty% tree is:
(define-class empty% (fields)
  ;left: tree -> tree
  ;returns the left side of the binary tree
  ;the left of an empty tree is empty
  (define (left)this)
  
  ;right: tree -> tree
  ;returns the right side of the binary tree
  ;the right of an empty tree is empty
  (define (right)this)
  
  ;element: tree -> number
  ;returns the top element of the binary tree
  ;an empty tree has no element
  (define (element)(error "there is no element in an empty tree"))
  
  ;getLowest: tree -> number
  ;returns the lowest # value in the binary tree
  ;there is no lowest in an empty tree
  (define (getLowest)(error "there is no lowest of an empty tree"))
  
  ;getHighest: tree -> number
  ;returns the highest # value in the binary tree
  ;there is no highest in an empty tree
  (define (getHighest)(error "there is no highest of an empty tree"))
  
  ;insert: tree # -> tree
  ;adds the new # value into the current binary tree
  ;inserting a number into an empty tree makes a tree% with one number (element)
  (define (insert e)(new tree% e (new empty%)(new empty%)))
  
  ;delete: tree # -> tree
  ;remove the given # value from the given tree
  ;deleting a number from an empty tree does nothing: return the empty tree
  (define (delete e)this)
  
  ;search: tree # -> tree
  ;determine whether the given number is in the tree, then return the sub-tree that it resides in
  ;there are no numbers in an empty tree and therefore no tree to return
  (define (search e)(error "the number you entered is not a member of the tree and therefore has no subtree"))
  
  ;member?: tree # -> boolean
  ;returns true if the given number is a member of the given tree, false if not
  ;an empty tree has no members
  (define (member? e)false)
  
  ;empty?: tree -> boolean
  ;determines whether the given tree is empty
  ;an empty tree is empty
  (define (empty?)true)
  
  ;in-order: tree -> list
  ;returns the elements of a tree in list form in ascending order
  ;an ordered list of an empty tree is an empty list
  (define (in-order)empty)
  
  ;pre-order: tree -> list
  ;returns the elements of a tree in list form in the order: element, left tree (descending), right tree (ascending)
  ;a pre-ordered list of an empty tree is an empty list
  (define (pre-order)empty)
  
  ;post-order: tree -> list
  ;returns the elements of a tree in list form in the order: left tree (ascending), right tree (descending), element
  ;a post-ordered list of an empty tree is an empty list
  (define (post-order)empty)
  
  )

;a nonempty tree% is:
;;;functions on a tree%:
;this . elem: tree -> number
;  purpose: returns the first element of the tree
;this . lt: tree -> tree
;  purpose: returns the left branch of the tree
;this . rt: tree -> tree
;  purpose: returns the right branch of the tree
(define-class tree% (fields elem lt rt)
  
  ;left: tree -> tree
  ;returns the left side of the binary tree
  (define (left)(this . lt))
  
  ;right: tree -> tree
  ;right returns the right branch of the tree
  (define (right)(this . rt))
  
  ;element: tree -> number
  ;element returns the first element of the tree
  (define (element)(this . elem))
  
  ;getLowest: tree -> number
  ;getLowest searches through the tree to find the lowest number value in the tree
  (define (getLowest)(cond[(not(this . left . empty?))(this . left . getLowest)]
                          [else (this . element)]))
  
  ;getHighest: tree -> number
  ;getHighest searches through the tree to find the highest number value in the tree
  (define (getHighest)(cond[(not (this . right . empty?))(this . right . getHighest)]
                           [else (this . element)]))
  
  ;insert: tree # -> tree
  ;insert makes a new tree with the given number placed in the correct spot of the given tree
  (define (insert e)(cond[(>= e (this . element))(new tree% (this . element)(this . left)(this . right . insert e))]
                         [else (new tree% (this . element)(this . left . insert e)(this . right))]))
  
  ;delete: tree # -> tree
  ;delete makes a new tree, removing the given number from the given tree, replacing it with the proper number from the given tree
  (define (delete e)(local [(define target (this . search e))
                            ;new element
                            (define l (and(target . right . empty?)(not (target . left . empty?))))
                            (define n (cond[l(target . left . element)]
                                           [(and(target . right . empty?)(target . left . empty?))false]
                                           [else (target . right . getLowest)]))]
                           (cond[(equal? e (this . element))(cond[l(this . search n)]
                                                                 [(boolean? n)(new empty%)]
                                                                 [else(new tree% n (this . left)(this . right . delete n))])]
                                [(equal? n (this . element))(this . right . delete e)]
                                [else (new tree% (this . element)(this . left . delete e)(this . right . delete e))])))
  
  ;search: tree # -> tree
  ;search checks to see if the given number is a member of the given tree, if so it returns the sub-tree that the given number belongs to
  (define (search e)(cond[(equal? e (this . element))this]
                         [else (cond[(this . left . member? e)(this . left . search e)]
                                    [(this . right . member? e)(this . right . search e)]
                                    [else (new empty%)])]))
  
  ;member?: tree # -> boolean
  ;member checks to see if the given number is a member of the given tree, if so it returns true
  (define (member? e)(cond[(equal? e (this . element))true]
                         [else (or (this . left . member? e)(this . right . member? e))]))
  
  ;in-order: tree -> list
  ;in-order creates a list of the ordered values of a tree by traversing the tree from right to left, appending the values as it goes and placing the elemnent in the middle of the two sorted trees
  (define (in-order)(append (this . left . in-order) (list (this . element))(this . right . in-order)))
  
  ;pre-order: tree -> list
  ;pre-order creates a list of the values of a tree, first ordering the left tree in reverse, then the right tree, then appending it to the element
  (define (pre-order)(append (list (this . element))(this . left . pre-order)(this . right . pre-order)))
  
  ;post-order: tree -> list
  ;post-order creates a list of the values of a tree, ordering the left tree, then the right in reverse, then appending the element to those
  (define (post-order)(append (this . left . post-order)(this . right . post-order)(list (this . element))))
  
  ;empty?: tree -> boolean
  ;a nonempty tree% is not empty
  (define (empty?) false)
 )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;TEST TREES;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(define test (new empty%))
(define stage1 (test . insert 50))
(define stage2 (stage1 . insert 43))
(define stage3 (stage2 . insert 42))
(define stage4 (stage3 . insert 66))
(define stage5 (stage4 . insert 75))
(define stage6 (stage5 . insert 89))
(define stage7 (stage6 . insert 12))
(define stage8 (stage7 . insert 54))
(define stage9 (stage8 . insert 68))
(define stage10(stage9 . insert 67))
(define stage11(stage10 . insert 69))
(define stage12(stage11 . insert 93))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;TESTS;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;right/left
(check-expect (test . left) (new empty%))
(check-expect (stage12 . left)(new tree% 43 (new tree% 42 (new tree% 12 (new empty%) (new empty%))
                              (new empty%)) (new empty%)))
(check-expect (test . right) (new empty%))
(check-expect (stage12 . right)(new tree% 66 (new tree% 54 (new empty%) (new empty%)) (new tree% 75 (new tree% 68 (new tree% 67 (new empty%) (new empty%))
                               (new tree% 69 (new empty%) (new empty%))) (new tree% 89 (new empty%) (new tree% 93 (new empty%) (new empty%))))))

;(check-expect (test . element) (error "there is no element in an empty tree")) --WORKS:ERROR THROWER
(check-expect (stage12 . element) 50)

;getHighest/Lowest
;(check-expect (test . getLowest) (error "there is no lowest of an empty tree")) --WORKS:ERROR THROWER
(check-expect (stage12 . getLowest) 12)
;(check-expect (test . getHighest) (error "there is no highest of an empty tree")) --WORKS:ERROR THROWER
(check-expect (stage12 . getHighest) 93)

;insert
(check-expect (test . insert 40) (new tree% 40 (new empty%)(new empty%)))
(check-expect (stage3 . insert 55) (new tree% 50 (new tree% 43 (new tree% 42 (new empty%) (new empty%)) (new empty%)) (new tree% 55 (new empty%) (new empty%))))

;search
;(check-expect (test . search 44) (error "the number you entered is not a member of the tree and therefore has no subtree")) --WORKS:ERROR THROWER
(check-expect (stage12 . search 43)(new tree% 43 (new tree% 42 (new tree% 12 (new empty%) (new empty%))(new empty%)) (new empty%)))
(check-expect (stage12 . search 44) (new empty%))

;member?
(check-expect (test . member? 44) false)
(check-expect (stage12 . member? 42) true)
(check-expect (stage12 . member? 44) false)

;delete
(check-expect (test . delete 44) (new empty%))
(check-expect (stage12 . delete 93) stage11)

;ordering
(check-expect (test . in-order) (list ))
(check-expect (stage12 . in-order) (list 12 42 43 50 54 66 67 68 69 75 89 93))
(check-expect (test . pre-order) (list ))
(check-expect (stage12 . pre-order) (list 50 43 42 12 66 54 75 68 67 69 89 93))
(check-expect (test . post-order)(list ))
(check-expect (stage12 . post-order) (list 12 42 43 54 67 69 68 93 89 75 66 50))

;empty
(check-expect (test . empty?) true)
(check-expect (stage12 . empty?) false)