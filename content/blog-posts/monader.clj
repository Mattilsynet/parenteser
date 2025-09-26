(ns blog-posts.monader)

(defn adjectivize [s]
  (map #(str % " " s)
       ["The fabulous", "The ingenious", "The completely dreamy-eyed"]))

(adjectivize "Arne")
;; => ("The fabulous Arne" "The ingenious Arne" "The completely dreamy-eyed Arne")

(mapcat adjectivize ["Arne", "Tom", "Tim", "John"])
;; => ("The fabulous Arne"
;;     "The ingenious Arne"
;;     "The completely dreamy-eyed Arne"
;;     "The fabulous Tom"
;;     "The ingenious Tom"
;;     "The completely dreamy-eyed Tom"
;;     "The fabulous Tim"
;;     "The ingenious Tim"
;;     "The completely dreamy-eyed Tim"
;;     "The fabulous John"
;;     "The ingenious John"
;;     "The completely dreamy-eyed John")

(quot x 2)

(defn safe-half [x]
  (let [guess (quot x 2)]
    (when (= x (* guess 2))
      guess)))

(safe-half 10)
;; => 5

(safe-half 9)
;; => nil

(some-> 100 safe-half safe-half)
;; => 25

(some-> 100 safe-half safe-half)

(some-> 1000 safe-half safe-half safe-half safe-half)
;; => nil

(some-> 2000 safe-half safe-half safe-half safe-half)
;; => 125

;; ----------------------------------------
;; ... men dette var en gammel idé ....

(keep safe-half (range 0 (inc 20)))
;; => (0 1 2 3 4 5 6 7 8 9 10)
