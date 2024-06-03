(ns parenteser.scenes.vcard-scenes
  (:require [parenteser.elements :as e]
            [portfolio.dumdom :refer-macros [defscene]]))

(defscene default
  (e/vcard
   {:title "Homer J. Simpson"
    :body "Disgruntled nuclear plant worker"
    :image "/portfolio-images/homer.jpg"
    :image-alt "Homer Simpson"}))

(defscene with-link
  (e/vcard
   {:title "Homer J. Simpson"
    :url "/somewhere/"
    :body "Disgruntled nuclear plant worker"
    :image "/portfolio-images/homer.jpg"
    :image-alt "Homer Simpson"}))
