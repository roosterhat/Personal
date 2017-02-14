#lang class/2

(define-class empty% (fields)
  (define (left)this)
  (define (right)this)
  (define (element)this)
  (define (getLowest)this)
  (define (getHighest)this)
  (define (insert e)(new tree% e (new empty%)(new empty%)))
  (define (delete e)this)
  (define (search e)false)
  (define (member? e)false)
  (define (empty?)true)
  (define (in-order)empty)
  (define (pre-order) empty)
  (define (post-order) empty)
  
  )

(define-class tree% (fields elem lt rt)
  (define (left)(this . lt))
  (define (right)(this . rt))
  (define (element)(this . elem))
  (define (getLowest)(cond[(not(this . left . empty?))(this . left . getLowest)]
                          [else (this . element)]))
  (define (getHighest)(cond[(not (this . right . empty?))(this . right . getHighest)]
                           [else (this . element)]))
  (define (insert e)(cond[(>= e (this . element))(new tree% (this . element)(this . left)(this . right . insert e))]
                         [else (new tree% (this . element)(this . left . insert e)(this . right))]))
  (define (delete e)(local [(define target (this . search e))
                            ;new element
                            (define l (and(target . right . empty?)(not (target . left . empty?))))
                            (define n (cond[l(target . left . element)]
                                           [(and(target . right . empty?)(target . left . empty?))false]
                                           [else (target . right . getLowest)]))
                            (define (helper node)
                              (cond[(equal? e (node . element))
                                    (cond[l(node . search n)]
                                         [(boolean? n)(new empty%)]
                                         [else(new tree% n (node . left)(node . right . delete n))])]
                                   [(equal? n (node . element))(helper(node . right))]
                                   [else (new tree% (node . element)(helper(node . left))(helper(node . right)))]))]
                           (helper this)))
  (define (search e)(cond[(equal? e (this . element))this]
                         [else (cond[(this . left . member? e)(this . left . search e)]
                                    [(this . right . member? e)(this . right . search e)]
                                    [else (new empty%)])]))
  (define (member? e)(cond[(equal? e (this . element))true]
                         [else (or (this . left . member? e)(this . right . member? e))]))
  (define (in-order)(append (this . left . in-order) (list (this . element))(this . right . in-order)))
  (define (pre-order)(append (list (this . element))(this . left . pre-order)(this . right . pre-order)))
  (define (post-order)(append (this . left . post-order)(this . right . post-order)(list (this . element))))
  (define (empty?) false)

  )

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

