(ns parenteser.pages
  (:require [datomic-type-extensions.api :as d]
            [parenteser.blog-post-page :as blog-post-page]
            [parenteser.blog-posts :as blog-posts]
            [parenteser.elements :as e]
            [parenteser.frontpage :as frontpage]
            [parenteser.layout :as layout]
            [parenteser.rss :as rss]
            [powerpack.markdown :as md]))

(defn render-404 [_page]
  (layout/layout {:title "Fant ikke siden!"} [:h1 "404 WAT"]))

(defn prepare-sequential-kicker [index teaser]
  (assoc teaser :kicker (str "Del " (inc index) ":")))

(defn render-series-page [series]
  (layout/layout
   {:title (str (:series/name series) " - Parenteser")}
   (e/header-section
    {:title "Parenteser"
     :slogan "Betraktninger fra Mat-teamets grønne enger"
     :href "/"})
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

(defn render-page [req page]
  (if-let [f (case (:page/kind page)
               :page.kind/frontpage frontpage/render-frontpage
               :page.kind/blog-post blog-post-page/render-blog-post
               :page.kind/series render-series-page
               :page.kind/rss-feed (fn [_] (rss/blog-post-feed (:app/db req)))
               nil)]
    (f page)
    (render-404 page)))

(comment

  (def system integrant.repl.state/system)

  (->> (d/db (:datomic/conn system))
       blog-posts/get-blog-posts
       (map #(into {:db/id (:db/id %)} %)))

  (into {}
        (->> [:page/uri "/blog/byggeklosser-for-sok/"]
             (d/entity (d/db (:datomic/conn system)))

             #_:blog-post/author
             #_:person/photo))

  )
