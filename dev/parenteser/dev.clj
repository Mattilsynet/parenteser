(ns parenteser.dev
  (:require [integrant.core :as ig]
            [parenteser.core :as parenteser]
            [powerpack.dev :as dev]
            [powerpack.export :as export]))

(defmethod ig/init-key :powerpack/powerpack [_ _]
  (parenteser/create-app))

(comment

  (set! *print-namespace-maps* false)

  (dev/start)
  (dev/stop)
  (dev/reset)

  (export/export (parenteser/create-app))

  integrant.repl.state/system

  )
