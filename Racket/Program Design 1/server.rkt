;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname server) (read-case-sensitive #t) (teachpacks ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp")) #f)))
(define width 800)
(define height 500)

(dfine-struct world (rockets aliens shots score timer state))
(define INITWORLD (make-world '() (addAliens 20) '() 0 0 true))

(define-struct player (name id))

(define players '())

(define (newPlayer w u)(make-bundle (map (lambda (x) (make-mail x "new player" 

(define (addPlayer p)(cons (make-player (second p)(first p))  players))

(define (removePlayer id)(filter (lambda (x) (not(equal? x id)))players))

(define-struct alien (position direction))

(define (movePlayer r dir)(make-world (cons(filter (lambda (x) (not(equal? r x)))(world-rockets world))(cond[(and(string=? dir "right")(<=(+ (posn-x r) rocketDX)(sub1 width)))(make-posn (+ (posn-x r) rocketDX)(posn-y r))]
                               [(and(string=? dir "left")(>=(- (posn-x r) rocketDX)0))            (make-posn (- (posn-x r) rocketDX)(posn-y r))]))
                                      (world-aliens world)
                                      (world-shots world)
                                      (world-timer world)
                                      (world-score world)))
                                                                    
(define (marshalWorld world)(list (cons 'rockets (map (lambda (x) (list (posn-x x)(posn-y x)))(world-rockets world)))
                                  (cons 'aliens (map (lambda (x) (list (posn-x x)(posn-y x)))(world-aliens world)))
                                  (cons 'shots (map (lambda (x) (list (posn-x x)(posn-y x)))(world-shots world)))
                                  (cons 'time (list (world-timer world)))
                                  (cons 'score (list (world-score world))))
                               
(define (processMessage world message)(cond[(string=?"left")(movePlayer (filter (lambda (x) (string=? (second message)x))(rockets world)) "left")]
                                           [(string=?"right")(movePlayer (filter (lambda (x) (string=? (second message)x))(rockets world)) "left")]
                                           [(string=?"fire")])


(define (updateWorld world)(local [
                                   (define (moveAlien a)(make-alien
                                                         (cond[(string=? (alien-direction a) "right")(make-posn (+(posn-x(alien-position a))alienDX)(posn-y(alien-position a)))]
                                                              [(string=? (alien-direction a) "left")(make-posn (-(posn-x(alien-position a))alienDX)(posn-y(alien-position a)))]
                                                              [(string=? (alien-direction a) "down")(cond[(>(posn-x(alien-position a))(/ width 2))(make-posn (-(posn-x(alien-position a))5)(+(posn-y(alien-position a))alienDY))]
                                                                                                         [else (make-posn (+(posn-x(alien-position a))5)(+(posn-y(alien-position a))alienDY))])])
                                                         (alien-direction a)))
                                   (define (moveAliens l)(map moveAlien l))
                                   (define (alienOnEdge? l)(map (lambda (a)(make-alien (alien-position a)
                                                    (cond[(and(or(string=? (alien-direction a)"right")
                                                                  (string=? (alien-direction a)"left"))
                                                              (onEdge? a))"down"]
                                                         [(string=? (alien-direction a) "down")(cond[(>(posn-x(alien-position a))(/ width 2)) "left"]
                                                                                                    [else "right"])]
                                                         [else (alien-direction a)])))l))
                                   (define (onEdge? a)(cond[(>= (+(abs(-(posn-x(alien-position a))(/ width 2)))(/(image-width alienImg)2))(/ width 2)) true]
                                                           [else false]))
                                   (define (alienHit? a s)(cond[(and (<= (abs (- (posn-x s) (posn-x (alien-position a))))(/ (image-width alienImg) 2))
                                                                     (<= (abs (- (posn-y s) (posn-y (alien-position a))))(/ (image-height alienImg) 2)))true]
                                                               [else false]))
                                   (define (aliensHit? al sl)(cond[(empty? sl)al]
                                                                  [else (filter  (lambda (a) (not(ormap (lambda (s) (alienHit? a s))sl)))al)]))
                                   
                                   (define (moveShots sl al)(cond[(empty? sl)sl]
                                                                 [else (map (lambda (s)(make-posn(posn-x s)(-(posn-y s)shotDY)))(filter (lambda (s) (shotHit? al s))sl))]))
                                   (define (shotHit? al s)(not(ormap (lambda (a) (alienHit? a s))al)))
                             
                                   (define (shotOutOfBounds? l)(cond[(empty? l)l]
                                                                    [else (filter  (lambda (s) (not(< (-(posn-y s) (image-height shot)) 0)))l)]))
                                   (define (updateTimer t)(+ t .1))
                                   
                                   (define (updateScore shots aliens score)(cond[(not(equal? aliens (aliensHit? aliens shots)))(+ score (* (- (length aliens) (length (aliensHit? aliens shots))) 50))]
                                             [(not(equal? shots (shotOutOfBounds? shots))) (- score 10)]
                                             [else score]))
                                   ]
  (make-world (world-rocket world)
                                       (moveAliens (aliensHit? (alienOnEdge?(world-aliens world))(world-shots world)))
                                       (moveShots (shotOutOfBounds?(world-shots world))(world-aliens world))
                                       (updateTimer (world-timer world))
                                       (updateScore (world-shots world)(world-aliens world)(world-score world))
                                       (world-shotTime world))))

(define (gameOver? world)(or (aliensReachedEarth? (world-aliens world))
                             (empty? (world-aliens world))))

(define (addAliens n) (build-list n (lambda (n)(make-alien(make-posn (* (+ n 2)(image-width alienImg)) (image-height alienImg))"right"))))

(define (runServer)
  (universe INITUNIVERSE
            (on-new newPlayer)
            (on-msg processMessage)
            (on-tick updateUniverse)
            (on-disconnect processDisconnect)
            (to-stirng renderExpression)))