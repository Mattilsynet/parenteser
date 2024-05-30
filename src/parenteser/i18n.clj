(ns parenteser.i18n
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)
           (java.util Locale)))

(def locales
  {:nb (Locale/forLanguageTag "nb-NO")})

(def and-word
  {:nb " og "
   :en " and "})

(defn enumerate [locale xs]
  (->> (interleave
        (into (list (and-word locale) "")
              (repeat (dec (count xs)) ", "))
        xs)
       (drop 1)))

(defn format-ymd [^LocalDateTime ldt]
  (.format ldt (DateTimeFormatter/ofPattern "d. MMMM y" (:nb locales))))

(defn m1p-fn-enumerate [{:keys [locale]} _ xs & _args]
  (enumerate locale xs))
