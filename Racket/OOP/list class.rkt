#lang class/2

;class: empty% :a empty list
;ref list% for method doc.
(define-class empty% (fields)
  (define (first)(error "list empty"))
  (define (second)(error "list too small"))
  (define (third)(error "list too small"))
  (define (rest)(error "list empty"))
  (define (cons e)(new list% e this))
  (define (length)0)
  (define (Empty?)true)
  (define (append l)l)
  (define (reverse)this)
  (define (map f)(new empty%))
  (define (filter f)(new empty%))
  (define (foldl f res)res)
  (define (foldr f res)res)
  (define (list-ref p)(error "List does not contain value"))
  (define (andmap f)true)
  (define (ormap f)false)
  (define (remove e)this)
  (define (memeber e)false)
  )

;class: list%: list of data (fields: value, list% of data)
(define-class list% (fields f r)
  ;first:[data] returns the first element in list (f)
  (define (first)(this . f))
  ;second:[data] returns the second element in list(the first of the rest of the data)
  (define (second)(this . rest . first))
  ;third:[data] returns the third element in list(the second of the rest of the data)
  (define (third)(this . rest . second))
  ;rest:[list%] returns the rest of the data (r)
  (define (rest)(this . r))
  ;cons:[data > list%] creates a new list object with element e in the front
  (define (cons e)(new list% e (this . rest . cons (this . first) )))
  ;length:[number] returns the length of the list
  (define (length)(+ 1 (this . rest . length)))
  ;Empty?:[boolean] returns true/false whether or not the list is empty
  (define (Empty?)false)
  ;append[list% > list%] combines list (l) to the end of the current list
  (define (append l)(new list%(this . first)(this . rest . append l)))
  ;map:[function > list%] returns a new list containing the results of the function for every element of the list
  (define (map f)(new list%(f (this . first))(this . rest . map f)))
  ;filter:[function > list%] returns a new list whose elements adhear to the functions specifications
  (define (filter f)(cond[(f (this . first))(new list% (this . first)(this . rest . filter f))]
                         [else (this . rest . filter f)]))
  ;foldl:[function result > result] similar to map but returns the result of all of the function opperations
  (define (foldl f res)(this . rest . foldl f(f (this . first)res)))
  ;foldr:[function result > result] similiar to foldr but in reverse
  (define (foldr f res)((this . reverse) .  foldl f res))
  ;reverse:[list%] returns the reverse of the current list
  (define (reverse)(this . foldl (lambda (a b)(b . cons a)) (new empty%)))
  ;list-ref:[index > value] returns the value at a certain index
  (define (list-ref p)(local[(define (helper l x)(cond[(= p x)(l . first)]
                                                      [else (helper (l . rest)(add1 x))]))]
                        (helper this 0)))
  ;andmap:[function > boolean]returns false if the result of any element in function (f) is false
  (define (andmap f)(cond[(f (this . first))(this . rest . andmap f)]
                         [else false]))
  ;ormap:[function > boolean]returns false if all of the results from the function (f) are false
  (define (ormap f)(cond[(f (this . first))true]
                        [else (this . rest . ormap f)]))
  ;remove[element > list%]removes a specific element (e) from the list
  (define (remove e)(this . filter (lambda (x)(not (equal? e x)))))
  ;member[element > boolean]returns true/false whether or not an element (e) exists in the list
  (define (member e)(this . ormap (lambda (x)(equal? e x))))
  )

(define l0 (new list% 1 (new list% 2(new list% 3 (new empty%)))))
(define l1 (new list% 4 (new empty%)))

(check-expect (l0 . length)3)
(check-expect (l0 . cons 0) (new list% 0 l0))
(check-expect (l0 . append l1) (new list% 1 (new list% 2(new list% 3 (new list% 4(new empty%))))))
(check-expect (l0 . reverse)(new list% 3 (new list% 2 (new list% 1 (new empty%)))))
(check-expect (l0 . map add1)(new list% 2 (new list% 3(new list% 4 (new empty%)))))
(check-expect (l0 . filter even?)(new list% 2 (new empty%)))
(check-expect (l0 . foldl (lambda (a b) (b . cons a))(new empty%))(new list% 3 (new list% 2 (new list% 1 (new empty%)))))
(check-expect (l0 . foldr (lambda (a b) (b . cons a))(new empty%))l0)
(check-expect (l0 . second)2)
(check-expect (l0 . third)3)
(check-expect (l0 . list-ref 2)3)
(check-expect (l0 . andmap even?)false)
(check-expect (l0 . ormap even?)true)
(check-expect (l0 . remove 1)(new list% 2(new list% 3(new empty%))))
(check-expect (l0 . member 1)true)
(check-expect (l0 . member 4)false)
    