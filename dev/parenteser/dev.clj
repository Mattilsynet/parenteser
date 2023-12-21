(ns parenteser.dev
  (:require [parenteser.core :as parenteser]
            [powerpack.dev :as dev]
            [powerpack.export :as export]))

(defmethod dev/configure! :default []
  (parenteser/create-app))

(comment

  (set! *print-namespace-maps* false)

  (dev/start)
  (dev/reset)

  (export/export (parenteser/create-app))

  (dev/get-app)

  )
