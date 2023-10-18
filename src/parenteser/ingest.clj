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
              (.getAttribute "src"))))

(defn ingest-blog-post [blog-post]
  (let [og-image (get-open-graph-image blog-post)]
    (cond-> (-> blog-post
                (assoc :page/kind :page.kind/blog-post)
                (update-in-existing [:page/uri] str/replace #"^/blog-posts" "")
                (update-in-existing [:blog-post/tags] reify-tags)
                (update :open-graph/title #(or % (:page/title blog-post)))
                (update :open-graph/description #(or % (:blog-post/description blog-post))))
      og-image (assoc :open-graph/image og-image))))

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

(defn on-ingested [{:keys [conn]}]
  (when-let [txes (get-tag-name-fixes (d/db conn))]
    @(d/transact conn txes)))

(comment

  (def system integrant.repl.state/system)
  (def db (d/db (:datomic/conn system)))

  (-> (d/entity db [:page/uri "/blog-posts/byggeklosser-for-sok/"])
      :blog-post/description
      md/render-html
      ;;(html5-walker.core/find-nodes [:p])
      )

  (get-tag-name-fixes db)

  (d/q '[:find ?u ?t
         :where
         [?e :page/title]
         [?e :page/uri ?u]
         [?e :blog-post/tags ?t]]
       db)

)
