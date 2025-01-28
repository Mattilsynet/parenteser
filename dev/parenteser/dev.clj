(ns parenteser.dev
  (:require [parenteser.core :as parenteser]
            [powerpack.dev :as dev :refer [reset]]
            [powerpack.export :as export]
            clojure.java.browse))

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

  )
