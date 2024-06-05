(ns parenteser.tag
  (:require [datomic-type-extensions.api :as d]
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

(defn render-tag-page [page]
  (layout/layout
   {:title [:i18n ::layout/page-title {:title (:tag/name (:page/tag page))}]}
   (layout/header {:href (router/get-frontpage-url page)})
   [:h1 (:tag/name (:page/tag page))]))

(comment

  (get-tag-pages (d/db (:datomic/conn (powerpack.dev/get-app))))


)
