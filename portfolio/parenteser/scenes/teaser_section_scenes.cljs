(ns parenteser.scenes.teaser-section-scenes
  (:require [parenteser.elements :as e]
            [portfolio.dumdom :refer-macros [defscene]]))

(defscene default
  (e/teaser-section
   {:teasers
    [{:url "/somewhere/"
      :title "Certainly an interesting teaser"
      :description "Interesting things have happenened, and this article is about to
                  tell you all about it. You won't believe what you're about to read
                  but I can assure you that it is all true."
      :footer "June 4th 2024"
      :aside {:title "Homer J. Simpson"
              :body "About News and Stuff"
              :image "/portfolio-images/homer.jpg"
              :image-alt "Homer Simpson"}}
     {:url "/somewhere/"
      :title "Clowning is on the rise"
      :description "Clowns have been repressed much too long, and now we're on
      the rise world-wide. Read more about what clowning is going on in your
      area."
      :footer "May 30th 2024"
      :aside {:title "Herschel Shmoikel Krustofsky"
              :body "About News and Stuff"
              :image "/portfolio-images/krusty.jpg"
              :image-alt "Herschel Shmoikel Krustofsky"}}]}))
