(ns parenteser.series-page
  (:require [parenteser.blog-posts :as blog-posts]
            [parenteser.elements :as e]
            [parenteser.layout :as layout]
            [powerpack.markdown :as md]))

(defn prepare-sequential-kicker [index teaser]
  (assoc teaser :kicker [:i18n ::part-n {:n (inc index)}]))

(defn render-series-page [series]
  (layout/layout
   {:title [:i18n ::title {:title (:series/name series)}]}
   (layout/header {:href "/"})
   [:div.section
    [:div.content.text-content.pbn
     [:h1.h1.mbm (:series/name series)]
     (when-let [description (:series/description series)]
       (md/render-html description))]]
   (e/teaser-section
    {:teasers (if (:series/sequential? series)
                (->> (:blog-post/_series series)
                     (sort-by :blog-post/published)
                     (map blog-posts/prepare-blog-post-teaser)
                     (map-indexed prepare-sequential-kicker))
                (->> (:blog-post/_series series)
                     (sort-by :blog-post/published)
                     (reverse)
                     (map blog-posts/prepare-blog-post-teaser)
                     (map #(dissoc % :kicker))))})))
