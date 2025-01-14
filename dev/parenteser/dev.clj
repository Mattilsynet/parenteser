(ns parenteser.dev
  (:require [parenteser.core :as parenteser]
            [powerpack.dev :as dev :refer [reset]]
            [powerpack.export :as export]))

(defmethod dev/configure! :default []
  (parenteser/create-app))

(defn start []
  (set! *print-namespace-maps* false)
  (dev/start))

(comment ;; s-:
  (dev/stop)
  (start)
  (reset)
  )

(comment

  (export/export (parenteser/create-app))

  (dev/get-app)

  )
