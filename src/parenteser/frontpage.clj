(ns parenteser.frontpage
  (:require [datomic-type-extensions.api :as d]
            [parenteser.blog-posts :as blog-posts]
            [parenteser.elements :as e]
            [parenteser.layout :as layout]))

(defn render-frontpage [page]
  (layout/layout
   {:title [:i18n ::page-title]}
   (layout/header)
   (e/info-section
    {:title [:i18n ::into-title]
     :text [:i18n ::info-text]})
   (e/teaser-section
    {:teasers (->> (blog-posts/get-blog-posts (d/entity-db page))
                   (map blog-posts/prepare-blog-post-teaser))})))
