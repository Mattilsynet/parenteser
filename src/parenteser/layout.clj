(ns parenteser.layout
  (:require [parenteser.elements :as e]))

(defn layout [{:keys [title]} & forms]
  [:html {:lang "nb"}
   [:head
    [:title title]
    [:meta {:name "theme-color" :content "#f1eadf"}]
    [:link {:href "/atom.xml"
            :rel "alternate"
            :title "Parenteser - Team Mat sin blogg"
            :type "application/atom+xml"}]]
   [:body
    forms
    (e/footer-section
     {:text [:div
             [:p "Har du innspill eller tanker? Kontakt gjerne "
              [:a {:href "mailto:magnar.sveen@mattilsynet.no"} "Magnar"]
              " eller "
              [:a {:href "mailto:christian.johansen@mattilsynet.no"} "Christian"]
              " på e-post."]
             [:p "Innleggene speiler våre personlige meninger. Vi snakker ikke for hele Mattilsynet, akkurat."]]})]])
