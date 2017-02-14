;; The first three lines of this file were inserted by DrRacket. They record metadata
;; about the language level of this file in a form that our tools can easily process.
#reader(lib "htdp-advanced-reader.ss" "lang")((modname network) (read-case-sensitive #t) (teachpacks ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp"))) (htdp-settings #(#t constructor repeating-decimal #t #t none #f ((lib "universe.rkt" "teachpack" "2htdp") (lib "image.rkt" "teachpack" "2htdp")) #f)))
(define LOCALHOST 10.99.42.117)

(define (runGame n)
 (big-bang INITWORLD
          (on-draw drawWorld)
          (on-tick updateWorld)
          (on-mouse processMouseEvent)
          (on-key processKeyEvent)
          (stop-when worldOver?)
          (register LOCALHOST)
          (on-receive processMessage)
          (name n)))


(define testMessage (make-message INITWORLD "test"))