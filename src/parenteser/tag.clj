(ns parenteser.tag
  (:require [datomic-type-extensions.api :as d]
            [parenteser.blog-posts :as blog-posts]
            [parenteser.elements :as e]
            [parenteser.layout :as layout]
            [parenteser.router :as router]))

(defn get-tag-pages [db]
  (for [[tag page locale]
        (d/q '[:find ?t ?p ?locale
               :where
               [?p :blog-post/tags ?t]
               [?t :tag/id ?tag]
               [?p :page/locale ?locale]]
             db)]
    {:page/uri (router/get-tag-url (d/entity db page))
     :page/locale locale
     :page/tag tag
     :page/kind :page.kind/tag}))

(defn get-blog-posts [db tag-id locale]
  (->> (blog-posts/get-blog-posts db #{locale})
       (filter #(contains? (set (map :tag/id (:blog-post/tags %))) tag-id))))

(defn render-tag-page [page]
  (layout/layout
   {:title [:i18n ::layout/page-title {:title (:tag/name (:page/tag page))}]}
   (layout/header {:href (router/get-frontpage-url page)})
   [:div.section
    [:div.content
     [:h1.h1 [:i18n ::title (:page/tag page)]]]]
   (e/teaser-section
    {:teasers (->> (get-blog-posts (d/entity-db page) (:tag/id (:page/tag page)) (:page/locale page))
                   (map blog-posts/prepare-blog-post-teaser))})))
