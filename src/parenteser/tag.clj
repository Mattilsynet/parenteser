(ns parenteser.tag
  (:require [datomic-type-extensions.api :as d]
            [parenteser.blog-posts :as blog-posts]
            [parenteser.elements :as e]
            [parenteser.layout :as layout]
            [parenteser.router :as router]
            [powerpack.markdown :as md]))

(defn get-tag-pages [db]
  (for [[locale eid]
        (d/q '[:find ?locale ?t
               :where
               [?p :blog-post/tags ?t]
               [?t :tag/id ?tag]
               [?p :page/locale ?locale]]
             db)]
    {:page/uri (router/get-tag-url (d/entity db eid))
     :page/locale locale
     :page/tag eid
     :page/kind :page.kind/tag}))

(comment

  (def db (d/db (:datomic/conn (powerpack.dev/get-app))))

  (get-tag-pages db)

)

(defn get-blog-posts [db tag-id locale]
  (->> (blog-posts/get-blog-posts db #{locale})
       (filter #(contains? (set (map :tag/id (:blog-post/tags %))) tag-id))))

(defn render-tag-page [page]
  (let [tag (:page/tag page)
        title (or (:tag/title tag)
                  (:tag/name tag))]
    (layout/layout
     {:title [:i18n ::layout/page-title {:title title}]}
     (layout/header {:href (router/get-frontpage-url page)})
     (if (or (:tag/description tag)
             (:tag/image tag))
       (e/info-section
        {:content
         [:div.media-front
          [:article.media-content
           [:h1.h3 title]
           [:p (md/render-html (:tag/description tag))]]
          [:aside.media-media
           [:img.img {:src (:tag/image tag)}]]]})
       [:div.section
        [:div.content
         [:h1.h1 [:i18n ::title tag]]]])
     (e/teaser-section
      {:teasers (->> (get-blog-posts (d/entity-db page) (:tag/id tag) (:page/locale page))
                     (map blog-posts/prepare-blog-post-teaser))}))))
