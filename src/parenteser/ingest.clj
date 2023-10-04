(ns parenteser.ingest
  (:require [datomic-type-extensions.api :as d]))

(defn ingest-blog-post [blog-post]
  (assoc blog-post :page/kind :page.kind/blog-post))

(defn create-tx [_db file-name datas]
  (cond->> datas
    (re-find #"^blog-posts\/" file-name)
    (map ingest-blog-post)))

(comment

  (def system integrant.repl.state/system)
  (def db (d/db (:datomic/conn system)))

  (d/q '[:find ?e
         :where
         [?e :blog-post/title]]
       db)

)
