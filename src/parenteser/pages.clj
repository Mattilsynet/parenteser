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

(defn layout [& forms]
  [:html
   [:head
    [:meta {:name "theme-color" :content "#f1eadf"}]]
   [:body
    forms
    (e/footer-section
     {:text [:div
             [:p "Har du innspill eller tanker? Kontakt gjerne "
              [:a {:href "mailto:magnar.sveen@mattilsynet.no"} "Magnar"]
              " eller "
              [:a {:href "mailto:christian.johansen@mattilsynet.no"} "Christian"]
              " på e-post."]
             [:p "Innleggene speiler våre personlige meninger. Vi snakker ikke for hele Mattilsynet, akkurat."]]})]])

(defn render-frontpage [page]
  (layout
   (e/header-section
    {:title "Parenteser"
     :slogan "Betraktninger fra Mat-teamets grønne enger"})
   (e/info-section
    {:title "Jøss, er du her også?"
     :text [:div
            [:p "Trivelig at du stakk innom. Vi er et lite produktteam hos Mattilsynet som jobber med mattrygghet. Her er vår tidvis tekniske blogg hvor vi deler litt av hva vi jobber med, og ting vi lærer på veien."]
            [:p "Hvorfor akkurat Parenteser? Vel, vi jobber mye i Clojure, som har rykte på seg å være belemret med unødvendige mengder parenteser. Neida, ikke bare er de nødvendige, de er aldeles smakfulle - som to fine bananer i headeren. Også hadde vi tenkt at disse bloggpostene kom litt på siden - litt i parentes, om du vil."]]})
   (e/teaser-section
    {:teasers (->> (get-blog-posts (d/entity-db page))
                   (map prepare-blog-post-teaser))})))

(defn render-blog-post [blog-post]
  (layout
   (e/header-section
    {:title "Parenteser"
     :slogan "Betraktninger fra Mat-teamets grønne enger"
     :href "/"})
   [:div.section
    [:div.content.text-content
     [:h1.h1 (:page/title blog-post)]
     (md/render-html (:blog-post/body blog-post))
     (e/vcard (get-blog-post-vcard blog-post))]]))

(defn render-404 [_page]
  (layout [:h1 "404 WAT"]))

(defn render-page [_req page]
  (if-let [f (case (:page/kind page)
               :page.kind/frontpage render-frontpage
               :page.kind/blog-post render-blog-post
               nil)]
    (f page)
    (render-404 page)))

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
