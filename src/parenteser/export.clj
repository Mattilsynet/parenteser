(ns parenteser.export
  (:require [parenteser.core :as parenteser]
            [powerpack.export :as export]))

(defn ^:export export [& _args]
  (export/export (parenteser/create-app)))
