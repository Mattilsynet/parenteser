(ns parenteser.rss
  (:require [clojure.data.xml :as xml]
            [datomic-type-extensions.api :as d]
            [hiccup.core :refer [html]]
            [parenteser.blog-posts :as blog-posts]
            [powerpack.markdown :as md])
  (:import [java.time ZoneId]))

(defn url [post]
  (str "https://parenteser.mattilsynet.io" (:page/uri post)))

(defn time-str [ldt]
  (str (.toOffsetDateTime
        (.atZone ldt (ZoneId/of "Europe/Oslo")))))

(defn entry [post]
  [:entry
   [:title (:page/title post)]
   [:updated (time-str (:blog-post/published post))]
   [:author [:name (str (:person/given-name (:blog-post/author post))
                        " "
                        (:person/family-name (:blog-post/author post)))]]
  [:link {:href (url post)}]
   [:id (str "urn:parenteser.mattilsynet.io:feed:post:"
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
     [:id "urn:parenteser.mattilsynet.io:feed"]
     [:updated (time-str (:blog-post/published (first blog-posts)))]
     [:title {:type "text"} "Parenteser - Team Mat sin blogg"]
     [:link {:rel "self" :href "https://parenteser.mattilsynet.io/atom.xml"}]
     (map entry blog-posts)])))

(defn blog-post-feed [db]
  {:status 200
   :headers {"Content-Type" "application/atom+xml"}
   :body (atom-xml (blog-posts/get-blog-posts db))})

(comment
  (def system integrant.repl.state/system)
  (def db (d/db (:datomic/conn (:powerpack/app system))))

  (def posts (blog-posts/get-blog-posts db))

  (def post (first posts))

  (entry post)
  (atom-xml posts)

  )
