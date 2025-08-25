(ns tasks
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
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

(defn git-config-user-email []
  (let [result (p/shell {:continue true :out :string}
                        "git config user.email")]
    (when (zero? (:exit result))
      (str/trim (:out result)))))

(comment
  ;; Hjelpere for å matche person/id med e-post i Git

  (->> (p/shell {:out :string} "git log --format=\"%ae\"")
       :out (str/split-lines) (into (sorted-set)))

  (->> (fs/list-dir "content/people")
       (map (comp :person/id read-string slurp fs/file))
       (into (sorted-set)))

  )

(def git-email->person-id
  {"christian.johansen@mattilsynet.no" :person/christian
   "magnar.sveen@mattilsynet.no" :person/magnar
   "mathias.iversen@mattilsynet.no" :person/mathias
   "sigmund.hansen@mattilsynet.no" :person/sigmund
   "teodor.lunaas.heggelund@mattilsynet.no" :person/teodor})

(def infer-author #(some-> (git-config-user-email) git-email->person-id))

(defn post-data->mapdown [page-data]
  (->> [:page/title
        :blog-post/author :blog-post/published :blog-post/tags
        :blog-post/description  :open-graph/description :blog-post/body]
       (map (fn [k]
              (cond
                (#{:page/title  :blog-post/published :blog-post/tags} k)
                (str (pr-str k) " " (pr-str (k page-data)))

                (= :blog-post/author k)
                (str (pr-str k) " " (pr-str {:person/id (k page-data)}))

                (#{:open-graph/description :blog-post/description :blog-post/body} k)
                (str (pr-str k) "\n\n" (get page-data k "") "\n\n"))))
       (str/join "\n")))

(defn create-post*
  ([slug] (create-post* slug {}))
  ([slug extra-opts]
   (binding [*print-namespace-maps* false]
     (let [file (str "content/blog-posts/" slug ".md")]
       (when (fs/exists? file)
         (throw (ex-info "File already exists"
                         {:file file})))
       (spit file
             (post-data->mapdown
              (merge {:blog-post/published (java.time.LocalDateTime/now)
                      :blog-post/tags []}
                     (when-let [author (infer-author)]
                       {:blog-post/author author})
                     (post-cli-opts->page-data extra-opts))))
       (println "Wrote" file)))))

(defn ^:export create-post
  {:org.babashka/cli {:args->opts [:slug]}}
  [{:keys [slug] :as opts}]
  (when-not slug
    (println "Error: slug is required for blog posts.")
    (println "Usage:")
    (println)
    (println "  bb create-post my-post")
    (System/exit 1))
  (create-post* slug opts))
