(ns parenteser.core
  (:require [parenteser.ingest :as ingest]
            [parenteser.pages :as pages]
            [powerpack.highlight :as highlight]))

(defn create-app []
  (-> {:site/base-url "https://parenteser.mattilsynet.io"
       :site/default-locale :no
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
                        {:vcard-small
                         {:transformations [[:fit {:width 184 :height 184}]
                                            [:crop {:preset :square}]]
                          :retina-optimized? true
                          :retina-quality 0.4
                          :width 184}}}

       :powerpack/create-ingest-tx #'ingest/create-tx
       :powerpack/render-page #'pages/render-page
       :powerpack/on-ingested #'ingest/on-ingested}
      highlight/install))
