(ns parenteser.pages
  (:require [datomic-type-extensions.api :as d]
            [parenteser.blog-post-page :as blog-post-page]
            [parenteser.blog-posts :as blog-posts]
            [parenteser.frontpage :as frontpage]
            [parenteser.layout :as layout]
            [parenteser.rss :as rss]
            [parenteser.series-page :as series-page]))

(defn render-404 [_page]
  (layout/layout
   {:title [:i18n ::not-found-title]}
   [:h1 [:i18n ::not-found-heading]]))

(defn render-page [req page]
  (if-let [f (case (:page/kind page)
               :page.kind/frontpage frontpage/render-frontpage
               :page.kind/blog-post blog-post-page/render-blog-post
               :page.kind/series series-page/render-series-page
               :page.kind/rss-feed (fn [_] (rss/blog-post-feed (:app/db req)))
               nil)]
    (f page)
    (render-404 page)))

(comment

  (def system (:powerpack/app integrant.repl.state/system))

  (->> (d/db (:datomic/conn system))
       blog-posts/get-blog-posts
       (map #(into {:db/id (:db/id %)} %)))

  (into {}
        (->> [:page/uri "/nats-import-eksport/"]
             (d/entity (d/db (:datomic/conn system)))
             :blog-post/author))

  )
