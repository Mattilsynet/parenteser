(ns parenteser.core
  (:require [parenteser.ingest :as ingest]
            [parenteser.pages :as pages]
            [powerpack.highlight :as highlight]))

(defn create-app []
  (-> {:config
       {:site/base-url "https://parenteser.mattilsynet.io"
        :site/default-language "no"
        :site/title "Parenteser"

        :stasis/build-dir "build"
        :powerpack/content-dir "resources/content"
        :powerpack/source-dirs ["src" "dev"]
        :powerpack/resource-dirs ["resources"]
        :powerpack/db "datomic:mem://parenteser"

        :optimus/assets [{:public-dir "public"
                          :paths [#"/images/*.*"
                                  #"/fonts/*"]}]
        :optimus/bundles {"styles.css"
                          {:public-dir "public"
                           :paths ["/css/parenteser.css"]}}

        :powerpack.server/port 5052

        :imagine/config {:prefix "image-assets"
                         :resource-path "public"
                         :disk-cache? true
                         :transformations
                         {:vcard-small
                          {:transformations [[:fit {:width 184 :height 184}]
                                             [:crop {:preset :square}]]
                           :retina-optimized? true
                           :retina-quality 0.4
                           :width 184}}}

        :datomic/schema-file "resources/schema.edn"}
       :create-ingest-tx #'ingest/create-tx
       :render-page #'pages/render-page}
      highlight/install))

