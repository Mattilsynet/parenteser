(ns parenteser.ingest
  (:require [datomic-type-extensions.api :as d]))

(defn create-tx [db file-name datas]
  (cond-> (vec datas)
    (re-find #"^blog\/" file-name)
    (assoc-in [0 :page/kind] :page.kind/blog-post)))
