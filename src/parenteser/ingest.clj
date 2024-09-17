(ns parenteser.ingest
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [datomic-type-extensions.api :as d]
            [html5-walker.core :as html5-walker]
            [parenteser.tag :as tag]
            [powerpack.markdown :as md]))

(defn update-in-existing [m path & args]
  (if-not (nil? (get-in m path))
    (apply update-in m path args)
    m))

(defn reify-tags [tags]
  (for [id tags]
    {:tag/id id}))

(defn get-open-graph-image [blog-post]
  (or (:open-graph/image blog-post)
      (some-> (:blog-post/body blog-post)
              md/render-html
              str
              (html5-walker/find-nodes [:img])
              first
              (.getAttribute "src"))
      (some->> blog-post
               :blog-post/author
               :person/open-graph-photo)
      (some->> blog-post
               :blog-post/author
               :person/photo)))

(defn ingest-blog-post [blog-post]
  (let [locale (or (:page/locale blog-post) :nb)]
    (-> blog-post
        (assoc :page/kind :page.kind/blog-post)
        (assoc :page/locale locale)
        (update-in-existing [:page/uri] (fn [uri]
                                          (str (when (not= :nb locale)
                                                 (str "/" (name locale)))
                                               (str/replace uri #"^/blog-posts(-en)?" ""))))
        (update-in-existing [:blog-post/tags] reify-tags)
        (update :open-graph/title #(or % (:page/title blog-post)))
        (update :open-graph/description #(or % (:blog-post/description blog-post))))))

(defn ingest-blog-post-pages [blog-post]
  (let [page (ingest-blog-post blog-post)]
    (into [page]
          (for [alt-uri (:page/alt-uris blog-post)]
            {:page/redirect-uri (:page/uri page)
             :page/uri alt-uri}))))

(defn create-tx [file-name datas]
  (cond->> datas
    (re-find #"^blog-posts(-en)?\/" file-name)
    (mapcat ingest-blog-post-pages)))

(defn get-tag-name-fixes [db]
  (for [tag (->> (d/q '[:find [?e ...]
                        :where
                        [?e :tag/id]]
                      db)
                 (map #(d/entity db %))
                 (remove :tag/name))]
    [:db/add (:db/id tag) :tag/name
     (str/capitalize (str/replace (name (:tag/id tag)) #"-" " "))]))

(defn suggest-og-image [blog-post]
  (when-let [image (get-open-graph-image blog-post)]
    [:db/add (:db/id blog-post) :open-graph/image image]))

(defn on-ingested [powerpack _]
  (let [conn (:datomic/conn powerpack)]
    (when-let [txes (get-tag-name-fixes (d/db conn))]
      @(d/transact conn txes))
    (let [tag-pages (tag/get-tag-pages (d/db conn))
          db (d/db conn)]
      (doseq [page tag-pages]
        (when-let [existing (d/entity db [:page/uri (:page/uri page)])]
          (when-not (= (-> existing :page/tag :db/id) (:page/tag page))
            (throw (ex-info (str "Tag page shares :page/uri with an existing page. "
                                 "Either rename the tag or give it an explicit :tag/slug "
                                 "in content/tags.edn")
                            {:tag-page page
                             :existing-page (into {} existing)})))))
      @(d/transact conn tag-pages))
    (let [db (d/db conn)]
      (some->> (d/q '[:find [?e ...]
                      :where
                      [?e :page/kind :page.kind/blog-post]
                      (not [?e :open-graph/image])]
                    db)
               (map #(d/entity db %))
               (keep suggest-og-image)
               seq
               (d/transact conn)
               deref))))

(comment

  (def system integrant.repl.state/system)
  (def db (d/db (:datomic/conn system)))

  (->> (d/q '[:find [?e ...]
              :where
              [?e :page/kind :page.kind/blog-post]
              (not [?e :open-graph/image])]
            db)
       (map #(d/entity db %))
       (map d/touch))

  (get-tag-name-fixes db)

  (d/q '[:find ?u ?t
         :where
         [?e :page/title]
         [?e :page/uri ?u]
         [?e :blog-post/tags ?t]]
       db)

)
