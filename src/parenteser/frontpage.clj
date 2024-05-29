(ns parenteser.frontpage
  (:require [datomic-type-extensions.api :as d]
            [parenteser.blog-posts :as blog-posts]
            [parenteser.elements :as e]
            [parenteser.layout :as layout]))

(defn render-frontpage [page]
  (layout/layout
   {:title "Parenteser - Betraktninger fra Mat-teamets grønne enger"}
   (e/header-section
    {:title "Parenteser"
     :slogan "Betraktninger fra Mat-teamets grønne enger"})
   (e/info-section
    {:title "Jøss, er du her også?"
     :text [:div
            [:p "Trivelig at du stakk innom. Vi er et lite produktteam hos Mattilsynet som jobber med mattrygghet. Her er vår tidvis tekniske blogg hvor vi deler litt av hva vi jobber med, og ting vi lærer på veien."]
            [:p "Så, hvorfor akkurat " [:strong "Parenteser"] "? Vel, vi jobber mye i Clojure, som har rykte på seg å være belemret med unødvendige mengder parenteser. Men nei, ikke bare er de nødvendige, de er aldeles smakfulle - som to fine bananer i headeren. Vi tenker også at disse bloggpostene kommer litt på siden - litt i parentes, om du vil."]]})
   (e/teaser-section
    {:teasers (->> (blog-posts/get-blog-posts (d/entity-db page))
                   (map blog-posts/prepare-blog-post-teaser))})))
