(ns parenteser.dev
  (:require [integrant.core :as ig]
            [parenteser.core :as parenteser]
            [powerpack.app :as app]))

(defmethod ig/init-key :powerpack/app [_ _]
  (-> (parenteser/create-app)
      (assoc-in [:config :site/base-url] nil)))

(comment

  (app/start)
  (app/stop)
  (app/reset)

  (def system integrant.repl.state/system)

  )
