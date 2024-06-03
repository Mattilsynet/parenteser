(ns parenteser.scenes.teaser-scenes
  (:require [parenteser.elements :as e]
            [portfolio.dumdom :refer-macros [defscene]]))

(defscene default
  (e/teaser
   {:url "/somewhere/"
    :title "Certainly an interesting teaser"
    :description "Interesting things have happenened, and this article is about to
                  tell you all about it. You won't believe what you're about to read
                  but I can assure you that it is all true."
    :published "June 4th 2024"}))

(defscene with-vcard
  (e/teaser
   {:url "/somewhere/"
    :title "Certainly an interesting teaser"
    :description "Interesting things have happenened, and this article is about to
                  tell you all about it. You won't believe what you're about to read
                  but I can assure you that it is all true."
    :published "June 4th 2024"
    :aside {:title "Homer J. Simpson"
            :body "About News and Stuff"
            :image "/portfolio-images/homer.jpg"
            :image-alt "Homer Simpson"}}))

(defscene with-kicker
  (e/teaser
   {:url "/somewhere/"
    :kicker "News around the world"
    :title "Certainly an interesting teaser"
    :description "Interesting things have happenened, and this article is about to
                  tell you all about it. You won't believe what you're about to read
                  but I can assure you that it is all true."
    :published "June 4th 2024"
    :aside {:title "Homer J. Simpson"
            :body "About News and Stuff"
            :image "/portfolio-images/homer.jpg"
            :image-alt "Homer Simpson"}}))
