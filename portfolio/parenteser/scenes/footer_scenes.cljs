(ns parenteser.scenes.footer-scenes
  (:require [parenteser.elements :as e]
            [portfolio.dumdom :refer-macros [defscene]]))

(defscene default
  (e/footer-section
   {:text [:span "We're a couple of developers writing about software. Feel free to "
           [:a {:href "/"} "contact us"] " if you have feedback."]}))
