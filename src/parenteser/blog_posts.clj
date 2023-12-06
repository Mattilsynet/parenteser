(ns parenteser.blog-posts
  (:require [datomic-type-extensions.api :as d]))

(defn get-blog-posts [db]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :page/uri]
              [?e :page/kind :page.kind/blog-post]]
            db)
       (map #(d/entity db %))
       (sort-by :blog-post/published)
       reverse))
