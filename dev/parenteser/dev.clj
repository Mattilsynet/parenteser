(ns parenteser.dev
  (:require [babashka.fs :as fs]
            [clojure.java.browse]
            [parenteser.core :as parenteser]
            [powerpack.dev :as dev :refer [reset]]
            [powerpack.export :as export]
            [tasks]))

(defmethod dev/configure! :default []
  (parenteser/create-app))

(defn start []
  (set! *print-namespace-maps* false)
  (dev/start))

(comment ;; s-:
  (dev/stop)
  (start)
  (reset)
  (clojure.java.browse/browse-url "http://localhost:5052")
  (let [f "bakoverkompatibilitet-med-data"]
    (fs/delete-if-exists (str "content/blog-posts/" f ".md"))
    (tasks/create-post* f))
  )

(comment

  (export/export (parenteser/create-app))

  (dev/get-app)

  )
