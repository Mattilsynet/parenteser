(ns parenteser.pages
  (:require [datomic-type-extensions.api :as d]
            [parenteser.elements :as e]
            [powerpack.markdown :as md])
  (:import (java.time LocalDateTime)
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
  (.format ldt (DateTimeFormatter/ofPattern "d. MMMM y" no)))

(defn comma-separated [coll]
  (drop 1 (interleave (into (list " og " "")
                            (repeat (dec (count coll)) ", "))
                      coll)))

(defn prepare-tags [tags]
  (seq (map :tag/name tags)))

(defn get-blog-post-vcard [{:blog-post/keys [author tags]}]
  {:image (:person/photo author)
   :title (:person/given-name author)
   :body (when-let [tags (prepare-tags tags)]
           [:span "Om " (comma-separated tags)])})

(defn prepare-blog-post-teaser [{:blog-post/keys [description published]
                                 :page/keys [title uri]
                                 :as blog-post}]
  (cond-> {:title title
           :url uri
           :description (md/render-html description)
           :aside (get-blog-post-vcard blog-post)
           :kind :teaser-article}
    published (assoc :published (ymd published))))

(defn render-frontpage [req page]
  [:html
   [:body
    (e/header-section {:title "Parenteser"})
    (e/teaser-section
     {:teasers (->> (get-blog-posts (d/entity-db page))
                    (map prepare-blog-post-teaser))})]])

(defn render-blog-post [req blog-post]
  [:html
   [:body
    (e/header-section {:title "Parenteser"
                       :href "/"})
    [:div.section
     [:div.content.text-content
      [:h1.h1 (:page/title blog-post)]
      (md/render-html (:blog-post/body blog-post))
      (e/vcard (get-blog-post-vcard blog-post))]]]])

(defn render-404 [req page]
  [:html
   [:body
    [:h1 "404 WAT"]]])

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
