(ns parenteser.blog-posts
  (:require [datomic-type-extensions.api :as d]
            [powerpack.markdown :as md]))

(defn get-blog-posts
  ([db]
   (get-blog-posts db nil))
  ([db locales]
   (->> (d/q '[:find [?e ...]
               :in $ [?locales ...]
               :where
               [?e :page/uri]
               [?e :page/kind :page.kind/blog-post]
               [?e :page/locale ?locales]]
             db
             (cond
               (keyword? locales) #{locales}
               (nil? locales) #{:nb :en}
               :else locales))
        (map #(d/entity db %))
        (sort-by :blog-post/published)
        reverse)))

(defn prepare-tags [tags]
  (seq (for [tag tags]
         (:tag/name tag))))

(defn get-blog-post-vcard [{:blog-post/keys [author tags vcard-photo]}]
  {:image (some->> (or vcard-photo (:person/photo author))
                   (str "/round-small"))
   :image-alt (:person/given-name author)
   :title (:person/given-name author)
   :body (when-let [tags (prepare-tags tags)]
           [:i18n ::vcard-tags {:tags tags}])})

(defn prepare-blog-post-teaser [{:blog-post/keys [description published series]
                                 :page/keys [title uri]
                                 :as blog-post}]
  (cond-> {:title {:text title
                   :uri uri}
           :kicker {:text (some-> (:series/name series) (str ": "))
                    :uri (:page/uri series)}
           :description (md/render-html description)
           :aside (get-blog-post-vcard blog-post)
           :kind :teaser-article}
    published (assoc :footer [:i18n :datetime/short-date published])))
