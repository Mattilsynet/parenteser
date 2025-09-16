(ns parenteser.dev
  (:require [clojure.java.browse :refer [browse-url]]
            [parenteser.core :as parenteser]
            [powerpack.dev :as dev :refer [reset]]
            [powerpack.export :as export]
            [tasks]))

(defmethod dev/configure! :default []
  (parenteser/create-app))

(defn start []
  (set! *print-namespace-maps* false)
  (dev/start))

(def parenteser-localhost "http://localhost:5052")

(comment ;; s-:
  (dev/stop)
  (start)
  (reset)
  (browse-url parenteser-localhost)

  )

(comment

  (export/export (parenteser/create-app))

  (dev/get-app)

  (tasks/create-post* "bakoverkompatibilitet-med-data")

  )
