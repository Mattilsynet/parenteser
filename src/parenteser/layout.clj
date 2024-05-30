(ns parenteser.layout
  (:require [parenteser.elements :as e]))

(defn header [& [{:keys [href]}]]
  (e/header-section
   (cond-> {:title [:i18n ::header-title]
            :slogan [:i18n ::header-slogan]}
     href (assoc :href href))))

(defn layout [{:keys [title]} & forms]
  [:html {:lang "nb"}
   [:head
    [:title title]
    [:meta {:name "theme-color" :content "#f1eadf"}]
    [:link {:href "/atom.xml"
            :rel "alternate"
            :title [:i18n ::atom-feed-title]
            :type "application/atom+xml"}]]
   [:body
    forms
    (e/footer-section
     {:text [:i18n ::footer-hiccup]})]])
