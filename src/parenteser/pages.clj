(ns parenteser.pages
  (:require [datomic-type-extensions.api :as d]
            [powerpack.html :as html]
            [powerpack.markdown :as md]))

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

  (->> [:page/uri "/blog/byggeklosser-for-sok/"]
       (d/entity (d/db (:datomic/conn system)))
       :blog-post/author
       :person/photo)

)
