(ns parenteser.rss
  (:require [clojure.data.xml :as xml]
            [datomic-type-extensions.api :as d]
            [hiccup.core :refer [html]]
            [parenteser.blog-posts :as blog-posts]
            [powerpack.markdown :as md])
  (:import [java.time ZoneId]
           [java.time.format DateTimeFormatter]))

(defn url [post]
  (str "https://parenteser.mattilsynet.io" (:page/uri post)))

(defn time-str [ldt]
  (-> ldt
      (.atZone (ZoneId/of "Europe/Oslo"))
      .toOffsetDateTime
      (.format DateTimeFormatter/ISO_OFFSET_DATE_TIME)))

(defn entry [post]
  [:entry
   [:title (:page/title post)]
   [:updated (time-str (:blog-post/published post))]
   [:author [:name (str (:person/given-name (:blog-post/author post))
                        " "
                        (:person/family-name (:blog-post/author post)))]]
   [:link {:href (url post)}]
   [:id (str "tag:parenteser.mattilsynet.io,2023:post-"
             (.toLocalDate (:blog-post/published post)))]
   [:content {:type "html"}
    (html
     [:div
      [:div (md/render-html (:blog-post/description post))]
      [:p [:a {:href (url post)}
           "Les artikkelen"]]])]])

(defn atom-xml [blog-posts]
  (xml/emit-str
   (xml/sexp-as-element
    [:feed {:xmlns "http://www.w3.org/2005/Atom"
            :xmlns:media "http://search.yahoo.com/mrss/"}
     [:id "tag:parenteser.mattilsynet.io,2023:feed"]
     [:updated (time-str (:blog-post/published (first blog-posts)))]
     [:title {:type "text"} "Parenteser - Team Servering sin blogg"]
     [:link {:rel "self" :href "https://parenteser.mattilsynet.io/atom.xml"}]
     (map entry blog-posts)])))

(defn blog-post-feed [page]
  {:status 200
   :headers {"Content-Type" "application/atom+xml"}
   :body (-> (d/entity-db page)
             (blog-posts/get-blog-posts (:page/locale page))
             atom-xml)})

(comment
  (def system integrant.repl.state/system)
  (def db (d/db (:datomic/conn (:powerpack/app system))))

  (def posts (blog-posts/get-blog-posts db :nb))

  (def post (first posts))

  (entry post)
  (atom-xml posts)

  )
