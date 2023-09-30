(ns parenteser.ingest
  (:require [datomic-type-extensions.api :as d]
            [parenteser.parens :refer [update-in-existing]]))

(defn mkref [s]
  {:person/id (read-string s)})

(defn ingest-blog-post [blog-post]
  (-> blog-post
      (assoc :page/kind :page.kind/blog-post)
      (update-in-existing [:blog-post/author] mkref)
      (update-in-existing [:blog-post/published] read-string)))

(defn create-tx [db file-name datas]
  (cond->> datas
    (re-find #"^blog\/" file-name)
    (map ingest-blog-post)))

(comment

  (def system integrant.repl.state/system)
  (def db (d/db (:datomic/conn system)))

  (d/q '[:find ?e
         :where
         [?e :blog-post/title]]
       db)

)
