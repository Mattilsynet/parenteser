(ns parenteser.view-transition-page
  (:require [parenteser.layout :as layout]
            [parenteser.router :as router]
            [powerpack.markdown :as md]))

(defn render-view-transition-page [page]
  (layout/layout
   {:title [:i18n ::layout/page-title {:title (:page/title page)}]
    :class "view-transition-page"}
   (layout/header {:href (router/get-frontpage-url page)})
   [:div.section
    [:div.content
     [:div.text-content
      (list [:figure.transition-banana-teaser
             [:div.banana-2]]
            (md/render-html (:page/body page)))]]]))
