(ns parenteser.pages
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [dev.onionpancakes.chassis.core :as chassis]
            [parenteser.blog-posts :as blog-posts]
            [parenteser.elements :as e]
            [parenteser.layout :as layout]
            [parenteser.rss :as rss]
            [powerpack.markdown :as md])
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)
           (java.util Locale)))

(def no (Locale/forLanguageTag "no"))

(defn ymd [^LocalDateTime ldt]
  (.format ldt (DateTimeFormatter/ofPattern "d. MMMM y" no)))

(defn comma-separated [coll]
  (drop 1 (interleave (into (list " og " "")
                            (repeat (dec (count coll)) ", "))
                      coll)))

(defn prepare-tags [tags]
  (seq (map :tag/name tags)))

(defn get-blog-post-vcard [{:blog-post/keys [author tags vcard-photo]}]
  {:image (or vcard-photo
              (:person/photo author))
   :image-alt (:person/given-name author)
   :title (:person/given-name author)
   :body (when-let [tags (prepare-tags tags)]
           [:span "Om " (comma-separated tags)])})

(defn prepare-blog-post-teaser [{:blog-post/keys [description published series]
                                 :page/keys [title uri]
                                 :as blog-post}]
  (cond-> {:title title
           :kicker (some-> (:series/name series) (str ": "))
           :url uri
           :description (md/render-html description)
           :aside (get-blog-post-vcard blog-post)
           :kind :teaser-article}
    published (assoc :published (ymd published))))

(defn render-frontpage [page]
  (layout/layout
   {:title "Parenteser - Betraktninger fra Mat-teamets grønne enger"}
   (e/header-section
    {:title "Parenteser"
     :slogan "Betraktninger fra Mat-teamets grønne enger"})
   (e/info-section
    {:title "Jøss, er du her også?"
     :text [:div
            [:p "Trivelig at du stakk innom. Vi er et lite produktteam hos Mattilsynet som jobber med mattrygghet. Her er vår tidvis tekniske blogg hvor vi deler litt av hva vi jobber med, og ting vi lærer på veien."]
            [:p "Så, hvorfor akkurat " [:strong "Parenteser"] "? Vel, vi jobber mye i Clojure, som har rykte på seg å være belemret med unødvendige mengder parenteser. Men nei, ikke bare er de nødvendige, de er aldeles smakfulle - som to fine bananer i headeren. Vi tenker også at disse bloggpostene kommer litt på siden - litt i parentes, om du vil."]]})
   (e/teaser-section
    {:teasers (->> (blog-posts/get-blog-posts (d/entity-db page))
                   (map prepare-blog-post-teaser))})))

(defn get-series-blurb [series]
  (-> (:series/blurb series)
      (str/replace #"\[(.+)\]" (fn [[_ content]]
                                 (str "<a href='" (:page/uri series) "'>" content "</a>")))
      chassis/raw))

(defn get-relevant-post [blog-post series]
  (let [other-posts (->> (:blog-post/_series series)
                         (remove #{blog-post})
                         (sort-by :blog-post/published))]
    (if (:series/sequential? series)
      (if-let [next-post (first (drop-while #(.isBefore (:blog-post/published %)
                                                        (:blog-post/published blog-post))
                                            other-posts))]
        {:prelude "Lyst til å lese videre? Her er det neste innlegget i serien:"
         :post next-post}
        (when (seq other-posts)
          {:prelude "Gikk du glipp av starten? Her er det første innlegget i serien:"
           :post (first other-posts)}))
      (when-let [newest-post (last other-posts)]
        {:prelude "Her er det siste innlegget i serien:"
         :post newest-post}))))

(defn render-series-conclusion [blog-post series]
  (let [blurb (get-series-blurb series)
        {:keys [prelude post]} (get-relevant-post blog-post series)]
    (when post
      [:div.section.slim
       [:div.content.info-section
        [:div.section-content.text-content
         (list
          [:p.mbl blurb " " prelude]
          (e/teaser (-> (prepare-blog-post-teaser post)
                        (dissoc :kicker))))]]])))

(defn render-blog-post [blog-post]
  (let [series (:blog-post/series blog-post)
        published (:blog-post/published blog-post)]
    (layout/layout
     {:title (str (:page/title blog-post) " - Parenteser")}
     (e/header-section
      {:title "Parenteser"
       :slogan "Betraktninger fra Mat-teamets grønne enger"
       :href "/"})
     [:div.section
      [:div.content
       [:div.text-content
        [:h1.h1
         (when series
           [:div.h4.mbxs [:a {:href (:page/uri series)} (:series/name series)] ": "])
         [:span (:page/title blog-post)]]
        (md/render-html (:blog-post/body blog-post))]
       (-> (get-blog-post-vcard blog-post)
           (assoc :class "mtxxl")
           (update :body (fn [b] [:div b
                                  [:div [:time.byline.text-s {:datetime published}
                                         (ymd published)]]]))
           e/vcard)]]
     (when series
       (render-series-conclusion blog-post series)))))

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
                     (map prepare-blog-post-teaser)
                     (map-indexed prepare-sequential-kicker))
                (->> (:blog-post/_series series)
                     (sort-by :blog-post/published)
                     (reverse)
                     (map prepare-blog-post-teaser)
                     (map #(dissoc % :kicker))))})))

(defn render-page [req page]
  (if-let [f (case (:page/kind page)
               :page.kind/frontpage render-frontpage
               :page.kind/blog-post render-blog-post
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
