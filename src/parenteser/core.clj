(ns parenteser.core
  (:require [datomic-type-extensions.types :refer [define-dte]]
            [parenteser.i18n :as i18n]
            [parenteser.ingest :as ingest]
            [parenteser.pages :as pages]
            [powerpack.highlight :as highlight]))

(define-dte :i18n/edn :db.type/string
  [this] (pr-str this)
  [^String s] (read-string s))

(defn create-app []
  (-> {:site/base-url "https://parenteser.mattilsynet.io"
       :site/default-locale :nb
       :site/title "Parenteser"

       :powerpack/build-dir "docker/build"
       :powerpack/content-dir "content"
       :powerpack/source-dirs ["src" "dev"]
       :powerpack/resource-dirs ["resources"]
       :datomic/uri "datomic:mem://parenteser"
       :datomic/schema-file "resources/schema.edn"

       :optimus/assets [{:public-dir "public"
                         :paths [#"/*.(png|ico)"
                                 #"/images/*.*"
                                 #"/fonts/*"]}]
       :optimus/bundles {"styles.css"
                         {:public-dir "public"
                          :paths ["/css/parenteser.css"]}}

       :powerpack/port 5052

       :imagine/config {:prefix "image-assets"
                        :resource-path "public"
                        :disk-cache? true
                        :transformations
                        {:round-small
                         {:transformations [[:fit {:width 184 :height 184}]
                                            [:crop {:preset :square}]]
                          :retina-optimized? true
                          :retina-quality 0.4
                          :width 184}}}

       :m1p/dictionaries {:nb ["src/parenteser/i18n/nb.edn"]
                          :en ["src/parenteser/i18n/en.edn"]}
       :m1p/dictionary-fns {:fn/enumerate #'i18n/m1p-fn-enumerate
                            :fn/format-date #'i18n/m1p-fn-format-date}

       :powerpack/create-ingest-tx #'ingest/create-tx
       :powerpack/render-page #'pages/render-page
       :powerpack/on-ingested #'ingest/on-ingested}
      highlight/install))
