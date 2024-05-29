(ns parenteser.i18n
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)
           (java.util Locale)))

(def locales
  {:nb (Locale/forLanguageTag "nb-NO")})

(def and-word
  {:nb " og "})

(defn enumerate [xs]
  (->> (interleave
        (into (list (and-word :nb) "")
              (repeat (dec (count xs)) ", "))
        xs)
       (drop 1)))

(defn format-ymd [^LocalDateTime ldt]
  (.format ldt (DateTimeFormatter/ofPattern "d. MMMM y" (:nb locales))))
