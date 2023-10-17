(ns parenteser.dev
  (:require [integrant.core :as ig]
            [parenteser.core :as parenteser]
            [powerpack.app :as app]))

(defmethod ig/init-key :powerpack/app [_ _]
  (-> (parenteser/create-app)
      (assoc-in [:config :site/base-url] nil)))

(comment

  (set! *print-namespace-maps* false)

  (app/start)
  (app/stop)
  (app/reset)

  integrant.repl.state/system

  )
