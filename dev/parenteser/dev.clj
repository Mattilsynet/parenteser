(ns parenteser.dev
  (:require [integrant.core :as ig]
            [parenteser.core :as parenteser]
            [powerpack.app :as app]
            [powerpack.export :as export]))

(defmethod ig/init-key :powerpack/app [_ _]
  (-> (parenteser/create-app)
      (assoc-in [:config :site/base-url] nil)))

(comment

  (set! *print-namespace-maps* false)

  (app/start)
  (app/stop)
  (app/reset)

  (export/export (parenteser/create-app))

  integrant.repl.state/system

  )
