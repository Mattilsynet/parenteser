(ns parenteser.dev
  (:require [parenteser.core :as parenteser]
            [powerpack.app :as app]))

(comment

  (def app (parenteser/create-app))

  (app/start (assoc-in app [:config :site/base-url] ""))
  (app/stop)
  (app/reset)

  integrant.repl.state/system

  )
