(ns parenteser.rss
  (:require [clojure.data.xml :as xml]
            [datomic-type-extensions.api :as d]
            [hiccup.core :refer [html]]
            [parenteser.pages :as pages]
            [powerpack.markdown :as md]))

(defn url [post]
  (str "https://parenteser.mattilsynet.no" (:page/uri post)))

(defn time-str [ldt]
  (str (.toOffsetDateTime
        (.atZone ldt #time/zid "Europe/Oslo"))))

(defn entry [post]
  [:entry
   [:title (:page/title post)]
   [:updated (time-str (:blog-post/published post))]
   [:author [:name (str (:person/given-name (:blog-post/author post))
                        " "
                        (:person/family-name (:blog-post/author post)))]]
  [:link {:href (url post)}]
   [:id (str "urn:parenteser.mattilsynet.no:feed:post:"
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
     [:id "urn:parenteser.mattilsynet.no:feed"]
     [:updated (time-str (:blog-post/published (first blog-posts)))]
     [:title {:type "text"} "Parenteser - Team Mat sin blogg"]
     [:link {:rel "self" :href "https://parenteser.mattilsynet.no/atom.xml"}]
     (map entry blog-posts)])))

(defn blog-post-feed [db]
  {:status 200
   :headers {"Content-Type" "application/atom+xml"}
   :body (atom-xml (pages/get-blog-posts db))})

(comment
  (def system integrant.repl.state/system)
  (def db (d/db (:datomic/conn (:powerpack/app system))))

  (def posts (pages/get-blog-posts db))

  (def post (first posts))

  (entry post)
  (atom-xml posts)

  )
