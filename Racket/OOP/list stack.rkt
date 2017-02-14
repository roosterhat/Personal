#lang class/0



(define-class emptyStack (fields)
  (define (stackEmpty?) true)
  (define (top) (error "stack is empty"))
  (define (bottom) (error "stack is empty"))
  (define (push i)(new listStack (list i)))
  (define (reverse) this))

(define-class listStack (fields stack)
  (define (stackEmpty?) false)
  (define (top) (first (send this stack)))
  (define (bottom) (cond[(empty? (rest(send this stack)))(new emptyStack)]
                        [else (new listStack (rest (send this stack)))]))
  (define (push i)(new listStack (cons i (send this stack))))
  (define (reverse)(local [(define (createStack newS oldS)(cond[(send oldS stackEmpty?) newS]
                                                               [else (createStack (send newS push (send oldS top)) (send oldS bottom))]))]
                     (createStack (new emptyStack) this))))

(define s (new listStack (list 1 2 3 4 5)))
