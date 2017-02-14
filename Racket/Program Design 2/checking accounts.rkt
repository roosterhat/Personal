;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname |checking accounts|) (read-case-sensitive #t) (teachpacks ((lib "image.rkt" "teachpack" "2htdp") (lib "universe.rkt" "teachpack" "2htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "image.rkt" "teachpack" "2htdp") (lib "universe.rkt" "teachpack" "2htdp")) #f)))
(define-struct account (id name amount))

(define (createCheckingAccount)
  (local [(define accounts '())
          
          (define (tasks msg)
            (cond [(eq? msg 'Deposite) deposite]
                  [(eq? msg 'Withdraw) withdraw]
                  [(eq? msg 'Balance) getBalance]
                  [(eq? msg 'Owner) getName]
                  [(eq? msg 'Add) createAccount]
                  [else (error 'task (format"Unknown command (~a)" msg))]))
          
          (define (search index)(local [(define (inspectElement k)(cond[(>= k (length accounts))false]
                                                                       [(cond[(eq? (list-ref accounts k) index)true]
                                                                             [(eq? (account-id(list-ref accounts k)) index)true]
                                                                             [(eq? (account-name(list-ref accounts k)) index)true]
                                                                             [else false])(list-ref accounts k)]
                                                                       [else (inspectElement (add1 k))]))]
                        (inspectElement 0)))
          
          (define (replace index new)(local [(define (iterateList k lst)(cond[(>= k (length accounts))lst]
                                                                             [else (cond[(eq? index (list-ref accounts k))(iterateList (add1 k)(cons new lst))]
                                                                                        [else (iterateList (add1 k)(cons lst '(list-ref accounts k)))])]))]
                                       (iterateList 0 '())))
          
          (define (deposite acc depAmount)
            (cond[(> depAmount 0)
                  (local [(define a (search acc))]
                               (cond[(boolean? a)(error 'search "Unable to find " acc)]
                                    [else (set! accounts (replace a(make-account (account-id a)(account-name a)(+(account-amount a)depAmount))))]))]
                 [else (error "Invalid amount")]))
          
          (define (withdraw acc withAmount)
            (cond[(> withAmount 0)
                      (local [(define a (search acc))]
                               (cond[(boolean? a)(error 'search "Unable to find " acc)]
                                    [else (cond[(>=(account-amount a)withAmount)(set! accounts (replace a(make-account (account-id a)(account-name a)(-(account-amount a)withAmount))))]
                                               [else (error "Insufficient funds")])]))]
                 [else (error "Invalid amount")]))
          
          (define (getBalance acc)(local [(define a (search acc))]
                                               (cond[(boolean? a)(error 'search "Unable to find " acc)]
                                                    [else (account-amount a)])))
          
          (define (getName acc)(local [(define a (search acc))]
                                               (cond[(boolean? a)(error 'search "Unable to find " acc)]
                                                    [else (account-name a)])))
          
          (define (createAccount name)(begin (set! accounts (cons (make-account (createID) name 0)accounts ))(account-id(list-ref accounts (sub1 (length accounts))))))
          
          (define (createID)(string->number(local [(define (repeat num count)(cond[(<= count 0)num]
                                                                                  [else (repeat (string-append num (number->string (random 10))) (sub1 count))]))]
                                             (repeat (number->string(+(random 9)1))8))))]
  tasks))

(define testBank (createCheckingAccount))
(define erik ((testBank 'Add) "erik"))