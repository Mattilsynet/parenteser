(ns parenteser.scenes.section-scenes
  (:require [parenteser.elements :as e]
            [portfolio.dumdom :refer-macros [defscene]]))

(defscene info-section
  (e/info-section
   {:title "Datomic tidbits"
    :content
    (list [:p.mbl [:a {:href "/"} "Datomic tidbits"] " is a series of blog posts
    about the weird and exciting database Datomic. Did you miss the start?
    Here's the first post in the series:"]
          (e/teaser
           {:title "An explosion of data"
            :description "Datomic is a delightful database. We kick off the new
year with a new series with tidbits from this functional database. First off
we'll have a look at the underlying data model – and explosions!"
            :published "January 3rd 2024"}))}))
