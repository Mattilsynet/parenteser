(ns tasks
  (:require [babashka.fs :as fs]
            [clojure.string :as str]))

(defmethod print-method java.time.LocalDateTime [ldt writer]
  (doto writer
    (.write "#time/ldt ")
    (.write (str \" ldt \"))))

(defn post-cli-opts->page-data [opts]
  (cond-> {}
    (:title opts)
    (assoc :post/title (:title opts))

    (:author opts)
    (assoc :blog-post/author {:person/id (:author opts)})

    (:published opts)
    (assoc :blog-post/published (java.time.LocalDateTime/parse (:published opts)))

    (:tags opts)
    (assoc :blog-post/tags (:tags opts))))

(defn post-data->mapdown [page-data]
  (->> [:page/title
        :blog-post/author :blog-post/published :blog-post/tags
        :blog-post/description  :open-graph/description :blog-post/body]
       (map (fn [k]
              (cond
                (#{:page/title :blog-post/author :blog-post/published :blog-post/tags} k)
                (str (pr-str k) " " (pr-str (k page-data)))

                (#{:open-graph/description :blog-post/description :blog-post/body} k)
                (str (pr-str k) "\n\n" (get page-data k "") "\n\n"))))
       (str/join "\n")))

(defn ^:export create-post
  {:org.babashka/cli {:args->opts [:slug]}}
  [{:keys [slug] :as opts}]
  (when-not slug
    (println "Error: slug is required for blog posts.")
    (println "Usage:")
    (println)
    (println "  bb create-post my-post")
    (System/exit 1))
  (binding [*print-namespace-maps* false]
    (let [file-name (str "content/blog-posts/" slug ".md")]
      (when (fs/exists? file-name)
        (println "Error:" file-name "already exits")
        (System/exit 1))
      (spit file-name
            (-> {:blog-post/published (java.time.LocalDateTime/now)
                 :blog-post/tags []}
                (merge (post-cli-opts->page-data opts))
                post-data->mapdown))
      (println "Wrote" file-name))))
