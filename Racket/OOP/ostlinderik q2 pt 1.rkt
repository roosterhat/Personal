#lang class/0

(define-class emptyStack (fields)
  (define (stackEmpty?) true)
  (define (top) (error "Stack is empty"))
  (define (bottom) (error "stack is empty"))
  (define (push i)(new stack i (new emptyStack)))
  (define (reverse) this))

(define-class stack (fields f r)
  (define (stackEmpty?) false)
  (define (top) (send this f))
  (define (bottom) (send this r))
  (define (push i) (new stack i (send (send this r) push (send this f))))
  (define (reverse)(local [(define (createStack newS oldS)(cond[(send oldS stackEmpty?)newS]
                                                               [else (createStack (send newS push (send oldS top))(send oldS bottom))]))]
                           (createStack (new emptyStack)this))))
(define s (new emptyStack))
(define s1(send s push 1))
(define s2(send s1 push 2))
(define s3(send s2 push 3))
(define s4(send s3 push 4))
(define s5(send s4 push 5))

