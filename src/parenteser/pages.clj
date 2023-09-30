(ns parenteser.pages
  (:require [powerpack.html :as html]
            [powerpack.markdown :as md]
            [imagine.core :as imagine]
            [datomic-type-extensions.api :as d]))

(defn get-blog-posts [db]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :page/uri]
              [?e :page/kind :page.kind/blog-post]]
            db)
       (map #(d/entity db %))))

(defn render-frontpage [req page]
  (html/render-hiccup
   req
   page
   [:div.section
    [:div.content.text-content
     [:h1 "Parenteser"]
     [:p "Beretninger fra Mat-teamets grønne enger"]
     [:ul
      (for [blog-post (get-blog-posts (d/entity-db page))]
        [:li [:a {:href (:page/uri blog-post)}
              (:page/title blog-post)]])]]]))

(defn render-blog-post [req blog-post]
  (html/render-hiccup
   req
   blog-post
   [:div.section
    [:div.content.text-content
     [:h1.h1 (:page/title blog-post)]
     (md/to-html (:blog-post/body blog-post))
     [:img {:src "/vcard-small/images/christian.jpg"}]]]))

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
