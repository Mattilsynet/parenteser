(ns parenteser.pages
  (:require [datomic-type-extensions.api :as d]
            [parenteser.elements :as e]
            [powerpack.html :as html]
            [powerpack.markdown :as md])
  (:import (java.time LocalDateTime ZoneId)
           (java.time.format DateTimeFormatter)
           (java.util Locale)))

(defn get-blog-posts [db]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :page/uri]
              [?e :page/kind :page.kind/blog-post]]
            db)
       (map #(d/entity db %))
       (sort-by :blog-post/published)
       reverse))

(def no (Locale/forLanguageTag "no"))

(defn ymd [^LocalDateTime ldt]
  (.format ldt (DateTimeFormatter/ofPattern "d. MMMM yyy" no)))

(defn ymd [^LocalDateTime ldt]
  (.format ldt (DateTimeFormatter/ofPattern "d. MMMM y" no)))

(defn md [^LocalDateTime ldt]
  (.format ldt (DateTimeFormatter/ofPattern "d. MMMM")))

(defn prepare-blog-post-teaser [{:blog-post/keys [description published]
                                 :page/keys [title uri] :as bp}]
  (cond-> {:title title
           :url uri
           :description (md/to-html description)
           :kind :teaser-article}
    published (assoc :published (ymd published))))

(defn render-frontpage [req page]
  (html/render-hiccup
   req
   page
   (e/teaser-section
    {:title "Parenteser"
     :description "Beretninger fra Mat-teamets grønne enger"
     :teasers (->> (get-blog-posts (d/entity-db page))
                   (map prepare-blog-post-teaser))})))

(defn render-blog-post [req blog-post]
  (html/render-hiccup
   req
   blog-post
   [:div.section
    [:div.content.text-content
     [:h1.h1 (:page/title blog-post)]
     (md/to-html (:blog-post/body blog-post))
     (when-let [photo (-> blog-post :blog-post/author :person/photo)]
       [:img {:src (str "/vcard-small" photo)}])]]))

(defn render-404 [req page]
  (html/render-hiccup
   req
   page
   [:h1 "404 WAT"]))

(defn render-page [req page]
  (if-let [f (case (:page/kind page)
               :page.kind/frontpage render-frontpage
               :page.kind/blog-post render-blog-post
               nil)]
    (f req page)
    (render-404 req page)))

(comment

  (def system integrant.repl.state/system)

  (->> (d/db (:datomic/conn system))
       get-blog-posts
       (map #(into {:db/id (:db/id %)} %)))

  (into {}
        (->> [:page/uri "/blog/byggeklosser-for-sok/"]
             (d/entity (d/db (:datomic/conn system)))

             #_:blog-post/author
             #_:person/photo))

  )
