(ns parenteser.core
  (:require [parenteser.ingest :as ingest]
            [parenteser.pages :as pages]
            [powerpack.app :as app]
            [powerpack.export :as export]
            [powerpack.highlight :as highlight]))

(def config
  {:site/base-url "https://parenteser.mattilsynet.io"
   :site/default-language "no"
   :site/title "Parenteser"

   :stasis/build-dir "build"
   :powerpack/content-dir "resources"
   :powerpack/source-dirs ["src" "dev"]
   :powerpack/db "datomic:mem://parenteser"

   :optimus/assets [{:public-dir "public"
                     :paths [#"/images/*.*"]}]
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

   :datomic/schema [{:db/ident :page/king
                     :db/valueType :db.type/keyword
                     :db/unique :db.unique/identity
                     :db/cardinality :db.cardinality/one}

                    {:db/ident :tag/id
                     :db/valueType :db.type/keyword
                     :db/unique :db.unique/identity
                     :db/cardinality :db.cardinality/one}

                    {:db/ident :tag/name
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one}

                    {:db/ident :blog-post/title
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one}

                    {:db/ident :blog-post/description
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one}

                    {:db/ident :blog-post/published
                     :db/valueType :db.type/instant
                     :db/cardinality :db.cardinality/one}

                    {:db/ident :blog-post/updated
                     :db/valueType :db.type/instant
                     :db/cardinality :db.cardinality/one}

                    {:db/ident :blog-post/tags
                     :db/valueType :db.type/ref
                     :db/cardinality :db.cardinality/many}

                    {:db/ident :blog-post/image
                     :db/valueType :db.type/ref
                     :db/cardinality :db.cardinality/one}

                    {:db/ident :blog-post/body
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one}]})

(defn create-app []
  (-> {:config config
       :create-ingest-tx #'ingest/create-tx
       :render-page #'pages/render-page}
      app/create-app
      highlight/install))