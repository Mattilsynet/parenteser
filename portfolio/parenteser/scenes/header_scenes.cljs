(ns parenteser.scenes.header-scenes
  (:require [parenteser.elements :as e]
            [portfolio.dumdom :refer-macros [defscene]]))

(defscene default
  (e/header-section
   {:title "Parens"
    :slogan "Software reflections from Team Mat"}))

(defscene with-link
  (e/header-section
   {:title "Parens"
    :slogan "Software reflections from Team Mat"
    :href "/"}))

(defscene different-text
  (e/header-section
   {:title "Simpsons studies"
    :slogan "Animation reflections from clowns"
    :href "/"}))
