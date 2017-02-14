;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname |homework 4-7-16|) (read-case-sensitive #t) (teachpacks ((lib "draw.rkt" "teachpack" "htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "draw.rkt" "teachpack" "htdp")) #f)))
(define-struct author( name age books))
(define-struct book( title price isbn authors))
(define authors '())


(define (addAuthor n d)
  (local[(define newAuthor (make-author n d '()))]
    (begin (set! authors (cons newAuthor authors))
           newAuthor)))

(define (find-author a)
  (local[(define (search i)(cond[(= i (length authors))false]
                                [else (cond[(equal? (author-name (list-ref authors i)) a)(list-ref authors i)]
                                           [else (search (add1 i))])]))]
    (search 0)))

(define (addBook t p is a)
  (for-each
   (lambda (au)(local [(define A (local[(define ex (find-author au))]
                       (cond [(boolean? ex)(addAuthor au -1)]
                             [else ex])))
           (define newBook (make-book t p is a))]
     (begin (set-author-books! A (cons newBook(author-books A))) newBook))
                                  )a))

(define (findBooksBy name)(local[(define (gather i)(cond[(= (length (author-books (find-author name))) i)'()]
                                                          [else (cons  (book-title(list-ref(author-books (find-author name))i))(gather (add1 i)))]))]
                            (gather 0)))

(define (findAuthorOf title)(for-each (lambda (a)
                                        (for-each (lambda (b)
                                                    (cond[(equal? (book-title b) title )(printf" ~a"(author-name a))]
                                                                            [else false]))(author-books a)))authors))

(addAuthor "this guy" 18)
(addAuthor "other guy" 1337)
(addBook "yes" 100 12349871325 (list "this guy" "other guy"))
(addBook "no" 0 12341234 (list "other guy"))
