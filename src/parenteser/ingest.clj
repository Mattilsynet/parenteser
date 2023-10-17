(ns parenteser.ingest
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]))

(defn update-in-existing [m path & args]
  (if-not (nil? (get-in m path))
    (apply update-in m path args)
    m))

(defn reify-tags [tags]
  (for [id tags]
    {:tag/id id}))

(defn ingest-blog-post [blog-post]
  (-> blog-post
      (assoc :page/kind :page.kind/blog-post)
      (update-in-existing [:blog-post/tags] reify-tags)))

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

  (get-tag-name-fixes db)

  (d/q '[:find ?e ?t
         :where
         [?e :page/title]
         [?e :blog-post/tags ?t]]
       db)

)
