(ns parenteser.ingest
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [html5-walker.core :as html5-walker]
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
              (html5-walker/find-nodes [:img])
              first
              (.getAttribute "src"))
      (some->> blog-post
               :blog-post/author
               :person/photo
               (str "/vcard-open-graph"))))

(defn ingest-blog-post [blog-post]
  (-> blog-post
      (assoc :page/kind :page.kind/blog-post)
      (update-in-existing [:page/uri] str/replace #"^/blog-posts" "")
      (update-in-existing [:blog-post/tags] reify-tags)
      (update :open-graph/title #(or % (:page/title blog-post)))
      (update :open-graph/description #(or % (:blog-post/description blog-post)))))

(defn create-tx [file-name datas]
  (cond->> datas
    (re-find #"^blog-posts\/" file-name)
    (map ingest-blog-post)))

(defn get-tag-name-fixes [db]
  (for [tag (->> (d/q '[:find [?e ...]
                        :where
                        [?e :tag/id]]
                      db)
                 (map #(d/entity db %))
                 (remove :tag/name))]
    [:db/add (:db/id tag) :tag/name (str/capitalize (name (:tag/id tag)))]))

(defn suggest-og-image [blog-post]
  (when-let [image (get-open-graph-image blog-post)]
    [:db/add (:db/id blog-post) :open-graph/image image]))

(defn on-ingested [{:keys [conn]}]
  (when-let [txes (get-tag-name-fixes (d/db conn))]
    @(d/transact conn txes))
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
             deref)))

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
