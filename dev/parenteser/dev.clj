(ns parenteser.dev
  (:require [parenteser.core :as parenteser]
            [powerpack.dev :as dev :refer [reset]]
            [powerpack.export :as export]))

(defmethod dev/configure! :default []
  (parenteser/create-app))

(defn start []
  (set! *print-namespace-maps* false)
  (dev/start))

(comment

  (dev/stop)
  (start)
  (reset)

  (export/export (parenteser/create-app))

  (dev/get-app)

  )
