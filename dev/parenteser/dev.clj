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

  )

(comment

  (export/export (parenteser/create-app))

  (dev/get-app)

  (tasks/create-post* "bakoverkompatibilitet-med-data")

  )
